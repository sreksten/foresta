package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.Azioni;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.personaggi.Personaggio;

public class UI {

	private UI() {
	}

	private static InterfacciaUtente interfacciaUtente;
	private static boolean interfacciaUtentePronta;

	public static final void impostaInterfacciaUtente(InterfacciaUtente userInterface) {
		interfacciaUtente = userInterface;
	}

	public static final void setInterfacciaUtentePronta() {
		interfacciaUtentePronta = true;
	}

	public static final boolean isInterfacciaUtentePronta() {
		return interfacciaUtentePronta;
	}

	/**
	 * Ripulisce eventuali dati da partite precedenti
	 */
	public static final void reinizializza() {
		interfacciaUtente.reinizializza();
	}

	/**
	 * Richiama la schermata o animazione di introduzione
	 */
	public static final void intro() {
		interfacciaUtente.intro();
	}

	/**
	 * Richiama la schermata di selezione nuovo gioco o caricamento di un salvataggio
	 */
	public static final void nuovoGiocoOCaricaPrecedente() {
		interfacciaUtente.nuovoGiocoOCaricaPrecedente();
	}

	/**
	 * Richiama la schermata di selezione salvataggio
	 */
	public static final void selezioneSlotSalvataggioDaCaricare() {
		interfacciaUtente.selezioneSlotSalvataggioDaCaricare();
	}

	/**
	 * Mostra la mappa di gioco conosciuta
	 */
	public static final void mappa() {
		interfacciaUtente.mappa();
	}

	/**
	 * Centra la mappa dopo un eventuale spostamento se non ci stava tutta a schermo
	 */
	public static final void centraMappa() {
		interfacciaUtente.centraMappa();
	}

	/**
	 * Muove la visuale della mappa di gioco conosciuta su ricezione di
	 * Comando.(NORD|SUD|EST|OVEST)
	 */
	public static final void muoviMappa(Comando direzione) {
		interfacciaUtente.muoviMappa(direzione);
	}

	/**
	 * Richiama la schermata di selezione salvataggio
	 */
	public static final void selezioneSlotSalvataggioDaSalvare() {
		interfacciaUtente.selezioneSlotSalvataggioDaSalvare();
	}

	/**
	 * Richiede se si vuole uscire dal gioco
	 */
	public static final void confermaUscita() {
		interfacciaUtente.confermaUscita();
	}
	
	/**
	 * Richiama la schermata o animazione di sconfitta
	 */
	public static final void perso() {
		interfacciaUtente.perso();
	}

	/**
	 * Richiama la schermata o animazione di vittoria
	 */
	public static final void vinto() {
		interfacciaUtente.vinto();
	}

	/**
	 * Richiama la schermata o animazione che mostra le statistiche sui mostri
	 * uccisi eccetera.
	 */
	public static final void statistiche() {
		interfacciaUtente.statistiche();
	}

	/**
	 * Richiama la schermata o animazione che riporta la tabella dei punteggi
	 */
	public static final void hiscore() {
		interfacciaUtente.hiscore();
	}

	/**
	 * Mostra un messaggio di comunicazione prima/dopo il gioco come ad esempio la
	 * richiesta di inserire il nome del giocatore
	 */
	public static final void scriviGrande(String messaggio) {
		interfacciaUtente.scriviGrande(messaggio);
	}

	/**
	 * Mostra un messaggio di gioco
	 */
	public static final void notifica(String messaggio) {
		interfacciaUtente.notifica(messaggio);
	}

	/**
	 * Porta in primo piano una finestra di gioco (UI.FINESTRA_...)
	 */
	public static final void primoPiano(InterfacciaUtente.Finestra finestra) {
		interfacciaUtente.primoPiano(finestra);
	}

	/**
	 * Mostra la richiesta di un testo da parte di un controllore di gioco
	 */
	public static final void chiediTesto() {
		interfacciaUtente.chiediTesto();
	}

	/**
	 * Inoltra un testo ricevuto ad un controllore di gioco
	 */
	public static final void riceviTesto(String s) {
		interfacciaUtente.riceviTesto(s);
	}

	/**
	 * Propone al giocatore una serie di possibili azioni tra le quali scegliere per
	 * poter continuare il gioco. L'array azioni[] viene riempito con valori
	 * provenienti dalla classe Comando
	 */
	public static final void impostaAzioni() {
		interfacciaUtente.impostaAzioni();
	}

	/**
	 * Scorciatoia per impostare i comandi e modificare subito l'interfaccia utente
	 * @param comandi
	 */
	public static final void impostaAzioni(Comando ... comandi) {
		Azioni.set(comandi);
		impostaAzioni();
	}

	public static final void preparaLocazione() {
		interfacciaUtente.preparaLocazione();
	}

	/**
	 * Mostra la forza in combattimento.
	 */
	public static final void infoCombattimento(boolean mostra, Personaggio combattente, Personaggio avversario) {
		interfacciaUtente.infoCombattimento(mostra, combattente, avversario);
	}

	public static final void notificaMorte(Personaggio personaggio) {
		interfacciaUtente.notificaMorte(personaggio);
	}
	
	public static final void variaForza(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaForza(personaggio, variazione);
	}

	public static final void variaForzaMassima(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaForzaMassima(personaggio, variazione);
	}

	public static final void variaMagia(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaMagia(personaggio, variazione);
	}
	
	public static final void variaMagiaMassima(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaMagiaMassima(personaggio, variazione);
	}
	
	public static final void variaCoraggio(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaCoraggio(personaggio, variazione);
	}
	
	public static final void variaValore(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaValore(personaggio, variazione);
	}
	
	public static final void variaCarisma(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaCarisma(personaggio, variazione);
	}
	
	public static final void variaStanchezza(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaStanchezza(personaggio, variazione);
	}

	public static final void variaTempo(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaTempo(personaggio, variazione);
	}

	public static final void variaGemme(int variazione) {
		interfacciaUtente.variaGemme(variazione);
	}

	public static final void variaMonete(int variazione) {
		interfacciaUtente.variaMonete(variazione);
	}

	public static final void variaPunti(int variazione) {
		interfacciaUtente.variaPunti(variazione);
	}

	public static final void variaIncantesimi(ClassiIncantesimo classeIncantesimo, int variazione) {
		interfacciaUtente.variaIncantesimi(classeIncantesimo, variazione);
	}

	public static final void variaPozioniForza(int variazione) {
		interfacciaUtente.variaPozioniForza(variazione);
	}

	public static final void variaPozioniMagia(int variazione) {
		interfacciaUtente.variaPozioniMagia(variazione);
	}

	public static final void variaPozioniGrandeForza(int variazione) {
		interfacciaUtente.variaPozioniForza(variazione);
	}

	public static final void variaMappa() {
		interfacciaUtente.variaMappa();
	}

	public static final void raccogliOggetto() {
		interfacciaUtente.raccogliOggetto();
	}

	/**
	 * Rinfresca l'interfaccia utente (ad esempio dopo aver richiesto un primo piano
	 * di una qualche finestra)
	 */
	public static final void rinfresca() {
		interfacciaUtente.rinfresca();
	}
}
