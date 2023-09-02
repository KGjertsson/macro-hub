export const rootUrl: string = 'http://localhost:8080';

export enum DATASET_NAMES {
  SwedenPolicyRate,
  UsdSekExchangeRate,
  GovernmentBondSweden2Year,
  GovernmentBondSweden5Year,
  GovernmentBondSweden7Year,
  GovernmentBondSweden10Year,
  GovernmentBillSweden1Month,
  GovernmentBillSweden3Month,
  GovernmentBillSweden6Month,
  GovernmentBillSweden12Month,
  EuroMarketRateDenmark3Month,
  EuroMarketRateEur3Month,
  EuroMarketRateGB3Month,
  EuroMarketRateJapan3Month,
  EuroMarketRateNorway3Month,
  EuroMarketRateUsa3Month,
}

export const allDatasetNames = [
  DATASET_NAMES[DATASET_NAMES.SwedenPolicyRate],
  DATASET_NAMES[DATASET_NAMES.UsdSekExchangeRate],
  DATASET_NAMES[DATASET_NAMES.GovernmentBondSweden2Year],
  DATASET_NAMES[DATASET_NAMES.GovernmentBondSweden5Year],
  DATASET_NAMES[DATASET_NAMES.GovernmentBondSweden7Year],
  DATASET_NAMES[DATASET_NAMES.GovernmentBondSweden10Year],
  DATASET_NAMES[DATASET_NAMES.GovernmentBillSweden1Month],
  DATASET_NAMES[DATASET_NAMES.GovernmentBillSweden3Month],
  DATASET_NAMES[DATASET_NAMES.GovernmentBillSweden6Month],
  DATASET_NAMES[DATASET_NAMES.GovernmentBillSweden12Month],
  DATASET_NAMES[DATASET_NAMES.EuroMarketRateDenmark3Month],
  DATASET_NAMES[DATASET_NAMES.EuroMarketRateEur3Month],
  DATASET_NAMES[DATASET_NAMES.EuroMarketRateGB3Month],
  DATASET_NAMES[DATASET_NAMES.EuroMarketRateJapan3Month],
  DATASET_NAMES[DATASET_NAMES.EuroMarketRateNorway3Month],
  DATASET_NAMES[DATASET_NAMES.EuroMarketRateUsa3Month],
];
