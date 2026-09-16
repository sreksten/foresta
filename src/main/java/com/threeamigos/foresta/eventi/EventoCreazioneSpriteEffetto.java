package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteEffetto;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come effetti grafici
 * (ad esempio, un effetto di stato applicato su un Personaggio).
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
