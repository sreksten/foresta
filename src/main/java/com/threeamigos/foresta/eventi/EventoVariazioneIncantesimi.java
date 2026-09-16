package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;

/**
 * Notifica un cambiamento nel totale degli incantesimi di un certo tipo disponibili nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneIncantesimi extends EventoBase {

    private final int valorePrecedente;
    private final int nuovoValore;
    private final ClasseIncantesimo classeIncantesimo;

    public EventoVariazioneIncantesimi(ClasseIncantesimo classeIncantesimo, int valorePrecedente, int nuovoValore) {
        super(TipoEvento.VARIAZIONE_INCANTESIMI);
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
