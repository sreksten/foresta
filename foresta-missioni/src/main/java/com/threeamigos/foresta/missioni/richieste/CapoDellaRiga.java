package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Optional;

/**
 * Il capo dei nemici di una riga di grammatica (il campo CAPO= delle righe di missioni.txt): SI se il nome si pesca da
 * NOME_CAPOBANDA, il nome di un'altra produzione di nomi (NOME_CAMPIONE, NOME_CAMPIONESSA...) per pescarlo da lì,
 * oppure il nome stesso, quando i testi lo dicono (la guardia Teodolinda). Dopo il nome può esserci la classe del capo, se
 * non è uno della banda ({@code CAPO=SI HOBGOBLIN}: un hobgoblin a capo dei goblin).
 */
final class CapoDellaRiga {

	private static final String SI = "SI";
	private static final String NOMI_DEI_CAPIBANDA = "NOME_CAPOBANDA";
	private static final String PREFISSO_DEI_NOMI = "NOME_";

	private final String valore;
	// La classe del capo se non è uno della banda, altrimenti null
	private final TipoPersonaggio classe;

	private CapoDellaRiga(String valore, TipoPersonaggio classe) {
		this.valore = valore;
		this.classe = classe;
	}

	/**
	 * Il capo del campo CAPO=, o null se la riga non ce l'ha.
	 */
	static CapoDellaRiga da(Optional<String> campo) {
		return campo.map(CapoDellaRiga::daTesto).orElse(null);
	}

	private static CapoDellaRiga daTesto(String testo) {
		String[] parti = testo.trim().split("\\s+");
		if (parti.length > 2) {
			throw new IllegalArgumentException("Il capo è il nome e forse la classe (SI HOBGOBLIN): " + testo);
		}
		TipoPersonaggio classe = parti.length == 2 ? TipoPersonaggio.valueOf(parti[1]) : null;
		return new CapoDellaRiga(SI.equals(parti[0]) ? NOMI_DEI_CAPIBANDA : parti[0], classe);
	}

	/**
	 * La classe del capo se non è uno della banda (un hobgoblin a capo dei goblin), altrimenti null.
	 */
	TipoPersonaggio getClasse() {
		return classe;
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
		return isFisso() ? valore : ProduttoreDiMissioniCasuale.rigaDiMissioni(valore);
	}
}
