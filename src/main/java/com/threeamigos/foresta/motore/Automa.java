package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Cantastorie;
import com.threeamigos.foresta.personaggi.Elfa;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.Guerriera;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladra;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Maga;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.OmbraFiamma;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.GestorePunteggi;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import com.threeamigos.foresta.tools.InterfacciaGestoreSalvataggi;
import com.threeamigos.foresta.tools.Temporizzatore;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

public class Automa implements ControlloreDiGioco {

	private static final String SCEGLI_NOME_PERSONAGGIO = "Scegli il nome del tuo personaggio o lascialo vuoto per un personaggio casuale.";

	private String nomePersonaggio;
	private Stato stato;
	private Stato statoPrecedente;

	private Temporizzatore temporizzatore;
	private GruppoGiocatore gruppo;
	private GruppoAvversario gruppoAvversario;
	private Personaggio personaggio;
	private Locazione locazioneCorrente;
	private Comando direzione; // serve a memorizzare la direzione prima di chiedere il numero di passi

	public void setTemporizzatore(Temporizzatore temporizzatore) {
		if (this.temporizzatore != temporizzatore) {
			this.temporizzatore = temporizzatore;
			temporizzatore.setControlloreDiGioco(this);
		}
	}

	public void inizia() {
		stato = Stato.INTRO;
		UI.intro();
		UI.impostaAzioni(Comando.PERGAMENA);
		temporizzatore.inizia(5);
	}

