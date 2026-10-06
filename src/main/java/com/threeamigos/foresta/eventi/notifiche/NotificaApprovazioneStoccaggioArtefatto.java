package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.interni.InternoStoccaggioArtefatto;
import com.threeamigos.foresta.interfacce.OggettoConPeso;

/**
 * Il GruppoGiocatore approva lo spostamento di un Artefatto da un Personaggio verso l'inventario generale.
 *
 * @author Stefano Reksten
 */
public class NotificaApprovazioneStoccaggioArtefatto extends NotificaApprovazioneSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param eventoRichiestaStoccaggioArtefatto la richiesta di stoccaggio di un Artefatto che si approva
     */
    public NotificaApprovazioneStoccaggioArtefatto(InternoStoccaggioArtefatto eventoRichiestaStoccaggioArtefatto) {
        super(TipoEvento.NOTIFICA_APPROVAZIONE_STOCCAGGIO_ARTEFATTO, eventoRichiestaStoccaggioArtefatto);
    }
}
