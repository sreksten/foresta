package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del commerciante e il costo
 * dell'Artefatto) approva la vendita di un Artefatto da un commerciante. L'Artefatto passa nell'inventario del
 * commerciante.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneVenditaArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param eventoRichiestaVenditaArtefatto la richiesta di vendita di un Artefatto che si approva
     */
    public NotificaApprovazioneVenditaArtefatto(ComandoVenditaArtefatto eventoRichiestaVenditaArtefatto) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_VENDITA_ARTEFATTO, eventoRichiestaVenditaArtefatto);
    }
}
