package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un personaggio si è arreso invece di morire, in un combattimento fino alla resa (vedi Personaggio.isFinoAllaResa):
 * è vivo, ma in panchina. Può essere un avversario (che conta comunque come sconfitto, vedi InternoAvversarioSconfitto)
 * o un personaggio del gruppo.
 */
public class InternoPersonaggioArreso extends EventoBase {

    private final Personaggio personaggio;

    public InternoPersonaggioArreso(Personaggio personaggio) {
        super(TipoEvento.INTERNO_PERSONAGGIO_ARRESO);
        this.personaggio = personaggio;
    }

    public Personaggio getPersonaggio() {
        return personaggio;
    }
}
