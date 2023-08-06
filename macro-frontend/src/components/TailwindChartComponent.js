import { useEffect, useState } from 'react';

const TailwindChartComponent = ({ values, labels }) => {
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    const init = async () => {
      if (!initialized) {
        console.log('running init thingy');
        const { Chart, initTE } = await import('tw-elements');
        initTE({ Chart });
        setInitialized(true);
      } else {
        console.log('use effect triggered again but already initialized');
      }
    };
    init();
  }, [values, labels]);

  return (
    <div className="mx-auto w-3/5 overflow-hidden">
      <div>{values}</div>
      <div>{labels}</div>
      <canvas
        data-te-chart="line"
        data-te-dataset-label="Styrränta, Sverige"
        data-te-labels={labels}
        data-te-dataset-data={values}
      />
    </div>
  );
};

export default TailwindChartComponent;
