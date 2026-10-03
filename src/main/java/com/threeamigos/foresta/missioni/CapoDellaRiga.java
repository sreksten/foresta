package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;

import java.util.Optional;

/**
 * Il capo dei nemici di una riga di grammatica (il campo CAPO= delle righe di missioni.txt): SI se il nome si pesca da
 * NOME_CAPOBANDA, il nome di un'altra produzione di nomi (NOME_CAMPIONE, NOME_CAMPIONESSA...) per pescarlo da lì,
 * oppure il nome stesso, quando i testi lo dicono (la guardia Teodolinda).
 */
final class CapoDellaRiga {

	private static final String SI = "SI";
	private static final String NOMI_DEI_CAPIBANDA = "NOME_CAPOBANDA";
	private static final String PREFISSO_DEI_NOMI = "NOME_";

	private final String valore;

	private CapoDellaRiga(String valore) {
		this.valore = valore;
	}

	/**
	 * Il capo del campo CAPO=, o null se la riga non ce l'ha.
	 */
	static CapoDellaRiga da(Optional<String> campo) {
		return campo.map(valore -> new CapoDellaRiga(SI.equals(valore) ? NOMI_DEI_CAPIBANDA : valore)).orElse(null);
	}

	/**
	 * Se il nome è scritto nella riga, e non si pesca.
	 */
	boolean isFisso() {
		return !valore.startsWith(PREFISSO_DEI_NOMI);
	}

	/**
	 * Il nome del capo: quello scritto nella riga, o uno pescato dalla sua produzione.
	 */
	String pescaNome() {
		return isFisso() ? valore : ProduttoreDiTestiCasuale.rigaDiMissioni(valore);
	}
}
