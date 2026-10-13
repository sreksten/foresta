package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il giocatore sposta un personaggio di un posto a sinistra o a destra nel gruppo (schermata delle tattiche).
 */
public class ComandoSpostaPersonaggio extends EventoBase {

    private final String uuidPersonaggio;
    private final boolean versoDestra;

    public ComandoSpostaPersonaggio(String uuidPersonaggio, boolean versoDestra) {
        super(TipoEvento.COMANDO_SPOSTA_PERSONAGGIO);
        this.uuidPersonaggio = uuidPersonaggio;
        this.versoDestra = versoDestra;
    }

    public String getUuidPersonaggio() {
        return uuidPersonaggio;
    }

    public boolean isVersoDestra() {
        return versoDestra;
    }
}
