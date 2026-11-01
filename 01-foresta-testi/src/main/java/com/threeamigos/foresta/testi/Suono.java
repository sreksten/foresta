package com.threeamigos.foresta.testi;

/**
 * Come comincia un nome, per scegliere l'articolo: dipende dal suono e non dalla lettera ("un'ora" e "un uomo" in italiano,
 * "a unicorn" e "an hour" in inglese). Quali suoni contano lo dice la lingua; se un nome fa eccezione, il suo file dei testi
 * lo dichiara con la voce {@code SUONO}.
 */
public enum Suono {
	CONSONANTE,
	VOCALE,
	/**
	 * Solo per l'italiano: s seguita da consonante, z, gn, ps... ("lo scheletro", "uno zombi").
	 */
	S_IMPURA;

	/**
	 * Il suono dalla voce {@code SUONO} dei file dei testi; vuoto o sconosciuto vale null, cioè «lo decide la lingua dalla
	 * lettera iniziale».
	 */
	public static Suono dalNome(String nome) {
		if (nome == null || nome.trim().isEmpty()) {
			return null;
		}
		try {
			return valueOf(nome.trim().toUpperCase(java.util.Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
