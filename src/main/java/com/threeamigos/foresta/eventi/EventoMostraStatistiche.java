package com.threeamigos.foresta.eventi;

/**
 * Chiede alla UI di mostrare le statistiche sui personaggi sconfitti durante la partita.
 *
 * @author Stefano Reksten
 */
public class EventoMostraStatistiche extends EventoBase {

    public EventoMostraStatistiche() {
        super(TipoEvento.MOSTRA_STATISTICHE);
    }
}
