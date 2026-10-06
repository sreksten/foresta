package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.eventi.interni.InternoPrelievoArtefatto;
import com.threeamigos.foresta.interfacce.OggettoConPeso;

/**
 * Il GruppoGiocatore rifiuta lo spostamento di un Artefatto dall'inventario generale verso un Personaggio
 * che non può equipaggiarlo (troppo carico, slot occupato, livello troppo alto...: vedi il motivo).
 * L'Artefatto rimane nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaRifiutoPrelievoArtefatto extends NotificaRifiutoSpostamentoArtefatto<OggettoConPeso> {

    private final String motivo;

    /**
     * @param eventoRichiestaPrelievoArtefatto la richiesta di prelievo di un Artefatto che si rifiuta
     * @param motivo perché il personaggio non può prendere l'Artefatto, nelle parole del fumetto che lo dice
     */
    public NotificaRifiutoPrelievoArtefatto(InternoPrelievoArtefatto eventoRichiestaPrelievoArtefatto,
                                            String motivo) {
        super(TipoEvento.NOTIFICA_RIFIUTO_PRELIEVO_ARTEFATTO, eventoRichiestaPrelievoArtefatto);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
