package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.interni.InternoSpostamentoArtefatto;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Classe base per i rifiuti delle richieste di spostamento di un artefatto.
 * (Personaggio <-> GruppoGiocatore, GruppoGiocatore <-> commerciante)
 *
 * @author Stefano Reksten
 */
public abstract class NotificaRifiutoSpostamentoArtefatto<T> extends EventoBase {

    private final InternoSpostamentoArtefatto<T> comandoSpostamentoArtefatto;

    /**
     * @param comandoSpostamentoArtefatto la richiesta di spostamento di un Artefatto che si rifiuta
     */
    public NotificaRifiutoSpostamentoArtefatto(TipoEvento tipoEvento, InternoSpostamentoArtefatto<T> comandoSpostamentoArtefatto) {
        super(tipoEvento);
        this.comandoSpostamentoArtefatto = comandoSpostamentoArtefatto;
    }

    /**
     * @return lo scambio aperto da cui veniva la richiesta, o null: le schermate rispondono solo alle notifiche del
     * proprio (la richiesta stessa è interna al motore)
     */
    public VistaScambio getScambio() {
        return comandoSpostamentoArtefatto.getScambio();
    }

    /**
     * @return l'oggetto di cui si chiedeva lo spostamento
     */
    public T getOggettoSpostato() {
        return comandoSpostamentoArtefatto.getOggettoDaSpostare();
    }

    /**
     * @return l'identificativo della richiesta che si rifiuta
     */
    public String getUuidRichiesta() {
        return comandoSpostamentoArtefatto.getUuid();
    }
}
