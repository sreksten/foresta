package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Acta est fabula. La partita, nel bene o nel male, è finita.
 *
 * @author Stefano Reksten
 */
public class NotificaFineGioco extends EventoBase {

    private final boolean completatoConSuccesso;

    public NotificaFineGioco(boolean completatoConSuccesso) {
        super(TipoEvento.NOTIFICA_FINE_GIOCO);
        this.completatoConSuccesso = completatoConSuccesso;
    }

    public boolean isCompletatoConSuccesso() {
        return completatoConSuccesso;
    }
}
