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
];
