/**
 * I trofei: traguardi che il giocatore conquista e che valgono da una partita all'altra (uccidere cento goblin, comprare mille artefatti, sconfiggere il drago...).
 * <p>
 * Ogni {@link com.threeamigos.foresta.trofei.Trofeo} ha un trigger che si controlla a fine locazione, finché non è stato vinto; i più comuni contano un evento interno fino a una
 * soglia ({@link com.threeamigos.foresta.trofei.TrofeoAContatore}). {@link com.threeamigos.foresta.trofei.ClasseTrofeo} elenca i trofei esistenti.
 * Quanto si guadagna in una locazione conta solo se il gruppo ne esce vivo. I dati stanno nel {@code TrofeiMD}, che si salva in un file a parte.
 */
package com.threeamigos.foresta.trofei;