	public void riceviTesto(String s) {
		switch (stato) {

		case PRE_GAME_ATTESA_NOME_PERSONAGGIO:
			Foresta.reimposta();
			gruppo = GruppoGiocatore.getIstanza();
			gruppoAvversario = GruppoAvversario.getIstanza();

			nomePersonaggio = s.trim();
			if (nomePersonaggio.equals("")) {
				personaggio = RegistroPersonaggi.getPersonaggioCasuale();
				if (personaggio == null) {
					UI.scriviGrande("Non ci sono personaggi disponibili. Occorre crearne uno specificando il nome.");
					UI.chiediTesto();
				} else {
					stato = Stato.INIZIALIZZAZIONE_GIOCO;
				}
				processaAzione(null);
				break;
			} else {
				// Qui mettiamo il codice per i personaggi nascosti :) tipo:
				if (s.equals("OmbraFiamma")) {
					personaggio = new OmbraFiamma("Alakazam");
					stato = Stato.INIZIALIZZAZIONE_GIOCO;
					processaAzione(null);
					break;
				}
				UI.scriviGrande("Scegli il sesso e la classe di " + nomePersonaggio);
				stato = Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO;
				UI.impostaAzioni(Comando.MASCHIO, Comando.FEMMINA);
			}
			break;

		case ATTESA_NOME_HI_SCORE:
			if (s.length() == 0) {
				s = GruppoGiocatore.getIstanza().getCapo().getNome();
			}
			GestorePunteggi.addRecord(s, Statistiche.getPunti());
			stato = Stato.HI_SCORE;
			UI.impostaAzioni(Comando.PERGAMENA);
			UI.hiscore();
			processaAzione(null);
			break;

		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Questa funzione in base allo stato del gruppo e alla azione ricevuta
	 * e' il motore di gioco vero e proprio, ed e' quindi abbastanza monumentale.
	 * Per alcuni stati l'azione ricevuta non serve a nulla, giusto per cambiare
	 * lo stato stesso.
	 */
	public void processaAzione(Comando azione) {
		Logger.log("Automa in stato " + stato.name() + "; processo azione " + azione);
		switch (stato) {

		case INTRO:
			if (azione == Comando.TIMER) {
				UI.intro();
			} else if (azione == Comando.PERGAMENA) {
				temporizzatore.termina();
				if (GestoreSalvataggi.getSalvataggiDisponibili().isEmpty()) {
					stato = Stato.PRE_GAME_SELEZIONE_PERSONAGGIO;
					processaAzione(null);
				} else {
					UI.nuovoGiocoOCaricaPrecedente();
					UI.impostaAzioni(Comando.NUMERO_1, Comando.NUMERO_2);
					stato = Stato.SELEZIONE_NUOVO_GIOCO_O_CARICA;
					processaAzione(null);
				}
				UI.rinfresca();
			}
			break;

		case SELEZIONE_NUOVO_GIOCO_O_CARICA:
			if (azione == Comando.NUMERO_1) {
				stato = Stato.PRE_GAME_SELEZIONE_PERSONAGGIO;
				processaAzione(null);
			} else if (azione == Comando.NUMERO_2) {
				stato = Stato.SELEZIONE_SALVATAGGIO_DA_LEGGERE;
				Azioni.clear();
				for (InterfacciaGestoreSalvataggi.InterfacciaTestataSalvataggio testata : GestoreSalvataggi.getSalvataggiDisponibili()) {
					String id = testata.getId();
					if ("1".equals(id)) {
						Azioni.add(Comando.NUMERO_1);
					} else if ("2".equals(id)) {
						Azioni.add(Comando.NUMERO_2);
					} else if ("3".equals(id)) {
						Azioni.add(Comando.NUMERO_3);
					} else if ("4".equals(id)) {
						Azioni.add(Comando.NUMERO_4);
					} else if ("5".equals(id)) {
						Azioni.add(Comando.NUMERO_5);
					}
				}
				UI.impostaAzioni();
				UI.selezioneSlotSalvataggioDaCaricare();
			}
			break;
			
		case SELEZIONE_SALVATAGGIO_DA_LEGGERE:
			if (leggi(azione)) {
				stato = Stato.ATTESA_DIREZIONE;
				gruppo = GruppoGiocatore.getIstanza();
				gruppoAvversario = GruppoAvversario.getIstanza();
				locazioneCorrente = gruppo.getClasseLocazione().getIstanza();
				locazioneCorrente.reimposta();
				UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
				UI.rinfresca();
			} else {
				UI.scriviGrande("Problema nella lettura file");
				stato = Stato.CONTROLLO_SALVATAGGI;
			}
			processaAzione(null);
			break;
			
		case LETTURA_SALVATAGGIO:
			break;			

		case PRE_GAME_SELEZIONE_PERSONAGGIO:
			Azioni.clear();
			UI.impostaAzioni();
			UI.scriviGrande(SCEGLI_NOME_PERSONAGGIO);
			stato = Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO;
			UI.chiediTesto();
			break;

		case PRE_GAME_ATTESA_SESSO_PERSONAGGIO:
			stato = Stato.PRE_GAME_ATTESA_CLASSE_PERSONAGGIO;
			if (azione == Comando.FEMMINA) {
				UI.impostaAzioni(Comando.GUERRIERA, Comando.LADRA, Comando.CANTASTORIE, Comando.ELFA, Comando.MAGA);
			} else {
				UI.impostaAzioni(Comando.GUERRIERO, Comando.LADRO, Comando.BARDO, Comando.ELFO, Comando.MAGO);
			}
			break;

		case PRE_GAME_ATTESA_CLASSE_PERSONAGGIO:
			switch (azione) {
			case GUERRIERA:
				personaggio = new Guerriera(nomePersonaggio);
				break;
			case GUERRIERO:
				personaggio = new Guerriero(nomePersonaggio);
				break;
			case LADRA:
				personaggio = new Ladra(nomePersonaggio);
				break;
			case LADRO:
				personaggio = new Ladro(nomePersonaggio);
				break;
			case CANTASTORIE:
				personaggio = new Cantastorie(nomePersonaggio);
				break;
			case BARDO:
				personaggio = new Bardo(nomePersonaggio);
				break;
			case ELFA:
				personaggio = new Elfa(nomePersonaggio);
				break;
			case ELFO:
				personaggio = new Elfo(nomePersonaggio);
				break;
			case MAGA:
				personaggio = new Maga(nomePersonaggio);
				break;
			case MAGO:
				personaggio = new Mago(nomePersonaggio);
				break;
			default:
				throw new IllegalArgumentException();
			}
			stato = Stato.INIZIALIZZAZIONE_GIOCO;
			processaAzione(null);
			break;

		case INIZIALIZZAZIONE_GIOCO:
			UI.reinizializza();
			gruppo.aggiungiPersonaggioSenzaNotificare(personaggio);
			stato = Stato.INZIO_LOCAZIONE;
			processaAzione(null);
			break;

		case INZIO_LOCAZIONE:
			for (Missione missione : RegistroMissioni.getMissioni()) {
				missione.controllaPreLocazione();
			}
			String evento = LineaTemporale.getEvento();
			if (evento != null) {
				UI.notifica(evento);
				if (LineaTemporale.isGiocoFinito()) {
					stato = Stato.GIOCO_PERSO;
					processaAzione(null);
					break;
				}
			}
			gruppo.setFuggito(false);
			gruppoAvversario.reimposta();
			locazioneCorrente = gruppo.getClasseLocazione().getIstanza();
			locazioneCorrente.reimposta();
			gruppo.setLocazioneCorrente(locazioneCorrente);
			UI.notifica("");
			locazioneCorrente.crea(gruppo, gruppoAvversario);
			UI.preparaLocazione();
			UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
			UI.notifica(LineaTemporale.getDescrizioneOraDelGiorno());
			locazioneCorrente.descrivi(gruppo, gruppoAvversario);
			for (Missione missione : RegistroMissioni.getMissioni()) {
				missione.controllaInLocazione();
			}
			/*
			 * Ogni locazione ha un metodo impostaAzioni; nel caso delle
			 * locazioni di base impostera' le azioni combattimento,
			 * incantesimo, corruzione, amicizia... mentre per alcune
			 * locazioni specifiche permettera' di accettare la proposta
			 * di aggregazione di altri personaggi eccetera. Se la locazione
			 * e' automaticamente completata il metodo torna LOCAZIONE_COMPLETA.
			 * Altrimenti ogni locazione e' in effetti un automa a stati finiti
			 * che tiene traccia del suo stato.
			 */
			stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, null);
			if (stato == Stato.FINE_LOCAZIONE) {
				processaAzione(null);
				break;
			}
			UI.impostaAzioni();
			/*
			 * A questo punto il giocatore si trova davanti la scelta delle
			 * azioni che puo' intraprendere.
			 */
			break;

		case IN_LOCAZIONE:
			statoPrecedente = stato;
			/*
			 * Continuiamo a fornire all'automa a stati finiti della
			 * locazione la possibilita' di andare avanti fino a
			 * LOCAZIONE_COMPLETA
			 */
			stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, azione);
			if (stato != Stato.IN_LOCAZIONE) {
				if (stato == Stato.IN_COMBATTIMENTO) {
					temporizzatore.inizia(1);
				} else {
					if (stato == Stato.GIOCO_PERSO || stato == Stato.GIOCO_VINTO || stato == Stato.FINE_LOCAZIONE) {
						temporizzatore.termina();
					}
					processaAzione(null);
					break;
				}
			}
			UI.impostaAzioni();
			break;

		case IN_COMBATTIMENTO:
			statoPrecedente = stato;
			stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, azione);
			if (stato != Stato.IN_COMBATTIMENTO) {
				temporizzatore.termina();
				processaAzione(null);
				break;
			}
			if (azione != Comando.TIMER) {
				UI.impostaAzioni();
			}
			break;

		case SCELTA_AUTOMATICA_PERSONAGGIO:
			azione = scegliPersonaggio(false);
			if (azione != null) {
				Logger.log(Stato.SCELTA_AUTOMATICA_PERSONAGGIO.name() + ": Torno allo stato " + statoPrecedente.name());
				stato = statoPrecedente;
				processaAzione(azione);
				break;
			} else {
				stato = Stato.SCELTA_MANUALE_PERSONAGGIO;
				processaAzione(null);
			}
			break;

		case SCELTA_PERSONAGGIO_QUALSIASI:
			azione = scegliPersonaggio(true);
			if (azione != null) {
				Logger.log(Stato.SCELTA_PERSONAGGIO_QUALSIASI.name() + ": Torno allo stato " + statoPrecedente.name());
				stato = statoPrecedente;
				processaAzione(azione);
				break;
			} else {
				stato = Stato.SCELTA_MANUALE_PERSONAGGIO;
				processaAzione(null);
			}
			break;

		case SCELTA_MANUALE_PERSONAGGIO:
			if (azione != null) {
				Logger.log(Stato.SCELTA_MANUALE_PERSONAGGIO.name() + ": Torno allo stato " + statoPrecedente.name());
				stato = statoPrecedente;
				processaAzione(azione);
			}
			break;

		case SCELTA_INCANTESIMO_DA_LANCIARE:
			Azioni.clear();
			for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
				if (gruppo.getIncantesimi(classeIncantesimo) > 0) {
					Azioni.add(classeIncantesimo.getComandoDiAttivazione());
				}
			}
			Azioni.add(Comando.NO_INCANTESIMO);
			UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
			UI.impostaAzioni();
			stato = Stato.INCANTESIMO_SCELTO;
			break;

