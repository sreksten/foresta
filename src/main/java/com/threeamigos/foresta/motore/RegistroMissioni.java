package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.ModalitaDiProva;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.missioni.SconfiggiIlDrago;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.RegistroMissioniMD;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.*;

public class RegistroMissioni {

	private static RegistroMissioniMD getRegistroMissioni() {
		return ModelloDati.getIstanza().getRegistroMissioniMD();
	}

	private RegistroMissioni() {
	}

	public enum TipoMissionePredefinita {
		SCONFIGGI_IL_DRAGO(ClasseMissione.SCONFIGGI_IL_DRAGO),
		// Solo in modalità di prova (vedi ModalitaDiProva)
		MISSIONE_DI_PROVA(ClasseMissione.MISSIONE_DI_PROVA, true),
		// Solo in modalità di prova: serve a vedere una missione fallita nella finestra delle missioni
		MISSIONE_CHE_FALLISCE(ClasseMissione.MISSIONE_CHE_FALLISCE, true),
		RECUPERA_IL_MEDAGLIONE(ClasseMissione.RECUPERA_IL_MEDAGLIONE),
		RECUPERA_LE_DERRATE_ALIMENTARI(ClasseMissione.RECUPERA_LE_DERRATE_ALIMENTARI),
		LA_LEGGENDA_DI_NYENA(ClasseMissione.LA_LEGGENDA_DI_NYENA),
		LA_LEGGENDA_DI_MALGAARD(ClasseMissione.LA_LEGGENDA_DI_MALGAARD),
		CRONACHE_DI_UN_FEGATO_EROICO(ClasseMissione.CRONACHE_DI_UN_FEGATO_EROICO),
		NESSUN_BOCCALE_LASCIATO_INDIETRO(ClasseMissione.NESSUN_BOCCALE_LASCIATO_INDIETRO),
		DISTURBATORE_DELLA_QUIETE_PUBBLICA(ClasseMissione.DISTURBATORE_DELLA_QUIETE_PUBBLICA),
		CACCIA_AI_GOBLIN(ClasseMissione.CACCIA_AI_GOBLIN),
		RICHIESTA_ALCHIMISTA(ClasseMissione.RICHIESTA_ALCHIMISTA),
		RICHIESTA_ARMAIOLO(ClasseMissione.RICHIESTA_ARMAIOLO),
		RICHIESTA_CAPITANO(ClasseMissione.RICHIESTA_CAPITANO),
		LA_TAGLIA_SULLA_BANDA(ClasseMissione.LA_TAGLIA_SULLA_BANDA),
		IL_PELLEGRINO(ClasseMissione.IL_PELLEGRINO),
		IL_RAPIMENTO(ClasseMissione.IL_RAPIMENTO),
		NON_SPARATE_SUL_PIANISTA(ClasseMissione.NON_SPARATE_SUL_PIANISTA),
		CACCIATORE_DI_TAGLIE(ClasseMissione.CACCIATORE_DI_TAGLIE),
		IL_CARTOGRAFO(ClasseMissione.IL_CARTOGRAFO);

		TipoMissionePredefinita(ClasseMissione classeMissione) {
			this(classeMissione, false);
		}

		TipoMissionePredefinita(ClasseMissione classeMissione, boolean diProva) {
			this.classeMissione = classeMissione;
			this.diProva = diProva;
		}

		private final ClasseMissione classeMissione;
		private final boolean diProva;

		public Missione getIstanza() {
			return classeMissione.getIstanza();
		}

