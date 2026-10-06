package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;
import java.util.Objects;

/**
 * La pagina di un intermezzo in una strada di città: il mandante di una missione aspetta sulla destra, il gruppo
 * arriva in fila come quando entra in un negozio (vedi {@link ScenaNegozio}) e, appena arrivato, cominciano le
 * battute. Il mandante ha l'aspetto del locandiere ({@link #conMandante()}), oppure è il locandiere
 * ({@link #conLocandiere()}), l'armaiolo ({@link #conArmaiolo()}) o l'alchimista ({@link #conAlchimista()}), o è il capitano delle guardie, con l'aspetto del guerriero
 * ({@link #conCapitano()}).
 * <pre>
 * ScenaInCitta.conMandante()
 *     .parlaIlMandante("Una banda di ladri mi ha rubato il medaglione!")
 *     .parlaIlCapo("Dove si nascondono?")
 *     .getPagine();
 * </pre>
 * Se la missione ha portato fin qui qualcuno (un ostaggio liberato, un colpevole catturato, un bardo da riportare a
 * casa) lo si aggiunge con {@link #conOspite}, prima delle battute: entra subito dopo il capo e guarda il mandante.
 * <pre>
 * ScenaInCitta.conMoglieDelBardo()
 *     .conOspite(TipoPersonaggio.BARDO)
 *     .parlaIlMandante("Di nuovo in queste condizioni!")
 *     .getPagine();
 * </pre>
 */
public final class ScenaInCitta {

	private static final String SFONDO = "locazioni/Citta.gif";
	private static final String LOCANDIERE = "personaggi/Locandiere.gif";
	private static final String ARMAIOLO = "personaggi/Armaiolo.gif";
	private static final String ALCHIMISTA = "personaggi/Alchimista.gif";
	private static final String CAPITANO = "personaggi/Guerriero.gif";
	private static final String MOGLIE_DEL_BARDO = "personaggi/MoglieDelBardo.gif";
	private static final String ID_MANDANTE = "mandante";
	// Le coordinate sono dello schermo e lo sfondo della città (390 × 320) è più piccolo di quelli dei negozi:
	// mandante e gruppo stanno sul selciato nelle stesse proporzioni dello sfondo, il mandante a destra
	private static final double X_MANDANTE = 0.62;
	private static final double Y_PERSONAGGI = 0.6;
	private static final double X_ARRIVO_CAPO = 0.5;
	private static final double RITARDO_FRA_PARTENZE = 0.6;

	private final String immagineMandante;
	private TipoPersonaggio ospite;
	// Si costruisce alla prima battuta (o a getPagine), così conOspite può ancora dire chi c'è in scena
	private ScenaNegozio scena;

	private ScenaInCitta(String immagineMandante) {
		this.immagineMandante = immagineMandante;
	}

	private ScenaNegozio scena() {
		if (scena == null) {
			scena = new ScenaNegozio(SFONDO, null, ID_MANDANTE,
					ElementoIntermezzo.di(ID_MANDANTE, ImmagineIntermezzo.risorsa(immagineMandante), X_MANDANTE, Y_PERSONAGGI),
					Y_PERSONAGGI, X_ARRIVO_CAPO, RITARDO_FRA_PARTENZE, ospite);
		}
		return scena;
	}

	/**
	 * Chi la missione ha scortato o portato fin qui, che nella scena compare insieme al gruppo: entra subito dopo il
	 * capo e prima degli altri personaggi, e come loro cammina verso il mandante, quindi lo guarda. La missione lo ha
	 * già congedato dal gruppo (vedi MissioneAPassi.scorta) e la scena non lo trova: la classe va detta qui, e per
	 * un {@link com.threeamigos.foresta.personaggi.Viandante} è {@code TipoPersonaggio.VIANDANTE}. Si chiama prima
	 * delle battute.
	 *
	 * @throws IllegalStateException se la scena ha già cominciato (battute già scritte)
	 */
	public ScenaInCitta conOspite(TipoPersonaggio classe) {
		if (scena != null) {
			throw new IllegalStateException("L'ospite va detto prima delle battute");
		}
		this.ospite = Objects.requireNonNull(classe);
		return this;
	}

	/**
	 * Un mandante qualsiasi: per ora ha l'aspetto del locandiere.
	 */
	public static ScenaInCitta conMandante() {
		return new ScenaInCitta(LOCANDIERE);
	}

	/**
	 * Il locandiere della città, fuori dalla sua locanda.
	 */
	public static ScenaInCitta conLocandiere() {
		return new ScenaInCitta(LOCANDIERE);
	}

	/**
	 * L'armaiolo della città, fuori dalla sua bottega.
	 */
	public static ScenaInCitta conArmaiolo() {
		return new ScenaInCitta(ARMAIOLO);
	}

	/**
	 * L'alchimista della città, fuori dalla sua bottega.
	 */
	public static ScenaInCitta conAlchimista() {
		return new ScenaInCitta(ALCHIMISTA);
	}

	/**
	 * Il capitano delle guardie della città: per ora ha l'aspetto del guerriero.
	 */
	public static ScenaInCitta conCapitano() {
		return new ScenaInCitta(CAPITANO);
	}

	/**
	 * La moglie del bardo ubriaco, in città
	 */
	public static ScenaInCitta conMoglieDelBardo() {
		return new ScenaInCitta(MOGLIE_DEL_BARDO);
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
	 * Una battuta dell'ospite, se la scena ne ha uno (vedi {@link #conOspite}): per esempio il ringraziamento di un
	 * ostaggio liberato.
	 */
	public ScenaInCitta parlaLOspite(String testo) {
		if (ospite == null) {
			throw new IllegalStateException("Nessun ospite in scena: vedi conOspite");
		}
		scena().parlaLOspite(testo);
		return this;
	}

	public List<PaginaIntermezzo> getPagine() {
		return scena().getPagine();
	}
}
