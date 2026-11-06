#!/bin/sh
# Lancia solo i test elencati in test_porting_ui.txt (compilando tutto il resto, ma senza eseguirne i test).
# Uso: risorse_e_documenti_vari/test_porting_ui.sh [altri argomenti di mvn]
cd "$(dirname "$0")/.." || exit 1
ELENCO=$(grep -v '^#' risorse_e_documenti_vari/test_porting_ui.txt | grep -v '^$' | paste -sd, -)
exec mvn -o test -Dtest="$ELENCO" -Dsurefire.failIfNoSpecifiedTests=false "$@"
