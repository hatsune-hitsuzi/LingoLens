#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
SQL_FILE="${PROJECT_DIR}/sql/20260624_repair_dirty_word_asset_payloads.sql"

: "${LINGOLENS_DB_HOST:=159.75.140.18}"
: "${LINGOLENS_DB_PORT:=5432}"
: "${LINGOLENS_DB_NAME:=lingolens}"
: "${LINGOLENS_DB_USER:=postgres}"
: "${LINGOLENS_DB_PASSWORD:?Please export LINGOLENS_DB_PASSWORD first.}"

PGPASSWORD="${LINGOLENS_DB_PASSWORD}" psql \
  -h "${LINGOLENS_DB_HOST}" \
  -p "${LINGOLENS_DB_PORT}" \
  -U "${LINGOLENS_DB_USER}" \
  -d "${LINGOLENS_DB_NAME}" \
  -v ON_ERROR_STOP=1 \
  -f "${SQL_FILE}"

echo "dirty word assets repaired"
