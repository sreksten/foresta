package com.threeamigos.foresta.intermezzi;

/**
 * I locali in cui il gruppo entra con una scenetta: i negozi di città e le locande.
 * Per ognuno lo sfondo, il negoziante e dove si mette, dove si ferma il gruppo, a che ora
 * il locale dovrebbe chiudere e quando ci si entra (vedi {@link #isIngresso}).
 */
enum NegozioInScena {

	ARMAIOLO(MomentoIntermezzo.INGRESSO_ARMAIOLO, "armaiolo", "armaioli",
			"fondi/InternoArmaiolo.gif", "fondi/ForegroundArmaiolo.gif", "personaggi/Armaiolo.gif",
			0.65, 0.6, 0.55, 0.8, 20),
	ALCHIMISTA(MomentoIntermezzo.INGRESSO_ALCHIMISTA, "alchimista", "alchimisti",
			"fondi/InternoAlchimista.gif", "fondi/ForegroundAlchimista.gif", "personaggi/Alchimista.gif",
			0.60, 0.55, 0.51, 0.8, 20),
	VENDITORE_DI_PERGAMENE(MomentoIntermezzo.INGRESSO_VENDITORE_DI_PERGAMENE, "venditore", "venditori di pergamene",
			"fondi/InternoVenditoreDiPergamene.gif", "fondi/ForegroundVenditoreDiPergamene.gif", "personaggi/VenditoreDiPergamene.gif",
			0.65, 0.6, 0.55, 0.8, 20),
	INCANTATORE(MomentoIntermezzo.INGRESSO_INCANTATORE, "incantatore", "incantatori",
			"fondi/InternoIncantatore.gif", "fondi/ForegroundIncantatore.gif", "personaggi/Incantatore.gif",
			0.65, 0.6, 0.55, 0.8, 20),
	// Nel bosco o in città: il momento d'ingresso dipende da dove si trova la locanda (vedi isIngresso)
	LOCANDA(null, "locandiere", "locandieri",
			"fondi/InternoLocanda.gif", "fondi/ForegroundLocanda.gif", "personaggi/Locandiere.gif",
			0.65, 0.6, 0.5, 0.6, 23);

	private final MomentoIntermezzo ingresso;
	private final String idNegoziante;
	private final String negozianti;
	private final String sfondo;
	private final String primoPiano;
	private final String immagineNegoziante;
	private final double xNegoziante;
	private final double yNegoziante;
	private final double xArrivoCapo;
	private final double ritardoFraPartenze;
	private final int oraDiChiusura;

	NegozioInScena(MomentoIntermezzo ingresso, String idNegoziante, String negozianti,
				   String sfondo, String primoPiano, String immagineNegoziante,
				   double xNegoziante, double yNegoziante, double xArrivoCapo, double ritardoFraPartenze, int oraDiChiusura) {
		this.ingresso = ingresso;
		this.idNegoziante = idNegoziante;
		this.negozianti = negozianti;
		this.sfondo = sfondo;
		this.primoPiano = primoPiano;
		this.immagineNegoziante = immagineNegoziante;
		this.xNegoziante = xNegoziante;
		this.yNegoziante = yNegoziante;
		this.xArrivoCapo = xArrivoCapo;
		this.ritardoFraPartenze = ritardoFraPartenze;
		this.oraDiChiusura = oraDiChiusura;
	}

	/**
	 * Se nel momento indicato il gruppo sta entrando in questo locale.
	 */
	boolean isIngresso(MomentoIntermezzo momento) {
		if (this == LOCANDA) {
			return Locande.momentoCoerenteConLocazioneCorrente(momento);
		}
		return momento == ingresso;
	}

	/**
	 * Il plurale del negoziante, per chiedergli se quelli come lui non chiudono mai.
	 */
	String getNegozianti() {
		return negozianti;
	}

	/**
	 * L'ora dopo la quale il locale dovrebbe essere chiuso.
	 */
	int getOraDiChiusura() {
		return oraDiChiusura;
	}

	/**
	 * La scena di questo locale: il negoziante al suo posto e il gruppo che entra in fila, capo in testa.
	 */
	ScenaNegozio entraIlGruppo() {
		return new ScenaNegozio(sfondo, primoPiano, idNegoziante,
				ElementoIntermezzo.di(idNegoziante, ImmagineIntermezzo.risorsa(immagineNegoziante), xNegoziante, yNegoziante),
				yNegoziante, xArrivoCapo, ritardoFraPartenze);
	}
}
