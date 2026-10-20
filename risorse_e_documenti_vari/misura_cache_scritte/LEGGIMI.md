# Misura della cache delle scritte (`ImageCache.cacheDinamica`)

Banco di prova esterno al gioco, scritto il 2026-10-21 per capire quanto valga la cache delle scritte e quanto costino le `WeakReference`
(vedi la voce "Cache delle scritte" in `assessment.md`, sezione 5.4).

`MisuraCache.java` simula un ciclo di disegno a 60 fps: a ogni fotogramma chiede 80 scritte (una parte con un numero che cambia) al
`DoomdarkTextProducer` vero, le disegna con `drawImage` su una tela, e produce una spazzatura a scelta. Confronta quattro politiche:
`none` (nessuna cache), `weak`, `soft`, `strong`.

## Come si lancia

Dalla radice del progetto, dopo `mvn -q -o compile`:

```
CP=foresta-swing/target/classes:foresta-contratti/target/classes:foresta-tipi/target/classes:foresta-modellodati/target/classes
javac -cp $CP -d /tmp/misura risorse_e_documenti_vari/misura_cache_scritte/MisuraCache.java
java -Djava.awt.headless=true -cp /tmp/misura:$CP MisuraCache <none|weak|soft|strong> <MB di spazzatura al secondo> <secondi> <scritte per fotogramma>
```

Per simulare poca memoria si aggiunge `-Xmx96m`.

`risultati-2026-10-21.txt` ha i numeri di quella sessione (macOS, JDK 22, 16 GB di RAM): `weak`, `soft` e `strong` con 20, 60 e 200 MB/s di
spazzatura, 80 scritte, 15 secondi a prova, con heap di default e con `-Xmx96m`. Il caso `none` dà il 100% di ricostruzioni, il 14-16% del tempo.

Limiti: il carico è simulato e i tempi valgono per quel Mac; su un telefono la ricostruzione costa molto di più.
