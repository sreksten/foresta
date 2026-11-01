package com.threeamigos.foresta.testi;

/**
 * Il genere grammaticale di un nome. Dipende dalla lingua: "spada" è femminile in italiano, in tedesco "Schwert" è neutro,
 * in inglese i nomi non hanno genere e si usa {@link #NEUTRO}.
 */
public enum Genere {
	MASCHILE, FEMMINILE, NEUTRO;

	/**
	 * Il genere dalla sua sigla nei file dei testi: "M", "F" o "N" (anche in minuscolo); vuoto o sconosciuto vale
	 * {@link #NEUTRO}.
	 */
	public static Genere dallaSigla(String sigla) {
		if (sigla == null) {
			return NEUTRO;
		}
		switch (sigla.trim().toUpperCase(java.util.Locale.ROOT)) {
			case "M":
				return MASCHILE;
			case "F":
				return FEMMINILE;
			default:
				return NEUTRO;
		}
	}
}
