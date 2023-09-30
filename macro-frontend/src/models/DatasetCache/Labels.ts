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
): string[] =>
  datasets
    .map(stringToDate)
    .reduce(concatLabels, [])
    .sort(dateCompare)
    .map(dateToString);

const dateCompare = (first: Date, second: Date) =>
  first.getTime() - second.getTime();

const stringToDate = (dataset: Dataset): Date[] =>
  dataset.labels!.map((l) => new Date(l));

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

const sortUnion = (result: Date[], currentValue: Date): Date[] => {
  console.log('calling sort with result.length = ' + result.length);
  if (result.length === 0) {
    console.log('adding first value ' + dateToString(currentValue));
    return [currentValue];
  } else if (result.includes(currentValue)) {
    return result;
  } else if (currentValue < result[0]) {
    console.log('prepending ' + dateToString(currentValue));
    return [currentValue].concat(result);
  } else {
    let newResult = [];

    result.forEach((d, index) => {
      if (currentValue < d) {
        console.log(
          'inserting new value ' +
            dateToString(currentValue) +
            ' between ' +
            dateToString(result[index - 1]) +
            ' and ' +
            dateToString(d)
        );
        console.log('---');
        return [result.slice(0, index), currentValue, result.slice(index)];
      }
    });

    console.log('appending ' + dateToString(currentValue));
    result.push(currentValue);
    return result;
  }
};

const buildDateString = (
  unixTimeNumber: number,
  selectedSample: SAMPLE_SIZE
): string => {
  const fullDate = new Date(unixTimeNumber);
  let dateBuilder = fullDate.getUTCFullYear().toString();

  if ([SAMPLE_SIZE.Month, SAMPLE_SIZE.Day].includes(selectedSample)) {
    const month = fullDate.getUTCMonth() + 1;
    dateBuilder = dateBuilder + '-' + month;
  }
  if (selectedSample === SAMPLE_SIZE.Day) {
    const day = fullDate.getUTCDay() + 1;
    dateBuilder = dateBuilder + '-' + day;
  }

  return dateBuilder;
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
