'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';
import MultiLineChartJsComponent from '@/components/MultiLineChartJsComponent';

const ChartInternationalGovernmentBillRates5Year = () => {
  const [valuesEur, setValuesEur] = useState([]);
  const [valuesFrance, setValuesFrance] = useState([]);
  const [valuesGB, setValuesGB] = useState([]);
  const [valuesGermany, setValuesGermany] = useState([]);
  const [valuesJapan, setValuesJapan] = useState([]);
  const [valuesNetherlands, setValuesNetherlands] = useState([]);
  const [valuesUsa, setValuesUsa] = useState([]);

  const [labels, setLabels] = useState([]);

  useEffect(() => {
    const init = async () => {
      const nlArgs = '?period=5year&country=netherlands';
      const baseUrl = 'http://localhost:8080/government-bonds/international';

      const url5YearEur = baseUrl + '?period=5year&country=eur';
      const url5YearFrance = baseUrl + '?period=5year&country=france';
      const url5YearGB = baseUrl + '?period=5year&country=gb';
      const url5YearGermany = baseUrl + '?period=5year&country=germany';
      const url5YearJapan = baseUrl + '?period=5year&country=japan';
      const url5YearNetherlands = baseUrl + nlArgs;
      const url5YearUsa = baseUrl + '?period=5year&country=usa';

      const response5YearEur = await ky.get(url5YearEur);
      const response5YearFrance = await ky.get(url5YearFrance);
      const response5YearGB = await ky.get(url5YearGB);
      const response5YearGermany = await ky.get(url5YearGermany);
      const response5YearJapan = await ky.get(url5YearJapan);
      const response5YearNetherlands = await ky.get(url5YearNetherlands);
      const response5YearUsa = await ky.get(url5YearUsa);

      const body5YearEur = await response5YearEur.json();
      const body5YearFrance = await response5YearFrance.json();
      const body5YearGB = await response5YearGB.json();
      const body5YearGermany = await response5YearGermany.json();
      const body5YearJapan = await response5YearJapan.json();
      const body5YeasNL = await response5YearNetherlands.json();
      const body5YearUsa = await response5YearUsa.json();

      // @ts-ignore
      const values5YearEur = body5YearEur.map((o) => o['value']);
      // @ts-ignore
      const values5YearFrance = body5YearFrance.map((o) => o['value']);
      // @ts-ignore
      const values5YearGB = body5YearGB.map((o) => o['value']);
      // @ts-ignore
      const values5YearGermany = body5YearGermany.map((o) => o['value']);
      // @ts-ignore
      const values5YearJapan = body5YearJapan.map((o) => o['value']);
      // @ts-ignore
      const values5YearNetherlands = body5YeasNL.map((o) => o['value']);
      // @ts-ignore
      const values5YearUsa = body5YearUsa.map((o) => o['value']);

      // @ts-ignore
      const labelsRaw = body5YearGB.map((o) => o['date'].join('-'));

      setValuesEur(values5YearEur);
      setValuesFrance(values5YearFrance);
      setValuesGB(values5YearGB);
      setValuesGermany(values5YearGermany);
      setValuesJapan(values5YearJapan);
      setValuesNetherlands(values5YearNetherlands);
      setValuesUsa(values5YearUsa);

      setLabels(labelsRaw);
    };
    init();
  }, []);

  return (
    <MultiLineChartJsComponent
      datasets={[
        {
          label: 'Internationella statsobligationer 5-års löptid: Eur',
          data: valuesEur,
          borderColor: 'rgb(255, 99, 71)',
          backgroundColor: 'rgba(255, 99, 71, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 5-års löptid: Frankrike',
          data: valuesFrance,
          borderColor: 'rgb(255, 255, 0)',
          backgroundColor: 'rgba(255, 255, 0, 1)',
          pointRadius: 1,
        },
        {
          label:
            'Internationella statsobligationer 5-års löptid: Storbritannien',
          data: valuesGB,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 5-års löptid: Tyskland',
          data: valuesGermany,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 5-års löptid: Japan',
          data: valuesJapan,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label:
            'Internationella statsobligationer 5-års löptid: Nederländerna',
          data: valuesNetherlands,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 5-års löptid: USA',
          data: valuesUsa,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
      ]}
      labels={labels}
      title={'Internationella statsobligationer 5-års löptid'}
    />
  );
};

export default ChartInternationalGovernmentBillRates5Year;
