/**
 * La modifica di una tattica da parte del giocatore, senza niente di grafico: {@link com.threeamigos.foresta.tattiche.modifica.BozzaTattica} è la tattica
 * mentre la si modifica (i dati <code>*MD</code> sono immutabili: la bozza tiene le voci in liste che la schermata cambia, e solo alla conferma si
 * costruisce la {@code TatticaMD} da mandare al motore), con le regole su che cosa si può mettere in una voce; {@link com.threeamigos.foresta.tattiche.modifica.TestoTattica}
 * dà le parole con cui la schermata descrive voci, selettori, condizioni e azioni, lette da <code>testo_tattica_&lt;lingua&gt;.properties</code>.
 * Le usano le schermate delle tattiche di ogni front-end. Vedi tattiche_di_combattimento_automatiche.md §11.15.
 */
package com.threeamigos.foresta.tattiche.modifica;
