#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/java-env.sh
mkdir -p .local
# Maven usa proxy XML; no toma HTTPS_PROXY automáticamente.
python3 - <<'PYTHON'
import os, pathlib, urllib.parse, xml.etree.ElementTree as E
r=E.Element('settings')
v=os.environ.get('HTTPS_PROXY') or os.environ.get('https_proxy')
if v:
 u=urllib.parse.urlparse(v); ps=E.SubElement(r,'proxies'); p=E.SubElement(ps,'proxy')
 for k,x in [('id','entorno'),('active','true'),('protocol','http'),('host',u.hostname),('port',str(u.port or 80)),('nonProxyHosts','localhost|127.0.0.1')]: E.SubElement(p,k).text=x
 for k,x in [('username',u.username),('password',u.password)]:
  if x: E.SubElement(p,k).text=urllib.parse.unquote(x)
p=pathlib.Path('.local/maven-settings.xml'); E.ElementTree(r).write(p,encoding='unicode');p.chmod(0o600)
PYTHON
if command -v mvn >/dev/null 2>&1; then
  ejecutable=$(command -v mvn)
else
  if [[ ! -x .local/apache-maven-3.9.11/bin/mvn ]]; then
    curl -fsSL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.tar.gz -o .local/maven.tar.gz
    curl -fsSL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.tar.gz.sha512 -o .local/maven.sha512
    python3 - <<'PYTHON'
import pathlib, hashlib, tarfile
p=pathlib.Path('.local/maven.tar.gz')
if hashlib.sha512(p.read_bytes()).hexdigest()!=pathlib.Path('.local/maven.sha512').read_text().strip(): raise SystemExit('Checksum Maven incorrecto.')
with tarfile.open(p) as t: t.extractall('.local',filter='data')
PYTHON
  fi
  ejecutable="$PWD/.local/apache-maven-3.9.11/bin/mvn"
fi
exec "$ejecutable" -s .local/maven-settings.xml -Dmaven.repo.local="$PWD/.local/m2" "$@"
