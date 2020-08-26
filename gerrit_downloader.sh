#!/bin/sh

# ------------
# Preparations
# ------------

script_path=$(dirname "$0")

# -----
# Build
# -----

"${script_path}/gradlew" -p "${script_path}" assemble

# -------------
# Download data
# -------------

java -Xmx4096m -Xms256m -jar "${script_path}/GerritDownloader/build/libs/GerritDownloader.jar" "$@"
