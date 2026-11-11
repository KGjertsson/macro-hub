#!/bin/sh
# Claude Code PostToolUse hook for Edit and Write: runs Checkstyle on an edited Java file and hands
# the violations back to Claude (exit 2). The file may be in another worktree of this repository, so
# the script is taken from the file's own checkout.
file=$(python3 -I -c 'import json, sys; print(json.load(sys.stdin).get("tool_input", {}).get("file_path", ""))')

case "$file" in
  *.java) ;;
  *) exit 0 ;;
esac

checkout=$(git -C "$(dirname "$file")" rev-parse --show-toplevel 2>/dev/null) || exit 0
script="$checkout/macro-infra/scripts/checkstyle.sh"
[ -x "$script" ] || exit 0

output=$("$script" "$file" 2>&1)
case $? in
  0) ;;
  1)
    echo "Checkstyle (macro-backend/checkstyle.xml) rejects $file, fix it:" >&2
    printf '%s\n' "$output" | sed 's/^/  /' >&2
    exit 2
    ;;
  *)
    echo "Checkstyle could not run on $file:" >&2
    printf '%s\n' "$output" | tail -20 >&2
    exit 1
    ;;
esac
