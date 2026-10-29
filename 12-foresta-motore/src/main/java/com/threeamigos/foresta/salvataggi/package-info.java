/**
 * La conservazione su file di salvataggi, classifica e trofei, nella cartella {@code .foresta} della cartella dell'utente.
 * <p>
 * Sono le implementazioni su file delle interfacce {@code GestoreSalvataggi}, {@code GestorePunteggi} e {@code GestoreTrofei}; il formato lo decidono le classi
 * del modello dati (vedi {@code Serializzabile}). Un front-end per un'altra piattaforma, dove la cartella dell'utente è diversa, scrive la propria implementazione
 * delle stesse interfacce.
 */
package com.threeamigos.foresta.salvataggi;
