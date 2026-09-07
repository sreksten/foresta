package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.motore.ComandiPossibili;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.personaggi.Personaggio;
import java.util.concurrent.CountDownLatch;

public class UI {

	private UI() {
	}

	private static InterfacciaUtente interfacciaUtente;
	private static final CountDownLatch interfacciaUtentePronta = new CountDownLatch(1);

	public static void impostaInterfacciaUtente(InterfacciaUtente userInterface) {
		interfacciaUtente = userInterface;
	}

	public static void setInterfacciaUtentePronta() {
		interfacciaUtentePronta.countDown();
	}

	public static void aspettaInterfacciaUtentePronta() throws InterruptedException {
		interfacciaUtentePronta.await();
	}

	/**
	 * Ripulisce eventuali dati da partite precedenti
	 */
	public static void reinizializza() {
		interfacciaUtente.reinizializza();
	}

	/**
	 * Richiama la schermata o animazione di introduzione
	 */
	public static void intro() {
		interfacciaUtente.intro();
	}

	/**
	 * Richiama la schermata di selezione nuovo gioco o caricamento di un salvataggio
	 */
	public static void nuovoGiocoOCaricaPrecedente() {
		interfacciaUtente.nuovoGiocoOCaricaPrecedente();
	}

	/**
	 * Richiama la schermata di selezione salvataggio
	 */
	public static void selezioneSlotSalvataggioDaCaricare() {
		interfacciaUtente.selezioneSlotSalvataggioDaCaricare();
	}

	/**
	 * Va alla schermata di gioco vera e propria
	 */
	public static void mostraSchermataGioco() {
		interfacciaUtente.mostraSchermataGioco();
	}

	/**
	 * Mostra la mappa di gioco conosciuta
	 */
	public static void mappa() {
		interfacciaUtente.mappa();
	}

	/**
	 * Centra la mappa dopo un eventuale spostamento se non ci stava tutta a schermo
	 */
	public static void centraMappa() {
		interfacciaUtente.centraMappa();
	}

	/**
	 * Muove la visuale della mappa di gioco conosciuta su ricezione di
	 * Comando.(NORD|SUD|EST|OVEST)
	 */
	public static void muoviMappa(Comando direzione) {
		interfacciaUtente.muoviMappa(direzione);
	}

	/**
	 * Richiama la schermata di selezione salvataggio
	 */
	public static void selezioneSlotSalvataggioDaSalvare() {
		interfacciaUtente.selezioneSlotSalvataggioDaSalvare();
	}

	/**
	 * Richiede se si vuole uscire dal gioco
	 */
	public static void confermaUscita() {
		interfacciaUtente.confermaUscita();
	}
	
	/**
	 * Richiama la schermata o animazione di sconfitta
	 */
	public static void perso() {
		interfacciaUtente.perso();
	}

	/**
	 * Richiama la schermata o animazione di vittoria
	 */
	public static void vinto() {
		interfacciaUtente.vinto();
	}

	/**
	 * Richiama la schermata o animazione che mostra le statistiche sui mostri
	 * uccisi eccetera.
	 */
	public static void statistiche() {
		interfacciaUtente.statistiche();
	}

	/**
	 * Richiama la schermata o animazione che riporta la tabella dei punteggi
	 */
	public static void punteggi() {
		interfacciaUtente.punteggi();
	}

	/**
	 * Mostra un messaggio di comunicazione prima/dopo il gioco come ad esempio la
	 * richiesta di inserire il nome del giocatore
	 */
	public static void scriviGrande(String messaggio) {
		interfacciaUtente.scriviGrande(messaggio);
	}

	/**
	 * Mostra un messaggio di gioco
	 */
	public static void notifica(String messaggio) {
		interfacciaUtente.notifica(messaggio);
	}

	/**
	 * Porta in primo piano una finestra di gioco (UI.FINESTRA_...)
	 */
	public static void primoPiano(InterfacciaUtente.Finestra finestra) {
		interfacciaUtente.primoPiano(finestra);
	}

