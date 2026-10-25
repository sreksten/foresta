/**
 * I danni e le loro reazioni con gli stati alterati: {@link com.threeamigos.foresta.tipi.TipoDanno} e
 * {@link com.threeamigos.foresta.tipi.CategoriaDanno} dicono quali danni esistono e quali stati possono causare, e
 * {@link com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato} elenca le reazioni che nascono quando un danno
 * colpisce chi ha un certo stato (il fulmine su un bagnato, il fuoco su uno scudo di ghiaccio...).
 * <p>
 * Le debolezze e le resistenze delle classi di personaggio ai tipi di danno non sono qui: stanno in {@code foresta-personaggi}, che
 * dipende da questo modulo.
 */
package com.threeamigos.foresta.danni.tipi;

