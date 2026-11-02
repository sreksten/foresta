package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.NegozioInScena;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import com.threeamigos.foresta.testi.ProduttoreDiFrasiDelMondoCasuale;

/**
 * Costruisce l'intermezzo di ogni TipoIntermezzo. La funzione riceve il tipo stesso: un intermezzo può usarlo come
 * identificativo. Quelli di questo modulo si registrano da soli; quelli che hanno bisogno di chi sta sopra (le missioni) li
 * registra chi li conosce, con {@link #registra}.
 */
public final class FabbricaIntermezzi {

	// I saluti dei negozianti (da mondo.txt)
	private static String salutoAlchimista() {
		return ProduttoreDiFrasiDelMondoCasuale.salutoDellAlchimista();
	}

	private static String salutoIncantatore() {
		return ProduttoreDiFrasiDelMondoCasuale.salutoDellIncantatore();
	}

	private static final Map<TipoIntermezzo, Function<TipoIntermezzo, Intermezzo>> COSTRUTTORI = new EnumMap<>(TipoIntermezzo.class);

	static {
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_INTRODUTTIVO, tipo -> new IntermezzoIntroduttivo());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_FINE_PRIMA_LOCAZIONE, tipo -> new IntermezzoFinePrimaLocazione());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA, tipo -> new IntermezzoLocandaPrimaVisita());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_LOCANDA_VISITA_SUCCESSIVA, tipo -> new IntermezzoLocandaVisitaSuccessiva());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_ARMAIOLO, tipo -> new IntermezzoBenvenutoNegozio(tipo, NegozioInScena.ARMAIOLO, ProduttoreDiFrasiDelMondoCasuale.salutoDellArmaiolo()));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_ALCHIMISTA, tipo -> new IntermezzoOroscopo(tipo, salutoAlchimista()));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_SPADA_NASCOSTA, tipo -> new IntermezzoSpadaNascosta(tipo, salutoAlchimista()));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_VENDITORE_DI_PERGAMENE, tipo -> new IntermezzoBenvenutoNegozio(tipo, NegozioInScena.VENDITORE_DI_PERGAMENE, ProduttoreDiFrasiDelMondoCasuale.salutoDelVenditoreDiPergamene()));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_INCANTATORE, tipo -> new IntermezzoBenvenutoNegozio(tipo, NegozioInScena.INCANTATORE, salutoIncantatore()));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_RIPROGRAMMAZIONE, tipo -> new IntermezzoRiprogrammazione(tipo, salutoIncantatore()));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_ACCAMPAMENTO, tipo -> new IntermezzoAccampamento());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO, tipo -> new IntermezzoLibroTecnico());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_ARMAIOLO, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.ARMAIOLO));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_ALCHIMISTA, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.ALCHIMISTA));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_VENDITORE_DI_PERGAMENE, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.VENDITORE_DI_PERGAMENE));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_INCANTATORE, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.INCANTATORE));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_LOCANDA, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.LOCANDA));
	}

	private FabbricaIntermezzi() {
	}

	/**
	 * Dice come costruire l'intermezzo di quel tipo (per quelli che questo modulo non conosce).
	 */
	public static void registra(TipoIntermezzo tipo, Function<TipoIntermezzo, Intermezzo> costruttore) {
		COSTRUTTORI.put(tipo, costruttore);
	}

	/**
	 * Se si sa costruire l'intermezzo di quel tipo.
	 */
	public static boolean isRegistrato(TipoIntermezzo tipo) {
		return COSTRUTTORI.containsKey(tipo);
	}

	/**
	 * L'intermezzo di quel tipo.
	 *
	 * @throws IllegalStateException se nessuno ha detto come costruirlo (vedi {@link #registra})
	 */
	public static Intermezzo crea(TipoIntermezzo tipo) {
		Function<TipoIntermezzo, Intermezzo> costruttore = COSTRUTTORI.get(tipo);
		if (costruttore == null) {
			throw new IllegalStateException("Nessuno ha detto come costruire l'intermezzo " + tipo + " (vedi FabbricaIntermezzi.registra)");
		}
		return costruttore.apply(tipo);
	}
}
