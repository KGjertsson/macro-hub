import { Dataset } from '@/models/Dataset';
import { SAMPLE_SIZE } from '@/models/Constants';

export const generateLabels = (datasets: Dataset[]): string[] => {
  const [minDate, maxDate] = datasets
    .map((d) => d.labels!.map((l) => new Date(l)))
    .reduce(findEdgeDates, [new Date('2300-1-1'), new Date('1900-1-1')]);

  return generateDatesBetween(minDate, maxDate).map((d) => d.toDateString());
};

export const unionLabels = (
  datasets: Dataset[],
  selectedSample: SAMPLE_SIZE
): string[] => {
  const labelList = datasets
    .map((d) => d.labels!.map((l) => Date.parse(l)))
    .flat();
  const labelSet = new Set(labelList);

  return Array.from(labelSet).map((l) => buildDateString(l, selectedSample));
};

const buildDateString = (
  unixTimeNumber: number,
  selectedSample: SAMPLE_SIZE
): string => {
  const fullDate = new Date(unixTimeNumber);
  let dateBuilder = fullDate.getUTCFullYear().toString();
  if ([SAMPLE_SIZE.Month, SAMPLE_SIZE.Day].includes(selectedSample)) {
    dateBuilder = dateBuilder + '-' + fullDate.getUTCMonth();
  }
  if (selectedSample === SAMPLE_SIZE.Day) {
    dateBuilder = dateBuilder + '-' + fullDate.getUTCDay();
  }
  return dateBuilder;
};

export const extractLatestLabels = (datasets: Dataset[]): string[] => {
  return datasets.reduce((result: string[], current: Dataset) => {
    if (result.length === 0) {
      return current.labels!;
    }
    const resultEnd = Date.parse(result[result.length - 1]);
    const currentEnd = Date.parse(current.labels![current.labels!.length - 1]);

    return currentEnd > resultEnd ? current.labels! : result;
  }, []);
};

const findEdgeDates = (result: Date[], current: Date[]) => {
  const minCurrent = current[0];
  const maxCurrent = current[current.length - 1];
  const newMin = minCurrent < result[0] ? minCurrent : result[0];
  const newMax = maxCurrent > result[1] ? maxCurrent : result[1];

  return [newMin, newMax];
};

const generateDatesBetween = (startDate: Date, endDate: Date): Date[] => {
  const datesBetween = [];
  let currentDate = new Date(startDate);

  while (currentDate <= endDate) {
    datesBetween.push(currentDate);
    currentDate = new Date(currentDate.setDate(currentDate.getDate() + 1));
  }

  return datesBetween;
};
