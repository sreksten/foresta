package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.tools.GestorePunteggi;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import com.threeamigos.foresta.tools.InterfacciaGestoreSalvataggi;
import com.threeamigos.foresta.tools.Temporizzatore;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

import java.util.ArrayList;
import java.util.function.Consumer;

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
			if (nomePersonaggio.isEmpty()) {
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
				// Qui mettiamo il codice per i personaggi nascosti tipo:
				if (s.equals("OmbraFiamma")) {
					personaggio = new OmbraFiamma("Alakazam", 5);
					stato = Stato.INIZIALIZZAZIONE_GIOCO;
					processaAzione(null);
					break;
				}
				UI.scriviGrande("Scegli il sesso e la classe di " + nomePersonaggio);
				stato = Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO;
				UI.impostaAzioni(Comando.MASCHIO, Comando.FEMMINA);
			}
			break;

		case ATTESA_NOME_PUNTEGGI:
			if (s.isEmpty()) {
				s = GruppoGiocatore.getIstanza().getPersonaggio(0).getNomeProprio().orElseThrow(Personaggio.PERSONAGGIO_SENZA_NOME);
			}
			GestorePunteggi.addPunteggio(s, Statistiche.getPunti());
			stato = Stato.PUNTEGGI;
			UI.impostaAzioni(Comando.PERGAMENA);
			UI.punteggi();
			processaAzione(null);
			break;

		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Questa funzione in base allo stato del gruppo e alla azione ricevuta
	 * è il motore di gioco vero e proprio, ed è quindi abbastanza monumentale.
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
					ComandiPossibili.reimposta();
					for (InterfacciaGestoreSalvataggi.InterfacciaTestataSalvataggio testata : GestoreSalvataggi.getSalvataggiDisponibili()) {
						String id = testata.getId();
						if ("1".equals(id)) {
							ComandiPossibili.add(Comando.NUMERO_1);
						} else if ("2".equals(id)) {
							ComandiPossibili.add(Comando.NUMERO_2);
						} else if ("3".equals(id)) {
							ComandiPossibili.add(Comando.NUMERO_3);
						} else if ("4".equals(id)) {
							ComandiPossibili.add(Comando.NUMERO_4);
						} else if ("5".equals(id)) {
							ComandiPossibili.add(Comando.NUMERO_5);
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
					locazioneCorrente = gruppo.getClasseLocazioneCorrente().getIstanza();
					locazioneCorrente.reimposta();
					UI.reinizializza();
					UI.iniziaGioco();
					UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
					UI.rinfresca();
				} else {
					UI.scriviGrande("Problema nella lettura file");
					stato = Stato.CONTROLLO_SALVATAGGI;
				}
				processaAzione(null);
				break;

			case LETTURA_SALVATAGGIO:

            case ATTESA_NOME_PUNTEGGI:
                break;

			case PRE_GAME_SELEZIONE_PERSONAGGIO:
				ComandiPossibili.reimposta();
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
				stato = Stato.INIZIALIZZAZIONE_GIOCO;
				processaAzione(null);
				break;

			case INIZIALIZZAZIONE_GIOCO:
				UI.reinizializza();
				UI.iniziaGioco();
				gruppo.aggiungiPersonaggioSenzaNotificare(personaggio);
				stato = Stato.INZIO_LOCAZIONE;
				processaAzione(null);
				break;

			case INZIO_LOCAZIONE:
				controllaMissioni(Missione::controllaPreLocazione, OrdineVisita.PADRE_PRIMA);
				String evento = LineaTemporale.getEvento();
				if (evento != null) {
					UI.notifica(evento);
					if (LineaTemporale.isGiocoFinito()) {
						stato = Stato.GIOCO_PERSO;
						processaAzione(null);
						break;
					}
				}
				gruppoAvversario.reimposta();
				locazioneCorrente = gruppo.getClasseLocazioneCorrente().getIstanza();
				locazioneCorrente.reimposta();
				gruppo.setLocazioneCorrente(locazioneCorrente);
				UI.notifica("");
				locazioneCorrente.crea(gruppo, gruppoAvversario);
				UI.preparaLocazione();
				UI.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
				UI.notifica(LineaTemporale.getDescrizioneOraDelGiorno());
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
				 * locazione la possibilità di andare avanti fino a
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
					// Il battito del combattimento viene interrotto ogni volta che si
					// esce da IN_COMBATTIMENTO (per esempio per scegliere chi combatte):
					// qui lo si riavvia, dato che i round sono guidati da Comando.TIMER.
					temporizzatore.inizia(1);
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
				ComandiPossibili.reimposta();
				Personaggio formulante = gruppo.getFormulante();
				for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
					if (gruppo.getIncantesimi(classeIncantesimo) > 0 && formulante.getMagia() >= classeIncantesimo.getIstanza().getCostoLancio()) {
						ComandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
					}
				}
				ComandiPossibili.add(Comando.NO_INCANTESIMO);
				UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
				UI.impostaAzioni();
				stato = Stato.INCANTESIMO_SCELTO;
				break;

			case ATTESA_INCANTESIMO_QUALSIASI:
				ComandiPossibili.reimposta();
				for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
					ComandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
				}
				ComandiPossibili.add(Comando.NO_INCANTESIMO);
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
				} else if (azione != Comando.TIMER) {
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
								Logger.log("L'oggetto non si lascia prendere con l'azione " + azione);
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
					controllaMissioni(Missione::controllaPostLocazione, OrdineVisita.FIGLI_PRIMA);
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

				stato = Stato.ATTESA_DIREZIONE;
				processaAzione(null);
				break;

			case ATTESA_DIREZIONE:
				UI.notifica(gruppo.chiMaiuscolo() + " se ne va. In quale direzione si incammina?");
				ComandiPossibili.reimposta();
				if (gruppo.getMaxPassiNord() > 0) {
					ComandiPossibili.add(Comando.NORD);
				}
				if (gruppo.getMaxPassiEst() > 0) {
					ComandiPossibili.add(Comando.EST);
				}
				if (gruppo.getMaxPassiSud() > 0) {
					ComandiPossibili.add(Comando.SUD);
				}
				if (gruppo.getMaxPassiOvest() > 0) {
					ComandiPossibili.add(Comando.OVEST);
				}
				ComandiPossibili.add(Comando.MAPPA);
				if (gruppo.getNumeroPersonaggiVivi() > 1 && (LineaTemporale.getOra() > 20 || LineaTemporale.getOra() < 6)) {
					ClassiLocazione classeLocazione = gruppo.getClasseLocazioneCorrente();
					if (classeLocazione.getTipoLocazione() != TipoLocazione.CITTA &&
							classeLocazione != ClassiLocazione.LOCANDA &&
							classeLocazione != ClassiLocazione.PALUDE) {
						ComandiPossibili.add(Comando.ACCAMPAMENTO);
					}
				}
				if (gruppo.getPozioniSalute() > 0) {
					ComandiPossibili.add(Comando.POZIONE_SALUTE);
				}
				if (gruppo.getPozioniSaluteGrande() > 0) {
					ComandiPossibili.add(Comando.POZIONE_SALUTE_GRANDE);
				}
				if (gruppo.getPozioniMagia() > 0) {
					ComandiPossibili.add(Comando.POZIONE_MAGIA);
				}
				if (gruppo.getPozioniMagiaGrande() > 0) {
					ComandiPossibili.add(Comando.POZIONE_MAGIA_GRANDE);
				}
				ComandiPossibili.add(Comando.FLOPPY);

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
					gruppo.pernotta(locazioneCorrente.getTipoRiposo());
					LineaTemporale.mattinoSeguente();
					LineaTemporale.eventi(gruppo);
					UI.primoPiano(InterfacciaUtente.Finestra.STATO);
					stato = Stato.ATTESA_DIREZIONE;
					processaAzione(null);
					return;
				case POZIONE_SALUTE:
					statoPrecedente = Stato.ATTESA_POZIONE_SALUTE;
					stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
					processaAzione(null);
					return;
				case POZIONE_SALUTE_GRANDE:
					statoPrecedente = Stato.ATTESA_POZIONE_SALUTE_GRANDE;
					stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
					processaAzione(null);
					return;
				case POZIONE_MAGIA:
					statoPrecedente = Stato.ATTESA_POZIONE_MAGIA;
					stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
					processaAzione(null);
					return;
				case POZIONE_MAGIA_GRANDE:
					statoPrecedente = Stato.ATTESA_POZIONE_MAGIA_GRANDE;
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

			case ATTESA_POZIONE_SALUTE:
				if (azione != null && azione != Comando.ANNULLA) {
					gruppo.consumaPozioneSalute(azione);
				}
				stato = Stato.ATTESA_DIREZIONE;
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				processaAzione(null);
				break;

			case ATTESA_POZIONE_SALUTE_GRANDE:
				if (azione != null && azione != Comando.ANNULLA) {
					gruppo.consumaPozioneSaluteGrande(azione);
				}
				stato = Stato.ATTESA_DIREZIONE;
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				processaAzione(null);
				break;

			case ATTESA_POZIONE_MAGIA:
				if (azione != null && azione != Comando.ANNULLA) {
					gruppo.consumaPozioneMagia(azione);
				}
				stato = Stato.ATTESA_DIREZIONE;
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				processaAzione(null);
				break;

			case ATTESA_POZIONE_MAGIA_GRANDE:
				if (azione != null && azione != Comando.ANNULLA) {
					gruppo.consumaPozioneMagiaGrande(azione);
				}
				stato = Stato.ATTESA_DIREZIONE;
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				processaAzione(null);
				break;

			case MAPPA:
				Logger.log("Stato MAPPA, azione " + azione);
				if (azione == null) {
					UI.impostaAzioni(Comando.SINISTRA, Comando.SU, Comando.GIU, Comando.DESTRA, Comando.SI);
					UI.notifica(gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " consulta la sua mappa della Foresta.");
					UI.mappa();
				} else {
					switch (azione) {
					case SI:
						stato = statoPrecedente;
						UI.iniziaGioco();
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
						stato = Stato.ATTESA_NOME_PUNTEGGI;
						UI.scriviGrande("congratulazioni! inserisci il tuo nome");
						UI.chiediTesto();
					} else {
						stato = Stato.INTRO;
					}
				}
				UI.impostaAzioni(Comando.PERGAMENA);
				break;

            case PUNTEGGI:
				inizia();
				break;

			default:
				throw new IllegalStateException("Stato " + stato + " non correttamente gestito!");
		}
	}

	/**
	 * L'ordine con cui un controllo percorre l'albero delle missioni. Non e' un
	 * dettaglio di efficienza: decide quale stato una missione vede nell'altra meta'
	 * dell'albero, e quindi quali cascate si chiudono nello stesso giro anziche' in
	 * quello dopo.
	 */
	private enum OrdineVisita {
		/**
		 * Il padre viene controllato prima dei figli. L'attivazione scende: una missione
		 * che si attiva adesso porta con se' le proprie figlie nello stesso giro, cosi'
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
		for (Missione missione : RegistroMissioni.getMissioni()) {
			controllaMissione(missione, controllo, ordineVisita);
		}
	}

	private void controllaMissione(Missione missione, Consumer<Missione> controllo, OrdineVisita ordineVisita) {
		if (ordineVisita == OrdineVisita.PADRE_PRIMA) {
			controllo.accept(missione);
		}
		// Nei rami spenti o gia' conclusi non si scende, e le figlie completate si
		// saltano: e' lo stesso filtro che getMissioni() applica alle radici. Con
		// FIGLI_PRIMA la condizione si valuta prima che il padre sia controllato,
		// quindi una missione che si attiva adesso vedra' le proprie figlie al giro
		// successivo.
		if (missione.isAttiva() && !missione.isCompleta()) {
			// Copia difensiva: un controllo puo' aggiungere sotto-missioni al nodo
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

	/**
	 * Riporta Azione.PERSONAGGIO_1 se un unico personaggio è disponibile,
	 * altrimenti null e imposta le azioni per scegliere il personaggio
	 */
	private Comando scegliPersonaggio(boolean ancheSeMorto) {
		if (gruppo.getNumeroPersonaggiVivi() == 1 && !ancheSeMorto) {
			Logger.log("Automa::scegliPersonaggio(ancheMorto=" + ancheSeMorto + "): automaticamente PERSONAGGIO_1");
			return Comando.PERSONAGGIO_1;
		} else {
			ComandiPossibili.reimposta();
			int i = 0;
			for (Personaggio personaggioCorrente : gruppo.getPersonaggi()) {
				if (ancheSeMorto || personaggioCorrente.isVivo()) {
					ComandiPossibili.add(Comando.ofPersonaggio(i));
				}
				i++;
			}
			ComandiPossibili.add(Comando.ANNULLA);
			Logger.log("Automa::scegliPersonaggio(ancheMorto=" + ancheSeMorto + "): imposto le azioni");
			UI.impostaAzioni();
			return null;
		}
	}

	private void impostaAzioniPerNumeroPassi(int numeroPassi) {
		ComandiPossibili.reimposta();
		if (numeroPassi > 0) {
			ComandiPossibili.add(Comando.NUMERO_1);
		}
		if (numeroPassi > 1) {
			ComandiPossibili.add(Comando.NUMERO_2);
		}
		if (numeroPassi > 2) {
			ComandiPossibili.add(Comando.NUMERO_3);
		}
		if (numeroPassi > 3) {
			ComandiPossibili.add(Comando.NUMERO_4);
		}
		if (numeroPassi > 4) {
			ComandiPossibili.add(Comando.NUMERO_5);
		}
	}
	
	private String convertiComandoInSlotSalvataggio(Comando azione) {
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
	
	private boolean leggi(Comando azione) {
		return GestoreSalvataggi.leggi(convertiComandoInSlotSalvataggio(azione));
	}

	private void salva(Comando azione) {
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
			sb.append(gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA))
			.append(" - giorno ")
			.append(LineaTemporale.getGiorno())
			.append(", ora ")
			.append(LineaTemporale.getOra());
			GestoreSalvataggi.salva(id, sb.toString());
		}
	}
}
