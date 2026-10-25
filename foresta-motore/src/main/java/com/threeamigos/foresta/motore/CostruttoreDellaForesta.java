package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.locazioni.*;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.mappa.LocazioneMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale;
import com.threeamigos.foresta.testi.ProduttoriDiTesti;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.trofei.RegistroTrofei;

import java.util.*;

/**
 * Chi costruisce il mondo sulla mappa della Foresta (vedi {@link Foresta}): le città e i castelli (che sono unici), le locande, i templi con
 * i loro artefatti, le grotte, le paludi e il bosco, con i personaggi e i registri che ci vivono; e chi dà l'istanza di una casella per la
 * visita. Fa anche da ponte fra la mappa e le missioni, gli artefatti e il gruppo (le caselle da segnalare, la mappa intorno al gruppo).
 */
public class CostruttoreDellaForesta {

	private static final int DIMENSIONE_X = 20;
	private static final int DIMENSIONE_Y = 20;

	private CostruttoreDellaForesta() {
	}

	/**
	 * Fa della casella in cui si trova il gruppo una locazione di quel tipo.
	 */
	public static void impostaLocazioneCorrente(TipoLocazione tipoLocazione) {
		Foresta.impostaLocazione(GruppoGiocatore.getIstanza().getCoordinate(), tipoLocazione);
	}

	/**
	 * Il punto unico dove nasce l'istanza di una casella: la classe la costruisce,
	 * e il modello dati della casella le dice di quale casella si tratta.
	 * L'istanza vive quanto la visita e poi si butta.
	 */
	public static Locazione costruisciIstanza(CoordinateMD coordinate) {
		LocazioneMD locazioneMD = Foresta.getLocazioneMD(coordinate);
		Locazione locazione = FabbricaLocazioni.crea(locazioneMD.getTipo());
		locazione.setModelloDati(locazioneMD);
		return locazione;
	}
	
