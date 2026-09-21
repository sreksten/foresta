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
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.Missione;
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

// FIXME conosciuti: l'incantesimo di resurrezione prende il primo personaggio morto e lo fa resuscitare dal primo personaggio che può farlo senza dare possibilità di scelta

public class Automa implements ControlloreDiGioco, Temporizzabile {

	private final Map<Stato, Consumer<Comando>> gestoriComando;
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

	public Automa(Temporizzatore temporizzatore) {
		this.temporizzatore = temporizzatore;
		temporizzatore.setTemporizzabile(this);

		BusEventi.iscriviti(ComandoDiGioco.class, this::onEventoComandoDiGioco);
		BusEventi.iscriviti(ComandoInvioTesto.class, this::onEventoTestoDisponibile);

		gestoriComando = new EnumMap<>(Stato.class);
		gestoriComando.put(Stato.INTRO, this::processaComandoInStatoIntro);
		gestoriComando.put(Stato.PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE, this::processaComandoInStatoPreGameSelezionaSalvataggioDaLeggere);
		gestoriComando.put(Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO, this::processaComandoInStatoPreGameAttesaSessoPersonaggio);
		gestoriComando.put(Stato.PRE_GAME_ATTESA_CLASSE_PERSONAGGIO, this::processaComandoInStatoPreGameAttesaClassePersonaggio);
		gestoriComando.put(Stato.INZIO_LOCAZIONE, this::processaComandoInStatoInizioLocazione);
		gestoriComando.put(Stato.IN_LOCAZIONE, this::processaComandoInStatoInLocazione);
		gestoriComando.put(Stato.IN_COMBATTIMENTO, this::processaComandoInStatoInCombattimento);
		gestoriComando.put(Stato.SCELTA_AUTOMATICA_PERSONAGGIO, this::processaComandoInStatoSceltaAutomaticaPersonaggio);
		gestoriComando.put(Stato.SCELTA_PERSONAGGIO_QUALSIASI, this::processaComandoInStatoSceltaPersonaggioQualsiasi);
		gestoriComando.put(Stato.SCELTA_MANUALE_PERSONAGGIO, this::processaComandoInStatoSceltaManualePersonaggio);
		gestoriComando.put(Stato.SCELTA_INCANTESIMO_DA_LANCIARE, this::processaComandoInStatoSceltaIncantesimoDaLanciare);
		gestoriComando.put(Stato.ATTESA_INCANTESIMO_QUALSIASI, this::processaComandoInStatoAttesaIncantesimoQualsiasi);
		gestoriComando.put(Stato.INCANTESIMO_SCELTO, this::processaComandoInStatoIncantesimoScelto);
		gestoriComando.put(Stato.ATTESA_SI_NO, this::processaComandoInStatoAttesaSiNo);
		gestoriComando.put(Stato.FINE_LOCAZIONE, this::processaComandoInStatoFineLocazione);
		gestoriComando.put(Stato.ATTESA_DIREZIONE, this::processaComandoInStatoAttesaDirezione);
		gestoriComando.put(Stato.ATTESA_PASSI, this::processaComandoInStatoAttesaPassi);
		gestoriComando.put(Stato.IN_CAMMINO, this::processaComandoInStatoInCammino);
		gestoriComando.put(Stato.ATTESA_POZIONE_SALUTE, this::processaComandoInStatoAttesaPozioneSalute);
		gestoriComando.put(Stato.ATTESA_POZIONE_SALUTE_GRANDE, this::processaComandoInStatoAttesaPozioneSaluteGrande);
		gestoriComando.put(Stato.ATTESA_POZIONE_MAGIA, this::processaComandoInStatoAttesaPozioneMagia);
		gestoriComando.put(Stato.ATTESA_POZIONE_MAGIA_GRANDE, this::processaComandoInStatoAttesaPozioneMagiaGrande);
		gestoriComando.put(Stato.SCELTA_BERSAGLIO_RESURREZIONE, this::processaComandoInStatoSceltaBersaglioResurrezione);
		gestoriComando.put(Stato.ESEECUZIONE_RESURREZIONE, this::processaComandoInStatoEsecuzioneResurrezione);
		gestoriComando.put(Stato.MAPPA, this::processaComandoInStatoMappa);
		gestoriComando.put(Stato.INVENTARIO, this::processaComandoInStatoInventario);
		gestoriComando.put(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE, this::processaComandoInStatoSelezioneSalvataggioDaScrivere);
		gestoriComando.put(Stato.CONFERMA_USCITA, this::processaComandoInStatoConfermaUscita);
		gestoriComando.put(Stato.GIOCO_PERSO, this::processaComandoInStatoGiocoPerso);
		gestoriComando.put(Stato.GIOCO_PERSO_2, this::processaComandoInStatoGiocoPerso2);
		gestoriComando.put(Stato.GIOCO_VINTO, this::processaComandoInStatoGiocoVinto);
		gestoriComando.put(Stato.GIOCO_VINTO_2, this::processaComandoInStatoGiocoVinto2);
		gestoriComando.put(Stato.STATISTICHE, this::processaComandoInStatoStatistiche);
		gestoriComando.put(Stato.PUNTEGGI, this::processaComandoInStatoPunteggi);
	}

