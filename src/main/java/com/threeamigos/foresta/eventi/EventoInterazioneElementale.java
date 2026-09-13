package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un evento di interazione elementale che accade su un Personaggio durante un combattimento.
 *
 * @author Stefano Reksten
 */
public class EventoInterazioneElementale extends EventoPersonaggio {

    private final TipoInterazioneElementale tipoInterazioneElementale;

    /**
     * @param personaggio il Personaggio su cui si verifica l'evento
     * @param tipoInterazioneElementale il tipo di interazione elementale che si verifica
     */
    public EventoInterazioneElementale(Personaggio personaggio, TipoInterazioneElementale tipoInterazioneElementale) {
        super(TipoEvento.PERSONAGGIO_INTERAZIONE_ELEMENTALE, personaggio);
        this.tipoInterazioneElementale = tipoInterazioneElementale;
    }

    /**
     * @return il tipo di interazione elementale che si verifica
     */
    public TipoInterazioneElementale getTipoInterazioneElementale() {
        return tipoInterazioneElementale;
    }
}
