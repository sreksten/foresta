package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneEffettoDiStato extends EventoPersonaggio {

    public enum TipoVariazione {
        AGGIUNTA,
        VARIAZIONE,
        RIMOZIONE
    }

    private final TipoVariazione tipoVariazione;
    private final TipoEffettoDiStato effetto;
    private final int valorePrecedente;
    private final int nuovoValore;

    public EventoVariazioneEffettoDiStato(Personaggio personaggio, TipoVariazione tipoVariazione, TipoEffettoDiStato effetto,
                                          int valorePrecedente, int nuovoValore) {
        super(TipoEvento.PERSONAGGIO_VARIAZIONE_EFFETTO_DI_STATO, personaggio);
        this.tipoVariazione = tipoVariazione;
        this.effetto = effetto;
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    public TipoVariazione getTipo() {
        return tipoVariazione;
    }

    public TipoEffettoDiStato getEffetto() {
        return effetto;
    }

    public int getValorePrecedente() {
        return valorePrecedente;
    }

    public int getNuovoValore() {
        return nuovoValore;
    }
}
