import { DATASET_NAMES } from '@/models/Constants';

export type LineConfig = {
  label: string;
  pointRadius: number;
};

export type Dataset = {
  values?: number[];
  labels?: string[];
  lineConfig: LineConfig;
  selected: boolean;
  name: DATASET_NAMES;
  url: string;
};

const policyRateSweden: Dataset = {
  lineConfig: {
    label: 'Sveriges styrränta',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.PolicyRateSweden,
  url: '/policy-rate/sweden',
};

const usdSekExchange: Dataset = {
  lineConfig: {
    label: 'USD till SEK växelkurs',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.UsdSekExchangeRate,
  url: '/exchange-rate/usd-sek',
};

const governmentBondSweden2Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 2 års löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden2Year,
  url: '/government-bonds/sweden?period=2',
};

const governmentBondSweden5Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 5 års löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden5Year,
  url: '/government-bonds/sweden?period=5',
};

const governmentBondSweden7Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 7 års löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden7Year,
  url: '/government-bonds/sweden?period=2',
};

const governmentBondSweden10Year: Dataset = {
  lineConfig: {
    label: 'Svenska Statsobligationer 10 års löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBondSweden10Year,
  url: '/government-bonds/sweden?period=2',
};

const governmentBillsSweden1Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 1-månads löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden1Month,
  url: '/government-bills/sweden?period=1',
};

const governmentBillsSweden3Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 3-månads löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden3Month,
  url: '/government-bills/sweden?period=3',
};

const governmentBillsSweden6Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 6-månads löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden6Month,
  url: '/government-bills/sweden?period=6',
};

const governmentBillsSweden12Month: Dataset = {
  lineConfig: {
    label: 'Svensk statsskuldväxel 12-månads löptid',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.GovernmentBillSweden12Month,
  url: '/government-bills/sweden?period=12',
};

const euroMarketRateDenmark3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Danmark',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateDenmark3Month,
  url: '/euro-market-rate?period=3month&country=denmark',
};

const euroMarketRateEur3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Eur',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateEur3Month,
  url: '/euro-market-rate?period=3month&country=eur',
};

const euroMarketRateGB3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: GB',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateGB3Month,
  url: '/euro-market-rate?period=3month&country=gb',
};

const euroMarketRateJapan3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Japan',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateJapan3Month,
  url: '/euro-market-rate?period=3month&country=japan',
};

const euroMarketRateNorway3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: Norge',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateNorway3Month,
  url: '/euro-market-rate?period=3month&country=norway',
};

const euroMarketRateUsa3Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 3-månaders löptid: USA',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateUsa3Month,
  url: '/euro-market-rate?period=3month&country=usa',
};

const euroMarketRateDenmark6Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 6-månaders löptid: Danmark',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateDenmark6Month,
  url: '/euro-market-rate?period=6month&country=denmark',
};

const euroMarketRateEur6Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 6-månaders löptid: Eur',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateEur6Month,
  url: '/euro-market-rate?period=6month&country=eur',
};

const euroMarketRateGB6Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 6-månaders löptid: GB',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateGB6Month,
  url: '/euro-market-rate?period=6month&country=gb',
};

const euroMarketRateJapan6Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 6-månaders löptid: Japan',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateJapan6Month,
  url: '/euro-market-rate?period=6month&country=japan',
};

const euroMarketRateNorway6Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 6-månaders löptid: Norge',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateNorway6Month,
  url: '/euro-market-rate?period=6month&country=norway',
};

const euroMarketRateUsa6Month: Dataset = {
  lineConfig: {
    label: 'Euromarknadsräntor 6-månaders löptid: USA',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.EuroMarketRateUsa6Month,
  url: '/euro-market-rate?period=6month&country=usa',
};

const internationalGovernmentBondEur5Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 5-års löptid: Eur',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsEur5Year,
  url: '/government-bonds/international?period=5year&country=eur',
};

const internationalGovernmentBondFrance5Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 5-års löptid: Frankrike',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsFrance5Year,
  url: '/government-bonds/international?period=5year&country=france',
};

