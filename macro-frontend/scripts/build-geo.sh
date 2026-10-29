#!/bin/sh
# Rebuilds public/geo/*.topo.json from SCB's "Digitala gränser" (CC0, SWEREF 99 TM).
# Run from macro-frontend/ when SCB publishes new boundaries; update SCB_URL to the latest file first.
set -eu

SCB_URL="https://www.scb.se/contentassets/3443fea3fa6640f7a57ea15d9a372d33/shape_svenska_260225.zip"
MAPSHAPER="npx -y mapshaper@0.6.102"
OUT_DIR="public/geo"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

curl -sSfL -o "$WORK_DIR/shape.zip" "$SCB_URL"
unzip -q "$WORK_DIR/shape.zip" -d "$WORK_DIR"
unzip -q "$WORK_DIR/Kommun_Sweref99TM.zip" -d "$WORK_DIR/kommun"
unzip -q "$WORK_DIR/LanSweref99TM.zip" -d "$WORK_DIR/lan"
mkdir -p "$OUT_DIR"

$MAPSHAPER "$WORK_DIR"/kommun/*.shp \
  -rename-fields code=KnKod,name=KnNamn \
  -proj wgs84 \
  -simplify 60% keep-shapes \
  -rename-layers kommuner \
  -o "$OUT_DIR/kommuner.topo.json" format=topojson quantization=1e5

$MAPSHAPER "$WORK_DIR"/lan/*.shp \
  -rename-fields code=LnKod,name=LnNamn \
  -proj wgs84 \
  -simplify 60% keep-shapes \
  -rename-layers lan \
  -o "$OUT_DIR/lan.topo.json" format=topojson quantization=1e5

# Riksdag valkretsar are whole kommuner (scripts/valkretsar.csv), so they are dissolved from the kommun layer
$MAPSHAPER "$WORK_DIR"/kommun/*.shp \
  -rename-fields code=KnKod,name=KnNamn \
  -join scripts/valkretsar.csv keys=code,kommunkod string-fields=kommunkod,valkretskod \
  -dissolve valkretskod copy-fields=valkretsnamn \
  -rename-fields code=valkretskod,name=valkretsnamn \
  -proj wgs84 \
  -simplify 60% keep-shapes \
  -rename-layers valkretsar \
  -o "$OUT_DIR/valkretsar.topo.json" format=topojson quantization=1e5
