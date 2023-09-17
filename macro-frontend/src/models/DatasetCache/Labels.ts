import { Dataset } from '@/models/Dataset';

export const generateLabels = (datasets: Dataset[]): string[] => {
  const [minDate, maxDate] = datasets
    .map((d) => d.labels!.map((l) => new Date(l)))
    .reduce(
      (result: Date[], current: Date[]) => {
        const minCurrent = current[0];
        const maxCurrent = current[current.length - 1];
        const newMin = minCurrent < result[0] ? minCurrent : result[0];
        const newMax = maxCurrent > result[1] ? maxCurrent : result[1];

        return [newMin, newMax];
      },
      [new Date('2300-1-1'), new Date('1900-1-1')]
    );

  return generateDatesBetween(minDate, maxDate).map((d) => d.toDateString());
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