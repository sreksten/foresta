package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.OggettoConCosto;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del commerciante e il costo
 * dell'Artefatto) approva la vendita di un Artefatto da un commerciante. L'Artefatto passa nell'inventario del
 * commerciante.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneVenditaArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     */
    public NotificaApprovazioneVenditaArtefatto(VistaScambio scambio, OggettoConCosto oggetto, String uuidRichiesta) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_VENDITA_ARTEFATTO, scambio, oggetto, uuidRichiesta);
    }
}