	public void tick() {
		processaComando(Comando.TIMER);
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

		switch (stato) {

			case PRE_GAME_ATTESA_NOME_PERSONAGGIO:
				processaComandoInStatoPreGameAttesaNomePersonaggio(testoDisponibile);
				break;

			case ATTESA_NOME_PUNTEGGI:
				processaComandoInStatoPostGameAttesaNomePerPunteggio(testoDisponibile);
				break;

			default:
				BusEventi.pubblica(new InternoErrore("onEventoTestoDisponibile: Stato non gestito: " + stato));
		}
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
	 * Per alcuni stati il comando ricevuta non serve a nulla, giusto per cambiare
	 * lo stato stesso.
	 */
	public void processaComando(Comando comando) {
		BusEventi.pubblica(new InternoMessaggio("Automa in stato " + stato.name() + "; processo Comando " + comando));
		Consumer<Comando> gestore = gestoriComando.get(stato);
		if (gestore != null) {
			gestore.accept(comando);
		} else {
			comandoNonValido(comando);
			throw new IllegalStateException("Stato " + stato + " non correttamente gestito!");
		}
	}

	private void processaComandoInStatoIntro(Comando comando) {
		switch(comando) {
			case PERGAMENA:
				stato = Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO;
				BusEventi.pubblica(new InternoStatoDiGioco(stato));
				break;
			case FLOPPY:
				stato = Stato.PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE;
				Collection<TestataSalvataggio> salvataggiDisponibili = GestoreSalvataggi.getSalvataggiDisponibili();
				BusEventi.pubblica(new RichiestaSelezioneSlotPerRilettura(salvataggiDisponibili));
				break;
			default:
				comandoNonValido(comando);
		}
	}

	private void processaComandoInStatoPreGameSelezionaSalvataggioDaLeggere(Comando comando) {
		if (comando != Comando.ANNULLA && GestoreSalvataggi.leggi(comando)) {
			stato = Stato.ATTESA_DIREZIONE;
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			processaComando(null);
		} else {
			BusEventi.pubblica(new InternoStatoDiGioco(Stato.INTRO, getComandiPossibiliInStatoIntro()));
			stato = Stato.INTRO;
		}
	}

	private void processaComandoInStatoPreGameAttesaNomePersonaggio(String testoDisponibile) {
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
			inizializzaGioco();
			// Questo mi sa che serve per poter impostare i comando possibili. Solo che in INIZIO_LOCAZIONE non ce ne dovrebbero essere.
			processaComando(null);
			return;
		}

		stato = Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO;
		BusEventi.pubblica(new InternoStatoDiGioco(stato, Comando.MASCHIO, Comando.FEMMINA));
	}

