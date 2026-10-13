package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.ui.SpriteAnnuncioGlobale;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come annuncio globale
 * (ad esempio, l'inizio di una nuova missione).
 *
 * @author Stefano Reksten
 */
public class InternoCreazioneSpriteAnnuncioGlobale extends EventoBase {

    private final SpriteAnnuncioGlobale sprite;

    public InternoCreazioneSpriteAnnuncioGlobale(SpriteAnnuncioGlobale sprite) {
        super(TipoEvento.INTERNO_CREAZIONE_SPRITE_ANNUNCIO_GLOBALE);
        this.sprite = sprite;
    }

    public SpriteAnnuncioGlobale getSprite() {
        return sprite;
    }
}
