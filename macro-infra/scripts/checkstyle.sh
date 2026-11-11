#!/bin/sh
# Usage: macro-infra/scripts/checkstyle.sh [file...]
# Checks the given Java files against macro-backend/checkstyle.xml, or the whole backend without
# arguments, and prints each violation as <path from the repo root>:<line>: <message>.
set -eu

root=$(cd "$(dirname "$0")/../.." && pwd)
backend="$root/macro-backend"

if [ -z "${JAVA_HOME:-}" ] && [ -d "$HOME/.sdkman/candidates/java/current" ]; then
  JAVA_HOME="$HOME/.sdkman/candidates/java/current"
  export JAVA_HOME
fi

files=""
if [ "$#" -eq 0 ]; then
  modules=$(sed -n 's:.*<module>\(.*\)</module>.*:\1:p' "$backend/pom.xml")
else
  for f in "$@"; do
    case "$f" in /*) ;; *) f="$PWD/$f" ;; esac
    case "$f" in
      */jooq/generated/*) ;;
      "$backend"/*.java) files="$files${f#"$root"/}
" ;;
    esac
  done
  modules=$(printf '%s' "$files" | sed -n 's:^macro-backend/\([^/]*\)/.*:\1:p' | sort -u)
fi

[ -n "$modules" ] || exit 0

violations=""
for module in $modules; do
  # Checkstyle only checks whole modules; the files are picked out of its output below.
  if ! output=$(cd "$backend" && ./mvnw -q -B checkstyle:check -pl "$module" 2>&1); then
    found=$(printf '%s\n' "$output" | sed -n \
      "s#^\[ERROR\] \(src/[^:]*\.java\):\[\([0-9]*\)[^]]*\] ([a-z]*) \(.*\)#macro-backend/$module/\1:\2: \3#p")
    if [ -z "$found" ]; then
      printf '%s\n' "$output" >&2
      exit 1
    fi
    violations="$violations$found
"
  fi
done

if [ -n "$files" ]; then
  patterns=$(mktemp)
  printf '%s' "$files" | sed 's/$/:/' > "$patterns"
  violations=$(printf '%s' "$violations" | grep -F -f "$patterns" || true)
  rm -f "$patterns"
fi

if [ -n "$violations" ]; then
  printf '%s\n' "$violations" | sed '/^$/d' >&2
  exit 1
fi
