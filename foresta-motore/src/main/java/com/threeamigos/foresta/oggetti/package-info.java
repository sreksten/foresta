/**
 * Gli oggetti che il gruppo trova nelle locazioni e gli artefatti che porta con sé.
 * <p>
 * Ci sono due concetti vicini, da tenere distinti:
 * <ul>
 * <li>il {@link com.threeamigos.foresta.oggetti.Bottino} è qualcosa che si trova in una locazione e si può prendere: monete,
 * pietre preziose, un cofano, un anello, un oggetto di una missione. Non si salva, ed è solo un dato: cosa succede quando il
 * gruppo lo prende lo decide {@link com.threeamigos.foresta.oggetti.RaccoltaBottino};</li>
 * <li>l'{@link com.threeamigos.foresta.artefatti.Artefatto} è un pezzo di equipaggiamento vero (un'arma, uno scudo, un ingrediente
 * magico...), con il suo {@code ArtefattoMD} che si salva. Un artefatto trovato in una locazione sta dentro un bottino:
 * {@link com.threeamigos.foresta.oggetti.BottinoArtefatto}, sia per uno già fatto (quello di un tempio, che si rivela quando lo si
 * prende) sia per una spada, uno scudo... per terra, il cui artefatto nasce con lui. Quali forme esistono lo dice
 * {@link com.threeamigos.foresta.oggetti.FormaDelBottino}.</li>
 * </ul>
 * <p>
 * Gli artefatti si generano a caso con {@link com.threeamigos.foresta.artefatti.GeneratoreArtefatti} (per il bottino e per i negozi),
 * si costruiscono a mano con {@link com.threeamigos.foresta.artefatti.CostruttoreArtefatto}, e gli ingredienti magici hanno un prezzo
 * ({@link com.threeamigos.foresta.artefatti.ListinoPergamene}). I leggendari e i loro set stanno nel
 * sottopacchetto {@code leggendari}. Vedi artefatti_e_incantamenti.md.
 */
package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.ArtefattoMD;
