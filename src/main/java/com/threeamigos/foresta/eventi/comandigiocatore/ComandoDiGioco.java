package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.Comando;

/**
 * Il giocatore invia un generico comando al motore per proseguire il gioco.
 *
 * @author Stefano Reksten
 */
public class ComandoDiGioco extends EventoBase {

    private final Comando comando;

    public ComandoDiGioco(Comando comando) {
        super(TipoEvento.COMANDO_DI_GIOCO);
        this.comando = comando;
    }

    public Comando getComando() {
        return comando;
    }
}
