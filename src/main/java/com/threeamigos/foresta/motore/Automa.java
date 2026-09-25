package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioGruppo;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoInvioTesto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVisualizzazioneMappa;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.tools.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: mancano le immagini per il supertipo artefatto elmo e incantamento
// TODO: carta, forbice e sasso
// TODO: mostrare in locazione anche i personaggi del gruppo.
// TODO: (dovrebbe essere stato fatto) se prendono un oggetto che è troppo pesante e questo va nell'inventario del gruppo dovrà apparire un fumetto "è troppo pesante" oltre al messaggio
// TODO: implementare fumetto che attende chiusura
// TODO: implementare sistema di aiuto

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

	private String nomePersonaggio;
	private Stato stato;
	private Stato statoPrecedente;

	private final GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
	private final GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
	private Personaggio personaggio;
	// Ultimo personaggio scelto nella schermata inventario: non salvato, si azzera a ogni avvio.
	private int indicePersonaggioInventario = 0;
	private Locazione locazioneCorrente;
	private Comando direzione; // serve a memorizzare la direzione prima di chiedere il numero di passi
	private Personaggio formulanteResurrezione; // chi lancerà la Resurrezione, scelto prima del bersaglio

	// L'intermezzo in corso, la pagina mostrata, e dove riprendere quando non ce ne sono altri
	private Intermezzo intermezzoCorrente;
	private List<PaginaIntermezzo> pagineIntermezzo;
	private int paginaIntermezzo;
	private MomentoIntermezzo momentoIntermezzo;
	private Stato statoDopoIntermezzi;

	public Automa(Temporizzatore temporizzatore) {
		this.temporizzatore = temporizzatore;
		temporizzatore.setTemporizzabile(this);

		BusEventi.iscriviti(ComandoDiGioco.class, this::onEventoComandoDiGioco);
		BusEventi.iscriviti(ComandoInvioTesto.class, this::onEventoTestoDisponibile);

		gestoriIngresso = new EnumMap<>(Stato.class);
		gestoriIngresso.put(Stato.INIZIO_GIOCO, this::entraInStatoInizioGioco);
		gestoriIngresso.put(Stato.INZIO_LOCAZIONE, this::entraInStatoInizioLocazione);
		gestoriIngresso.put(Stato.INTERMEZZO, this::entraInStatoIntermezzo);
		gestoriIngresso.put(Stato.PREPARAZIONE_LOCAZIONE, this::entraInStatoPreparazioneLocazione);
		gestoriIngresso.put(Stato.SCELTA_AUTOMATICA_PERSONAGGIO, this::entraInStatoSceltaAutomaticaPersonaggio);
		gestoriIngresso.put(Stato.SCELTA_PERSONAGGIO_QUALSIASI, this::entraInStatoSceltaPersonaggioQualsiasi);
		gestoriIngresso.put(Stato.SCELTA_MANUALE_PERSONAGGIO, this::entraInStatoSceltaManualePersonaggio);
		gestoriIngresso.put(Stato.SCELTA_DESTINATARIO_OGGETTO, this::entraInStatoSceltaDestinatarioOggetto);
		gestoriIngresso.put(Stato.SCELTA_INCANTESIMO_DA_LANCIARE, this::entraInStatoSceltaIncantesimoDaLanciare);
		gestoriIngresso.put(Stato.ATTESA_INCANTESIMO_QUALSIASI, this::entraInStatoAttesaIncantesimoQualsiasi);
		gestoriIngresso.put(Stato.ATTESA_SI_NO, this::entraInStatoAttesaSiNo);
		gestoriIngresso.put(Stato.FINE_LOCAZIONE, () -> eseguiFineLocazione(null));
		gestoriIngresso.put(Stato.ATTESA_DIREZIONE, this::entraInStatoAttesaDirezione);
		gestoriIngresso.put(Stato.SCELTA_FORMULANTE_RESURREZIONE, this::entraInStatoSceltaFormulanteResurrezione);
		gestoriIngresso.put(Stato.SCELTA_BERSAGLIO_RESURREZIONE, this::entraInStatoSceltaBersaglioResurrezione);
		gestoriIngresso.put(Stato.MAPPA, this::entraInStatoMappa);
		gestoriIngresso.put(Stato.INVENTARIO, this::entraInStatoInventario);
		// Uscendo da MAPPA o INVENTARIO aperti mentre si era IN_LOCAZIONE si torna qui
		// tramite CONTINUA_CON_INGRESSO (statoPrecedente vale IN_LOCAZIONE), con comando
		// nullo: bisogna ripubblicare i comandi della locazione, altrimenti restano quelli
		// della mappa e il giocatore deve cliccare due volte perché la UI si aggiorni.
		gestoriIngresso.put(Stato.IN_LOCAZIONE, this::entraInStatoInLocazione);
		gestoriIngresso.put(Stato.CONFERMA_USCITA, () -> Esito.FERMATI);
		gestoriIngresso.put(Stato.GIOCO_PERSO, this::entraInStatoGiocoPerso);
		gestoriIngresso.put(Stato.GIOCO_PERSO_2, this::entraInStatoGiocoPerso2);
		gestoriIngresso.put(Stato.GIOCO_VINTO, this::entraInStatoGiocoVinto);
		gestoriIngresso.put(Stato.GIOCO_VINTO_2, this::entraInStatoGiocoVinto2);
		gestoriIngresso.put(Stato.STATISTICHE, this::entraInStatoStatistiche);
		gestoriIngresso.put(Stato.PUNTEGGI, this::entraOGestisciStatoPunteggi);

		gestoriComando = new EnumMap<>(Stato.class);
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
		gestoriComando.put(Stato.FINE_LOCAZIONE, this::eseguiFineLocazione);
		gestoriComando.put(Stato.SCELTA_DIREZIONE, this::gestisciComandoInStatoSceltaDirezione);
		gestoriComando.put(Stato.SCELTA_PASSI, this::gestisciComandoInStatoSceltaPassi);
		gestoriComando.put(Stato.ATTESA_POZIONE_SALUTE, this::gestisciComandoInStatoAttesaPozioneSalute);
		gestoriComando.put(Stato.ATTESA_POZIONE_SALUTE_GRANDE, this::gestisciComandoInStatoAttesaPozioneSaluteGrande);
		gestoriComando.put(Stato.ATTESA_POZIONE_MAGIA, this::gestisciComandoInStatoAttesaPozioneMagia);
		gestoriComando.put(Stato.ATTESA_POZIONE_MAGIA_GRANDE, this::gestisciComandoInStatoAttesaPozioneMagiaGrande);
		gestoriComando.put(Stato.SCELTA_FORMULANTE_RESURREZIONE, this::gestisciComandoInStatoSceltaFormulanteResurrezione);
		gestoriComando.put(Stato.SCELTA_BERSAGLIO_RESURREZIONE, this::gestisciComandoInStatoSceltaBersaglioResurrezione);
		gestoriComando.put(Stato.ESEECUZIONE_RESURREZIONE, this::gestisciComandoInStatoEsecuzioneResurrezione);
		gestoriComando.put(Stato.MAPPA, this::gestisciComandoInStatoMappa);
		gestoriComando.put(Stato.INVENTARIO, this::gestisciComandoInStatoInventario);
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
	 * Schermata introduttiva coi titoli. QUi è possibile scegliere se iniziare una nuova partita o
	 * caricare una partita preesistente.
	 */
	public void inizia() {
		stato = Stato.INTRO;
		BusEventi.pubblica(new InternoStatoDiGioco(Stato.INTRO, getComandiPossibiliInStatoIntro()));
	}

	private Collection<Comando> getComandiPossibiliInStatoIntro() {
		Collection<Comando> comandiPossibili = new ArrayList<>();
		comandiPossibili.add(Comando.PERGAMENA);
		if (!GestoreSalvataggi.getSalvataggiDisponibili().isEmpty()) {
			comandiPossibili.add(Comando.FLOPPY);
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
				BusEventi.pubblica(new InternoStatoDiGioco(stato));
				return Esito.FERMATI;
			case FLOPPY:
				stato = Stato.PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE;
				Collection<TestataSalvataggio> salvataggiDisponibili = GestoreSalvataggi.getSalvataggiDisponibili();
				BusEventi.pubblica(new RichiestaSelezioneSlotPerRilettura(salvataggiDisponibili));
				return Esito.FERMATI;
			default:
				comandoNonValido(comando);
				return Esito.FERMATI;
		}
	}

	private Esito gestisciComandoInStatoPreGameSelezionaSalvataggioDaLeggere(Comando comando) {
		if (comando != Comando.ANNULLA && GestoreSalvataggi.leggi(comando)) {
			stato = Stato.ATTESA_DIREZIONE;
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			// Copia: la lista viva continua a ricevere i messaggi pubblicati da qui in poi
			BusEventi.pubblica(new InternoCaricamentoCompletato(new ArrayList<>(Notizie.getUltimiMessaggi())));
			return Esito.CONTINUA_CON_INGRESSO;
		} else {
			BusEventi.pubblica(new InternoStatoDiGioco(Stato.INTRO, getComandiPossibiliInStatoIntro()));
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
		BusEventi.pubblica(new InternoStatoDiGioco(stato, Comando.MASCHIO, Comando.FEMMINA));
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoPreGameAttesaSessoPersonaggio(Comando comando) {
		stato = Stato.PRE_GAME_ATTESA_CLASSE_PERSONAGGIO;
		if (comando == Comando.FEMMINA) {
			BusEventi.pubblica(new InternoStatoDiGioco(stato, Comando.GUERRIERA, Comando.LADRA,
					Comando.CANTASTORIE, Comando.ELFA, Comando.MAGA));
		} else {
			BusEventi.pubblica(new InternoStatoDiGioco(stato, Comando.GUERRIERO, Comando.LADRO,
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
	 * Gli intermezzi di apertura vanno mostrati prima di INZIO_LOCAZIONE: lì partono i
	 * controlli delle missioni, le cui notifiche comparirebbero già durante l'intermezzo.
	 */
	private Esito entraInStatoInizioGioco() {
		return avviaProssimoIntermezzo(MomentoIntermezzo.INIZIO_GIOCO, Stato.INZIO_LOCAZIONE);
	}

	private Esito entraInStatoInizioLocazione() {
		controllaMissioni(Missione::controllaPreLocazione, OrdineVisita.PADRE_PRIMA);
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
		return avviaProssimoIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, Stato.PREPARAZIONE_LOCAZIONE);
	}

	/**
	 * Mostra il prossimo intermezzo che deve scattare nel momento indicato; se non ce
	 * ne sono (più) prosegue con statoDopo. Dopo ogni intermezzo si torna qui, così più
	 * intermezzi scattati nello stesso momento vengono mostrati uno dopo l'altro.
	 */
	private Esito avviaProssimoIntermezzo(MomentoIntermezzo momento, Stato statoDopo) {
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
		BusEventi.pubblica(new NotificaPaginaIntermezzo(pagineIntermezzo.get(paginaIntermezzo),
				paginaIntermezzo + 1, pagineIntermezzo.size()));
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		double secondi = pagineIntermezzo.get(paginaIntermezzo).getSecondiPrimaDiAvanzare(intermezzoCorrente.getSecondiPerPagina());
		if (secondi > 0) {
			// Riavviato a ogni pagina: un click riporta a zero il conto alla rovescia
			temporizzatore.iniziaDopo((int) Math.ceil(secondi * 1_000));
		} else {
			// Pagina che avanza solo al click: non deve scattare il timer della precedente
			temporizzatore.termina();
		}
	}


	/**
	 * La pagina avanza con la pergamena o con il timer; dopo l'ultima si passa al
	 * prossimo intermezzo o si riprende il gioco.
	 */
	private Esito gestisciComandoInStatoIntermezzo(Comando comando) {
		if (comando != Comando.PERGAMENA && comando != Comando.TIMER) {
			comandoNonValido(comando);
			return Esito.FERMATI;
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
			// Nessun altro intermezzo in coda: la UI torna alla schermata di gioco
			BusEventi.pubblica(new InternoMostraSchermataGioco());
		}
		return esito;
	}

	private Esito entraInStatoPreparazioneLocazione() {
		gruppoAvversario.reimposta();
		gruppo.getPersonaggiVivi().forEach(Personaggio::rimuoviTuttiGliEffettiDiStato);
		locazioneCorrente = Foresta.costruisciIstanza(gruppo.getCoordinate());
		gruppo.setLocazioneCorrente(locazioneCorrente);
		locazioneCorrente.crea(gruppo, gruppoAvversario);
		BusEventi.pubblica(new InternoPreparazioneLocazione());
		BusEventi.pubblica(new NotificaTestoParagrafo(LineaTemporale.getDescrizioneOraDelGiorno()));
		locazioneCorrente.descrivi(gruppo, gruppoAvversario);
		controllaMissioni(Missione::controllaInLocazione, OrdineVisita.PADRE_PRIMA);
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
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			if (gruppo.getIncantesimi(classeIncantesimo) > 0 && formulante.getMagia() >= classeIncantesimo.getIstanza(formulante.getLivello()).getCostoLancio()) {
				comandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
			}
		}
		// Mago ed Elfo hanno anche il dardo arcano, che non consuma pergamene
		if (DardoArcano.puoLanciarlo(formulante)) {
			comandiPossibili.add(Comando.DARDO_ARCANO);
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
						BusEventi.pubblica(new InternoMessaggio("Oggetto raccolto."));
						locazioneCorrente.rimuoviOggetto();
					}
				}
			}
			// Le missioni vanno controllate prima di azzerare la locazione altrimenti la
			// distruzione di un castello con sostituzione con rovine non fa completare le
			// missioni. Potremmo anche salvare il tipo di locazione nelle missioni ma così
			// mi pare più pulito.
			controllaMissioni(Missione::controllaPostLocazione, OrdineVisita.FIGLI_PRIMA);
			locazioneCorrente.azzeraLocazione(gruppo);
		}

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

		// Aumentiamo la stanchezza
		for (Personaggio personaggioCorrente : gruppo.getPersonaggiVivi()) {
			personaggioCorrente.addStanchezza(1);
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
		Collection<Comando> comandiPossibiliPerNumeroPassi = null;
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
				gruppo.pernotta(locazioneCorrente.getTipoRiposo());
				LineaTemporale.mattinoSeguente();
				LineaTemporale.eventi(gruppo);
				stato = Stato.ATTESA_DIREZIONE;
				return Esito.CONTINUA_CON_INGRESSO;
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
			case AIUTO:
				for (Personaggio personaggio : gruppo.getPersonaggi()) {
					BusEventi.pubblica(new NotificaTestoParagrafo(personaggio.getDescrizione()));
				}
				stato = Stato.ATTESA_DIREZIONE;
				return Esito.CONTINUA_CON_INGRESSO;
			case FLOPPY:
				stato = Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE;
				BusEventi.pubblica(new InternoStatoDiGioco(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE,
						Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5,
						Comando.NO));
				return Esito.FERMATI;
			default:
				break;
		}
		stato = Stato.SCELTA_PASSI;
		if (comandiPossibiliPerNumeroPassi != null) {
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibiliPerNumeroPassi));
		}
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoSceltaPassi(Comando comando) {
		if (comando == Comando.ANNULLA) {
			// Scegliere la direzione ha solo memorizzato il campo direzione, che verrà
			// riscritto alla prossima scelta: si può tornare indietro senza nulla da disfare.
			stato = Stato.ATTESA_DIREZIONE;
			return Esito.CONTINUA_CON_INGRESSO;
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
		stato = Stato.INZIO_LOCAZIONE;
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
			stato = Stato.ESEECUZIONE_RESURREZIONE;
			return Esito.continuaCon(comandoRisolto);
		}
		return Esito.FERMATI;
	}

	private Esito gestisciComandoInStatoSceltaBersaglioResurrezione(Comando comando) {
		// Il bersaglio scelto (o ANNULLA) passa all'esecuzione
		stato = Stato.ESEECUZIONE_RESURREZIONE;
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
		BusEventi.pubblica(new ComandoVisualizzazioneMappa());
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
			default:
				throw new IllegalArgumentException();
		}
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
			salva(comando);
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
		if (comando == Comando.PERGAMENA && !GestorePunteggi.isPunteggioInClassifica(Statistiche.getPunti())) {
			// Nessun punteggio da registrare: si torna all'intro, che riparte dai loghi.
			// inizia() pubblica anche lo stato INTRO, senza il quale la UI resterebbe
			// sulle statistiche.
			inizia();
			return Esito.FERMATI;
		}
		if (comando == Comando.PERGAMENA) {
			stato = Stato.ATTESA_NOME_PUNTEGGI;
			BusEventi.pubblica(new RichiestaTesto("congratulazioni! inserisci il tuo nome"));
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
		GestorePunteggi.addPunteggio(testoDisponibile, Statistiche.getPunti());
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
		inizia();
		return Esito.FERMATI;
	}

	private void inizializzaGioco() {

		gruppo.aggiungiPersonaggioSenzaNotificare(personaggio);

		// PER TEST
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

		Artefatto megaspada = CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("la Spada della Morte alata con rinterzo laterale")
				.setDescrizione("che massacra i porci")
				.setLivello(5)
				.setDanniBase(50)
				.setCostoAcquisto(100)
				.setPeso(3)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_FISSO, 2)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.QUANTITA_ASSOLUTA, 1)
				.setIncantamento("Incantesimo di RomyJona", TipoDanno.NECROTICO, 10, 0.5)
				.costruisci();
		personaggio.addArtefatto(megaspada);

		Artefatto superscudo = CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SCUDO)
				.setNome("lo scudo fiscale")
				.setDescrizione("che si fa fare sconti sugli acquisti")
				.setLivello(5)
				.setDanniBase(50)
				.setCostoAcquisto(100_000)
				.setPeso(3000)
				.setModificatore(TipoAttributo.COSTITUZIONE, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.AUMENTO_PERCENTUALE, 2)
				.setModificatore(TipoAttributo.CONTRATTAZIONE, TipoModificatore.AUMENTO_FISSO, 16)
				.setIncantamento("La battuta del cavolo", TipoDanno.GELO, 10, 0.5)
				.costruisci();
		// Leggendario, obiettivo di una missione: con +16 in CONTRATTAZIONE porta ai limiti di RegoleContrattazione
		superscudo.getModelloDati().setRarita(RaritaArtefatto.LEGGENDARIO);
		personaggio.addArtefatto(superscudo);

		Artefatto scarponi = CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ARMATURA)
				.setNome("gli scarponi di RomyJona")
				.setDescrizione("che tritura i tegami")
				.setLivello(5)
				.setDanniBase(50)
				.setCostoAcquisto(100)
				.setPeso(3)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.QUANTITA_ASSOLUTA, 0)
				.costruisci();
		personaggio.addArtefatto(scarponi);

		Artefatto occhiali = CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.NINNOLO)
				.setNome("occhiali da sole del Ruttatore")
				.setDescrizione("che tritura i tegami")
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
				.setNome("portafogli di Samuele")
				.setDescrizione("che tritura i tegami")
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
		// FINE PER TEST

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
		if (missione.isAttiva() && !missione.isCompleta()) {
			// Copia difensiva: un controllo può aggiungere sotto-missioni al nodo
			for (Missione missioneSecondaria : new ArrayList<>(missione.getMissioniSecondarie())) {
				if (!missioneSecondaria.isCompleta()) {
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
			BusEventi.pubblica(new InternoMessaggio("Automa::scegliPersonaggio(ancheMorto=false): automaticamente PERSONAGGIO_1"));
			return Comando.PERSONAGGIO_1;
		} else {
			List<Comando> comandiPossibili = new ArrayList<>();
			int i = 0;
			for (Personaggio personaggioCorrente : gruppo.getPersonaggi()) {
				if (ancheSeMorto || personaggioCorrente.isVivo()) {
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
		Incantesimo incantesimo = ClasseIncantesimo.RESURREZIONE.getIstanza(formulante.getLivello());
		incantesimo.formula(formulante, personaggioBersaglio, null);
		gruppo.subIncantesimi(ClasseIncantesimo.RESURREZIONE, 1);
	}

	/**
	 * I personaggi vivi con abbastanza magia da formulare una Resurrezione,
	 * nell'ordine del gruppo; vuota se nessuno può farlo.
	 */
	private List<Personaggio> trovaFormulantiResurrezione() {
		int costoLancio = ClasseIncantesimo.RESURREZIONE.getIstanza(1).getCostoLancio();
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

	private void salva(Comando azione) {
		if (azione != Comando.ANNULLA) {
			GestoreSalvataggi.salva(azione);
		}
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
		if (gruppo.getNumeroPersonaggiVivi() > 1 && (LineaTemporale.getOra() > 20 || LineaTemporale.getOra() < 6)) {
			ClassiLocazione classeLocazione = gruppo.getClasseLocazioneCorrente();
			if (classeLocazione.getTipoLocazione() != TipoLocazione.CITTA &&
					classeLocazione != ClassiLocazione.LOCANDA &&
					classeLocazione != ClassiLocazione.PALUDE) {
				comandiPossibili.add(Comando.ACCAMPAMENTO);
			}
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
		comandiPossibili.add(Comando.AIUTO);
		comandiPossibili.add(Comando.FLOPPY);
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
		comandiPossibili.add(Comando.ANNULLA);

		// Ultimo personaggio selezionato
		indicePersonaggioInventario = Math.max(0, Math.min(indicePersonaggioInventario, gruppo.getNumeroPersonaggi() - 1));
		Personaggio personaggioScelto = gruppo.getPersonaggio(indicePersonaggioInventario);

		BusEventi.pubblica(new ComandoAperturaInventarioGruppo(comandiPossibili,
				new AutomaInventario(personaggioScelto, gruppo)));
	}
}
