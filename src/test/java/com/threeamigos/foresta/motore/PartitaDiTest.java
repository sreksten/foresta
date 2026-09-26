package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioFornitore;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoInvioTesto;
import com.threeamigos.foresta.eventi.interni.InternoAggiornamentoComandiDisponibili;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.interni.InternoFineLogoIniziale;
import com.threeamigos.foresta.eventi.interni.InternoStatoDiGioco;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.eventi.richieste.RichiestaSelezioneDirezione;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tools.GestorePunteggi;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import com.threeamigos.foresta.tools.ModalitaDiProva;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Una partita guidata da un test al posto del giocatore e della UI: manda comandi e testi all'Automa come farebbe
 * la UI e ne raccoglie le risposte dal bus.
 * <p>
 * {@link #nuova(long)} riparte da zero: bus senza iscritti e con consegna immediata sul thread del test, Dado col
 * seme dato, ModelloDati e gruppi nuovi, salvataggi e classifica in memoria, un temporizzatore che scatta solo a
 * comando. {@link #close()} rimette la consegna del gioco, quindi va usata in try-with-resources o in @AfterEach.
 * <p>
 * Ogni comando o testo che fa pubblicare all'automa un InternoErrore o un InternoException fa fallire il test.
 * <p>
 * Gli intermezzi possono scattare in qualunque momento (inizio partita, ingresso in una locazione...): dopo ogni
 * comando, testo o impulso la partita li fa scorrere da sola fino alla fine, come un giocatore che clicca sulla
 * pergamena. Un test sugli intermezzi lo disattiva con {@link #nonSaltareIntermezzi()}.
 * <p>
 * Le partite di test girano in modalità di prova (ModalitaDiProva: monete, pergamene e pozioni a volontà, la mappa
 * svelata, le missioni e l'intermezzo di prova), tranne quelle di {@link #nuovaSenzaTrucchi(long)}, che partono
 * come per un giocatore vero. {@link #close()} rimette la system property com'era.
 */
final class PartitaDiTest implements AutoCloseable {

	private final Automa automa;
	private final TemporizzatoreManuale temporizzatore = new TemporizzatoreManuale();
	private final GestoreSalvataggiInMemoria salvataggi;
	private final RegistratoreEventi registratore = new RegistratoreEventi();
	private Collection<Comando> comandiDisponibili = new ArrayList<>();
	private int erroriVisti;
	private boolean saltaIntermezzi = true;
	private final String modalitaDiProvaPrecedente = System.getProperty(ModalitaDiProva.PROPRIETA);

	private PartitaDiTest(long seme, GestoreSalvataggiInMemoria salvataggi, boolean modalitaDiProva) {
		this.salvataggi = salvataggi;
		if (modalitaDiProva) {
			System.setProperty(ModalitaDiProva.PROPRIETA, "true");
		} else {
			System.clearProperty(ModalitaDiProva.PROPRIETA);
		}
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		Dado.ripristina();
		Dado.impostaSeme(seme);
		ModelloDati.setIstanza(new ModelloDati());
		GruppoGiocatore.azzeraIstanza();
		GruppoAvversario.azzeraIstanza();
		GestoreSalvataggi.impostaGestoreSalvataggi(salvataggi);
		GestorePunteggi.impostaGestorePunteggi(new GestorePunteggiInMemoria());
		Notizie.registrati();

		registratore.ascolta(InternoErrore.class, InternoException.class, InternoStatoDiGioco.class,
				InternoAggiornamentoComandiDisponibili.class, RichiestaSelezioneDirezione.class,
				ComandoAperturaInventarioCommerciante.class, ComandoAperturaInventarioFornitore.class,
				NotificaTestoFrase.class, NotificaTestoParagrafo.class, NotificaPaginaIntermezzo.class,
				NotificaApprovazioneAcquistoArtefatto.class, NotificaRifiutoAcquistoArtefatto.class,
				NotificaApprovazioneVenditaArtefatto.class, NotificaRifiutoVenditaArtefatto.class);
		BusEventi.iscriviti(InternoStatoDiGioco.class, e -> comandiDisponibili = new ArrayList<>(e.getComandiPossibili()));
		BusEventi.iscriviti(InternoAggiornamentoComandiDisponibili.class, this::aggiornaComandi);
		BusEventi.iscriviti(RichiestaSelezioneDirezione.class, this::aggiornaComandi);
		BusEventi.iscriviti(ComandoAperturaInventarioCommerciante.class, this::aggiornaComandi);
		BusEventi.iscriviti(ComandoAperturaInventarioFornitore.class, this::aggiornaComandi);

		// Il precaricamento del motore sullo stesso thread, cosi' finisce prima che inizia() ritorni
		automa = new Automa(temporizzatore, Runnable::run);
		automa.inizia();
		verificaNessunErrore();
	}

	/**
	 * Una partita nuova, ferma alla schermata iniziale (INTRO): il logo iniziale e' gia' passato, come se la UI
	 * avesse finito l'animazione.
	 */
	static PartitaDiTest nuova(long seme) {
		return nuovaConSalvataggi(seme, new GestoreSalvataggiInMemoria());
	}

	/**
	 * Una partita nuova, ferma all'INTRO, che trova i salvataggi fatti in un'altra: come riavviare il gioco e
	 * caricare una partita.
	 */
	static PartitaDiTest nuovaConSalvataggi(long seme, GestoreSalvataggiInMemoria salvataggi) {
		PartitaDiTest partita = new PartitaDiTest(seme, salvataggi, true);
		partita.fineLogoIniziale();
		return partita;
	}

	/**
	 * Una partita nuova ferma al primissimo stato, LOGO_INIZIALE, in attesa che la UI finisca l'animazione.
	 */
	static PartitaDiTest nuovaAlLogoIniziale(long seme) {
		return new PartitaDiTest(seme, new GestoreSalvataggiInMemoria(), true);
	}

	/**
	 * Una partita nuova, ferma all'INTRO, senza la modalità di prova: parte come per un giocatore vero.
	 */
	static PartitaDiTest nuovaSenzaTrucchi(long seme) {
		PartitaDiTest partita = new PartitaDiTest(seme, new GestoreSalvataggiInMemoria(), false);
		partita.fineLogoIniziale();
		return partita;
	}

	/**
	 * Quello che fa la UI quando l'animazione del logo e' finita e le sue risorse sono caricate.
	 */
	PartitaDiTest fineLogoIniziale() {
		BusEventi.pubblica(new InternoFineLogoIniziale());
		verificaNessunErrore();
		return this;
	}

	private void aggiornaComandi(RichiestaConComandi richiesta) {
		comandiDisponibili = new ArrayList<>(richiesta.getPossibilita());
	}

	// ---- comandi del giocatore ----

	PartitaDiTest comando(Comando comando) {
		BusEventi.pubblica(new ComandoDiGioco(comando));
		verificaNessunErrore();
		dopoOgniPasso();
		return this;
	}

	PartitaDiTest testo(String testo) {
		BusEventi.pubblica(new ComandoInvioTesto(testo));
		verificaNessunErrore();
		dopoOgniPasso();
		return this;
	}

	/**
	 * Un impulso del temporizzatore (un round di combattimento, una pagina di intermezzo che avanza da sola...).
	 */
	PartitaDiTest scatta() {
		temporizzatore.scatta();
		verificaNessunErrore();
		dopoOgniPasso();
		return this;
	}

	/**
	 * Da qui in poi gli intermezzi restano sullo schermo finché il test non li fa avanzare.
	 */
	PartitaDiTest nonSaltareIntermezzi() {
		saltaIntermezzi = false;
		return this;
	}

	private void dopoOgniPasso() {
		if (saltaIntermezzi) {
			saltaIntermezzi();
		}
	}

	/**
	 * Crea il protagonista e inizia la partita. Tra la scelta del nome, quando il mondo è già stato costruito, e
	 * quella del sesso si esegue primaDiIniziare: è il momento per preparare il mondo, per esempio spostare il
	 * gruppo con {@link #spostaGruppoIn}.
	 */
	PartitaDiTest iniziaCon(String nome, Comando sesso, Comando classe, Runnable primaDiIniziare) {
		comando(Comando.PERGAMENA);
		testo(nome);
		assertStato(Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO);
		primaDiIniziare.run();
		comando(sesso);
		comando(classe);
		return this;
	}

	/**
	 * Mette il gruppo sulla casella di una locazione unica (una città, un castello...), prima che la partita inizi.
	 */
	void spostaGruppoIn(ClassiLocazione locazioneUnica) {
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(locazioneUnica);
		if (coordinate == null) {
			throw new IllegalArgumentException(locazioneUnica + " non è nella Foresta");
		}
		gruppo().setCoordinate(coordinate);
		Foresta.aggiornaMappaCircostante(gruppo());
	}

	/**
	 * Fa avanzare tutte le pagine degli intermezzi in corso, come se il giocatore cliccasse sulla pergamena.
	 */
	PartitaDiTest saltaIntermezzi() {
		while (automa.getStato() == Stato.INTERMEZZO) {
			BusEventi.pubblica(new ComandoDiGioco(Comando.PERGAMENA));
			verificaNessunErrore();
		}
		return this;
	}

	// ---- stato della partita ----

	Stato stato() {
		return automa.getStato();
	}

	GruppoGiocatore gruppo() {
		return GruppoGiocatore.getIstanza();
	}

	/**
	 * Se l'inventario del gruppo contiene quell'artefatto. Si confrontano i modelli dati: getInventario() crea
	 * ogni volta nuovi Artefatto attorno ai modelli, che non sono uguali tra loro.
	 */
	static boolean contiene(Collection<Artefatto> inventario, Artefatto artefatto) {
		return inventario.stream().anyMatch(a -> a.getModelloDati() == artefatto.getModelloDati());
	}

	/**
	 * I comandi che la UI offrirebbe adesso al giocatore.
	 */
	Collection<Comando> comandiDisponibili() {
		return comandiDisponibili;
	}

	RegistratoreEventi eventi() {
		return registratore;
	}

	TemporizzatoreManuale temporizzatore() {
		return temporizzatore;
	}

	GestoreSalvataggiInMemoria salvataggi() {
		return salvataggi;
	}

	/**
	 * Tutto il testo mostrato al giocatore finora, frasi e paragrafi.
	 */
	List<String> testi() {
		return registratore.inOrdine(NotificaTestoFrase.class, NotificaTestoParagrafo.class).stream()
				.map(e -> e instanceof NotificaTestoFrase
						? ((NotificaTestoFrase) e).getMessaggio()
						: ((NotificaTestoParagrafo) e).getMessaggio())
				.collect(Collectors.toList());
	}

	void assertStato(Stato atteso) {
		if (automa.getStato() != atteso) {
			throw new AssertionError("Stato atteso " + atteso + ", invece " + automa.getStato());
		}
	}

	void assertComandoDisponibile(Comando comando) {
		if (!comandiDisponibili.contains(comando)) {
			throw new AssertionError(comando + " non è tra i comandi disponibili: " + comandiDisponibili);
		}
	}

	private void verificaNessunErrore() {
		List<String> errori = new ArrayList<>();
		registratore.tutti(InternoErrore.class).forEach(e -> errori.add(e.getMessaggio()));
		registratore.tutti(InternoException.class).forEach(e -> errori.add(e.getMessaggio() + ": " + e.getException()));
		if (errori.size() > erroriVisti) {
			List<String> nuovi = errori.subList(erroriVisti, errori.size());
			erroriVisti = errori.size();
			throw new AssertionError("L'automa ha segnalato errori: " + nuovi.stream().collect(Collectors.joining("; ")));
		}
	}

	@Override
	public void close() {
		temporizzatore.termina();
		Dado.ripristina();
		Dado.impostaSeme(System.nanoTime());
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_EDT);
		if (modalitaDiProvaPrecedente == null) {
			System.clearProperty(ModalitaDiProva.PROPRIETA);
		} else {
			System.setProperty(ModalitaDiProva.PROPRIETA, modalitaDiProvaPrecedente);
		}
	}
}
