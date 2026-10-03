package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.Bosco;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.locazioni.Rovine;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ForestaMD;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Contiene la mappa di una istanza della Foresta,
 * l'elenco dei Cacciatori di Draghi tuttora disponibili,
 * varie ed eventuali
 */

public class Foresta {

	private static final int DIMENSIONE_X = 20;
	private static final int DIMENSIONE_Y = 20;

	private Foresta() {
	}

	private static ForestaMD getForestaMD() {
		return ModelloDati.getIstanza().getForestaMD();
	}
	
	public static int getDimensioneX() {
		return getForestaMD().getDimensioneX();
	}
	
	public static int getDimensioneY() {
		return getForestaMD().getDimensioneY();
	}
	
	public static void impostaLocazioneCorrente(ClassiLocazione classeLocazione) {
		getForestaMD().impostaLocazione(GruppoGiocatore.getIstanza().getCoordinate(), classeLocazione);
	}

	public static CoordinateMD getCoordinateLocazioneUnica(ClassiLocazione classeLocazione) {
		if (!classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException();
		}
		return getForestaMD().ottieniCoordinateLocazioneUnica(classeLocazione);
	}
	
	public static void distruggiLocazioneUnica(ClassiLocazione classeLocazione, ClassiLocazione nuovaClasseLocazione) {
		CoordinateMD coordinate = getCoordinateLocazioneUnica(classeLocazione);
		if (coordinate != null) {
			ForestaMD md = getForestaMD();
			md.impostaLocazione(coordinate, nuovaClasseLocazione);
			md.rimuoviLocazioneUnica(classeLocazione);
			if (nuovaClasseLocazione == ClassiLocazione.ROVINE) {
				// Le rovine di un castello o di una città prendono il nome da quello che c'era
				getLocazioneMD(coordinate).setNome("le Rovine " + Misc.conPreposizione("di", classeLocazione.getNomeProprio()));
			}
		}
	}

	public static ClassiLocazione getLocazione(CoordinateMD coordinate) {
		return getForestaMD().ottieniClasseLocazione(coordinate);
	}
	
	public static ClassiLocazione getLocazione(int x, int y) {
		return getForestaMD().ottieniClasseLocazione(x, y);
	}

	public static LocazioneMD getLocazioneMD(CoordinateMD coordinate) {
		return getForestaMD().ottieniLocazioneMD(coordinate);
	}

	/**
	 * Il punto unico dove nasce l'istanza di una casella: la classe la costruisce,
	 * e il modello dati della casella le dice di quale casella si tratta.
	 * L'istanza vive quanto la visita e poi si butta.
	 */
	public static Locazione costruisciIstanza(CoordinateMD coordinate) {
		LocazioneMD locazioneMD = getForestaMD().ottieniLocazioneMD(coordinate);
		Locazione locazione = locazioneMD.getClasse().getIstanza();
		locazione.setModelloDati(locazioneMD);
		return locazione;
	}
	
	public static CoordinateMD costruisciLocazioneUnica(ClassiLocazione classeLocazioneUnica, boolean conosciutaSuMappa) {
		return costruisciLocazioneUnica(classeLocazioneUnica, getCoordinateLibere(), conosciutaSuMappa);
	}

