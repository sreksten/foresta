package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Classe base astratta per eventi che accadono all'interno del motore e che richiedono al giocatore
 * di fare una scelta tra un insieme di possibili comandi.
 *
 * @author Stefano Reksten
 */
public class RichiestaConComandi extends EventoBase {

    private final Collection<Comando> possibilita;

    public RichiestaConComandi(TipoEvento tipoEvento, Collection<Comando> possibilita) {
        super(tipoEvento);
        this.possibilita = possibilita;
    }

    public Collection<Comando> getPossibilita() {
        return possibilita;
    }
}
