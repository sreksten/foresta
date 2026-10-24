package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.artefatti.OggettoConPeso;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il GruppoGiocatore approva lo spostamento di un Artefatto da un Personaggio verso l'inventario generale.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneStoccaggioArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     */
    public NotificaApprovazioneStoccaggioArtefatto(VistaScambio scambio, OggettoConPeso oggetto, String uuidRichiesta) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_STOCCAGGIO_ARTEFATTO, scambio, oggetto, uuidRichiesta);
    }

}
