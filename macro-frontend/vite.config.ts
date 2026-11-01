import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-swc";
import path from "path";
import { componentTagger } from "lovable-tagger";

// Backends per route, the same split as nginx/default.conf.template. Everything is still the one
// backend on :8080. Order matters: the first key that the request path starts with wins.
const MACRO_SERIES_URL = "http://localhost:8080";
const REGIONAL_STATISTICS_URL = "http://localhost:8080";
const PARLIAMENT_URL = "http://localhost:8080";
const ELECTION_URL = "http://localhost:8080";
const BACKEND_URL = "http://localhost:8080";

const proxy = {
  "/macro-analyzer/series-config": MACRO_SERIES_URL,
  "/macro-analyzer/chart-data": MACRO_SERIES_URL,
  "/macro-analyzer/regional-statistics/": REGIONAL_STATISTICS_URL,
  "/macro-analyzer/parliament/": PARLIAMENT_URL,
  "/macro-analyzer/municipalities/": ELECTION_URL,
  "/macro-analyzer/regions/": ELECTION_URL,
  "/macro-analyzer/constituencies/": ELECTION_URL,
  // The manual enqueue and scrape endpoints, and anything else
  "/macro-analyzer/": BACKEND_URL,
};

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  server: {
    host: "::",
    port: 5173,
    proxy, // vite preview uses it as well
  },
  plugins: [react(), mode === "development" && componentTagger()].filter(Boolean),
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
}));
