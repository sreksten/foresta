package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.ui.SpriteInDissolvenza;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come una dissolvenza
 * (ad esempio, la sconfitta di un Personaggio avversario).
 *
 * @author Stefano Reksten
 */
public class InternoCreazioneSpriteInDissolvenza extends EventoBase {

    private final SpriteInDissolvenza spriteInDissolvenza;

    public InternoCreazioneSpriteInDissolvenza(SpriteInDissolvenza spriteInDissolvenza) {
        super(TipoEvento.INTERNO_CREAZIONE_SPRITE_A_TEMPO);
        this.spriteInDissolvenza = spriteInDissolvenza;
    }

    public SpriteInDissolvenza getSprite() {
        return spriteInDissolvenza;
    }
}