	private void processaComandoInStatoPreGameAttesaSessoPersonaggio(Comando comando) {
		stato = Stato.PRE_GAME_ATTESA_CLASSE_PERSONAGGIO;
		if (comando == Comando.FEMMINA) {
			BusEventi.pubblica(new InternoStatoDiGioco(stato, Comando.GUERRIERA, Comando.LADRA,
					Comando.CANTASTORIE, Comando.ELFA, Comando.MAGA));
		} else {
			BusEventi.pubblica(new InternoStatoDiGioco(stato, Comando.GUERRIERO, Comando.LADRO,
					Comando.BARDO, Comando.ELFO, Comando.MAGO));
		}
	}

	private void processaComandoInStatoPreGameAttesaClassePersonaggio(Comando comando) {
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
		processaComando(null);
	}

	private void processaComandoInStatoInizioLocazione(Comando comando) {
		controllaMissioni(Missione::controllaPreLocazione, OrdineVisita.PADRE_PRIMA);
		String evento = LineaTemporale.getEvento();
		if (evento != null) {
			BusEventi.pubblica(new NotificaTestoFrase(evento));
			if (LineaTemporale.isGiocoFinito()) {
				stato = Stato.GIOCO_PERSO;
				processaComando(null);
				return;
			}
		}
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
		if (stato == Stato.FINE_LOCAZIONE) {
			processaComando(null);
		}
		/*
		 * A questo punto il giocatore si trova davanti la scelta delle
		 * azioni che puo' intraprendere.
		 */
	}

