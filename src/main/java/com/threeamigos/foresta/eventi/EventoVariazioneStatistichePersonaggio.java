package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un Personaggio riceve una variazione a uno dei suoi Attributi (questo può provocare un ricalcolo delle
 * statistiche secondarie).
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneStatistichePersonaggio extends EventoPersonaggio {

    private final TipoAttributo tipoAttributo;
    private final double valorePrecedente;
    private final double nuovoValore;

    /**
     * @param personaggio il Personaggio la cui statistica cambia
     * @param tipoAttributo il tipo di Attributo che cambia
     * @param valorePrecedente il valore precedente alla variazione
     * @param nuovoValore il valore successivo alla variazione
     */
    public EventoVariazioneStatistichePersonaggio(Personaggio personaggio, TipoAttributo tipoAttributo,
                                                  double valorePrecedente, double nuovoValore) {
        super(TipoEvento.PERSONAGGIO_VARIAZIONE_STATISTICHE, personaggio);
        this.tipoAttributo = tipoAttributo;
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    /**
     * @return il tipo di Attributo che cambia
     */
    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }

    /**
     * @return il valore precedente alla variazione
     */
    public double getValorePrecedente() {
        return valorePrecedente;
    }

    /**
     * @return il valore successivo alla variazione
     */
    public double getNuovoValore() {
        return nuovoValore;
    }
}