	/**
	 * Fa diventare la casella una locazione ordinaria di quella classe (per esempio un tempio, al posto di un bosco
	 * rivendicato da una missione), come non ancora visitata.
	 */
	public static void costruisciLocazione(CoordinateMD coordinate, TipoLocazione tipoLocazione) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		Logger.log(() -> "Costruzione di " + tipoLocazione + " in " + coordinate);
		setLocazione(coordinate, tipoLocazione);
		Foresta.setLocazioneVisitata(coordinate, false);
	}

	/**
	 * Reimposta completamente il mondo
	 */
	static void reimposta() {
		// Reset produzioni one-shot (nomi locande, fiabe, oroscopi) per la nuova partita
		ProduttoriDiTesti.resetProduzioni();

		RegistroPersonaggi.reimposta();
		LineaTemporale.reimposta();
		RegistroMissioni.reimposta();
		RegistroArtefatti.reimposta();
		Statistiche.reimposta();
		IntermezzoLibroTecnico.reimposta();
		// Quello che la partita precedente aveva guadagnato per i trofei e non aveva ancora consolidato non vale per questa
		RegistroTrofei.scartaIlProgressoInSospeso();
		// Gli intermezzi già visti e le notizie sono di una partita: la nuova ricomincia da capo (l'intro, le prime visite)
		ModelloDati.getIstanza().getIntermezziMD().reimposta();
		ModelloDati.getIstanza().getNotizieMD().reimposta();

		Foresta.azzera(DIMENSIONE_X, DIMENSIONE_Y);

		int numeroCitta = 0;
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
				numeroCitta++;
			}
		}
		int numeroLocandeMax = Math.max(RegistroPersonaggi.getNumeroPersonaggiDisponibili(), (Foresta.getDimensioneX() + Foresta.getDimensioneY()) >> 2);
		List<ProduttoreDiLocandeCasuale.DatiLocanda> poolDatiLocanda = ProduttoreDiLocandeCasuale.getDatiLocanda(numeroCitta + numeroLocandeMax);

		costruisciCittaEPosizionaPersonaggi(poolDatiLocanda);
		// Dopo i locandieri e le persone delle città, e senza azzerare i nomi: nessuno ha il nome di un altro
		RegistroPersonaggi.creaIlBardoESuaMoglie();
		// I castelli degli alleati del Drago non si costruiscono qui: li rivendica ciascuno la propria missione
		// all'inizio della partita (vedi RegistroMissioni.rivendicaPerLocazioneUnica)
		costruisciLocandeEPosizionaPersonaggi(poolDatiLocanda);
		costruisciTempliEPosizionaArtefatti();
		
		int media = (Foresta.getDimensioneX() + Foresta.getDimensioneY()) / 2;

		costruisci(TipoLocazione.GROTTA, media >> 1);
		costruisci(TipoLocazione.PALUDE, media);
		costruisci(TipoLocazione.ROVINE, media);
		// Poche radure, quante le grotte: ci crescono alcuni ingredienti dell'alchimista
		costruisci(TipoLocazione.RADURA, media >> 1);

		// Il resto della Foresta è bosco. Va posato prima di sistemare il gruppo,
		// che appena arriva si guarda intorno e ha bisogno di caselle su cui farlo.
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (Foresta.getLocazione(coordinate) == null) {
					setLocazione(coordinate, TipoLocazione.BOSCO);
					Bosco.impostaVarianteMappaACaso(Foresta.getLocazioneMD(coordinate));
				}
			}
		}

		GruppoGiocatore.getIstanza().reimposta();
		GruppoAvversario.getIstanza().reimposta();
	}

	private static void setLocazione(CoordinateMD coordinate, TipoLocazione tipoLocazione) {
		Foresta.impostaLocazione(coordinate, tipoLocazione);
		// Templi e rovine hanno il loro nome da quando nascono
		if (tipoLocazione == TipoLocazione.TEMPIO) {
			Tempio.getNome(Foresta.getLocazioneMD(coordinate));
		} else if (tipoLocazione == TipoLocazione.ROVINE) {
			Rovine.getNome(Foresta.getLocazioneMD(coordinate));
		}
	}

	/**
	 * I nomi delle missioni da mostrare sulla mappa per quella casella (vedi RegistroMissioni.getNomiMissioniDaSegnalare),
	 * se il gruppo la conosce; altrimenti nessuno.
	 */
	public static List<String> getNomiMissioniDaMostrare(CoordinateMD coordinate) {
		if (!Foresta.isDentroLaMappa(coordinate) || !Foresta.isLocazioneConosciuta(coordinate)) {
			return Collections.emptyList();
		}
		return RegistroMissioni.getNomiMissioniDaSegnalare(coordinate);
	}

	/**
	 * Costruisce le città, una per quadrante (in ordine casuale), e ci piazza un personaggio a caso
	 */
	private static void costruisciCittaEPosizionaPersonaggi(List<ProduttoreDiLocandeCasuale.DatiLocanda> poolDatiLocanda) {
		List<Quadrante> quadranti = Quadrante.inOrdineCasuale();
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
				CoordinateMD coordinate = quadranti.isEmpty()
						? Foresta.costruisciLocazioneUnica(tipoLocazione, false)
						: Foresta.costruisciLocazioneUnica(tipoLocazione, getCoordinateLibere(quadranti.remove(0)), false);
				Locanda.impostaDatiLocanda(Foresta.getLocazioneMD(coordinate), poolDatiLocanda.remove(0));
				RegistroPersonaggi.creaIResidentiDellaCitta(tipoLocazione);
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
	private static void costruisciLocandeEPosizionaPersonaggi(List<ProduttoreDiLocandeCasuale.DatiLocanda> poolDatiLocanda) {
		int locandeCostruite = 0;
		Personaggio personaggioDisponibile;
		while ((personaggioDisponibile = RegistroPersonaggi.getPersonaggioDisponibile()) != null) {
			costruisci(TipoLocazione.LOCANDA, personaggioDisponibile, poolDatiLocanda.remove(0));
			locandeCostruite++;
		}
		int media = (Foresta.getDimensioneX() + Foresta.getDimensioneY()) >> 2;
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
		int media = (Foresta.getDimensioneX() + Foresta.getDimensioneY()) >> 2;
		int templiSenzaArtefatto = Math.max(templiConArtefatto, media - templiConArtefatto);
		costruisci(TipoLocazione.TEMPIO, templiSenzaArtefatto);
	}

	private static void costruisci(TipoLocazione tipoLocazione, Personaggio personaggio, ProduttoreDiLocandeCasuale.DatiLocanda datiLocanda) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		CoordinateMD coordinate = Foresta.getCoordinateLibere();
		setLocazione(coordinate, tipoLocazione);
		Locanda.impostaDatiLocanda(Foresta.getLocazioneMD(coordinate), datiLocanda);
		RegistroPersonaggi.addPersonaggioInLocazione(personaggio, coordinate);
	}

	private static void costruisci(TipoLocazione tipoLocazione, ProduttoreDiLocandeCasuale.DatiLocanda datiLocanda) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		CoordinateMD coordinate = Foresta.getCoordinateLibere();
		setLocazione(coordinate, tipoLocazione);
		Locanda.impostaDatiLocanda(Foresta.getLocazioneMD(coordinate), datiLocanda);
	}
	
	private static void costruisci(TipoLocazione tipoLocazione, Artefatto artefatto) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		CoordinateMD coordinate = Foresta.getCoordinateLibere();
		setLocazione(coordinate, tipoLocazione);
		RegistroArtefatti.addArtefattoInLocazione(artefatto, coordinate);
	}

	private static void costruisci(TipoLocazione tipoLocazione, int quantita) {
		if (tipoLocazione.isLocazioneUnica()) {
			throw new IllegalArgumentException("Utilizzare costruisciLocazioneUnica per creare " + tipoLocazione.name());
		}
		Logger.log(() -> "Costruzione di " + quantita + " classiLocazione " + tipoLocazione);
		for (int i = 0; i < quantita; i++) {
			setLocazione(Foresta.getCoordinateLibere(), tipoLocazione);
		}
	}
	
	/**
	 * Come {@link Foresta#getCoordinateLibere()}, ma dentro il quadrante.
	 */
	static CoordinateMD getCoordinateLibere(Quadrante quadrante) {
		CoordinateMD coordinate;
		do {
			coordinate = quadrante.getCoordinateACaso();
		} while (!Foresta.isLibera(Foresta.getLocazione(coordinate)));
		return coordinate;
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
		Foresta.ottieniMappaZona(daX, daY, aX, aY);
	}
}
