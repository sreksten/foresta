package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.artefatti.OggettoConCosto;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il GruppoGiocatore rifiuta la vendita di un Artefatto a un commerciante che non tratta quel genere di
 * oggetti (l'armaiolo non compra pergamene, il venditore di pergamene compra solo quelle).
 * <p>
 * NOTA: in questo momento i commercianti hanno fondi infiniti, per cui non rifiutano mai per mancanza di denaro.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoVenditaArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     */
    public NotificaRifiutoVenditaArtefatto(VistaScambio scambio, OggettoConCosto oggetto, String uuidRichiesta) {
        super(TipoEvento.NOTIFICA_RIFIUTO_VENDITA_ARTEFATTO, scambio, oggetto, uuidRichiesta);
    }

}
