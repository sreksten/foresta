/**
 * Come si scrivono e si rileggono i salvataggi: un testo a righe, una riga per record, con i campi separati da {@code |}.
 * <p>
 * {@link com.threeamigos.foresta.serializzazione.Serializzabile} dichiara che un oggetto sa scriversi e rileggersi e offre
 * i metodi per scrivere una riga; {@link com.threeamigos.foresta.serializzazione.LettoreCampi} la rilegge campo per campo;
 * {@link com.threeamigos.foresta.serializzazione.MappaProprieta} scrive e rilegge una mappa chiave-valore in una riga.
 * <p>
 * Questo modulo non dipende da nessun altro del progetto (solo dal JDK) e non sa niente di che cosa si salva: il formato
 * dei dati del gioco sta in {@code modellodati}. Il formato non deve restare compatibile con le versioni precedenti.
 */
package com.threeamigos.foresta.serializzazione;
