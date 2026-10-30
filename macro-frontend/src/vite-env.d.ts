/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Backend base URL; unset defaults to http://localhost:8080, empty means same origin */
  readonly VITE_API_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
