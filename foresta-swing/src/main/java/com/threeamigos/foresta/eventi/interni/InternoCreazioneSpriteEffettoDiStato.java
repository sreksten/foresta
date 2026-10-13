package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.ui.SpriteEffetto;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come effetti grafici
 * (ad esempio, un effetto di stato applicato su un Personaggio).
 *
 * @author Stefano Reksten
 */
public class InternoCreazioneSpriteEffettoDiStato extends EventoBase {

    private final SpriteEffetto spriteEffetto;

    public InternoCreazioneSpriteEffettoDiStato(SpriteEffetto spriteEffetto) {
        super(TipoEvento.INTERNO_CREAZIONE_SPRITE_EFFETTO_DI_STATO);
        this.spriteEffetto = spriteEffetto;
    }

    public SpriteEffetto getSprite() {
        return spriteEffetto;
    }
}
