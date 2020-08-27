#!/bin/bash

#
# Runs GerritStats, generating HTML output by default.
#

# ------------
# Preparations
# ------------

script_path=$(dirname "$0")
generation_dir=$(realpath "${script_path}/GerritStats/out-html")
output_dir=$(realpath "${script_path}/out-html")

new_args=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --output-dir | -o)
            output_dir=$(realpath "$2")
            shift 2
            ;;

        *)
            new_args=(${new_args} $1)
            shift 1
            ;;
    esac
done

if [[ ! -d "${script_path}" ]]; then
    echo "ERROR: Script path not available: ${script_path}"
    exit 1
fi

# -----
# Build
# -----

"${script_path}/gradlew" -p "${script_path}" assemble

# -------------
# Generate data
# -------------

java -Xmx4096m -Xms256m -jar "${script_path}/GerritStats/build/libs/GerritStats.jar" -o "${generation_dir}" "${new_args[@]}"
exit_code=$?

if [[ ${exit_code} -ne 0 ]]; then
    echo "ERROR: Data processing failed, please check above log."
    exit 1
fi

# ----------------
# Generate website
# ----------------

npm run webpack --prefix "${script_path}/GerritStats" -o "${output_dir}/bundle.js"
exit_code=$?

if [[ ${exit_code} -ne 0 ]]; then
    echo "ERROR: Webpack failed, please check above log."
    exit 1
fi

mkdir -p "${output_dir}"
cp "${generation_dir}/index.html" "${output_dir}"

echo
echo "Output generated to ${output_dir}"