	/**
	 * Mostra la richiesta di un testo da parte di un controllore di gioco
	 */
	public static void chiediTesto() {
		interfacciaUtente.chiediTesto();
	}

	/**
	 * Inoltra un testo ricevuto a un controllore di gioco
	 */
	public static void riceviTesto(String s) {
		interfacciaUtente.riceviTesto(s);
	}

	/**
	 * Propone al giocatore una serie di possibili azioni tra le quali scegliere per
	 * poter continuare il gioco. L'elenco delle possibili azioni viene riempito con valori
	 * provenienti dalla classe Comando
	 */
	public static void impostaAzioni() {
		interfacciaUtente.impostaAzioni();
	}

	/**
	 * Scorciatoia per impostare i comandi e modificare subito l'interfaccia utente
	 */
	public static void impostaAzioni(Comando... comandi) {
		ComandiPossibili.set(comandi);
		impostaAzioni();
	}

	public static void preparaLocazione() {
		interfacciaUtente.preparaLocazione();
	}

	/**
	 * Mostra la forza in combattimento.
	 */
	public static void infoCombattimento(boolean mostra, Personaggio combattente, Personaggio avversario) {
		interfacciaUtente.infoCombattimento(mostra, combattente, avversario);
	}

	public static void notificaMorte(Personaggio personaggio) {
		interfacciaUtente.notificaMorte(personaggio);
	}

	public static void notificaMissione(String etichetta, String nomeMissione) {
		interfacciaUtente.notificaMissione(etichetta, nomeMissione);
	}
	
	public static void variaSalute(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaSalute(personaggio, variazione);
	}

	public static void variaSaluteMassima(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaSaluteMassima(personaggio, variazione);
	}

	public static void variaMagia(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaMagia(personaggio, variazione);
	}
	
	public static void variaMagiaMassima(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaMagiaMassima(personaggio, variazione);
	}

	public static void variaLivello(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaLivello(personaggio, variazione);
	}

	public static void variaCoraggio(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaCoraggio(personaggio, variazione);
	}
	
	public static void variaValore(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaValore(personaggio, variazione);
	}
	
	public static void variaCarisma(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaCarisma(personaggio, variazione);
	}
	
	public static void variaStanchezza(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaStanchezza(personaggio, variazione);
	}

	public static void variaTempo(Personaggio personaggio, int variazione) {
		interfacciaUtente.variaTempo(personaggio, variazione);
	}

	public static void variaGemme(int variazione) {
		interfacciaUtente.variaGemme(variazione);
	}

	public static void variaMonete(int variazione) {
		interfacciaUtente.variaMonete(variazione);
	}

	public static void variaPunti(int variazione) {
		interfacciaUtente.variaPunti(variazione);
	}

	public static void variaIncantesimi(ClassiIncantesimo classeIncantesimo, int variazione) {
		interfacciaUtente.variaIncantesimi(classeIncantesimo, variazione);
	}

	public static void variaPozioniSalute(int variazione) {
		interfacciaUtente.variaPozioniSalute(variazione);
	}

	public static void variaPozioniSaluteGrande(int variazione) {
		interfacciaUtente.variaPozioniSaluteGrande(variazione);
	}

	public static void variaPozioniMagia(int variazione) {
		interfacciaUtente.variaPozioniMagia(variazione);
	}

	public static void variaPozioniMagiaGrande(int variazione) {
		interfacciaUtente.variaPozioniMagiaGrande(variazione);
	}

	public static void variaMappa() {
		interfacciaUtente.variaMappa();
	}

	public static void raccogliOggetto() {
		interfacciaUtente.raccogliOggetto();
	}

	/**
	 * Rinfresca l'interfaccia utente (ad esempio dopo aver richiesto un primo piano
	 * di una qualche finestra)
	 */
	public static void rinfresca() {
		interfacciaUtente.rinfresca();
	}
}
