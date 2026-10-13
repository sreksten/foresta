package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Un avversario del gruppo è morto, per mano di un personaggio del gruppo o per gli effetti di stato, o si è arreso
 * (vedi InternoPersonaggioArreso). Chi tiene il conto delle uccisioni (statistiche, trofei) si iscrive qui.
 */
public class InternoAvversarioSconfitto extends EventoBase {

    private final TipoPersonaggio classe;

    public InternoAvversarioSconfitto(TipoPersonaggio classe) {
        super(TipoEvento.INTERNO_AVVERSARIO_SCONFITTO);
        this.classe = classe;
    }

    public TipoPersonaggio getClasse() {
        return classe;
    }
}
