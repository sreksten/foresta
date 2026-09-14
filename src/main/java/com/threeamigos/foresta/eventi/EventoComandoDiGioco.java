package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

/**
 *
 * @author Stefano Reksten
 */
public class EventoComandoDiGioco extends EventoBase {

    private final Comando comando;

    public EventoComandoDiGioco(Comando comando) {
        super(TipoEvento.COMANDO_DI_GIOCO);
        this.comando = comando;
    }

    public Comando getComando() {
        return comando;
    }
}
