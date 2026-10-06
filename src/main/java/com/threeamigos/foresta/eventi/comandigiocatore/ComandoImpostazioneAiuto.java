package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il giocatore accende o spegne i cartigli dell'aiuto con l'interruttore della barra delle icone. L'Automa lo
 * scrive nel modello dati, che lo salva con la partita.
 */
public class ComandoImpostazioneAiuto extends EventoBase {

    private final boolean abilitato;

    public ComandoImpostazioneAiuto(boolean abilitato) {
        super(TipoEvento.COMANDO_IMPOSTAZIONE_AIUTO);
        this.abilitato = abilitato;
    }

    public boolean isAbilitato() {
        return abilitato;
    }
}
