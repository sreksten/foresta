package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un attacco o un incantesimo contro un Personaggio non è andato a segno.
 */
public class NotificaColpoMancato extends EventoSuPersonaggio {

    /**
     * @param personaggio il Personaggio che è stato mancato
     */
    public NotificaColpoMancato(VistaPersonaggio personaggio) {
        super(TipoEvento.NOTIFICA_COLPO_MANCATO, personaggio);
    }

}
