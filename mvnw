#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Shell script
# ----------------------------------------------------------------------------

which mvn >/dev/null 2>&1 && exec mvn "$@"

echo "[ERROR] Maven not found. Please install Maven or make sure it is in PATH." >&2
exit 1
