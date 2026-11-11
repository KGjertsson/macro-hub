import { endpointGroups } from './gateway.js';
import { pages } from './pages.js';

const number = (name, fallback) => {
  const raw = __ENV[name];
  if (raw === undefined || raw === '') {
    return fallback;
  }
  const value = Number(raw);
  if (Number.isNaN(value) || value < 0) {
    throw new Error(`${name} must be a number, got "${raw}"`);
  }
  return value;
};

const smoke = () => ({
  scenarios: {
    smoke: { executor: 'per-vu-iterations', vus: 1, iterations: pages.length * number('LOAD_TEST_SMOKE_ROUNDS', 3), maxDuration: '5m' },
  },
  ordered: true,
  p95Ms: 5000,
  errorRate: 0.01,
});

const load = () => ({
  scenarios: {
    load: {
      executor: 'constant-arrival-rate',
      rate: number('LOAD_TEST_RATE', 5),
      timeUnit: '1s',
      duration: __ENV.LOAD_TEST_DURATION || '5m',
      preAllocatedVUs: 20,
      maxVUs: number('LOAD_TEST_MAX_VUS', 200),
    },
  },
  ordered: false,
  p95Ms: 1000,
  errorRate: 0.01,
});

const stressStages = (maxRate, holdSeconds) => {
  const rampSeconds = Math.ceil(holdSeconds / 2);
  return [16, 8, 4, 2, 1].flatMap((divisor) => {
    const target = Math.max(1, Math.round(maxRate / divisor));
    return [
      { target, duration: `${rampSeconds}s` },
      { target, duration: `${holdSeconds}s` },
    ];
  });
};

const stress = () => ({
  scenarios: {
    stress: {
      executor: 'ramping-arrival-rate',
      startRate: 1,
      timeUnit: '1s',
      stages: stressStages(number('LOAD_TEST_STRESS_MAX_RATE', 160), number('LOAD_TEST_STRESS_STEP_SECONDS', 60)),
      preAllocatedVUs: 50,
      maxVUs: number('LOAD_TEST_MAX_VUS', 500),
    },
  },
  ordered: false,
  p95Ms: 5000,
  errorRate: 0.1,
  abortOnFail: true,
});

const PROFILES = { smoke, load, stress };

const thresholds = ({ p95Ms, errorRate, abortOnFail }) => {
  const failed = abortOnFail
    ? [{ threshold: `rate<${errorRate}`, abortOnFail: true, delayAbortEval: '30s' }]
    : [`rate<${errorRate}`];
  return Object.fromEntries(
    endpointGroups.flatMap((group) => [
      [`http_req_duration{endpoint_group:${group}}`, [`p(95)<${p95Ms}`]],
      [`http_req_failed{endpoint_group:${group}}`, failed],
      // Never fails: a threshold is what makes k6 print a metric per group in the summary
      [`http_reqs{endpoint_group:${group}}`, ['count>=0']],
    ]),
  );
};

export const selectedProfile = () => {
  const name = __ENV.PROFILE;
  if (!PROFILES[name]) {
    throw new Error(`PROFILE must be one of ${Object.keys(PROFILES).join(', ')}, got "${name}"`);
  }
  const profile = PROFILES[name]();
  return {
    name,
    ordered: profile.ordered,
    scenarios: profile.scenarios,
    thresholds: thresholds({
      p95Ms: number('LOAD_TEST_P95_MS', profile.p95Ms),
      errorRate: number('LOAD_TEST_ERROR_RATE', profile.errorRate),
      abortOnFail: profile.abortOnFail,
    }),
  };
};
