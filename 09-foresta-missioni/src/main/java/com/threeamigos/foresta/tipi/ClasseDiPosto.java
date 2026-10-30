package com.threeamigos.foresta.tipi;

import java.util.Optional;

/**
 * La classe del posto di una tappa di una missione a tappe (vedi TappeRichieste e missione_a_tappe.md): una città, una locanda nel
 * bosco, o uno dei posti della Foresta. La città è una locazione unica (una fra quelle in piedi), quindi non ha un {@link TipoLocazione}.
 *
 * @author Stefano Reksten
 */
public enum ClasseDiPosto {

	CITTA(null),
	LOCANDA(TipoLocazione.LOCANDA),
	ROVINE(TipoLocazione.ROVINE),
	TEMPIO(TipoLocazione.TEMPIO),
	GROTTA(TipoLocazione.GROTTA),
	BOSCO(TipoLocazione.BOSCO),
	PALUDE(TipoLocazione.PALUDE),
	RADURA(TipoLocazione.RADURA);

	private final TipoLocazione locazione;

	ClasseDiPosto(TipoLocazione locazione) {
		this.locazione = locazione;
	}

	public boolean isCitta() {
		return this == CITTA;
	}

	/**
	 * Il tipo di locazione dei posti di questa classe; vuoto per la città.
	 */
	public Optional<TipoLocazione> getLocazione() {
		return Optional.ofNullable(locazione);
	}
}
