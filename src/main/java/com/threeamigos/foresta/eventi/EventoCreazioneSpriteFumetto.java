package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteFumetto;

/**
 *
 * @author Stefano Reksten
 */
public class EventoCreazioneSpriteFumetto extends EventoBase {

    private final SpriteFumetto spriteFumetto;

    public EventoCreazioneSpriteFumetto(SpriteFumetto spriteFumetto) {
        super(TipoEvento.CREAZIONE_SPRITE_A_TEMPO);
        this.spriteFumetto = spriteFumetto;
    }

    public SpriteFumetto getSprite() {
        return spriteFumetto;
    }
}
