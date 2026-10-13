package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Evento iniziale che annuncia il completamento del caricamento del gioco e della preparazione
 * della UI. Il gioco può iniziare.
 *
 * @author Stefano Reksten
 */
public class InternoInterfacciaUtentePronta extends EventoBase {

    public InternoInterfacciaUtentePronta() {
        super(TipoEvento.INTERNO_INTERFACCIA_UTENTE_PRONTA);
    }
}
