package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteATempo;

/**
 *
 * @author Stefano Reksten
 */
public class EventoCreazioneSpriteATempo extends EventoBase {

    private final SpriteATempo spriteATempo;

    public EventoCreazioneSpriteATempo(SpriteATempo spriteATempo) {
        super(TipoEvento.CREAZIONE_SPRITE_A_TEMPO);
        this.spriteATempo = spriteATempo;
    }

    public SpriteATempo getSprite() {
        return spriteATempo;
    }
}
