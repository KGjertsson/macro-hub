import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import { componentTagger } from "lovable-tagger";

// Backends per route, the same split as nginx/default.conf.template. Order matters: the first key that matches the
// request path wins.
const MACRO_SERIES_URL = "http://localhost:8083";
const REGIONAL_STATISTICS_URL = "http://localhost:8082";
const PARLIAMENT_URL = "http://localhost:8084";
const ELECTION_URL = "http://localhost:8085";
const AUTH_URL = "http://localhost:8086";

const proxy = {
  "/macro-analyzer/series-config": MACRO_SERIES_URL,
  "/macro-analyzer/chart-data": MACRO_SERIES_URL,
  "/macro-analyzer/scrape/macro-series/": MACRO_SERIES_URL,
  "/macro-analyzer/regional-statistics/": REGIONAL_STATISTICS_URL,
  "/macro-analyzer/scrape/regional-statistics/": REGIONAL_STATISTICS_URL,
  "/macro-analyzer/parliament/": PARLIAMENT_URL,
  "/macro-analyzer/scrape/parliament/": PARLIAMENT_URL,
  "/macro-analyzer/municipalities/": ELECTION_URL,
  "/macro-analyzer/regions/": ELECTION_URL,
  "/macro-analyzer/constituencies/": ELECTION_URL,
  "/macro-analyzer/scrape/election/": ELECTION_URL,
  "/macro-analyzer/auth/": AUTH_URL,
  // Any other API path answers 404, like nginx. bypass returning false does that, so nothing reaches the target; without
  // this entry the dev server would answer a GET with index.html and a 200
  "^/macro-analyzer(/|\\?|$)": { target: ELECTION_URL, bypass: () => false as const },
};

export default defineConfig(({ mode }) => ({
  server: {
    host: "::",
    port: 5173,
    proxy,
  },
  plugins: [react(), mode === "development" && componentTagger()].filter(Boolean),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
}));
