package com.threeamigos.foresta.intermezzi;

import java.util.List;

/**
 * La pagina di un intermezzo dentro una locanda: il locandiere aspetta dietro il bancone, il gruppo arriva in fila
 * come quando entra in un negozio (vedi {@link ScenaNegozio}) e, appena arrivato, cominciano le battute. Come
 * {@link ScenaInCitta}, ma per le missioni che nascono in una locanda. Al posto del locandiere può esserci un
 * avventore che chiede qualcosa al gruppo: un sacerdote o una sacerdotessa ({@link #conSacerdote}).
 */
public final class ScenaInLocanda {

	private static final String SFONDO = "fondi/InternoLocanda.gif";
	private static final String PRIMO_PIANO = "fondi/ForegroundLocanda.gif";
	private static final String LOCANDIERE = "personaggi/Locandiere.gif";
	// Per ora un sacerdote ha l'aspetto del mago, una sacerdotessa quello della maga
	private static final String SACERDOTE = "personaggi/Mago.gif";
	private static final String SACERDOTESSA = "personaggi/Maga.gif";
	private static final String ID_LOCANDIERE = "locandiere";
	// Come la scenetta d'ingresso nella locanda (vedi NegozioInScena.LOCANDA)
	private static final double X_LOCANDIERE = 0.65;
	private static final double Y_PERSONAGGI = 0.6;
	private static final double X_ARRIVO_CAPO = 0.5;
	private static final double RITARDO_FRA_PARTENZE = 0.6;

	private final ScenaNegozio scena;

	private ScenaInLocanda(String immagine) {
		scena = new ScenaNegozio(SFONDO, PRIMO_PIANO, ID_LOCANDIERE,
				ElementoIntermezzo.di(ID_LOCANDIERE, ImmagineIntermezzo.risorsa(immagine), X_LOCANDIERE, Y_PERSONAGGI),
				Y_PERSONAGGI, X_ARRIVO_CAPO, RITARDO_FRA_PARTENZE);
	}

	public static ScenaInLocanda conLocandiere() {
		return new ScenaInLocanda(LOCANDIERE);
	}

	/**
	 * Un sacerdote, o una sacerdotessa, seduto nella locanda: parla con {@link #parlaIlLocandiere}, al suo posto.
	 */
	public static ScenaInLocanda conSacerdote(boolean sacerdotessa) {
		return new ScenaInLocanda(sacerdotessa ? SACERDOTESSA : SACERDOTE);
	}

	public ScenaInLocanda parlaIlLocandiere(String testo) {
		scena.parlaIlNegoziante(testo);
		return this;
	}

	public ScenaInLocanda parlaIlCapo(String testo) {
		scena.parlaIlCapo(testo);
		return this;
	}

	public List<PaginaIntermezzo> getPagine() {
		return scena.getPagine();
	}
}
