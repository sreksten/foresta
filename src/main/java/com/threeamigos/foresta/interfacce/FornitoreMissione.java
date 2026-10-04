package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.missioni.Missione;

/**
 * Interfaccia generale per poter fornire una missione al motore che ne seguirà gli sviluppi.
 * In questo modo le missioni fornite o generate a caso dal gioco (a parte le poche missioni predefinite)
 * potranno essere selezionate o fornite in un dato ordine per esempio da un tool di debug (ad esempio una finestra
 * secondaria).
 *
 * @author Stefano Reksten
 */
public interface FornitoreMissione {

    Missione fornisciProssimaMissione();

}
