/**
 * I meccanismi con cui si valuta una tattica: ogni classe risponde per un tipo di elemento dei dati.
 * <p>
 * {@link com.threeamigos.foresta.tattiche.valutazione.Selettori} applica un selettore (la rosa, i filtri e l'ordinamento che sceglie uno solo);
 * {@link com.threeamigos.foresta.tattiche.valutazione.Filtri}, {@link com.threeamigos.foresta.tattiche.valutazione.Ordinamenti} e
 * {@link com.threeamigos.foresta.tattiche.valutazione.Predicati} danno la risposta di ogni tipo di filtro, ordinamento e condizione;
 * {@link com.threeamigos.foresta.tattiche.valutazione.Requisiti} dice quando un'azione si può fare,
 * {@link com.threeamigos.foresta.tattiche.valutazione.SceltaIncantesimo} quale incantesimo di danno lanciare e
 * {@link com.threeamigos.foresta.tattiche.valutazione.Astuzia} quanto è astuto un personaggio.
 */
package com.threeamigos.foresta.tattiche.valutazione;
