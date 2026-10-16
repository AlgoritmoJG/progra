#!/usr/bin/env bash
# Activación local; no modifica el Java instalado por el usuario.
if [[ -x /workspace/tools/jdk25/bin/javac && ! -x "${JAVA_HOME:-/no-configurado}/bin/javac" ]]; then
  export JAVA_HOME=/workspace/tools/jdk25
  export PATH="$JAVA_HOME/bin:$PATH"
fi
if [[ -r /etc/ssl/certs/java/cacerts && "${JAVA_HOME:-}" == /workspace/tools/jdk25 && "${MAVEN_OPTS:-}" != *javax.net.ssl.trustStore=* ]]; then
  export MAVEN_OPTS="${MAVEN_OPTS:-} -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
fi
