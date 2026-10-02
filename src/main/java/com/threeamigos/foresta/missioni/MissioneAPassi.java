package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Viandante;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Una missione fatta di {@link Passo}, come un piccolo automa (vedi gestione_missioni.md, §3): i passi hanno un
 * id stringa e ognuno decide quale viene dopo, così una missione può diramarsi secondo lo stato di gioco o una
 * scelta del giocatore, e una missione generata può costruire i suoi passi al volo.
 * <p>
 * Si salva solo l'id del passo corrente ({@code PASSO_CORRENTE}), più i parametri che le sottoclassi scrivono come
 * proprietà ordinarie: i passi si ricostruiscono ogni volta da {@link #costruisciPasso(String)}, che deve quindi
 * saper ricostruire ogni id che la missione abbia mai usato, anche dopo un salvataggio e un caricamento.
 * <p>
 * A ogni controllo dell'automa ({@link #controllaPreLocazione()} e seguenti), se la missione non è finita e il
 * passo corrente va valutato in quel controllo ed è concluso, se ne esegue l'azione e si passa al successivo; se
 * anche quello è dello stesso controllo ed è già concluso si prosegue subito, così un passo di solo testo non
 * costa un turno. {@link Passo#FINE} chiude la missione, completandola se l'azione non l'ha già fatto.
 * <p>
 * Un passo con una domanda ({@link Passo#chiediConferma}, {@link Passo#chiediScelta}) non si conclude da solo:
 * quando la sua condizione è vera la missione la offre con {@link #getDomandaDaPorre}, l'automa la pone al
 * giocatore e consegna la risposta con {@link #rispondi}; al controllo successivo il passo si conclude e la
 * diramazione la legge con {@link #getRisposta(String)}.
 * <p>
 * Un passo con un intermezzo, una volta concluso, lascia il suo id tra quelli con un intermezzo in attesa
 * ({@link #getPassiConIntermezzoInAttesa()}): chi lo mostra lo segna con {@link #segnaIntermezzoPassoMostrato}.
 * <p>
 * Le sottoclassi che hanno bisogno di altra logica (per esempio far fallire la missione se una città è stata
 * distrutta) sovrascrivono il controllo che serve e chiamano il metodo della superclasse alla fine.
 */
public abstract class MissioneAPassi extends MissioneBase {

	private static final String PASSO_CORRENTE = "PASSO_CORRENTE";
	private static final String SEQUENZA_PASSI = "SEQUENZA_PASSI";
	private static final String INTERMEZZI_IN_ATTESA = "INTERMEZZI_IN_ATTESA";
	private static final String INTERMEZZO_MOSTRATO = "INTERMEZZO_MOSTRATO_";
	private static final String RISPOSTA = "RISPOSTA_";
	private static final String PUNTO_DI_PARTENZA = "PUNTO_DI_PARTENZA";
	private static final String INIZIO_PASSO = "INIZIO_";
	private static final String EVENTO = "EVENTO_";
	private static final String CONTATORE = "CONTATORE_";
	private static final String SCORTATO = "SCORTATO";
	/**
	 * Separatore delle liste di id salvate come una proprietà: '§' e '|' sono già riservati dal salvataggio
	 */
	private static final String SEPARATORE = ",";
	/**
	 * Quanti passi si possono concludere di fila nello stesso controllo: di più è quasi certamente un ciclo
	 */
	private static final int PASSI_DI_FILA_MASSIMI = 50;

	protected MissioneAPassi(ClasseMissione classe) {
		super(classe);
	}

	/**
	 * L'id del primo passo.
	 */
	protected abstract String passoIniziale();

	/**
	 * Il passo con quell'id, ricostruito: si chiama ogni volta che serve, non si tiene da parte.
	 */
	protected abstract Passo costruisciPasso(String id);

	@Override
	public void controllaPreLocazione() {
		avanzaSePronto(MomentoControllo.PRE_LOCAZIONE);
	}

	@Override
	public void controllaInLocazione() {
		avanzaSePronto(MomentoControllo.IN_LOCAZIONE);
	}

	@Override
	public void controllaPostLocazione() {
		avanzaSePronto(MomentoControllo.POST_LOCAZIONE);
	}

	/**
	 * L'id del passo corrente: il passo iniziale finché la missione non è avanzata, {@link Passo#FINE} quando è
	 * finita per l'ultimo passo.
	 */
	public final String getPassoCorrente() {
		String passo = ottieniProprieta(PASSO_CORRENTE);
		return passo != null ? passo : passoIniziale();
	}

	private void avanzaSePronto(MomentoControllo momento) {
		for (int passiDiFila = 0; !isCompleta() && !isFallita(); passiDiFila++) {
			String id = getPassoCorrente();
			if (Passo.FINE.equals(id)) {
				return;
			}
			Passo passo = costruisciPasso(id);
			// La guardia di falliscoSe vale in tutti i controlli, prima di tutto il resto
			if (passo.isFallito()) {
				BusEventi.pubblica(new NotificaTestoParagrafo(passo.getTestoFallimento()));
				fallisciMissione();
				return;
			}
			if (passo.getMomento() != momento) {
				return;
			}
			// Una domanda si conclude con la risposta, che può arrivare solo dall'automa (vedi getDomandaDaPorre)
			if (passo.isDomanda() ? getRisposta(id) == null : !passo.isConcluso()) {
				return;
			}
			if (passiDiFila >= PASSI_DI_FILA_MASSIMI) {
				throw new IllegalStateException("La missione " + getId() + " ha concluso " + PASSI_DI_FILA_MASSIMI
						+ " passi di fila nello stesso controllo: c'è un ciclo fra i passi? Ultimo passo: " + id);
			}
			passo.eseguiAzione();
			if (passo.haIntermezzo()) {
				aggiungiIntermezzoInAttesa(id);
			}
			// L'azione può aver fatto fallire la missione: allora non si avanza più
			if (isFallita()) {
				return;
			}
			String prossimo = passo.getProssimoPasso();
			impostaPassoCorrente(prossimo);
			if (!Passo.FINE.equals(prossimo)) {
				aggiungiProprieta(INIZIO_PASSO + prossimo, String.valueOf(oreDiGioco()));
			}
			if (Passo.FINE.equals(prossimo) && !isCompleta()) {
				completaMissione();
			}
		}
	}

	private void impostaPassoCorrente(String id) {
		aggiungiProprieta(PASSO_CORRENTE, validaId(id));
	}

	/**
	 * Come per ogni missione, e in più si ricorda dove si trova il gruppo: è il punto di partenza a cui tornare con
	 * {@link #tornaAlPuntoDiPartenza}.
	 */
	@Override
	public void attivaMissione() {
		CoordinateMD coordinate = GruppoGiocatore.getIstanza().getCoordinate();
		// Senza una casella (fuori da una partita) non c'è un punto di partenza da ricordare
		if (ottieniProprieta(PUNTO_DI_PARTENZA) == null && coordinate != null) {
			aggiungiProprieta(PUNTO_DI_PARTENZA, coordinate.getX() + SEPARATORE + coordinate.getY());
		}
		super.attivaMissione();
	}

	// --- Passi pronti (vedi passi_missioni.md, §2): ognuno restituisce un Passo da completare con poi

	/**
	 * VAI: si conclude quando il gruppo è in quella casella.
	 */
	protected final Passo vai(MomentoControllo momento, CoordinateMD destinazione) {
		return Passo.quando(momento, () -> destinazione.equals(GruppoGiocatore.getIstanza().getCoordinate()));
	}

	/**
	 * VAI: si conclude quando il gruppo è in quella locazione unica (una città, un castello…).
	 */
	protected final Passo vai(MomentoControllo momento, ClassiLocazione locazioneUnica) {
		return Passo.quando(momento, () -> GruppoGiocatore.getIstanza().isInLocazioneUnica(locazioneUnica));
	}

	/**
	 * VAI_INIZIALE: si conclude quando il gruppo torna nella casella in cui la missione si è attivata.
	 */
	protected final Passo tornaAlPuntoDiPartenza(MomentoControllo momento) {
		return Passo.quando(momento, () -> {
			CoordinateMD partenza = getPuntoDiPartenza();
			return partenza != null && partenza.equals(GruppoGiocatore.getIstanza().getCoordinate());
		});
	}

	/**
	 * La casella in cui la missione si è attivata, o null se non si è ancora attivata.
	 */
	public final CoordinateMD getPuntoDiPartenza() {
		String valore = ottieniProprieta(PUNTO_DI_PARTENZA);
		if (valore == null) {
			return null;
		}
		String[] parti = valore.split(SEPARATORE);
		return new CoordinateMD(Integer.parseInt(parti[0]), Integer.parseInt(parti[1]));
	}

	/**
	 * DIALOGO: scrive il testo nel riquadro del testo e passa oltre, al primo controllo del suo momento.
	 */
	protected final Passo dialogo(MomentoControllo momento, Supplier<String> testo) {
		return Passo.quando(momento, () -> true).esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(testo.get())));
	}

	/**
	 * RICOMPENSA: dà le monete al gruppo e scrive il testo. Spesso è l'ultimo passo, con {@code poi(Passo.FINE)}.
	 */
	protected final Passo ricompensa(MomentoControllo momento, int monete, Supplier<String> testo) {
		return Passo.quando(momento, () -> true).esegui(() -> {
			GruppoGiocatore.getIstanza().addMonete(monete);
			BusEventi.pubblica(new NotificaTestoParagrafo(testo.get()));
		});
	}

	/**
	 * ATTENDI: si conclude quando sono passate almeno quelle ore di gioco da quando è diventato il passo corrente.
	 */
	protected final Passo attendiOre(MomentoControllo momento, int ore) {
		return Passo.quando(momento, () -> oreDiGioco() - inizioDelPassoCorrente() >= ore);
	}

	/**
	 * CONTA_FINCHE: si conclude quando il contatore della missione arriva a {@code obiettivo} (vedi
	 * {@link #incrementaContatore}).
	 */
	protected final Passo contaFinche(MomentoControllo momento, String contatore, int obiettivo) {
		return Passo.quando(momento, () -> getContatore(contatore) >= obiettivo);
	}

	protected final void incrementaContatore(String contatore, int quanto) {
		aggiungiProprieta(CONTATORE + validaId(contatore), String.valueOf(getContatore(contatore) + quanto));
	}

	public final int getContatore(String contatore) {
		String valore = ottieniProprieta(CONTATORE + contatore);
		return valore == null ? 0 : Integer.parseInt(valore);
	}

	/**
	 * VAGABONDA_FINCHE + COMBATTI + CONTA_FINCHE: si conclude quando il gruppo ha sconfitto {@code quanti} avversari
	 * di quella classe, dovunque, da quando questo è il passo corrente.
	 */
	protected final Passo sconfiggi(MomentoControllo momento, ClassePersonaggio classe, int quanti) {
		return Passo.quando(momento, () -> getConteggioNelPassoCorrente(eventoSconfitto(classe)) >= quanti);
	}

	/**
	 * VAGABONDA_FINCHE + RACCOGLI + CONTA_FINCHE: si conclude quando il gruppo ha raccolto {@code quanti} oggetti di
	 * quella classe, da quando questo è il passo corrente.
	 */
	protected final Passo raccogli(MomentoControllo momento, ClassiOggetto classe, int quanti) {
		return Passo.quando(momento, () -> getConteggioNelPassoCorrente(eventoRaccolto(classe)) >= quanti);
	}

	/**
	 * RACCOGLI di oggetti che esistono solo per la missione (vedi {@link OggettoMissione}): finché è il passo
	 * corrente la missione li mette nelle locazioni indicate, e si conclude quando il gruppo ne ha raccolti quanti
	 * ne servono. Si contano sotto la chiave degli oggetti ({@link #getContatore}).
	 */
	protected final Passo raccogli(MomentoControllo momento, OggettiDaRaccogliere oggetti) {
		return contaFinche(momento, oggetti.getChiave(), oggetti.getQuantita()).semina(oggetti);
	}

	/**
	 * Il gruppo ha raccolto un oggetto di questa missione (vedi OggettoMissione.prendi).
	 */
	public final void oggettoDiMissioneRaccolto(String chiave, int quantita) {
		if (!isCompleta() && !isFallita()) {
			incrementaContatore(chiave, quantita);
		}
	}

	/**
	 * Se il passo corrente semina oggetti (vedi {@link Passo#semina}) e la locazione è adatta, mai visitata e
	 * fortunata, quanti ne mancano fino a quelli che possono stare in una locazione.
	 */
	@Override
	public Optional<Oggetto> getOggettoInLocazione(CoordinateMD coordinate, ClassiLocazione classe, boolean visitata) {
		if (!isAttiva() || isCompleta() || isFallita() || visitata || Passo.FINE.equals(getPassoCorrente())) {
			return Optional.empty();
		}
		OggettiDaRaccogliere oggetti = costruisciPasso(getPassoCorrente()).getOggettiDaSeminare();
		if (oggetti == null || !oggetti.getLocazioni().contains(classe)) {
			return Optional.empty();
		}
		int mancanti = oggetti.getQuantita() - getContatore(oggetti.getChiave());
		if (mancanti <= 0 || Dado.tira(100) > oggetti.getProbabilita()) {
			return Optional.empty();
		}
		int quanti = Math.min(Dado.tiraAncheAUnaFaccia(oggetti.getMassimoPerLocazione()), mancanti);
		return Optional.of(new OggettoMissione(getId(), oggetti.getChiave(), oggetti.getNome(), quanti));
	}

	/**
	 * CONSEGNA: quando la condizione è vera (per esempio il gruppo è dal mandante) e il gruppo ha gli oggetti, li
	 * consegna: escono dal conteggio della missione e si scrive il testo. Gli oggetti di missione non stanno
	 * nell'inventario: il gruppo li ha se la missione li ha contati (vedi {@link #raccogli(MomentoControllo,
	 * OggettiDaRaccogliere)}).
	 */
	protected final Passo consegna(MomentoControllo momento, BooleanSupplier dove, OggettiDaRaccogliere oggetti,
								   Supplier<String> testo) {
		return Passo.quando(momento, () -> dove.getAsBoolean() && getContatore(oggetti.getChiave()) >= oggetti.getQuantita())
				.esegui(() -> {
					incrementaContatore(oggetti.getChiave(), -oggetti.getQuantita());
					BusEventi.pubblica(new NotificaTestoParagrafo(testo.get()));
				});
	}

	// --- Combattimenti e scorte

	/**
	 * COMBATTI(bersaglio): finché è il passo corrente, nella locazione in quelle coordinate ci sono gli avversari
	 * dell'incontro, al posto di quelli che ci sarebbero stati; si conclude a fine locazione, lì, quando il gruppo
	 * ne ha sconfitti quanti ne erano. Gli avversari della stessa classe sconfitti altrove nel frattempo contano
	 * anche loro: per una banda in un covo va bene così.
	 */
	protected final Passo combatti(Supplier<CoordinateMD> dove, IncontroDiMissione incontro) {
		return Passo.quando(MomentoControllo.POST_LOCAZIONE,
						() -> dove.get() != null && dove.get().equals(GruppoGiocatore.getIstanza().getCoordinate())
								&& getConteggioNelPassoCorrente(eventoSconfitto(incontro.getClasse())) >= incontro.getNumero())
				.affronta(dove, incontro);
	}

	@Override
	public Optional<List<Personaggio>> getIncontroInLocazione(CoordinateMD coordinate) {
		if (!isAttiva() || isCompleta() || isFallita() || Passo.FINE.equals(getPassoCorrente())) {
			return Optional.empty();
		}
		IncontroDiMissione incontro = costruisciPasso(getPassoCorrente()).getIncontroIn(coordinate);
		return incontro == null ? Optional.empty() : Optional.of(incontro.crea());
	}

	/**
	 * L'inizio di una SCORTA: quando la condizione è vera, un {@link Viandante} con quel nome si unisce al gruppo
	 * come ospite (vedi GruppoGiocatore.aggiungiOspite: non combatte e non conta nei limiti del gruppo); la missione
	 * se lo ricorda e lo si ritrova con {@link #getScortato()}.
	 */
	protected final Passo prendiInScorta(MomentoControllo momento, BooleanSupplier quando, String nome) {
		return Passo.quando(momento, quando)
				.esegui(() -> {
					Viandante viandante = new Viandante(nome, EquipaggiamentoIniziale.livelloCasualeDalMondo());
					GruppoGiocatore.getIstanza().aggiungiOspite(viandante);
					aggiungiProprieta(SCORTATO, viandante.getModelloDati().getUuid());
				});
	}

	/**
	 * SCORTA: si conclude quando il gruppo arriva in quelle coordinate con lo scortato, che allora si separa dal
	 * gruppo.
	 */
	protected final Passo scorta(MomentoControllo momento, Supplier<CoordinateMD> destinazione) {
		return Passo.quando(momento, () -> destinazione.get() != null
						&& destinazione.get().equals(GruppoGiocatore.getIstanza().getCoordinate()) && getScortato().isPresent())
				.esegui(this::congedaScortato);
	}

	/**
	 * Chi la missione sta scortando, se viaggia con il gruppo.
	 */
	public final Optional<Personaggio> getScortato() {
		String uuid = ottieniProprieta(SCORTATO);
		if (uuid == null) {
			return Optional.empty();
		}
		return GruppoGiocatore.getIstanza().getOspiti().stream()
				.filter(p -> uuid.equals(p.getModelloDati().getUuid()))
				.findFirst();
	}

	private void congedaScortato() {
		getScortato().ifPresent(GruppoGiocatore.getIstanza()::rimuoviOspite);
		rimuoviProprieta(SCORTATO);
	}

	/**
	 * Come per ogni missione, e chi la missione stava scortando si separa dal gruppo.
	 */
	@Override
	public void fallisciMissione() {
		congedaScortato();
		super.fallisciMissione();
	}

	// --- Eventi di gioco contati per il passo corrente (vedi RegistroMissioni.registrati)

	public static String eventoSconfitto(ClassePersonaggio classe) {
		return "SCONFITTO_" + classe.name();
	}

	public static String eventoRaccolto(ClassiOggetto classe) {
		return "RACCOLTO_" + classe.name();
	}

	/**
	 * Il gioco segnala alla missione un evento (un avversario sconfitto, un oggetto raccolto): si conta per il passo
	 * corrente, così un passo conta solo quel che succede da quando è corrente.
	 */
	public final void registraEvento(String evento, int quantita) {
		if (isCompleta() || isFallita() || Passo.FINE.equals(getPassoCorrente())) {
			return;
		}
		String chiave = EVENTO + validaId(getPassoCorrente()) + "_" + validaId(evento);
		aggiungiProprieta(chiave, String.valueOf(getConteggioNelPassoCorrente(evento) + quantita));
	}

	public final int getConteggioNelPassoCorrente(String evento) {
		String valore = ottieniProprieta(EVENTO + getPassoCorrente() + "_" + evento);
		return valore == null ? 0 : Integer.parseInt(valore);
	}

	/**
	 * Quando il passo corrente è diventato tale, in ore di gioco. Il passo iniziale non ci è arrivato da un altro
	 * passo: la prima volta che serve si prende l'ora di adesso.
	 */
	private long inizioDelPassoCorrente() {
		String chiave = INIZIO_PASSO + getPassoCorrente();
		String valore = ottieniProprieta(chiave);
		if (valore == null) {
			valore = String.valueOf(oreDiGioco());
			aggiungiProprieta(chiave, valore);
		}
		return Long.parseLong(valore);
	}

	private static long oreDiGioco() {
		return LineaTemporale.getGiorno() * 24L + LineaTemporale.getOra();
	}

	// --- Locazioni da procurarsi

	/**
	 * Un passo che si procura una locazione esistente di quella classe che nessuna missione in corso ha
	 * rivendicato (vedi {@link RegistroMissioni#cerca}): si conclude quando la trova, rivendicandola per la missione;
	 * finché non c'è, si riprova a ogni controllo del suo momento. La coordinata trovata si legge poi con
	 * {@link RegistroMissioni#getLocazioneOccupata(Missione)}. Come ogni passo, si completa con {@code poi} e,
	 * se serve, {@code esegui}.
	 */
	protected final Passo cercaLocazione(MomentoControllo momento, ClassiLocazione richiesta) {
		return Passo.quando(momento, () -> RegistroMissioni.cerca(richiesta, this).isPresent());
	}

	// --- Domande al giocatore

	/**
	 * Il passo corrente, se è una domanda di quel controllo che si può porre adesso e non ha ancora risposta;
	 * altrimenti null.
	 */
	public final Passo getDomandaDaPorre(MomentoControllo momento) {
		if (isCompleta() || isFallita()) {
			return null;
		}
		String id = getPassoCorrente();
		if (Passo.FINE.equals(id)) {
			return null;
		}
		Passo passo = costruisciPasso(id);
		if (passo.isDomanda() && passo.getMomento() == momento && getRisposta(id) == null && passo.isConcluso()) {
			return passo;
		}
		return null;
	}

	/**
	 * La risposta del giocatore alla domanda del passo corrente: {@link Passo#SI}/{@link Passo#NO} per una conferma,
	 * "1".."N" per una scelta. Il passo si conclude al prossimo controllo del suo momento.
	 */
	public final void rispondi(String risposta) {
		String id = getPassoCorrente();
		Passo passo = costruisciPasso(id);
		if (!passo.isDomanda()) {
			throw new IllegalStateException("Il passo " + id + " della missione " + getId() + " non è una domanda");
		}
		if (!passo.getRispostePossibili().contains(risposta)) {
			throw new IllegalArgumentException("Risposta " + risposta + " non valida per il passo " + id
					+ ": possibili " + passo.getRispostePossibili());
		}
		aggiungiProprieta(RISPOSTA + validaId(id), risposta);
	}

	/**
	 * La risposta data alla domanda di quel passo, o null se non c'è ancora.
	 */
	public final String getRisposta(String idPasso) {
		return ottieniProprieta(RISPOSTA + idPasso);
	}

	// --- Sequenze di passi decise alla generazione

	/**
	 * Fissa una sequenza di passi decisa alla generazione della missione (per esempio "visita N locazioni scelte a
	 * caso"), da percorrere con {@link #prossimoNellaSequenza()}. Si decide una volta sola e resta stabile per tutta
	 * la missione, anche attraverso salvataggio e caricamento. Gli id devono essere tutti diversi.
	 */
	protected final void impostaSequenzaPassi(List<String> idPassi) {
		if (idPassi.isEmpty()) {
			throw new IllegalArgumentException("La sequenza di passi è vuota");
		}
		if (new HashSet<>(idPassi).size() != idPassi.size()) {
			throw new IllegalArgumentException("Un passo compare più volte nella sequenza: " + idPassi);
		}
		idPassi.forEach(MissioneAPassi::validaId);
		aggiungiProprieta(SEQUENZA_PASSI, String.join(SEPARATORE, idPassi));
	}

	protected final List<String> leggiSequenzaPassi() {
		return leggiLista(SEQUENZA_PASSI);
	}

	/**
	 * Per un passo di una sequenza fissata con {@link #impostaSequenzaPassi}: il passo che lo segue nella sequenza,
	 * o {@link Passo#FINE} se era l'ultimo.
	 */
	protected final Supplier<String> prossimoNellaSequenza() {
		return () -> {
			List<String> sequenza = leggiSequenzaPassi();
			int indice = sequenza.indexOf(getPassoCorrente());
			if (indice < 0) {
				throw new IllegalStateException("Il passo " + getPassoCorrente() + " non fa parte della sequenza " + sequenza);
			}
			return indice + 1 < sequenza.size() ? sequenza.get(indice + 1) : Passo.FINE;
		};
	}

	// --- Intermezzi dei passi

	/**
	 * Gli id dei passi conclusi il cui intermezzo non è ancora stato mostrato, nell'ordine in cui si sono conclusi.
	 */
	public final List<String> getPassiConIntermezzoInAttesa() {
		return Collections.unmodifiableList(leggiLista(INTERMEZZI_IN_ATTESA));
	}

	/**
	 * Il primo passo con un intermezzo in attesa per quel momento, o null.
	 */
	public final String getPassoConIntermezzoInAttesa(MomentoIntermezzo momento) {
		for (String id : leggiLista(INTERMEZZI_IN_ATTESA)) {
			if (costruisciPasso(id).getMomentoIntermezzo() == momento) {
				return id;
			}
		}
		return null;
	}

	/**
	 * Se la missione mostrerà un intermezzo in quel momento: ne ha uno in attesa, oppure il passo corrente si valuta
	 * in quel controllo, ha un intermezzo in quel momento ed è già concluso, quindi scatterà appena la missione verrà
	 * controllata. Serve a chi non vuole sovrapporsi agli intermezzi delle altre missioni (vedi IncaricoInCitta).
	 * Valuta la condizione del passo corrente solo se ha un intermezzo: le condizioni di quei passi non devono avere
	 * effetti.
	 */
	public final boolean haUnIntermezzoInArrivo(MomentoControllo controllo, MomentoIntermezzo momento) {
		if (getPassoConIntermezzoInAttesa(momento) != null) {
			return true;
		}
		if (isCompleta() || isFallita() || Passo.FINE.equals(getPassoCorrente())) {
			return false;
		}
		Passo passo = costruisciPasso(getPassoCorrente());
		return passo.getMomento() == controllo && passo.getMomentoIntermezzo() == momento && !passo.isDomanda()
				&& !passo.isFallito() && passo.isConcluso();
	}

	public final boolean isIntermezzoPassoMostrato(String idPasso) {
		return ottieniProprieta(INTERMEZZO_MOSTRATO + idPasso) != null;
	}

	public final void segnaIntermezzoPassoMostrato(String idPasso) {
		aggiungiProprieta(INTERMEZZO_MOSTRATO + validaId(idPasso), AFFERMATIVO);
		List<String> inAttesa = leggiLista(INTERMEZZI_IN_ATTESA);
		inAttesa.remove(idPasso);
		scriviLista(INTERMEZZI_IN_ATTESA, inAttesa);
	}

	private void aggiungiIntermezzoInAttesa(String idPasso) {
		if (isIntermezzoPassoMostrato(idPasso)) {
			return;
		}
		List<String> inAttesa = leggiLista(INTERMEZZI_IN_ATTESA);
		if (!inAttesa.contains(idPasso)) {
			inAttesa.add(idPasso);
			scriviLista(INTERMEZZI_IN_ATTESA, inAttesa);
		}
	}

	// --- Liste di id salvate come una proprietà

	private List<String> leggiLista(String chiave) {
		String valore = ottieniProprieta(chiave);
		if (valore == null || valore.isEmpty()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(Arrays.asList(valore.split(SEPARATORE)));
	}

	private void scriviLista(String chiave, List<String> lista) {
		if (lista.isEmpty()) {
			rimuoviProprieta(chiave);
		} else {
			aggiungiProprieta(chiave, String.join(SEPARATORE, lista));
		}
	}

	/**
	 * Un id di passo finisce nelle proprietà salvate e nelle liste separate da virgole: niente caratteri riservati.
	 */
	private static String validaId(String id) {
		if (id == null || id.isEmpty() || id.contains(SEPARATORE) || id.contains("§") || id.contains("|")) {
			throw new IllegalArgumentException("Id di passo non valido: " + id);
		}
		return id;
	}
}
