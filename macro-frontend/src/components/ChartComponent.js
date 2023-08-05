import { useEffect } from 'react';

const ChartComponent = ({ values, labels }) => {
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
        data-te-dataset-label="Styrränta, Sverige"
        data-te-labels={labels}
        data-te-dataset-data={values}
      ></canvas>
    </div>
  );
};

export default ChartComponent;
