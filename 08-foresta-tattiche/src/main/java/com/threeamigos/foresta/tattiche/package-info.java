/**
 * La logica delle tattiche di combattimento automatiche (vedi tattiche_di_combattimento_automatiche.md): come un personaggio decide che cosa fare in combattimento.
 * <p>
 * La tattica di un personaggio è un dato ({@link com.threeamigos.foresta.modellodati.TatticaMD}): una lista di
 * {@link com.threeamigos.foresta.modellodati.VoceMD voci}; ogni voce ha un {@link com.threeamigos.foresta.modellodati.SelettoreMD selettore} (chi scegliere, con i suoi
 * {@link com.threeamigos.foresta.modellodati.FiltroMD filtri} e un {@link com.threeamigos.foresta.modellodati.OrdinamentoMD ordinamento}), delle condizioni
 * ({@link com.threeamigos.foresta.modellodati.PredicatoMD predicati}) e delle {@link com.threeamigos.foresta.modellodati.AzioneMD azioni}.
 * I dati (le classi {@code …MD}) sono puri e immutabili, fatti solo di enumerati, numeri e liste: la stessa classe serve all'interfaccia, che li legge e li modifica,
 * al salvataggio ({@link com.threeamigos.foresta.modellodati.TatticheMD} li scrive insieme, ognuno legato al suo personaggio) e al motore, che li valuta.
 * Qui c'è anche ciò che li fa funzionare:
 * <ul>
 * <li>{@link com.threeamigos.foresta.tattiche.Valutatore} scorre le voci e sceglie la prima eseguibile, restituendo una
 * {@link com.threeamigos.foresta.tattiche.Decisione};</li>
 * <li>{@link com.threeamigos.foresta.tattiche.EsecutoreTattiche} esegue la decisione, e {@link com.threeamigos.foresta.tattiche.ContestoTattico} è
 * tutto ciò che le tattiche possono leggere dello scontro;</li>
 * <li>{@link com.threeamigos.foresta.tattiche.TatticheBase} e {@link com.threeamigos.foresta.tattiche.RegistroTattiche} danno le tattiche di partenza dei mostri
 * e dei personaggi giocabili.</li>
 * </ul>
 * I sottopacchetti dividono i meccanismi: {@code rose} (gli insiemi da cui si pescano i bersagli) e {@code valutazione} (filtri, predicati, ordinamenti,
 * selettori, requisiti delle azioni e scelta dell'incantesimo).
 */
package com.threeamigos.foresta.tattiche;

