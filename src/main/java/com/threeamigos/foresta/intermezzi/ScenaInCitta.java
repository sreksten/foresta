package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;

/**
 * La pagina di un intermezzo in una strada di città: il mandante di una missione aspetta sulla destra, il gruppo
 * arriva in fila come quando entra in un negozio (vedi {@link ScenaNegozio}) e, appena arrivato, cominciano le
 * battute. Il mandante è un personaggio ({@link #con}): un mandante senza personaggio proprio ha l'aspetto del locandiere ({@link #conMandante()}) o, se donna, della moglie del bardo ({@link #conMandantessa()}); oppure è il locandiere
 * ({@link #conLocandiere()}), l'armaiolo ({@link #conArmaiolo()}) o l'alchimista ({@link #conAlchimista()}), o è il capitano delle guardie
 * ({@link #conCapitano()}).
 * <pre>
 * ScenaInCitta.conMandante()
 *     .parlaIlMandante("Una banda di ladri mi ha rubato il medaglione!")
 *     .parlaIlCapo("Dove si nascondono?")
 *     .getPagine();
 * </pre>
 * Se la missione ha portato fin qui qualcuno (un ostaggio liberato, un colpevole catturato, un bardo da riportare a
 * casa), che è ancora tra gli ospiti del gruppo (la missione lo congeda alla fine), la scena lo mostra con la sua
 * classe: entra subito dopo il capo, prima degli altri, e guarda il mandante. Se è morto non c'è.
 */
public final class ScenaInCitta {

	private static final String SFONDO = "locazioni/Citta.gif";
	private static final String ID_MANDANTE = "mandante";
	// Le coordinate sono dello schermo e lo sfondo della città (390 × 320) è più piccolo di quelli dei negozi:
	// mandante e gruppo stanno sul selciato nelle stesse proporzioni dello sfondo, il mandante a destra
	private static final double X_MANDANTE = 0.62;
	private static final double Y_PERSONAGGI = 0.6;
	private static final double X_ARRIVO_CAPO = 0.5;
	private static final double RITARDO_FRA_PARTENZE = 0.6;

	private final TipoPersonaggio mandante;
	// Si costruisce alla prima battuta (o a getPagine), con il gruppo di quel momento
	private ScenaNegozio scena;

	private ScenaInCitta(TipoPersonaggio mandante) {
		this.mandante = mandante;
	}

	private ScenaNegozio scena() {
		if (scena == null) {
			scena = new ScenaNegozio(SFONDO, null, ID_MANDANTE,
					ElementoIntermezzo.personaggio(ID_MANDANTE, mandante, X_MANDANTE, Y_PERSONAGGI),
					Y_PERSONAGGI, X_ARRIVO_CAPO, RITARDO_FRA_PARTENZE);
		}
		return scena;
	}

	/**
	 * Un mandante di cui non c'è ancora il personaggio, se maschio: ha l'aspetto del locandiere.
	 */
	public static ScenaInCitta conMandante() {
		return con(TipoPersonaggio.LOCANDIERE);
	}

	/**
	 * Come {@link #conMandante()}, ma una donna: ha l'aspetto della moglie del bardo.
	 */
	public static ScenaInCitta conMandantessa() {
		return con(TipoPersonaggio.MOGLIE_DEL_BARDO);
	}

	/**
	 * Il mandante è il personaggio di quella classe.
	 */
	public static ScenaInCitta con(TipoPersonaggio classe) {
		return new ScenaInCitta(classe);
	}

	/**
	 * Il locandiere della città, fuori dalla sua locanda.
	 */
	public static ScenaInCitta conLocandiere() {
		return con(TipoPersonaggio.LOCANDIERE);
	}

	/**
	 * L'armaiolo della città, fuori dalla sua bottega.
	 */
	public static ScenaInCitta conArmaiolo() {
		return con(TipoPersonaggio.ARMAIOLO);
	}

	/**
	 * L'alchimista della città, fuori dalla sua bottega.
	 */
	public static ScenaInCitta conAlchimista() {
		return con(TipoPersonaggio.ALCHIMISTA);
	}

	/**
	 * Il capitano delle guardie della città.
	 */
	public static ScenaInCitta conCapitano() {
		return con(TipoPersonaggio.CAPITANO_DELLE_GUARDIE);
	}

	/**
	 * La moglie del bardo ubriaco, in città
	 */
	public static ScenaInCitta conMoglieDelBardo() {
		return con(TipoPersonaggio.MOGLIE_DEL_BARDO);
	}

	public ScenaInCitta parlaIlMandante(String testo) {
		scena().parlaIlNegoziante(testo);
		return this;
	}

	public ScenaInCitta parlaIlCapo(String testo) {
		scena().parlaIlCapo(testo);
		return this;
	}

	/**
	 * Una battuta dell'ospite, se la scena ne ha uno (un ospite del gruppo): per esempio il ringraziamento di un
	 * ostaggio liberato.
	 */
	public ScenaInCitta parlaLOspite(String testo) {
		scena().parlaLOspite(testo);
		return this;
	}

	public List<PaginaIntermezzo> getPagine() {
		return scena().getPagine();
	}
}
