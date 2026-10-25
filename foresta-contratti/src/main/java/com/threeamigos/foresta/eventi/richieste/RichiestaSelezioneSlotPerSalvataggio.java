package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;

import java.util.Collection;

/**
 * Chiede al giocatore in quale slot salvare la partita; porta le testate di quelli disponibili.
 *
 * @author Stefano Reksten
 */
public class RichiestaSelezioneSlotPerSalvataggio extends EventoBase {

    private final Collection<TestataSalvataggio> salvataggiDisponibili;

    public RichiestaSelezioneSlotPerSalvataggio(Collection<TestataSalvataggio> salvataggiDisponibili) {
        super(TipoEvento.RICHIESTA_SELEZIONE_SLOT_PER_SALVATAGGIO);
        this.salvataggiDisponibili = salvataggiDisponibili;
    }

    public Collection<TestataSalvataggio> getSalvataggiDisponibili() {
        return salvataggiDisponibili;
    }

}
