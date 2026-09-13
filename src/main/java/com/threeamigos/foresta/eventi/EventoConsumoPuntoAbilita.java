package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.PersonaggioBase;

/**
 * Un giocatore consuma un Punto Abilità di un Personaggio per aumentargli un certo Attributo.
 *
 * @author Stefano Reksten
 */
public class EventoConsumoPuntoAbilita extends EventoPersonaggio {

    private final TipoAttributo tipoAttributo;

    /**
     * @param personaggio il Personaggio che consuma il Punto Abilità
     * @param tipoAttributo l'Attributo del Personaggio che viene aumentato
     */
	public EventoConsumoPuntoAbilita(PersonaggioBase personaggio, TipoAttributo tipoAttributo) {
		super(TipoEvento.PERSONAGGIO_CONSUMO_PUNTO_ABILITA, personaggio);
        this.tipoAttributo = tipoAttributo;
	}

    /**
     * @return l'Attributo del Personaggio che viene aumentato
     */
    public TipoAttributo getTipoAttributo() {
        return tipoAttributo;
    }
}
