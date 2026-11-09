import { check } from 'k6';
import exec from 'k6/execution';
import http from 'k6/http';

const GATEWAY = (__ENV.GATEWAY || '').replace(/\/+$/, '');
const API_PREFIX = '/macro-analyzer/';

export const DISCOVERY_GROUP = 'discovery';

// The groups are the Macro Hub Gateway dashboard's route labels, so a group can be followed from one dashboard to the other
const endpoint = (group, method, template) => ({ group, method, template, name: `${method} ${template}` });

export const endpoints = {
  seriesConfig: endpoint('series-config', 'GET', '/macro-analyzer/series-config'),
  chartData: endpoint('chart-data', 'POST', '/macro-analyzer/chart-data'),
  parties: endpoint('parliament', 'GET', '/macro-analyzer/parliament/parties'),
  parliamentElections: endpoint('parliament', 'GET', '/macro-analyzer/parliament/elections'),
  composition: endpoint('parliament', 'GET', '/macro-analyzer/parliament/composition'),
  members: endpoint('parliament', 'GET', '/macro-analyzer/parliament/members'),
  member: endpoint('parliament', 'GET', '/macro-analyzer/parliament/members/{id}'),
  municipalityElections: endpoint('municipalities', 'GET', '/macro-analyzer/municipalities/elections'),
  municipality: endpoint('municipalities', 'GET', '/macro-analyzer/municipalities/{code}'),
  regionElections: endpoint('regions', 'GET', '/macro-analyzer/regions/elections'),
  region: endpoint('regions', 'GET', '/macro-analyzer/regions/{code}'),
  constituencyElections: endpoint('constituencies', 'GET', '/macro-analyzer/constituencies/elections'),
  constituency: endpoint('constituencies', 'GET', '/macro-analyzer/constituencies/{code}'),
  indicators: endpoint('regional-statistics', 'GET', '/macro-analyzer/regional-statistics/indicators'),
  values: endpoint('regional-statistics', 'GET', '/macro-analyzer/regional-statistics/values'),
};

export const endpointGroups = [...new Set(Object.values(endpoints).map((e) => e.group))];

const decodedPath = (path) => {
  try {
    return decodeURIComponent(path.split('?')[0]);
  } catch {
    return null;
  }
};

export const refusalReason = (path) => {
  const decoded = decodedPath(path);
  if (decoded === null) {
    return 'its path cannot be decoded';
  }
  if (!decoded.startsWith(API_PREFIX)) {
    return `it is outside ${API_PREFIX}: no telemetry, static files or third parties`;
  }
  // Compared after decoding and per segment, so that %2Fscrape or a parent segment can't reach a scrape route
  if (decoded.split('/').some((segment) => segment.toLowerCase() === 'scrape')) {
    return 'it is a scrape path';
  }
  return null;
};

const fillTemplate = (template, params) =>
  template.replace(/\{(\w+)\}/g, (_, key) => encodeURIComponent(params[key]));

const queryString = (query) => {
  const parts = Object.entries(query).map(([key, value]) => `${key}=${encodeURIComponent(value)}`);
  return parts.length ? `?${parts.join('&')}` : '';
};

// 204 is a valid answer (nothing scraped yet); a 3xx would be a misrouted request, so it doesn't count as success
http.setResponseCallback(http.expectedStatuses(200, 204));

export const send = (target, { params = {}, query = {}, body = null, discovery = false } = {}) => {
  const path = fillTemplate(target.template, params) + queryString(query);
  const reason = refusalReason(path);
  if (reason) {
    exec.test.abort(`Refusing ${target.method} ${path}: ${reason}`);
  }
  const group = discovery ? DISCOVERY_GROUP : target.group;
  const response = http.request(target.method, GATEWAY + path, body, {
    headers: target.method === 'POST' ? { 'Content-Type': 'application/json' } : {},
    tags: { name: target.name, endpoint_group: group },
    // Bodies are discarded unless discovery needs one: a VU doesn't need them, and regional values are about 200 KB each
    responseType: discovery ? 'text' : 'none',
  });
  if (!discovery) {
    check(response, { 'status is 200 or 204': (r) => r.status === 200 || r.status === 204 }, { endpoint_group: group });
  }
  return response;
};

export const stopRun = (message) => {
  exec.test.abort(message);
};
