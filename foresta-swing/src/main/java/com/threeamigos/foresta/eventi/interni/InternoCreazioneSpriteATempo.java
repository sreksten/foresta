package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.ui.SpriteATempo;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come sprite a tempo
 * (ad esempio, l'aumento del numero di pozioni).
 *
 * @author Stefano Reksten
 */
public class InternoCreazioneSpriteATempo extends EventoBase {

    private final SpriteATempo spriteATempo;

    public InternoCreazioneSpriteATempo(SpriteATempo spriteATempo) {
        super(TipoEvento.INTERNO_CREAZIONE_SPRITE_A_TEMPO);
        this.spriteATempo = spriteATempo;
    }

    public SpriteATempo getSprite() {
        return spriteATempo;
    }
}
