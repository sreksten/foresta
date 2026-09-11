package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.PersonaggioBase;

/**
 *
 * @author Stefano Reksten
 */
public class EventoConsumoPuntoAbilita extends EventoPersonaggio {

    private final TipoAttributo tipoAttributo;

	public EventoConsumoPuntoAbilita(PersonaggioBase personaggio, TipoAttributo tipoAttributo) {
		super(TipoEvento.PERSONAGGIO_CONSUMO_PUNTO_ABILITA, personaggio);
        this.tipoAttributo = tipoAttributo;
	}

    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }
}
