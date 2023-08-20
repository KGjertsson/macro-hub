import { DATASET_NAMES } from '@/models/Constants';

export type Dataset = {
  label?: String;
  data?: number[];
  borderColor?: string;
  backgroundColor?: string;
  pointRadius?: number;
  selected: boolean;
  name: string;
  url: string;
};

export const defaultDataset = [
  {
    selected: false,
    name: DATASET_NAMES.SwedenPolicyRate,
    url: '/policy-rate/sweden',
  },
  {
    selected: false,
    name: DATASET_NAMES.UsdSekExchangeRate,
    url: '/exchange-rate/usd-sek',
  },
];
