package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.OggettoConCosto;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del GruppoGiocatore e il costo
 * dell'Artefatto) approva l'acquisto di un Artefatto da un commerciante. L'Artefatto passa nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneAcquistoArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     */
    public NotificaApprovazioneAcquistoArtefatto(VistaScambio scambio, OggettoConCosto oggetto, String uuidRichiesta) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_ACQUISTO_ARTEFATTO, scambio, oggetto, uuidRichiesta);
    }
}
