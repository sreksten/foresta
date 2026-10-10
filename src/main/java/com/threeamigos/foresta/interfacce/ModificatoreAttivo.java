package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;

/**
 * Un modificatore che sta agendo su un personaggio, con la sua fonte (il nome dell'artefatto che lo porta o la nota
 * di un modificatore proprio, per esempio una benedizione): serve alla UI per dire da dove viene un valore.
 */
public final class ModificatoreAttivo {

	private final ModificatoreAttributoMD modificatore;
	private final String fonte;

	public ModificatoreAttivo(ModificatoreAttributoMD modificatore, String fonte) {
		this.modificatore = modificatore;
		this.fonte = fonte == null ? "" : fonte;
	}

	public ModificatoreAttributoMD getModificatore() {
		return modificatore;
	}

	/**
	 * @return il nome dell'artefatto o la nota del modificatore; vuoto se non si sa
	 */
	public String getFonte() {
		return fonte;
	}
}
