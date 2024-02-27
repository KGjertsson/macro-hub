import { DATASET_NAMES } from '@/models/Constants';

export type Dataset = {
  values?: number[];
  labels?: string[];
  color?: string;
  name: DATASET_NAMES;
};

const policyRateSweden: Dataset = {
  name: DATASET_NAMES.PolicyRateSweden,
};

const usdSekExchange: Dataset = {
  name: DATASET_NAMES.UsdSekExchangeRate,
};

const governmentBondSweden2Year: Dataset = {
  name: DATASET_NAMES.GovernmentBondSweden2Year,
};

const governmentBondSweden5Year: Dataset = {
  name: DATASET_NAMES.GovernmentBondSweden5Year,
};

const governmentBondSweden7Year: Dataset = {
  name: DATASET_NAMES.GovernmentBondSweden7Year,
};

const governmentBondSweden10Year: Dataset = {
  name: DATASET_NAMES.GovernmentBondSweden10Year,
};

const governmentBillsSweden1Month: Dataset = {
  name: DATASET_NAMES.GovernmentBillSweden1Month,
};

const governmentBillsSweden3Month: Dataset = {
  name: DATASET_NAMES.GovernmentBillSweden3Month,
};

const governmentBillsSweden6Month: Dataset = {
  name: DATASET_NAMES.GovernmentBillSweden6Month,
};

const governmentBillsSweden12Month: Dataset = {
  name: DATASET_NAMES.GovernmentBillSweden12Month,
};

const euroMarketRateDenmark3Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateDenmark3Month,
};

const euroMarketRateEur3Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateEur3Month,
};

const euroMarketRateGB3Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateGB3Month,
};

const euroMarketRateJapan3Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateJapan3Month,
};

const euroMarketRateNorway3Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateNorway3Month,
};

const euroMarketRateUsa3Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateUsa3Month,
};

const euroMarketRateDenmark6Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateDenmark6Month,
};

const euroMarketRateEur6Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateEur6Month,
};

const euroMarketRateGB6Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateGB6Month,
};

const euroMarketRateJapan6Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateJapan6Month,
};

const euroMarketRateNorway6Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateNorway6Month,
};

const euroMarketRateUsa6Month: Dataset = {
  name: DATASET_NAMES.EuroMarketRateUsa6Month,
};

const internationalGovernmentBondEur5Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsEur5Year,
};

const internationalGovernmentBondFrance5Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsFrance5Year,
};

const internationalGovernmentBondGB5Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsGB5Year,
};

const internationalGovernmentBondGermany5Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsGermany5Year,
};

const internationalGovernmentBondJapan5Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsJapan5Year,
};

const internationalGovernmentBondNetherlands5Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsNetherlands5Year,
};

const internationalGovernmentBondUsa5Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsUsa5Year,
};

const internationalGovernmentBondDenmark10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsDenmark10Year,
};

const internationalGovernmentBondEur10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsEur10Year,
};

const internationalGovernmentBondFinland10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsFinland10Year,
};

const internationalGovernmentBondFrance10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsFrance10Year,
};

const internationalGovernmentBondGB10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsGB10Year,
};

const internationalGovernmentBondGermany10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsGermany10Year,
};

const internationalGovernmentBondJapan10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsJapan10Year,
};

const internationalGovernmentBondNetherlands10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsNetherlands10Year,
};

const internationalGovernmentBondNorway10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsNorway10Year,
};

const internationalGovernmentBondUsa10Year: Dataset = {
  name: DATASET_NAMES.InternationalGovernmentBondsUsa10Year,
};

export const defaultDataset = [
  policyRateSweden,
  usdSekExchange,
  governmentBondSweden2Year,
  governmentBondSweden5Year,
  governmentBondSweden7Year,
  governmentBondSweden10Year,
  governmentBillsSweden1Month,
  governmentBillsSweden3Month,
  governmentBillsSweden6Month,
  governmentBillsSweden12Month,
  euroMarketRateDenmark3Month,
  euroMarketRateEur3Month,
  euroMarketRateGB3Month,
  euroMarketRateJapan3Month,
  euroMarketRateNorway3Month,
  euroMarketRateUsa3Month,
  euroMarketRateDenmark6Month,
  euroMarketRateEur6Month,
  euroMarketRateGB6Month,
  euroMarketRateJapan6Month,
  euroMarketRateNorway6Month,
  euroMarketRateUsa6Month,
  internationalGovernmentBondEur5Year,
  internationalGovernmentBondFrance5Year,
  internationalGovernmentBondGB5Year,
  internationalGovernmentBondGermany5Year,
  internationalGovernmentBondJapan5Year,
  internationalGovernmentBondNetherlands5Year,
  internationalGovernmentBondUsa5Year,
  internationalGovernmentBondDenmark10Year,
  internationalGovernmentBondEur10Year,
  internationalGovernmentBondFinland10Year,
  internationalGovernmentBondFrance10Year,
  internationalGovernmentBondGB10Year,
  internationalGovernmentBondGermany10Year,
  internationalGovernmentBondJapan10Year,
  internationalGovernmentBondNetherlands10Year,
  internationalGovernmentBondNorway10Year,
  internationalGovernmentBondUsa10Year,
];
