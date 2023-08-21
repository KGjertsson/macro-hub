export const rootUrl: string = 'http://localhost:8080';

export enum DATASET_NAMES {
  SwedenPolicyRate,
  UsdSekExchangeRate,
}

export const allDatasetNames = [
  DATASET_NAMES[DATASET_NAMES.SwedenPolicyRate],
  DATASET_NAMES[DATASET_NAMES.UsdSekExchangeRate],
];
