package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Classe base per i rifiuti delle richieste di spostamento di un artefatto.
 * (Personaggio <-> GruppoGiocatore, GruppoGiocatore <-> commerciante)
 *
 * @author Stefano Reksten
 */
public abstract class NotificaRifiutoSpostamentoArtefatto<T> extends EventoBase {

    private final VistaScambio scambio;
    private final T oggettoSpostato;
    private final String uuidRichiesta;

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggettoSpostato l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta che si rifiuta
     */
    public NotificaRifiutoSpostamentoArtefatto(TipoEvento tipoEvento, VistaScambio scambio, T oggettoSpostato, String uuidRichiesta) {
        super(tipoEvento);
        this.scambio = scambio;
        this.oggettoSpostato = oggettoSpostato;
        this.uuidRichiesta = uuidRichiesta;
    }

    /**
     * @return lo scambio aperto da cui veniva la richiesta, o null: le schermate rispondono solo alle notifiche del
     * proprio (la richiesta stessa è interna al motore)
     */
    public VistaScambio getScambio() {
        return scambio;
    }

    /**
     * @return l'oggetto di cui si chiedeva lo spostamento
     */
    public T getOggettoSpostato() {
        return oggettoSpostato;
    }

    /**
     * @return l'identificativo della richiesta che si rifiuta
     */
    public String getUuidRichiesta() {
        return uuidRichiesta;
    }
}
