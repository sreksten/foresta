package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoArtefatto;
import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere" controllando le disponibilità economiche del GruppoGiocatore e il costo
 * dell'Artefatto) approva l'acquisto di un Artefatto da un commerciante. L'Artefatto passa nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneAcquistoArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param comandoAcquistoArtefatto la richiesta di acquisto di un Artefatto che si approva
     */
    public NotificaApprovazioneAcquistoArtefatto(ComandoAcquistoArtefatto comandoAcquistoArtefatto) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_ACQUISTO_ARTEFATTO, comandoAcquistoArtefatto);
    }
}
