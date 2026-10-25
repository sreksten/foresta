/**
 * I dati delle tattiche di combattimento automatiche: dati puri, immutabili, fatti solo di enumerati, numeri e liste.
 * <p>
 * Una {@link com.threeamigos.foresta.modellodati.tattiche.TatticaMD tattica} è una lista di
 * {@link com.threeamigos.foresta.modellodati.tattiche.VoceMD voci}; ogni voce ha un
 * {@link com.threeamigos.foresta.modellodati.tattiche.SelettoreMD selettore} (chi scegliere, con i suoi
 * {@link com.threeamigos.foresta.modellodati.tattiche.FiltroMD filtri} e un
 * {@link com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD ordinamento}), delle condizioni
 * ({@link com.threeamigos.foresta.modellodati.tattiche.PredicatoMD predicati}) e delle
 * {@link com.threeamigos.foresta.modellodati.tattiche.AzioneMD azioni}.
 * <p>
 * La stessa classe serve a tre cose: l'interfaccia le legge e le modifica, il salvataggio le scrive insieme al
 * personaggio e il motore le valuta. Per questo non dipendono dal motore, ma solo dai tipi di base e fra loro. La
 * logica che le applica sta nel pacchetto <code>tattiche</code> del motore (vedi
 * tattiche_di_combattimento_automatiche.md §11).
 */
package com.threeamigos.foresta.modellodati.tattiche;
