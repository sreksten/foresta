package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoPrelievoArtefatto;
import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 * Il GruppoGiocatore (che controlla il peso dell'Artefatto e la disponibilità di carico di un Personaggio)
 * rifiuta lo spostamento di un Artefatto dall'inventario generale verso un Personaggio perché già troppo carico.
 * L'Artefatto rimane nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoPrelievoArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param eventoRichiestaPrelievoArtefatto la richiesta di prelievo di un Artefatto che si rifiuta
     */
    public NotificaRifiutoPrelievoArtefatto(ComandoPrelievoArtefatto eventoRichiestaPrelievoArtefatto) {
        super(TipoEvento.NOTIFICA_RIFIUTO_PRELIEVO_ARTEFATTO, eventoRichiestaPrelievoArtefatto);
    }
}
