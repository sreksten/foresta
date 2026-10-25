package com.threeamigos.foresta.personaggi;

/**
 * Come il giocatore si rivolge a un avversario che potrebbe non combattere: con amicizia o con corruzione. Da come lo fa dipendono le
 * offerte che l'avversario può fare (vedi {@link Personaggio#getOfferta}).
 */
public enum TipoApproccio {

    /**
     * Il giocatore stringe amicizia.
     */
    AMICIZIA,
    /**
     * Il giocatore tenta di corrompere.
     */
    CORRUZIONE
}
