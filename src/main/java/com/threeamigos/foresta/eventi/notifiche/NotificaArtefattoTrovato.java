package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.oggetti.Artefatto;

/**
 * Il gruppo ha trovato un artefatto o un ingrediente magico in un cofano, o ha preso l'artefatto di un tempio:
 * la UI lo mostra con una rivelazione (vedi SpriteRivelazioneArtefatto). Il livello del mondo serve a dire
 * quanto l'artefatto spicca rispetto a quel che si trova in quel momento.
 */
public class NotificaArtefattoTrovato extends EventoBase {

    private final Artefatto artefatto;
    private final int livelloMondo;

    public NotificaArtefattoTrovato(Artefatto artefatto, int livelloMondo) {
        super(TipoEvento.NOTIFICA_ARTEFATTO_TROVATO);
        this.artefatto = artefatto;
        this.livelloMondo = livelloMondo;
    }

    public Artefatto getArtefatto() {
        return artefatto;
    }

    public int getLivelloMondo() {
        return livelloMondo;
    }
}
