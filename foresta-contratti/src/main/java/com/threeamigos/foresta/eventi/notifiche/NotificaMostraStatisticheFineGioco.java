package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Alla fine del gioco mostra al giocatore le statistiche sui personaggi sconfitti durante la partita.
 *
 * @author Stefano Reksten
 */
public class NotificaMostraStatisticheFineGioco extends EventoBase {

    public NotificaMostraStatisticheFineGioco() {
        super(TipoEvento.NOTIFICA_MOSTRA_STATISTICHE_FINE_GIOCO);
    }
}
