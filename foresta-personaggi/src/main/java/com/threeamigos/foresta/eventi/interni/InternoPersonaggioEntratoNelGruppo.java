package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Un personaggio è entrato a far parte del gruppo del giocatore: quello scelto a inizio partita, un compagno reclutato, un aiuto a tempo, o chi
 * si ritrova in un gruppo caricato da un salvataggio (gli ospiti, che viaggiano con il gruppo senza farne parte, no). Il bestiario ne sblocca la voce.
 */
public class InternoPersonaggioEntratoNelGruppo extends EventoBase {

    private final TipoPersonaggio classe;

    public InternoPersonaggioEntratoNelGruppo(TipoPersonaggio classe) {
        super(TipoEvento.INTERNO_PERSONAGGIO_ENTRATO_NEL_GRUPPO);
        this.classe = classe;
    }

    public TipoPersonaggio getClasse() {
        return classe;
    }

}
