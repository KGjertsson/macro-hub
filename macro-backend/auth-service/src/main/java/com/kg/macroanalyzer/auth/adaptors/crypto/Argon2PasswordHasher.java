package com.kg.macroanalyzer.auth.adaptors.crypto;

import com.kg.macroanalyzer.auth.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

@Component
public class Argon2PasswordHasher implements PasswordHasherPort {

    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final int PARALLELISM = 1;
    private static final int MEMORY_KIB = 19_456;
    private static final int ITERATIONS = 2;
    // 19 MiB per hash and 200 request threads: without a limit, a burst of logins could take most of a small machine's memory
    private static final int CONCURRENT_HASHES = 4;

    private final Argon2PasswordEncoder encoder =
            new Argon2PasswordEncoder(SALT_BYTES, HASH_BYTES, PARALLELISM, MEMORY_KIB, ITERATIONS);
    private final Semaphore hashing = new Semaphore(CONCURRENT_HASHES);

    @Override
    public String hash(String password) {
        hashing.acquireUninterruptibly();
        try {
            return encoder.encode(password);
        } finally {
            hashing.release();
        }
    }

    @Override
    public boolean matches(String password, String hash) {
        hashing.acquireUninterruptibly();
        try {
            return encoder.matches(password, hash);
        } finally {
            hashing.release();
        }
    }

}
