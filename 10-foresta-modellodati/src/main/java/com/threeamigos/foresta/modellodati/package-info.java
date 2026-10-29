/**
 * Il modello dei dati del gioco: tutto ciò che si salva e si rilegge, e niente di ciò che il gioco fa con quei dati.
 * <p>
 * Le classi hanno il suffisso <b>MD</b> (modello dati) e sono dati puri: campi, accessori e le poche regole di
 * coerenza che servono a non scrivere un salvataggio rotto (per esempio il carattere "|" si toglie dai testi, perché
 * separa i campi, e gli a capo diventano spazi, perché il salvataggio è a righe). Le regole del gioco stanno nel motore, che le usa: per ogni personaggio c'è un
 * {@link com.threeamigos.foresta.modellodati.PersonaggioMD}, per ogni missione un
 * {@link com.threeamigos.foresta.modellodati.MissioneMD}, e così via.
 * <p>
 * La radice è {@link com.threeamigos.foresta.modellodati.ModelloDati}, che riunisce le parti di una partita: il gruppo
 * del giocatore, le statistiche, la linea temporale, la mappa della foresta, i registri di personaggi, artefatti e
 * missioni, le notizie e gli intermezzi mostrati. Una parte a sé è {@link com.threeamigos.foresta.modellodati.TrofeiMD},
 * che passa da una partita all'altra e si salva in un file separato.
 * <p>
 * <b>Collezioni.</b> Quelle che le classi mostrano (gli artefatti e i modificatori di un personaggio, i personaggi di un
 * gruppo, i magazzini dei negozi, le missioni, le notizie...) sono di sola lettura, anche se sono una vista dell'elenco
 * vero e quindi seguono i suoi cambiamenti: chi vuole cambiarle passa dai metodi che lo dicono ({@code aggiungi…},
 * {@code togli…}, {@code riordina…}), così nessuno modifica per sbaglio un elenco che appartiene al modello. Una vista
 * di una lista si confronta con {@code equals} come la lista.
 * <p>
 * <b>Salvataggio.</b> Ogni classe che si salva implementa {@link com.threeamigos.foresta.serializzazione.Serializzabile}:
 * scrive un testo a righe, con i campi separati da {@code |}, e lo rilegge con
 * {@link com.threeamigos.foresta.serializzazione.LettoreCampi}. Il formato non deve restare compatibile con le versioni
 * precedenti.
 * <p>
 * Questo modulo non ha classi dei dati di parti (ognuna sta nel modulo della sua parte: <code>ArtefattoMD</code> negli artefatti,
 * <code>ForestaMD</code> nella mappa, <code>PersonaggioMD</code> nei personaggi…): ha <code>ModelloDati</code>, che le mette insieme, e i dati delle notizie
 * (<code>NotizieMD</code>, <code>NotiziaMD</code>, <code>MessaggioMD</code>). Non conosce motore né interfaccia.
 */
package com.threeamigos.foresta.modellodati;

