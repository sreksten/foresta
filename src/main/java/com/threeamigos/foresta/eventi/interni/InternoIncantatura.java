package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.BancoDiLavoro;

/**
 * Il giocatore conferma la fusione di quel che sta sul banco di lavoro, con il nome proprio scelto
 * per l'artefatto. Il GruppoGiocatore ricontrolla le regole, fa pagare e risponde con
 * NotificaApprovazioneIncantatura o NotificaRifiutoIncantatura.
 */
public class InternoIncantatura extends EventoBase {

    private final BancoDiLavoro banco;
    private final String nomeProprio;

    public InternoIncantatura(BancoDiLavoro banco, String nomeProprio) {
        super(TipoEvento.INTERNO_INCANTATURA);
        this.banco = banco;
        this.nomeProprio = nomeProprio;
    }

    public BancoDiLavoro getBanco() {
        return banco;
    }

    public String getNomeProprio() {
        return nomeProprio;
    }
}
