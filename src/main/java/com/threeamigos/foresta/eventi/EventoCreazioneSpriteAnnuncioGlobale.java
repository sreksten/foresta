package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteAnnuncioGlobale;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come annuncio globale
 * (ad esempio, l'inizio di una nuova missione).
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
