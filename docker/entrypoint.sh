#!/bin/sh
# Serve the trusted library with `lcaac serve`.
#
# Environment:
#   LCA_DIRECTORY            trusted library, holds lcaac.yaml (default: /trusted_library)
#   LCA_LIB_DIRECTORY        .lca sources, relative to LCA_DIRECTORY (default: lib)
#   LCA_INVENTORY_DIRECTORY  CSV inventory, relative to LCA_DIRECTORY (default: inventory);
#                            must match the csv connector directory of lcaac.yaml
#   LCA_ARCHIVE_DIRECTORY    archives, relative to LCA_DIRECTORY (default: archive)
#   LCA_INVENTORY_ZIP        optional zip in LCA_ARCHIVE_DIRECTORY, extracted over the inventory at startup
#   PORT                     port to listen on (default: 8080)
#
# Extra arguments are passed to `lcaac serve`.
set -eu

LCA_DIRECTORY="${LCA_DIRECTORY:-/trusted_library}"
LCA_LIB_DIRECTORY="${LCA_LIB_DIRECTORY:-lib}"
LCA_INVENTORY_DIRECTORY="${LCA_INVENTORY_DIRECTORY:-inventory}"
LCA_ARCHIVE_DIRECTORY="${LCA_ARCHIVE_DIRECTORY:-archive}"
PORT="${PORT:-8080}"

if [ -n "${LCA_INVENTORY_ZIP:-}" ]; then
    zip="$LCA_DIRECTORY/$LCA_ARCHIVE_DIRECTORY/$LCA_INVENTORY_ZIP"
    if [ -f "$zip" ]; then
        echo "Extracting $zip to $LCA_DIRECTORY/$LCA_INVENTORY_DIRECTORY"
        unzip -o -q "$zip" -d "$LCA_DIRECTORY/$LCA_INVENTORY_DIRECTORY"
    else
        echo "File $zip is not a regular file"
    fi
fi

exec lcaac serve \
    -c "$LCA_DIRECTORY/lcaac.yaml" \
    -s "$LCA_DIRECTORY/$LCA_LIB_DIRECTORY" \
    --host 0.0.0.0 \
    -p "$PORT" \
    "$@"
