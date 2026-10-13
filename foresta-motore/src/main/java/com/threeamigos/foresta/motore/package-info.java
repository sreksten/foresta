/**
 * Il cuore del motore di gioco.
 * <p>
 * <b>La macchina a stati.</b> {@link com.threeamigos.foresta.motore.Automa} avanza solo in risposta ai comandi del giocatore e agli impulsi
 * del temporizzatore, passando fra gli {@link com.threeamigos.foresta.motore.Stato stati} del gioco; {@code AutomaInventario} e
 * le sue sorelle gestiscono gli scambi di artefatti. Vedi motore_di_gioco.md §2.
 * <p>
 * <b>Lo stato della partita.</b> Classi con un'istanza sola, che lavorano sopra il modello dati: il gruppo del giocatore
 * ({@link com.threeamigos.foresta.motore.GruppoGiocatore}) e quello avversario, la mappa ({@link com.threeamigos.foresta.motore.Foresta}), il
 * tempo ({@link com.threeamigos.foresta.motore.LineaTemporale}), le {@link com.threeamigos.foresta.motore.Statistiche}, le notizie e i
 * registri di personaggi e artefatti.
 * <p>
 * <b>Le regole.</b> Equipaggiamento, incantatura, contrattazione, riposo, progressione dei livelli e la distribuzione dei punti
 * alla creazione dei personaggi; le costanti di bilanciamento stanno in {@link com.threeamigos.foresta.motore.Costanti}, e il caso è
 * tutto in {@link com.threeamigos.foresta.motore.Dado}, così nei test si può fissare il seme.
 * <p>
 * Le regole che riguardano un dominio preciso stanno nei loro pacchetti: {@code combattimento}, {@code personaggi}, {@code oggetti},
 * {@code locazioni}, {@code missioni}, {@code incantesimi}, {@code canti}, {@code tattiche}, {@code testi} e {@code salvataggi}.
 */
package com.threeamigos.foresta.motore;
