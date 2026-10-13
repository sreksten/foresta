package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.OggettoConCosto;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del GruppoGiocatore e il costo
 * dell'Artefatto) rifiuta l'acquisto di un Artefatto da un commerciante per mancanza di fondi.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoAcquistoArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     */
    public NotificaRifiutoAcquistoArtefatto(VistaScambio scambio, OggettoConCosto oggetto, String uuidRichiesta) {
        super(TipoEvento.NOTIFICA_RIFIUTO_ACQUISTO_ARTEFATTO, scambio, oggetto, uuidRichiesta);
    }
}
