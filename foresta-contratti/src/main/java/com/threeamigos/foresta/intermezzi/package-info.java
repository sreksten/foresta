/**
 * I dati degli intermezzi, le brevi scene con personaggi, dialoghi e animazioni che il gioco mostra in certi momenti.
 * <p>
 * Si descrivono con un piccolo linguaggio a catena: una {@link com.threeamigos.foresta.intermezzi.PaginaIntermezzo} ha uno sfondo e degli
 * {@link com.threeamigos.foresta.intermezzi.ElementoIntermezzo elementi}; ogni elemento ha un'immagine e delle
 * {@link com.threeamigos.foresta.intermezzi.Tappa tappe} (dove va, quanto è grande, quanto è visibile) e le battute dei personaggi sono
 * {@link com.threeamigos.foresta.intermezzi.BattutaIntermezzo battute} con il loro fumetto. La UI sa disegnarli; la logica che decide
 * quando scatta un intermezzo e quali pagine mostrare sta nel motore.
 * <p>
 * Il pacchetto è diviso fra più moduli: qui stanno i dati che la UI disegna.
 */
package com.threeamigos.foresta.intermezzi;
