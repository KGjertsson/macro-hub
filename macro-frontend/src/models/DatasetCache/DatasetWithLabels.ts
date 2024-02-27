import { Dataset } from '@/models/Dataset';

export type DatasetWithLabels = {
  labels: string[];
  chartData: Dataset[];
};
