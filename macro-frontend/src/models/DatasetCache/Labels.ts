import { Dataset } from '@/models/Dataset';

export const generateLabels = (datasets: Dataset[]): string[] => {
  const [minDate, maxDate] = datasets
    .map((d) => d.labels!.map((l) => new Date(l)))
    .reduce(findEdgeDates, [new Date('2300-1-1'), new Date('1900-1-1')]);

  return generateDatesBetween(minDate, maxDate).map((d) => d.toDateString());
};

export const unionLabels = (datasets: Dataset[]): string[] =>
  datasets
    .map(stringToDate)
    .reduce(concatLabels, [])
    .sort(dateCompare)
    .map(dateToString)
    .reduce(distinct, []);

const stringToDate = (dataset: Dataset): Date[] =>
  dataset.labels!.map((l) => new Date(l));

const dateCompare = (first: Date, second: Date) =>
  first.getTime() - second.getTime();

const distinct = (result: string[], current: string): string[] => {
  if (result.length === 0) {
    return [current];
  }
  if (result.includes(current)) {
    return result;
  }

  return result.concat(current);
};

const concatLabels = (result: Date[], currentValue: Date[]) =>
  result.concat(currentValue);

const dateToString = (d: Date) => {
  // Extract year, month, and day components
  const year = d.getFullYear(); // Get the 4-digit year
  const month = String(d.getMonth() + 1).padStart(2, '0'); // Get the month (add 1 because months are zero-based) and pad with '0' if necessary
  const day = String(d.getDate()).padStart(2, '0'); // Get the day of the month and pad with '0' if necessary

  // Create the formatted string
  return `${year}-${month}-${day}`;
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
