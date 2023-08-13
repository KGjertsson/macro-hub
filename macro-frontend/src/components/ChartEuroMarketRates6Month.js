'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';
import MultiLineChartJsComponent from '@/components/MultiLineChartJsComponent';

const ChartEuroMarketRates6Month = () => {
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
      const url6MonthDenmark = base3MonthUrl + '?period=6month&country=denmark';
      const url6MonthEur = base3MonthUrl + '?period=6month&country=eur';
      const url6MonthGB = base3MonthUrl + '?period=6month&country=gb';
      const url6MonthJapan = base3MonthUrl + '?period=6month&country=japan';
      const url6MonthNorway = base3MonthUrl + '?period=6month&country=norway';
      const url6MonthUsa = base3MonthUrl + '?period=6month&country=usa';

      const response6MonthDenmark = await ky.get(url6MonthDenmark);
      const response6MonthEur = await ky.get(url6MonthEur);
      const response6MonthGB = await ky.get(url6MonthGB);
      const response6MonthJapan = await ky.get(url6MonthJapan);
      const response6MonthNorway = await ky.get(url6MonthNorway);
      const response6MonthUsa = await ky.get(url6MonthUsa);

      const body6MonthDenmark = await response6MonthDenmark.json();
      const body6MonthEur = await response6MonthEur.json();
      const body6MonthGB = await response6MonthGB.json();
      const body6MonthJapan = await response6MonthJapan.json();
      const body6MonthNorway = await response6MonthNorway.json();
      const body6MonthUsa = await response6MonthUsa.json();

      const values6MonthDenmark = body6MonthDenmark.map((o) => o['value']);
      const values6MonthEur = body6MonthEur.map((o) => o['value']);
      const values6MonthGB = body6MonthGB.map((o) => o['value']);
      const values6MonthJapan = body6MonthJapan.map((o) => o['value']);
      const values6MonthNorway = body6MonthNorway.map((o) => o['value']);
      const values6MonthUsa = body6MonthUsa.map((o) => o['value']);

      const labelsRaw = body6MonthUsa.map((o) => o['date'].join('-'));

      setValuesDenmark(values6MonthDenmark);
      setValuesEur(values6MonthEur);
      setValuesGB(values6MonthGB);
      setValuesJapan(values6MonthJapan);
      setValuesNorway(values6MonthNorway);
      setValuesUsa(values6MonthUsa);

      setLabels(labelsRaw);
    };
    init();
  }, []);

  return (
    <MultiLineChartJsComponent
      datasets={[
        {
          label: 'Euromarknadsräntor 6-månaders löptid: Danmark',
          data: valuesDenmark,
          borderColor: 'rgb(255, 255, 0)',
          backgroundColor: 'rgba(255, 255, 0, 1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 6-månaders löptid: Eur',
          data: valuesEur,
          borderColor: 'rgb(255, 99, 71)',
          backgroundColor: 'rgba(255, 99, 71, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 6-månaders löptid: Storbritannien',
          data: valuesGB,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 6-månaders löptid: Japan',
          data: valuesJapan,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 6-månaders löptid: Norge',
          data: valuesNorway,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Euromarknadsräntor 6-månaders löptid: USA',
          data: valuesUsa,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
      ]}
      labels={labels}
      title={'Euromarknadsräntor 6-månaders löptid'}
    />
  );
};

export default ChartEuroMarketRates6Month;