		public static boolean contieneMissione(String id) {
			return Arrays.stream(TipoMissionePredefinita.values()).anyMatch(m -> m.name().equals(id));
		}
	}

	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefinite = new EnumMap<>(TipoMissionePredefinita.class);
	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefiniteCompletate = new EnumMap<>(TipoMissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarie = new ArrayList<>();
	private static final List<Missione> elencoMissioniSecondarieCompletate = new ArrayList<>();
	// Le missioni concluse senza successo (vedi Missione.fallisciMissione)
	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefiniteFallite = new EnumMap<>(TipoMissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarieFallite = new ArrayList<>();

	/**
	 * Le locazioni rivendicate dalle missioni (vedi {@link #cerca}): per ogni coordinata l'id della missione che
	 * l'ha rivendicata per ultima. Un claim non si cancella mai, si sovrascrive: così la coordinata ricorda chi
	 * l'ha avuta anche dopo che la missione è finita. Non si salva a parte: si ricostruisce dalle proprietà delle
	 * missioni ({@code LOCAZIONE_OCCUPATA}).
	 */
	private static final Map<CoordinateMD, String> locazioniOccupate = new HashMap<>();
	// Il numero d'ordine del prossimo claim: dopo un caricamento, fra due missioni che hanno rivendicato la stessa
	// coordinata vince quella che l'ha fatto per ultima
	private static int prossimoClaim;
	private static final String LOCAZIONE_OCCUPATA = "LOCAZIONE_OCCUPATA";

	private static void pulisciElenchi() {
		locazioniOccupate.clear();
		prossimoClaim = 0;
		elencoMissioniPredefinite.clear();
		elencoMissioniPredefiniteCompletate.clear();
		elencoMissioniSecondarie.clear();
		elencoMissioniSecondarieCompletate.clear();
		elencoMissioniPredefiniteFallite.clear();
		elencoMissioniSecondarieFallite.clear();
	}

	/**
	 * Si iscrive agli eventi di gioco che le missioni a passi contano (vedi MissioneAPassi.registraEvento): gli
	 * avversari sconfitti e gli oggetti raccolti. Vanno a tutte le missioni non ancora finite.
	 */
	public static void registrati() {
		BusEventi.iscriviti(InternoRichiestaAperturaFinestraCombattimento.class,
				evento -> registraEvento(MissioneAPassi.EVENTO_COMBATTIMENTO, 1));
		BusEventi.iscriviti(InternoAvversarioSconfitto.class, evento -> {
			registraEvento(MissioneAPassi.eventoSconfitto(evento.getClasse()), 1);
			// Anche un avversario abbattuto da un incantesimo è un combattimento
			registraEvento(MissioneAPassi.EVENTO_COMBATTIMENTO, 1);
			// Anche con la casella in cui è successo: per i combattimenti di una missione in una locazione precisa
			CoordinateMD coordinate = GruppoGiocatore.getIstanza().getCoordinate();
			if (coordinate != null) {
				registraEvento(MissioneAPassi.eventoSconfittoIn(evento.getClasse(), coordinate), 1);
			}
		});
		BusEventi.iscriviti(InternoOggettoRaccolto.class,
				evento -> registraEvento(MissioneAPassi.eventoRaccolto(evento.getClasse()), evento.getQuantita()));
	}

	private static void registraEvento(String evento, int quantita) {
		for (Missione missione : getTutteLeMissioni()) {
			if (missione instanceof MissioneAPassi && !missione.isCompleta() && !missione.isFallita()) {
				((MissioneAPassi) missione).registraEvento(evento, quantita);
			}
		}
	}

	public static void reimposta() {
		RegistroMissioniMD md = getRegistroMissioni();
		md.reimposta();
		pulisciElenchi();

		for (TipoMissionePredefinita tipoMissionePredefinita : TipoMissionePredefinita.values()) {
			if (tipoMissionePredefinita.diProva && !ModalitaDiProva.isAttiva()) {
				continue;
			}
			Missione missione = tipoMissionePredefinita.getIstanza();
			missione.getModelloDati().setId(tipoMissionePredefinita.name());
			elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
			md.aggiungiMissione(tipoMissionePredefinita.name(), missione.getModelloDati());
		}
	}
	
	public static void aggiornaDopoRilettura() {
		RegistroMissioniMD md = getRegistroMissioni();
		pulisciElenchi();
		aggiornaDopoRiletturaImpl(md.getMissioniAttive());
		aggiornaDopoRiletturaImpl(md.getMissioniCompletate());
		ricostruisciLocazioniOccupate();
	}

	// --- Locazioni rivendicate dalle missioni (vedi gestione_missioni.md, §6-7)

	/**
	 * La missione rivendica la locazione in quelle coordinate, al posto di chiunque l'avesse prima. La missione
	 * ricorda la sua coordinata nella proprietà {@code LOCAZIONE_OCCUPATA}; ne rivendica una sola alla volta.
	 */
	public static void occupaLocazione(CoordinateMD coordinate, Missione missione) {
		missione.aggiungiProprieta(LOCAZIONE_OCCUPATA, coordinate.getX() + "," + coordinate.getY() + "," + prossimoClaim++);
		locazioniOccupate.put(coordinate, missione.getId());
	}

	/**
	 * La coordinata rivendicata dalla missione, o null.
	 */
	public static CoordinateMD getLocazioneOccupata(Missione missione) {
		String valore = missione.ottieniProprieta(LOCAZIONE_OCCUPATA);
		if (valore == null) {
			return null;
		}
		String[] parti = valore.split(",");
		return new CoordinateMD(Integer.parseInt(parti[0]), Integer.parseInt(parti[1]));
	}

	/**
	 * La missione che ha rivendicato per ultima quella coordinata, anche se nel frattempo è finita: per dire, per
	 * esempio, "qui sorgeva il castello della Strega".
	 */
	public static Optional<Missione> getMissioneCheHaOccupato(CoordinateMD coordinate) {
		String id = locazioniOccupate.get(coordinate);
		return id == null ? Optional.empty() : getTutteLeMissioni().stream().filter(m -> id.equals(m.getId())).findFirst();
	}

	/**
	 * Le caselle da far lampeggiare sulla mappa per le missioni: quelle rivendicate dalle missioni a passi attive e non
	 * ancora finite (il covo dei ladri, il tempio di una leggenda o di un pellegrino, il bosco di una banda, il posto
	 * del ripiego di una raccolta...), se il gruppo le conosce. I castelli delle missioni Sconfiggi* no.
	 */
	public static List<CoordinateMD> getLocazioniDaSegnalare() {
		List<CoordinateMD> caselle = new ArrayList<>();
		for (Missione missione : getTutteLeMissioni()) {
			if (missione instanceof MissioneAPassi && missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()) {
				CoordinateMD coordinate = getLocazioneOccupata(missione);
				if (coordinate != null && Foresta.isLocazioneConosciuta(coordinate)) {
					caselle.add(coordinate);
				}
			}
		}
		return caselle;
	}

	/**
	 * Una missione secondaria nuova, fuori dall'albero della missione principale (per esempio l'incarico in città
	 * che ne ripete uno finito): si controlla come le altre e si salva con il registro.
	 */
	public static void aggiungiMissioneSecondaria(Missione missione) {
		getRegistroMissioni().aggiungiMissione(missione.getId(), missione.getModelloDati());
		elencoMissioniSecondarie.add(missione);
	}

	/**
	 * La missione con quell'id, in qualunque stato.
	 */
	public static Optional<Missione> getMissione(String id) {
		return getTutteLeMissioni().stream().filter(m -> m.getId().equals(id)).findFirst();
	}

	/**
	 * L'oggetto che una missione vuole nella locazione in cui il gruppo sta entrando (vedi
	 * Missione.getOggettoInLocazione), se ce n'è uno: chiede prima alla missione che ha rivendicato la casella, poi
	 * alle altre in corso, e vince la prima che risponde.
	 */
	public static Optional<Oggetto> getOggettoMissione(CoordinateMD coordinate, ClassiLocazione classe, boolean visitata) {
		List<Missione> candidate = new ArrayList<>();
		getMissioneCheHaOccupato(coordinate).ifPresent(candidate::add);
		for (Missione missione : getTutteLeMissioni()) {
			if (!candidate.contains(missione)) {
				candidate.add(missione);
			}
		}
		for (Missione missione : candidate) {
			if (missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()) {
				Optional<Oggetto> oggetto = missione.getOggettoInLocazione(coordinate, classe, visitata);
				if (oggetto.isPresent()) {
					return oggetto;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Gli avversari che una missione in corso vuole nella locazione in cui il gruppo sta entrando (vedi
	 * Missione.getIncontroInLocazione), se ce ne sono: vince la prima missione che risponde.
	 */
	public static Optional<List<Personaggio>> getIncontroMissione(CoordinateMD coordinate) {
		for (Missione missione : getTutteLeMissioni()) {
			if (missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()) {
				Optional<List<Personaggio>> avversari = missione.getIncontroInLocazione(coordinate);
				if (avversari.isPresent()) {
					return avversari;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Il ricordo della missione finita che ha rivendicato per ultima quella casella, se ce n'è uno: la frase che
	 * legge chi ci entra, per esempio "Qui sorgeva il castello della Strega.".
	 */
	public static Optional<String> getRicordo(CoordinateMD coordinate) {
		return getMissioneCheHaOccupato(coordinate)
				.filter(missione -> missione.isCompleta() || missione.isFallita())
				.map(Missione::getRicordoDellaLocazione);
	}

	/**
	 * Cerca, a quadrati concentrici attorno a una casella a caso, una locazione che esiste già di quella classe
	 * (un BOSCO da trasformare in castello, un TEMPIO...) e che sia disponibile: nessuna missione l'ha rivendicata,
	 * oppure quella che l'ha fatto è finita, oppure è la missione stessa. Non si sceglie mai la casella in cui si
	 * trova il gruppo, per non cambiargli la locazione sotto i piedi. La coordinata trovata viene subito
	 * rivendicata per la missione. Vuoto se su tutta la mappa non ce n'è nessuna: la missione riproverà.
	 */
	public static Optional<CoordinateMD> cerca(ClassiLocazione richiesta, Missione missione) {
		return cerca(richiesta, missione, null);
	}

	/**
	 * Come {@link #cerca(ClassiLocazione, Missione)}, ma solo dentro il quadrante (su tutta la mappa se è null).
	 */
	public static Optional<CoordinateMD> cerca(ClassiLocazione richiesta, Missione missione, Quadrante quadrante) {
		CoordinateMD gruppo = GruppoGiocatore.getIstanza().getCoordinate();
		for (CoordinateMD coordinate : aQuadratiConcentrici(quadrante)) {
			if (!coordinate.equals(gruppo) && Foresta.getLocazione(coordinate) == richiesta && isDisponibile(coordinate, missione)) {
				occupaLocazione(coordinate, missione);
				return Optional.of(coordinate);
			}
		}
		return Optional.empty();
	}

	/**
	 * Le caselle della mappa, o del quadrante se non è null, a quadrati concentrici attorno a una casella a caso.
	 */
	private static List<CoordinateMD> aQuadratiConcentrici(Quadrante quadrante) {
		int dimensioneX = Foresta.getDimensioneX();
		int dimensioneY = Foresta.getDimensioneY();
		CoordinateMD centro = quadrante != null ? quadrante.getCoordinateACaso()
				: new CoordinateMD(Dado.tira(dimensioneX) - 1, Dado.tira(dimensioneY) - 1);
		int raggioMassimo = Math.max(dimensioneX, dimensioneY);
		List<CoordinateMD> caselle = new ArrayList<>();
		for (int raggio = 0; raggio <= raggioMassimo; raggio++) {
			for (CoordinateMD coordinate : bordo(centro.getX(), centro.getY(), raggio)) {
				if (coordinate.getX() >= 0 && coordinate.getX() < dimensioneX && coordinate.getY() >= 0 && coordinate.getY() < dimensioneY
						&& (quadrante == null || quadrante.contiene(coordinate))) {
					caselle.add(coordinate);
				}
			}
		}
		return caselle;
	}

	/**
	 * Come {@link #cerca}, ma se su tutta la mappa non c'è una locazione disponibile di quella classe se ne costruisce
	 * una nuova al posto di un bosco o di una palude disponibile, preferendo quelli già visitati, e la si rivendica
	 * per la missione come non ancora visitata. Non si tocca la casella del gruppo né una casella con un artefatto del
	 * registro. Vuoto solo se non c'è neanche un bosco o una palude da sostituire.
	 */
	public static Optional<CoordinateMD> cercaOCostruisci(ClassiLocazione richiesta, Missione missione) {
		return cercaOCostruisci(richiesta, missione, null);
	}

	/**
	 * Come {@link #cercaOCostruisci(ClassiLocazione, Missione)}, ma solo dentro il quadrante (su tutta la mappa se è
	 * null).
	 */
	public static Optional<CoordinateMD> cercaOCostruisci(ClassiLocazione richiesta, Missione missione, Quadrante quadrante) {
		Optional<CoordinateMD> trovata = cerca(richiesta, missione, quadrante);
		if (trovata.isPresent()) {
			return trovata;
		}
		Optional<CoordinateMD> sostituita = cercaDaSostituire(missione, quadrante, true);
		if (!sostituita.isPresent()) {
			sostituita = cercaDaSostituire(missione, quadrante, false);
		}
		sostituita.ifPresent(coordinate -> {
			Foresta.costruisciLocazione(coordinate, richiesta);
			occupaLocazione(coordinate, missione);
		});
		return sostituita;
	}

	private static Optional<CoordinateMD> cercaDaSostituire(Missione missione, Quadrante quadrante, boolean soloVisitate) {
		CoordinateMD gruppo = GruppoGiocatore.getIstanza().getCoordinate();
		for (CoordinateMD coordinate : aQuadratiConcentrici(quadrante)) {
			ClassiLocazione classe = Foresta.getLocazione(coordinate);
			if (!coordinate.equals(gruppo)
					&& (classe == ClassiLocazione.BOSCO || classe == ClassiLocazione.PALUDE)
					&& (!soloVisitate || Foresta.isLocazioneVisitata(coordinate))
					&& isDisponibile(coordinate, missione)
					&& RegistroArtefatti.getArtefattoInLocazione(coordinate) == null) {
				return Optional.of(coordinate);
			}
		}
		return Optional.empty();
	}

	/**
	 * Per una missione che si procura la propria locazione unica: cerca una casella di quella classe
	 * ({@link #cercaOCostruisci}), ci costruisce la locazione unica come non ancora visitata e ne restituisce la coordinata,
	 * o null se non ne ha trovata nessuna. Il castello di un alleato del Drago la cerca in un quadrante dove non ci
	 * sono altri castelli (vedi {@link #quadranteSenza}), così i quattro castelli finiscono uno per quadrante; quello
	 * del Drago, che arriva dopo, va dovunque.
	 */
	public static CoordinateMD rivendicaPerLocazioneUnica(ClassiLocazione locazioneUnica, ClassiLocazione suCasellaDi, Missione missione) {
		Quadrante quadrante = locazioneUnica.getTipoLocazione() == ClassiLocazione.TipoLocazione.CASTELLO
				&& locazioneUnica != ClassiLocazione.CASTELLO_DRAGO
				? quadranteSenza(ClassiLocazione.TipoLocazione.CASTELLO) : null;
		Optional<CoordinateMD> coordinate = cercaOCostruisci(suCasellaDi, missione, quadrante);
		if (!coordinate.isPresent()) {
			return null;
		}
		Foresta.costruisciLocazioneUnica(locazioneUnica, coordinate.get(), false);
		Foresta.setLocazioneVisitata(coordinate.get(), false);
		return coordinate.get();
	}

	/**
	 * Un quadrante a caso in cui non c'è nessuna locazione unica di quel tipo, o null se ce n'è in tutti.
	 */
	static Quadrante quadranteSenza(ClassiLocazione.TipoLocazione tipo) {
		List<Quadrante> liberi = Quadrante.inOrdineCasuale();
		for (ClassiLocazione classe : ClassiLocazione.values()) {
			if (classe.getTipoLocazione() == tipo && classe.isLocazioneUnica()) {
				CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(classe);
				if (coordinate != null) {
					liberi.remove(Quadrante.di(coordinate));
				}
			}
		}
		return liberi.isEmpty() ? null : liberi.get(0);
	}

	private static boolean isDisponibile(CoordinateMD coordinate, Missione richiedente) {
		String id = locazioniOccupate.get(coordinate);
		if (id == null || id.equals(richiedente.getId())) {
			return true;
		}
		Optional<Missione> proprietaria = getMissioneCheHaOccupato(coordinate);
		return !proprietaria.isPresent() || proprietaria.get().isCompleta() || proprietaria.get().isFallita();
	}

	/**
	 * Le celle a distanza esattamente {@code raggio} (di Chebyshev) dal centro, dal lato nord in senso orario; il
	 * centro stesso per raggio 0. Possono uscire dalla mappa: le scarta chi le usa.
	 */
	static List<CoordinateMD> bordo(int x0, int y0, int raggio) {
		List<CoordinateMD> celle = new ArrayList<>();
		if (raggio == 0) {
			celle.add(new CoordinateMD(x0, y0));
			return celle;
		}
		for (int x = x0 - raggio; x < x0 + raggio; x++) {
			celle.add(new CoordinateMD(x, y0 - raggio));
		}
		for (int y = y0 - raggio; y < y0 + raggio; y++) {
			celle.add(new CoordinateMD(x0 + raggio, y));
		}
		for (int x = x0 + raggio; x > x0 - raggio; x--) {
			celle.add(new CoordinateMD(x, y0 + raggio));
		}
		for (int y = y0 + raggio; y > y0 - raggio; y--) {
			celle.add(new CoordinateMD(x0 - raggio, y));
		}
		return celle;
	}

	/**
	 * Dopo un caricamento: i claim si rileggono dalle proprietà delle missioni, nell'ordine in cui sono stati fatti.
	 */
	private static void ricostruisciLocazioniOccupate() {
		List<String[]> claim = new ArrayList<>();
		for (Missione missione : getTutteLeMissioni()) {
			String valore = missione.ottieniProprieta(LOCAZIONE_OCCUPATA);
			if (valore != null) {
				String[] parti = valore.split(",");
				claim.add(new String[] {parti[0], parti[1], parti[2], missione.getId()});
			}
		}
		claim.sort(Comparator.comparingInt(c -> Integer.parseInt(c[2])));
		for (String[] c : claim) {
			locazioniOccupate.put(new CoordinateMD(Integer.parseInt(c[0]), Integer.parseInt(c[1])), c[3]);
			prossimoClaim = Integer.parseInt(c[2]) + 1;
		}
	}

	private static void aggiornaDopoRiletturaImpl(Collection<MissioneMD> missioni) {
		for (MissioneMD missioneMD : missioni) {
			if (TipoMissionePredefinita.contieneMissione(missioneMD.getId())) {
				TipoMissionePredefinita tipoMissionePredefinita = TipoMissionePredefinita.valueOf(missioneMD.getId());
				Missione missione = ricostruisci(tipoMissionePredefinita.getIstanza(), missioneMD);
				if (missione.isFallita()) {
					elencoMissioniPredefiniteFallite.put(tipoMissionePredefinita, missione);
				} else if (missione.isCompleta()) {
					elencoMissioniPredefiniteCompletate.put(tipoMissionePredefinita, missione);
				} else {
					elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
				}
			} else {
				Missione missione = ricostruisci(missioneMD);
				if (missione.isFallita()) {
					elencoMissioniSecondarieFallite.add(missione);
				} else if (missione.isCompleta()) {
					elencoMissioniSecondarieCompletate.add(missione);
				} else {
					elencoMissioniSecondarie.add(missione);
				}
			}
		}
	}

	/**
	 * Ricrea l'oggetto di dominio del nodo, della classe dichiarata nel modello dati,
	 * e ricorre sui figli.
	 */
	private static Missione ricostruisci(MissioneMD missioneMD) {
		return ricostruisci(missioneMD.getClasse().getIstanza(), missioneMD);
	}

	private static Missione ricostruisci(Missione missione, MissioneMD missioneMD) {
		missione.setModelloDati(missioneMD);
		List<Missione> missioniSecondarie = new ArrayList<>();
		for (MissioneMD missioneSecondariaMD : missioneMD.getMissioniMD()) {
			missioniSecondarie.add(ricostruisci(missioneSecondariaMD));
		}
		missione.sostituisciMissioniSecondarie(missioniSecondarie);
		return missione;
	}

	/**
	 * Riporta le missioni di primo livello non completate, attive e da attivare. La discesa
	 * nell'albero è a carico del chiamante.
	 */
	public static List<Missione> getMissioniNonCompletate() {
        List<Missione> missioni = new ArrayList<>(elencoMissioniPredefinite.values());
		missioni.addAll(elencoMissioniSecondarie);
		return missioni;
	}

	/**
	 * Ogni missione dell'albero, una volta sola e in qualunque stato (da attivare, attiva, completata, fallita),
	 * sotto-missioni comprese: per chi cerca qualcosa che una missione ha lasciato in sospeso anche dopo essersi
	 * conclusa, come l'intermezzo del suo ultimo passo (vedi RegistroIntermezzi).
	 */
	public static List<Missione> getTutteLeMissioni() {
		List<Missione> radici = new ArrayList<>(getMissioniNonCompletate());
		radici.addAll(elencoMissioniPredefiniteCompletate.values());
		radici.addAll(elencoMissioniSecondarieCompletate);
		radici.addAll(getMissioniFallite());
		Set<Missione> viste = Collections.newSetFromMap(new IdentityHashMap<>());
		List<Missione> missioni = new ArrayList<>();
		for (Missione radice : radici) {
			aggiungiConSottoMissioni(radice, viste, missioni);
		}
		return missioni;
	}

	private static void aggiungiConSottoMissioni(Missione missione, Set<Missione> viste, List<Missione> missioni) {
		if (viste.add(missione)) {
			missioni.add(missione);
			for (Missione missioneSecondaria : missione.getMissioniSecondarie()) {
				aggiungiConSottoMissioni(missioneSecondaria, viste, missioni);
			}
		}
	}

	/**
	 * Riporta solo le missioni attive.
	 */
	public static List<Missione> getMissioniAttive() {
		List<Missione> missioni = new ArrayList<>();
		elencoMissioniPredefinite.values().stream().filter(Missione::isAttiva).forEach(missioni::add);
		elencoMissioniSecondarie.stream().filter(Missione::isAttiva).forEach(missioni::add);
		return missioni;
	}

	/**
	 * La missione predefinita indicata è attualmente attiva (accettata e non ancora completata
	 * né fallita)?
	 */
	public static boolean isMissioneAttiva(TipoMissionePredefinita tipoMissionePredefinita) {
		return elencoMissioniPredefinite.containsKey(tipoMissionePredefinita);
	}

	/**
	 * Le missioni di primo livello completate, seguite dalle sotto-missioni completate di quelle ancora in corso.
	 * Quando si completa anche la missione che le contiene, le sotto-missioni non compaiono più qui da sole ma
	 * sotto di lei, perché la missione passa tra le completate con tutto il suo albero.
	 */
	public static List<Missione> getMissioniCompletate() {
		List<Missione> missioni = new ArrayList<>(elencoMissioniPredefiniteCompletate.values());
		missioni.addAll(elencoMissioniSecondarieCompletate);
		for (Missione missione : getMissioniNonCompletate()) {
			aggiungiSottoMissioniCompletate(missione, missioni);
		}
		return missioni;
	}

	private static void aggiungiSottoMissioniCompletate(Missione missione, List<Missione> completate) {
		for (Missione missioneSecondaria : missione.getMissioniSecondarie()) {
			if (missioneSecondaria.isCompleta()) {
				completate.add(missioneSecondaria);
			} else if (!missioneSecondaria.isFallita()) {
				aggiungiSottoMissioniCompletate(missioneSecondaria, completate);
			}
		}
	}

	/**
	 * Le missioni di primo livello concluse senza successo.
	 */
	public static List<Missione> getMissioniFallite() {
		List<Missione> missioni = new ArrayList<>(elencoMissioniPredefiniteFallite.values());
		missioni.addAll(elencoMissioniSecondarieFallite);
		return missioni;
	}

	/**
	 * Sposta tra le fallite una missione di primo livello. Una sotto-missione fallita resta dov'e', dentro la sua
	 * missione, con la sua proprieta' FALLITA.
	 */
	public static void fallisciMissione(Missione missione) {
		if (TipoMissionePredefinita.contieneMissione(missione.getId())) {
			TipoMissionePredefinita tipoMissione = TipoMissionePredefinita.valueOf(missione.getId());
			elencoMissioniPredefinite.remove(tipoMissione);
			elencoMissioniPredefiniteFallite.put(tipoMissione, missione);
		} else if (elencoMissioniSecondarie.removeIf(missione::equals)) {
			elencoMissioniSecondarieFallite.add(missione);
		}
	}

	/**
	 * La missione principale, attiva o gia' completata: completandola (drago sconfitto) passa tra le completate,
	 * ma e' proprio allora che l'Automa chiede se lo sia per decidere tra vittoria e sconfitta.
	 */
	public static SconfiggiIlDrago getMissionePrincipale() {
		Missione missione = elencoMissioniPredefinite.get(TipoMissionePredefinita.SCONFIGGI_IL_DRAGO);
		if (missione == null) {
			missione = elencoMissioniPredefiniteCompletate.get(TipoMissionePredefinita.SCONFIGGI_IL_DRAGO);
		}
		return (SconfiggiIlDrago) missione;
	}

	/**
	 * Sposta tra le completate una missione di primo livello. Una sotto-missione completata resta dentro la sua
	 * missione, con la sua proprieta' COMPLETA (vedi getMissioniCompletate).
	 */
	public static void completaMissione(Missione missione) {
		if (TipoMissionePredefinita.contieneMissione(missione.getId())) {
			TipoMissionePredefinita tipoMissione = TipoMissionePredefinita.valueOf(missione.getId());
			elencoMissioniPredefinite.remove(tipoMissione);
			elencoMissioniPredefiniteCompletate.put(tipoMissione, missione);
		} else if (elencoMissioniSecondarie.removeIf(missione::equals)) {
			elencoMissioniSecondarieCompletate.add(missione);
		}
	}
}
