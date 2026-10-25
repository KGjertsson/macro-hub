export const rootUrl: string = 'http://localhost:8080';

export enum SAMPLE_STRATEGY {
  Day,
  Month,
  Year,
}

export const sampleStrategies = [
  SAMPLE_STRATEGY[SAMPLE_STRATEGY.Day],
  SAMPLE_STRATEGY[SAMPLE_STRATEGY.Month],
  SAMPLE_STRATEGY[SAMPLE_STRATEGY.Year]
];

export enum TIME_FRAME {
  All,
  OneYear,
  FiveYear,
  TenYear,
  OneMonth
}

export const timeWindows = [
  TIME_FRAME[TIME_FRAME.All],
  TIME_FRAME[TIME_FRAME.OneYear],
  TIME_FRAME[TIME_FRAME.FiveYear],
  TIME_FRAME[TIME_FRAME.TenYear],
  TIME_FRAME[TIME_FRAME.OneMonth]
];
