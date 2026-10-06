package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.*;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.interfacce.ControlloreDiGioco;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.FaseDiGioco;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.*;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class Automa implements ControlloreDiGioco, Temporizzabile {

	/**
	 * Numero massimo di transizioni automatiche (senza un comando reale del giocatore
	 * di mezzo) consentite in cascata prima di considerare la cosa un ciclo tra stati.
	 * È una rete di sicurezza, non un limite atteso in condizioni normali: la cascata
	 * più lunga osservata nel codice attuale è di poche unità di stati.
	 */
	private static final int MAX_TRANSIZIONI_AUTOMATICHE = 100;

	/**
	 * Esito dell'esecuzione di un passo della macchina a stati.
	 * FERMATI: lo stato corrente aspetta un comando reale del giocatore.
	 * "continua" con prossimoComando nullo: lo stato appena impostato deve
	 * eseguire subito la propria logica di ingresso (nessun input reale coinvolto).
	 * "continua" con prossimoComando non nullo: lo stato appena impostato deve
	 * reagire subito a QUEL comando, come se il giocatore lo avesse inviato ora
	 * (usato per inoltrare un comando reale, o uno risolto automaticamente,
	 * a uno stato diverso da quello in cui è stato ricevuto).
	 */
	private static final class Esito {
		static final Esito FERMATI = new Esito(false, null);
		static final Esito CONTINUA_CON_INGRESSO = new Esito(true, null);

		final boolean continua;
		final Comando prossimoComando;

		private Esito(boolean continua, Comando prossimoComando) {
			this.continua = continua;
			this.prossimoComando = prossimoComando;
		}

		static Esito continuaCon(Comando comando) {
			return new Esito(true, comando);
		}
	}

	// Eseguiti quando si entra in uno stato senza che ci sia un comando reale del
	// giocatore di mezzo (ingresso automatico/a cascata).
	private final Map<Stato, Supplier<Esito>> gestoriIngresso;
	// Eseguiti quando arriva un comando reale del giocatore (o inoltrato come tale).
	private final Map<Stato, Function<Comando, Esito>> gestoriComando;
	private final Temporizzatore temporizzatore;
	private final GestoreSalvataggi gestoreSalvataggi;
	private final GestorePunteggi gestorePunteggi;

	private String nomePersonaggio;
	private Stato stato;
	private Stato statoPrecedente;

	private final Executor esecutorePrecaricamento;
	// Il logo iniziale lascia il posto all'INTRO quando la UI e il motore hanno finito entrambi
	private boolean logoInizialeMostrato;
	private boolean motorePrecaricato;
	private final GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
	private final GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
	private Personaggio personaggio;
	// Ultimo personaggio scelto nella schermata inventario: non salvato, si azzera a ogni avvio.
	private int indicePersonaggioInventario = 0;
	private Locazione locazioneCorrente;
	private Comando direzione; // serve a memorizzare la direzione prima di chiedere il numero di passi
	private Collection<Comando> comandiPossibiliPerNumeroPassi; // i passi consentiti in quella direzione, più ANNULLA
	private Personaggio formulanteResurrezione; // chi lancerà la Resurrezione, scelto prima del bersaglio
	private Comando comandoNegozioInAttesa; // il comando di ingresso in un negozio, in attesa del suo intermezzo

	// L'intermezzo in corso, la pagina mostrata, e dove riprendere quando non ce ne sono altri
	private Intermezzo intermezzoCorrente;
	private List<PaginaIntermezzo> pagineIntermezzo;
	private int paginaIntermezzo;
	// Quando è comparsa la pagina corrente dell'intermezzo, secondo l'orologio: spostato all'indietro quando il
	// giocatore salta al fumetto successivo, come fa la UI
	private long inizioPaginaIntermezzo;
	private final LongSupplier orologioNanosecondi;
	private MomentoIntermezzo momentoIntermezzo;
	private Stato statoDopoIntermezzi;

	// La UI sta ancora animando qualcosa (sprite, annuncio globale): l'intermezzo pronto a
	// scattare aspetta che segnali InternoUiInattiva prima di essere mostrato
	private boolean uiOccupata;
	// La domanda di una missione a passi in attesa di risposta (vedi controllaMissioniEDomande): chi l'ha posta, in
	// quale controllo, con quali comandi, e come si prosegue una volta consegnata la risposta
	private MissioneAPassi missioneInAttesaDiRisposta;
	private MomentoControllo momentoDomanda;
	private List<Comando> comandiRispostaMissione;
	private Supplier<Esito> ripresaDopoRisposta;
	// Le risposte a una scelta di missione: l'opzione N-esima è NUMERO_N
	private static final List<Comando> COMANDI_OPZIONI = Arrays.asList(Comando.NUMERO_1, Comando.NUMERO_2,
			Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5);
	private MomentoIntermezzo momentoIntermezzoInAttesa;
	private Stato statoDopoIntermezzoInAttesa;
	// Si è appena usciti da Stato.INTERMEZZO (ultima pagina mostrata): il ritorno alla
	// schermata di gioco va notificato solo quando la cascata automatica che segue si sarà
	// davvero fermata (vedi prosegui()), non a questo singolo passo intermedio — potrebbe
	// scattare subito un altro intermezzo, o la UI potrebbe non essere ancora pronta.
	private boolean mostraSchermataGiocoAllaRipresa;

	/**
	 * @param gestoreSalvataggi dove stanno le partite salvate (nel gioco GestoreSalvataggiSuFile)
	 * @param gestorePunteggi   la classifica (nel gioco GestorePunteggiSuFile)
	 */
	public Automa(Temporizzatore temporizzatore, GestoreSalvataggi gestoreSalvataggi, GestorePunteggi gestorePunteggi) {
		this(temporizzatore, gestoreSalvataggi, gestorePunteggi, Automa::precaricaInBackground);
	}

	/**
	 * @param esecutorePrecaricamento dove eseguire, durante il logo iniziale, il caricamento delle risorse del motore:
	 *                                nel gioco un thread a parte, nei test lo stesso thread ({@code Runnable::run})
	 */
	Automa(Temporizzatore temporizzatore, GestoreSalvataggi gestoreSalvataggi, GestorePunteggi gestorePunteggi,
		   Executor esecutorePrecaricamento) {
		this(temporizzatore, gestoreSalvataggi, gestorePunteggi, esecutorePrecaricamento, System::nanoTime);
	}

	/**
	 * @param orologioNanosecondi l'orologio con cui si misura da quanto è comparsa una pagina di un intermezzo, per
	 *                            sapere quale fumetto viene dopo (vedi gestisciComandoInStatoIntermezzo): nel gioco
	 *                            System.nanoTime, come la UI
	 */
	Automa(Temporizzatore temporizzatore, GestoreSalvataggi gestoreSalvataggi, GestorePunteggi gestorePunteggi,
		   Executor esecutorePrecaricamento, LongSupplier orologioNanosecondi) {
		this.temporizzatore = temporizzatore;
		this.gestoreSalvataggi = gestoreSalvataggi;
		this.gestorePunteggi = gestorePunteggi;
		this.esecutorePrecaricamento = esecutorePrecaricamento;
		this.orologioNanosecondi = orologioNanosecondi;
		temporizzatore.setTemporizzabile(this);

		BusEventi.iscriviti(ComandoDiGioco.class, this::onEventoComandoDiGioco);
		BusEventi.iscriviti(ComandoInvioTesto.class, this::onEventoTestoDisponibile);
		// Gli spostamenti di artefatti nella schermata di scambio aperta: li fa lo scambio stesso
		BusEventi.iscriviti(ComandoScambioArtefatto.class, AutomaScambiatoreArtefatti::esegui);
		BusEventi.iscriviti(ComandoSpesaPuntoAbilita.class, this::onComandoSpesaPuntoAbilita);
		BusEventi.iscriviti(ComandoCommutazioneElenco.class, Automa::onComandoCommutazioneElenco);
		// L'interruttore dell'aiuto vale in ogni stato: non passa dalla macchina a stati
		BusEventi.iscriviti(ComandoImpostazioneAiuto.class,
				e -> ModelloDati.getIstanza().setAiutoAbilitato(e.isAbilitato()));
		BusEventi.iscriviti(InternoFineLogoIniziale.class, e -> {
			logoInizialeMostrato = true;
			passaAllIntroSePronto();
		});
		BusEventi.iscriviti(InternoPrecaricamentoMotoreCompletato.class, e -> {
			motorePrecaricato = true;
			passaAllIntroSePronto();
		});
		BusEventi.iscriviti(InternoUiOccupata.class, e -> uiOccupata = true);
		BusEventi.iscriviti(InternoUiInattiva.class, e -> {
			uiOccupata = false;
			if (stato == Stato.ATTESA_UI_PER_INTERMEZZO) {
				prosegui(avviaProssimoIntermezzo(momentoIntermezzoInAttesa, statoDopoIntermezzoInAttesa));
			}
		});

		gestoriIngresso = new EnumMap<>(Stato.class);
		gestoriIngresso.put(Stato.INIZIO_GIOCO, this::entraInStatoInizioGioco);
		gestoriIngresso.put(Stato.INIZIO_LOCAZIONE, this::entraInStatoInizioLocazione);
		gestoriIngresso.put(Stato.INTERMEZZO, this::entraInStatoIntermezzo);
		gestoriIngresso.put(Stato.PREPARAZIONE_LOCAZIONE, this::entraInStatoPreparazioneLocazione);
		gestoriIngresso.put(Stato.SCELTA_AUTOMATICA_PERSONAGGIO, this::entraInStatoSceltaAutomaticaPersonaggio);
		gestoriIngresso.put(Stato.SCELTA_PERSONAGGIO_QUALSIASI, this::entraInStatoSceltaPersonaggioQualsiasi);
		gestoriIngresso.put(Stato.SCELTA_MANUALE_PERSONAGGIO, this::entraInStatoSceltaManualePersonaggio);
		gestoriIngresso.put(Stato.SCELTA_DESTINATARIO_OGGETTO, this::entraInStatoSceltaDestinatarioOggetto);
		gestoriIngresso.put(Stato.SCELTA_INCANTESIMO_DA_LANCIARE, this::entraInStatoSceltaIncantesimoDaLanciare);
		gestoriIngresso.put(Stato.ATTESA_INCANTESIMO_QUALSIASI, this::entraInStatoAttesaIncantesimoQualsiasi);
		gestoriIngresso.put(Stato.ATTESA_SI_NO, this::entraInStatoAttesaSiNo);
		gestoriIngresso.put(Stato.ATTESA_RISPOSTA_MISSIONE, this::entraInStatoAttesaRispostaMissione);
		gestoriIngresso.put(Stato.FINE_LOCAZIONE, () -> eseguiFineLocazione(null));
		gestoriIngresso.put(Stato.FINE_LOCAZIONE_2, this::entraInStatoFineLocazione2);
		gestoriIngresso.put(Stato.ATTESA_DIREZIONE, this::entraInStatoAttesaDirezione);
		gestoriIngresso.put(Stato.SCELTA_FORMULANTE_RESURREZIONE, this::entraInStatoSceltaFormulanteResurrezione);
		gestoriIngresso.put(Stato.SCELTA_BERSAGLIO_RESURREZIONE, this::entraInStatoSceltaBersaglioResurrezione);
		gestoriIngresso.put(Stato.MAPPA, this::entraInStatoMappa);
		gestoriIngresso.put(Stato.INVENTARIO, this::entraInStatoInventario);
		gestoriIngresso.put(Stato.TROFEI, this::entraInStatoTrofei);
		// Uscendo da MAPPA o INVENTARIO aperti mentre si era IN_LOCAZIONE si torna qui
		// tramite CONTINUA_CON_INGRESSO (statoPrecedente vale IN_LOCAZIONE), con comando
		// nullo: bisogna ripubblicare i comandi della locazione, altrimenti restano quelli
		// della mappa e il giocatore deve cliccare due volte perché la UI si aggiorni.
		gestoriIngresso.put(Stato.IN_LOCAZIONE, this::entraInStatoInLocazione);
		gestoriIngresso.put(Stato.INGRESSO_NEGOZIO, this::entraInStatoIngressoNegozio);
		gestoriIngresso.put(Stato.ACCAMPAMENTO, this::entraInStatoAccampamento);
		gestoriIngresso.put(Stato.CONFERMA_USCITA, () -> Esito.FERMATI);
		gestoriIngresso.put(Stato.GIOCO_PERSO, this::entraInStatoGiocoPerso);
		gestoriIngresso.put(Stato.GIOCO_PERSO_2, this::entraInStatoGiocoPerso2);
		gestoriIngresso.put(Stato.GIOCO_VINTO, this::entraInStatoGiocoVinto);
		gestoriIngresso.put(Stato.GIOCO_VINTO_2, this::entraInStatoGiocoVinto2);
		gestoriIngresso.put(Stato.STATISTICHE, this::entraInStatoStatistiche);
		gestoriIngresso.put(Stato.PUNTEGGI, this::entraOGestisciStatoPunteggi);

		gestoriComando = new EnumMap<>(Stato.class);
		// Il logo iniziale non si salta: i comandi (e gli impulsi) si ignorano
		gestoriComando.put(Stato.LOGO_INIZIALE, comando -> Esito.FERMATI);
		// In attesa del nome per la classifica non c'e' nessuna icona: un comando arrivato comunque si ignora
		gestoriComando.put(Stato.ATTESA_NOME_PUNTEGGI, comando -> Esito.FERMATI);
		// In attesa che la UI finisca le sue animazioni: i comandi arrivati nel frattempo si ignorano
		gestoriComando.put(Stato.ATTESA_UI_PER_INTERMEZZO, comando -> Esito.FERMATI);
		gestoriComando.put(Stato.INTRO, this::gestisciComandoInStatoIntro);
		gestoriComando.put(Stato.INTERMEZZO, this::gestisciComandoInStatoIntermezzo);
		gestoriComando.put(Stato.PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE, this::gestisciComandoInStatoPreGameSelezionaSalvataggioDaLeggere);
		gestoriComando.put(Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO, this::gestisciComandoInStatoPreGameAttesaSessoPersonaggio);
		gestoriComando.put(Stato.PRE_GAME_ATTESA_CLASSE_PERSONAGGIO, this::gestisciComandoInStatoPreGameAttesaClassePersonaggio);
		gestoriComando.put(Stato.IN_LOCAZIONE, this::gestisciComandoInStatoInLocazione);
		gestoriComando.put(Stato.IN_COMBATTIMENTO, this::gestisciComandoInStatoInCombattimento);
		gestoriComando.put(Stato.SCELTA_MANUALE_PERSONAGGIO, this::gestisciComandoInStatoSceltaManualePersonaggio);
		gestoriComando.put(Stato.SCELTA_DESTINATARIO_OGGETTO, this::gestisciComandoInStatoSceltaDestinatarioOggetto);
		gestoriComando.put(Stato.INCANTESIMO_SCELTO, this::gestisciComandoInStatoIncantesimoScelto);
		gestoriComando.put(Stato.ATTESA_SI_NO, this::gestisciComandoInStatoAttesaSiNo);
		gestoriComando.put(Stato.ATTESA_RISPOSTA_MISSIONE, this::gestisciComandoInStatoAttesaRispostaMissione);
		gestoriComando.put(Stato.FINE_LOCAZIONE, this::eseguiFineLocazione);
		gestoriComando.put(Stato.SCELTA_DIREZIONE, this::gestisciComandoInStatoSceltaDirezione);
		gestoriComando.put(Stato.SCELTA_PASSI, this::gestisciComandoInStatoSceltaPassi);
		gestoriComando.put(Stato.ATTESA_POZIONE_SALUTE, this::gestisciComandoInStatoAttesaPozioneSalute);
		gestoriComando.put(Stato.ATTESA_POZIONE_SALUTE_GRANDE, this::gestisciComandoInStatoAttesaPozioneSaluteGrande);
		gestoriComando.put(Stato.ATTESA_POZIONE_MAGIA, this::gestisciComandoInStatoAttesaPozioneMagia);
		gestoriComando.put(Stato.ATTESA_POZIONE_MAGIA_GRANDE, this::gestisciComandoInStatoAttesaPozioneMagiaGrande);
		gestoriComando.put(Stato.SCELTA_FORMULANTE_RESURREZIONE, this::gestisciComandoInStatoSceltaFormulanteResurrezione);
		gestoriComando.put(Stato.SCELTA_BERSAGLIO_RESURREZIONE, this::gestisciComandoInStatoSceltaBersaglioResurrezione);
		gestoriComando.put(Stato.ESECUZIONE_RESURREZIONE, this::gestisciComandoInStatoEsecuzioneResurrezione);
		gestoriComando.put(Stato.MAPPA, this::gestisciComandoInStatoMappa);
		gestoriComando.put(Stato.INVENTARIO, this::gestisciComandoInStatoInventario);
		gestoriComando.put(Stato.TROFEI, this::gestisciComandoInStatoTrofei);
		gestoriComando.put(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE, this::gestisciComandoInStatoSelezioneSalvataggioDaScrivere);
		gestoriComando.put(Stato.CONFERMA_USCITA, this::gestisciComandoInStatoConfermaUscita);
		gestoriComando.put(Stato.GIOCO_PERSO, this::gestisciComandoInStatoGiocoPerso);
		gestoriComando.put(Stato.GIOCO_PERSO_2, this::gestisciComandoInStatoGiocoPerso2);
		gestoriComando.put(Stato.GIOCO_VINTO, this::gestisciComandoInStatoGiocoVinto);
		gestoriComando.put(Stato.GIOCO_VINTO_2, this::gestisciComandoInStatoGiocoVinto2);
		gestoriComando.put(Stato.STATISTICHE, this::gestisciComandoInStatoStatistiche);
		gestoriComando.put(Stato.PUNTEGGI, comando -> entraOGestisciStatoPunteggi());
	}

	public void tick() {
		processaComando(Comando.TIMER);
	}

	/**
	 * Lo stato corrente, per i test.
	 */
	Stato getStato() {
		return stato;
	}

	/**
	 * L'avvio del gioco: il primissimo stato, LOGO_INIZIALE. La UI traccia il logo 3AM e intanto carica le sue
	 * risorse; qui si caricano quelle del motore (grammatiche, generatore di artefatti). Quando entrambi hanno
	 * finito si passa all'INTRO, e al logo non si torna piu'.
	 */
	public void inizia() {
		stato = Stato.LOGO_INIZIALE;
		logoInizialeMostrato = false;
		motorePrecaricato = false;
		BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.LOGO_INIZIALE));
		esecutorePrecaricamento.execute(() -> {
			ProduttoreDiTestiCasuale.precarica();
			GeneratoreArtefatti.precarica();
			BusEventi.pubblica(new InternoPrecaricamentoMotoreCompletato());
		});
	}

	private static void precaricaInBackground(Runnable precaricamento) {
		Thread thread = new Thread(precaricamento, "Precaricamento motore");
		thread.setDaemon(true);
		thread.start();
	}

	private void passaAllIntroSePronto() {
		if (stato == Stato.LOGO_INIZIALE && logoInizialeMostrato && motorePrecaricato) {
			mostraIntro();
		}
	}

	/**
	 * Schermata introduttiva coi titoli. QUi è possibile scegliere se iniziare una nuova partita o
	 * caricare una partita preesistente.
	 */
	private void mostraIntro() {
		stato = Stato.INTRO;
		BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.INTRO, getComandiPossibiliInStatoIntro()));
	}

	private Collection<Comando> getComandiPossibiliInStatoIntro() {
		Collection<Comando> comandiPossibili = new ArrayList<>();
		comandiPossibili.add(Comando.PERGAMENA);
		if (!gestoreSalvataggi.getSalvataggiDisponibili().isEmpty()) {
			comandiPossibili.add(Comando.FLOPPY_CARICA);
		}
		return comandiPossibili;
	}

	private void onEventoTestoDisponibile(ComandoInvioTesto evento) {

		String testoDisponibile = evento.getTesto();

		Esito esito;
		switch (stato) {

			case PRE_GAME_ATTESA_NOME_PERSONAGGIO:
				esito = gestisciTestoInStatoPreGameAttesaNomePersonaggio(testoDisponibile);
				break;

			case ATTESA_NOME_PUNTEGGI:
				esito = gestisciTestoInStatoPostGameAttesaNomePerPunteggio(testoDisponibile);
				break;

			case IN_LOCAZIONE:
				// Una locazione che ha chiesto un testo (es. l'incantatore, per il nome dell'artefatto)
				stato = locazioneCorrente.riceviTesto(gruppo, testoDisponibile);
				esito = esitoDaStatoLocazione(stato);
				break;

			default:
				BusEventi.pubblica(new InternoErrore("onEventoTestoDisponibile: Stato non gestito: " + stato));
				return;
		}
		// Il testo arriva con un evento diverso da ComandoDiGioco, ma le transizioni
		// che ne seguono passano dallo stesso ciclo dei comandi.
		prosegui(esito);
	}

	private void onEventoComandoDiGioco(ComandoDiGioco evento) {
		processaComando(evento.getComando());
	}

	private void comandoNonValido(Comando comando) {
		BusEventi.pubblica(new InternoErrore("Stato: " + stato + " - Comando non valido: " + comando));
	}

	/**
	 * Questa funzione in base allo stato del gruppo e alla azione ricevuta
	 * è il motore di gioco vero e proprio, ed è quindi abbastanza monumentale.
	 * Per alcuni stati il comando ricevuto non serve a nulla, giusto per cambiare
	 * lo stato stesso.
	 * <p>
	 * Non è ricorsiva: un solo ciclo guida tutte le transizioni automatiche in
	 * cascata (uno stato che, appena impostato, deve subito eseguire la propria
	 * logica di ingresso, o subito reagire a un comando inoltrato) finché uno
	 * stato non decide di fermarsi e aspettare un comando reale del giocatore.
	 */
	public void processaComando(Comando comando) {
		if (comando == null) {
			// null è riservato al ciclo interno, dove indica l'ingresso in uno stato
			throw new IllegalArgumentException("processaComando richiede un comando reale");
		}
		BusEventi.pubblica(new InternoMessaggio("Automa in stato " + stato.name() + "; processo Comando " + comando));
		prosegui(eseguiPasso(comando));
	}

	/**
	 * Prosegue le transizioni automatiche a partire dall'esito di un primo passo, finché
	 * uno stato non decide di fermarsi e aspettare un input reale del giocatore.
	 */
	private void prosegui(Esito primoEsito) {
		Esito esito = primoEsito;
		int iterazioni = 0;
		while (esito.continua) {
			if (++iterazioni > MAX_TRANSIZIONI_AUTOMATICHE) {
				throw new IllegalStateException(
						"Troppe transizioni automatiche consecutive (possibile ciclo tra stati) — ultimo stato: " + stato);
			}
			Comando prossimo = esito.prossimoComando;
			BusEventi.pubblica(new InternoMessaggio("Automa in stato " + stato.name() + "; processo Comando " + prossimo));
			esito = eseguiPasso(prossimo);
		}
		// La cascata automatica si è davvero fermata (non solo un passo intermedio):
		// solo ora, se era in sospeso, si può avvisare la UI del ritorno al gioco.
		if (mostraSchermataGiocoAllaRipresa && stato != Stato.INTERMEZZO && stato != Stato.ATTESA_UI_PER_INTERMEZZO) {
			mostraSchermataGiocoAllaRipresa = false;
			BusEventi.pubblica(new InternoMostraSchermataGioco());
		}
	}

	private Esito eseguiPasso(Comando comando) {
		if (comando != null) {
			Function<Comando, Esito> gestore = gestoriComando.get(stato);
			if (gestore == null) {
				comandoNonValido(comando);
				throw new IllegalStateException("Stato " + stato + " non correttamente gestito!");
			}
			return gestore.apply(comando);
		} else {
			Supplier<Esito> gestore = gestoriIngresso.get(stato);
			if (gestore == null) {
				comandoNonValido(null);
				throw new IllegalStateException("Stato " + stato + " non correttamente gestito!");
			}
			return gestore.get();
		}
	}

	private Esito gestisciComandoInStatoIntro(Comando comando) {
		switch (comando) {
			case PERGAMENA:
				stato = Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO;
				BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.NOME_PERSONAGGIO));
				return Esito.FERMATI;
			case FLOPPY_CARICA:
				stato = Stato.PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE;
				Collection<TestataSalvataggio> salvataggiDisponibili = gestoreSalvataggi.getSalvataggiDisponibili();
				BusEventi.pubblica(new RichiestaSelezioneSlotPerRilettura(salvataggiDisponibili));
				return Esito.FERMATI;
			default:
				comandoNonValido(comando);
				return Esito.FERMATI;
		}
	}

	private Esito gestisciComandoInStatoPreGameSelezionaSalvataggioDaLeggere(Comando comando) {
		if (comando != Comando.ANNULLA && gestoreSalvataggi.leggi(comando)) {
			RiletturaPartita.ricostruisci();
			gruppo.getLocazioneCorrente().azzeraLocazione(gruppo);
			stato = Stato.ATTESA_DIREZIONE;
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			// Copia: la lista viva continua a ricevere i messaggi pubblicati da qui in poi
			BusEventi.pubblica(new InternoCaricamentoCompletato(new ArrayList<>(Notizie.getUltimiMessaggi())));
			return Esito.CONTINUA_CON_INGRESSO;
		} else {
			BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.INTRO, getComandiPossibiliInStatoIntro()));
			stato = Stato.INTRO;
			return Esito.FERMATI;
		}
	}

	private Esito gestisciTestoInStatoPreGameAttesaNomePersonaggio(String testoDisponibile) {
		Foresta.reimposta();
		personaggio = null;

		nomePersonaggio = testoDisponibile.trim();
		if (nomePersonaggio.isEmpty()) {
			personaggio = RegistroPersonaggi.getPersonaggioCasuale();
		} else {
			// Qui mettiamo il codice per i personaggi nascosti tipo:
			if (testoDisponibile.equals("OmbraFiamma")) {
				personaggio = new OmbraFiamma("Alakazam", 5);
			}
		}

		if (personaggio != null) {
			// Personaggio casuale o nascosto: si salta la scelta di sesso e classe e,
			// come dopo la scelta della classe, si entra subito nella prima locazione.
			inizializzaGioco();
			return Esito.CONTINUA_CON_INGRESSO;
		}

		stato = Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO;
		BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.SESSO_PERSONAGGIO, Comando.MASCHIO, Comando.FEMMINA));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoPreGameAttesaSessoPersonaggio(Comando comando) {
		stato = Stato.PRE_GAME_ATTESA_CLASSE_PERSONAGGIO;
		if (comando == Comando.FEMMINA) {
			BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.CLASSE_PERSONAGGIO, Comando.GUERRIERA, Comando.LADRA,
					Comando.CANTASTORIE, Comando.ELFA, Comando.MAGA));
		} else {
			BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.CLASSE_PERSONAGGIO, Comando.GUERRIERO, Comando.LADRO,
					Comando.BARDO, Comando.ELFO, Comando.MAGO));
		}
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoPreGameAttesaClassePersonaggio(Comando comando) {
		switch (comando) {
			case GUERRIERA:
				personaggio = new Guerriera(nomePersonaggio, 1);
				break;
			case GUERRIERO:
				personaggio = new Guerriero(nomePersonaggio, 1);
				break;
			case LADRA:
				personaggio = new Ladra(nomePersonaggio, 1);
				break;
			case LADRO:
				personaggio = new Ladro(nomePersonaggio, 1);
				break;
			case CANTASTORIE:
				personaggio = new Cantastorie(nomePersonaggio, 1);
				break;
			case BARDO:
				personaggio = new Bardo(nomePersonaggio, 1);
				break;
			case ELFA:
				personaggio = new Elfa(nomePersonaggio, 1);
				break;
			case ELFO:
				personaggio = new Elfo(nomePersonaggio, 1);
				break;
			case MAGA:
				personaggio = new Maga(nomePersonaggio, 1);
				break;
			case MAGO:
				personaggio = new Mago(nomePersonaggio, 1);
				break;
			default:
				throw new IllegalArgumentException();
		}
		inizializzaGioco();
		return Esito.CONTINUA_CON_INGRESSO;
	}

	/**
	 * Gli intermezzi di apertura vanno mostrati prima di INIZIO_LOCAZIONE: lì partono i
	 * controlli delle missioni, le cui notifiche comparirebbero già durante l'intermezzo.
	 */
	private Esito entraInStatoInizioGioco() {
		return avviaIntermezzi(MomentoIntermezzo.INIZIO_GIOCO, Stato.INIZIO_LOCAZIONE);
	}

	private Esito entraInStatoInizioLocazione() {
		return controllaMissioniEDomande(MomentoControllo.PRE_LOCAZIONE, this::proseguiInizioLocazione);
	}

	private Esito proseguiInizioLocazione() {
		String evento = LineaTemporale.getEvento();
		if (evento != null) {
			BusEventi.pubblica(new NotificaTestoFrase(evento));
			if (LineaTemporale.isGiocoFinito()) {
				stato = Stato.GIOCO_PERSO;
				return Esito.CONTINUA_CON_INGRESSO;
			}
		}
		// Missioni ed eventi del tempo sono aggiornati e la partita non è persa: è il
		// momento degli intermezzi, prima che la locazione venga costruita
		return avviaIntermezzi(MomentoIntermezzo.INIZIO_LOCAZIONE, Stato.PREPARAZIONE_LOCAZIONE);
	}

	/**
	 * Comincia un nuovo momento degli intermezzi (vedi RegistroIntermezzi.nuovoMomento) e
	 * mostra il primo che deve scattare; se non ce ne sono prosegue con statoDopo.
	 */
	private Esito avviaIntermezzi(MomentoIntermezzo momento, Stato statoDopo) {
		RegistroIntermezzi.nuovoMomento();
		return avviaProssimoIntermezzo(momento, statoDopo);
	}

	/**
	 * Mostra il prossimo intermezzo che deve scattare nel momento indicato; se non ce
	 * ne sono (più) prosegue con statoDopo. Dopo ogni intermezzo si torna qui, così più
	 * intermezzi scattati nello stesso momento vengono mostrati uno dopo l'altro.
	 */
	private Esito avviaProssimoIntermezzo(MomentoIntermezzo momento, Stato statoDopo) {
		// Senza intermezzi da mostrare non c'è motivo di aspettare che la UI finisca le
		// sue animazioni: si prosegue subito
		if (RegistroIntermezzi.getProssimoIntermezzo(momento) == null) {
			stato = statoDopo;
			return Esito.CONTINUA_CON_INGRESSO;
		}
		if (uiOccupata) {
			momentoIntermezzoInAttesa = momento;
			statoDopoIntermezzoInAttesa = statoDopo;
			stato = Stato.ATTESA_UI_PER_INTERMEZZO;
			// In attesa nessun comando è applicabile: la barra delle icone va svuotata
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili());
			return Esito.FERMATI;
		}
		Intermezzo intermezzo;
		List<PaginaIntermezzo> pagine;
		do {
			intermezzo = RegistroIntermezzi.getProssimoIntermezzo(momento);
			if (intermezzo == null) {
				stato = statoDopo;
				return Esito.CONTINUA_CON_INGRESSO;
			}
			// Segnato subito, così non viene riproposto: né dopo essere stato mostrato,
			// né se non ha pagine (in quel caso si passa semplicemente al successivo)
			RegistroIntermezzi.segnaScattato(intermezzo);
			pagine = intermezzo.getPagine();
		} while (pagine == null || pagine.isEmpty());
		intermezzoCorrente = intermezzo;
		pagineIntermezzo = pagine;
		paginaIntermezzo = 0;
		momentoIntermezzo = momento;
		statoDopoIntermezzi = statoDopo;
		stato = Stato.INTERMEZZO;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoIntermezzo() {
		mostraPaginaIntermezzo();
		return Esito.FERMATI;
	}

	private void mostraPaginaIntermezzo() {
		inizioPaginaIntermezzo = orologioNanosecondi.getAsLong();
		BusEventi.pubblica(new NotificaPaginaIntermezzo(pagineIntermezzo.get(paginaIntermezzo),
				paginaIntermezzo + 1, pagineIntermezzo.size()));
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		avviaTimerIntermezzo(0);
	}

	/**
	 * Il conto alla rovescia della pagina, se avanza da sola, da quei secondi dalla sua comparsa.
	 */
	private void avviaTimerIntermezzo(double secondiTrascorsi) {
		double secondi = pagineIntermezzo.get(paginaIntermezzo).getSecondiPrimaDiAvanzare(intermezzoCorrente.getSecondiPerPagina());
		if (secondi > 0) {
			// Riavviato a ogni pagina e a ogni fumetto saltato: un click riporta a zero il conto alla rovescia
			temporizzatore.iniziaDopo((int) Math.max(1, Math.ceil((secondi - secondiTrascorsi) * 1_000)));
		} else {
			// Pagina che avanza solo al click: non deve scattare il timer della precedente
			temporizzatore.termina();
		}
	}

	/**
	 * Il primo fumetto della pagina corrente che non è ancora cominciato, se c'è.
	 */
	private Optional<BattutaProgrammata> getFumettoSuccessivo() {
		double trascorsi = (orologioNanosecondi.getAsLong() - inizioPaginaIntermezzo) / 1_000_000_000.0;
		return pagineIntermezzo.get(paginaIntermezzo).getBattuteProgrammate().stream()
				.filter(battuta -> battuta.getInizio() > trascorsi)
				.min(Comparator.comparingDouble(BattutaProgrammata::getInizio));
	}


	/**
	 * La pergamena salta al fumetto successivo della pagina, se ce n'è ancora uno da cominciare; altrimenti, come il
	 * timer, fa avanzare la pagina. Dopo l'ultima si passa al prossimo intermezzo o si riprende il gioco.
	 */
	private Esito gestisciComandoInStatoIntermezzo(Comando comando) {
		if (comando != Comando.PERGAMENA && comando != Comando.TIMER) {
			comandoNonValido(comando);
			return Esito.FERMATI;
		}
		if (comando == Comando.PERGAMENA) {
			Optional<BattutaProgrammata> fumetto = getFumettoSuccessivo();
			if (fumetto.isPresent()) {
				double secondi = fumetto.get().getInizio();
				inizioPaginaIntermezzo = orologioNanosecondi.getAsLong() - (long) (secondi * 1_000_000_000L);
				BusEventi.pubblica(new InternoFumettoSuccessivo(secondi));
				avviaTimerIntermezzo(secondi);
				return Esito.FERMATI;
			}
		}
		paginaIntermezzo++;
		if (paginaIntermezzo < pagineIntermezzo.size()) {
			mostraPaginaIntermezzo();
			return Esito.FERMATI;
		}
		temporizzatore.termina();
		intermezzoCorrente = null;
		pagineIntermezzo = null;
		Esito esito = avviaProssimoIntermezzo(momentoIntermezzo, statoDopoIntermezzi);
		if (stato != Stato.INTERMEZZO) {
			// Il ritorno al gioco va segnalato solo a cascata automatica completamente ferma
			// (vedi prosegui()): questo passo potrebbe non essere l'ultimo, per esempio se
			// stato è uno stato intermedio che scatenerà subito un altro intermezzo, o se la
			// UI non è ancora pronta e si finirà comunque in ATTESA_UI_PER_INTERMEZZO.
			mostraSchermataGiocoAllaRipresa = true;
		}
		return esito;
	}

	private Esito entraInStatoPreparazioneLocazione() {
		gruppoAvversario.reimposta();
		gruppo.svuotaPanchina();
		gruppo.getPersonaggiVivi().forEach(Personaggio::rimuoviTuttiGliEffettiDiStato);
		locazioneCorrente = Foresta.costruisciIstanza(gruppo.getCoordinate());
		gruppo.setLocazioneCorrente(locazioneCorrente);
		locazioneCorrente.crea(gruppo, gruppoAvversario);
		if (RegistroMissioni.isDaSopprimere(gruppo.getCoordinate())) {
			// Una missione è appena arrivata qui e considera il posto sicuro (un tempio, un posto di un rito...):
			// niente avversari né oggetti a caso
			gruppoAvversario.rimuoviPersonaggi();
			locazioneCorrente.rimuoviOggetto();
		} else {
			// Qui e non in crea, che molte locazioni ridefiniscono: gli avversari che una missione vuole qui, al posto di
			// quelli della locazione...
			RegistroMissioni.getIncontroMissione(gruppo.getCoordinate()).ifPresent(avversari -> {
				gruppoAvversario.rimuoviPersonaggi();
				avversari.forEach(gruppoAvversario::aggiungiPersonaggio);
				// ...con le ondate che arrivano dopo, se la missione ne vuole (vedi LocazioneBase.impostaAzioni)
				gruppoAvversario.setOndateSuccessive(RegistroMissioni.getOndateSuccessiveMissione(gruppo.getCoordinate()));
			});
			// ...e l'oggetto che una missione vuole qui, che può dipendere dagli avversari (i trofei di una caccia)
			RegistroMissioni.getOggettoMissione(gruppo.getCoordinate(), gruppo.getTipoLocazioneCorrente(),
							Foresta.isLocazioneVisitata(gruppo.getCoordinate()))
					.ifPresent(locazioneCorrente::collocaOggettoMissione);
		}
		BusEventi.pubblica(new InternoPreparazioneLocazione());
		BusEventi.pubblica(new NotificaTestoParagrafo(LineaTemporale.getDescrizioneOraDelGiorno()));
		locazioneCorrente.descrivi(gruppo, gruppoAvversario);
		// Dove una missione finita aveva la sua locazione, per esempio fra le rovine di un castello sconfitto
		RegistroMissioni.getRicordo(gruppo.getCoordinate())
				.ifPresent(ricordo -> BusEventi.pubblica(new NotificaTestoFrase(ricordo)));
		return controllaMissioniEDomande(MomentoControllo.IN_LOCAZIONE, this::proseguiPreparazioneLocazione);
	}

	private Esito proseguiPreparazioneLocazione() {
		/*
		 * Ogni locazione ha un metodo impostaAzioni; nel caso delle
		 * locazioni di base imposterà le azioni combattimento,
		 * incantesimo, corruzione, amicizia... Mentre per alcune
		 * locazioni specifiche permetterà di accettare la proposta
		 * di aggregazione di altri personaggi eccetera. Se la locazione
		 * è automaticamente completata il metodo torna LOCAZIONE_COMPLETA.
		 * Altrimenti ogni locazione è in effetti un automa a stati finiti
		 * che tiene traccia del suo stato.
		 */
		stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, null);
		/*
		 * Se si resta IN_LOCAZIONE il giocatore si trova davanti la scelta
		 * delle azioni che puo' intraprendere.
		 */
		return esitoDaStatoLocazione(stato);
	}

	/**
	 * Il ritorno da mappa o inventario non è un'azione del giocatore: la locazione va solo
	 * ripresentata, non fatta avanzare. Per questo non si usa impostaAzioni(..., null), che
	 * in LocazioneBase fa trascorrere un turno (danni da effetti di stato, possibile fine
	 * della locazione o del gioco) e nelle altre locazioni può eseguire il passo successivo.
	 */
	private Esito entraInStatoInLocazione() {
		locazioneCorrente.ripresentaComandi();
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoInLocazione(Comando comando) {
		if (comando == Comando.INVENTARIO) {
			statoPrecedente = Stato.IN_LOCAZIONE;
			stato = Stato.INVENTARIO;
			richiediAperturaInventarioGruppo();
			return Esito.CONTINUA_CON_INGRESSO;
		}
		MomentoIntermezzo momentoIngressoNegozio = momentoIngressoNegozio(comando);
		if (momentoIngressoNegozio != null) {
			// Prima di entrare nel negozio (locanda compresa) si dà modo al suo intermezzo
			// di scattare; locazioneCorrente.impostaAzioni(...) viene chiamato solo dopo,
			// da entraInStatoIngressoNegozio()
			comandoNegozioInAttesa = comando;
			return avviaIntermezzi(momentoIngressoNegozio, Stato.INGRESSO_NEGOZIO);
		}
		statoPrecedente = stato;
		/*
		 * Continuiamo a fornire all'automa a stati finiti della
		 * locazione la possibilità di andare avanti fino a
		 * LOCAZIONE_COMPLETA
		 */
		stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, comando);
		return esitoDaStatoLocazione(stato);
	}

	/**
	 * Il comando con cui si entra in un negozio di città (locanda compresa), se comando
	 * è uno di questi, altrimenti null. La locanda in città riusa gli stessi intermezzi
	 * delle locande nel bosco; gli altri negozi hanno un intermezzo proprio, mostrato una
	 * sola volta per l'intera partita.
	 */
	private static MomentoIntermezzo momentoIngressoNegozio(Comando comando) {
		switch (comando) {
			case LOCANDA:
				return MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA;
			case ALCHIMISTA:
				return MomentoIntermezzo.INGRESSO_ALCHIMISTA;
			case ARMAIOLO:
				return MomentoIntermezzo.INGRESSO_ARMAIOLO;
			case VENDITORE_DI_PERGAMENE:
				return MomentoIntermezzo.INGRESSO_VENDITORE_DI_PERGAMENE;
			case INCANTATORE:
				return MomentoIntermezzo.INGRESSO_INCANTATORE;
			default:
				return null;
		}
	}

	/**
	 * Dopo l'eventuale intermezzo di ingresso nel negozio, esegue finalmente il comando
	 * con cui ci si era entrati.
	 */
	private Esito entraInStatoIngressoNegozio() {
		Comando comando = comandoNegozioInAttesa;
		comandoNegozioInAttesa = null;
		statoPrecedente = Stato.IN_LOCAZIONE;
		stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, comando);
		if (comando != Comando.LOCANDA) {
			// Armaiolo, alchimista, venditore di pergamene e incantatore hanno già richiesto,
			// nella chiamata sopra, la loro finestra specifica (ComandoAperturaInventario*/
			// RichiestaAperturaIncantatore): non bisogna poi sovrascriverla con la schermata di
			// gioco normale (vedi mostraSchermataGiocoAllaRipresa in prosegui()), a differenza
			// della locanda, il cui dialogo compare nella normale schermata di gioco.
			mostraSchermataGiocoAllaRipresa = false;
		}
		return esitoDaStatoLocazione(stato);
	}

	/**
	 * Dopo l'eventuale intermezzo di accampamento, il gruppo passa davvero la notte.
	 */
	private Esito entraInStatoAccampamento() {
		// Dalla casella, non da locazioneCorrente: dopo un caricamento questa e' null, e dopo un castello
		// completato e' ancora il castello mentre sulla casella ci sono le rovine
		gruppo.pernotta(Foresta.costruisciIstanza(gruppo.getCoordinate()).getTipoRiposo());
		LineaTemporale.mattinoSeguente();
		LineaTemporale.eventi(gruppo);
		stato = Stato.ATTESA_DIREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	/**
	 * Traduce lo stato restituito da Locazione.impostaAzioni nel passo successivo
	 * dell'automa: IN_LOCAZIONE attende il giocatore, IN_COMBATTIMENTO avvia il battito
	 * dei round e attende, qualunque altro stato viene eseguito subito (fermando il
	 * battito se la locazione o il gioco sono finiti).
	 */
	private Esito esitoDaStatoLocazione(Stato statoLocazione) {
		if (statoLocazione == Stato.IN_LOCAZIONE) {
			return Esito.FERMATI;
		}
		if (statoLocazione == Stato.IN_COMBATTIMENTO) {
			temporizzatore.inizia(1_000);
			return Esito.FERMATI;
		}
		if (statoLocazione == Stato.GIOCO_PERSO || statoLocazione == Stato.GIOCO_VINTO || statoLocazione == Stato.FINE_LOCAZIONE) {
			temporizzatore.termina();
		}
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito gestisciComandoInStatoInCombattimento(Comando comando) {
		statoPrecedente = stato;
		stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, comando);
		if (stato != Stato.IN_COMBATTIMENTO) {
			temporizzatore.termina();
			return Esito.CONTINUA_CON_INGRESSO;
		}
		if (comando != Comando.TIMER) {
			// Il battito del combattimento viene interrotto ogni volta che si
			// esce da IN_COMBATTIMENTO (per esempio per scegliere chi combatte):
			// qui lo si riavvia, dato che i round sono guidati da Comando.TIMER.
			temporizzatore.inizia(1_000);
		}
		return Esito.FERMATI;
	}

	private Esito entraInStatoSceltaAutomaticaPersonaggio() {
		Comando comandoRisolto = scegliPersonaggio(false);
		if (comandoRisolto != null) {
			Logger.log(Stato.SCELTA_AUTOMATICA_PERSONAGGIO.name() + ": Torno allo stato " + statoPrecedente.name());
			stato = statoPrecedente;
			return Esito.continuaCon(comandoRisolto);
		} else {
			stato = Stato.SCELTA_MANUALE_PERSONAGGIO;
			return Esito.CONTINUA_CON_INGRESSO;
		}
	}

	private Esito entraInStatoSceltaPersonaggioQualsiasi() {
		Comando comandoRisolto = scegliPersonaggio(true);
		if (comandoRisolto != null) {
			Logger.log(Stato.SCELTA_PERSONAGGIO_QUALSIASI.name() + ": Torno allo stato " + statoPrecedente.name());
			stato = statoPrecedente;
			return Esito.continuaCon(comandoRisolto);
		} else {
			stato = Stato.SCELTA_MANUALE_PERSONAGGIO;
			return Esito.CONTINUA_CON_INGRESSO;
		}
	}

	private Esito entraInStatoSceltaManualePersonaggio() {
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoSceltaManualePersonaggio(Comando comando) {
		Logger.log(Stato.SCELTA_MANUALE_PERSONAGGIO.name() + ": Torno allo stato " + statoPrecedente.name());
		stato = statoPrecedente;
		return Esito.continuaCon(comando);
	}

	/**
	 * Come SCELTA_AUTOMATICA_PERSONAGGIO, ma al posto di ANNULLA c'è GRUPPO: rinunciare
	 * all'oggetto non ha senso, mentre lo si può riporre nell'inventario del gruppo.
	 * Con ANNULLA, per giunta, Oggetto.prendi riceveva un comando che non indica nessun
	 * personaggio e Gruppo.getPersonaggio andava fuori dalla lista.
	 * Si propongono solo i candidati (vedi comandiDestinatarioOggetto); di solito ce ne sono
	 * almeno due, perché con uno solo o nessuno ci pensa già Artefatto.raccogli.
	 */
	private Esito entraInStatoSceltaDestinatarioOggetto() {
		List<Comando> comandiPossibili = comandiDestinatarioOggetto();
		if (comandiPossibili.size() <= 1) {
			stato = statoPrecedente;
			return Esito.continuaCon(comandiPossibili.isEmpty() ? Comando.GRUPPO : comandiPossibili.get(0));
		}
		comandiPossibili.add(Comando.GRUPPO);
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibili));
		return Esito.FERMATI;
	}

	/**
	 * I personaggi a cui si può dare l'oggetto della locazione: se porta un artefatto, quelli che
	 * possono equipaggiarlo (Artefatto.candidati), altrimenti tutti i vivi.
	 */
	private List<Comando> comandiDestinatarioOggetto() {
		Oggetto oggetto = locazioneCorrente.getOggetto();
		Optional<Artefatto> artefatto = oggetto == null ? Optional.empty() : oggetto.getArtefatto();
		if (artefatto.isPresent()) {
			return Artefatto.candidati(gruppo, artefatto.get());
		}
		List<Comando> vivi = new ArrayList<>();
		int i = 0;
		for (Personaggio personaggioCorrente : gruppo.getPersonaggi()) {
			if (personaggioCorrente.isVivo()) {
				vivi.add(Comando.ofPersonaggio(i));
			}
			i++;
		}
		return vivi;
	}

	private Esito gestisciComandoInStatoSceltaDestinatarioOggetto(Comando comando) {
		if (comando != Comando.GRUPPO && !comandiDestinatarioOggetto().contains(comando)) {
			comandoNonValido(comando);
			return Esito.FERMATI;
		}
		stato = statoPrecedente;
		return Esito.continuaCon(comando);
	}

	private Esito entraInStatoSceltaIncantesimoDaLanciare() {
		List<Comando> comandiPossibili = new ArrayList<>();
		Personaggio formulante = gruppo.getFormulante();
		// Mago ed Elfo hanno anche il dardo arcano, che non consuma pergamene
		if (DardoArcano.puoLanciarlo(formulante)) {
			comandiPossibili.add(Comando.DARDO_ARCANO);
		}
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			if (gruppo.getIncantesimi(classeIncantesimo) > 0 && formulante.getMagia() >= FabbricaIncantesimi.crea(classeIncantesimo, formulante.getLivello()).getCostoLancio()) {
				comandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
			}
		}
		comandiPossibili.add(Comando.NO_INCANTESIMO);
		BusEventi.pubblica(new RichiestaSelezioneIncantesimoDaLanciare(comandiPossibili));
		stato = Stato.INCANTESIMO_SCELTO;
		return Esito.FERMATI;
	}

	private Esito entraInStatoAttesaIncantesimoQualsiasi() {
		List<Comando> comandiPossibili = new ArrayList<>();
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			comandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
		}
		comandiPossibili.add(Comando.NO_INCANTESIMO);
		BusEventi.pubblica(new RichiestaSelezioneIncantesimoDaLanciare(comandiPossibili));
		stato = Stato.INCANTESIMO_SCELTO;
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoIncantesimoScelto(Comando comando) {
		stato = Stato.IN_LOCAZIONE;
		return Esito.continuaCon(comando);
	}

	private Esito entraInStatoAttesaSiNo() {
		BusEventi.pubblica(new RichiestaSelezioneSiNo());
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoAttesaSiNo(Comando comando) {
		if (comando != Comando.TIMER) {
			stato = statoPrecedente;
			return Esito.continuaCon(comando);
		}
		return Esito.FERMATI;
	}

	/**
	 * Corpo condiviso fra ingresso automatico (comando nullo) e reazione a un
	 * comando reale: la logica di FINE_LOCAZIONE non distingue i due casi, usa
	 * "comando" solo come dato da passare a Oggetto.prendi(...).
	 */
	private Esito eseguiFineLocazione(Comando comando) {
		temporizzatore.termina();
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		// Chi si era arreso torna in campo: il combattimento è finito
		gruppo.svuotaPanchina();

		// Recuperiamo l'oggetto se fattibile
		if (locazioneCorrente.isCompleta()) {
			if (!locazioneCorrente.isHaStrettoAmicizia()) {
				Oggetto oggetto = locazioneCorrente.getOggetto();
				if (oggetto != null) {
					BusEventi.pubblica(new InternoMessaggio("Tentativo di recupero oggetto utilizzando l'azione " + comando));
					if (!oggetto.prendi(gruppo, comando)) {
						BusEventi.pubblica(new InternoMessaggio("L'oggetto non si lascia prendere con l'azione " + comando));
						statoPrecedente = Stato.FINE_LOCAZIONE;
						stato = Stato.SCELTA_DESTINATARIO_OGGETTO;
						return Esito.CONTINUA_CON_INGRESSO;
					} else {
						BusEventi.pubblica(new NotificaRaccoltaOggetti());
						BusEventi.pubblica(new InternoOggettoRaccolto(oggetto.getClasse(), oggetto.getQuantita(),
								oggetto.getArtefatto().orElse(null), locazioneCorrente.isCustodita()));
						// L'artefatto di un tempio (quello del registro) si mostra con la rivelazione, come quelli dei cofani
						if (oggetto.getClasse() == TipoOggetto.ARTEFATTO) {
							oggetto.getArtefatto().ifPresent(a -> BusEventi.pubblica(new NotificaArtefattoTrovato(a, Statistiche.getLivello())));
						}
						BusEventi.pubblica(new InternoMessaggio("Oggetto raccolto."));
						locazioneCorrente.rimuoviOggetto();
					}
				}
			}
			// Le missioni vanno controllate prima di azzerare la locazione altrimenti la
			// distruzione di un castello con sostituzione con rovine non fa completare le
			// missioni. Potremmo anche salvare il tipo di locazione nelle missioni ma così
			// mi pare più pulito.
			return controllaMissioniEDomande(MomentoControllo.POST_LOCAZIONE, () -> {
				locazioneCorrente.azzeraLocazione(gruppo);
				return concludiFineLocazione();
			});
		}
		// Anche da una locazione lasciata a metà (una fuga, un duello perso, un passaggio inosservato) le missioni
		// vanno controllate: possono essere successe cose che contano, come un capo abbattuto prima di fuggire
		return controllaMissioniEDomande(MomentoControllo.POST_LOCAZIONE, this::concludiFineLocazione);
	}

	private Esito concludiFineLocazione() {
		// Anche se la locazione non è completa: chi deve, per esempio i trofei, se ne accorge
		BusEventi.pubblica(new InternoFineLocazione());

		// Missioni ed eventuale locazione azzerate: è il momento degli intermezzi, prima
		// del controllo di game over e del resto della coda di fine locazione
		return avviaIntermezzi(MomentoIntermezzo.LOCAZIONE_COMPLETATA, Stato.FINE_LOCAZIONE_2);
	}

	private Esito entraInStatoFineLocazione2() {
		if (LineaTemporale.isGiocoFinito()) {
			if (RegistroMissioni.getMissionePrincipale().isCompleta()) {
				stato = Stato.GIOCO_VINTO;
			} else {
				stato = Stato.GIOCO_PERSO;
			}
			return Esito.CONTINUA_CON_INGRESSO;
		}

		// Controlliamo i personaggi "a tempo"
		for (Personaggio personaggioCorrente : gruppo.getPersonaggiVivi()) {
			if (personaggioCorrente.isATempo()) {
				int tempo = personaggioCorrente.decrementaTempo();
				if (tempo == 0) {
					gruppo.rimuoviPersonaggio(personaggioCorrente);
				}
			}
		}

		// Aumentiamo la stanchezza, e fra una locazione e l'altra si riprende un po' di salute e di magia
		for (Personaggio personaggioCorrente : gruppo.getPersonaggiVivi()) {
			personaggioCorrente.addStanchezza(1);
			personaggioCorrente.addSalute(personaggioCorrente.getRigenerazioneSalute());
			personaggioCorrente.addMagia(personaggioCorrente.getRigenerazioneMagia());
		}

		Statistiche.incrementaTurniGiocati();

		stato = Stato.ATTESA_DIREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoAttesaDirezione() {
		stato = Stato.SCELTA_DIREZIONE;
		BusEventi.pubblica(new NotificaTestoParagrafo(gruppo.chiMaiuscolo() + " se ne va. In quale direzione si incammina?"));
		BusEventi.pubblica(new RichiestaSelezioneDirezione(getComandiPossibiliInStatoAttesaDirezione()));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoSceltaDirezione(Comando comando) {
		// Occorre memorizzare l'informazione sulla direzione
		switch (comando) {
			case MAPPA:
				statoPrecedente = Stato.ATTESA_DIREZIONE;
				stato = Stato.MAPPA;
				return Esito.CONTINUA_CON_INGRESSO;
			case INVENTARIO:
				statoPrecedente = Stato.ATTESA_DIREZIONE;
				stato = Stato.INVENTARIO;
				richiediAperturaInventarioGruppo();
				return Esito.CONTINUA_CON_INGRESSO;
			case ACCAMPAMENTO:
				// L'icona viene offerta solo se ci si può accampare: un comando arrivato
				// comunque si rifiuta, o l'intermezzo dell'accampamento non avrebbe chi mostrare
				if (!isAccampamentoPossibile()) {
					comandoNonValido(comando);
					return Esito.FERMATI;
				}
				// Il contatore incrementa qui, una sola volta per accampamento, e non
				// dentro IntermezzoAccampamento: lì verrebbe ricontrollato più volte in
				// cascata (vedi gestisciComandoInStatoIntermezzo) finendo per scattare più
				// volte nello stesso accampamento invece che una volta per accampamento.
				ModelloDati.getIstanza().getIntermezziMD().incrementaNumeroAccampamenti();
				// Poi le missioni che nascono intorno al fuoco (vedi LaLealta), e si dà modo agli intermezzi
				// dell'accampamento di scattare; la notte passa solo dopo, in entraInStatoAccampamento()
				return controllaMissioniEDomande(MomentoControllo.ACCAMPAMENTO,
						() -> avviaIntermezzi(MomentoIntermezzo.ACCAMPAMENTO, Stato.ACCAMPAMENTO));
			case POZIONE_SALUTE:
				statoPrecedente = Stato.ATTESA_POZIONE_SALUTE;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				return Esito.CONTINUA_CON_INGRESSO;
			case POZIONE_SALUTE_GRANDE:
				statoPrecedente = Stato.ATTESA_POZIONE_SALUTE_GRANDE;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				return Esito.CONTINUA_CON_INGRESSO;
			case POZIONE_MAGIA:
				statoPrecedente = Stato.ATTESA_POZIONE_MAGIA;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				return Esito.CONTINUA_CON_INGRESSO;
			case POZIONE_MAGIA_GRANDE:
				statoPrecedente = Stato.ATTESA_POZIONE_MAGIA_GRANDE;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				return Esito.CONTINUA_CON_INGRESSO;
			case RESURREZIONE:
				stato = Stato.SCELTA_FORMULANTE_RESURREZIONE;
				return Esito.CONTINUA_CON_INGRESSO;
			case NORD:
				direzione = Comando.NORD;
				comandiPossibiliPerNumeroPassi = getComandiPossibiliPerNumeroPassi(gruppo.getMaxPassiNord());
				break;
			case EST:
				direzione = Comando.EST;
				comandiPossibiliPerNumeroPassi = getComandiPossibiliPerNumeroPassi(gruppo.getMaxPassiEst());
				break;
			case SUD:
				direzione = Comando.SUD;
				comandiPossibiliPerNumeroPassi = getComandiPossibiliPerNumeroPassi(gruppo.getMaxPassiSud());
				break;
			case OVEST:
				direzione = Comando.OVEST;
				comandiPossibiliPerNumeroPassi = getComandiPossibiliPerNumeroPassi(gruppo.getMaxPassiOvest());
				break;
			case FLOPPY_SALVA:
				stato = Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE;
				// Le testate dei salvataggi per la schermata di scelta: la UI non legge i salvataggi da sé
				BusEventi.pubblica(new RichiestaSelezioneSlotPerSalvataggio(gestoreSalvataggi.getSalvataggiDisponibili()));
				BusEventi.pubblica(new InternoFaseDiGioco(FaseDiGioco.SALVATAGGIO_DA_SCRIVERE,
						Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5,
						Comando.NO));
				return Esito.FERMATI;
			default:
				// Comando inatteso: si resta ad aspettare la direzione
				BusEventi.pubblica(new InternoMessaggio("Automa in stato " + stato.name() + ": ignoro il Comando " + comando));
				return Esito.FERMATI;
		}
		stato = Stato.SCELTA_PASSI;
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibiliPerNumeroPassi));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoSceltaPassi(Comando comando) {
		if (comando == Comando.ANNULLA) {
			// Scegliere la direzione ha solo memorizzato il campo direzione, che verrà
			// riscritto alla prossima scelta: si può tornare indietro senza nulla da disfare.
			stato = Stato.ATTESA_DIREZIONE;
			return Esito.CONTINUA_CON_INGRESSO;
		}
		if (!comandiPossibiliPerNumeroPassi.contains(comando)) {
			// Solo NUMERO_1 .. NUMERO_n, dove n sono i passi possibili in quella direzione
			BusEventi.pubblica(new InternoMessaggio("Automa in stato " + stato.name() + ": ignoro il Comando " + comando));
			return Esito.FERMATI;
		}
		int passi = comando.ordinal() - Comando.NUMERO_1.ordinal() + 1;
		switch (direzione) {
			case NORD:
				gruppo.muoveNord(passi);
				break;
			case EST:
				gruppo.muoveEst(passi);
				break;
			case SUD:
				gruppo.muoveSud(passi);
				break;
			case OVEST:
				gruppo.muoveOvest(passi);
				break;
			default:
				throw new IllegalArgumentException();
		}
		LineaTemporale.aggiungiOre(passi);
		LineaTemporale.eventi(gruppo);
		stato = Stato.INIZIO_LOCAZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito gestisciComandoInStatoAttesaPozioneSalute(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneSalute(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito gestisciComandoInStatoAttesaPozioneSaluteGrande(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneSaluteGrande(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito gestisciComandoInStatoAttesaPozioneMagia(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneMagia(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito gestisciComandoInStatoAttesaPozioneMagiaGrande(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneMagiaGrande(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	/**
	 * Come per gli incantesimi nelle locazioni (LocazioneBase, CHI_FORMULA), prima si
	 * sceglie chi formula: se un solo personaggio ha la magia sufficiente lo si prende
	 * direttamente, altrimenti si chiede quale fra quelli che possono.
	 */
	private Esito entraInStatoSceltaFormulanteResurrezione() {
		List<Personaggio> formulanti = trovaFormulantiResurrezione();
		if (formulanti.size() == 1) {
			formulanteResurrezione = formulanti.get(0);
			stato = Stato.SCELTA_BERSAGLIO_RESURREZIONE;
			return Esito.CONTINUA_CON_INGRESSO;
		}
		List<Comando> comandiPossibili = new ArrayList<>();
		for (Personaggio formulante : formulanti) {
			comandiPossibili.add(Comando.ofPersonaggio(gruppo.getPersonaggi().indexOf(formulante)));
		}
		comandiPossibili.add(Comando.ANNULLA);
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibili));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoSceltaFormulanteResurrezione(Comando comando) {
		if (comando == Comando.ANNULLA) {
			stato = Stato.ATTESA_DIREZIONE;
			return Esito.CONTINUA_CON_INGRESSO;
		}
		formulanteResurrezione = gruppo.getPersonaggio(comando);
		stato = Stato.SCELTA_BERSAGLIO_RESURREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoSceltaBersaglioResurrezione() {
		// Come nella scelta del bersaglio quando si formula un incantesimo in
		// combattimento (Stato.SCELTA_PERSONAGGIO_QUALSIASI): se c'è un solo
		// personaggio morto lo si risuscita direttamente, altrimenti si chiede quale.
		Comando comandoRisolto = scegliPersonaggioMorto();
		if (comandoRisolto != null) {
			stato = Stato.ESECUZIONE_RESURREZIONE;
			return Esito.continuaCon(comandoRisolto);
		}
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoSceltaBersaglioResurrezione(Comando comando) {
		// Il bersaglio scelto (o ANNULLA) passa all'esecuzione
		stato = Stato.ESECUZIONE_RESURREZIONE;
		return Esito.continuaCon(comando);
	}

	private Esito gestisciComandoInStatoEsecuzioneResurrezione(Comando comando) {
		if (comando != Comando.ANNULLA) {
			eseguiResurrezione(formulanteResurrezione, gruppo.getPersonaggio(comando));
		}
		formulanteResurrezione = null;
		stato = Stato.ATTESA_DIREZIONE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoMappa() {
		Logger.log("Stato MAPPA, azione null");
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.SI));
		BusEventi.pubblica(new NotificaTestoParagrafo(gruppo.getCapo().getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " consulta la sua mappa della Foresta."));
		BusEventi.pubblica(new RichiestaVisualizzazioneMappa());
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoMappa(Comando comando) {
		Logger.log("Stato MAPPA, azione " + comando);
		if (comando == Comando.SI) {
			stato = statoPrecedente;
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			return Esito.CONTINUA_CON_INGRESSO;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private Esito entraInStatoInventario() {
		Logger.log("Stato INVENTARIO, azione null");
		richiediAperturaInventarioGruppo();
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoInventario(Comando comando) {
		Logger.log("Stato INVENTARIO, azione " + comando);
		switch (comando) {
			case ANNULLA:
				stato = statoPrecedente;
				BusEventi.pubblica(new InternoMostraSchermataGioco());
				return Esito.CONTINUA_CON_INGRESSO;
			case PERSONAGGIO_1:
			case PERSONAGGIO_2:
			case PERSONAGGIO_3:
			case PERSONAGGIO_4:
			case PERSONAGGIO_5:
				indicePersonaggioInventario = comando.ordinal() - Comando.PERSONAGGIO_1.ordinal();
				richiediAperturaInventarioGruppo();
				return Esito.FERMATI;
			case MOSTRA_TROFEI:
				stato = Stato.TROFEI;
				return Esito.CONTINUA_CON_INGRESSO;
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * La pagina dei trofei: l'unico comando è ANNULLA, che torna all'inventario.
	 */
	private Esito entraInStatoTrofei() {
		BusEventi.pubblica(new RichiestaAperturaTrofei(Collections.singletonList(Comando.ANNULLA)));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoTrofei(Comando comando) {
		if (comando != Comando.ANNULLA) {
			throw new IllegalArgumentException();
		}
		stato = Stato.INVENTARIO;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	/**
	 * NOTA: rispetto al codice originale qui è stato corretto un bug — un secondo
	 * "if" indipendente (non un else-if) faceva sì che rispondere NO alla richiesta
	 * dello slot di salvataggio innescasse comunque anche il salvataggio (in uno
	 * slot "NO" inesistente) e la richiesta di conferma-uscita dal gioco.
	 */
	private Esito gestisciComandoInStatoSelezioneSalvataggioDaScrivere(Comando comando) {
		if (comando == Comando.NO) {
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			stato = Stato.ATTESA_DIREZIONE;
			return Esito.CONTINUA_CON_INGRESSO;
		} else if (comando != null) {
			if (comando != Comando.ANNULLA && !gestoreSalvataggi.salva(comando)) {
				// Senza un salvataggio valido non si propone di uscire: si torna al gioco
				BusEventi.pubblica(new NotificaTestoParagrafo("Il salvataggio non è riuscito."));
				BusEventi.pubblica(new InternoMostraSchermataGioco());
				stato = Stato.ATTESA_DIREZIONE;
				return Esito.CONTINUA_CON_INGRESSO;
			}
			BusEventi.pubblica(new RichiestaUscitaDalGioco());
			stato = Stato.CONFERMA_USCITA;
			return Esito.CONTINUA_CON_INGRESSO;
		}
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoConfermaUscita(Comando comando) {
		if (comando == Comando.SI) {
			System.exit(0);
		} else if (comando == Comando.NO) {
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			stato = Stato.ATTESA_DIREZIONE;
			return Esito.CONTINUA_CON_INGRESSO;
		}
		return Esito.FERMATI;
	}

	private Esito entraInStatoGiocoPerso() {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoGiocoPerso(Comando comando) {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		stato = Stato.GIOCO_PERSO_2;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoGiocoPerso2() {
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		BusEventi.pubblica(new NotificaFineGioco(false));
		// La prima pagina deve restare per un periodo intero, come nell'intro
		temporizzatore.iniziaDopo(5_000);
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoGiocoPerso2(Comando comando) {
		if (comando == Comando.TIMER) {
			BusEventi.pubblica(new NotificaFineGioco(false));
			return Esito.FERMATI;
		}
		temporizzatore.termina();
		stato = Stato.STATISTICHE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoGiocoVinto() {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoGiocoVinto(Comando comando) {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		stato = Stato.GIOCO_VINTO_2;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoGiocoVinto2() {
		BusEventi.pubblica(new NotificaFineGioco(true));
		// La prima pagina deve restare per un periodo intero, come nell'intro
		temporizzatore.iniziaDopo(5_000);
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoGiocoVinto2(Comando comando) {
		if (comando == Comando.TIMER) {
			BusEventi.pubblica(new NotificaFineGioco(true));
			return Esito.FERMATI;
		}
		temporizzatore.termina();
		stato = Stato.STATISTICHE;
		return Esito.CONTINUA_CON_INGRESSO;
	}

	private Esito entraInStatoStatistiche() {
		BusEventi.pubblica(new NotificaMostraStatisticheFineGioco());
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoStatistiche(Comando comando) {
		if (comando == Comando.PERGAMENA && !gestorePunteggi.isPunteggioInClassifica(Statistiche.getPunti(), Statistiche.getIdPartita())) {
			// Nessun punteggio da registrare: si torna all'intro, che riparte dai loghi.
			// mostraIntro() pubblica anche lo stato INTRO, senza il quale la UI resterebbe
			// sulle statistiche. Al logo iniziale non si torna.
			mostraIntro();
			return Esito.FERMATI;
		}
		if (comando == Comando.PERGAMENA) {
			stato = Stato.ATTESA_NOME_PUNTEGGI;
			BusEventi.pubblica(new RichiestaTesto("congratulazioni! inserisci il tuo nome"));
			// Si aspetta solo il nome: nessuna icona
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili());
			return Esito.FERMATI;
		}
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		return Esito.FERMATI;
	}

	private Esito gestisciTestoInStatoPostGameAttesaNomePerPunteggio(String testoDisponibile) {
		// Senza nome (o con soli spazi) si usa quello del personaggio; se non ne ha uno, quello della sua classe
		if (testoDisponibile.trim().isEmpty()) {
			Personaggio primo = GruppoGiocatore.getIstanza().getPersonaggio(0);
			testoDisponibile = primo.getNomeProprio()
					.orElseGet(() -> primo.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
		}
		gestorePunteggi.addPunteggio(testoDisponibile, Statistiche.getPunti(), Statistiche.getIdPartita());
		stato = Stato.PUNTEGGI;
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		BusEventi.pubblica(new NotificaMostraPunteggiMigliori());
		return Esito.CONTINUA_CON_INGRESSO;
	}

	/**
	 * Ignora sempre il comando ricevuto (sia ingresso automatico sia comando
	 * reale): dopo aver mostrato i punteggi, la partita torna comunque alla
	 * schermata iniziale.
	 */
	private Esito entraOGestisciStatoPunteggi() {
		mostraIntro();
		return Esito.FERMATI;
	}

	private void inizializzaGioco() {

		gruppo.aggiungiPersonaggioSenzaNotificare(personaggio);

		if (!ModalitaDiProva.isAttiva()) {
			// In modalità di prova ha già i suoi artefatti potenti (vedi sotto)
			EquipaggiamentoIniziale.equipaggia(personaggio);
		}

		// Solo in modalità di prova: artefatti potenti e punti abilità al primo personaggio
		if (ModalitaDiProva.isAttiva()) {

			// La Spada della Morte e lo Scudo Fiscale non ci sono più: ora sono leggendari da conquistare con le
			// leggende (vedi LaLeggenda e leggendari.txt)
			Artefatto scarponi = CostruttoreArtefatto.istanza()
					.setTipo(TipoArtefatto.SCHINIERI)
					.setNome("gli scarponi di RomyJona")
					.setDescrizione("che schiacciano i tegami")
					.setLivello(5)
					.setDanniBase(50)
					.setCostoAcquisto(100)
					.setPeso(3)
					.setModificatore(TipoAttributo.CARISMA, TipoModificatore.QUANTITA_ASSOLUTA, 0)
					.costruisci();
			personaggio.addArtefatto(scarponi);

			Artefatto elmoTremendo = CostruttoreArtefatto.istanza()
					.setTipo(TipoArtefatto.ELMO)
					.setNome("la dvra cervice di RomyJona")
					.setDescrizione("che fa tremare il nemico")
					.setLivello(5)
					.setDanniBase(0)
					.setCostoAcquisto(100)
					.setPeso(3)
					.setModificatore(TipoAttributo.COSTITUZIONE, TipoModificatore.AUMENTO_PERCENTUALE, 5)
					.costruisci();
			personaggio.addArtefatto(elmoTremendo);

			Artefatto cazzabubbolo = CostruttoreArtefatto.istanza()
					.setTipo(TipoArtefatto.NINNOLO)
					.setNome("il cazzabubbolo a molla della morte alata perforante")
					.setDescrizione("il cui potere è nel fancazzismo")
					.setLivello(1)
					.setDanniBase(5)
					.setCostoAcquisto(10)
					.setPeso(1)
					.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 500)
					.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 400)
					.setIncantamento("Il Peperoncino di Cayenna", TipoDanno.FUOCO, 10, 0.5)
					.costruisci();
			personaggio.addArtefatto(cazzabubbolo);

			Artefatto manualeUnix = CostruttoreArtefatto.istanza()
					.setTipo(TipoArtefatto.LIBRO_MAGICO)
					.setNome("il Manuale di Unix System 5")
					.setDescrizione("che aiuta a far gli spregi ar CDA")
					.setLivello(1)
					.setDanniBase(0)
					.setCostoAcquisto(20)
					.setPeso(1)
					.setModificatore(TipoAttributo.SAGGEZZA, TipoModificatore.AUMENTO_PERCENTUALE, 30)
					.costruisci();
			personaggio.addArtefatto(manualeUnix);

			Artefatto occhiali = CostruttoreArtefatto.istanza()
					.setTipo(TipoArtefatto.NINNOLO)
					.setNome("gli occhiali da sole del Ruttatore")
					.setDescrizione("che impressionano le fie")
					.setLivello(5)
					.setDanniBase(50)
					.setCostoAcquisto(100)
					.setPeso(3)
					.setModificatore(TipoAttributo.CARISMA, TipoModificatore.QUANTITA_ASSOLUTA, 5)
					.setIncantamento("La serpe di Yalar", TipoDanno.VELENO, 10, 0.5)
					.setIncantamento("La mazzata nel capo", TipoDanno.CONTUNDENTE, 10, 0.5)
					.setIncantamento("Lo scherzo da prete", TipoDanno.SACRO, 10, 0.5)
					.costruisci();
			personaggio.addArtefatto(occhiali);

			Artefatto portafogli = CostruttoreArtefatto.istanza()
					.setTipo(TipoArtefatto.NINNOLO)
					.setNome("il portafogli di Samuele")
					.setDescrizione("che obnubila i tegami")
					.setLivello(5)
					.setDanniBase(50)
					.setCostoAcquisto(100)
					.setPeso(3)
					.setModificatore(TipoAttributo.CARISMA, TipoModificatore.QUANTITA_ASSOLUTA, 5)
					.setIncantamento("Il pelo di topa", TipoDanno.ARCANO, 10, 0.5)
					.setIncantamento("Giocondo", TipoDanno.SONICO, 10, 0.5)
					.setIncantamento("Il cervello di Tarlo", TipoDanno.VUOTO, 10, 0.5)
					.costruisci();
			personaggio.addArtefatto(portafogli);

			personaggio.getModelloDati().setPuntiAbilitaDisponibili(10);
		}

		stato = Stato.INIZIO_GIOCO;
		BusEventi.pubblica(new InternoRichiestaReinizializzazioneUI());
	}

	/**
	 * L'ordine con cui un controllo percorre l'albero delle missioni. Non è un
	 * dettaglio di efficienza: decide quale stato una missione vede nell'altra metà
	 * dell'albero, e quindi quali cascate si chiudono nello stesso giro anziché in
	 * quello dopo.
	 */
	private enum OrdineVisita {
		/**
		 * Il padre viene controllato prima dei figli. L'attivazione scende: una missione
		 * che si attiva adesso porta con sé le proprie figlie nello stesso giro, così
		 * un albero appena attivato compare completo invece di srotolarsi una riga per
		 * volta.
		 */
		PADRE_PRIMA,
		/**
		 * I figli vengono controllati prima del padre. Il completamento sale: una
		 * missione-contenitore che si completa quando tutte le figlie sono complete le
		 * vede nello stato di questo giro, non del precedente.
		 */
		FIGLI_PRIMA
	}

	/**
	 * Fa il controllo delle missioni di quel momento; se poi una missione a passi ha una domanda da porre al
	 * giocatore, passa ad ATTESA_RISPOSTA_MISSIONE tenendo da parte come proseguire. Consegnata la risposta, il
	 * controllo si rifà (così il passo con la domanda si conclude, e può nascerne un'altra) e solo allora si
	 * prosegue con {@code seguito}, il resto del lavoro che veniva dopo il controllo. Non si torna allo stato di
	 * prima: rientrarci rifarebbe cose già fatte, come costruire la locazione o raccoglierne l'oggetto.
	 */
	private Esito controllaMissioniEDomande(MomentoControllo momento, Supplier<Esito> seguito) {
		switch (momento) {
			case PRE_LOCAZIONE:
				controllaMissioni(Missione::controllaPreLocazione, OrdineVisita.PADRE_PRIMA);
				break;
			case IN_LOCAZIONE:
				controllaMissioni(Missione::controllaInLocazione, OrdineVisita.PADRE_PRIMA);
				break;
			case ACCAMPAMENTO:
				controllaMissioni(Missione::controllaAccampamento, OrdineVisita.PADRE_PRIMA);
				break;
			default:
				controllaMissioni(Missione::controllaPostLocazione, OrdineVisita.FIGLI_PRIMA);
				break;
		}
		for (Missione missione : missioniControllate()) {
			if (missione instanceof MissioneAPassi && ((MissioneAPassi) missione).getDomandaDaPorre(momento) != null) {
				missioneInAttesaDiRisposta = (MissioneAPassi) missione;
				momentoDomanda = momento;
				ripresaDopoRisposta = () -> controllaMissioniEDomande(momento, seguito);
				stato = Stato.ATTESA_RISPOSTA_MISSIONE;
				return Esito.CONTINUA_CON_INGRESSO;
			}
		}
		return seguito.get();
	}

	/**
	 * Pubblica la domanda, con le opzioni numerate se è una scelta, e le icone per rispondere: SI e NO per una
	 * conferma, NUMERO_1..NUMERO_N per una scelta.
	 */
	private Esito entraInStatoAttesaRispostaMissione() {
		Passo passo = missioneInAttesaDiRisposta.getDomandaDaPorre(momentoDomanda);
		BusEventi.pubblica(new NotificaTestoParagrafo(passo.getDomanda()));
		List<String> opzioni = passo.getOpzioni();
		for (int i = 0; i < opzioni.size(); i++) {
			BusEventi.pubblica(new NotificaTestoFrase((i + 1) + ". " + opzioni.get(i)));
		}
		comandiRispostaMissione = passo.isConferma()
				? Arrays.asList(Comando.SI, Comando.NO)
				: new ArrayList<>(COMANDI_OPZIONI.subList(0, opzioni.size()));
		BusEventi.pubblica(new RichiestaSelezioneMissione(comandiRispostaMissione));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoAttesaRispostaMissione(Comando comando) {
		if (comando == Comando.TIMER) {
			return Esito.FERMATI;
		}
		int indice = comandiRispostaMissione.indexOf(comando);
		if (indice < 0) {
			comandoNonValido(comando);
			return Esito.FERMATI;
		}
		Passo passo = missioneInAttesaDiRisposta.getDomandaDaPorre(momentoDomanda);
		missioneInAttesaDiRisposta.rispondi(passo.getRispostePossibili().get(indice));
		Supplier<Esito> ripresa = ripresaDopoRisposta;
		missioneInAttesaDiRisposta = null;
		momentoDomanda = null;
		comandiRispostaMissione = null;
		ripresaDopoRisposta = null;
		return ripresa.get();
	}

	/**
	 * Le missioni che controllaMissioni visita: le radici non completate e, sotto le missioni attive e non concluse,
	 * le figlie non concluse.
	 */
	private static List<Missione> missioniControllate() {
		List<Missione> missioni = new ArrayList<>();
		for (Missione missione : RegistroMissioni.getMissioniNonCompletate()) {
			aggiungiMissioneControllata(missione, missioni);
		}
		return missioni;
	}

	private static void aggiungiMissioneControllata(Missione missione, List<Missione> missioni) {
		missioni.add(missione);
		if (missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()) {
			for (Missione missioneSecondaria : missione.getMissioniSecondarie()) {
				if (!missioneSecondaria.isCompleta() && !missioneSecondaria.isFallita()) {
					aggiungiMissioneControllata(missioneSecondaria, missioni);
				}
			}
		}
	}

	/**
	 * Applica il controllo a tutto l'albero delle missioni, non solo ai nodi di primo
	 * livello: le sotto-missioni si attivano e si completano da sole come le altre.
	 */
	private void controllaMissioni(Consumer<Missione> controllo, OrdineVisita ordineVisita) {
		for (Missione missione : RegistroMissioni.getMissioniNonCompletate()) {
			controllaMissione(missione, controllo, ordineVisita);
		}
	}

	private void controllaMissione(Missione missione, Consumer<Missione> controllo, OrdineVisita ordineVisita) {
		if (ordineVisita == OrdineVisita.PADRE_PRIMA) {
			controllo.accept(missione);
		}
		// Nei rami spenti o già conclusi non si scende, e le figlie completate si
		// saltano: è lo stesso filtro che getMissioniNonCompletate() applica alle radici. Con
		// FIGLI_PRIMA la condizione si valuta prima che il padre sia controllato,
		// quindi una missione che si attiva adesso vedrà le proprie figlie al giro
		// successivo.
		if (missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()) {
			// Copia difensiva: un controllo può aggiungere sotto-missioni al nodo
			for (Missione missioneSecondaria : new ArrayList<>(missione.getMissioniSecondarie())) {
				if (!missioneSecondaria.isCompleta() && !missioneSecondaria.isFallita()) {
					controllaMissione(missioneSecondaria, controllo, ordineVisita);
				}
			}
		}
		if (ordineVisita == OrdineVisita.FIGLI_PRIMA) {
			controllo.accept(missione);
		}
	}

	private Comando scegliPersonaggio(boolean ancheSeMorto) {
		if (gruppo.getNumeroPersonaggiVivi() == 1 && !ancheSeMorto) {
			// L'unico in campo: non per forza il primo del gruppo, se gli altri sono morti o in panchina
			Comando unico = Comando.ofPersonaggio(gruppo.getPersonaggi().indexOf(gruppo.getPersonaggiVivi().get(0)));
			BusEventi.pubblica(new InternoMessaggio("Automa::scegliPersonaggio(ancheMorto=false): automaticamente " + unico));
			return unico;
		} else {
			List<Comando> comandiPossibili = new ArrayList<>();
			int i = 0;
			for (Personaggio personaggioCorrente : gruppo.getPersonaggi()) {
				if (ancheSeMorto || !personaggioCorrente.isFuoriCombattimento()) {
					comandiPossibili.add(Comando.ofPersonaggio(i));
				}
				i++;
			}
			comandiPossibili.add(Comando.ANNULLA);
			BusEventi.pubblica(new InternoMessaggio("Automa::scegliPersonaggio(ancheMorto=" + ancheSeMorto + "): imposto le azioni"));
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibili));
			return null;
		}
	}

	/**
	 * Riporta Azione.PERSONAGGIO_x se un solo personaggio è morto, altrimenti null
	 * e imposta le azioni per scegliere quale dei personaggi morti risuscitare.
	 */
	private Comando scegliPersonaggioMorto() {
		List<Integer> indiciMorti = new ArrayList<>();
		int i = 0;
		for (Personaggio personaggioCorrente : gruppo.getPersonaggi()) {
			if (!personaggioCorrente.isVivo()) {
				indiciMorti.add(i);
			}
			i++;
		}
		if (indiciMorti.size() == 1) {
			return Comando.ofPersonaggio(indiciMorti.get(0));
		}
		List<Comando> comandiPossibili = new ArrayList<>();
		for (int indice : indiciMorti) {
			comandiPossibili.add(Comando.ofPersonaggio(indice));
		}
		comandiPossibili.add(Comando.ANNULLA);
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibili));
		return null;
	}

	/**
	 * Se al termine di una locazione c'è almeno un personaggio morto, il gruppo possiede
	 * un incantesimo di Resurrezione e c'è chi ha la magia per formularlo, la Resurrezione
	 * diventa una delle azioni proponibili.
	 */
	private boolean isResurrezioneDisponibile() {
		if (gruppo.getIncantesimi(ClasseIncantesimo.RESURREZIONE) <= 0) {
			return false;
		}
		boolean qualcunoMorto = gruppo.getPersonaggi().stream().anyMatch(p -> !p.isVivo());
		return qualcunoMorto && !trovaFormulantiResurrezione().isEmpty();
	}

	private void eseguiResurrezione(Personaggio formulante, Personaggio personaggioBersaglio) {
		Incantesimo incantesimo = FabbricaIncantesimi.crea(ClasseIncantesimo.RESURREZIONE, formulante.getLivello());
		incantesimo.formula(formulante, personaggioBersaglio, null);
		gruppo.subIncantesimi(ClasseIncantesimo.RESURREZIONE, 1);
	}

	/**
	 * I personaggi vivi con abbastanza magia da formulare una Resurrezione,
	 * nell'ordine del gruppo; vuota se nessuno può farlo.
	 */
	private List<Personaggio> trovaFormulantiResurrezione() {
		int costoLancio = FabbricaIncantesimi.crea(ClasseIncantesimo.RESURREZIONE, 1).getCostoLancio();
		List<Personaggio> formulanti = new ArrayList<>();
		for (Personaggio personaggioCorrente : gruppo.getPersonaggiVivi()) {
			if (personaggioCorrente.getMagia() >= costoLancio) {
				formulanti.add(personaggioCorrente);
			}
		}
		return formulanti;
	}

	private Collection<Comando> getComandiPossibiliPerNumeroPassi(int numeroPassi) {
		List<Comando> comandiPossibili = new ArrayList<>();
		if (numeroPassi > 0) {
			comandiPossibili.add(Comando.NUMERO_1);
		}
		if (numeroPassi > 1) {
			comandiPossibili.add(Comando.NUMERO_2);
		}
		if (numeroPassi > 2) {
			comandiPossibili.add(Comando.NUMERO_3);
		}
		if (numeroPassi > 3) {
			comandiPossibili.add(Comando.NUMERO_4);
		}
		if (numeroPassi > 4) {
			comandiPossibili.add(Comando.NUMERO_5);
		}
		// Permette di tornare alla scelta della direzione (vedi gestisciComandoInStatoSceltaPassi)
		comandiPossibili.add(Comando.ANNULLA);
		return comandiPossibili;
	}

	/**
	 * Ci si accampa solo di notte, con almeno un altro personaggio vivo da mettere di
	 * guardia, e non in città, castelli, locande o paludi.
	 */
	private boolean isAccampamentoPossibile() {
		if (gruppo.getNumeroPersonaggiVivi() <= 1 || (LineaTemporale.getOra() <= 20 && LineaTemporale.getOra() >= 6)) {
			return false;
		}
		TipoLocazione tipoLocazione = gruppo.getTipoLocazioneCorrente();
		// Nei castelli non si riposa (getTipoRiposo lancia un'eccezione): ci si resta dopo una fuga
		return tipoLocazione.getCategoria() != CategoriaLocazione.CITTA &&
				tipoLocazione.getCategoria() != CategoriaLocazione.CASTELLO &&
				tipoLocazione != TipoLocazione.LOCANDA &&
				tipoLocazione != TipoLocazione.PALUDE;
	}

	/**
	 * I possibili comandi che il giocatore può dare quando la locazione è completata e sta andando via.
	 */
	private Collection<Comando> getComandiPossibiliInStatoAttesaDirezione() {
		List<Comando> comandiPossibili = new ArrayList<>();
		if (gruppo.getMaxPassiNord() > 0) {
			comandiPossibili.add(Comando.NORD);
		}
		if (gruppo.getMaxPassiEst() > 0) {
			comandiPossibili.add(Comando.EST);
		}
		if (gruppo.getMaxPassiSud() > 0) {
			comandiPossibili.add(Comando.SUD);
		}
		if (gruppo.getMaxPassiOvest() > 0) {
			comandiPossibili.add(Comando.OVEST);
		}
		comandiPossibili.add(Comando.MAPPA);
		if (isAccampamentoPossibile()) {
			comandiPossibili.add(Comando.ACCAMPAMENTO);
		}
		if (gruppo.getPozioniSalute() > 0) {
			comandiPossibili.add(Comando.POZIONE_SALUTE);
		}
		if (gruppo.getPozioniSaluteGrande() > 0) {
			comandiPossibili.add(Comando.POZIONE_SALUTE_GRANDE);
		}
		if (gruppo.getPozioniMagia() > 0) {
			comandiPossibili.add(Comando.POZIONE_MAGIA);
		}
		if (gruppo.getPozioniMagiaGrande() > 0) {
			comandiPossibili.add(Comando.POZIONE_MAGIA_GRANDE);
		}
		if (isResurrezioneDisponibile()) {
			comandiPossibili.add(Comando.RESURREZIONE);
		}
		comandiPossibili.add(Comando.INVENTARIO);
		comandiPossibili.add(Comando.FLOPPY_SALVA);
		return comandiPossibili;
	}

	/**
	 * Apre l'inventario di gruppo sul Personaggio all'indice indicato e ricorda la scelta,
	 * così che la prossima apertura dell'inventario riparta da lì.
	 */
	private void richiediAperturaInventarioGruppo() {
		Collection<Comando> comandiPossibili = new ArrayList<>();
		int l = gruppo.getNumeroPersonaggi();
		for (int i = 0; i < l; i++) {
			comandiPossibili.add(Comando.ofPersonaggio(i));
		}
		comandiPossibili.add(Comando.MOSTRA_TROFEI);
		comandiPossibili.add(Comando.ANNULLA);

		// Ultimo personaggio selezionato
		indicePersonaggioInventario = Math.max(0, Math.min(indicePersonaggioInventario, gruppo.getNumeroPersonaggi() - 1));
		Personaggio personaggioScelto = gruppo.getPersonaggio(indicePersonaggioInventario);

		BusEventi.pubblica(new RichiestaAperturaInventarioGruppo(comandiPossibili,
				new AutomaInventario(personaggioScelto, gruppo), personaggioScelto));
	}

	/**
	 * Il punto abilità che il giocatore spende dall'inventario, su un personaggio del gruppo
	 */
	private void onComandoSpesaPuntoAbilita(ComandoSpesaPuntoAbilita comando) {
		GruppoGiocatore.getIstanza().getPersonaggi().stream()
				.filter(personaggio -> personaggio.getUuid().equals(comando.getUuidPersonaggio()))
				.findFirst()
				.ifPresent(personaggio -> personaggio.spendiPuntoAbilita(comando.getAttributo()));
	}

	/**
	 * Apre o chiude l'elenco dei modificatori di un artefatto o la descrizione di una missione
	 */
	private static void onComandoCommutazioneElenco(ComandoCommutazioneElenco comando) {
		if (comando.getUuidArtefatto() != null) {
			AutomaScambiatoreArtefatti.trova(comando.getIdScambio())
					.flatMap(scambio -> scambio.trovaArtefatto(comando.getUuidArtefatto()))
					.ifPresent(artefatto -> {
						if (artefatto.isFigliVisibili()) {
							artefatto.nascondiFigli();
						} else {
							artefatto.mostraFigli();
						}
					});
		} else if (comando.getIdMissione() != null) {
			RegistroMissioni.getMissione(comando.getIdMissione()).ifPresent(missione -> {
				if (missione.isDescrizioneVisibile()) {
					missione.nascondiDescrizione();
				} else {
					missione.mostraDescrizione();
				}
			});
		}
	}
}
