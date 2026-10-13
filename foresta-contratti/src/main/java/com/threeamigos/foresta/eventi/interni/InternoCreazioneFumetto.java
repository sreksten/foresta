package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Un fumetto di un intermezzo viene creato dal gestore grafico: utile per debuggare
 * come viene spezzato il testo delle battute in frasi.
 *
 * @author Stefano Reksten
 */
public class InternoCreazioneFumetto extends EventoBase {

    private final String frase;

    public InternoCreazioneFumetto(String frase) {
        super(TipoEvento.INTERNO_CREAZIONE_FUMETTO);
        this.frase = frase;
    }

    public String getFrase() {
        return frase;
    }
}
