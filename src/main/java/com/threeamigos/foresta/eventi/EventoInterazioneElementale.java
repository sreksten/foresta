package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoInterazioneElementale extends EventoPersonaggio {

    private final TipoInterazioneElementale tipoInterazioneElementale;

    public EventoInterazioneElementale(Personaggio personaggio, TipoInterazioneElementale tipoInterazioneElementale) {
        super(TipoEvento.PERSONAGGIO_INTERAZIONE_ELEMENTALE, personaggio);
        this.tipoInterazioneElementale = tipoInterazioneElementale;
    }

    public TipoInterazioneElementale getTipoInterazioneElementale() {
        return tipoInterazioneElementale;
    }
}
