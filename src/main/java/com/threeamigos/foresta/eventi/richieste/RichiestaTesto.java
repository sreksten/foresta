package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il gioco richiede un testo al giocatore (ad esempio il nome del personaggio).
 *
 * @author Stefano Reksten
 */
public class RichiestaTesto extends EventoBase {

    private final String richiesta;
    private final String testoPredefinito;

    public RichiestaTesto(String richiesta) {
        this(richiesta, "");
    }

    /**
     * @param testoPredefinito il testo con cui il Prompt si apre già compilato
     */
    public RichiestaTesto(String richiesta, String testoPredefinito) {
        super(TipoEvento.RICHIESTA_TESTO);
        this.richiesta = richiesta;
        this.testoPredefinito = testoPredefinito == null ? "" : testoPredefinito;
    }

    public String getRichiesta() {
        return richiesta;
    }

    public String getTestoPredefinito() {
        return testoPredefinito;
    }
}
