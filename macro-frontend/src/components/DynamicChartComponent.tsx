import React, { useEffect } from 'react';

import {
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Title,
  Tooltip,
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import ky from 'ky';
import GraphConfigItem from '@/models/GraphConfigItem';
import DataSeries from '@/models/DataSeries';
import { rootUrl } from '@/models/Constants';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

interface GraphData {
  SwedenPolicyRate: DataSeries;
  UsdSekExchangeRate: DataSeries;
}

interface DynamicChartComponentProps {
  graphConfigItems: GraphConfigItem[];
}

const DynamicChartComponent = ({
  graphConfigItems,
}: DynamicChartComponentProps) => {
  let graphData: GraphData = {
    SwedenPolicyRate: DataSeries.init(),
    UsdSekExchangeRate: DataSeries.init(),
  };

  useEffect(() => {
    const init = async () => {
      for (const graphConfigItem of graphConfigItems) {
        const key = graphConfigItem.name;
        const selectedGraphData = graphData[key as keyof GraphData];

        if (selectedGraphData.isEmpty()) {
          console.log('fetching data for ' + key);
          const responseRaw = await ky.get(rootUrl + graphConfigItem.url);
          const responseData = await responseRaw.json();
          console.log(responseData);
        }
      }
    };

    init();
  }, [graphConfigItems]);

  return (
    <Line
      className="mx-auto w-3/5 overflow-hidden"
      style={{ width: '1000px', height: '1000px' }}
      options={{
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
          title: {
            display: true,
            text: 'super bra titel',
          },
        },
      }}
      data={{
        labels: [],
        datasets: [],
      }}
    />
  );
};

export default DynamicChartComponent;
