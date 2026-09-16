package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ForestaMD;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.List;

/**
 * Contiene la mappa di una istanza della Foresta,
 * l'elenco dei Cacciatori di Draghi tuttora disponibili,
 * varie ed eventuali
 */

public class Foresta {

	private Foresta() {
	}

	private static final ForestaMD forestaMD = ModelloDati.getIstanza().getForestaMD();
	
	public static int getDimensioneX() {
		return forestaMD.getDimensioneX();
	}
	
	public static int getDimensioneY() {
		return forestaMD.getDimensioneY();
	}
	
	public static void impostaLocazioneCorrente(ClassiLocazione classeLocazione) {
		forestaMD.impostaLocazione(GruppoGiocatore.getIstanza().getCoordinate(), classeLocazione);
	}

	public static CoordinateMD getCoordinateLocazioneUnica(ClassiLocazione classeLocazione) {
		if (!classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException();
		}
		return forestaMD.ottieniCoordinateLocazioneUnica(classeLocazione);
	}
	
	public static void distruggiLocazioneUnica(ClassiLocazione classeLocazione, ClassiLocazione nuovaClasseLocazione) {
		CoordinateMD coordinate = getCoordinateLocazioneUnica(classeLocazione);
		if (coordinate != null) {
			forestaMD.impostaLocazione(coordinate, nuovaClasseLocazione);
			forestaMD.rimuoviLocazioneUnica(classeLocazione);
		}
	}

	public static ClassiLocazione getLocazione(CoordinateMD coordinate) {
		return forestaMD.ottieniClasseLocazione(coordinate);
	}
	
	public static ClassiLocazione getLocazione(int x, int y) {
		return forestaMD.ottieniClasseLocazione(x, y);
	}

	public static LocazioneMD getLocazioneMD(CoordinateMD coordinate) {
		return forestaMD.ottieniLocazioneMD(coordinate);
	}

	/**
	 * Il punto unico dove nasce l'istanza di una casella: la classe la costruisce,
	 * e il modello dati della casella le dice di quale casella si tratta.
	 * L'istanza vive quanto la visita e poi si butta.
	 */
	public static Locazione costruisciIstanza(CoordinateMD coordinate) {
		LocazioneMD locazioneMD = forestaMD.ottieniLocazioneMD(coordinate);
		Locazione locazione = locazioneMD.getClasse().getIstanza();
		locazione.setModelloDati(locazioneMD);
		return locazione;
	}
	
	public static CoordinateMD costruisciLocazioneUnica(ClassiLocazione classeLocazioneUnica, boolean conosciutaSuMappa) {
		if (!classeLocazioneUnica.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazione per creare " + classeLocazioneUnica.name());
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
	 * Reimposta completamente il mondo
	 */
	static void reimposta() {
		// Reset produzioni one-shot (nomi locande, fiabe, oroscopi) per la nuova partita
		ProduttoreDiTestiCasuale.resetProduzioni();

		RegistroPersonaggi.reimposta();
		LineaTemporale.reimposta();
		RegistroMissioni.reimposta();
		RegistroArtefatti.reimposta();
		Statistiche.reimposta();

		//TODO quando tutti i modelli dati sono stati creati spostare reimposta su ModelloDati
		final int dimensioneX = 20;
		final int dimensioneY = 20;
		forestaMD.reimposta(dimensioneX, dimensioneY);

		int numeroCitta = 0;
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CITTA) {
				numeroCitta++;
			}
		}
		int numeroLocandeMax = Math.max(RegistroPersonaggi.getNumeroPersonaggiDisponibili(), (getDimensioneX() + getDimensioneY()) >> 2);
		List<ProduttoreDiTestiCasuale.DatiLocanda> poolDatiLocanda = ProduttoreDiTestiCasuale.getDatiLocanda(numeroCitta + numeroLocandeMax);

		costruisciCittaEPosizionaPersonaggi(poolDatiLocanda);
		costruisciCastelli();		
		costruisciLocandeEPosizionaPersonaggi(poolDatiLocanda);
		costruisciTempliEPosizionaArtefatti();
		
		int media = (getDimensioneX() + getDimensioneY()) / 2;

		costruisci(ClassiLocazione.GROTTA, media >> 1);
		costruisci(ClassiLocazione.PALUDE, media);
		costruisci(ClassiLocazione.ROVINE, media);

		// Il resto della Foresta è bosco. Va posato prima di sistemare il gruppo,
		// che appena arriva si guarda intorno e ha bisogno di caselle su cui farlo.
		for (int x = 0; x < getDimensioneX(); x++) {
			for (int y = 0; y < getDimensioneY(); y++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (getLocazione(coordinate) == null) {
					setLocazione(coordinate, ClassiLocazione.BOSCO);
				}
			}
		}

		GruppoGiocatore.getIstanza().reimposta();
		GruppoAvversario.getIstanza().reimposta();
	}

	private static void setLocazione(CoordinateMD coordinate, ClassiLocazione classeLocazione) {
		forestaMD.impostaLocazione(coordinate, classeLocazione);
	}
	
