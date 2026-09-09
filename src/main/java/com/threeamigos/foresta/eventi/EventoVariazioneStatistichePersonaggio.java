package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneStatistichePersonaggio extends EventoPersonaggio {

    private final TipoAttributo tipoAttributo;
    private final double valorePrecedente;
    private final double nuovoValore;

    public EventoVariazioneStatistichePersonaggio(Personaggio personaggio, TipoAttributo tipoAttributo,
                                                  double valorePrecedente, double nuovoValore) {
        super(TipoEvento.PERSONAGGIO_VARIAZIONE_STATISTICHE, personaggio);
        this.tipoAttributo = tipoAttributo;
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }

    public double getValorePrecedente() {
        return valorePrecedente;
    }

    public double getNuovoValore() {
        return nuovoValore;
    }
}
