#!/bin/sh
# Toglie dal Mac gli strumenti installati per il prototipo Android (vedi porting_libGDX.md, Fase 7).
# Non tocca i JDK 22 e Corretto 8, ne' il progetto: la cartella foresta-android/ si toglie a parte.
brew uninstall --cask android-commandlinetools
brew uninstall --cask android-platform-tools
# Il JDK 17 e' stato scaricato da Adoptium (tar.gz, senza sudo) nella cartella dei JDK dell'utente
rm -rf "$HOME/Library/Java/JavaVirtualMachines/temurin-17.jdk"
brew uninstall gradle
# L'openjdk di Homebrew arrivato come dipendenza di gradle (se qualcos'altro lo usa, brew si rifiuta e va bene cosi')
brew uninstall openjdk
# L'SDK scaricato con sdkmanager (piattaforma, build-tools), le cache di Gradle e le chiavi di adb
rm -rf /opt/homebrew/share/android-commandlinetools "$HOME/.gradle" "$HOME/.android"