	/**
	 * Fa diventare la casella una locazione ordinaria di quella classe (per esempio un tempio, al posto di un bosco
	 * rivendicato da una missione), come non ancora visitata.
	 */
	public static void costruisciLocazione(CoordinateMD coordinate, ClassiLocazione classeLocazione) {
		if (classeLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + classeLocazione.name());
		}
		Logger.log("Costruzione di " + classeLocazione + " in " + coordinate);
		setLocazione(coordinate, classeLocazione);
		setLocazioneVisitata(coordinate, false);
	}

	/**
	 * Costruisce la locazione unica in quelle coordinate, scelte da chi chiama (per esempio da
	 * RegistroMissioni.cerca, per una missione che si procura la propria locazione).
	 */
	public static CoordinateMD costruisciLocazioneUnica(ClassiLocazione classeLocazioneUnica, CoordinateMD coordinate, boolean conosciutaSuMappa) {
		if (!classeLocazioneUnica.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazione per creare " + classeLocazioneUnica.name());
		}
		Logger.log("Costruzione di " + classeLocazioneUnica + " in " + coordinate);
		setLocazione(coordinate, classeLocazioneUnica);
		getLocazioneMD(coordinate).setNome(classeLocazioneUnica.getNomeProprio());
		getForestaMD().aggiungiLocazioneUnica(classeLocazioneUnica, coordinate);
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

		getForestaMD().reimposta(DIMENSIONE_X, DIMENSIONE_Y);

		int numeroCitta = 0;
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CITTA) {
				numeroCitta++;
			}
		}
		int numeroLocandeMax = Math.max(RegistroPersonaggi.getNumeroPersonaggiDisponibili(), (getDimensioneX() + getDimensioneY()) >> 2);
		List<ProduttoreDiTestiCasuale.DatiLocanda> poolDatiLocanda = ProduttoreDiTestiCasuale.getDatiLocanda(numeroCitta + numeroLocandeMax);

		costruisciCittaEPosizionaPersonaggi(poolDatiLocanda);
		// I castelli degli alleati del Drago non si costruiscono qui: li rivendica ciascuno la propria missione
		// all'inizio della partita (vedi RegistroMissioni.rivendicaPerLocazioneUnica)
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
					Bosco.impostaVarianteMappaACaso(getLocazioneMD(coordinate));
				}
			}
		}

		GruppoGiocatore.getIstanza().reimposta();
		GruppoAvversario.getIstanza().reimposta();
	}

	private static void setLocazione(CoordinateMD coordinate, ClassiLocazione classeLocazione) {
		getForestaMD().impostaLocazione(coordinate, classeLocazione);
		// Templi e rovine hanno il loro nome da quando nascono
		if (classeLocazione == ClassiLocazione.TEMPIO) {
			Tempio.getNome(getLocazioneMD(coordinate));
		} else if (classeLocazione == ClassiLocazione.ROVINE) {
			Rovine.getNome(getLocazioneMD(coordinate));
		}
	}

	// Quante volte provare a pescare un nome che nessun'altra casella dello stesso tipo ha già
	private static final int TENTATIVI_NOME_NUOVO = 20;

	/**
	 * Un nome dal generatore, possibilmente diverso da quello di tutte le caselle di quella classe (per esempio di
	 * tutti gli altri templi): se dopo qualche tentativo non ci si riesce, va bene anche un doppione.
	 */
	public static String nomeNuovo(ClassiLocazione classe, Supplier<String> generatore) {
		Set<String> giaDati = new HashSet<>();
		for (int x = 0; x < getDimensioneX(); x++) {
			for (int y = 0; y < getDimensioneY(); y++) {
				LocazioneMD md = getLocazioneMD(new CoordinateMD(x, y));
				if (md != null && md.getClasse() == classe && md.getNome() != null) {
					giaDati.add(md.getNome());
				}
			}
		}
		String nome = generatore.get();
		for (int i = 1; i < TENTATIVI_NOME_NUOVO && giaDati.contains(nome); i++) {
			nome = generatore.get();
		}
		return nome;
	}

	/**
	 * Il nome della casella da mostrare sulla mappa, se il gruppo la conosce e ha un nome (città, castelli, locande,
	 * templi...); altrimenti null.
	 */
	public static String getNomeDaMostrare(CoordinateMD coordinate) {
		if (coordinate.getX() < 0 || coordinate.getX() >= getDimensioneX() || coordinate.getY() < 0 || coordinate.getY() >= getDimensioneY()
				|| !isLocazioneConosciuta(coordinate)) {
			return null;
		}
		return getLocazioneMD(coordinate).getNome();
	}
	
	/**
	 * Costruisce le città, una per quadrante (in ordine casuale), e ci piazza un personaggio a caso
	 */
	private static void costruisciCittaEPosizionaPersonaggi(List<ProduttoreDiTestiCasuale.DatiLocanda> poolDatiLocanda) {
		List<Quadrante> quadranti = Quadrante.inOrdineCasuale();
		for (ClassiLocazione classeLocazione : ClassiLocazione.values()) {
			if (classeLocazione.getTipoLocazione() == TipoLocazione.CITTA) {
				CoordinateMD coordinate = quadranti.isEmpty()
						? costruisciLocazioneUnica(classeLocazione, false)
						: costruisciLocazioneUnica(classeLocazione, getCoordinateLibere(quadranti.remove(0)), false);
				Locanda.impostaDatiLocanda(getLocazioneMD(coordinate), poolDatiLocanda.remove(0));
				RegistroArtefatti.riempiMagazzini(coordinate, GeneratoreArtefatti.istanza());
				Personaggio personaggioDisponibile = RegistroPersonaggi.getPersonaggioDisponibile();
				if (personaggioDisponibile != null) {
					RegistroPersonaggi.addPersonaggioInLocazione(personaggioDisponibile, coordinate);
				}
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
		} while (!isLibera(classeLocazione));
		return coordinate;
	}

	/**
	 * Come {@link #getCoordinateLibere()}, ma dentro il quadrante.
	 */
	static CoordinateMD getCoordinateLibere(Quadrante quadrante) {
		CoordinateMD coordinate;
		do {
			coordinate = quadrante.getCoordinateACaso();
		} while (!isLibera(getLocazione(coordinate)));
		return coordinate;
	}

	private static boolean isLibera(ClassiLocazione classeLocazione) {
		return classeLocazione == null || classeLocazione == ClassiLocazione.BOSCO || classeLocazione == ClassiLocazione.RADURA;
	}

	/**
	 * Abbiamo appena visitato questa locazione
	 */
	static void setLocazioneVisitata(CoordinateMD coordinate) {
		getForestaMD().impostaLocazioneVisitata(coordinate);
	}

	public static void setLocazioneVisitata(CoordinateMD coordinate, boolean visitata) {
		getForestaMD().impostaLocazioneVisitata(coordinate, visitata);
	}

	/**
	 * Siamo già passati da questa locazione?
	 */
	public static boolean isLocazioneVisitata(CoordinateMD coordinate) {
		return getForestaMD().isLocazioneVisitata(coordinate);
	}

	/**
	 * Sappiamo cosa ci sia in questa locazione
	 */
	public static void setLocazioneConosciuta(CoordinateMD coordinate) {
		getForestaMD().impostaLocazioneConosciuta(coordinate);
	}

	public static boolean isLocazioneConosciuta(CoordinateMD coordinate) {
		return getForestaMD().isLocazioneConosciuta(coordinate);
	}

	/**
	 * Le locazioni da segnalare sulla mappa con Indicatore.gif: quelle di un artefatto di cui
	 * si è saputo tramite Informazioni, più il bersaglio, ancora da raggiungere, di una missione
	 * "Recupera le derrate alimentari" o "Recupera il medaglione" attualmente attiva.
	 */
	public static List<CoordinateMD> getCoordinateDaSegnalare() {
		Set<CoordinateMD> coordinateDaSegnalare = new LinkedHashSet<>(RegistroArtefatti.getLocalizzazioniConosciute());
		coordinateDaSegnalare.addAll(RegistroMissioni.getLocazioniDaSegnalare());
		return new ArrayList<>(coordinateDaSegnalare);
	}

	public static int getVersioneMappa() {
		return getForestaMD().getVersioneMappa();
	}

	/**
	 * Un personaggio compra la mappa della foresta da un PNG
	 */
	public static void ottieniMappa() {
		getForestaMD().ottieniMappa();
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
	public static void aggiornaMappaCircostante(GruppoGiocatore gruppo) {
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
		return getForestaMD().getMinXConosciuta();
	}
	
	public static int getMaxXConosciuta() {
		return getForestaMD().getMaxXConosciuta();
	}
	
	public static int getMinYConosciuta() {
		return getForestaMD().getMinYConosciuta();
	}
	
	public static int getMaxYConosciuta() {
		return getForestaMD().getMaxYConosciuta();
	}
}
