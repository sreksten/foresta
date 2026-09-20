package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Classe astratta per eventi che richiedono al giocatore di fare una scelta tra un insieme di possibili comandi.
 *
 * @author Stefano Reksten
 */
public class EventoConComandi extends EventoBase {

    private final Collection<Comando> possibilita;

    public EventoConComandi(TipoEvento tipoEvento, Collection<Comando> possibilita) {
        super(tipoEvento);
        this.possibilita = possibilita;
    }

    public Collection<Comando> getPossibilita() {
        return possibilita;
    }
}
