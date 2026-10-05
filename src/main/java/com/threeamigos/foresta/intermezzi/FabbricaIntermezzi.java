package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Costruisce l'intermezzo di ogni TipoIntermezzo. La funzione riceve il tipo stesso: un intermezzo può usarlo come
 * identificativo.
 */
public final class FabbricaIntermezzi {

	private static final Map<TipoIntermezzo, Function<TipoIntermezzo, Intermezzo>> COSTRUTTORI = new EnumMap<>(TipoIntermezzo.class);

	static {
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_INTRODUTTIVO, tipo -> new IntermezzoIntroduttivo());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_FINE_PRIMA_LOCAZIONE, tipo -> new IntermezzoFinePrimaLocazione());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_LOCANDA_PRIMA_VISITA, tipo -> new IntermezzoLocandaPrimaVisita());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_LOCANDA_SECONDA_VISITA, tipo -> new IntermezzoLocandaSecondaVisita());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_ARMAIOLO, tipo -> new IntermezzoBenvenutoNegozio(tipo, NegozioInScena.ARMAIOLO, "Benvenuto nella mia bottega da armaiolo. Compro e vendo armi, armature, elmi, tutto della migliore qualità."));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_ALCHIMISTA, tipo -> new IntermezzoBenvenutoNegozio(tipo, NegozioInScena.ALCHIMISTA, "Benvenuti nella mia bottega da Alchimista! Posso vendervi incantesimi, pozioni, mappe oppure incrementare il vostro potere magico."));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_VENDITORE_DI_PERGAMENE, tipo -> new IntermezzoBenvenutoNegozio(tipo, NegozioInScena.VENDITORE_DI_PERGAMENE, "Benvenuti nel mio negozio di Pergamene, Amuleti, Gingilli e Ninnoli vari! Questi sono gli ingredienti di base necessari per migliorare le vostre armi."));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_INCANTATORE, tipo -> new IntermezzoBenvenutoNegozio(tipo, NegozioInScena.INCANTATORE, "Benvenuti nel mio laboratorio di incantamenti. Posso migliorare le vostre armi con quel pizzico di magia in più."));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_ACCAMPAMENTO, tipo -> new IntermezzoAccampamento());
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_ARMAIOLO, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.ARMAIOLO));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_ALCHIMISTA, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.ALCHIMISTA));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_VENDITORE_DI_PERGAMENE, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.VENDITORE_DI_PERGAMENE));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_INCANTATORE, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.INCANTATORE));
		COSTRUTTORI.put(TipoIntermezzo.INTERMEZZO_NOTTE_LOCANDA, tipo -> new IntermezzoNonChiudeteMai(tipo, NegozioInScena.LOCANDA));
	}

	private FabbricaIntermezzi() {
	}

	public static Intermezzo crea(TipoIntermezzo tipo) {
		return COSTRUTTORI.get(tipo).apply(tipo);
	}
}
