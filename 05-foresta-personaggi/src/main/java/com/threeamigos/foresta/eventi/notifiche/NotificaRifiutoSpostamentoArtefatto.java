package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.artefatti.VistaScambio;

/**
 * Classe base per i rifiuti delle richieste di spostamento di un artefatto
 * (Personaggio &lt;-&gt; GruppoGiocatore, GruppoGiocatore &lt;-&gt; commerciante). I dati stanno in {@link EsitoSpostamentoArtefatto}.
 *
 * @author Stefano Reksten
 */
public abstract class NotificaRifiutoSpostamentoArtefatto<T> extends EsitoSpostamentoArtefatto<T> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggettoSpostato l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta a cui si risponde
     */
    public NotificaRifiutoSpostamentoArtefatto(TipoEvento tipoEvento, VistaScambio scambio, T oggettoSpostato, String uuidRichiesta) {
        super(tipoEvento, scambio, oggettoSpostato, uuidRichiesta);
    }
}
