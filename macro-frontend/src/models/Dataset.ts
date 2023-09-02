import { DATASET_NAMES } from '@/models/Constants';

export type LineConfig = {
  label: string;
  borderColor: string;
  backgroundColor: string;
  pointRadius: number;
};

export type Dataset = {
  data?: number[];
  labels?: string[];
  lineConfig: LineConfig;
  selected: boolean;
  name: DATASET_NAMES;
  url: string;
};

const policyRateSweden: Dataset = {
  lineConfig: {
    label: 'Sveriges styrränta',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.SwedenPolicyRate,
  url: '/policy-rate/sweden',
};

const usdSekExchange: Dataset = {
  lineConfig: {
    label: 'USD till SEK växelkurs',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.UsdSekExchangeRate,
  url: '/exchange-rate/usd-sek',
};

const governmentBondSweden2Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 2 års löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden2Year,
  url: '/government-bonds/sweden?period=2',
};

const governmentBondSweden5Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 5 års löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden5Year,
  url: '/government-bonds/sweden?period=5',
};

const governmentBondSweden7Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 7 års löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden7Year,
  url: '/government-bonds/sweden?period=2',
};

const governmentBondSweden10Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 10 års löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden10Year,
  url: '/government-bonds/sweden?period=2',
};

const governmentBillsSweden1Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 1-månads löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden1Month,
  url: '/government-bills/sweden?period=1',
};

const governmentBillsSweden3Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 3-månads löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden3Month,
  url: '/government-bills/sweden?period=3',
};

const governmentBillsSweden6Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 6-månads löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden6Month,
  url: '/government-bills/sweden?period=6',
};

const governmentBillsSweden12Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 12-månads löptid',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden12Month,
  url: '/government-bills/sweden?period=12',
};

const euroMarketRateDenmark3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Danmark',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateDenmark3Month,
  url: '/euro-market-rate?period=3month&country=denmark',
};

const euroMarketRateEur3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Eur',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateEur3Month,
  url: '/euro-market-rate?period=3month&country=eur',
};

const euroMarketRateGB3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: GB',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateGB3Month,
  url: '/euro-market-rate?period=3month&country=gb',
};

const euroMarketRateJapan3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Japan',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateJapan3Month,
  url: '/euro-market-rate?period=3month&country=japan',
};

const euroMarketRateNorway3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Norge',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateNorway3Month,
  url: '/euro-market-rate?period=3month&country=norway',
};

const euroMarketRateUsa3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: USA',
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateUsa3Month,
  url: '/euro-market-rate?period=3month&country=usa',
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
];
