package com.threeamigos.foresta.eventi;

/**
 * Chiede alla UI di mostrare la schermata principale di gioco.
 *
 * @author Stefano Reksten
 */
public class EventoMostraSchermataGioco extends EventoBase {

    public EventoMostraSchermataGioco() {
        super(TipoEvento.MOSTRA_SCHERMATA_GIOCO);
    }
}
