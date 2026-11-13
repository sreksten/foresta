package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * La base degli eventi con cui il motore chiede al giocatore di scegliere fra alcuni comandi (per esempio i pulsanti di
 * una domanda): la UI mostra le {@link #getPossibilita() possibilità} e risponde con il comando scelto. Sta in questo
 * modulo, e non negli eventi, perché porta dei {@link Comando}.
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
