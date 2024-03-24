package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ForestaMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Random;

/**
 * Contiene la mappa di una istanza della Foresta,
 * l'elenco dei Cacciatori di Draghi tuttora disponibili,
 * varie ed eventuali
 */

public class Foresta {

	private Foresta() {
	}

	private static ForestaMD forestaMD = ModelloDati.getIstanza().getForestaMD();
	
	public static final int getDimensioneX() {
		return forestaMD.getDimensioneX();
	}
	
	public static final int getDimensioneY() {
		return forestaMD.getDimensioneY();
	}
	
	public static final void impostaLocazioneCorrente(ClassiLocazione classeLocazione) {
		forestaMD.impostaLocazione(GruppoGiocatore.getIstanza().getCoordinate(), classeLocazione);
	}

	public static final CoordinateMD getCoordinateLocazioneUnica(ClassiLocazione classeLocazione) {
		if (!classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException();
		}
		return forestaMD.ottieniCoordinateLocazioneUnica(classeLocazione);
	}
	
	public static final void distruggiLocazioneUnica(ClassiLocazione classeLocazione, ClassiLocazione nuovaClasseLocazione) {
		CoordinateMD coordinate = getCoordinateLocazioneUnica(classeLocazione);
		if (coordinate != null) {
			forestaMD.impostaLocazione(coordinate, nuovaClasseLocazione);
			forestaMD.rimuoviLocazioneUnica(classeLocazione);
		}
	}

	public static ClassiLocazione getLocazione(CoordinateMD coordinate) {
		return forestaMD.ottieniCasseLocazione(coordinate);
	}
	
	public static ClassiLocazione getLocazione(int x, int y) {
		return forestaMD.ottieniClasseLocazione(x, y);
	}
	
