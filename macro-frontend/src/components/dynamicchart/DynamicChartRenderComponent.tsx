import React from 'react';
import { RotateCcw } from 'lucide-react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { rootUrl, SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';
import DynamicChartComponent from '@/components/dynamicchart/DynamicChartComponent';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { SeriesConfig } from '@/models/SeriesConfig';
import { Dataset } from '@/models/Dataset';
import { ChartSeries, SeriesStyle, toIndexMode } from '@/components/dynamicchart/seriesMetadata';
import { ChartZoom, resolveZoom, sliceLabels, sliceSeries } from '@/components/dynamicchart/chartZoom';

interface Props {
  selectedItems: SeriesConfig[];
  styles: Record<string, SeriesStyle>;
  sampleStrategy: SAMPLE_STRATEGY;
  timeFrame: TIME_FRAME;
  indexed: boolean;
  zoom?: ChartZoom;
  onZoomChange: (zoom: ChartZoom | undefined) => void;
}

type ChartLabel = number | string | number[];

type ChartDataResponse = {
  labels: ChartLabel[];
  chartData: Dataset[];
};

// Dates arrive as arrays ([2024, 6] for months, [2024, 6, 1] for days) or as a plain year
const formatLabel = (label: ChartLabel): string =>
  Array.isArray(label)
    ? label.map((part, index) => (index === 0 ? String(part) : String(part).padStart(2, '0'))).join('-')
    : String(label);

const Hint = ({ children }: { children: React.ReactNode }) => (
  <p className='text-sm text-muted-foreground'>{children}</p>
);

const DynamicChartRenderComponent = ({
  selectedItems,
  styles,
  sampleStrategy,
  timeFrame,
  indexed,
  zoom,
  onZoomChange
}: Props) => {
  const body = {
    strategy: sampleStrategy,
    timeFrame: timeFrame,
    chartSeriesParams: selectedItems.map((config) => {
      return {
        name: config.name,
      };
    }),
  };

  // The backend answers 204 when the selected series have no data points
  const fetchChartData = async (): Promise<ChartDataResponse | null> => {
    const res = await fetch(rootUrl + '/macro-analyzer/chart-data', {
      method: 'POST',
      body: JSON.stringify(body),
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (res.status === 204) {
      return null;
    }
    if (!res.ok) {
      throw new Error(`servern svarade med status ${res.status}`);
    }
    return res.json();
  };

  const { isPending, error, data } = useQuery({
    queryKey: ['chartData', sampleStrategy, timeFrame, selectedItems.map((i) => i.name)],
    queryFn: fetchChartData,
    enabled: selectedItems.length > 0,
    // Keep showing the previous chart while a changed selection loads
    placeholderData: keepPreviousData,
  });

  if (selectedItems.length === 0) {
    return <Hint>Välj minst en serie med ”Lägg till serie” för att visa en graf.</Hint>;
  }

  if (isPending) return <Skeleton className='h-[min(60vh,560px)] min-h-[320px] w-full' />;

  if (error) return <p className='text-sm text-destructive'>Ett fel uppstod: {error.message}</p>;

  if (!data) {
    return <Hint>Ingen data finns för de valda serierna i den valda tidsramen.</Hint>;
  }

  // While a new selection loads the response may still be the previous one, so match by name
  const allSeries: ChartSeries[] = data.chartData.flatMap((dataset) => {
    const config = selectedItems.find((item) => item.name === dataset.name);
    return config ? [{ config, style: styles[config.name], values: dataset.values, rawValues: dataset.values }] : [];
  });

  // A zoom is a window over the fetched data, applied before indexing so series rebase at its start
  const allLabels = data.labels.map(formatLabel);
  const zoomWindow = resolveZoom(allLabels, zoom);
  const labels = sliceLabels(allLabels, zoomWindow);
  const series = sliceSeries(allSeries, zoomWindow);

  // A series with no data points in the window still keeps its chip, but is noted instead of plotted
  const withoutData = series.filter((s) => s.values.every((v) => v === null));
  const withData = series.filter((s) => !withoutData.includes(s));

  const { plotted, notIndexable } = indexed ? toIndexMode(withData) : { plotted: withData, notIndexable: [] };

  // Axes only for units that have something to draw, in the order the series were added
  const units = Array.from(new Set(
    selectedItems.filter((item) => withData.some((s) => s.config === item)).map((item) => item.unit ?? '')
  ));

  return (
    <div className='flex flex-col gap-3'>
      {!indexed && units.length > 2 && (
        <Hint>
          De valda serierna har {units.length} olika enheter, men grafen har bara två axlar: {units.slice(1).join(', ')} delar
          den högra. Slå på ”Index = 100 vid start” för att jämföra utvecklingen på en gemensam axel.
        </Hint>
      )}
      {indexed && plotted.some((s) => s.indexKind === 'change') && (
        <Hint>
          Serier i procent indexeras inte utan visas som förändring i procentenheter (p.e.) sedan start
          {plotted.some((s) => s.indexKind === 'index') ? ', på den högra axeln' : ''}.
        </Hint>
      )}
      {withoutData.length > 0 && (
        <Hint>Ingen data i perioden: {withoutData.map((s) => s.config.displayName).join(', ')}.</Hint>
      )}
      {notIndexable.length > 0 && (
        <Hint>
          Kan inte indexeras eftersom startvärdet är noll eller negativt, visas inte:{' '}
          {notIndexable.map((s) => s.config.displayName).join(', ')}.
        </Hint>
      )}
      {plotted.length > 0 && (
        <>
          <DynamicChartComponent
            labels={labels}
            series={plotted}
            indexed={indexed}
            units={units}
            onZoom={(start, end) => onZoomChange([labels[start], labels[end]])}
            onResetZoom={() => zoomWindow && onZoomChange(undefined)}
          />
          {zoomWindow ? (
            <div className='flex flex-wrap items-center gap-3'>
              <Button variant='outline' size='sm' onClick={() => onZoomChange(undefined)}>
                <RotateCcw />
                Visa hela perioden
              </Button>
              <Hint>
                Visar {labels[0]} – {labels[labels.length - 1]}. Dubbelklicka i grafen för att återställa.
              </Hint>
            </div>
          ) : (
            labels.length > 1 && <Hint>Dra i grafen för att zooma in på en period.</Hint>
          )}
        </>
      )}
    </div>
  );
};

export default DynamicChartRenderComponent;
