'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';
import MultiLineChartJsComponent from '@/components/MultiLineChartJsComponent';

const ChartInternationalGovernmentBillRates10Year = () => {
  const [valuesDenmark, setValuesDenmark] = useState([]);
  const [valuesEur, setValuesEur] = useState([]);
  const [valuesFinland, setValuesFinland] = useState([]);
  const [valuesFrance, setValuesFrance] = useState([]);
  const [valuesGB, setValuesGB] = useState([]);
  const [valuesGermany, setValuesGermany] = useState([]);
  const [valuesJapan, setValuesJapan] = useState([]);
  const [valuesNetherlands, setValuesNetherlands] = useState([]);
  const [valuesNorway, setValuesNorway] = useState([]);
  const [valuesUsa, setValuesUsa] = useState([]);

  const [labels, setLabels] = useState([]);

  useEffect(() => {
    const init = async () => {
      const nlArgs = '?period=10year&country=netherlands';
      const base10YearUrl = 'http://localhost:8080/euro-market-rate';

      const url10YearDenmark = base10YearUrl + '?period=10year&country=denmark';
      const url10YearEur = base10YearUrl + '?period=10year&country=eur';
      const url10YearFinland = base10YearUrl + '?period=10year&country=france';
      const url10YearFrance = base10YearUrl + '?period=10year&country=france';
      const url10YearGB = base10YearUrl + '?period=10year&country=gb';
      const url10YearGermany = base10YearUrl + '?period=10year&country=germany';
      const url10YearJapan = base10YearUrl + '?period=10year&country=japan';
      const url10YearNetherlands = base10YearUrl + nlArgs;
      const url10YearNorway = base10YearUrl + '?period=10year&country=norway';
      const url10YearUsa = base10YearUrl + '?period=10year&country=usa';

      const response10YearDenmark = await ky.get(url10YearDenmark);
      const response10YearEur = await ky.get(url10YearEur);
      const response10YearFrance = await ky.get(url10YearFrance);
      const response10YearFinland = await ky.get(url10YearFinland);
      const response10YearGB = await ky.get(url10YearGB);
      const response10YearGermany = await ky.get(url10YearGermany);
      const response10YearJapan = await ky.get(url10YearJapan);
      const response10YearNetherlands = await ky.get(url10YearNetherlands);
      const response10YearNorway = await ky.get(url10YearNorway);
      const response10YearUsa = await ky.get(url10YearUsa);

      const body10YearDenmark = await response10YearDenmark.json();
      const body10YearEur = await response10YearEur.json();
      const body10YearFrance = await response10YearFrance.json();
      const body10YearFinland = await response10YearFinland.json();
      const body10YearGB = await response10YearGB.json();
      const body10YearGermany = await response10YearGermany.json();
      const body10YearJapan = await response10YearJapan.json();
      const body10YeasNL = await response10YearNetherlands.json();
      const body10YeasNorway = await response10YearNorway.json();
      const body10YearUsa = await response10YearUsa.json();

      const values10YearDenmark = body10YearDenmark.map((o) => o['value']);
      const values10YearEur = body10YearEur.map((o) => o['value']);
      const values10YearFrance = body10YearFrance.map((o) => o['value']);
      const values10YearFinland = body10YearFinland.map((o) => o['value']);
      const values10YearGB = body10YearGB.map((o) => o['value']);
      const values10YearGermany = body10YearGermany.map((o) => o['value']);
      const values10YearJapan = body10YearJapan.map((o) => o['value']);
      const values10YearNetherlands = body10YeasNL.map((o) => o['value']);
      const values10YearNorway = body10YeasNorway.map((o) => o['value']);
      const values10YearUsa = body10YearUsa.map((o) => o['value']);

      const labelsRaw = body10YearEur.map((o) => o['date'].join('-'));

      setValuesDenmark(values10YearDenmark);
      setValuesEur(values10YearEur);
      setValuesFrance(values10YearFrance);
      setValuesFinland(values10YearFinland);
      setValuesGB(values10YearGB);
      setValuesGermany(values10YearGermany);
      setValuesJapan(values10YearJapan);
      setValuesNetherlands(values10YearNetherlands);
      setValuesNorway(values10YearNorway);
      setValuesUsa(values10YearUsa);

      setLabels(labelsRaw);
    };
    init();
  }, []);

  return (
    <MultiLineChartJsComponent
      datasets={[
        {
          label: 'Internationella statsobligationer 10-års löptid: Danmark',
          data: valuesDenmark,
          borderColor: 'rgb(255, 99, 71)',
          backgroundColor: 'rgba(255, 99, 71, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 10-års löptid: Eur',
          data: valuesEur,
          borderColor: 'rgb(255, 99, 71)',
          backgroundColor: 'rgba(255, 99, 71, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 10-års löptid: Frankrike',
          data: valuesFrance,
          borderColor: 'rgb(255, 255, 0)',
          backgroundColor: 'rgba(255, 255, 0, 1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 10-års löptid: Finland',
          data: valuesFinland,
          borderColor: 'rgb(255, 255, 0)',
          backgroundColor: 'rgba(255, 255, 0, 1)',
          pointRadius: 1,
        },
        {
          label:
            'Internationella statsobligationer 10-års löptid: Storbritannien',
          data: valuesGB,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 10-års löptid: Tyskland',
          data: valuesGermany,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 10-års löptid: Japan',
          data: valuesJapan,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label:
            'Internationella statsobligationer 10-års löptid: Nederländerna',
          data: valuesNetherlands,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 10-års löptid: Norge',
          data: valuesNorway,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Internationella statsobligationer 10-års löptid: USA',
          data: valuesUsa,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
      ]}
      labels={labels}
      title={'Internationella statsobligationer 10-års löptid'}
    />
  );
};

export default ChartInternationalGovernmentBillRates10Year;
