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
    .map(labelsToUnixTime)
    .reduce(concatLabels, [])
    .reduce(sortUnion, [])
    .map((l) => buildDateString(l, selectedSample));

const labelsToUnixTime = (dataset: Dataset): number[] =>
  dataset.labels!.map(Date.parse);

const concatLabels = (result: number[], currentValue: number[]) =>
  result.concat(currentValue);

const sortUnion = (result: number[], currentValue: number): number[] => {
  if (result.length === 0) {
    console.log(
      'adding first value ' + buildDateString(currentValue, SAMPLE_SIZE.Month)
    );
    return [currentValue];
  } else if (result.includes(currentValue)) {
    console.log(
      'removed duplicate ' + buildDateString(currentValue, SAMPLE_SIZE.Month)
    );
    return result;
  } else if (currentValue < result[0]) {
    console.log(
      'prepending ' + buildDateString(currentValue, SAMPLE_SIZE.Month)
    );
    return [currentValue].concat(result);
  } else {
    result.forEach((d, index) => {
      if (currentValue < d) {
        // console.log(
        //   buildDateString(currentValue, SAMPLE_SIZE.Year) +
        //   ' < ' +
        //   buildDateString(d, SAMPLE_SIZE.Year) +
        //   ' at index: ' +
        //   index
        // );
        console.log(
          'inserting new value ' +
            buildDateString(currentValue, SAMPLE_SIZE.Month) +
            ' between ' +
            buildDateString(d, SAMPLE_SIZE.Month) +
            ' and ' +
            buildDateString(result[index + 1], SAMPLE_SIZE.Month)
        );
        return [result.slice(0, index), currentValue, result.slice(index)];
      }
    });

    console.log(
      'appending ' + buildDateString(currentValue, SAMPLE_SIZE.Month)
    );
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
    dateBuilder = dateBuilder + '-' + fullDate.getUTCMonth();
  }
  if (selectedSample === SAMPLE_SIZE.Day) {
    dateBuilder = dateBuilder + '-' + fullDate.getUTCDay();
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
