import { keepPreviousData, useQuery } from '@tanstack/react-query';

// Photon (komoot) geocodes OpenStreetMap data; the bbox keeps answers around Sweden.
// Photon rejects lang=sv, and its default language returns the local (Swedish) names anyway
const PHOTON_URL = 'https://photon.komoot.io/api/';
const SWEDEN_BBOX = '10.5,55.0,24.3,69.1';
const MIN_QUERY_LENGTH = 3;

interface PhotonFeature {
  geometry: { type: 'Point'; coordinates: [number, number] };
  properties: {
    osm_id: number;
    osm_type: string;
    countrycode?: string;
    name?: string;
    street?: string;
    housenumber?: string;
    postcode?: string;
    city?: string;
    district?: string;
    locality?: string;
    county?: string;
  };
}

export interface AddressHit {
  id: string;
  label: string;
  detail: string;
  lngLat: [number, number];
}

const toHit = ({ geometry, properties: p }: PhotonFeature): AddressHit => {
  const street = p.street ? [p.street, p.housenumber].filter(Boolean).join(' ') : undefined;
  const label = p.name ?? street ?? p.city ?? '';
  const detail = [p.name ? street : undefined, [p.postcode, p.city ?? p.locality].filter(Boolean).join(' '), p.county]
    .filter(part => part && part !== label)
    .join(', ');
  return { id: `${p.osm_type}${p.osm_id}`, label, detail, lngLat: geometry.coordinates };
};

export const useAddressSearch = (query: string) => {
  const trimmed = query.trim();
  return useQuery({
    queryKey: ['address-search', trimmed],
    queryFn: async ({ signal }) => {
      const params = new URLSearchParams({ q: trimmed, limit: '6', bbox: SWEDEN_BBOX });
      const response = await fetch(`${PHOTON_URL}?${params}`, { signal });
      if (!response.ok) {
        throw new Error(`Photon search failed with ${response.status}`);
      }
      const body = (await response.json()) as { features: PhotonFeature[] };
      // The bbox also covers parts of Norway, Finland and Denmark
      return body.features.filter(f => f.properties.countrycode === 'SE').map(toHit);
    },
    enabled: trimmed.length >= MIN_QUERY_LENGTH,
    staleTime: Infinity,
    placeholderData: keepPreviousData,
  });
};
