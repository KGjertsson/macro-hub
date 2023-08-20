import { DATASET_NAMES } from '@/models/Constants';

export type Dataset = {
  data?: number[];
  dataOptions: {};
  selected: boolean;
  name: string;
  url: string;
};

const foo: Dataset = {
  dataOptions: {
    borderColor: 'rgb(75, 192, 192)',
    backgroundColor: 'rgb(75, 192, 192, 0.1)',
    pointRadius: 1,
  },
  selected: false,
  name: DATASET_NAMES.SwedenPolicyRate,
  url: '/policy-rate/sweden',
};

export const defaultDataset = [
  foo,
  {
    options: {
      label: 'USD till SEK växelkurs',
      borderColor: 'rgb(75, 192, 192)',
      backgroundColor: 'rgb(75, 192, 192, 0.1)',
      pointRadius: 1,
    },
    selected: false,
    name: DATASET_NAMES.UsdSekExchangeRate,
    url: '/exchange-rate/usd-sek',
  },
];
