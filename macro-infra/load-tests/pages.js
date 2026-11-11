import { SAMPLE, TIME_FRAME, chartDataBody } from './discovery.js';
import { endpoints, send } from './gateway.js';

const pick = (list) => list[Math.floor(Math.random() * list.length)];

const pickSome = (list, count) => {
  const start = Math.floor(Math.random() * list.length);
  return Array.from({ length: Math.min(count, list.length) }, (_, index) => list[(start + index) % list.length]);
};

const namesFrom = (data) => (data.chartSeries.length ? data.chartSeries : data.seriesNames);

const postChartData = (names, sample, timeFrame) => {
  if (names.length > 0) {
    send(endpoints.chartData, { body: chartDataBody(names, sample, timeFrame) });
  }
};

const startPage = (data) => {
  send(endpoints.seriesConfig);
  postChartData(pickSome(namesFrom(data), 4), SAMPLE.DAY, TIME_FRAME.ONE_YEAR);
  postChartData(pickSome(namesFrom(data), 1), SAMPLE.DAY, TIME_FRAME.TEN_YEAR);
};

const chartPage = (data) => {
  send(endpoints.seriesConfig);
  postChartData(pickSome(namesFrom(data), 2), SAMPLE.MONTH, TIME_FRAME.TEN_YEAR);
  postChartData(pickSome(namesFrom(data), 3), SAMPLE.DAY, pick([TIME_FRAME.ONE_YEAR, TIME_FRAME.FIVE_YEAR, TIME_FRAME.ALL]));
};

const parliamentPage = (data) => {
  send(endpoints.parliamentElections);
  send(endpoints.composition);
  send(endpoints.parties);
  if (data.electionYears.length > 0) {
    const year = Math.random() < 0.8 ? data.electionYears[0] : pick(data.electionYears);
    send(endpoints.members, { query: { year } });
  }
};

const politicianPage = (data) => {
  if (data.memberIds.length > 0) {
    send(endpoints.member, { params: { id: pick(data.memberIds) } });
  }
  send(endpoints.parties);
  send(endpoints.parliamentElections);
};

const electionPage = (list, detail, codes) => {
  send(list);
  if (codes.length > 0) {
    send(detail, { params: { code: pick(codes) } });
  }
};

const regionalPage = (data) => {
  send(endpoints.indicators);
  if (data.levelsWithData.length > 0) {
    const shown = pick(data.levelsWithData);
    send(endpoints.values, { query: { indicator: shown.indicator, level: shown.level } });
    send(endpoints.values, { query: { indicator: shown.indicator, level: shown.level, year: pick(shown.years) } });
  } else if (data.indicatorIds.length > 0) {
    send(endpoints.values, { query: { indicator: pick(data.indicatorIds), level: 'DeSO' } });
  }
};

// Weights are a guess at how the site is used: the start page is the landing page, the maps and the Riksdag follow, and
// the smaller maps get less. A page issues the calls the real page makes on load, so the start page asks for one trend:
// the tabs only mount the active one, and the others fetch when clicked
export const pages = [
  { name: 'start', weight: 30, visit: startPage },
  { name: 'macroindicator', weight: 15, visit: chartPage },
  { name: 'parliament', weight: 15, visit: parliamentPage },
  { name: 'politician', weight: 10, visit: politicianPage },
  { name: 'municipalities', weight: 10, visit: (d) => electionPage(endpoints.municipalityElections, endpoints.municipality, d.municipalities) },
  { name: 'regions', weight: 5, visit: (d) => electionPage(endpoints.regionElections, endpoints.region, d.regions) },
  { name: 'constituencies', weight: 5, visit: (d) => electionPage(endpoints.constituencyElections, endpoints.constituency, d.constituencies) },
  { name: 'omraden', weight: 10, visit: regionalPage },
];

const totalWeight = pages.reduce((sum, page) => sum + page.weight, 0);

export const weightedPage = () => {
  let remaining = Math.random() * totalWeight;
  for (const page of pages) {
    remaining -= page.weight;
    if (remaining < 0) {
      return page;
    }
  }
  return pages[pages.length - 1];
};
