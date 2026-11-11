package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Un personaggio è entrato a far parte del gruppo del giocatore: quello scelto a inizio partita, un compagno reclutato, un aiuto a tempo, o chi
 * si ritrova in un gruppo caricato da un salvataggio (gli ospiti, che viaggiano con il gruppo senza farne parte, no). Il bestiario ne sblocca la voce; durante il tutorial il motore rende salvo dalla morte chi entra a tempo.
 */
public class InternoPersonaggioEntratoNelGruppo extends EventoBase {

    private final Personaggio personaggio;

    public InternoPersonaggioEntratoNelGruppo(Personaggio personaggio) {
        super(TipoEvento.INTERNO_PERSONAGGIO_ENTRATO_NEL_GRUPPO);
        this.personaggio = personaggio;
    }

    public Personaggio getPersonaggio() {
        return personaggio;
    }

    public TipoPersonaggio getClasse() {
        return personaggio.getClasse();
    }

}
