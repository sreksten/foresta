package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * L'incantatore accetta quel che c'è sul banco ma avverte di qualcosa (es. gli incantamenti elementali che
 * su un libro magico andranno persi).
 */
public class NotificaAvvisoIncantatura extends EventoBase {

    private final String frase;

    public NotificaAvvisoIncantatura(String frase) {
        super(TipoEvento.NOTIFICA_AVVISO_INCANTATURA);
        this.frase = frase;
    }

    public String getFrase() {
        return frase;
    }
}
