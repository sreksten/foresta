/**
 * Gli incantesimi: ciò che il gruppo o i mostri possono lanciare.
 * <p>
 * Ogni incantesimo implementa {@link com.threeamigos.foresta.incantesimi.Incantesimo}: ha una classe, un livello, un costo di lancio in magia e
 * un modo di essere formulato su un personaggio o su un gruppo. I <b>malefici</b> (aria, acqua, terra, fuoco, fulmine, gelo, veleno, morte,
 * silenzio) estendono {@link com.threeamigos.foresta.incantesimi.IncantesimoMaleficoImpl}, che per ogni
 * bersaglio tira per colpire e calcola il danno come per un'arma; l'unico <b>benefico</b> è la resurrezione.
 * <p>
 * Si costruiscono con {@link com.threeamigos.foresta.incantesimi.FabbricaIncantesimi}. Le pergamene sono scorte del gruppo. Il dardo arcano, l'alba sacra e la luce divina, innati, non sono incantesimi ma abilità
 * (pacchetto {@code abilita}); i canti del Bardo nemmeno (pacchetto {@code canti}).
 */
package com.threeamigos.foresta.incantesimi;
