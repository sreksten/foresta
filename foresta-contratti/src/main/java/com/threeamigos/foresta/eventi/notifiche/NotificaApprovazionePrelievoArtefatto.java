package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.artefatti.OggettoConPeso;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il GruppoGiocatore (che controlla il peso dell'Artefatto e la disponibilità di carico di un Personaggio)
 * approva lo spostamento di un Artefatto dall'inventario generale verso un Personaggio.
 * L'Artefatto passa nell'inventario del Personaggio.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazionePrelievoArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     */
    public NotificaApprovazionePrelievoArtefatto(VistaScambio scambio, OggettoConPeso oggetto, String uuidRichiesta) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_PRELIEVO_ARTEFATTO, scambio, oggetto, uuidRichiesta);
    }

}
