package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoPrelievoArtefatto;
import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 * Il GruppoGiocatore (che controlla il peso dell'Artefatto e la disponibilità di carico di un Personaggio)
 * approva lo spostamento di un Artefatto dall'inventario generale verso un Personaggio.
 * L'Artefatto passa nell'inventario del Personaggio.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazionePrelievoArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param eventoRichiestaPrelievoArtefatto la richiesta di prelievo di un Artefatto che si approva
     */
    public NotificaApprovazionePrelievoArtefatto(ComandoPrelievoArtefatto eventoRichiestaPrelievoArtefatto) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_PRELIEVO_ARTEFATTO, eventoRichiestaPrelievoArtefatto);
    }
}
