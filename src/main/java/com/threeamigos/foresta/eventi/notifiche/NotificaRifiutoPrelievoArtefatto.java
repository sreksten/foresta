package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoPrelievoArtefatto;
import com.threeamigos.foresta.interfacce.OggettoConPeso;
import com.threeamigos.foresta.motore.RegoleEquipaggiamento;

/**
 * Il GruppoGiocatore rifiuta lo spostamento di un Artefatto dall'inventario generale verso un Personaggio
 * che non può equipaggiarlo (troppo carico, slot occupato, livello troppo alto...: vedi il motivo).
 * L'Artefatto rimane nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoPrelievoArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConPeso> {

    private final RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento esito;

    /**
     * @param eventoRichiestaPrelievoArtefatto la richiesta di prelievo di un Artefatto che si rifiuta
     * @param esito perché il personaggio non può prendere l'Artefatto
     */
    public NotificaRifiutoPrelievoArtefatto(ComandoPrelievoArtefatto eventoRichiestaPrelievoArtefatto,
                                            RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento esito) {
        super(TipoEvento.NOTIFICA_RIFIUTO_PRELIEVO_ARTEFATTO, eventoRichiestaPrelievoArtefatto);
        this.esito = esito;
    }

    public RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento getEsito() {
        return esito;
    }
}
