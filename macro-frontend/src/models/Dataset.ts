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

export const defaultDataset = [
  policyRateSweden,
  usdSekExchange,
  governmentBondSweden2Year,
  governmentBondSweden5Year,
  governmentBondSweden7Year,
  governmentBondSweden10Year,
];
