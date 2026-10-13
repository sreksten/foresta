package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaArtefatto;

/**
 * Il gruppo ha trovato un artefatto o un ingrediente magico in un cofano, o ha preso l'artefatto di un tempio:
 * la UI lo mostra con una rivelazione (vedi SpriteRivelazioneArtefatto). Il livello del mondo serve a dire
 * quanto l'artefatto spicca rispetto a quel che si trova in quel momento.
 */
public class NotificaArtefattoTrovato extends EventoBase {

    private final VistaArtefatto artefatto;
    private final int livelloMondo;

    public NotificaArtefattoTrovato(VistaArtefatto artefatto, int livelloMondo) {
        super(TipoEvento.NOTIFICA_ARTEFATTO_TROVATO);
        this.artefatto = artefatto;
        this.livelloMondo = livelloMondo;
    }

    public VistaArtefatto getArtefatto() {
        return artefatto;
    }

    public int getLivelloMondo() {
        return livelloMondo;
    }
}
