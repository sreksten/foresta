package com.threeamigos.foresta.intermezzi;

import java.util.List;

/**
 * La pagina di un intermezzo in una strada di città: il mandante di una missione aspetta sulla destra, il gruppo
 * arriva in fila come quando entra in un negozio (vedi {@link ScenaNegozio}) e, appena arrivato, cominciano le
 * battute. Per ora il mandante ha l'aspetto del locandiere.
 * <pre>
 * ScenaInCitta.conMandante()
 *     .parlaIlMandante("Una banda di ladri mi ha rubato il medaglione!")
 *     .parlaIlCapo("Dove si nascondono?")
 *     .getPagine();
 * </pre>
 */
public final class ScenaInCitta {

	private static final String SFONDO = "locazioni/Citta.gif";
	private static final String MANDANTE = "personaggi/Locandiere.gif";
	private static final String ID_MANDANTE = "mandante";
	// Le coordinate sono dello schermo e lo sfondo della città (390 × 320) è più piccolo di quelli dei negozi:
	// mandante e gruppo stanno sul selciato nelle stesse proporzioni dello sfondo, il mandante a destra
	private static final double X_MANDANTE = 0.62;
	private static final double Y_PERSONAGGI = 0.6;
	private static final double X_ARRIVO_CAPO = 0.5;
	private static final double RITARDO_FRA_PARTENZE = 0.6;

	private final ScenaNegozio scena;

	private ScenaInCitta() {
		scena = new ScenaNegozio(SFONDO, null, ID_MANDANTE,
				ElementoIntermezzo.di(ID_MANDANTE, ImmagineIntermezzo.risorsa(MANDANTE), X_MANDANTE, Y_PERSONAGGI),
				Y_PERSONAGGI, X_ARRIVO_CAPO, RITARDO_FRA_PARTENZE);
	}

	public static ScenaInCitta conMandante() {
		return new ScenaInCitta();
	}

	public ScenaInCitta parlaIlMandante(String testo) {
		scena.parlaIlNegoziante(testo);
		return this;
	}

	public ScenaInCitta parlaIlCapo(String testo) {
		scena.parlaIlCapo(testo);
		return this;
	}

	public List<PaginaIntermezzo> getPagine() {
		return scena.getPagine();
	}
}
