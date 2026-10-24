package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.artefatti.OggettoConPeso;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il GruppoGiocatore rifiuta lo spostamento di un Artefatto da un Personaggio verso l'inventario generale.
 * Il discriminante è il peso dell'oggetto.
 * <p>
 * (NOTA: in questo momento l'inventario generale non ha limite di peso, quindi questo evento non viene sollevato).
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoStoccaggioArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     */
    public NotificaRifiutoStoccaggioArtefatto(VistaScambio scambio, OggettoConPeso oggetto, String uuidRichiesta) {
        super(TipoEvento.NOTIFICA_RIFIUTO_STOCCAGGIO_ARTEFATTO, scambio, oggetto, uuidRichiesta);
    }

}
