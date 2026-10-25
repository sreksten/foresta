package com.threeamigos.foresta.motore;

/**
 * Quanta esperienza dà al gruppo un fatto del gioco, in percentuale dei punti che separano il livello del mondo dal successivo (vedi
 * {@link GestoreProgressione}).
 */
public final class EsperienzaDelGruppo {

	private EsperienzaDelGruppo() {
	}

	/**
	 * Un artefatto dà il 5% dei punti necessari per arrivare al livello successivo
	 */
	public static void acquisisciArtefattoMinore() {
		aggiungiPercentuale(5);
	}

	/**
	 * Una missione secondaria dà il 20% dei punti necessari per arrivare al livello successivo
	 */
	public static void completaMissioneSecondaria() {
		aggiungiPercentuale(20);
	}

	/**
	 * Una missione principale dà il 50% dei punti necessari per arrivare al livello successivo
	 */
	public static void completaMissionePrincipale() {
		aggiungiPercentuale(50);
	}

	private static void aggiungiPercentuale(int percentuale) {
		// La percentuale e' degli XP che separano il livello attuale dal successivo (non del livello)
		GruppoGiocatore.getIstanza().addPuntiEsperienza(GestoreProgressione.getXpRichiestiPerProssimoLivello(Statistiche.getLivello()) * percentuale / 100);
	}
}
