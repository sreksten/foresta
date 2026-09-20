package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.ui.SpriteFumetto;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come fumetto
 * (ad esempio, la notifica che un Personaggio non può equipaggiare un Artefatto a causa del peso eccessivo).
 *
 * @author Stefano Reksten
 */
public class InternoCreazioneSpriteFumettoATempo extends EventoBase {

    private final SpriteFumetto spriteFumetto;

    public InternoCreazioneSpriteFumettoATempo(SpriteFumetto spriteFumetto) {
        super(TipoEvento.INTERNO_CREAZIONE_SPRITE_A_TEMPO);
        this.spriteFumetto = spriteFumetto;
    }

    public SpriteFumetto getSprite() {
        return spriteFumetto;
    }
}