	/**
	 * Costruisce le città e ci piazza un personaggio a caso
	 */
	private static void costruisciCittaEPosizionaPersonaggi(List<ProduttoreDiTestiCasuale.DatiLocanda> poolDatiLocanda) {
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CITTA) {
				CoordinateMD coordinate = costruisciLocazioneUnica(classeLocazione, false);
				Locanda.impostaDatiLocanda(getLocazioneMD(coordinate), poolDatiLocanda.remove(0));
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
	private static void costruisciCastelli() {
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CASTELLO && classeLocazione != ClassiLocazione.CASTELLO_DRAGO) {
				costruisciLocazioneUnica(classeLocazione, false);
			}
		}
	}
	
	/**
	 * Costruisce le locande e piazza i rimanenti personaggi disponibili
	 */
	private static void costruisciLocandeEPosizionaPersonaggi(List<ProduttoreDiTestiCasuale.DatiLocanda> poolDatiLocanda) {
		int locandeCostruite = 0;
		Personaggio personaggioDisponibile;
		while ((personaggioDisponibile = RegistroPersonaggi.getPersonaggioDisponibile()) != null) {
			costruisci(ClassiLocazione.LOCANDA, personaggioDisponibile, poolDatiLocanda.remove(0));
			locandeCostruite++;
		}
		int media = (getDimensioneX() + getDimensioneY()) >> 2;
		for (int i = locandeCostruite; i < media; i++) {
			costruisci(ClassiLocazione.LOCANDA, poolDatiLocanda.remove(0));
		}
	}

	/**
	 * Costruisce le locande e piazza i rimanenti personaggi disponibili
	 */
	private static void costruisciTempliEPosizionaArtefatti() {
		int templiCostruiti = 0;
		Artefatto artefattoDisponibile;
		while ((artefattoDisponibile = RegistroArtefatti.getArtefattoDisponibile()) != null) {
			costruisci(ClassiLocazione.TEMPIO, artefattoDisponibile);
			templiCostruiti++;
		}
		int media = (getDimensioneX() + getDimensioneY()) >> 2;
		if (media > templiCostruiti) {
			costruisci(ClassiLocazione.TEMPIO, media - templiCostruiti);
		}
	}

	private static void costruisci(ClassiLocazione classeLocazione, Personaggio personaggio, ProduttoreDiTestiCasuale.DatiLocanda datiLocanda) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + classeLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, classeLocazione);
		Locanda.impostaDatiLocanda(getLocazioneMD(coordinate), datiLocanda);
		RegistroPersonaggi.addPersonaggioInLocazione(personaggio, coordinate);
	}

	private static void costruisci(ClassiLocazione classeLocazione, ProduttoreDiTestiCasuale.DatiLocanda datiLocanda) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + classeLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, classeLocazione);
		Locanda.impostaDatiLocanda(getLocazioneMD(coordinate), datiLocanda);
	}
	
	private static void costruisci(ClassiLocazione classeLocazione, Artefatto artefatto) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + classeLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, classeLocazione);
		RegistroArtefatti.addArtefattoInLocazione(artefatto, coordinate);
	}

	private static void costruisci(ClassiLocazione classeLocazione, int quantita) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + classeLocazione.name());
		}
		Logger.log("Costruzione di " + quantita + " classiLocazione " + classeLocazione);
		for (int i = 0; i < quantita; i++) {
			setLocazione(getCoordinateLibere(), classeLocazione);
		}
	}
	
	static CoordinateMD getCoordinateLibere() {
		ClassiLocazione classeLocazione;
		CoordinateMD coordinate;
		do {
			coordinate = new CoordinateMD(Dado.tira(getDimensioneX()) - 1, Dado.tira(getDimensioneY()) - 1);
			classeLocazione = getLocazione(coordinate);
		} while (classeLocazione != null && classeLocazione != ClassiLocazione.BOSCO && classeLocazione != ClassiLocazione.RADURA);
		return coordinate;
	}

	/**
	 * Abbiamo appena visitato questa locazione
	 */
	static void setLocazioneVisitata(CoordinateMD coordinate) {
		forestaMD.impostaLocazioneVisitata(coordinate);
	}

	public static void setLocazioneVisitata(CoordinateMD coordinate, boolean visitata) {
		forestaMD.impostaLocazioneVisitata(coordinate, visitata);
	}

	/**
	 * Siamo già passati da questa locazione?
	 */
	public static boolean isLocazioneVisitata(CoordinateMD coordinate) {
		return forestaMD.isLocazioneVisitata(coordinate);
	}

	/**
	 * Sappiamo cosa ci sia in questa locazione
	 */
	public static void setLocazioneConosciuta(CoordinateMD coordinate) {
		forestaMD.impostaLocazioneConosciuta(coordinate);
	}

	public static boolean isLocazioneConosciuta(CoordinateMD coordinate) {
		return forestaMD.isLocazioneConosciuta(coordinate);
	}
	/**
	 * Un personaggio compra la mappa della foresta da un PNG
	 */
	public static void ottieniMappa() {
		forestaMD.ottieniMappa();
	}

	/**
	 * Un PNG mostra a un personaggio un pezzetto della mappa della foresta
	 */
	public static void ottieniMappaZona(int daX, int daY, int aX, int aY) {
		for (int x = daX; x <= aX; x++) {
			if (x >= 0 && x < Foresta.getDimensioneX()) {
				for (int y = daY; y <= aY; y++) {
					if (y >= 0 && y < Foresta.getDimensioneY()) {
						setLocazioneConosciuta(new CoordinateMD(x, y));
					}
				}
			}
		}
	}

	/**
	 * Questa funzione viene richiamata ogni volta che un gruppo si sposta
	 */
	public static void aggiorna(GruppoGiocatore gruppo) {
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
	
	public static int getMinXConosciuta() {
		return forestaMD.getMinXConosciuta();
	}
	
	public static int getMaxXConosciuta() {
		return forestaMD.getMaxXConosciuta();
	}
	
	public static int getMinYConosciuta() {
		return forestaMD.getMinYConosciuta();
	}
	
	public static int getMaxYConosciuta() {
		return forestaMD.getMaxYConosciuta();
	}
}
