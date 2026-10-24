/**
 * La generazione dei testi a caso del gioco.
 * <p>
 * {@link com.threeamigos.foresta.testi.GrammarBean} (modulo foresta-testi) legge una grammatica da un file di testo e produce testi
 * espandendo a caso le sue produzioni (alternative, riferimenti, valori fissati, pesi, sostituzioni finali: vedi GrammarBean.md).
 * I produttori dei testi (fiabe, oroscopi, luoghi, sfide, missioni, locande) stanno in foresta-testi con le loro grammatiche e non
 * conoscono il bus: una grammatica che manca solleva una {@link com.threeamigos.foresta.testi.GrammaticaNonValidaException}. Qui nel motore
 * restano quello che dipende dal gioco: {@link com.threeamigos.foresta.artefatti.ProduttoreDiArtefattiCasuale} (leggendari, libri tecnici,
 * aggiornamenti, con le grammatiche degli artefatti), {@link com.threeamigos.foresta.testi.NotizieDelleLocande} (sceglie le notizie
 * della locanda adatte al gruppo) e {@link com.threeamigos.foresta.testi.PrecaricatoreDeiTesti}, che precarica tutte le grammatiche
 * all'avvio e traduce un errore in una richiesta di uscita sul bus.
 */
package com.threeamigos.foresta.testi;
