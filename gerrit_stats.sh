#!/bin/bash

#
# Runs GerritStats, generating HTML output by default.
#

# ------------
# Preparations
# ------------

script_path=$(dirname "$0")
output_dir="${script_path}/out-html"

new_args=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --output-dir | -o)
            output_dir=$2
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

output_dir=$(realpath "${output_dir}")
mkdir -p "${output_dir}/data"

# -------------
# Generate data
# -------------

java -Xmx4096m -Xms256m -jar "${script_path}/GerritStats/build/libs/GerritStats.jar" -o "${output_dir}/data" "${new_args[@]}"
exit_code=$?

if [[ ${exit_code} -ne 0 ]]; then
    echo "ERROR: Data processing failed, please check above log."
    exit 1
fi

# ----------------
# Generate website
# ----------------

cd "${script_path}/GerritStats" || exit
npm run webpack -o "${output_dir}/bundle.js"
exit_code=$?
cd - || exit

if [[ ${exit_code} -ne 0 ]]; then
    echo "ERROR: Webpack failed, please check above log."
fi

echo
echo "Output generated to ${output_dir}"