const internationalGovernmentBondGB5Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 5-års löptid: GB',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsGB5Year,
  url: '/government-bonds/international?period=5year&country=gb',
};

const internationalGovernmentBondGermany5Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 5-års löptid: Tyskland',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsGermany5Year,
  url: '/government-bonds/international?period=5year&country=germany',
};

const internationalGovernmentBondJapan5Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 5-års löptid: Japan',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsJapan5Year,
  url: '/government-bonds/international?period=5year&country=japan',
};

const internationalGovernmentBondNetherlands5Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 5-års löptid: Nederländerna',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsNetherlands5Year,
  url: '/government-bonds/international?period=5year&country=netherlands',
};

const internationalGovernmentBondUsa5Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 5-års löptid: USA',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsUsa5Year,
  url: '/government-bonds/international?period=5year&country=usa',
};

const internationalGovernmentBondDenmark10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Danmark',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsDenmark10Year,
  url: '/government-bonds/international?period=10year&country=denmark',
};

const internationalGovernmentBondEur10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Eur',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsEur10Year,
  url: '/government-bonds/international?period=10year&country=eur',
};

const internationalGovernmentBondFinland10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Finland',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsFinland10Year,
  url: '/government-bonds/international?period=10year&country=finland',
};

const internationalGovernmentBondFrance10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Frankrike',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsFrance10Year,
  url: '/government-bonds/international?period=10year&country=france',
};

const internationalGovernmentBondGB10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: GB',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsGB10Year,
  url: '/government-bonds/international?period=10year&country=gb',
};

const internationalGovernmentBondGermany10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Germany',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsGermany10Year,
  url: '/government-bonds/international?period=10year&country=germany',
};

const internationalGovernmentBondJapan10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Japan',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsJapan10Year,
  url: '/government-bonds/international?period=10year&country=japan',
};

const internationalGovernmentBondNetherlands10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Nederländerna',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsNetherlands10Year,
  url: '/government-bonds/international?period=10year&country=netherlands',
};

const internationalGovernmentBondNorway10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: Norge',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsNorway10Year,
  url: '/government-bonds/international?period=10year&country=norway',
};

const internationalGovernmentBondUsa10Year: Dataset = {
  lineConfig: {
    label: 'Internationella statsobligationer 10-års löptid: USA',
    pointRadius: 2,
  },
  selected: false,
  name: DATASET_NAMES.InternationalGovernmentBondsUsa10Year,
  url: '/government-bonds/international?period=10year&country=usa',
};

export const defaultDataset = [
  policyRateSweden,
  usdSekExchange,
  governmentBondSweden2Year,
  governmentBondSweden5Year,
  governmentBondSweden7Year,
  governmentBondSweden10Year,
  governmentBillsSweden1Month,
  governmentBillsSweden3Month,
  governmentBillsSweden6Month,
  governmentBillsSweden12Month,
  euroMarketRateDenmark3Month,
  euroMarketRateEur3Month,
  euroMarketRateGB3Month,
  euroMarketRateJapan3Month,
  euroMarketRateNorway3Month,
  euroMarketRateUsa3Month,
  euroMarketRateDenmark6Month,
  euroMarketRateEur6Month,
  euroMarketRateGB6Month,
  euroMarketRateJapan6Month,
  euroMarketRateNorway6Month,
  euroMarketRateUsa6Month,
  internationalGovernmentBondEur5Year,
  internationalGovernmentBondFrance5Year,
  internationalGovernmentBondGB5Year,
  internationalGovernmentBondGermany5Year,
  internationalGovernmentBondJapan5Year,
  internationalGovernmentBondNetherlands5Year,
  internationalGovernmentBondUsa5Year,
  internationalGovernmentBondDenmark10Year,
  internationalGovernmentBondEur10Year,
  internationalGovernmentBondFinland10Year,
  internationalGovernmentBondFrance10Year,
  internationalGovernmentBondGB10Year,
  internationalGovernmentBondGermany10Year,
  internationalGovernmentBondJapan10Year,
  internationalGovernmentBondNetherlands10Year,
  internationalGovernmentBondNorway10Year,
  internationalGovernmentBondUsa10Year,
];
