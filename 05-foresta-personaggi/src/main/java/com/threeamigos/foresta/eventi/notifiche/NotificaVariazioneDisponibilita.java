package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Quel che hanno in comune le notifiche che una scorta del gruppo (le pergamene di un incantesimo, un tipo di pozione) è
 * cambiata: quante ce n'erano e quante ce ne sono. Ogni scorta ha la sua sottoclasse, perché chi ascolta il bus si iscrive a
 * una classe precisa (il riquadro degli incantesimi e delle pozioni le ascolta tutte, una per una).
 *
 * @author Stefano Reksten
 */
public abstract class NotificaVariazioneDisponibilita extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;

    protected NotificaVariazioneDisponibilita(TipoEvento tipoEvento, int valorePrecedente, int nuovoValore) {
        super(tipoEvento);
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    /**
     * Quante ce n'erano prima del cambiamento
     */
    public int getValorePrecedente() {
        return valorePrecedente;
    }

    /**
     * Quante ce ne sono adesso
     */
    public int getNuovoValore() {
        return nuovoValore;
    }
}
