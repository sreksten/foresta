package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteATempo;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come sprite a tempo
 * (ad esempio, l'aumento del numero di pozioni).
 *
 * @author Stefano Reksten
 */
public class EventoCreazioneSpriteATempo extends EventoBase {

    private final SpriteATempo spriteATempo;

    public EventoCreazioneSpriteATempo(SpriteATempo spriteATempo) {
        super(TipoEvento.CREAZIONE_SPRITE_A_TEMPO);
        this.spriteATempo = spriteATempo;
    }

    public SpriteATempo getSprite() {
        return spriteATempo;
    }
}
