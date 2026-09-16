package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteAnnuncioGlobale;

/**
 *
 * @author Stefano Reksten
 */
public class EventoCreazioneSpriteAnnuncioGlobale extends EventoBase {

    private final SpriteAnnuncioGlobale sprite;

    public EventoCreazioneSpriteAnnuncioGlobale(SpriteAnnuncioGlobale sprite) {
        super(TipoEvento.CREAZIONE_SPRITE_ANNUNCIO_GLOBALE);
        this.sprite = sprite;
    }

    public SpriteAnnuncioGlobale getSprite() {
        return sprite;
    }
}
