package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Mostra i migliori 10 giocatori di tutti i tempi
 *
 * @author Stefano Reksten
 */
public class NotificaMostraPunteggiMigliori extends EventoBase {

    public NotificaMostraPunteggiMigliori() {
        super(TipoEvento.NOTIFICA_MOSTRA_PUNTEGGI_MIGLIORI);
    }
}
