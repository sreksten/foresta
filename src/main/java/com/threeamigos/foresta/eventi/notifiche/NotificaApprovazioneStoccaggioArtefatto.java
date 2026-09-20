package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoStoccaggioArtefatto;
import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 * Il GruppoGiocatore approva lo spostamento di un Artefatto da un Personaggio verso l'inventario generale.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneStoccaggioArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param eventoRichiestaStoccaggioArtefatto la richiesta di stoccaggio di un Artefatto che si approva
     */
    public NotificaApprovazioneStoccaggioArtefatto(ComandoStoccaggioArtefatto eventoRichiestaStoccaggioArtefatto) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_STOCCAGGIO_ARTEFATTO, eventoRichiestaStoccaggioArtefatto);
    }
}
