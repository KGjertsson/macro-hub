package com.kg.macroanalyzer.acceptance;

import com.github.dockerjava.api.exception.NotFoundException;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

public final class AcceptanceStack implements AutoCloseable {

    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String POSTGRES_ALIAS = "postgres";
    private static final String POSTGRES_SUPERUSER = "macrouser";
    private static final String POSTGRES_INIT_PROPERTY = "acceptance.postgres-init";
    private static final String POSTGRES_INIT_DEFAULT = "../macro-infra/docker/postgres-init";

    private static final String WIREMOCK_IMAGE = "wiremock/wiremock:3.13.2";
    private static final String SOURCES_ALIAS = "sources";
    private static final int SOURCES_PORT = 8080;
    private static final String SOURCES_URL = "http://%s:%d".formatted(SOURCES_ALIAS, SOURCES_PORT);

    private static final String GATEWAY_IMAGE_PROPERTY = "acceptance.image.gateway";
    private static final String GATEWAY_DEFAULT_IMAGE = "macro-frontend:acceptance";
    private static final int GATEWAY_PORT = 80;

    private static final String HEALTH_PATH = "/actuator/health";
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);

    private final Network network = Network.newNetwork();
    private final List<GenericContainer<?>> containers = new CopyOnWriteArrayList<>();
    private final GenericContainer<?> postgres;
    private final Gateway gateway;
    private final Sources sources;

    // Testcontainers' Ryuk removes the containers and the network when the test JVM exits, so it is never closed here
    private static final class Holder {
        private static final AcceptanceStack RUNNING = new AcceptanceStack(BackendService.ALL);
    }

    public static AcceptanceStack running() {
        return Holder.RUNNING;
    }

    public static AcceptanceStack startWith(List<BackendService> services) {
        return new AcceptanceStack(services);
    }

    private AcceptanceStack(List<BackendService> services) {
        try {
            this.postgres = postgres();
            final var wireMock = wireMock();
            containers.addAll(List.of(postgres, wireMock));
            Startables.deepStart(postgres, wireMock).join();
            startAll(services);
            final var frontend = startGateway();

            this.gateway = new Gateway(baseUrl(frontend, GATEWAY_PORT));
            this.sources = new Sources(new Gateway(baseUrl(wireMock, SOURCES_PORT)));
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    public void startLater(BackendService service) {
        startWithSourcesRedirected(service);
    }

    // GenericContainer.stop() would remove the container, and with it the anonymous volume that holds the data
    public void stopPostgres() {
        postgres.getDockerClient().stopContainerCmd(postgres.getContainerId()).exec();
    }

    public void startStoppedPostgres() {
        postgres.getDockerClient().startContainerCmd(postgres.getContainerId()).exec();
    }

    public Gateway gateway() {
        return gateway;
    }

    public Sources sources() {
        return sources;
    }

    @Override
    public void close() {
        containers.reversed().forEach(GenericContainer::stop);
        network.close();
    }

    private GenericContainer<?> postgres() {
        return new GenericContainer<>(POSTGRES_IMAGE)
                .withNetwork(network)
                .withNetworkAliases(POSTGRES_ALIAS)
                .withEnv("POSTGRES_DB", "postgres")
                .withEnv("POSTGRES_USER", POSTGRES_SUPERUSER)
                .withEnv("POSTGRES_PASSWORD", "macropassword")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(postgresInitScripts()),
                        "/docker-entrypoint-initdb.d/")
                // Twice, because the image's init scripts run on a temporary server that also logs this
                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2)
                        .withStartupTimeout(STARTUP_TIMEOUT));
    }

    private Path postgresInitScripts() {
        final var scripts = Path.of(System.getProperty(POSTGRES_INIT_PROPERTY, POSTGRES_INIT_DEFAULT));
        if (!Files.isDirectory(scripts)) {
            throw new IllegalStateException("No postgres-init directory at %s. Run from macro-backend/acceptance-tests, or set -D%s"
                    .formatted(scripts.toAbsolutePath(), POSTGRES_INIT_PROPERTY));
        }

        return scripts;
    }

    private GenericContainer<?> wireMock() {
        return new GenericContainer<>(WIREMOCK_IMAGE)
                .withNetwork(network)
                .withNetworkAliases(SOURCES_ALIAS)
                .withExposedPorts(SOURCES_PORT)
                .waitingFor(Wait.forHttp("/__admin/health").withStartupTimeout(STARTUP_TIMEOUT));
    }

    // allOf waits for every start, also after one has failed, so close() can't miss a container that is still starting
    private void startAll(List<BackendService> services) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture.allOf(services.stream()
                    .map(service -> CompletableFuture.runAsync(
                            () -> startWithSourcesRedirected(service), executor))
                    .toArray(CompletableFuture[]::new)).join();
        }
    }

    // Flyway seeds series_configuration with the real sources' URLs, and a service only reads it at start-up. So it starts
    // once to seed, and again to read the rows pointing at the stub. The first start has no reason to call anything: the
    // scheduler is off and nothing has been enqueued
    private void startWithSourcesRedirected(BackendService service) {
        final var container = serviceContainer(service);
        containers.add(container);
        container.start();
        redirectSources(service);
        container.stop();
        container.start();
    }

    private GenericContainer<?> serviceContainer(BackendService service) {
        return new GenericContainer<>(requireBuilt(service.image()))
                .withNetwork(network)
                .withNetworkAliases(service.networkAlias())
                .withEnv("SPRING_DATASOURCE_URL", service.jdbcUrl(POSTGRES_ALIAS))
                .withEnv("SPRING_DATASOURCE_USERNAME", service.database())
                .withEnv("SPRING_DATASOURCE_PASSWORD", service.databasePassword())
                .withEnv("SCHEDULER_ENABLED", "false")
                .withEnv(service.environment())
                .withExposedPorts(service.port())
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(service.networkAlias())))
                .waitingFor(Wait.forHttp(HEALTH_PATH).withStartupTimeout(STARTUP_TIMEOUT));
    }

    private void redirectSources(BackendService service) {
        psql(service.database(), """
                UPDATE series_configuration
                SET scrape_url = regexp_replace(scrape_url, '^https?://[^/]+', '%s')""".formatted(SOURCES_URL));
        final var stillReal = psql(service.database(), """
                SELECT count(*) FROM series_configuration WHERE scrape_url !~ '^%s(/|$)'""".formatted(SOURCES_URL));
        if (!"0".equals(stillReal.strip())) {
            throw new IllegalStateException(
                    "%s configs of %s still point at a real source".formatted(stillReal.strip(), service.database()));
        }
    }

    private String psql(String database, String sql) {
        try {
            final var result = postgres.execInContainer(
                    "psql", "-U", POSTGRES_SUPERUSER, "-d", database, "-v", "ON_ERROR_STOP=1", "-tAc", sql);
            if (result.getExitCode() != 0) {
                throw new IllegalStateException("psql failed on %s: %s".formatted(sql, result.getStderr()));
            }
            return result.getStdout();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    // nginx resolves an upstream per request, so the routes of a service that isn't running answer 502, don't stop it
    // starting, and work once the service has started under its network alias
    private GenericContainer<?> startGateway() {
        final var image = System.getProperty(GATEWAY_IMAGE_PROPERTY, GATEWAY_DEFAULT_IMAGE);
        final GenericContainer<?> frontend = new GenericContainer<>(requireBuilt(image))
                .withNetwork(network)
                .withExposedPorts(GATEWAY_PORT)
                .waitingFor(Wait.forHttp("/").withStartupTimeout(STARTUP_TIMEOUT));
        BackendService.ALL.forEach(service -> frontend.withEnv(service.gatewayUrlVariable(), service.internalUrl()));
        containers.add(frontend);
        frontend.start();

        return frontend;
    }

    private String requireBuilt(String image) {
        try {
            DockerClientFactory.lazyClient().inspectImageCmd(image).exec();
            return image;
        } catch (NotFoundException e) {
            throw new IllegalStateException(
                    "No image %s. Build it with macro-backend/macro-infra/scripts/acceptance-test.sh".formatted(image), e);
        }
    }

    private String baseUrl(GenericContainer<?> container, int port) {
        return "http://%s:%d".formatted(container.getHost(), container.getMappedPort(port));
    }

}
