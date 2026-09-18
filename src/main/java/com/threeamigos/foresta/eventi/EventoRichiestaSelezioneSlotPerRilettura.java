package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.tools.TestataSalvataggio;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaSelezioneSlotPerRilettura extends EventoBase {

    private final Collection<TestataSalvataggio> salvataggiDisponibili;

    public EventoRichiestaSelezioneSlotPerRilettura(Collection<TestataSalvataggio> salvataggiDisponibili) {
        super(TipoEvento.RICHIESTA_SELEZIONE_SLOT_PER_RILETTURA);
        this.salvataggiDisponibili = salvataggiDisponibili;
    }

    public Collection<TestataSalvataggio> getSalvataggiDisponibili() {
        return salvataggiDisponibili;
    }
}
