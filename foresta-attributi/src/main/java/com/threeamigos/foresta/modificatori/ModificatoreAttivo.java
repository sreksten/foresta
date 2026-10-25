package com.threeamigos.foresta.modificatori;

/**
 * Un modificatore che sta agendo su un personaggio, con la sua fonte (il nome dell'artefatto che lo porta o la nota
 * di un modificatore proprio, per esempio una benedizione): serve alla UI per dire da dove viene un valore.
 */
public final class ModificatoreAttivo {

	private final ModificatoreAttributoMD modificatore;
	private final String fonte;

	/**
	 * Un modificatore con la sua fonte.
	 *
	 * @param modificatore il modificatore che sta agendo
	 * @param fonte        da dove viene; null vale "non si sa"
	 */
	public ModificatoreAttivo(ModificatoreAttributoMD modificatore, String fonte) {
		this.modificatore = modificatore;
		this.fonte = fonte == null ? "" : fonte;
	}

	/**
	 * Il modificatore che sta agendo.
	 *
	 * @return il modificatore
	 */
	public ModificatoreAttributoMD getModificatore() {
		return modificatore;
	}

	/**
	 * Da dove viene il modificatore.
	 *
	 * @return il nome dell'artefatto o la nota del modificatore; vuoto se non si sa
	 */
	public String getFonte() {
		return fonte;
	}

}
