package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tools.ModalitaDiProva;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.missioni.SconfiggiIlDrago;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.RegistroMissioniMD;

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
		DISTURBATORE_DELLA_QUIETE_PUBBLICA(ClasseMissione.DISTURBATORE_DELLA_QUIETE_PUBBLICA);

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
		BusEventi.iscriviti(InternoAvversarioSconfitto.class,
				evento -> registraEvento(MissioneAPassi.eventoSconfitto(evento.getClasse()), 1));
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
		int dimensioneX = Foresta.getDimensioneX();
		int dimensioneY = Foresta.getDimensioneY();
		int x0 = Dado.tira(dimensioneX) - 1;
		int y0 = Dado.tira(dimensioneY) - 1;
		CoordinateMD gruppo = GruppoGiocatore.getIstanza().getCoordinate();
		int raggioMassimo = Math.max(dimensioneX, dimensioneY);
		for (int raggio = 0; raggio <= raggioMassimo; raggio++) {
			for (CoordinateMD coordinate : bordo(x0, y0, raggio)) {
				if (coordinate.getX() < 0 || coordinate.getX() >= dimensioneX || coordinate.getY() < 0 || coordinate.getY() >= dimensioneY
						|| coordinate.equals(gruppo) || Foresta.getLocazione(coordinate) != richiesta || !isDisponibile(coordinate, missione)) {
					continue;
				}
				occupaLocazione(coordinate, missione);
				return Optional.of(coordinate);
			}
		}
		return Optional.empty();
	}

	/**
	 * Per una missione che si procura la propria locazione unica: cerca una casella di quella classe
	 * ({@link #cerca}), ci costruisce la locazione unica come non ancora visitata e ne restituisce la coordinata,
	 * o null se non ne ha trovata nessuna.
	 */
	public static CoordinateMD rivendicaPerLocazioneUnica(ClassiLocazione locazioneUnica, ClassiLocazione suCasellaDi, Missione missione) {
		Optional<CoordinateMD> coordinate = cerca(suCasellaDi, missione);
		if (!coordinate.isPresent()) {
			return null;
		}
		Foresta.costruisciLocazioneUnica(locazioneUnica, coordinate.get(), false);
		Foresta.setLocazioneVisitata(coordinate.get(), false);
		return coordinate.get();
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
