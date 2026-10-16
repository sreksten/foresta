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
 * <b>Salvataggio.</b> Ogni classe che si salva implementa {@link com.threeamigos.foresta.modellodati.Serializzabile}:
 * scrive un testo a righe, con i campi separati da {@code |}, e lo rilegge con
 * {@link com.threeamigos.foresta.modellodati.LettoreCampi}. Il formato non deve restare compatibile con le versioni
 * precedenti.
 * <p>
 * Questo modulo dipende solo dai tipi di base (<code>tipi</code>) e non conosce motore, eventi né interfaccia.
 */
package com.threeamigos.foresta.modellodati;
