/**
 * Il caso del gioco: {@link com.threeamigos.foresta.strumenti.dado.Dado} è l'unica sorgente di caso, con il seme che si può fissare e una
 * coda di risultati truccati per i test. I lanci importanti hanno un contesto
 * ({@link com.threeamigos.foresta.strumenti.dado.ContestoLancio}) che, in modalità di prova, un
 * {@link com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi} può decidere.
 * <p>
 * Non dipende da nessun altro modulo del progetto; scrive gli errori nel registro del sottopacchetto {@code logger}.
 */
package com.threeamigos.foresta.strumenti.dado;
