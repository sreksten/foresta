package com.threeamigos.foresta.tipi;

import java.util.Optional;

/**
 * Il motivo per cui un certo tipo di personaggio ha il moltiplicatore che ha per un attributo, in una frase:
 * "Più piccolo di un umano e costretto ad arrangiarsi con equipaggiamento leggero" per il carico massimo del goblin.
 * <p>
 * Le frasi non stanno nel codice ma nel file {@code note_personaggi_<lingua>.properties} (vedi {@link Testi}),
 * una mappa (tipo di personaggio, attributo) → nota.
 * <p>
 * Le note sono per {@link TipoAttributo}, più {@link #DANNI_MAGICI} e {@link #DANNI_FISICI}, che sono moltiplicatori
 * dei danni e non attributi. Un tipo può usare le note di un altro tipo o di un gruppo (la voce
 * {@code TIPO.COME=ALTRO} del file: il capitano delle guardie ha quelle del guerriero, i non combattenti hanno le loro
 * in comune), e per chi non ha una nota propria vale la voce {@code PREDEFINITA.ATTRIBUTO}.
 *
 * @author Stefano Reksten
 */
public final class NoteDeiPersonaggi {

	/**
	 * Il moltiplicatore dei danni di un incantesimo, che non è un attributo.
	 */
	public static final String DANNI_MAGICI = "DANNI_MAGICI";

	/**
	 * Il moltiplicatore dei danni fisici, che non è un attributo.
	 */
	public static final String DANNI_FISICI = "DANNI_FISICI";

	private static final String FILE = "note_personaggi";
	private static final String COME = "COME";
	private static final String PREDEFINITA = "PREDEFINITA";
	// Un tipo che rimanda a un altro che rimanda a un altro...: oltre questo si smette (un giro nel file)
	private static final int RIMANDI_MASSIMI = 4;

	/**
	 * La nota di quel tipo di personaggio per quell'attributo, se c'è.
	 */
	public static Optional<String> nota(TipoPersonaggio tipo, TipoAttributo attributo) {
		return nota(tipo, attributo.name());
	}

	/**
	 * Come {@link #nota(TipoPersonaggio, TipoAttributo)}, per una chiave che può essere anche {@link #DANNI_MAGICI} o
	 * {@link #DANNI_FISICI}.
	 */
	public static Optional<String> nota(TipoPersonaggio tipo, String chiave) {
		String corrente = tipo.name();
		for (int rimandi = 0; rimandi <= RIMANDI_MASSIMI; rimandi++) {
			Optional<String> nota = Testi.get(FILE, corrente + "." + chiave);
			if (nota.isPresent()) {
				return nota;
			}
			Optional<String> come = Testi.get(FILE, corrente + "." + COME);
			if (!come.isPresent()) {
				break;
			}
			corrente = come.get();
		}
		return Testi.get(FILE, PREDEFINITA + "." + chiave);
	}

	private NoteDeiPersonaggi() {
	}

}
