# 00-foresta-tutorial

Il **tutorial**: i suggerimenti per chi gioca per la prima volta (il piano e le decisioni sono in [`tutorial.md`](tutorial.md)). Il modulo è la *memoria* del tutorial e nient'altro: non sa niente della UI e non manda eventi, e chi vuole sapere se il tutorial è acceso (la UI per mostrare un suggerimento, il motore per pilotare la partita) lo chiede qui.

Per ora contiene solo l'interruttore acceso/spento. Quando il registro conoscerà i suggerimenti (l'enum, i visti, i testi per lingua) e avrà bisogno dei comandi e dei testi, il suo livello salirà a 03 e il modulo si rinominerà `03-foresta-tutorial` (vedi `tutorial.md` §3).

## Dipendenze

**Dichiarate nel pom:** nessuna.

**Lo dichiarano:** `12-foresta-motore`.

Le dipendenze sono transitive: un modulo vede anche quello che vedono i moduli da cui dipende, quindi il compilatore garantisce la separazione fra i moduli che non si raggiungono.

## Funzionamento

- **`RegistroSuggerimenti`** (pacchetto `tutorial`): `isAttivo()` e `setAttivo(boolean)`. Il tutorial vale da una partita all'altra e non fa parte del salvataggio; finché non è finito non si salva niente (decisione 10 di `tutorial.md`), quindi parte sempre **acceso**.
- **Chi lo usa:** `LocazioneBase` (`12-foresta-motore`): al primo turno della partita (`Statistiche.getTurniGiocati() == 0`) a tutorial acceso non offre l'icona della fuga né quella di passare inosservato, e ignora i due comandi se arrivano lo stesso.
- **I test** (`PartitaDiTest`) spengono il tutorial all'avvio e lo rimettono com'era alla chiusura, come per la modalità di prova: chi vuole provarlo lo riaccende.

## Osservazioni

- Lo stato è statico, come per `ModalitaDiProva`: i test devono rimettere com'era (`RegistroSuggerimentiTest` lo fa).
- Mancano il registro dei suggerimenti visti, il modello dati e il gestore su file (come il bestiario), e il truccatore dei dadi del tutorial: vedi "Da fare" di `tutorial.md`.
