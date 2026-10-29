/**
 * I personaggi del gioco: i giocanti, i mostri e i non combattenti.
 * <p>
 * {@link com.threeamigos.foresta.personaggi.Personaggio} è l'interfaccia che tutti rispettano (e che la UI vede, in sola
 * lettura, come {@code VistaPersonaggio}); {@link com.threeamigos.foresta.personaggi.PersonaggioBase} ne implementa le regole
 * comuni sopra un {@code PersonaggioMD}. Ogni classe concreta aggiunge i dati della sua classe, presi dalle costanti
 * ({@code CostantiPersonaggi.GOBLIN_*} e simili): attributi di partenza, nomi e articoli, repertorio di incantesimi, immunità e offerte.
 * <p>
 * Le classi sono di tre famiglie:
 * <ul>
 * <li>i <b>personaggi giocanti</b> (Guerriero e Guerriera, Ladro e Ladra, Mago e Maga, Elfo e Elfa, X * Sacerdotessa, Chierico e Chierica, Druido e Druida), uno al maschile e uno al femminile con lo stesso ruolo;</li>
 * <li>i <b>mostri</b>, dal goblin al drago, alcuni dei quali sono i boss dei castelli (i non morti volanti incorporei condividono
 * la base {@link com.threeamigos.foresta.personaggi.NonMortoVolante});</li>
 * <li>i <b>non combattenti</b> ({@link com.threeamigos.foresta.personaggi.PersonaggioNonCombattente}): il locandiere, l'armaiolo,
 * l'alchimista e gli altri abitanti che compaiono nelle scene.</li>
 * </ul>
 * Si costruiscono dalla classe con {@link com.threeamigos.foresta.personaggi.FabbricaPersonaggi}.
 */
package com.threeamigos.foresta.personaggi;