	public static final CoordinateMD costruisciLocazioneUnica(ClassiLocazione classeLocazioneUnica, boolean conosciutaSuMappa) {
		if (!classeLocazioneUnica.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazione per istanziare " + classeLocazioneUnica.name());
		}
		Logger.log("Costruzione di " + classeLocazioneUnica);
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, classeLocazioneUnica);
		forestaMD.aggiungiLocazioneUnica(classeLocazioneUnica, coordinate);
		if (conosciutaSuMappa) {
			setLocazioneConosciuta(coordinate);
		}
		return coordinate;
	}

	/**
	 * Per uso da parte di un ControlloreDiGioco
	 */
	static final void reimposta() {
		
		RegistroPersonaggi.reimposta();
		LineaTemporale.reimposta();
		RegistroMissioni.reimposta();
		RegistroArtefatti.reimposta();
		ModelloDati.getIstanza().getStatisticheMD().reimposta();
		
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			classeLocazione.getIstanza().reimposta();
		}
		
		//TODO quando tutti i modelli dati sono stati creati spostare reimposta su ModelloDati
		final int dimensioneX = 20;
		final int dimensioneY = 20;
		forestaMD.reimposta(dimensioneX, dimensioneY);

		costruisciCittaEPosizionaPersonaggi();
		costruisciCastelli();		
		costruisciLocandeEPosizionaPersonaggi();
		costruisciTempliEPosizionaArtefatti();
		
		int media = (getDimensioneX() + getDimensioneY()) / 2;

		costruisci(ClassiLocazione.GROTTA, media >> 1);
		costruisci(ClassiLocazione.PALUDE, media);
		costruisci(ClassiLocazione.ROVINE, media);

		GruppoGiocatore.getIstanza().reimposta();
		GruppoAvversario.getIstanza().reimposta();

		for (int x = 0; x < getDimensioneX(); x++) {
			for (int y = 0; y < getDimensioneY(); y++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (getLocazione(coordinate) == null) {
					setLocazione(coordinate, ClassiLocazione.BOSCO);
				}
			}
		}
	}

	private static final void setLocazione(CoordinateMD coordinate, ClassiLocazione classeLocazione) {
		forestaMD.impostaClasseLocazione(coordinate, classeLocazione);
	}
	
	/**
	 * Costruisce le città e ci piazza un personaggio a caso
	 */
	private static final void costruisciCittaEPosizionaPersonaggi() {
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CITTA) {
				CoordinateMD coordinate = costruisciLocazioneUnica(classeLocazione, false);
				Personaggio personaggioDisponibile = RegistroPersonaggi.getPersonaggioDisponibile();
				if (personaggioDisponibile != null) {
					RegistroPersonaggi.addPersonaggioInLocazione(personaggioDisponibile, coordinate);
				}
			}
		}
	}
	
	/**
	 * Costruisce tutti i castelli tranne quello del Drago che appare solo dopo aver distrutto tutti gli altri
	 */
	private static final void costruisciCastelli() {
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CASTELLO && classeLocazione != ClassiLocazione.CASTELLO_DRAGO) {
				costruisciLocazioneUnica(classeLocazione, false);
			}
		}
	}
	
	/**
	 * Costruisce le locande e piazza i rimanenti personaggi disponibili
	 */
	private static final void costruisciLocandeEPosizionaPersonaggi() {
		int locandeCostruite = 0;
		Personaggio personaggioDisponibile = null;
		while ((personaggioDisponibile = RegistroPersonaggi.getPersonaggioDisponibile()) != null) {
			costruisci(ClassiLocazione.LOCANDA, personaggioDisponibile);
			locandeCostruite++;
		}
		int media = (getDimensioneX() + getDimensioneY()) >> 2;
		if (media > locandeCostruite) {
			costruisci(ClassiLocazione.LOCANDA, media - locandeCostruite);
		}
	}

	/**
	 * Costruisce le locande e piazza i rimanenti personaggi disponibili
	 */
	private static final void costruisciTempliEPosizionaArtefatti() {
		int templiCostruiti = 0;
		Artefatto artefattoDisponibile = null;
		while ((artefattoDisponibile = RegistroArtefatti.getArtefattoDisponibile()) != null) {
			costruisci(ClassiLocazione.TEMPIO, artefattoDisponibile);
			templiCostruiti++;
		}
		int media = (getDimensioneX() + getDimensioneY()) >> 2;
		if (media > templiCostruiti) {
			costruisci(ClassiLocazione.TEMPIO, media - templiCostruiti);
		}
	}

	private static final void costruisci(ClassiLocazione classeLocazione, Personaggio personaggio) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per istanziare " + classeLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, classeLocazione);
		RegistroPersonaggi.addPersonaggioInLocazione(personaggio, coordinate);
	}
	
	private static final void costruisci(ClassiLocazione classeLocazione, Artefatto artefatto) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per istanziare " + classeLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, classeLocazione);
		RegistroArtefatti.addArtefattoInLocazione(artefatto, coordinate);
	}

	private static final void costruisci(ClassiLocazione classeLocazione, int quantita) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per istanziare " + classeLocazione.name());
		}
		Logger.log("Costruzione di " + quantita + " classiLocazione " + classeLocazione);
		for (int i = 0; i < quantita; i++) {
			setLocazione(getCoordinateLibere(), classeLocazione);
		}
	}
	
	static final CoordinateMD getCoordinateLibere() {
		ClassiLocazione classeLocazione;
		CoordinateMD coordinate;
		do {
			coordinate = new CoordinateMD(Random.getInt(getDimensioneX()), Random.getInt(getDimensioneY()));
			classeLocazione = getLocazione(coordinate);
		} while (classeLocazione != null && classeLocazione != ClassiLocazione.BOSCO && classeLocazione != ClassiLocazione.RADURA);
		return coordinate;
	}

	/**
	 * Abbiamo appena visitato questa locazione
	 */
	static final void setLocazioneVisitata(CoordinateMD coordinate) {
		forestaMD.impostaLocazioneVisitata(coordinate);
	}

	public static final void setLocazioneVisitata(CoordinateMD coordinate, boolean visitata) {
		forestaMD.impostaLocazioneVisitata(coordinate, visitata);
	}

	/**
	 * Siamo gia' passati da questa locazione?
	 */
	public static final boolean isLocazioneVisitata(CoordinateMD coordinate) {
		return forestaMD.isLocazioneVisitata(coordinate);
	}

	/**
	 * Sappiamo cosa ci sia in questa locazione
	 */
	public static final void setLocazioneConosciuta(CoordinateMD coordinate) {
		forestaMD.impostaLocazioneConosciuta(coordinate);
	}

	public static final boolean isLocazioneConosciuta(CoordinateMD coordinate) {
		return forestaMD.isLocazioneConosciuta(coordinate);
	}
	/**
	 * Un personaggio compra la mappa della foresta da un PNG
	 */
	public static final void ottieniMappa() {
		forestaMD.ottieniMappa();
	}

	/**
	 * Un PNG mostra ad un personaggio un pezzetto della mappa della foresta
	 */
	public static final void ottieniMappaZona(int daX, int daY, int aX, int aY) {
		for (int x = daX; x <= aX; x++) {
			for (int y = daY; y <= aY; y++) {
				setLocazioneConosciuta(new CoordinateMD(x, y));
			}
		}
	}

	/**
	 * Questa funzione viene richiamata ogni volta che un gruppo si sposta
	 */
	public static final void aggiorna(GruppoGiocatore gruppo) {
		int x = gruppo.getX();
		int y = gruppo.getY();
		int daX = x - 3;
		int aX = x + 3;
		int daY = y - 3;
		int aY = y + 3;
		if (daX < 0) {
			daX = 0;
			aX = 6;
		}
		if (aX >= Foresta.getDimensioneX()) {
			daX = Foresta.getDimensioneX() - 7;
			aX = Foresta.getDimensioneX() - 1;
		}
		if (daY < 0) {
			daY = 0;
			aY = 6;
		}
		if (aY >= Foresta.getDimensioneY()) {
			daY = Foresta.getDimensioneY() - 7;
			aY = Foresta.getDimensioneY() - 1;
		}
		ottieniMappaZona(daX, daY, aX, aY);
	}
	
	public static final int getMinXConosciuta() {
		return forestaMD.getMinXConosciuta();
	}
	
	public static final int getMaxXConosciuta() {
		return forestaMD.getMaxXConosciuta();
	}
	
	public static final int getMinYConosciuta() {
		return forestaMD.getMinYConosciuta();
	}
	
	public static final int getMaxYConosciuta() {
		return forestaMD.getMaxYConosciuta();
	}
}
