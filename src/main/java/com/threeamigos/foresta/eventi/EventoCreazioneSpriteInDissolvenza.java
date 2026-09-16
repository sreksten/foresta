package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteInDissolvenza;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come una dissolvenza
 * (ad esempio, la sconfitta di un Personaggio avversario).
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
