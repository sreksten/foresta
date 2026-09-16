package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteEffetto;

/**
 *
 * @author Stefano Reksten
 */
public class EventoCreazioneSpriteEffetto extends EventoBase {

    private final SpriteEffetto spriteEffetto;

    public EventoCreazioneSpriteEffetto(SpriteEffetto spriteEffetto) {
        super(TipoEvento.CREAZIONE_SPRITE_A_TEMPO);
        this.spriteEffetto = spriteEffetto;
    }

    public SpriteEffetto getSprite() {
        return spriteEffetto;
    }
}
