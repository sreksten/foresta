package com.threeamigos.foresta.tipi;

/**
 * Le fasi del gioco che il motore annuncia alla UI con InternoFaseDiGioco: quelle in cui la UI deve cambiare
 * schermata o animazione. Sono una piccola parte degli stati dell'Automa (che la UI non conosce).
 */
public enum FaseDiGioco {

	/**
	 * Il primissimo stato, solo all'avvio: la UI traccia il logo 3AM mentre carica le risorse
	 */
	LOGO_INIZIALE,
	/**
	 * La schermata introduttiva coi titoli: nuova partita o caricamento di una esistente
	 */
	INTRO,
	/**
	 * Si attende il nome del personaggio (vuoto per uno casuale)
	 */
	NOME_PERSONAGGIO,
	/**
	 * Si attende il sesso del personaggio
	 */
	SESSO_PERSONAGGIO,
	/**
	 * Si attende la classe del personaggio
	 */
	CLASSE_PERSONAGGIO,
	/**
	 * Si attende lo slot in cui scrivere il salvataggio (la schermata è aperta da RichiestaSelezioneSlotPerSalvataggio)
	 */
	SALVATAGGIO_DA_SCRIVERE

}
