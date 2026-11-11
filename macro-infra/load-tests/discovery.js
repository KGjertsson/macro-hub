import { endpoints, send, stopRun } from './gateway.js';

export const SAMPLE = { DAY: 0, MONTH: 1, YEAR: 2 };
export const TIME_FRAME = { ALL: 0, ONE_YEAR: 1, FIVE_YEAR: 2, TEN_YEAR: 3 };

const readJson = (target, options) => {
  const response = send(target, { ...options, discovery: true });
  if (response.status === 0) {
    stopRun(`Cannot reach the gateway ${__ENV.GATEWAY}: ${response.error}. Is the Compose stack running?`);
  }
  if (response.status === 204) {
    return null;
  }
  if (response.status !== 200) {
    stopRun(
      `${target.name} answered ${response.status} during discovery, so a service behind the gateway is down or broken. ` +
        'Fix that first: the load test would only measure the failure.',
    );
  }
  return response.json();
};

export const chartDataBody = (names, sample, timeFrame) =>
  JSON.stringify({ strategy: sample, timeFrame, chartSeriesParams: names.map((name) => ({ name })) });

const seriesWithData = (names) =>
  names.filter(
    (name) =>
      send(endpoints.chartData, { body: chartDataBody([name], SAMPLE.YEAR, TIME_FRAME.ALL), discovery: true }).status ===
      200,
  );

const areaCodes = (target) => (readJson(target) || []).map((area) => area.code);

const discoverRegional = () => {
  const indicators = readJson(endpoints.indicators) || [];
  const levelsWithData = indicators.flatMap((indicator) =>
    ['DeSO', 'RegSO']
      .filter((level) => indicator.availability[level].length > 0)
      .map((level) => ({ indicator: indicator.id, level, years: indicator.availability[level].map((a) => a.year) })),
  );
  return { indicatorIds: indicators.map((indicator) => indicator.id), levelsWithData };
};

export const discover = () => {
  const seriesNames = (readJson(endpoints.seriesConfig) || []).map((config) => config.name);
  const termsWithMembers = (readJson(endpoints.parliamentElections) || [])
    .map((election) => ({
      year: election.year,
      members: readJson(endpoints.members, { query: { year: election.year } }) || [],
    }))
    .filter((term) => term.members.length > 0);
  return {
    seriesNames,
    chartSeries: seriesWithData(seriesNames),
    electionYears: termsWithMembers.map((term) => term.year),
    memberIds: termsWithMembers.length ? termsWithMembers[0].members.map((member) => member.id) : [],
    municipalities: areaCodes(endpoints.municipalityElections),
    regions: areaCodes(endpoints.regionElections),
    constituencies: areaCodes(endpoints.constituencyElections),
    ...discoverRegional(),
  };
};

const noDataLines = (found) => {
  const lines = [];
  if (found.chartSeries.length === 0) {
    lines.push(`chart-data: none of the ${found.seriesNames.length} series has data points, so it answers 204`);
  }
  if (found.memberIds.length === 0) {
    lines.push('parliament: no members, so members and members/{id} are skipped');
  }
  [
    ['municipalities', found.municipalities],
    ['regions', found.regions],
    ['constituencies', found.constituencies],
  ]
    .filter(([, codes]) => codes.length === 0)
    .forEach(([kind]) => lines.push(`${kind}: no election results, so ${kind}/{code} is skipped`));
  if (found.levelsWithData.length === 0) {
    lines.push('regional-statistics: no indicator has data, so values answers 204');
  }
  return lines;
};

export const describeDiscovery = (found) =>
  `${found.seriesNames.length} chart series (${found.chartSeries.length} with data), ` +
  `${found.memberIds.length} members, ${found.municipalities.length} municipalities, ${found.regions.length} regions, ` +
  `${found.constituencies.length} constituencies, ${found.levelsWithData.length} indicator levels with data`;

export const reportNoData = (found) => {
  const lines = noDataLines(found);
  if (lines.length > 0) {
    console.warn("NO DATA YET, so these numbers don't say much. Populate the stack first with macro-infra/scripts/scrape-all.sh:");
    lines.forEach((line) => console.warn(`  ${line}`));
  }
};
