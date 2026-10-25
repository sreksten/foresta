package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.CostruttoreDellaForesta;

import com.threeamigos.foresta.combattimento.Ondata;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoPassaggioInosservato;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.missioni.incarichi.IlNegozio;
import com.threeamigos.foresta.missioni.incarichi.SceltaDegliIncarichi;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.modellodati.MissioneMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.RegistroMissioniMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Quadrante;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.mappa.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.mappa.TipoLocazione;

import java.util.*;

/**
 * Le missioni della partita: quelle predefinite ({@link MissionePredefinita#isPredefinita}), in tre elenchi (in corso, completate, fallite), e le secondarie nate durante il gioco, anch'esse
 * in tre elenchi. Le controlla a ogni locazione (prima, durante, dopo), consegna loro gli eventi di gioco (avversari sconfitti, oggetti raccolti, combattimenti) e tiene i
 * conti delle locazioni che le missioni rivendicano, segnano sulla mappa o svuotano, e delle ricerche di una casella adatta (vedi gestione_missioni.md, §6-7). I dati stanno nel
 * {@code RegistroMissioniMD} e si salvano con la partita.
 */
public class RegistroMissioni {

	private static RegistroMissioniMD getRegistroMissioni() {
		return ModelloDati.getIstanza().getRegistroMissioniMD();
	}

	private RegistroMissioni() {
	}

