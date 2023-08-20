'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';
import MultiLineChartJsComponent from '@/components/MultiLineChartJsComponent';

const ChartEuroMarketRates3Month = () => {
  const [valuesDenmark, setValuesDenmark] = useState([]);
  const [valuesEur, setValuesEur] = useState([]);
  const [valuesGB, setValuesGB] = useState([]);
  const [valuesJapan, setValuesJapan] = useState([]);
  const [valuesNorway, setValuesNorway] = useState([]);
  const [valuesUsa, setValuesUsa] = useState([]);

  const [labels, setLabels] = useState([]);

  useEffect(() => {
    const init = async () => {
      const base3MonthUrl = 'http://localhost:8080/euro-market-rate';
      const url3MonthDenmark = base3MonthUrl + '?period=3month&country=denmark';
      const url3MonthEur = base3MonthUrl + '?period=3month&country=eur';
      const url3MonthGB = base3MonthUrl + '?period=3month&country=gb';
      const url3MonthJapan = base3MonthUrl + '?period=3month&country=japan';
      const url3MonthNorway = base3MonthUrl + '?period=3month&country=norway';
      const url3MonthUsa = base3MonthUrl + '?period=3month&country=usa';

      const response3MonthDenmark = await ky.get(url3MonthDenmark);
      const response3MonthEur = await ky.get(url3MonthEur);
      const response3MonthGB = await ky.get(url3MonthGB);
      const response3MonthJapan = await ky.get(url3MonthJapan);
      const response3MonthNorway = await ky.get(url3MonthNorway);
      const response3MonthUsa = await ky.get(url3MonthUsa);

      const body3MonthDenmark = await response3MonthDenmark.json();
      const body3MonthEur = await response3MonthEur.json();
      const body3MonthGB = await response3MonthGB.json();
      const body3MonthJapan = await response3MonthJapan.json();
      const body3MonthNorway = await response3MonthNorway.json();
      const body3MonthUsa = await response3MonthUsa.json();

      // @ts-ignore
      const values3MonthDenmark = body3MonthDenmark.map((o) => o['value']);
      // @ts-ignore
      const values3MonthEur = body3MonthEur.map((o) => o['value']);
      // @ts-ignore
      const values3MonthGB = body3MonthGB.map((o) => o['value']);
      // @ts-ignore
      const values3MonthJapan = body3MonthJapan.map((o) => o['value']);
      // @ts-ignore
      const values3MonthNorway = body3MonthNorway.map((o) => o['value']);
      // @ts-ignore
      const values3MonthUsa = body3MonthUsa.map((o) => o['value']);

      // @ts-ignore
      const labelsRaw = body3MonthUsa.map((o) => o['date'].join('-'));

      setValuesDenmark(values3MonthDenmark);
      setValuesEur(values3MonthEur);
      setValuesGB(values3MonthGB);
      setValuesJapan(values3MonthJapan);
      setValuesNorway(values3MonthNorway);
      setValuesUsa(values3MonthUsa);

      setLabels(labelsRaw);
    };
    init();
  }, []);

  return (
    <MultiLineChartJsComponent
      datasets={[
        {
          label: 'Euromarknadsräntor 3-månaders löptid: Danmark',
          data: valuesDenmark,
          borderColor: 'rgb(255, 255, 0)',
          backgroundColor: 'rgba(255, 255, 0, 1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 3-månaders löptid: Eur',
          data: valuesEur,
          borderColor: 'rgb(255, 99, 71)',
          backgroundColor: 'rgba(255, 99, 71, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 3-månaders löptid: Storbritannien',
          data: valuesGB,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 3-månaders löptid: Japan',
          data: valuesJapan,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 3-månaders löptid: Norge',
          data: valuesNorway,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 3-månaders löptid: USA',
          data: valuesUsa,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
      ]}
      labels={labels}
      title={'Euromarknadsräntor 3-månaders löptid'}
    />
  );
};

export default ChartEuroMarketRates3Month;
