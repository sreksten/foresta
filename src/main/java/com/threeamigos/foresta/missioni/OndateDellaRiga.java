package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

/**
 * Le ondate di una riga di grammatica (vedi IncontroDiMissione.poi): dopo i nemici della riga (NEMICO=, NUMERO=,
 * CAPO=) ne arrivano altri, sconfitti i primi, fino a {@value IncontroDiMissione#ONDATE_MASSIME} ondate in tutto.
 * <pre>
 * ONDATA_2=HOBGOBLIN 3;ARRIVO_2=Dal bosco arrivano tre hobgoblin!;ONDATA_3=TROLL 1 SI;ARRIVO_3=Arriva %CAPO_3%!
 * </pre>
 * Un'ondata è classe e numero, e se ha un capo con un nome il terzo pezzo è come CAPO= (SI, NOME_CAMPIONE... o il nome
 * stesso, vedi CapoDellaRiga). Nei testi %CAPO_2% e %CAPO_3% sono i nomi dei capi delle ondate.
 */
final class OndateDellaRiga {

	/**
	 * I campi delle ondate, da ammettere nelle righe che le possono avere.
	 */
	static final String[] CAMPI = {"ONDATA_2", "ARRIVO_2", "ONDATA_3", "ARRIVO_3"};
	private static final int PRIMA_ONDATA_SUCCESSIVA = 2;

	private static final class OndataDellaRiga {
		private final ClassePersonaggio classe;
		private final int numero;
		private final CapoDellaRiga capo;
		private final String arrivo;

		private OndataDellaRiga(ClassePersonaggio classe, int numero, CapoDellaRiga capo, String arrivo) {
			this.classe = classe;
			this.numero = numero;
			this.capo = capo;
			this.arrivo = arrivo;
		}
	}

	private final List<OndataDellaRiga> ondate = new ArrayList<>();

	private OndateDellaRiga(CampiDiGrammatica campi, String riga) {
		for (int n = PRIMA_ONDATA_SUCCESSIVA; n <= IncontroDiMissione.ONDATE_MASSIME; n++) {
			Optional<String> ondata = campi.facoltativo("ONDATA_" + n);
			Optional<String> arrivo = campi.facoltativo("ARRIVO_" + n);
			if (ondata.isPresent() != arrivo.isPresent()) {
				throw new IllegalArgumentException("ONDATA_" + n + " e ARRIVO_" + n + " vanno insieme: " + riga);
			}
			if (!ondata.isPresent()) {
				continue;
			}
			if (ondate.size() != n - PRIMA_ONDATA_SUCCESSIVA) {
				throw new IllegalArgumentException("ONDATA_" + n + " senza l'ondata prima: " + riga);
			}
			String[] parti = ondata.get().trim().split("\\s+", 3);
			if (parti.length < 2) {
				throw new IllegalArgumentException("Un'ondata è classe e numero, e forse il capo (TROLL 1 SI): " + riga);
			}
			int numero = Integer.parseInt(parti[1]);
			if (numero < 1) {
				throw new IllegalArgumentException("Un'ondata ha almeno un nemico: " + riga);
			}
			ondate.add(new OndataDellaRiga(ClassePersonaggio.valueOf(parti[0]), numero,
					CapoDellaRiga.da(parti.length == 3 ? Optional.of(parti[2]) : Optional.empty()), arrivo.get()));
		}
	}

	static OndateDellaRiga da(CampiDiGrammatica campi, String riga) {
		return new OndateDellaRiga(campi, riga);
	}

	/**
	 * Unisce ai campi di una produzione quelli delle ondate.
	 */
	static String[] conICampiDelleOndate(String... campi) {
		String[] tutti = new String[campi.length + CAMPI.length];
		System.arraycopy(campi, 0, tutti, 0, campi.length);
		System.arraycopy(CAMPI, 0, tutti, campi.length, CAMPI.length);
		return tutti;
	}

	/**
	 * Il segnaposto del nome del capo di quell'ondata nei testi: %CAPO_2%.
	 */
	static String capo(int ondata) {
		return "%CAPO_" + ondata + "%";
	}

	boolean isVuota() {
		return ondate.isEmpty();
	}

	/**
	 * I numeri delle ondate con un capo con un nome (2, 3).
	 */
	List<Integer> getOndateConCapo() {
		List<Integer> conCapo = new ArrayList<>();
		for (int i = 0; i < ondate.size(); i++) {
			if (ondate.get(i).capo != null) {
				conCapo.add(i + PRIMA_ONDATA_SUCCESSIVA);
			}
		}
		return conCapo;
	}

	/**
	 * Il nome del capo di quell'ondata, scritto nella riga o pescato. Solo se ne ha uno.
	 */
	String pescaNomeDelCapo(int ondata) {
		return ondate.get(ondata - PRIMA_ONDATA_SUCCESSIVA).capo.pescaNome();
	}

	/**
	 * Aggiunge le ondate all'incontro, con i nomi dei capi e i testi che la missione ha già fissato.
	 */
	IncontroDiMissione aggiungiA(IncontroDiMissione incontro, IntFunction<String> nomeDelCapo, UnaryOperator<String> testo) {
		for (int i = 0; i < ondate.size(); i++) {
			OndataDellaRiga ondata = ondate.get(i);
			IncontroDiMissione nemici = IncontroDiMissione.di(ondata.classe, ondata.numero);
			if (ondata.capo != null) {
				nemici.conCapo(nomeDelCapo.apply(i + PRIMA_ONDATA_SUCCESSIVA));
			}
			incontro.poi(nemici, testo.apply(ondata.arrivo));
		}
		return incontro;
	}
}
