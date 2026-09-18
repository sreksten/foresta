package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.*;
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
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class Automa implements ControlloreDiGioco, Temporizzabile {

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

		BusEventi.iscriviti(EventoComandoDiGioco.class, this::onEventoComandoDiGioco);
		BusEventi.iscriviti(EventoTestoDisponibile.class, this::onEventoTestoDisponibile);
	}

	public void tick() {
		processaAzione(Comando.TIMER);
	}

	public void inizia() {
		stato = Stato.INTRO;
		BusEventi.pubblica(new EventoStatoDiGioco(Stato.INTRO, getComandiPossibiliInStatoIntro()));
	}

	private void onEventoTestoDisponibile(EventoTestoDisponibile evento) {

		String testoDisponibile = evento.getTesto();

		switch (stato) {

			case PRE_GAME_ATTESA_NOME_PERSONAGGIO:
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
					processaAzione(null);
					break;
				}
				stato = Stato.PRE_GAME_ATTESA_SESSO_PERSONAGGIO;
				BusEventi.pubblica(new EventoStatoDiGioco(stato, Comando.MASCHIO, Comando.FEMMINA));
				break;

			case ATTESA_NOME_PUNTEGGI:
				if (testoDisponibile.isEmpty()) {
					testoDisponibile = GruppoGiocatore.getIstanza().getPersonaggio(0).getNomeProprio().orElseThrow(Personaggio.PERSONAGGIO_SENZA_NOME);
				}
				GestorePunteggi.addPunteggio(testoDisponibile, Statistiche.getPunti());
				stato = Stato.PUNTEGGI;
				UI.impostaAzioni(Comando.PERGAMENA);
				BusEventi.pubblica(new EventoMostraPunteggi());
				processaAzione(null);
				break;

			default:
				BusEventi.pubblica(new EventoErroreInterno("onEventoTestoDisponibile: Stato non gestito: " + stato));
		}
	}

	private void onEventoComandoDiGioco(EventoComandoDiGioco evento) {
		processaAzione(evento.getComando());
	}

	private void comandoNonValido(Comando comando) {
		BusEventi.pubblica(new EventoErroreInterno("Stato: " + stato + " - Comando non valido: " + comando));
	}

	/**
	 * Questa funzione in base allo stato del gruppo e alla azione ricevuta
	 * è il motore di gioco vero e proprio, ed è quindi abbastanza monumentale.
	 * Per alcuni stati l'azione ricevuta non serve a nulla, giusto per cambiare
	 * lo stato stesso.
	 */
	public void processaAzione(Comando azione) {
		BusEventi.pubblica(new EventoMessaggioInterno("Automa in stato " + stato.name() + "; processo azione " + azione));
		switch (stato) {

			case INTRO:
				switch(azione) {
					case PERGAMENA:
						stato = Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO;
						BusEventi.pubblica(new EventoStatoDiGioco(stato));
						break;
					case FLOPPY:
						stato = Stato.SELEZIONE_SALVATAGGIO_DA_LEGGERE;
						Collection<TestataSalvataggio> salvataggiDisponibili = GestoreSalvataggi.getSalvataggiDisponibili();
						BusEventi.pubblica(new EventoRichiestaSelezioneSlotPerRilettura(salvataggiDisponibili));
						break;
					default:
						comandoNonValido(azione);
				}
				break;

			case SELEZIONE_SALVATAGGIO_DA_LEGGERE:
				if (leggi(azione)) {
					stato = Stato.ATTESA_DIREZIONE;
					processaAzione(null);
				} else {
					stato = Stato.INTRO;
				}
				break;

			case PRE_GAME_ATTESA_SESSO_PERSONAGGIO:
				stato = Stato.PRE_GAME_ATTESA_CLASSE_PERSONAGGIO;
				if (azione == Comando.FEMMINA) {
					BusEventi.pubblica(new EventoStatoDiGioco(stato, Comando.GUERRIERA, Comando.LADRA,
							Comando.CANTASTORIE, Comando.ELFA, Comando.MAGA));
				} else {
					BusEventi.pubblica(new EventoStatoDiGioco(stato, Comando.GUERRIERO, Comando.LADRO,
							Comando.BARDO, Comando.ELFO, Comando.MAGO));
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
				inizializzaGioco();
				processaAzione(null);
				break;

			case INZIO_LOCAZIONE:
				controllaMissioni(Missione::controllaPreLocazione, OrdineVisita.PADRE_PRIMA);
				String evento = LineaTemporale.getEvento();
				if (evento != null) {
					BusEventi.pubblica(new EventoMessaggio(evento));
					if (LineaTemporale.isGiocoFinito()) {
						stato = Stato.GIOCO_PERSO;
						processaAzione(null);
						break;
					}
				}
				gruppoAvversario.reimposta();
				gruppo.getPersonaggiVivi().forEach(Personaggio::rimuoviTuttiGliEffettiDiStato);
				locazioneCorrente = Foresta.costruisciIstanza(gruppo.getCoordinate());
				gruppo.setLocazioneCorrente(locazioneCorrente);
				locazioneCorrente.crea(gruppo, gruppoAvversario);
				BusEventi.pubblica(new EventoPreparazioneLocazione());
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.GRAFICA));
				BusEventi.pubblica(new EventoParagrafo(LineaTemporale.getDescrizioneOraDelGiorno()));
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
				if (azione == Comando.INVENTARIO) {
					statoPrecedente = Stato.IN_LOCAZIONE;
					stato = Stato.INVENTARIO;
					richiediAperturaInventarioGruppo();
					processaAzione(null);
					return;
				}
				statoPrecedente = stato;
				/*
				 * Continuiamo a fornire all'automa a stati finiti della
				 * locazione la possibilità di andare avanti fino a
				 * LOCAZIONE_COMPLETA
				 */
				stato = locazioneCorrente.impostaAzioni(gruppo, gruppoAvversario, azione);
				if (stato != Stato.IN_LOCAZIONE) {
					if (stato == Stato.IN_COMBATTIMENTO) {
						temporizzatore.inizia(1_000);
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
					temporizzatore.inizia(1_000);
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
				List<Comando> comandiPossibili = new ArrayList<>();
				Personaggio formulante = gruppo.getFormulante();
				for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
					if (gruppo.getIncantesimi(classeIncantesimo) > 0 && formulante.getMagia() >= classeIncantesimo.getIstanza(formulante.getLivello()).getCostoLancio()) {
						comandiPossibili.add(classeIncantesimo.getComandoDiAttivazione());
					}
				}
				comandiPossibili.add(Comando.NO_INCANTESIMO);
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.INCANTESIMI));
				BusEventi.pubblica(new EventoComandiDisponibili(comandiPossibili));
				stato = Stato.INCANTESIMO_SCELTO;
				break;

			case ATTESA_INCANTESIMO_QUALSIASI:
				List<Comando> comandiPossibili2 = new ArrayList<>();
				for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
					comandiPossibili2.add(classeIncantesimo.getComandoDiAttivazione());
				}
				comandiPossibili2.add(Comando.NO_INCANTESIMO);
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.INCANTESIMI));
				BusEventi.pubblica(new EventoComandiDisponibili(comandiPossibili2));
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
				BusEventi.pubblica(new EventoRichiestaChiusuraFinestraCombattimento());
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));

				// Recuperiamo l'oggetto se fattibile
				if (locazioneCorrente.isCompleta()) {
					if (!locazioneCorrente.isHaStrettoAmicizia()) {
						Oggetto oggetto = locazioneCorrente.getOggetto();
						if (oggetto != null) {
							BusEventi.pubblica(new EventoMessaggioInterno("Tentativo di recupero oggetto utilizzando l'azione " + azione));
							if (!oggetto.prendi(gruppo, azione)) {
								BusEventi.pubblica(new EventoMessaggioInterno("L'oggetto non si lascia prendere con l'azione " + azione));
								statoPrecedente = Stato.FINE_LOCAZIONE;
								stato = Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
								processaAzione(null);
								break;
							} else {
								BusEventi.pubblica(new EventoRaccoltaOggetti());
								BusEventi.pubblica(new EventoMessaggioInterno("Oggetto raccolto."));
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

				Statistiche.incrementaTurniGiocati();

				stato = Stato.ATTESA_DIREZIONE;
				processaAzione(null);
				break;

			case ATTESA_DIREZIONE:
				BusEventi.pubblica(new EventoParagrafo(gruppo.chiMaiuscolo() + " se ne va. In quale direzione si incammina?"));
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.MAPPA));
				stato = Stato.ATTESA_PASSI;
				BusEventi.pubblica(new EventoComandiDisponibili(getComandiPossibiliInStatoAttesaDirezione()));
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
				case INVENTARIO:
					statoPrecedente = Stato.ATTESA_DIREZIONE;
					stato = Stato.INVENTARIO;
					richiediAperturaInventarioGruppo();
					processaAzione(null);
					return;
				case ACCAMPAMENTO:
					gruppo.pernotta(locazioneCorrente.getTipoRiposo());
					LineaTemporale.mattinoSeguente();
					LineaTemporale.eventi(gruppo);
					BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
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
				case RESURREZIONE:
					statoPrecedente = Stato.ATTESA_RESURREZIONE;
					stato = Stato.SCELTA_BERSAGLIO_RESURREZIONE;
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
				case AIUTO:
					for (Personaggio personaggio : gruppo.getPersonaggi()) {
						BusEventi.pubblica(new EventoParagrafo(personaggio.getDescrizione()));
					}
					BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
					stato = Stato.ATTESA_DIREZIONE;
					processaAzione(null);
					return;
				case FLOPPY:
					stato = Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE;
					BusEventi.pubblica(new EventoStatoDiGioco(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE,
							Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5,
							Comando.NO));
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
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
				processaAzione(null);
				break;

			case ATTESA_POZIONE_SALUTE_GRANDE:
				if (azione != null && azione != Comando.ANNULLA) {
					gruppo.consumaPozioneSaluteGrande(azione);
				}
				stato = Stato.ATTESA_DIREZIONE;
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
				processaAzione(null);
				break;

			case ATTESA_POZIONE_MAGIA:
				if (azione != null && azione != Comando.ANNULLA) {
					gruppo.consumaPozioneMagia(azione);
				}
				stato = Stato.ATTESA_DIREZIONE;
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
				processaAzione(null);
				break;

			case ATTESA_POZIONE_MAGIA_GRANDE:
				if (azione != null && azione != Comando.ANNULLA) {
					gruppo.consumaPozioneMagiaGrande(azione);
				}
				stato = Stato.ATTESA_DIREZIONE;
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
				processaAzione(null);
				break;

			case SCELTA_BERSAGLIO_RESURREZIONE:
				// Come nella scelta del bersaglio quando si formula un incantesimo in
				// combattimento (Stato.SCELTA_PERSONAGGIO_QUALSIASI): se c'è un solo
				// personaggio morto lo si risuscita direttamente, altrimenti si chiede quale.
				azione = scegliPersonaggioMorto();
				if (azione != null) {
					stato = statoPrecedente;
					processaAzione(azione);
				}
				break;

			case ATTESA_RESURREZIONE:
				if (azione != null && azione != Comando.ANNULLA) {
					eseguiResurrezione(gruppo.getPersonaggio(azione));
				}
				stato = Stato.ATTESA_DIREZIONE;
				BusEventi.pubblica(new EventoMostraFinestra(InterfacciaUtente.Finestra.STATO));
				processaAzione(null);
				break;

			case MAPPA:
				Logger.log("Stato MAPPA, azione " + azione);
				if (azione == null) {
					UI.impostaAzioni(Comando.SINISTRA, Comando.SU, Comando.GIU, Comando.DESTRA, Comando.SI);
					BusEventi.pubblica(new EventoParagrafo(gruppo.getCapo().getNome(
							Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
							Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " consulta la sua mappa della Foresta."));
					BusEventi.pubblica(new EventoRichiestaVisualizzazioneMappa());
				} else {
					switch (azione) {
					case SI:
						stato = statoPrecedente;
						BusEventi.pubblica(new EventoMostraSchermataGioco());
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

			case INVENTARIO:
				Logger.log("Stato INVENTARIO, azione " + azione);
				if (azione == null) {
					richiediAperturaInventarioGruppo();
				} else {
					switch (azione) {
						case ANNULLA:
							stato = statoPrecedente;
							BusEventi.pubblica(new EventoMostraSchermataGioco());
							processaAzione(null);
							break;
						case PERSONAGGIO_1:
						case PERSONAGGIO_2:
						case PERSONAGGIO_3:
						case PERSONAGGIO_4:
						case PERSONAGGIO_5:
							indicePersonaggioInventario = azione.ordinal() - Comando.PERSONAGGIO_1.ordinal();
							richiediAperturaInventarioGruppo();
							break;
						default:
							throw new IllegalArgumentException();
					}
				}
				break;

			case SELEZIONE_SALVATAGGIO_DA_SCRIVERE:
				if (azione == Comando.NO) {
					BusEventi.pubblica(new EventoMostraSchermataGioco());
					stato = Stato.ATTESA_DIREZIONE;
					processaAzione(null);
				}
				if (azione != null) {
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
					BusEventi.pubblica(new EventoMostraSchermataGioco());
					stato = Stato.ATTESA_DIREZIONE;
					processaAzione(null);
				}
				break;

			case GIOCO_PERSO:
				BusEventi.pubblica(new EventoRichiestaChiusuraFinestraCombattimento());
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
					BusEventi.pubblica(new EventoFineGioco(false));
					temporizzatore.inizia(5_000);
				} else if (azione == Comando.TIMER) {
					BusEventi.pubblica(new EventoFineGioco(false));
				} else {
					stato = Stato.STATISTICHE;
					processaAzione(null);
				}
				break;

			case GIOCO_VINTO:
				BusEventi.pubblica(new EventoRichiestaChiusuraFinestraCombattimento());
				if (azione == null) {
					UI.impostaAzioni(Comando.PERGAMENA);
				} else {
					stato = Stato.GIOCO_VINTO_2;
					processaAzione(null);
				}
				break;

			case GIOCO_VINTO_2:
				if (azione == null) {
					BusEventi.pubblica(new EventoFineGioco(true));
					temporizzatore.inizia(5_000);
					UI.impostaAzioni(Comando.PERGAMENA);
				} else if (azione == Comando.TIMER) {
					BusEventi.pubblica(new EventoFineGioco(true));
				} else {
					stato = Stato.STATISTICHE;
					processaAzione(null);
				}
				break;

			case STATISTICHE:
				if (azione == null) {
					BusEventi.pubblica(new EventoMostraStatistiche());
				} else if (azione == Comando.PERGAMENA) {
					if (GestorePunteggi.isPunteggioInClassifica(Statistiche.getPunti())) {
						stato = Stato.ATTESA_NOME_PUNTEGGI;
						BusEventi.pubblica(new EventoRichiestaTesto("congratulazioni! inserisci il tuo nome"));
					} else {
						stato = Stato.INTRO;
					}
				}
				UI.impostaAzioni(Comando.PERGAMENA);
				break;

			case ATTESA_NOME_PUNTEGGI:
				break;

            case PUNTEGGI:
				inizia();
				break;

			default:
				throw new IllegalStateException("Stato " + stato + " non correttamente gestito!");
		}
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
		BusEventi.pubblica(new EventoRichiestaReinizializzazioneUI());
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
		ComandiPossibili.reimposta();
		for (int indice : indiciMorti) {
			ComandiPossibili.add(Comando.ofPersonaggio(indice));
		}
		ComandiPossibili.add(Comando.ANNULLA);
		UI.impostaAzioni();
		return null;
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

	/**
	 * Se al termine di una locazione c'è almeno un personaggio morto, il gruppo possiede
	 * un incantesimo di Resurrezione e c'è chi ha la magia per formularlo, la Resurrezione
	 * diventa una delle azioni proponibili.
	 */
	private boolean isResurrezioneDisponibile() {
		if (gruppo.getIncantesimi(ClasseIncantesimo.RESURREZIONE) <= 0) {
			return false;
		}
		boolean cQualcunoMorto = gruppo.getPersonaggi().stream().anyMatch(p -> !p.isVivo());
		return cQualcunoMorto && trovaFormulanteResurrezione() != null;
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

	private boolean leggi(Comando azione) {
		if (azione == Comando.ANNULLA) {
			return false;
		}
		if (GestoreSalvataggi.leggi(azione)) {
			stato = Stato.ATTESA_DIREZIONE;
			BusEventi.pubblica(new EventoStatoDiGioco(stato, getComandiPossibiliInStatoAttesaDirezione()));
			BusEventi.pubblica(new EventoMostraSchermataGioco());
			return true;
		}
		//FIXME in questo caso che si fa?
		return false;
	}

	private void salva(Comando azione) {
		if (azione != Comando.ANNULLA) {
			GestoreSalvataggi.salva(azione);
		}
	}

	/**
	 * I comandi possibili quando il gioco si trova in stato Intro:
	 * inizio di una nuova partita o caricamento di una partita preesistente.
	 */
	private Collection<Comando> getComandiPossibiliInStatoIntro() {
		Collection<Comando> comandiPossibili = new ArrayList<>();
		comandiPossibili.add(Comando.PERGAMENA);
		if (!GestoreSalvataggi.getSalvataggiDisponibili().isEmpty()) {
			comandiPossibili.add(Comando.FLOPPY);
		}
		return comandiPossibili;
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

		BusEventi.pubblica(new EventoRichiestaAperturaInventarioGruppo(comandiPossibili,
				new AutomaInventario(personaggioScelto, gruppo)));
	}
}
