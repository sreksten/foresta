package com.threeamigos.foresta.intermezzi;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Elenca le classi concrete di intermezzo, come ClasseMissione per le missioni. L'ordine
 * conta: se più intermezzi scattano nello stesso momento vengono mostrati in quest'ordine.
 */
public enum ClasseIntermezzo {

	INTERMEZZO_INTRODUTTIVO(IntermezzoIntroduttivo::new),
	INTERMEZZO_FINE_PRIMA_LOCAZIONE(IntermezzoFinePrimaLocazione::new),
	INTERMEZZO_LOCANDA_PRIMA_VISITA(IntermezzoLocandaPrimaVisita::new),
	INTERMEZZO_LOCANDA_SECONDA_VISITA(IntermezzoLocandaSecondaVisita::new),
	INTERMEZZO_ARMAIOLO(classe -> new IntermezzoBenvenutoNegozio(classe, NegozioInScena.ARMAIOLO,
			"Benvenuto nella mia bottega da armaiolo. Compro e vendo armi, armature, elmi, tutto della migliore qualità.")),
	INTERMEZZO_ALCHIMISTA(classe -> new IntermezzoBenvenutoNegozio(classe, NegozioInScena.ALCHIMISTA,
			"Benvenuti nella mia bottega da Alchimista! Posso vendervi incantesimi, pozioni, mappe oppure incrementare il vostro potere magico.")),
	INTERMEZZO_VENDITORE_DI_PERGAMENE(classe -> new IntermezzoBenvenutoNegozio(classe, NegozioInScena.VENDITORE_DI_PERGAMENE,
			"Benvenuti nel mio negozio di Pergamene, Amuleti, Gingilli e Ninnoli vari! Questi sono gli ingredienti di base necessari per migliorare le vostre armi.")),
	INTERMEZZO_INCANTATORE(classe -> new IntermezzoBenvenutoNegozio(classe, NegozioInScena.INCANTATORE,
			"Benvenuti nel mio laboratorio di incantamenti. Posso migliorare le vostre armi con quel pizzico di magia in più.")),
	INTERMEZZO_ACCAMPAMENTO(IntermezzoAccampamento::new),

	// Di ripiego: chi entra a ore impossibili, solo se all'ingresso non scatta nient'altro
	INTERMEZZO_NOTTE_ARMAIOLO(classe -> new IntermezzoNonChiudeteMai(classe, NegozioInScena.ARMAIOLO)),
	INTERMEZZO_NOTTE_ALCHIMISTA(classe -> new IntermezzoNonChiudeteMai(classe, NegozioInScena.ALCHIMISTA)),
	INTERMEZZO_NOTTE_VENDITORE_DI_PERGAMENE(classe -> new IntermezzoNonChiudeteMai(classe, NegozioInScena.VENDITORE_DI_PERGAMENE)),
	INTERMEZZO_NOTTE_INCANTATORE(classe -> new IntermezzoNonChiudeteMai(classe, NegozioInScena.INCANTATORE)),
	INTERMEZZO_NOTTE_LOCANDA(classe -> new IntermezzoNonChiudeteMai(classe, NegozioInScena.LOCANDA));

	// Riceve la classe stessa: un intermezzo può usarla come identificativo, cosa che una
	// costante dell'enum non può fare riferendosi a sé nel proprio inizializzatore
	private final Function<ClasseIntermezzo, Intermezzo> fabbrica;
	private final boolean diProva;

	ClasseIntermezzo(Supplier<Intermezzo> supplier) {
		this(classe -> supplier.get(), false);
	}

	ClasseIntermezzo(Supplier<Intermezzo> supplier, boolean diProva) {
		this(classe -> supplier.get(), diProva);
	}

	ClasseIntermezzo(Function<ClasseIntermezzo, Intermezzo> fabbrica) {
		this(fabbrica, false);
	}

	ClasseIntermezzo(Function<ClasseIntermezzo, Intermezzo> fabbrica, boolean diProva) {
		this.fabbrica = fabbrica;
		this.diProva = diProva;
	}

	/**
	 * Se l'intermezzo esiste solo in modalità di prova
	 */
	public boolean isDiProva() {
		return diProva;
	}

	public Intermezzo getIstanza() {
		return fabbrica.apply(this);
	}
}
