package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoSpostamentoArtefatto;

/**
 * Classe base per le approvazioni delle richieste di spostamento di un artefatto.
 * (Personaggio <-> GruppoGiocatore, GruppoGiocatore <-> commerciante)
 *
 * @author Stefano Reksten
 */
public abstract class NotificaApprovazioneSpostamentoArtefatto<T> extends EventoBase {

    private final ComandoSpostamentoArtefatto<T> comandoSpostamentoArtefatto;

    /**
     * @param comandoSpostamentoArtefatto la richiesta di spostamento di un Artefatto che si approva
     */
    public NotificaApprovazioneSpostamentoArtefatto(TipoEvento tipoEvento, ComandoSpostamentoArtefatto<T> comandoSpostamentoArtefatto) {
        super(tipoEvento);
        this.comandoSpostamentoArtefatto = comandoSpostamentoArtefatto;
    }

    /**
     * @return la richiesta originale di spostamento di un Artefatto che si approva
     */
    public ComandoSpostamentoArtefatto<T> getEventoRichiestaSpostamentoArtefatto() {
        return comandoSpostamentoArtefatto;
    }
}
