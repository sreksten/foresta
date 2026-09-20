package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import java.util.Collection;

/**
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
