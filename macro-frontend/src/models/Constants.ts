export const rootUrl: string = 'http://localhost:8080';

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
