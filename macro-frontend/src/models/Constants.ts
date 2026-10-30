// Backend base URL, set at build time with VITE_API_URL. Unset (npm run dev) means the local
// backend on :8080; an empty value means same origin (the nginx image proxies /macro-analyzer/).
const apiUrl: string | undefined = import.meta.env.VITE_API_URL;
export const rootUrl: string = (apiUrl ?? 'http://localhost:8080').replace(/\/+$/, '');

// Sent to /chart-data as ordinals, so the order must match the backend's enums
export enum SAMPLE_STRATEGY {
  Day,
  Month,
  Year,
}

export enum TIME_FRAME {
  All,
  OneYear,
  FiveYear,
  TenYear,
  OneMonth
}
