'use client';

import ky from 'ky';

const ScrapeAllButton = () => {
  const scrapeAll = async () => {
    const httpConfig = { timeout: 10000 };

    console.log('scraping sweden policy rates...');
    const sPolicyRateUrl = 'http://localhost:8080/scrape/policy-rate/sweden';
    const swedenPolicyRate = await ky.post(sPolicyRateUrl, httpConfig);
    const sPolicyArray = await swedenPolicyRate.json();
    setTimeout(function () {
      console.log('scraped ' + sPolicyArray.length + ' items');
    }, 1000);

    console.log('scraping usd-sek exchange rate...');
    const usdSekUrl = 'http://localhost:8080/scrape/exchange-rate/usd-sek';
    const exchangeRateUsdSek = await ky.post(usdSekUrl, httpConfig);
    const usdSekArray = await exchangeRateUsdSek.json();
    setTimeout(function () {
      console.log('scraped ' + usdSekArray.length + ' items');
    }, 1000);

    console.log('scraping sweden government bills...');
    const gbSwedenUrl = 'http://localhost:8080/scrape/government-bills/sweden';
    const govBillsSweden = await ky.post(gbSwedenUrl, httpConfig);
    const govBillsSwedenArray = await govBillsSweden.json();
    setTimeout(function () {
      console.log('scraped ' + govBillsSwedenArray.length + ' items');
    }, 1000);

    console.log('done');
  };

  return (
    <button
      type="button"
      className="inline-block rounded bg-primary px-6 pb-2 pt-2.5 text-xs font-medium uppercase leading-normal text-white shadow-[0_4px_9px_-4px_#3b71ca] transition duration-150 ease-in-out hover:bg-primary-600 hover:shadow-[0_8px_9px_-4px_rgba(59,113,202,0.3),0_4px_18px_0_rgba(59,113,202,0.2)] focus:bg-primary-600 focus:shadow-[0_8px_9px_-4px_rgba(59,113,202,0.3),0_4px_18px_0_rgba(59,113,202,0.2)] focus:outline-none focus:ring-0 active:bg-primary-700 active:shadow-[0_8px_9px_-4px_rgba(59,113,202,0.3),0_4px_18px_0_rgba(59,113,202,0.2)] dark:shadow-[0_4px_9px_-4px_rgba(59,113,202,0.5)] dark:hover:shadow-[0_8px_9px_-4px_rgba(59,113,202,0.2),0_4px_18px_0_rgba(59,113,202,0.1)] dark:focus:shadow-[0_8px_9px_-4px_rgba(59,113,202,0.2),0_4px_18px_0_rgba(59,113,202,0.1)] dark:active:shadow-[0_8px_9px_-4px_rgba(59,113,202,0.2),0_4px_18px_0_rgba(59,113,202,0.1)]"
      onClick={scrapeAll}
    >
      Scrape All Data Points
    </button>
  );
};

export default ScrapeAllButton;
