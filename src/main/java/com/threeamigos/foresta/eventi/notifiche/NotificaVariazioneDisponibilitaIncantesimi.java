package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;

/**
 * Notifica un cambiamento nel totale degli incantesimi di un certo tipo disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneDisponibilitaIncantesimi extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;
    private final ClasseIncantesimo classeIncantesimo;

    public NotificaVariazioneDisponibilitaIncantesimi(ClasseIncantesimo classeIncantesimo, int valorePrecedente, int nuovoValore) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_DISPONIBILITA_INCANTESIMI);
        this.classeIncantesimo = classeIncantesimo;
        this.valorePrecedente = valorePrecedente;
        this.nuovoValore = nuovoValore;
    }

    public ClasseIncantesimo getClasseIncantesimo() {
        return classeIncantesimo;
    }

    public int getValorePrecedente() {
        return valorePrecedente;
    }

    public int getNuovoValore() {
        return nuovoValore;
    }
}
