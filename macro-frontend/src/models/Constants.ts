export const rootUrl: string = 'http://localhost:8080';

export enum SAMPLE_STRATEGY {
  Day,
  Month,
  Year,
}

export const sampleStrategies = [
  SAMPLE_STRATEGY[SAMPLE_STRATEGY.Day],
  SAMPLE_STRATEGY[SAMPLE_STRATEGY.Month],
  SAMPLE_STRATEGY[SAMPLE_STRATEGY.Year],
];
