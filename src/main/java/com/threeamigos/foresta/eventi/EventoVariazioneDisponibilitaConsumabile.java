package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.modellodati.TipoConsumabile;

/**
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneDisponibilitaConsumabile extends EventoBase {

    private final TipoConsumabile tipoConsumabile;
    private final ClasseIncantesimo classeIncantesimo;
    private final int variazione;

    public EventoVariazioneDisponibilitaConsumabile(TipoConsumabile tipoConsumabile, ClasseIncantesimo classeIncantesimo, int variazione) {
        super(TipoEvento.VARIAZIONE_DISPONIBILITA_CONSUMABILE);
        this.tipoConsumabile = tipoConsumabile;
        this.classeIncantesimo = classeIncantesimo;
        this.variazione = variazione;
    }

    public EventoVariazioneDisponibilitaConsumabile(TipoConsumabile tipoConsumabile, int variazione) {
        super(TipoEvento.VARIAZIONE_DISPONIBILITA_CONSUMABILE);
        this.tipoConsumabile = tipoConsumabile;
        this.classeIncantesimo = null;
        this.variazione = variazione;
    }

    public TipoConsumabile getTipoConsumabile() {
        return tipoConsumabile;
    }

    public ClasseIncantesimo getClasseIncantesimo() {
        return classeIncantesimo;
    }

    public int getVariazione() {
        return variazione;
    }
}
