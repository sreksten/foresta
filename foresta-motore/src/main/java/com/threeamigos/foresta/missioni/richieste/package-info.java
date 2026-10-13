/**
 * I dati degli incarichi: ogni famiglia di incarichi ha una classe "Richiesto" o "Richiesta" (per esempio {@code RitualeRichiesto} per
 * {@code IlRituale}) che legge una riga della grammatica {@code missioni.txt} e ne espone i campi; qui stanno anche gli aiuti per leggere queste righe
 * ({@code CampiDiGrammatica}, {@code CapoDellaRiga}, {@code OndateDellaRiga}, {@code ModificatoreDellaRiga}).
 * <p>
 * Dividere i dati dal comportamento permette di cambiare gli incarichi dai file di testo, senza toccare il codice.
 */
package com.threeamigos.foresta.missioni.richieste;
