package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.OggettoConPeso;
import com.threeamigos.foresta.interfacce.VistaScambio;

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
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggetto l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta
     * @param motivo perché il personaggio non può prendere l'Artefatto, nelle parole del fumetto che lo dice
     */
    public NotificaRifiutoPrelievoArtefatto(VistaScambio scambio, OggettoConPeso oggetto, String uuidRichiesta, String motivo) {
        super(TipoEvento.NOTIFICA_RIFIUTO_PRELIEVO_ARTEFATTO, scambio, oggetto, uuidRichiesta);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

}
