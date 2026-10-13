package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.ui.CoordinateFumetto;

/**
 * Evento interno del motore UI che mostra un certo messaggio sotto forma di fumetto.
 * (Commerciante che rifiuta una vendita, Personaggio che non può aggiungere un Artefatto al proprio inventario...)
 *
 * @author Stefano Reksten
 */
public class InternoNotificaViaFumettoATempo extends EventoBase {

    private final String testo;
    private final CoordinateFumetto coordinateFumetto;

    /**
     * @param testo il testo del fumetto da mostrare
     */
    public InternoNotificaViaFumettoATempo(String testo, CoordinateFumetto coordinateFumetto) {
        super(TipoEvento.INTERNO_NOTIFICA_VIA_FUMETTO_A_TEMPO);
        this.testo = testo;
        this.coordinateFumetto = coordinateFumetto;
    }

    /**
     * @return il testo del fumetto da mostrare
     */
    public String getTesto() {
        return testo;
    }

    /**
     * @return le coordinate alle quali disegnare balloon e puntamento
     */
    public CoordinateFumetto getCoordinateFumetto() {
        return coordinateFumetto;
    }
}

