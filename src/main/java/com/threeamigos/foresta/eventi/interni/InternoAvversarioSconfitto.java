package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 * Un avversario del gruppo è morto: per mano di un personaggio del gruppo o per gli
 * effetti di stato. Chi tiene il conto delle uccisioni (statistiche, trofei) si iscrive qui.
 */
public class InternoAvversarioSconfitto extends EventoBase {

    private final ClassePersonaggio classe;

    public InternoAvversarioSconfitto(ClassePersonaggio classe) {
        super(TipoEvento.INTERNO_AVVERSARIO_SCONFITTO);
        this.classe = classe;
    }

    public ClassePersonaggio getClasse() {
        return classe;
    }
}
