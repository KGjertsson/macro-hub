import exec from 'k6/execution';
import { describeDiscovery, discover, reportNoData } from './discovery.js';
import { pages, weightedPage } from './pages.js';
import { selectedProfile } from './profiles.js';

const profile = selectedProfile();

export const options = {
  scenarios: profile.scenarios,
  thresholds: profile.thresholds,
  setupTimeout: '3m',
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'max'],
  // Without this every sample also carries the url tag, which would be one series per area code and member id
  systemTags: ['status', 'method', 'name', 'scenario', 'expected_response', 'check'],
};

export function setup() {
  const found = discover();
  console.log(`Discovered ${describeDiscovery(found)}`);
  reportNoData(found);
  return found;
}

export default function (data) {
  const page = profile.ordered ? pages[exec.scenario.iterationInTest % pages.length] : weightedPage();
  page.visit(data);
}

export function teardown(data) {
  reportNoData(data);
}
