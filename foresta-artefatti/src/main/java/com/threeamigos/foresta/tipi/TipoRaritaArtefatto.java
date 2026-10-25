package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;


/**
 * Quanto è raro un artefatto. Decide quanti effetti (incantamenti più modificatori) può avere:
 * livello − 1 per un comune, livello per un raro, livello + 1 per un leggendario, fino al tetto della rarità.
 * I comuni sono il loot più probabile, i rari escono nel 10% dei casi, i leggendari non escono mai
 * nel loot perché si trovano nei templi o come premio di una missione.
 * <p>
 * I testi di ogni costante (NOME) non stanno qui ma nel file {@code tipo_rarita_artefatto_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoRaritaArtefatto {

	// L'ultimo numero è il tetto al numero di effetti (incantamenti più modificatori) di un artefatto, qualunque sia il
	// suo livello
	/**
	 * Un artefatto comune: due effetti in meno del livello, al più tre.
	 */
	COMUNE(-1, 3),
	/**
	 * Un artefatto raro: tanti effetti quanto il livello, al più quattro.
	 */
	RARO(0, 4),
	/**
	 * Un artefatto leggendario: un effetto in più del livello, al più cinque.
	 */
	LEGGENDARIO(1, 5);

	private final int scartoSulLivello;
	private final int tettoEffetti;

	TipoRaritaArtefatto(int scartoSulLivello, int tettoEffetti) {
		this.scartoSulLivello = scartoSulLivello;
		this.tettoEffetti = tettoEffetti;
	}

	/**
	 * Il nome della rarità, per i testi.
	 */
	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	/**
	 * Numero massimo di effetti per un artefatto incantabile di questo livello.
	 */
	public int getEffettiMassimi(int livello) {
		return Math.max(0, Math.min(tettoEffetti, livello + scartoSulLivello));
	}
}
