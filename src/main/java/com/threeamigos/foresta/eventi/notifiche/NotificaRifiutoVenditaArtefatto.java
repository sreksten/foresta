package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del commerciante e il costo
 * dell'Artefatto) rifiuta la vendita di un Artefatto verso un commerciante per mancanza di fondi.
 * <p>
 * NOTA: in questo momento i commercianti hanno fondi infiniti, per cui questo evento non può verificarsi.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoVenditaArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param eventoRichiestaVenditaArtefatto la richiesta di vendita di un Artefatto che si rifiuta
     */
    public NotificaRifiutoVenditaArtefatto(ComandoVenditaArtefatto eventoRichiestaVenditaArtefatto) {
        super(TipoEvento.NOTIFICA_RIFIUTO_VENDITA_ARTEFATTO, eventoRichiestaVenditaArtefatto);
    }
}
