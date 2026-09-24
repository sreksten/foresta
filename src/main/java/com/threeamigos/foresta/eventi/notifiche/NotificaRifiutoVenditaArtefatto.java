package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.motore.OggettoConCosto;

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
     * @param eventoRichiestaVenditaArtefatto la richiesta di vendita di un Artefatto che si rifiuta
     */
    public NotificaRifiutoVenditaArtefatto(ComandoVenditaArtefatto eventoRichiestaVenditaArtefatto) {
        super(TipoEvento.NOTIFICA_RIFIUTO_VENDITA_ARTEFATTO, eventoRichiestaVenditaArtefatto);
    }
}
