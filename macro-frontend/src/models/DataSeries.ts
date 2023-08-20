class DataSeries {
  values: Array<number>;
  labels: Array<String>;

  constructor(values: Array<number>, labels: Array<String>) {
    this.values = values;
    this.labels = labels;
  }

  static init() {
    return new DataSeries([], []);
  }

  setValues(values: Array<number>) {
    this.values = values;
  }

  setLabels(labels: Array<string>) {
    this.labels = labels;
  }

  isEmpty() {
    return this.values.length === 0;
  }
}

export default DataSeries;