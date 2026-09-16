package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un Personaggio subisce una variazione di un Effetto di Stato.
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

    /**
     * @param personaggio il Personaggio che subisce la variazione dell'Effetto di Stato
     * @param tipoVariazione il tipo di variazione (aggiunta, variazione, rimozione)
     * @param effetto l'Effetto di Stato che il personaggio subisce
     * @param valorePrecedente il valore precedente alla variazione (durata)
     * @param nuovoValore il valore successivo alla variazione (durata)
     */
    public EventoVariazioneEffettoDiStato(Personaggio personaggio, TipoVariazione tipoVariazione, TipoEffettoDiStato effetto,
                                          int valorePrecedente, int nuovoValore) {
        super(TipoEvento.PERSONAGGIO_VARIAZIONE_EFFETTO_DI_STATO, personaggio);
        this.tipoVariazione = tipoVariazione;
        this.effetto = effetto;
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    /**
     * @return il tipo di variazione (aggiunta, variazione, rimozione)
     */
    public TipoVariazione getTipo() {
        return tipoVariazione;
    }

    /**
     * @return l'Effetto di Stato che il Personaggio subisce
     */
    public TipoEffettoDiStato getEffetto() {
        return effetto;
    }

    /**
     * @return il valore precedente alla variazione (durata)
     */
    public int getValorePrecedente() {
        return valorePrecedente;
    }

    /**
     * @return il valore successivo alla variazione (durata)
     */
    public int getNuovoValore() {
        return nuovoValore;
    }
}
