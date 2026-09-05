package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneStatistichePersonaggio extends EventoPersonaggio{

    private final TipoAttributo tipoAttributo;
    private final int variazione;

    public EventoVariazioneStatistichePersonaggio(Personaggio personaggio, TipoAttributo tipoAttributo, int variazione) {
        super(personaggio);
        this.tipoAttributo = tipoAttributo;
        this.variazione = variazione;
    }

    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }

    public int getVariazione() {
        return variazione;
    }
}
