package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.ui.SpriteFumetto;

/**
 * Evento interno alla UI che decide di rappresentare eventi di gioco come fumetto
 * (ad esempio, la notifica che un Personaggio non può equipaggiare un Artefatto a causa del peso eccessivo).
 *
 * @author Stefano Reksten
 */
public class EventoCreazioneSpriteFumetto extends EventoBase {

    private final SpriteFumetto spriteFumetto;

    public EventoCreazioneSpriteFumetto(SpriteFumetto spriteFumetto) {
        super(TipoEvento.CREAZIONE_SPRITE_A_TEMPO);
        this.spriteFumetto = spriteFumetto;
    }

    public SpriteFumetto getSprite() {
        return spriteFumetto;
    }
}
