package com.threeamigos.foresta.artefatti;

/**
 * Che cosa farà perdere una fusione (vedi {@link RegoleIncantatura#avvisoEffettiPersi}): è un fatto, non una frase. Come dirlo, e in che
 * lingua, lo decide chi lo mostra.
 * <p>
 * O la fusione non cambierebbe niente ({@link #isNessunMiglioramento()}), oppure qualche effetto degli ingredienti non passa (la
 * {@link Perdita} e quanti ne passano) e/o qualche effetto dell'artefatto viene scalzato da uno migliore.
 *
 * @author Stefano Reksten
 */
public final class AvvisoDiFusione {

	/**
	 * Perché qualche effetto degli ingredienti non passa sull'artefatto.
	 */
	public enum Perdita {
		/**
		 * L'artefatto tiene i suoi migliori effetti, nel limite di quanti ne può avere
		 */
		LIMITE_DI_EFFETTI,
		/**
		 * I modificatori sul bersaglio si fondono solo sulle armi
		 */
		MODIFICATORI_SUL_BERSAGLIO_SOLO_SULLE_ARMI,
		/**
		 * Sul libro magico gli incantamenti elementali non hanno effetto
		 */
		INCANTAMENTI_ELEMENTALI_NON_SUL_LIBRO,
		/**
		 * Il potere magico si fonde solo su bastoni e libri magici
		 */
		POTERE_MAGICO_SOLO_SU_BASTONI_E_LIBRI
	}

	private final boolean nessunMiglioramento;
	private final Perdita perdita;
	private final int effettiCheEntrano;
	private final int effettiScalzati;

	private AvvisoDiFusione(boolean nessunMiglioramento, Perdita perdita, int effettiCheEntrano, int effettiScalzati) {
		this.nessunMiglioramento = nessunMiglioramento;
		this.perdita = perdita;
		this.effettiCheEntrano = effettiCheEntrano;
		this.effettiScalzati = effettiScalzati;
	}

	/**
	 * Nessun effetto è migliore di quelli dell'artefatto: la fusione non cambierebbe niente.
	 */
	static AvvisoDiFusione nessunMiglioramento() {
		return new AvvisoDiFusione(true, null, 0, 0);
	}

	/**
	 * Una fusione che perde qualcosa.
	 *
	 * @param perdita           perché degli effetti degli ingredienti non passano; null se passano tutti
	 * @param effettiCheEntrano quanti effetti degli ingredienti passano sull'artefatto
	 * @param effettiScalzati   quanti effetti dell'artefatto escono perché ne entrano di migliori
	 */
	static AvvisoDiFusione di(Perdita perdita, int effettiCheEntrano, int effettiScalzati) {
		return new AvvisoDiFusione(false, perdita, effettiCheEntrano, effettiScalzati);
	}

	/**
	 * Se la fusione non cambierebbe niente perché nessun effetto è migliore di quelli dell'artefatto.
	 */
	public boolean isNessunMiglioramento() {
		return nessunMiglioramento;
	}

	/**
	 * Perché degli effetti degli ingredienti non passano, o null se passano tutti.
	 */
	public Perdita getPerdita() {
		return perdita;
	}

	/**
	 * Quanti effetti degli ingredienti passano sull'artefatto.
	 */
	public int getEffettiCheEntrano() {
		return effettiCheEntrano;
	}

	/**
	 * Quanti effetti dell'artefatto escono perché ne entrano di migliori.
	 */
	public int getEffettiScalzati() {
		return effettiScalzati;
	}
}