	private static final Map<MissionePredefinita, Missione> elencoMissioniPredefinite = new EnumMap<>(MissionePredefinita.class);
	private static final Map<MissionePredefinita, Missione> elencoMissioniPredefiniteCompletate = new EnumMap<>(MissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarie = new ArrayList<>();
	private static final List<Missione> elencoMissioniSecondarieCompletate = new ArrayList<>();
	// Le missioni concluse senza successo (vedi Missione.fallisciMissione)
	private static final Map<MissionePredefinita, Missione> elencoMissioniPredefiniteFallite = new EnumMap<>(MissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarieFallite = new ArrayList<>();

	/**
	 * Le locazioni rivendicate dalle missioni (vedi {@link #cerca}): per ogni coordinata l'id della missione che
	 * l'ha rivendicata per ultima. Un claim non si cancella mai, si sovrascrive: così la coordinata ricorda chi
	 * l'ha avuta anche dopo che la missione è finita. Non si salva a parte: si ricostruisce dalle proprietà delle
	 * missioni ({@code LOCAZIONE_OCCUPATA} e {@code LOCAZIONI_GIA_OCCUPATE}).
	 */
	private static final Map<CoordinateMD, String> locazioniOccupate = new HashMap<>();
	// Il numero d'ordine del prossimo claim: dopo un caricamento, fra due missioni che hanno rivendicato la stessa
	// coordinata vince quella che l'ha fatto per ultima
	private static int prossimoClaim;
	private static final String LOCAZIONE_OCCUPATA = "LOCAZIONE_OCCUPATA";
	// I claim che la missione ha lasciato per rivendicare un altro posto, "x,y,numero" separati da ';': servono per
	// ricostruire dopo un caricamento anche i posti che la missione ha avuto prima di quello di adesso
	private static final String LOCAZIONI_GIA_OCCUPATE = "LOCAZIONI_GIA_OCCUPATE";

	/**
	 * Le caselle che le missioni in corso fanno lampeggiare sulla mappa perché ci si deve andare (vedi
	 * {@link #segnalaLocazione}): per ogni missione la casella, e più missioni possono segnalare la stessa. È una cache
	 * di quanto le missioni scrivono nella proprietà {@code LOCAZIONE_SEGNALATA}, e si ricostruisce da lì dopo un
	 * caricamento: serve a non ricalcolare i passi di ogni missione a ogni fotogramma.
	 */
	private static final Map<String, CoordinateMD> locazioniSegnalate = new HashMap<>();
	private static final String LOCAZIONE_SEGNALATA = "LOCAZIONE_SEGNALATA";

	/**
	 * La coordinata, se non null, dove la prossima {@code crea()} non deve generare né avversari né oggetti: una
	 * missione (LaBenedizione, IlPellegrino, IlRituale) vi conclude al suo arrivo, in un tempio o un posto che
	 * considera sicuro (un pellegrino non andrebbe in un tempio infestato). Si consuma una volta sola, vedi
	 * {@link #isDaSopprimere}.
	 */
	private static CoordinateMD coordinataSenzaContenuto;

	private static void pulisciElenchi() {
		SceltaDegliIncarichi.dimentica();
		locazioniOccupate.clear();
		locazioniSegnalate.clear();
		prossimoClaim = 0;
		elencoMissioniPredefinite.clear();
		elencoMissioniPredefiniteCompletate.clear();
		elencoMissioniSecondarie.clear();
		elencoMissioniSecondarieCompletate.clear();
		elencoMissioniPredefiniteFallite.clear();
		elencoMissioniSecondarieFallite.clear();
		coordinataSenzaContenuto = null;
	}

	/**
	 * Si iscrive agli eventi di gioco che le missioni a passi contano (vedi MissioneAPassi.registraEvento): gli
	 * avversari sconfitti e gli oggetti raccolti. Vanno a tutte le missioni non ancora finite.
	 */
	public static void registrati() {
		BusEventi.iscriviti(InternoRichiestaAperturaFinestraCombattimento.class, evento -> {
			registraEvento(MissioneAPassi.EVENTO_COMBATTIMENTO, 1);
			registraCombattimentoQui();
		});
		BusEventi.iscriviti(InternoAvversarioSconfitto.class, evento -> {
			registraEvento(MissioneAPassi.eventoSconfitto(evento.getClasse()), 1);
			// Anche un avversario abbattuto da un incantesimo è un combattimento
			registraEvento(MissioneAPassi.EVENTO_COMBATTIMENTO, 1);
			registraCombattimentoQui();
			// Anche con la casella in cui è successo: per i combattimenti di una missione in una locazione precisa
			CoordinateMD coordinate = GruppoGiocatore.getIstanza().getCoordinate();
			if (coordinate != null) {
				registraEvento(MissioneAPassi.eventoSconfittoIn(evento.getClasse(), coordinate), 1);
			}
		});
		BusEventi.iscriviti(InternoPassaggioInosservato.class, evento -> {
			CoordinateMD coordinate = GruppoGiocatore.getIstanza().getCoordinate();
			if (coordinate != null) {
				registraEvento(MissioneAPassi.eventoInosservatoIn(coordinate), 1);
			}
		});
		BusEventi.iscriviti(InternoOggettoRaccolto.class,
				evento -> registraEvento(MissioneAPassi.eventoRaccolto(evento.getClasse()), evento.getQuantita()));
	}

	/**
	 * Un combattimento, con la casella in cui c'è stato: per le missioni che vietano di combattere in un posto preciso.
	 */
	private static void registraCombattimentoQui() {
		CoordinateMD coordinate = GruppoGiocatore.getIstanza().getCoordinate();
		if (coordinate != null) {
			registraEvento(MissioneAPassi.eventoCombattimentoIn(coordinate), 1);
		}
	}

	private static void registraEvento(String evento, int quantita) {
		for (Missione missione : getTutteLeMissioni()) {
			if (missione instanceof MissioneAPassi && !missione.isCompleta() && !missione.isFallita()) {
				((MissioneAPassi) missione).registraEvento(evento, quantita);
			}
		}
	}

	/**
	 * Ricomincia da capo: dimentica le missioni e crea una missione per ogni predefinita.
	 */
	public static void reimposta() {
		RegistroMissioniMD md = getRegistroMissioni();
		md.reimposta();
		pulisciElenchi();

		for (MissionePredefinita classe : MissionePredefinita.values()) {
			if (!classe.isPredefinita()) {
				continue;
			}
			Missione missione = FabbricaMissioni.crea(classe);
			missione.getModelloDati().setId(classe.name());
			elencoMissioniPredefinite.put(classe, missione);
			md.aggiungiMissione(classe.name(), missione.getModelloDati());
		}
		IlNegozio.assegnaLeCitta(elencoMissioniPredefinite.values());
	}
	
	/**
	 * Dopo aver caricato un salvataggio ricostruisce le missioni dai dati letti e rimette ogni locazione rivendicata o segnalata.
	 */
	public static void aggiornaDopoRilettura() {
		RegistroMissioniMD md = getRegistroMissioni();
		pulisciElenchi();
		aggiornaDopoRiletturaImpl(md.getMissioni());
		ricostruisciLocazioniOccupate();
		ricostruisciLocazioniSegnalate();
	}

	// --- Locazioni rivendicate dalle missioni (vedi gestione_missioni.md, §6-7)

	/**
	 * La missione rivendica la locazione in quelle coordinate, al posto di chiunque l'avesse prima. La missione
	 * ricorda la sua coordinata nella proprietà {@code LOCAZIONE_OCCUPATA}; ne rivendica una sola alla volta, e quella che
	 * lascia passa in {@code LOCAZIONI_GIA_OCCUPATE}, così dopo un caricamento il posto ricorda ancora la missione.
	 */
	public static void occupaLocazione(CoordinateMD coordinate, Missione missione) {
		String precedente = missione.ottieniProprieta(LOCAZIONE_OCCUPATA);
		if (precedente != null) {
			String giaOccupate = missione.ottieniProprieta(LOCAZIONI_GIA_OCCUPATE);
			missione.aggiungiProprieta(LOCAZIONI_GIA_OCCUPATE, giaOccupate == null ? precedente : giaOccupate + ";" + precedente);
		}
		missione.aggiungiProprieta(LOCAZIONE_OCCUPATA, coordinate.getX() + "," + coordinate.getY() + "," + prossimoClaim++);
		locazioniOccupate.put(coordinate, missione.getId());
	}

	/**
	 * La missione chiede di segnare sulla mappa la casella dove ci si deve andare (la città in cui tornare, quella a cui
	 * portare qualcosa), e di far sapere al gruppo dov'è. Ne segna una sola alla volta: una nuova prende il posto della
	 * precedente. Più missioni possono segnare la stessa casella. Non cambia chi occupa la locazione (vedi
	 * {@link #occupaLocazione}): la casella non si rivendica, e il suo contenuto resta quello di sempre.
	 * Il segnalino sparisce con {@link #togliSegnalino}, o da solo quando la missione finisce.
	 */
	public static void segnalaLocazione(Missione missione, CoordinateMD coordinate) {
		if (coordinate == null) {
			togliSegnalino(missione);
			return;
		}
		if (coordinate.equals(locazioniSegnalate.get(missione.getId()))) {
			return;
		}
		locazioniSegnalate.put(missione.getId(), coordinate);
		missione.aggiungiProprieta(LOCAZIONE_SEGNALATA, coordinate.getX() + "," + coordinate.getY());
		Foresta.setLocazioneConosciuta(coordinate);
	}

	/**
	 * La missione non ha più bisogno del segnalino (se ce l'aveva).
	 */
	public static void togliSegnalino(Missione missione) {
		if (locazioniSegnalate.remove(missione.getId()) != null) {
			missione.rimuoviProprieta(LOCAZIONE_SEGNALATA);
		}
	}

	/**
	 * La casella che la missione sta segnando, o null.
	 */
	public static CoordinateMD getLocazioneSegnalata(Missione missione) {
		return locazioniSegnalate.get(missione.getId());
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
	 * Una missione (LaBenedizione, IlPellegrino, IlRituale) segnala che è appena arrivata, nello stesso controllo
	 * PRE_LOCAZIONE in cui lo scopre, al suo tempio o posto: lì non deve comparire niente a caso, né avversari né
	 * oggetti, perché per la missione quel posto è sicuro. Chiamare da un {@code aOgniControllo} del passo
	 * d'arrivo, non dalla sua azione: il passo potrebbe concludersi, ed eventualmente incatenarsi ad altri passi,
	 * nello stesso controllo in cui arriva, prima che {@code Automa} arrivi a consumare il segnale.
	 */
	public static void sopprimiContenutoLocazione(CoordinateMD coordinate) {
		coordinataSenzaContenuto = coordinate;
	}

	/**
	 * Se quella coordinata è quella appena segnalata da {@link #sopprimiContenutoLocazione}: consuma il segnale,
	 * che non vale più per i prossimi controlli né per altre coordinate.
	 */
	public static boolean isDaSopprimere(CoordinateMD coordinate) {
		boolean soppressa = coordinataSenzaContenuto != null && coordinataSenzaContenuto.equals(coordinate);
		coordinataSenzaContenuto = null;
		return soppressa;
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
			if (missione instanceof MissioneAPassi && isInCorso(missione) && !haUnAltroSegnalino(missione, getLocazioneOccupata(missione))) {
				CoordinateMD coordinate = getLocazioneOccupata(missione);
				if (coordinate != null && Foresta.isLocazioneConosciuta(coordinate)) {
					caselle.add(coordinate);
				}
			}
			// Il posto dove la missione dice di andare, anche se non l'ha rivendicato (una città, una locanda)
			CoordinateMD segnalata = locazioniSegnalate.get(missione.getId());
			if (segnalata != null && isInCorso(missione) && Foresta.isLocazioneConosciuta(segnalata)) {
				caselle.add(segnalata);
			}
		}
		return caselle;
	}

	/**
	 * Se il passo corrente della missione segna una casella diversa da quella rivendicata (la città in cui tornare, dopo
	 * aver sconfitto i nemici nel covo): allora il posto rivendicato ha fatto il suo e non lampeggia più.
	 */
	private static boolean haUnAltroSegnalino(Missione missione, CoordinateMD rivendicata) {
		CoordinateMD segnalata = locazioniSegnalate.get(missione.getId());
		return segnalata != null && !segnalata.equals(rivendicata);
	}

	private static boolean isInCorso(Missione missione) {
		return missione.isAttiva() && !missione.isCompleta() && !missione.isFallita();
	}

	/**
	 * I nomi delle missioni in corso per cui quella coordinata lampeggia (vedi {@link #getLocazioniDaSegnalare}): quella
	 * che l'ha rivendicata e tutte quelle che la segnano, anche in più di una per la stessa città. Vuota se la casella
	 * lampeggia solo per un artefatto di cui si è saputo.
	 */
	public static List<String> getNomiMissioniDaSegnalare(CoordinateMD coordinate) {
		List<String> nomi = new ArrayList<>();
		getMissioneCheHaOccupato(coordinate)
				.filter(missione -> missione instanceof MissioneAPassi && isInCorso(missione)
						&& coordinate.equals(getLocazioneOccupata(missione)) && !haUnAltroSegnalino(missione, coordinate))
				.ifPresent(missione -> nomi.add(missione.getNome()));
		for (Missione missione : getTutteLeMissioni()) {
			if (coordinate.equals(locazioniSegnalate.get(missione.getId())) && isInCorso(missione)
					&& !nomi.contains(missione.getNome())) {
				nomi.add(missione.getNome());
			}
		}
		return nomi;
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
	public static Optional<Bottino> getOggettoMissione(CoordinateMD coordinate, TipoLocazione tipo, boolean visitata) {
		List<Missione> candidate = new ArrayList<>();
		getMissioneCheHaOccupato(coordinate).ifPresent(candidate::add);
		for (Missione missione : getTutteLeMissioni()) {
			if (!candidate.contains(missione)) {
				candidate.add(missione);
			}
		}
		for (Missione missione : candidate) {
			if (missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()) {
				Optional<Bottino> oggetto = missione.getOggettoInLocazione(coordinate, tipo, visitata);
				if (oggetto.isPresent()) {
					return oggetto;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Se una missione in corso vuole quella locazione senza oggetti (vedi Missione.isLocazioneSenzaOggetti).
	 */
	public static boolean isLocazioneSenzaOggetti(CoordinateMD coordinate) {
		return getTutteLeMissioni().stream()
				.anyMatch(m -> m.isAttiva() && !m.isCompleta() && !m.isFallita() && m.isLocazioneSenzaOggetti(coordinate));
	}

	/**
	 * Gli avversari che una missione in corso vuole nella locazione in cui il gruppo sta entrando (vedi
	 * Missione.getIncontroInLocazione), se ce ne sono: vince la prima missione che risponde.
	 */
	public static Optional<List<Personaggio>> getIncontroMissione(CoordinateMD coordinate) {
		return getMissioneConIncontro(coordinate).flatMap(missione -> missione.getIncontroInLocazione(coordinate));
	}

	/**
	 * Le ondate che arrivano dopo gli avversari di {@link #getIncontroMissione}, dalla stessa missione (vedi
	 * Missione.getOndateSuccessiveInLocazione): vuota se non ce ne sono.
	 */
	public static List<Ondata> getOndateSuccessiveMissione(CoordinateMD coordinate) {
		return getMissioneConIncontro(coordinate).map(missione -> missione.getOndateSuccessiveInLocazione(coordinate))
				.orElse(Collections.emptyList());
	}

	/**
	 * La prima missione in corso che vuole degli avversari in quella locazione, se ce n'è una.
	 */
	private static Optional<Missione> getMissioneConIncontro(CoordinateMD coordinate) {
		for (Missione missione : getTutteLeMissioni()) {
			if (missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()
					&& missione.getIncontroInLocazione(coordinate).isPresent()) {
				return Optional.of(missione);
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
	 * oppure quella che l'ha fatto è finita, oppure è la missione stessa. Mai una casella con un artefatto del
	 * registro (un tempio così non è quasi certamente quello giusto: tenuto da un nido di viverne, non da chi ci
	 * manda la missione). Non si sceglie mai la casella in cui si trova il gruppo, per non cambiargli la locazione
	 * sotto i piedi. La coordinata trovata viene subito rivendicata per la missione; se è un TEMPIO, anche segnata
	 * come non ancora visitata, per farla trovare come la prima volta. Vuoto se su tutta la mappa non ce n'è
	 * nessuna: la missione riproverà.
	 */
	public static Optional<CoordinateMD> cerca(TipoLocazione richiesta, Missione missione) {
		return cerca(richiesta, missione, null);
	}

	/**
	 * Come {@link #cerca(TipoLocazione, Missione)}, ma solo dentro il quadrante (su tutta la mappa se è null).
	 */
	public static Optional<CoordinateMD> cerca(TipoLocazione richiesta, Missione missione, Quadrante quadrante) {
		CoordinateMD gruppo = GruppoGiocatore.getIstanza().getCoordinate();
		for (CoordinateMD coordinate : aQuadratiConcentrici(quadrante)) {
			if (!coordinate.equals(gruppo) && Foresta.getLocazione(coordinate) == richiesta && isDisponibile(coordinate, missione)
					&& RegistroArtefatti.getArtefattoInLocazione(coordinate) == null) {
				occupaLocazione(coordinate, missione);
				if (richiesta == TipoLocazione.TEMPIO) {
					Foresta.setLocazioneVisitata(coordinate, false);
				}
				return Optional.of(coordinate);
			}
		}
		return Optional.empty();
	}

	/**
	 * Se su tutta la mappa c'è un posto per quella classe di locazione, senza cambiare niente: una casella disponibile di
	 * quella classe o, per costruirla, un bosco o una palude disponibile (vedi {@link #cercaOCostruisci}).
	 */
	public static boolean esisteUnPostoPer(TipoLocazione richiesta, Missione missione) {
		CoordinateMD gruppo = GruppoGiocatore.getIstanza().getCoordinate();
		for (CoordinateMD coordinate : aQuadratiConcentrici(null)) {
			TipoLocazione tipo = Foresta.getLocazione(coordinate);
			if (!coordinate.equals(gruppo)
					&& (tipo == richiesta || tipo == TipoLocazione.BOSCO || tipo == TipoLocazione.PALUDE)
					&& isDisponibile(coordinate, missione)
					&& RegistroArtefatti.getArtefattoInLocazione(coordinate) == null) {
				return true;
			}
		}
		return false;
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
	public static Optional<CoordinateMD> cercaOCostruisci(TipoLocazione richiesta, Missione missione) {
		return cercaOCostruisci(richiesta, missione, null);
	}

	/**
	 * Come {@link #cercaOCostruisci(TipoLocazione, Missione)}, ma solo dentro il quadrante (su tutta la mappa se è
	 * null).
	 */
	public static Optional<CoordinateMD> cercaOCostruisci(TipoLocazione richiesta, Missione missione, Quadrante quadrante) {
		Optional<CoordinateMD> trovata = cerca(richiesta, missione, quadrante);
		if (trovata.isPresent()) {
			return trovata;
		}
		Optional<CoordinateMD> sostituita = cercaDaSostituire(missione, quadrante, true);
		if (!sostituita.isPresent()) {
			sostituita = cercaDaSostituire(missione, quadrante, false);
		}
		sostituita.ifPresent(coordinate -> {
			CostruttoreDellaForesta.costruisciLocazione(coordinate, richiesta);
			occupaLocazione(coordinate, missione);
		});
		return sostituita;
	}

	private static Optional<CoordinateMD> cercaDaSostituire(Missione missione, Quadrante quadrante, boolean soloVisitate) {
		CoordinateMD gruppo = GruppoGiocatore.getIstanza().getCoordinate();
		for (CoordinateMD coordinate : aQuadratiConcentrici(quadrante)) {
			TipoLocazione tipo = Foresta.getLocazione(coordinate);
			if (!coordinate.equals(gruppo)
					&& (tipo == TipoLocazione.BOSCO || tipo == TipoLocazione.PALUDE)
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
	public static CoordinateMD rivendicaPerLocazioneUnica(TipoLocazione locazioneUnica, TipoLocazione suCasellaDi, Missione missione) {
		Quadrante quadrante = locazioneUnica.getCategoriaLocazione() == CategoriaLocazione.CASTELLO
				&& locazioneUnica != TipoLocazione.CASTELLO_DRAGO
				? quadranteSenza(CategoriaLocazione.CASTELLO) : null;
		Optional<CoordinateMD> coordinate = cercaOCostruisci(suCasellaDi, missione, quadrante);
		if (!coordinate.isPresent()) {
			return null;
		}
		Foresta.costruisciLocazioneUnica(locazioneUnica, coordinate.get(), false);
		Foresta.setLocazioneVisitata(coordinate.get(), false);
		return coordinate.get();
	}

	/**
	 * Un quadrante a caso in cui non c'è nessuna locazione unica di quella categoria, o null se ce n'è in tutti.
	 */
	static Quadrante quadranteSenza(CategoriaLocazione categoria) {
		List<Quadrante> liberi = Quadrante.inOrdineCasuale();
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoriaLocazione() == categoria && tipo.isLocazioneUnica()) {
				CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(tipo);
				if (coordinate != null) {
					liberi.remove(Quadrante.di(coordinate));
				}
			}
		}
		return liberi.isEmpty() ? null : liberi.get(0);
	}

	/**
	 * Se la casella è libera per la missione: nessun'altra missione in corso l'ha rivendicata.
	 */
	public static boolean isDisponibile(CoordinateMD coordinate, Missione richiedente) {
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
	public static List<CoordinateMD> bordo(int x0, int y0, int raggio) {
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
	 * Dopo un caricamento: i segnalini si rileggono dalle proprietà delle missioni.
	 */
	private static void ricostruisciLocazioniSegnalate() {
		for (Missione missione : getTutteLeMissioni()) {
			String valore = missione.ottieniProprieta(LOCAZIONE_SEGNALATA);
			if (valore != null) {
				String[] parti = valore.split(",");
				locazioniSegnalate.put(missione.getId(), new CoordinateMD(Integer.parseInt(parti[0]), Integer.parseInt(parti[1])));
			}
		}
	}

	/**
	 * Dopo un caricamento: i claim si rileggono dalle proprietà delle missioni (quello di adesso e quelli lasciati
	 * prima), nell'ordine in cui sono stati fatti.
	 */
	private static void ricostruisciLocazioniOccupate() {
		List<String[]> claim = new ArrayList<>();
		for (Missione missione : getTutteLeMissioni()) {
			aggiungiIClaim(claim, missione.ottieniProprieta(LOCAZIONI_GIA_OCCUPATE), missione.getId());
			aggiungiIClaim(claim, missione.ottieniProprieta(LOCAZIONE_OCCUPATA), missione.getId());
		}
		claim.sort(Comparator.comparingInt(c -> Integer.parseInt(c[2])));
		for (String[] c : claim) {
			locazioniOccupate.put(new CoordinateMD(Integer.parseInt(c[0]), Integer.parseInt(c[1])), c[3]);
			prossimoClaim = Integer.parseInt(c[2]) + 1;
		}
	}

	/**
	 * Aggiunge all'elenco i claim scritti nel valore di una proprietà: "x,y,numero", più d'uno se separati da ';'.
	 */
	private static void aggiungiIClaim(List<String[]> claim, String valore, String idMissione) {
		if (valore == null) {
			return;
		}
		for (String uno : valore.split(";")) {
			String[] parti = uno.split(",");
			claim.add(new String[] {parti[0], parti[1], parti[2], idMissione});
		}
	}

	private static void aggiornaDopoRiletturaImpl(Collection<MissioneMD> missioni) {
		for (MissioneMD missioneMD : missioni) {
			MissionePredefinita tipoMissionePredefinita = MissionePredefinita.predefinitaConId(missioneMD.getId());
			if (tipoMissionePredefinita != null) {
				Missione missione = ricostruisci(FabbricaMissioni.crea(tipoMissionePredefinita), missioneMD);
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
		return ricostruisci(FabbricaMissioni.crea(missioneMD.getClasse()), missioneMD);
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
	 * Se una missione di quella classe è già partita in questa partita, in corso, finita o fallita.
	 */
	public static boolean isPartita(MissionePredefinita classe) {
		return getTutteLeMissioni().stream().anyMatch(missione -> missione.getClasse() == classe);
	}

	/**
	 * Fa partire una missione che parte da sola (vedi Missione.avviaDaSola) e la restituisce.
	 */
	public static Missione avvia(MissionePredefinita classe) {
		Missione missione = FabbricaMissioni.crea(classe);
		missione.avviaDaSola();
		return missione;
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
		togliSegnalino(missione);
		MissionePredefinita tipoMissione = MissionePredefinita.predefinitaConId(missione.getId());
		if (tipoMissione != null) {
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
	public static Missione getMissionePrincipale() {
		Missione missione = elencoMissioniPredefinite.get(MissionePredefinita.SCONFIGGI_IL_DRAGO);
		if (missione == null) {
			missione = elencoMissioniPredefiniteCompletate.get(MissionePredefinita.SCONFIGGI_IL_DRAGO);
		}
		return missione;
	}

	/**
	 * Sposta tra le completate una missione di primo livello. Una sotto-missione completata resta dentro la sua
	 * missione, con la sua proprieta' COMPLETA (vedi getMissioniCompletate).
	 */
	public static void completaMissione(Missione missione) {
		togliSegnalino(missione);
		MissionePredefinita tipoMissione = MissionePredefinita.predefinitaConId(missione.getId());
		if (tipoMissione != null) {
			elencoMissioniPredefinite.remove(tipoMissione);
			elencoMissioniPredefiniteCompletate.put(tipoMissione, missione);
		} else if (elencoMissioniSecondarie.removeIf(missione::equals)) {
			elencoMissioniSecondarieCompletate.add(missione);
		}
	}
}
