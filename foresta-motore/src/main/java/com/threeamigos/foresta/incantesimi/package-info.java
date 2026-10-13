/**
 * Gli incantesimi: ciò che il gruppo o i mostri possono lanciare.
 * <p>
 * Ogni incantesimo implementa {@link com.threeamigos.foresta.incantesimi.Incantesimo}: ha una classe, un livello, un costo di lancio in magia e
 * un modo di essere formulato su un personaggio o su un gruppo. I <b>malefici</b> (aria, acqua, terra, fuoco, fulmine, gelo, veleno, morte,
 * silenzio, luce divina, dardo arcano) estendono {@link com.threeamigos.foresta.incantesimi.IncantesimoMaleficoImpl}, che per ogni
 * bersaglio tira per colpire e calcola il danno come per un'arma; i <b>benefici</b> sono la resurrezione e l'alba sacra.
 * <p>
 * Si costruiscono con {@link com.threeamigos.foresta.incantesimi.FabbricaIncantesimi}. Le pergamene sono scorte del gruppo; il dardo arcano e,
 * per i sacerdoti, l'alba sacra e la luce divina sono invece innati e non le consumano. I canti del Bardo non sono incantesimi: stanno nel pacchetto
 * {@code canti}.
 */
package com.threeamigos.foresta.incantesimi;
