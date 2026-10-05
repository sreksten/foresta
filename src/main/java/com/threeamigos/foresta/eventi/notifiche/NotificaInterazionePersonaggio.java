package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;

/**
 * Un evento di interazione con un effetto di stato che accade su un Personaggio durante un combattimento.
 *
 * @author Stefano Reksten
 */
public class NotificaInterazionePersonaggio extends EventoSuPersonaggio {

    private final TipoInterazioneConEffettiDiStato tipoInterazione;

    /**
     * @param personaggio il Personaggio su cui si verifica l'evento
     * @param tipoInterazione il tipo di interazione che si verifica
     */
    public NotificaInterazionePersonaggio(Personaggio personaggio, TipoInterazioneConEffettiDiStato tipoInterazione) {
        super(TipoEvento.NOTIFICA_INTERAZIONE_PERSONAGGIO, personaggio);
        this.tipoInterazione = tipoInterazione;
    }

    /**
     * @return il tipo di interazione che si verifica
     */
    public TipoInterazioneConEffettiDiStato getTipoInterazione() {
        return tipoInterazione;
    }
}
