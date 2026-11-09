import {
  ErrorsInstrumentation,
  FetchTransport,
  initializeFaro,
  InternalLoggerLevel,
  WebVitalsInstrumentation,
  type TransportItem,
} from "@grafana/faro-web-sdk";

const COLLECT_PATH = "/telemetry/collect";
const REQUEST_TIMEOUT_MS = 5_000;

// A page's query string holds what the visitor picked, such as the area found by an address search
function withPathOnly(item: TransportItem): TransportItem {
  const url = item.meta.page?.url;
  if (!url) return item;

  return { ...item, meta: { ...item.meta, page: { ...item.meta.page, url: new URL(url).pathname } } };
}

export function startBrowserTelemetry(): void {
  initializeFaro({
    app: { name: "macro-hub", environment: "production" },
    // The observability stack is optional and often down. By default the transport tries 502s and network errors three
    // times per batch, and logs each batch it gives up on to console.error
    transports: [
      new FetchTransport({
        url: COLLECT_PATH,
        requestTimeoutMs: REQUEST_TIMEOUT_MS,
        retry: { maxAttempts: 1 },
      }),
    ],
    internalLoggerLevel: InternalLoggerLevel.OFF,
    instrumentations: [new ErrorsInstrumentation(), new WebVitalsInstrumentation()],
    sessionTracking: { enabled: false },
    beforeSend: withPathOnly,
  });
}
