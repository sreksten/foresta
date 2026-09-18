package com.threeamigos.foresta.eventi;

/**
 * Prepara la locazione per il turno che deve iniziare.
 *
 * @author Stefano Reksten
 */
public class EventoPreparazioneLocazione extends EventoBase {

    public EventoPreparazioneLocazione() {
        super(TipoEvento.PREPARAZIONE_LOCAZIONE);
    }
}
