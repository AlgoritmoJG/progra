#!/usr/bin/env bash
# Cargar desde la raíz del proyecto. No imprimir las variables ni activar set -x.
[[ -f .local/mysql.env && -f .local/mysql-truststore.p12 ]] || { echo 'Ejecute scripts/mysql-local.sh primero.' >&2; return 1; }
source .local/mysql.env
export POS_DB_USER="$MYSQL_USER" POS_DB_PASSWORD="$MYSQL_PASSWORD"
export POS_DB_URL="jdbc:mysql://127.0.0.1:3307/${1:-pos_dev}?sslMode=VERIFY_CA&trustCertificateKeyStoreUrl=file:$PWD/.local/mysql-truststore.p12&trustCertificateKeyStorePassword=changeit&trustCertificateKeyStoreType=PKCS12&connectionTimeZone=LOCAL"
