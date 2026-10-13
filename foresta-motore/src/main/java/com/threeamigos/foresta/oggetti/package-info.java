/**
 * Gli oggetti che il gruppo trova nelle locazioni e gli artefatti che porta con sé.
 * <p>
 * Ci sono due concetti vicini, da tenere distinti:
 * <ul>
 * <li>l'{@link com.threeamigos.foresta.oggetti.Oggetto} è qualcosa che si trova in una locazione e si può prendere: monete,
 * pietre preziose, un cofano, un anello, un oggetto di una missione. Non si salva;</li>
 * <li>l'{@link com.threeamigos.foresta.oggetti.Artefatto} è un pezzo di equipaggiamento vero (un'arma, uno scudo, un ingrediente
 * magico...), con il suo {@code ArtefattoMD} che si salva. Un artefatto trovato in una locazione è anche un oggetto, e
 * {@link com.threeamigos.foresta.oggetti.OggettoArtefatto} ne è il collegamento.</li>
 * </ul>
 * <p>
 * Gli artefatti si generano a caso con {@link com.threeamigos.foresta.oggetti.GeneratoreArtefatti} (per il bottino e per i negozi),
 * si costruiscono a mano con {@link com.threeamigos.foresta.oggetti.CostruttoreArtefatto}, e gli ingredienti magici hanno un prezzo
 * ({@link com.threeamigos.foresta.oggetti.ListinoPergamene}). I leggendari e i loro set stanno nel
 * sottopacchetto {@code leggendari}. Vedi artefatti_e_incantamenti.md.
 */
package com.threeamigos.foresta.oggetti;
