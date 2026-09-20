package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un evento di interazione elementale che accade su un Personaggio durante un combattimento.
 *
 * @author Stefano Reksten
 */
public class NotificaInterazioneElementalePersonaggio extends EventoSuPersonaggio {

    private final TipoInterazioneElementale tipoInterazioneElementale;

    /**
     * @param personaggio il Personaggio su cui si verifica l'evento
     * @param tipoInterazioneElementale il tipo di interazione elementale che si verifica
     */
    public NotificaInterazioneElementalePersonaggio(Personaggio personaggio, TipoInterazioneElementale tipoInterazioneElementale) {
        super(TipoEvento.NOTIFICA_INTERAZIONE_ELEMENTALE_PERSONAGGIO, personaggio);
        this.tipoInterazioneElementale = tipoInterazioneElementale;
    }

    /**
     * @return il tipo di interazione elementale che si verifica
     */
    public TipoInterazioneElementale getTipoInterazioneElementale() {
        return tipoInterazioneElementale;
    }
}