	private void processaComandoInStatoInLocazione(Comando comando) {
		if (comando == Comando.INVENTARIO) {
			statoPrecedente = Stato.IN_LOCAZIONE;
			stato = Stato.INVENTARIO;
			richiediAperturaInventarioGruppo();
			processaComando(null);
			return;
		}
		statoPrecedente = stato;
		/*
		 * Continuiamo a fornire all'automa a stati finiti della
		 * locazione la possibilità di andare avanti fino a
		 * LOCAZIONE_COMPLETA
		 */
		stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, comando);
		if (stato != Stato.IN_LOCAZIONE) {
			if (stato == Stato.IN_COMBATTIMENTO) {
				temporizzatore.inizia(1_000);
			} else {
				if (stato == Stato.GIOCO_PERSO || stato == Stato.GIOCO_VINTO || stato == Stato.FINE_LOCAZIONE) {
					temporizzatore.termina();
				}
				processaComando(null);
			}
		}
	}

	private void processaComandoInStatoInCombattimento(Comando comando) {
		statoPrecedente = stato;
		stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, comando);
		if (stato != Stato.IN_COMBATTIMENTO) {
			temporizzatore.termina();
			processaComando(null);
			return;
		}
		if (comando != Comando.TIMER) {
			// Il battito del combattimento viene interrotto ogni volta che si
			// esce da IN_COMBATTIMENTO (per esempio per scegliere chi combatte):
			// qui lo si riavvia, dato che i round sono guidati da Comando.TIMER.
			temporizzatore.inizia(1_000);
		}
	}

	private void processaComandoInStatoSceltaAutomaticaPersonaggio(Comando comando) {
		comando = scegliPersonaggio(false);
		if (comando != null) {
			Logger.log(Stato.SCELTA_AUTOMATICA_PERSONAGGIO.name() + ": Torno allo stato " + statoPrecedente.name());
			stato = statoPrecedente;
			processaComando(comando);
		} else {
			stato = Stato.SCELTA_MANUALE_PERSONAGGIO;
			processaComando(null);
		}
	}

	private void processaComandoInStatoSceltaPersonaggioQualsiasi(Comando comando) {
		comando = scegliPersonaggio(true);
		if (comando != null) {
			Logger.log(Stato.SCELTA_PERSONAGGIO_QUALSIASI.name() + ": Torno allo stato " + statoPrecedente.name());
			stato = statoPrecedente;
			processaComando(comando);
		} else {
			stato = Stato.SCELTA_MANUALE_PERSONAGGIO;
			processaComando(null);
		}
	}

	private void processaComandoInStatoSceltaManualePersonaggio(Comando comando) {
		if (comando != null) {
			Logger.log(Stato.SCELTA_MANUALE_PERSONAGGIO.name() + ": Torno allo stato " + statoPrecedente.name());
			stato = statoPrecedente;
			processaComando(comando);
		}
	}

	private void processaComandoInStatoSceltaIncantesimoDaLanciare(Comando comando) {
		List<Comando> comandiPossibili = new ArrayList<>();
		Personaggio formulante = gruppo.getFormulante();
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			if (gruppo.getIncantesimi(classeIncantesimo) > 0 && formulante.getMagia() >= classeIncantesimo.getIstanza(formulante.getLivello()).getCostoLancio()) {
				comandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
			}
		}
		comandiPossibili.add(Comando.NO_INCANTESIMO);
		BusEventi.pubblica(new RichiestaSelezioneIncantesimoDaLanciare(comandiPossibili));
		stato = Stato.INCANTESIMO_SCELTO;
	}

	private void processaComandoInStatoAttesaIncantesimoQualsiasi(Comando comando) {
		List<Comando> comandiPossibili = new ArrayList<>();
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			comandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
		}
		comandiPossibili.add(Comando.NO_INCANTESIMO);
		BusEventi.pubblica(new RichiestaSelezioneIncantesimoDaLanciare(comandiPossibili));
		stato = Stato.INCANTESIMO_SCELTO;
	}

	private void processaComandoInStatoIncantesimoScelto(Comando comando) {
		stato = Stato.IN_LOCAZIONE;
		processaComando(comando);
	}

	private void processaComandoInStatoAttesaSiNo(Comando comando) {
		if (comando == null) {
			BusEventi.pubblica(new RichiestaSelezioneSiNo());
		} else if (comando != Comando.TIMER) {
			stato = statoPrecedente;
			processaComando(comando);
		}
	}

	private void processaComandoInStatoFineLocazione(Comando comando) {
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
						stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
						processaComando(null);
						return;
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
			processaComando(null);
			return;
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
		processaComando(null);
	}

	private void processaComandoInStatoAttesaDirezione(Comando comando) {
		stato = Stato.ATTESA_PASSI;
		BusEventi.pubblica(new NotificaTestoParagrafo(gruppo.chiMaiuscolo() + " se ne va. In quale direzione si incammina?"));
		BusEventi.pubblica(new RichiestaSelezioneDirezione(getComandiPossibiliInStatoAttesaDirezione()));
	}

	private void processaComandoInStatoAttesaPassi(Comando comando) {
		// Occorre memorizzare l'informazione sulla direzione
		Collection<Comando> comandiPossibiliPerNumeroPassi = null;
		switch (comando) {
			case MAPPA:
				statoPrecedente = Stato.ATTESA_DIREZIONE;
				stato = Stato.MAPPA;
				processaComando(null);
				return;
			case INVENTARIO:
				statoPrecedente = Stato.ATTESA_DIREZIONE;
				stato = Stato.INVENTARIO;
				richiediAperturaInventarioGruppo();
				processaComando(null);
				return;
			case ACCAMPAMENTO:
				gruppo.pernotta(locazioneCorrente.getTipoRiposo());
				LineaTemporale.mattinoSeguente();
				LineaTemporale.eventi(gruppo);
				stato = Stato.ATTESA_DIREZIONE;
				processaComando(null);
				return;
			case POZIONE_SALUTE:
				statoPrecedente = Stato.ATTESA_POZIONE_SALUTE;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				processaComando(null);
				return;
			case POZIONE_SALUTE_GRANDE:
				statoPrecedente = Stato.ATTESA_POZIONE_SALUTE_GRANDE;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				processaComando(null);
				return;
			case POZIONE_MAGIA:
				statoPrecedente = Stato.ATTESA_POZIONE_MAGIA;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				processaComando(null);
				return;
			case POZIONE_MAGIA_GRANDE:
				statoPrecedente = Stato.ATTESA_POZIONE_MAGIA_GRANDE;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				processaComando(null);
				return;
			case RESURREZIONE:
				statoPrecedente = Stato.ESEECUZIONE_RESURREZIONE;
				stato = Stato.SCELTA_BERSAGLIO_RESURREZIONE;
				processaComando(null);
				return;
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
				processaComando(null);
				return;
			case FLOPPY:
				stato = Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE;
				BusEventi.pubblica(new InternoStatoDiGioco(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE,
						Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5,
						Comando.NO));
				return;
			default:
				break;
		}
		stato = Stato.IN_CAMMINO;
		if (comandiPossibiliPerNumeroPassi != null) {
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(comandiPossibiliPerNumeroPassi));
		}
	}

	private void processaComandoInStatoInCammino(Comando comando) {
		int passi = comando.ordinal() - Comando.NUMERO_1.ordinal() + 1;
		switch(direzione) {
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
		processaComando(null);
	}

	private void processaComandoInStatoAttesaPozioneSalute(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneSalute(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		processaComando(null);
	}

	private void processaComandoInStatoAttesaPozioneSaluteGrande(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneSaluteGrande(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		processaComando(null);
	}

	private void processaComandoInStatoAttesaPozioneMagia(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneMagia(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		processaComando(null);
	}

	private void processaComandoInStatoAttesaPozioneMagiaGrande(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			gruppo.consumaPozioneMagiaGrande(comando);
		}
		stato = Stato.ATTESA_DIREZIONE;
		processaComando(null);
	}

	private void processaComandoInStatoSceltaBersaglioResurrezione(Comando comando) {
		// Come nella scelta del bersaglio quando si formula un incantesimo in
		// combattimento (Stato.SCELTA_PERSONAGGIO_QUALSIASI): se c'è un solo
		// personaggio morto lo si risuscita direttamente, altrimenti si chiede quale.
		comando = scegliPersonaggioMorto();
		if (comando != null) {
			stato = statoPrecedente;
			processaComando(comando);
		}
	}

	private void processaComandoInStatoEsecuzioneResurrezione(Comando comando) {
		if (comando != null && comando != Comando.ANNULLA) {
			eseguiResurrezione(gruppo.getPersonaggio(comando));
		}
		stato = Stato.ATTESA_DIREZIONE;
		processaComando(null);
	}

	private void processaComandoInStatoMappa(Comando comando) {
		Logger.log("Stato MAPPA, azione " + comando);
		if (comando == null) {
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.SI));
			BusEventi.pubblica(new NotificaTestoParagrafo(gruppo.getCapo().getNome(
					Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
					Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " consulta la sua mappa della Foresta."));
			BusEventi.pubblica(new ComandoVisualizzazioneMappa());
		} else {
			if (comando == Comando.SI) {
				stato = statoPrecedente;
				BusEventi.pubblica(new InternoMostraSchermataGioco());
				processaComando(null);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	private void processaComandoInStatoInventario(Comando comando) {
		Logger.log("Stato INVENTARIO, azione " + comando);
		if (comando == null) {
			richiediAperturaInventarioGruppo();
		} else {
			switch (comando) {
				case ANNULLA:
					stato = statoPrecedente;
					BusEventi.pubblica(new InternoMostraSchermataGioco());
					processaComando(null);
					break;
				case PERSONAGGIO_1:
				case PERSONAGGIO_2:
				case PERSONAGGIO_3:
				case PERSONAGGIO_4:
				case PERSONAGGIO_5:
					indicePersonaggioInventario = comando.ordinal() - Comando.PERSONAGGIO_1.ordinal();
					richiediAperturaInventarioGruppo();
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	private void processaComandoInStatoSelezioneSalvataggioDaScrivere(Comando comando) {
		if (comando == Comando.NO) {
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			stato = Stato.ATTESA_DIREZIONE;
			processaComando(null);
		}
		if (comando != null) {
			salva(comando);
			BusEventi.pubblica(new RichiestaUscitaDalGioco());
			stato = Stato.CONFERMA_USCITA;
			processaComando(null);
		}
	}

	private void processaComandoInStatoConfermaUscita(Comando comando) {
		if (comando == Comando.SI) {
			System.exit(0);
		} else if (comando == Comando.NO) {
			BusEventi.pubblica(new InternoMostraSchermataGioco());
			stato = Stato.ATTESA_DIREZIONE;
			processaComando(null);
		}
	}

	private void processaComandoInStatoGiocoPerso(Comando comando) {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		if (comando == null) {
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		} else {
			stato = Stato.GIOCO_PERSO_2;
			processaComando(null);
		}
	}

	private void processaComandoInStatoGiocoPerso2(Comando comando) {
		if (comando == null) {
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
			BusEventi.pubblica(new NotificaFineGioco(false));
			temporizzatore.inizia(5_000);
		} else if (comando == Comando.TIMER) {
			BusEventi.pubblica(new NotificaFineGioco(false));
		} else {
			temporizzatore.termina();
			stato = Stato.STATISTICHE;
			processaComando(null);
		}
	}

	private void processaComandoInStatoGiocoVinto(Comando comando) {
		BusEventi.pubblica(new InternoRichiestaChiusuraFinestraCombattimento());
		if (comando == null) {
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		} else {
			stato = Stato.GIOCO_VINTO_2;
			processaComando(null);
		}
	}

	private void processaComandoInStatoGiocoVinto2(Comando comando) {
		if (comando == null) {
			BusEventi.pubblica(new NotificaFineGioco(true));
			temporizzatore.inizia(5_000);
			BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		} else if (comando == Comando.TIMER) {
			BusEventi.pubblica(new NotificaFineGioco(true));
		} else {
			temporizzatore.termina();
			stato = Stato.STATISTICHE;
			processaComando(null);
		}
	}

	private void processaComandoInStatoStatistiche(Comando comando) {
		if (comando == null) {
			BusEventi.pubblica(new NotificaMostraStatisticheFineGioco());
		} else if (comando == Comando.PERGAMENA) {
			if (GestorePunteggi.isPunteggioInClassifica(Statistiche.getPunti())) {
				stato = Stato.ATTESA_NOME_PUNTEGGI;
				BusEventi.pubblica(new RichiestaTesto("congratulazioni! inserisci il tuo nome"));
			} else {
				stato = Stato.INTRO;
			}
		}
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
	}

	private void processaComandoInStatoPostGameAttesaNomePerPunteggio(String testoDisponibile) {
		if (testoDisponibile.isEmpty()) {
			testoDisponibile = GruppoGiocatore.getIstanza().getPersonaggio(0).getNomeProprio().orElseThrow(Personaggio.PERSONAGGIO_SENZA_NOME);
		}
		GestorePunteggi.addPunteggio(testoDisponibile, Statistiche.getPunti());
		stato = Stato.PUNTEGGI;
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.PERGAMENA));
		BusEventi.pubblica(new NotificaMostraPunteggiMigliori());
		processaComando(null);
	}

	private void processaComandoInStatoPunteggi(Comando comando) {
		inizia();
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
				.setModificatore(TipoAttributo.FORTUNA, TipoModificatore.AUMENTO_PERCENTUALE, 1)
				.setIncantamento("La battuta del cavolo", TipoDanno.GELO, 10, 0.5)
				.costruisci();
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

		stato = Stato.INZIO_LOCAZIONE;
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
		return qualcunoMorto && trovaFormulanteResurrezione() != null;
	}

	private void eseguiResurrezione(Personaggio personaggioBersaglio) {
		Personaggio formulante = trovaFormulanteResurrezione();
		if (formulante == null) {
			// Non dovrebbe accadere: l'azione RESURREZIONE non sarebbe stata proposta.
			return;
		}
		Incantesimo incantesimo = ClasseIncantesimo.RESURREZIONE.getIstanza(formulante.getLivello());
		incantesimo.formula(formulante, personaggioBersaglio, null);
		gruppo.subIncantesimi(ClasseIncantesimo.RESURREZIONE, 1);
	}

	/**
	 * Un personaggio vivo con abbastanza magia da formulare una Resurrezione,
	 * o null se nessuno del gruppo può farlo.
	 */
	private Personaggio trovaFormulanteResurrezione() {
		int costoLancio = ClasseIncantesimo.RESURREZIONE.getIstanza(1).getCostoLancio();
		for (Personaggio personaggioCorrente : gruppo.getPersonaggiVivi()) {
			if (personaggioCorrente.getMagia() >= costoLancio) {
				return personaggioCorrente;
			}
		}
		return null;
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
