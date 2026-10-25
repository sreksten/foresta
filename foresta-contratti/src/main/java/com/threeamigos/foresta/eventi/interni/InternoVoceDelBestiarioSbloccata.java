package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Una voce del bestiario si è sbloccata per la prima volta, in tutte le partite: si è sconfitto un avversario di quella classe (o ci si è fatto
 * amico, o lo si è corrotto, o lo si è battuto a carta, forbici e sasso), oppure un personaggio di quella classe è entrato nel gruppo.
 */
public class InternoVoceDelBestiarioSbloccata extends EventoBase {

    private final TipoPersonaggio voce;

    /**
     * @param voce la voce, cioè il ruolo (la classe al maschile: Mago anche per la Maga)
     */
    public InternoVoceDelBestiarioSbloccata(TipoPersonaggio voce) {
        super(TipoEvento.INTERNO_VOCE_DEL_BESTIARIO_SBLOCCATA);
        this.voce = voce;
    }

    public TipoPersonaggio getVoce() {
        return voce;
    }

}
