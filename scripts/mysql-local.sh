#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/java-env.sh
mkdir -p .local
# Solo crea recursos de desarrollo de este proyecto; no usa instalaciones ajenas.
python3 - <<'PYTHON'
import pathlib,secrets
p=pathlib.Path('.local/mysql.env')
if not p.exists():
 p.write_text('MYSQL_ROOT_PASSWORD='+secrets.token_hex(24)+'\nMYSQL_DATABASE=pos_dev\nMYSQL_USER=pos\nMYSQL_PASSWORD='+secrets.token_hex(24)+'\n');p.chmod(0o600)
PYTHON
if docker container inspect progra-mysql-fase2 >/dev/null 2>&1; then
  docker start progra-mysql-fase2 >/dev/null
else
  docker run -d --name progra-mysql-fase2 --env-file .local/mysql.env -p 127.0.0.1:3307:3306 mysql:8.4 >/dev/null
fi
listo=false
for intento in {1..45}; do
  if docker exec progra-mysql-fase2 sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -Nse "SELECT 1"' >/dev/null 2>&1; then listo=true; break; fi
  sleep 1
done
[[ "$listo" == true ]] || { echo 'MySQL no quedó listo; consulte docker logs progra-mysql-fase2.' >&2; exit 1; }
# La base de pruebas está separada y es desechable.
docker exec -i progra-mysql-fase2 sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot' <<'SQL'
CREATE DATABASE IF NOT EXISTS pos_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
GRANT ALL ON pos_test.* TO 'pos'@'%';
SQL
tablas=$(docker exec progra-mysql-fase2 sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot pos_dev -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()"')
if [[ "$tablas" == 0 ]]; then
  docker exec -i progra-mysql-fase2 sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot pos_dev' < sql/01_schema.sql
fi
# Confiar en la CA de este servidor local, sin desactivar la verificación TLS.
docker cp progra-mysql-fase2:/var/lib/mysql/ca.pem .local/mysql-ca.pem >/dev/null
rm -f .local/mysql-truststore.p12
keytool -importcert -noprompt -alias mysql-local -file .local/mysql-ca.pem -keystore .local/mysql-truststore.p12 -storetype PKCS12 -storepass changeit >/dev/null
printf '%s\n' 'MySQL listo en 127.0.0.1:3307; pos_dev y pos_test separadas.'
