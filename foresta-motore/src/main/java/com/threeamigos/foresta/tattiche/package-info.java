/**
 * La logica delle tattiche di combattimento automatiche (vedi tattiche_di_combattimento_automatiche.md): come un personaggio decide che cosa fare in combattimento.
 * <p>
 * La tattica di un personaggio è un dato ({@code TatticaMD}, nel modello dati): una lista di voci, ciascuna con un selettore, delle condizioni e delle azioni.
 * Qui c'è ciò che la fa funzionare:
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
