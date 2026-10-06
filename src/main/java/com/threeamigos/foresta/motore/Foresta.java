package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.Bosco;
import com.threeamigos.foresta.locazioni.FabbricaLocazioni;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.locazioni.Rovine;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.ArrayList;
import java.util.Collections;
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
	
	public static void impostaLocazioneCorrente(TipoLocazione tipoLocazione) {
		getForestaMD().impostaLocazione(GruppoGiocatore.getIstanza().getCoordinate(), tipoLocazione);
	}

	public static CoordinateMD getCoordinateLocazioneUnica(TipoLocazione tipoLocazione) {
		if (!tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException();
		}
		return getForestaMD().ottieniCoordinateLocazioneUnica(tipoLocazione);
	}
	
	public static void distruggiLocazioneUnica(TipoLocazione tipoLocazione, TipoLocazione nuovoTipoLocazione) {
		CoordinateMD coordinate = getCoordinateLocazioneUnica(tipoLocazione);
		if (coordinate != null) {
			ForestaMD md = getForestaMD();
			md.impostaLocazione(coordinate, nuovoTipoLocazione);
			md.rimuoviLocazioneUnica(tipoLocazione);
			if (nuovoTipoLocazione == TipoLocazione.ROVINE) {
				// Le rovine di un castello o di una città prendono il nome da quello che c'era
				getLocazioneMD(coordinate).setNome("le Rovine " + Misc.conPreposizione("di", tipoLocazione.getNomeProprio()));
			}
		}
	}

	public static TipoLocazione getLocazione(CoordinateMD coordinate) {
		return getForestaMD().ottieniTipoLocazione(coordinate);
	}
	
	public static TipoLocazione getLocazione(int x, int y) {
		return getForestaMD().ottieniTipoLocazione(x, y);
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
		Locazione locazione = FabbricaLocazioni.crea(locazioneMD.getTipo());
		locazione.setModelloDati(locazioneMD);
		return locazione;
	}
	
	public static CoordinateMD costruisciLocazioneUnica(TipoLocazione tipoLocazioneUnica, boolean conosciutaSuMappa) {
		return costruisciLocazioneUnica(tipoLocazioneUnica, getCoordinateLibere(), conosciutaSuMappa);
	}

	/**
	 * Fa diventare la casella una locazione ordinaria di quella classe (per esempio un tempio, al posto di un bosco
	 * rivendicato da una missione), come non ancora visitata.
	 */
	public static void costruisciLocazione(CoordinateMD coordinate, TipoLocazione tipoLocazione) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		Logger.log("Costruzione di " + tipoLocazione + " in " + coordinate);
		setLocazione(coordinate, tipoLocazione);
		setLocazioneVisitata(coordinate, false);
	}

	/**
	 * Costruisce la locazione unica in quelle coordinate, scelte da chi chiama (per esempio da
	 * RegistroMissioni.cerca, per una missione che si procura la propria locazione).
	 */
	public static CoordinateMD costruisciLocazioneUnica(TipoLocazione tipoLocazioneUnica, CoordinateMD coordinate, boolean conosciutaSuMappa) {
		if (!tipoLocazioneUnica.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazione per creare " + tipoLocazioneUnica.name());
		}
		Logger.log("Costruzione di " + tipoLocazioneUnica + " in " + coordinate);
		setLocazione(coordinate, tipoLocazioneUnica);
		getLocazioneMD(coordinate).setNome(tipoLocazioneUnica.getNomeProprio());
		getForestaMD().aggiungiLocazioneUnica(tipoLocazioneUnica, coordinate);
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
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoria() == CategoriaLocazione.CITTA) {
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

		costruisci(TipoLocazione.GROTTA, media >> 1);
		costruisci(TipoLocazione.PALUDE, media);
		costruisci(TipoLocazione.ROVINE, media);
		// Poche radure, quante le grotte: ci crescono alcuni ingredienti dell'alchimista
		costruisci(TipoLocazione.RADURA, media >> 1);

		// Il resto della Foresta è bosco. Va posato prima di sistemare il gruppo,
		// che appena arriva si guarda intorno e ha bisogno di caselle su cui farlo.
		for (int x = 0; x < getDimensioneX(); x++) {
			for (int y = 0; y < getDimensioneY(); y++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (getLocazione(coordinate) == null) {
					setLocazione(coordinate, TipoLocazione.BOSCO);
					Bosco.impostaVarianteMappaACaso(getLocazioneMD(coordinate));
				}
			}
		}

		GruppoGiocatore.getIstanza().reimposta();
		GruppoAvversario.getIstanza().reimposta();
	}

	private static void setLocazione(CoordinateMD coordinate, TipoLocazione tipoLocazione) {
		getForestaMD().impostaLocazione(coordinate, tipoLocazione);
		// Templi e rovine hanno il loro nome da quando nascono
		if (tipoLocazione == TipoLocazione.TEMPIO) {
			Tempio.getNome(getLocazioneMD(coordinate));
		} else if (tipoLocazione == TipoLocazione.ROVINE) {
			Rovine.getNome(getLocazioneMD(coordinate));
		}
	}

	// Quante volte provare a pescare un nome che nessun'altra casella dello stesso tipo ha già
	private static final int TENTATIVI_NOME_NUOVO = 20;

	/**
	 * Un nome dal generatore, possibilmente diverso da quello di tutte le caselle di quella classe (per esempio di
	 * tutti gli altri templi): se dopo qualche tentativo non ci si riesce, va bene anche un doppione.
	 */
	public static String nomeNuovo(TipoLocazione tipo, Supplier<String> generatore) {
		Set<String> giaDati = new HashSet<>();
		for (int x = 0; x < getDimensioneX(); x++) {
			for (int y = 0; y < getDimensioneY(); y++) {
				LocazioneMD md = getLocazioneMD(new CoordinateMD(x, y));
				if (md != null && md.getTipo() == tipo && md.getNome() != null) {
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
	 * I nomi delle missioni da mostrare sulla mappa per quella casella (vedi RegistroMissioni.getNomiMissioniDaSegnalare),
	 * se il gruppo la conosce; altrimenti nessuno.
	 */
	public static List<String> getNomiMissioniDaMostrare(CoordinateMD coordinate) {
		if (coordinate.getX() < 0 || coordinate.getX() >= getDimensioneX() || coordinate.getY() < 0 || coordinate.getY() >= getDimensioneY()
				|| !isLocazioneConosciuta(coordinate)) {
			return Collections.emptyList();
		}
		return RegistroMissioni.getNomiMissioniDaSegnalare(coordinate);
	}

	/**
	 * Costruisce le città, una per quadrante (in ordine casuale), e ci piazza un personaggio a caso
	 */
	private static void costruisciCittaEPosizionaPersonaggi(List<ProduttoreDiTestiCasuale.DatiLocanda> poolDatiLocanda) {
		List<Quadrante> quadranti = Quadrante.inOrdineCasuale();
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoria() == CategoriaLocazione.CITTA) {
				CoordinateMD coordinate = quadranti.isEmpty()
						? costruisciLocazioneUnica(tipoLocazione, false)
						: costruisciLocazioneUnica(tipoLocazione, getCoordinateLibere(quadranti.remove(0)), false);
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
			costruisci(TipoLocazione.LOCANDA, personaggioDisponibile, poolDatiLocanda.remove(0));
			locandeCostruite++;
		}
		int media = (getDimensioneX() + getDimensioneY()) >> 2;
		for (int i = locandeCostruite; i < media; i++) {
			costruisci(TipoLocazione.LOCANDA, poolDatiLocanda.remove(0));
		}
	}

	/**
	 * Costruisce i templi e ci piazza gli artefatti: solo la metà dei templi ne ha uno, gli altri restano liberi.
	 */
	private static void costruisciTempliEPosizionaArtefatti() {
		int templiConArtefatto = 0;
		Artefatto artefattoDisponibile;
		while ((artefattoDisponibile = RegistroArtefatti.getArtefattoDisponibile()) != null) {
			costruisci(TipoLocazione.TEMPIO, artefattoDisponibile);
			templiConArtefatto++;
		}
		int media = (getDimensioneX() + getDimensioneY()) >> 2;
		int templiSenzaArtefatto = Math.max(templiConArtefatto, media - templiConArtefatto);
		costruisci(TipoLocazione.TEMPIO, templiSenzaArtefatto);
	}

	private static void costruisci(TipoLocazione tipoLocazione, Personaggio personaggio, ProduttoreDiTestiCasuale.DatiLocanda datiLocanda) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, tipoLocazione);
		Locanda.impostaDatiLocanda(getLocazioneMD(coordinate), datiLocanda);
		RegistroPersonaggi.addPersonaggioInLocazione(personaggio, coordinate);
	}

	private static void costruisci(TipoLocazione tipoLocazione, ProduttoreDiTestiCasuale.DatiLocanda datiLocanda) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, tipoLocazione);
		Locanda.impostaDatiLocanda(getLocazioneMD(coordinate), datiLocanda);
	}
	
	private static void costruisci(TipoLocazione tipoLocazione, Artefatto artefatto) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		CoordinateMD coordinate = getCoordinateLibere();
		setLocazione(coordinate, tipoLocazione);
		RegistroArtefatti.addArtefattoInLocazione(artefatto, coordinate);
	}

	private static void costruisci(TipoLocazione tipoLocazione, int quantita) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		Logger.log("Costruzione di " + quantita + " classiLocazione " + tipoLocazione);
		for (int i = 0; i < quantita; i++) {
			setLocazione(getCoordinateLibere(), tipoLocazione);
		}
	}
	
	static CoordinateMD getCoordinateLibere() {
		TipoLocazione tipoLocazione;
		CoordinateMD coordinate;
		do {
			coordinate = new CoordinateMD(Dado.tira(getDimensioneX()) - 1, Dado.tira(getDimensioneY()) - 1);
			tipoLocazione = getLocazione(coordinate);
		} while (!isLibera(tipoLocazione));
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

	/**
	 * Una casella su cui si può costruire: vuota, o un bosco. Non una radura: sono poche apposta, e una locanda o il
	 * covo di una missione costruiti sopra le cancellerebbero.
	 */
	private static boolean isLibera(TipoLocazione tipoLocazione) {
		return tipoLocazione == null || tipoLocazione == TipoLocazione.BOSCO;
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
	 * si è saputo tramite Informazioni, più le caselle rivendicate dalle missioni a passi attive che il gruppo conosce.
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
