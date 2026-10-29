#!/bin/sh
# Rebuilds public/geo/{deso,regso}_*.topo.json from SCB's WFS (CC0) for the Områdesstatistik map.
# Run from macro-frontend/ (needs network, npx and node). DeSO names use the kommun names in public/geo/kommuner.topo.json.
set -eu

WFS_URL="https://geodata.scb.se/geoserver/stat/wfs"
MAPSHAPER="npx -y mapshaper@0.6.102"
OUT_DIR="public/geo"
# Douglas-Peucker tolerance in metres, scaled by feature size: ~4 m for inner-city areas, up to 150 m for large rural ones
INTERVAL="Math.min(150, Math.max(4, Math.sqrt(this.area) / 150))"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

mkdir -p "$OUT_DIR"
$MAPSHAPER "$OUT_DIR/kommuner.topo.json" -o "$WORK_DIR/kommuner.csv" format=csv

# Downloads a layer in one request (the server's CountDefault is 1,000,000) and fails if it doesn't match the hit count
download() {
  layer=$1
  query="service=WFS&version=2.0.0&request=GetFeature&typeNames=stat:$layer"
  curl -sSf -o "$WORK_DIR/$layer.json" "$WFS_URL?$query&outputFormat=application/json&srsName=EPSG:4326"
  expected=$(curl -sSf "$WFS_URL?$query&resultType=hits" | sed -n 's/.*numberMatched="\([0-9]*\)".*/\1/p')
  node -e '
    const [file, expected] = process.argv.slice(1);
    const n = JSON.parse(require("fs").readFileSync(file)).features.length;
    if (String(n) !== expected) { console.error(`${file}: got ${n} features, WFS reports ${expected}`); process.exit(1); }
  ' "$WORK_DIR/$layer.json" "$expected"
}

# Fails unless the output has the expected feature count, unique codes without version suffix, a name (with a matched kommun) and kommunkod on every feature, and a regsokod on every DeSO
verify() {
  file=$1
  expected=$2
  node -e '
    const [file, expected] = process.argv.slice(1);
    const name = require("path").basename(file, ".topo.json");
    const geometries = JSON.parse(require("fs").readFileSync(file)).objects[name].geometries;
    const codes = new Set();
    for (const g of geometries) {
      const p = g.properties;
      const badName = !p.name || p.name.startsWith("undefined");
      const badRegso = name.startsWith("deso") && !/^\d{4}R\d{3}$/.test(p.regsokod);
      if (!g.arcs || !/^\d{4}[ABCR]\d{3,4}$/.test(p.code) || badName || !/^\d{4}$/.test(p.kommunkod) || badRegso || codes.has(p.code)) {
        console.error(`${file}: bad feature ${JSON.stringify(p)}`); process.exit(1);
      }
      codes.add(p.code);
    }
    if (String(geometries.length) !== expected) { console.error(`${file}: ${geometries.length} features, expected ${expected}`); process.exit(1); }
  ' "$file" "$expected"
}

# Dissolves a downloaded layer into a land outline. The 2025 layers extend into territorial waters, so they are clipped to the 2018/2020 outline
land_outline() {
  $MAPSHAPER "$WORK_DIR/$1.json" -dissolve -o "$WORK_DIR/$1.land.json"
}

# Optional second argument: the layer whose land outline the output is clipped to
build_deso() {
  layer=$1
  land=${2:-}
  stem=$(echo "$layer" | tr '[:upper:]' '[:lower:]')
  download "$layer"
  set --
  if [ -n "$land" ]; then set -- -clip "$WORK_DIR/$land.land.json" remove-slivers; fi
  $MAPSHAPER "$WORK_DIR/$layer.json" \
    -rename-fields code=desokod \
    -join "$WORK_DIR/kommuner.csv" keys=kommunkod,code string-fields=code fields=name \
    -each 'name = name + " " + code' \
    -filter-fields code,name,kommunkod,regsokod \
    "$@" \
    -simplify dp variable interval="$INTERVAL" keep-shapes \
    -rename-layers "$stem" \
    -o "$OUT_DIR/$stem.topo.json" format=topojson quantization=1e6
  verify "$OUT_DIR/$stem.topo.json" "$expected"
}

build_regso() {
  layer=$1
  land=${2:-}
  stem=$(echo "$layer" | tr '[:upper:]' '[:lower:]')
  download "$layer"
  set --
  if [ -n "$land" ]; then set -- -clip "$WORK_DIR/$land.land.json" remove-slivers; fi
  $MAPSHAPER "$WORK_DIR/$layer.json" \
    -rename-fields code=regsokod,name=regsonamn \
    -filter-fields code,name,kommunkod \
    "$@" \
    -simplify dp variable interval="$INTERVAL" keep-shapes \
    -rename-layers "$stem" \
    -o "$OUT_DIR/$stem.topo.json" format=topojson quantization=1e6
  verify "$OUT_DIR/$stem.topo.json" "$expected"
}

build_deso DeSO_2018
land_outline DeSO_2018
build_deso DeSO_2025 DeSO_2018
build_regso RegSO_2020
land_outline RegSO_2020
build_regso RegSO_2025 RegSO_2020
