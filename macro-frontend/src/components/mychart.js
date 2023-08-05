'use client';
import { useEffect, useState } from 'react';

const MyChart = () => {
  const [labels, setLabels] = useState([]);
  const [data, setData] = useState([]);

  useEffect(() => {
    const init = async () => {
      const { Chart, initTE } = await import('tw-elements');
      initTE({ Chart });
    };
    init();
  }, []);

  return (
    <div className="mx-auto w-3/5 overflow-hidden">
      <canvas
        data-te-chart="line"
        data-te-dataset-label="Reporäntan, Sverige"
        data-te-labels="['Monday', 'Tuesday' , 'Wednesday' , 'Thursday' , 'Friday' , 'Saturday' , 'Sunday ']"
        data-te-dataset-data="[2112, 2343, 2545, 3423, 2365, 1985, 987]"
      ></canvas>
    </div>
  );
};

export default MyChart;
