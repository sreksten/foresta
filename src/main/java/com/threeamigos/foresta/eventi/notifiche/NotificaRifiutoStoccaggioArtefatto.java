package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoStoccaggioArtefatto;
import com.threeamigos.foresta.motore.OggettoConPeso;

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
     * @param eventoRichiestaStoccaggioArtefatto la richiesta di stoccaggio di un Artefatto che si rifiuta
     */
    public NotificaRifiutoStoccaggioArtefatto(ComandoStoccaggioArtefatto eventoRichiestaStoccaggioArtefatto) {
        super(TipoEvento.NOTIFICA_RIFIUTO_STOCCAGGIO_ARTEFATTO, eventoRichiestaStoccaggioArtefatto);
    }
}
