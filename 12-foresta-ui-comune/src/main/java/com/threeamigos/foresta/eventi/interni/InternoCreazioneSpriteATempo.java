package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come sprite a tempo
 * (ad esempio, l'aumento del numero di pozioni). Porta la descrizione dello sprite, non lo sprite: lo costruisce chi lo mostra,
 * col suo toolkit grafico.
 *
 * @author Stefano Reksten
 */
public class InternoCreazioneSpriteATempo extends EventoBase {

    private final DescrizioneSpriteATempo descrizione;

    public InternoCreazioneSpriteATempo(DescrizioneSpriteATempo descrizione) {
        super(TipoEvento.INTERNO_CREAZIONE_SPRITE_A_TEMPO);
        this.descrizione = descrizione;
    }

    public DescrizioneSpriteATempo getDescrizione() {
        return descrizione;
    }
}
