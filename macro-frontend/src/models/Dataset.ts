// One series as returned by /chart-data, aligned to the response's labels
export type Dataset = {
  values: (number | null)[];
  name: string;
};
