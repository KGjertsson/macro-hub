import { Dataset } from '@/models/Dataset';
import { dateToString } from '@/utils/DateUtils';
import { distinct } from '@/utils/ObjectUtils';

export const generateLabels = (datasets: Dataset[]): string[] => {
  const [minDate, maxDate] = datasets
    .map((d) => d.labels!.map((l) => new Date(l)))
    .reduce(findEdgeDates, [new Date('2300-1-1'), new Date('1900-1-1')]);

  return generateDatesBetween(minDate, maxDate).map(dateToString);
};

export const unionLabels = (datasets: Dataset[]): string[] =>
  datasets
    .map(stringToDate)
    .reduce(concatLabels, [])
    .sort(dateCompare)
    .map(dateToString)
    .reduce<string[]>(distinct, []);

const stringToDate = (dataset: Dataset): Date[] =>
  dataset.labels!.map((l) => new Date(l));

const concatLabels = (result: Date[], currentValue: Date[]) =>
  result.concat(currentValue);

const dateCompare = (first: Date, second: Date) =>
  first.getTime() - second.getTime();

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