		case ATTESA_INCANTESIMO_QUALSIASI:
			Azioni.clear();
			for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
				Azioni.add(classeIncantesimo.getComandoDiAttivazione());
			}
			Azioni.add(Comando.NO_INCANTESIMO);
			UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
			UI.impostaAzioni();
			stato = Stato.INCANTESIMO_SCELTO;
			break;

		case INCANTESIMO_SCELTO:
			stato = Stato.IN_LOCAZIONE;
			processaAzione(azione);
			break;

		case ATTESA_SI_NO:
			if (azione == null) {
				UI.impostaAzioni(Comando.SI, Comando.NO);
			} else if (azione == Comando.TIMER) {
				// non fa niente
			} else {
				stato = statoPrecedente;
				processaAzione(azione);
			}
			break;

		case FINE_LOCAZIONE:
			temporizzatore.termina();
			UI.infoCombattimento(false, null, null);
			UI.primoPiano(InterfacciaUtente.Finestra.STATO);

			// Recuperiamo l'oggetto se fattibile
			if (locazioneCorrente.isCompleta()) {				
				if (!locazioneCorrente.isHaStrettoAmicizia()) {
					Oggetto oggetto = locazioneCorrente.getOggetto();
					if (oggetto != null) {
						Logger.log("Prendo oggetto utilizzando azione " + azione);
						if (!oggetto.prendi(gruppo, azione)) {
							Logger.log("L'oggetto non si lascia prendere con l'azione specificata");
							statoPrecedente = Stato.FINE_LOCAZIONE;
							stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
							processaAzione(null);
							break;
						} else {
							UI.raccogliOggetto();
							Logger.log("Ho preso l'oggetto");
							locazioneCorrente.rimuoviOggetto();
						}
					}
				}
				locazioneCorrente.azzeraLocazione(gruppo);
				for (Missione missione : RegistroMissioni.getMissioni()) {
					missione.controllaPostLocazione();
				}
			}

			if (LineaTemporale.isGiocoFinito()) {
				if (RegistroMissioni.getMissionePrincipale().isCompleta()) {
					stato = Stato.GIOCO_VINTO;
				} else {
					stato = Stato.GIOCO_PERSO;
				}
				processaAzione(null);
				break;
			}

			// Controlliamo i personaggi "a tempo"
			for (Personaggio personaggioCorrente : gruppo.getPersonaggiVivi()) {
				int tempo = personaggioCorrente.decrementaTempo();
				if (tempo != Personaggio.NO_TEMPO && tempo == 0) {
					gruppo.rimuoviPersonaggio(personaggioCorrente);
				}
			}

			// Aumentiamo la stanchezza
			for (Personaggio personaggioCorrente : gruppo.getPersonaggiVivi()) {
				personaggioCorrente.addStanchezza(1);
			}

			stato = Stato.ATTESA_DIREZIONE;
			processaAzione(null);
			break;

		case ATTESA_DIREZIONE:
			UI.notifica(gruppo.chiMaiuscolo() + " se ne va. In quale direzione si incammina?");
			Azioni.clear();
			if (gruppo.getMaxPassiNord() > 0) {
				Azioni.add(Comando.NORD);
			}
			if (gruppo.getMaxPassiEst() > 0) {
				Azioni.add(Comando.EST);
			}
			if (gruppo.getMaxPassiSud() > 0) {
				Azioni.add(Comando.SUD);
			}
			if (gruppo.getMaxPassiOvest() > 0) {
				Azioni.add(Comando.OVEST);
			}
			Azioni.add(Comando.MAPPA);
			if (gruppo.getNumeroPersonaggiVivi() > 1 && (LineaTemporale.getOra() > 20 || LineaTemporale.getOra() < 6)) {
				ClassiLocazione classeLocazione = gruppo.getClasseLocazione();
				if (classeLocazione.getTipoLocazione() != TipoLocazione.CITTA && 
						classeLocazione != ClassiLocazione.LOCANDA &&
						classeLocazione != ClassiLocazione.PALUDE) {
					Azioni.add(Comando.ACCAMPAMENTO);
				}
			}
			if (gruppo.getPozioniForza() > 0) {
				Azioni.add(Comando.FORZA);
			}
			if (gruppo.getPozioniGrandeForza() > 0) {
				Azioni.add(Comando.GRANDE_FORZA);
			}
			if (gruppo.getPozioniMagia() > 0) {
				Azioni.add(Comando.MAGIA);
			}
			Azioni.add(Comando.FLOPPY);

			stato = Stato.ATTESA_PASSI;
			UI.impostaAzioni();
			UI.rinfresca();
			break;

		case ATTESA_PASSI:
			// Occorre memorizzare l'informazione sulla direzione
			switch (azione) {
			case MAPPA:
				statoPrecedente = Stato.ATTESA_DIREZIONE;
				stato = Stato.MAPPA;
				UI.centraMappa();
				processaAzione(null);
				return;
			case ACCAMPAMENTO:
				gruppo.pernotta();
				LineaTemporale.mattinoSeguente();
				LineaTemporale.eventi(gruppo);
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				stato = Stato.ATTESA_DIREZIONE;
				processaAzione(null);
				return;
			case FORZA:
				statoPrecedente = Stato.ATTESA_FORZA;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				processaAzione(null);
				return;
			case GRANDE_FORZA:
				statoPrecedente = Stato.ATTESA_GRANDE_FORZA;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				processaAzione(null);
				return;
			case MAGIA:
				statoPrecedente = Stato.ATTESA_MAGIA;
				stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				processaAzione(null);
				return;
			case NORD:
				direzione = Comando.NORD;
				impostaAzioniPerNumeroPassi(gruppo.getMaxPassiNord());
				break;
			case EST:
				direzione = Comando.EST;
				impostaAzioniPerNumeroPassi(gruppo.getMaxPassiEst());
				break;
			case SUD:
				direzione = Comando.SUD;
				impostaAzioniPerNumeroPassi(gruppo.getMaxPassiSud());
				break;
			case OVEST:
				direzione = Comando.OVEST;
				impostaAzioniPerNumeroPassi(gruppo.getMaxPassiOvest());
				break;
			case FLOPPY:
				stato = Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE;
				UI.impostaAzioni(Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5, Comando.NO);
				UI.selezioneSlotSalvataggioDaSalvare();
				UI.rinfresca();
				processaAzione(null);
				return;
			default:
				break;
			}
			stato = Stato.IN_CAMMINO;
			UI.impostaAzioni();
			break;

		case IN_CAMMINO:
			int passi = azione.ordinal() - Comando.NUMERO_1.ordinal() + 1;
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
			processaAzione(null);
			break;

		case ATTESA_FORZA:
			if (azione != Comando.ANNULLA) {
				personaggio = gruppo.getPersonaggio(azione);
				personaggio.addForza(100);
				gruppo.subPozioniForza(1);
				UI.notifica(personaggio.getNome() + " ha bevuto una pozione che fa riacquistare forza.");
			}
			stato = Stato.ATTESA_DIREZIONE;
			UI.primoPiano(InterfacciaUtente.Finestra.STATO);
			processaAzione(null);
			break;

		case ATTESA_GRANDE_FORZA:
			if (azione != Comando.ANNULLA) {
				personaggio = gruppo.getPersonaggio(azione);
				personaggio.addForza(personaggio.getForzaMassima());
				personaggio.addForzaMassima(10);
				gruppo.subPozioniGrandeForza(1);
				UI.notifica(personaggio.getNome() + " ha bevuto una pozione che fa aumentare la forza!");
			}
			stato = Stato.ATTESA_DIREZIONE;
			UI.primoPiano(InterfacciaUtente.Finestra.STATO);
			processaAzione(null);
			break;

		case ATTESA_MAGIA:
			if (azione != Comando.ANNULLA) {
				personaggio = gruppo.getPersonaggio(azione);
				personaggio.addMagia(10);
				gruppo.subPozioniMagia(1);
				UI.notifica(personaggio.getNome() + " ha bevuto una pozione che fa acquistare magia.");
			}
			stato = Stato.ATTESA_DIREZIONE;
			UI.primoPiano(InterfacciaUtente.Finestra.STATO);
			processaAzione(null);
			break;

		case MAPPA:
			Logger.log("Stato MAPPA, azione " + azione);
			if (azione == null) {
				UI.impostaAzioni(Comando.SINISTRA, Comando.SU, Comando.GIU, Comando.DESTRA, Comando.SI);
				UI.notifica(gruppo.getCapo().getNome() + " consulta la sua mappa della Foresta.");
				UI.mappa();
			} else {
				switch (azione) {
				case SI:
					stato = statoPrecedente;
					UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
					processaAzione(null);
					break;
				case SINISTRA:
				case SU:
				case GIU:
				case DESTRA:
					UI.muoviMappa(azione);
					UI.impostaAzioni();
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
			break;

		case SELEZIONE_SALVATAGGIO_DA_SCRIVERE:
			if (azione == Comando.NO) {
				UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
				stato = Stato.ATTESA_DIREZIONE;
				processaAzione(null);
			}
			if (convertiComandoInSlotSalvataggio(azione) != null) {
				salva(azione);
				UI.confermaUscita();
				UI.impostaAzioni(Comando.SI, Comando.NO);
				stato = Stato.CONFERMA_USCITA;
				processaAzione(null);
			}
			break;
			
		case CONFERMA_USCITA:
			if (azione == Comando.SI) {
				System.exit(0);
			} else if (azione == Comando.NO) {
				UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
				stato = Stato.ATTESA_DIREZIONE;
				processaAzione(null);
			}
			break;

		case GIOCO_PERSO:
			UI.infoCombattimento(false, null, null);
			if (azione == null) {
				UI.impostaAzioni(Comando.PERGAMENA);
			} else {
				stato = Stato.GIOCO_PERSO_2;
				processaAzione(null);
			}
			break;

		case GIOCO_PERSO_2:
			if (azione == null) {
				UI.impostaAzioni(Comando.PERGAMENA);
				UI.perso();
				temporizzatore.inizia(5);
			} else if (azione == Comando.TIMER) {
				UI.perso();
			} else {
				stato = Stato.STATISTICHE;
				processaAzione(null);
			}
			break;

		case GIOCO_VINTO:
			UI.infoCombattimento(false, null, null);
			if (azione == null) {
				UI.impostaAzioni(Comando.PERGAMENA);
			} else {
				stato = Stato.GIOCO_VINTO_2;
				processaAzione(null);
			}
			break;

		case GIOCO_VINTO_2:
			if (azione == null) {
				UI.vinto();
				temporizzatore.inizia(5);
				UI.impostaAzioni(Comando.PERGAMENA);
			} else if (azione == Comando.TIMER) {
				UI.vinto();
			} else {
				stato = Stato.STATISTICHE;
				processaAzione(null);
			}
			break;

		case STATISTICHE:
			if (azione == null) {
				UI.statistiche();
			} else if (azione == Comando.PERGAMENA) {
				if (GestorePunteggi.isPunteggioInClassifica(Statistiche.getPunti())) {
					stato = Stato.ATTESA_NOME_HI_SCORE;
					UI.scriviGrande("congratulazioni! inserisci il tuo nome");
					UI.chiediTesto();
				} else {
					stato = Stato.INTRO;
				}
			}
			UI.impostaAzioni(Comando.PERGAMENA);
			break;

		case HI_SCORE:
			inizia();
			break;

		default:
			throw new IllegalStateException("Stato " + stato + " non correttamente gestito!");
		}
	}

	/**
	 * Riporta Azione.PERSONAGGIO_1 se un unico personaggio è disponibile,
	 * altrimenti null ed imposta le azioni per scegliere il personaggio
	 */
	private Comando scegliPersonaggio(boolean ancheSeMorto) {
		if (gruppo.getNumeroPersonaggiVivi() == 1) {
			Logger.log("Automa::scegliPersonaggio(ancheMorto=" + ancheSeMorto + "): automaticamente PERSONAGGIO_1");
			return Comando.PERSONAGGIO_1;
		} else {
			Azioni.clear();
			int i = 0;
			for (Personaggio personaggioCorrente : gruppo.getPersonaggi()) {
				if (ancheSeMorto || personaggioCorrente.isVivo()) {
					Azioni.add(Comando.ofPersonaggio(i));
				}
				i++;
			}
			Azioni.add(Comando.ANNULLA);
			Logger.log("Automa::scegliPersonaggio(ancheMorto=" + ancheSeMorto + "): imposto le azioni");
			UI.impostaAzioni();
			return null;
		}
	}

	private final void impostaAzioniPerNumeroPassi(int numeroPassi) {
		Azioni.clear();
		if (numeroPassi > 0) {
			Azioni.add(Comando.NUMERO_1);
		}
		if (numeroPassi > 1) {
			Azioni.add(Comando.NUMERO_2);
		}
		if (numeroPassi > 2) {
			Azioni.add(Comando.NUMERO_3);
		}
		if (numeroPassi > 3) {
			Azioni.add(Comando.NUMERO_4);
		}
		if (numeroPassi > 4) {
			Azioni.add(Comando.NUMERO_5);
		}
	}
	
	private final String convertiComandoInSlotSalvataggio(Comando azione) {
		if (azione == Comando.NUMERO_1) {
			return "1";
		} else if (azione == Comando.NUMERO_2) {
			return "2";
		} else if (azione == Comando.NUMERO_3) {
			return "3";
		} else if (azione == Comando.NUMERO_4) {
			return "4";
		} else if (azione == Comando.NUMERO_5) {
			return "5";
		} else {
			return null;
		}
	}
	
	private final boolean leggi(Comando azione) {
		return GestoreSalvataggi.leggi(convertiComandoInSlotSalvataggio(azione));
	}

	private final void salva(Comando azione) {
		String id = convertiComandoInSlotSalvataggio(azione);
		if (id != null) {
			StringBuilder sb = new StringBuilder();
			int numeroPersonaggi = gruppo.getNumeroPersonaggi();
			for (int i = 0; i < numeroPersonaggi; i++) {
				sb.append(gruppo.getPersonaggio(i).getClasse().ordinal());
				if (i < numeroPersonaggi - 1) {
					sb.append(",");
				}
			}
			sb.append("|");
			sb.append(gruppo.getCapo().getNome())
			.append(" - giorno ")
			.append(LineaTemporale.getGiorno())
			.append(", ora ")
			.append(LineaTemporale.getOra());
			GestoreSalvataggi.salva(id, sb.toString());
		}
	}
}
