package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteInDissolvenza;

/**
 *
 * @author Stefano Reksten
 */
public class EventoCreazioneSpriteInDissolvenza extends EventoBase {

    private final SpriteInDissolvenza spriteInDissolvenza;

    public EventoCreazioneSpriteInDissolvenza(SpriteInDissolvenza spriteInDissolvenza) {
        super(TipoEvento.CREAZIONE_SPRITE_A_TEMPO);
        this.spriteInDissolvenza = spriteInDissolvenza;
    }

    public SpriteInDissolvenza getSprite() {
        return spriteInDissolvenza;
    }
}
