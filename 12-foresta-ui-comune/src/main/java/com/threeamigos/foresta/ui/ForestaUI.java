package com.threeamigos.foresta.ui;

import java.util.List;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.TestoDeiModificatori;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzabile;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;
import com.threeamigos.foresta.tipi.BersaglioDelSuggerimento;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.FaseDiGioco;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.StatoDelloSchermo;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.ui.barra.ComandiPossibili;
import com.threeamigos.foresta.ui.riquadri.RiquadroCombattimento;
import com.threeamigos.foresta.ui.sfx.GestoreNuvole;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;
import com.threeamigos.foresta.ui.testo.TestoReazioneDiClasse;
import com.threeamigos.foresta.ui.tutorial.SituazioniDellaUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * La classe radice dell'interfaccia, senza toolkit: si iscrive, subito, agli eventi del bus che la UI deve mostrare e traduce
 * ciascuno in una chiamata sullo schermo del gioco ({@link SchermoDelGioco}) o sul campo di testo ({@link CampoDiTesto}), e tiene il
 * temporizzatore della UI (il logo iniziale, l'intro). Il toolkit (<code>ForestaSwing</code>, <code>ForestaLibGDX</code>) apre la
 * finestra, mostra il logo iniziale mentre carica le risorse, costruisce schermo e campo di testo e li consegna con
 * {@link #interfacciaCompleta}; quando la finestra è aperta pubblica {@code InternoInterfacciaUtentePronta}, e da lì l'automa parte.
 * Vedi motore_grafico.md.
 */
public abstract class ForestaUI implements Temporizzabile {

	/** Quanto resta la prima schermata dell'intro (loghi o classifica) prima che l'intro vada avanti da sola */
	private static final int PRIMA_SCHERMATA_DELL_INTRO_MS = 5_000;

	// Da quanti personaggi nella locazione, sul telefono, si sta stretti e il tutorial spiega che la pila dei riquadri si sposta
	static final int PERSONAGGI_DI_UNA_LOCAZIONE_AFFOLLATA = 4;
	protected final VistaPartita vistaPartita;
	protected final Temporizzatore temporizzatore;
	// Arrivano quando il toolkit ha finito di costruirli (durante il logo iniziale)
	private SchermoDelGioco<?, ?, ?, ?> schermo;
	private CampoDiTesto campo;
	private FaseDiGioco faseDiGioco;
	// Quel che il tutorial deve sapere e che non si legge dalle icone (vedi aggiornaIlTutorial)
	private final SituazioniDellaUI situazioni = new SituazioniDellaUI();

	/**
	 * Si iscrive agli eventi: le iscrizioni devono esserci tutte prima che l'interfaccia, appena pronta, pubblichi eventi a cui il
	 * motore risponde.
	 *
	 * @param vistaPartita    lo stato della partita da mostrare, in sola lettura
	 * @param temporizzatore  il temporizzatore della UI: fa avanzare il logo iniziale e l'intro
	 */
	protected ForestaUI(VistaPartita vistaPartita, Temporizzatore temporizzatore) {
		this.vistaPartita = vistaPartita;
		this.temporizzatore = temporizzatore;
		temporizzatore.setTemporizzabile(this);

		BusEventi.iscriviti(InternoCaricamentoCompletato.class, e -> {
			// Caricata un'altra partita: le nuvole della mappa si rigenerano sulla sua area conosciuta
			GestoreNuvole.azzera();
			schermo.ripristinaMessaggi(e.getUltimiMessaggi());
		});
		// Un errore interno lo si dice al giocatore (oltre che nel log): senza, una transizione a metà lascerebbe il gioco fermo e muto
		SegnalatoreDegliErrori segnalatoreDegliErrori = new SegnalatoreDegliErrori(messaggio -> {
			if (schermo != null) {
				schermo.notifica(messaggio);
			}
		});
		BusEventi.iscriviti(InternoException.class, segnalatoreDegliErrori::segnala);
		BusEventi.iscriviti(NotificaErroreCaricamento.class,
				e -> schermo.notificaAnnuncioGlobale(TestiDellaUI.get("ERRORE"), TestiDellaUI.get("FILE_CORROTTO")));
		BusEventi.iscriviti(NotificaGlobale.class, e -> schermo.notificaAnnuncioGlobale(e.getEtichetta(), e.getMessaggio()));
		BusEventi.iscriviti(InternoTrofeoAcquisito.class,
				e -> schermo.notificaAnnuncioGlobale(TestiDellaUI.get("TROFEO_VINTO"), e.getTrofeo().getNome()));
		// Il nome della voce, che per chi ha due forme le dice tutte e due ("Maga e Mago")
		BusEventi.iscriviti(InternoVoceDelBestiarioSbloccata.class,
				e -> schermo.notificaAnnuncioGlobale(TestiDellaUI.get("NUOVA_VOCE"), e.getVoce().getNomeDellaVoce()));
		BusEventi.iscriviti(InternoAggiornamentoComandiDisponibili.class, e -> {
			ComandiPossibili.set(e.getPossibilita());
			schermo.impostaAzioniIcone();
			aggiornaIlTutorial();
		});
		// Si può spendere un punto dopo l'altro, anche su personaggi diversi: ogni annuncio sostituisce il precedente
		BusEventi.iscriviti(NotificaConsumoPuntoAbilitaPersonaggio.class,
				e -> schermo.notificaAnnuncioGlobaleSostituibile("AUMENTO", e.getTipoAttributo().getNome().toUpperCase(Locale.ROOT)));
		BusEventi.iscriviti(NotificaFineGioco.class, e -> {
			if (e.isCompletatoConSuccesso()) {
				schermo.vinto();
			} else {
				schermo.perso();
			}
		});
		BusEventi.iscriviti(InternoNotificaViaFumettoATempo.class, e -> schermo.notificaFumetto(e.getTesto(), e.getCoordinateFumetto()));
		BusEventi.iscriviti(NotificaInterazionePersonaggio.class, e -> {
			schermo.aggiungiInterazione(e.getPersonaggio(), e.getTipoInterazione());
			// La prima reazione elementale si spiega subito, nel mezzo dello scontro
			situazioni.segnaReazioneScatenata();
			aggiornaIlTutorial();
		});
		BusEventi.iscriviti(NotificaReazioneDiClassePersonaggio.class, e -> schermo.aggiungiTestoSulPersonaggio(e.getPersonaggio(),
				TestoReazioneDiClasse.di(e.getTipoDanno(), e.getMoltiplicatore()), TestoReazioneDiClasse.colore(e.getMoltiplicatore())));
		BusEventi.iscriviti(NotificaColpoMancato.class, e -> schermo.aggiungiColpoMancato(e.getPersonaggio()));
		BusEventi.iscriviti(NotificaTestoFrase.class, e -> schermo.notifica(e.getMessaggio()));
		BusEventi.iscriviti(NotificaMessaggioDelTutorial.class, e -> {
			if (e.getSuggerimento().getInnesco() != Situazione.MESSAGGIO_DEL_MOTORE) {
				// Un fatto che solo il motore conosce (il recupero fra le locazioni): diventa una situazione, e il suggerimento viene al suo turno,
				// nell'ordine dell'enum, invece di passare davanti a quelli già in attesa
				situazioni.segnaDalMotore(e.getSuggerimento().getInnesco());
				aggiornaIlTutorial();
				return;
			}
			// Il motore vuole che la cosa si noti: la finestra del tutorial, come i suggerimenti (e come loro tiene occupata la UI finché non si legge)
			if (schermo.getTutor().mostra(e.getSuggerimento(), e.getParametri()).isPresent()) {
				schermo.segnalaUiOccupata();
				rinfresca();
			}
		});
		BusEventi.iscriviti(InternoMostraFinestraStato.class, e -> mostraFinestra(TipoFinestra.STATO));
		BusEventi.iscriviti(InternoMostraFinestraStatistiche.class, e -> mostraFinestra(TipoFinestra.STATISTICHE));
		BusEventi.iscriviti(InternoMostraFinestraIncantesimiEPozioni.class, e -> mostraFinestra(TipoFinestra.INCANTESIMI_E_POZIONI));
		BusEventi.iscriviti(NotificaMostraPunteggiMigliori.class, e -> schermo.mostraPunteggi());
		BusEventi.iscriviti(InternoMostraSchermataGioco.class, e -> {
			schermo.iniziaGioco();
			schermo.primoPiano(TipoFinestra.GRAFICA);
			situazioni.segnaSchermataDiGioco();
			aggiornaIlTutorial();
		});
		BusEventi.iscriviti(NotificaMostraStatisticheFineGioco.class, e -> schermo.mostraStatistiche());
		BusEventi.iscriviti(NotificaTestoParagrafo.class, e -> schermo.notificaParagrafo(e.getMessaggio()));
		// A tutto schermo, testo e personaggi; il ritorno al gioco arriva con InternoMostraSchermataGioco dopo l'ultima pagina
		BusEventi.iscriviti(NotificaPaginaIntermezzo.class, e -> schermo.mostraPaginaIntermezzo(e.getPagina()));
		BusEventi.iscriviti(InternoFumettoSuccessivo.class, e -> schermo.saltaNellaPaginaIntermezzo(e.getSecondi()));
		BusEventi.iscriviti(InternoPreparazioneLocazione.class, e -> {
			// La sfida a carta, forbici e sasso finita nella locazione prima resta visibile fino a qui
			schermo.getRiquadroSfida().setVisibile(false);
			schermo.preparaLocazione();
			situazioni.segnaNuovaLocazione();
		});
		BusEventi.iscriviti(InternoAssegnaCoordinateAPersonaggi.class, e -> schermo.assegnaCoordinateAPersonaggi());
		BusEventi.iscriviti(InternoRichiestaAperturaFinestraCombattimento.class, e -> {
			RiquadroCombattimento combattimento = schermo.getRiquadroCombattimento();
			combattimento.setCombattente(e.getPersonaggio());
			combattimento.setAvversario(e.getAvversario());
			combattimento.setVisible(true);
			rinfresca();
			situazioni.segnaCombattimentoAperto();
			aggiornaIlTutorial();
		});
		BusEventi.iscriviti(InternoAzioneInCombattimento.class, this::gestisciEventoAzioneInCombattimento);
		// La tattica ha scelto l'azione (viene prima che si esegua): il riquadro la scrive sotto il personaggio
		BusEventi.iscriviti(InternoDecisioneTattica.class, e -> schermo.getRiquadroCombattimento().setAzione(e.getPersonaggio(),
				RiquadroCombattimento.testoDellAzione(e.getAzione(), e.getPersonaggio(), e.getBersaglio())));
		BusEventi.iscriviti(RichiestaAperturaInventarioCommerciante.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.impostaScambioCommerciante(e.getNegozio(), e.getScambio());
			schermo.commerciante();
		});
		BusEventi.iscriviti(RichiestaAperturaIncantatore.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.impostaBancoIncantatore(e.getBanco());
			schermo.incantatore(e.getMessaggio());
		});
		BusEventi.iscriviti(RichiestaAperturaInventarioFornitore.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.impostaOfferteAlchimista(e.getOfferte());
			schermo.alchimista();
		});
		BusEventi.iscriviti(RichiestaAperturaInventarioGruppo.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.impostaScambioInventario(e.getScambio(), e.getPersonaggio());
			schermo.inventario();
		});
		BusEventi.iscriviti(RichiestaAperturaTrofei.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.trofei();
		});
		BusEventi.iscriviti(RichiestaAperturaBestiario.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.bestiario();
		});
		BusEventi.iscriviti(RichiestaAperturaGrimorio.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.grimorio();
		});
		BusEventi.iscriviti(RichiestaAperturaTattiche.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.tattiche(e.getPersonaggio(), e.getTattica(), e.getTatticaPredefinita(), e.getAzioniPossibili());
		});
		// Se il fumetto del tutorial sta indicando il combattimento (una reazione elementale che ha ucciso l'ultimo avversario) il riquadro resta finché
		// non lo si chiude
		BusEventi.iscriviti(InternoRichiestaChiusuraFinestraCombattimento.class, e -> schermo.dopoIlSuggerimentoSu(BersaglioDelSuggerimento.RIQUADRO_DEL_COMBATTIMENTO, () -> {
			schermo.primoPiano(TipoFinestra.STATO);
			schermo.getRiquadroCombattimento().setVisible(false);
			situazioni.segnaCombattimentoChiuso();
			// Finito lo scontro: se qualcuno è caduto, è il momento di dire che si può riportare in vita
			aggiornaIlTutorial();
		}));
		BusEventi.iscriviti(InternoSfidaCartaForbiciSasso.class, e -> {
			schermo.getRiquadroSfida().aggiorna(e.getMossaDelGiocatore(), e.getMossaDellAvversario(), e.getPunteggioDelGiocatore(),
					e.getPunteggioDellAvversario(), e.isInizio());
			schermo.primoPiano(TipoFinestra.SFIDA);
			if (e.isFinale()) {
				schermo.dissolviLaSfida();
			}
			rinfresca();
		});
		BusEventi.iscriviti(NotificaRaccoltaOggetti.class, e -> {
			schermo.raccogliOggetto();
			situazioni.segnaOggettoTrovato();
			aggiornaIlTutorial();
		});
		BusEventi.iscriviti(RichiestaUscitaDalGioco.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.confermaUscita();
		});
		BusEventi.iscriviti(InternoRichiestaRefreshUI.class, e -> rinfresca());
		BusEventi.iscriviti(InternoRichiestaReinizializzazioneUI.class, e -> {
			// Si ricomincia: il fumetto aperto si chiude senza segnare nulla, e la schermata di gioco ricomparirà
			schermo.getTutor().chiudiEDimentica();
			situazioni.segnaFuoriDalGioco();
			schermo.reinizializza();
			schermo.iniziaGioco();
			schermo.primoPiano(TipoFinestra.GRAFICA);
			situazioni.segnaSchermataDiGioco();
			rinfresca();
		});
		BusEventi.iscriviti(RichiestaTesto.class, e -> {
			schermo.scriviGrande(e.getRichiesta());
			campo.mostra(e.getTestoPredefinito());
		});
		BusEventi.iscriviti(RichiestaSelezioneSlotPerRilettura.class, e -> {
			temporizzatore.termina();
			Collection<Comando> possibilita = new ArrayList<>();
			e.getSalvataggiDisponibili().stream().map(TestataSalvataggio::getId).forEach(possibilita::add);
			possibilita.add(Comando.ANNULLA);
			impostaAzioni(possibilita);
			schermo.selezioneSlotSalvataggioDaCaricare(e.getSalvataggiDisponibili());
		});
		BusEventi.iscriviti(RichiestaSelezioneSlotPerSalvataggio.class, e -> schermo.selezioneSlotSalvataggioDaSalvare(e.getSalvataggiDisponibili()));
		BusEventi.iscriviti(RichiestaVisualizzazioneMappa.class, e -> schermo.mappa());
		BusEventi.iscriviti(RichiestaSelezioneDirezione.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.primoPiano(TipoFinestra.MAPPA);
		});
		BusEventi.iscriviti(RichiestaSelezioneIncantesimoDaLanciare.class, e -> {
			ComandiPossibili.set(e.getPossibilita(), e.getComandiVisibili(), e.getInRicarica());
			schermo.impostaAzioniIcone();
			schermo.primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		});
		BusEventi.iscriviti(RichiestaSelezioneSiNo.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.primoPiano(TipoFinestra.STATO);
		});
		// Domanda e opzioni sono nel riquadro del testo: lì va lo sguardo
		BusEventi.iscriviti(RichiestaSelezioneMissione.class, e -> {
			impostaAzioni(e.getPossibilita());
			schermo.primoPiano(TipoFinestra.TESTO);
		});
		BusEventi.iscriviti(InternoFaseDiGioco.class, this::gestisciEventoFaseDiGioco);
		// Scelta dalle opzioni: la barra si sposta subito
		BusEventi.iscriviti(com.threeamigos.foresta.eventi.notifiche.NotificaPosizioneDellaBarra.class, e -> cambiaPosizioneDellaBarra(e.getPosizione()));
		BusEventi.iscriviti(NotificaVariazioneEffettoDiStatoPersonaggio.class, this::gestisciEventoVariazioneEffettoDiStato);
		BusEventi.iscriviti(NotificaVariazioneModificatorePersonaggio.class, this::gestisciEventoVariazioneModificatore);
		BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, e -> schermo.notificaVariazioneStatoVitale(e.getPersonaggio()));
	}

	/**
	 * Il toolkit ha costruito lo schermo e il campo di testo: da qui gli eventi li raggiungono. Il campo serve anche alla schermata
	 * delle tattiche, che chiede il nome di una voce.
	 */
	protected final void interfacciaCompleta(SchermoDelGioco<?, ?, ?, ?> schermo, CampoDiTesto campo) {
		this.schermo = schermo;
		this.campo = campo;
		schermo.setRichiestaTesto(campo::mostra, campo::annulla);
		schermo.setRicontrollaIlTutorial(this::aggiornaIlTutorial);
	}

	/**
	 * Il giocatore ha scelto dove sta la barra dei comandi (dalla schermata delle opzioni, quindi senza una partita in corso): il toolkit rifà lo
	 * schermo con la barra lì. Di base niente: lo fanno i toolkit che lo sanno fare.
	 */
	protected void cambiaPosizioneDellaBarra(PosizioneDellaBarra posizione) {
	}

	/**
	 * Se su questo schermo la barra può stare in basso: non sul telefono, né su un tablet troppo basso. Di base sì.
	 */
	protected boolean barraInBassoPossibile() {
		return true;
	}

	protected SchermoDelGioco<?, ?, ?, ?> getSchermo() {
		return schermo;
	}

	protected FaseDiGioco getFaseDiGioco() {
		return faseDiGioco;
	}

	/**
	 * Fase LOGO_INIZIALE: il toolkit fa partire l'animazione del logo (con il temporizzatore, se gli serve) e intanto carica le
	 * risorse e costruisce l'interfaccia.
	 */
	protected abstract void avviaLogoIniziale();

	/**
	 * Un fotogramma del logo iniziale, a ogni impulso del temporizzatore durante la fase LOGO_INIZIALE: quando l'animazione è finita
	 * e l'interfaccia è completa il toolkit lascia il posto allo schermo e pubblica {@code InternoFineLogoIniziale}.
	 */
	protected abstract void avanzaLogoIniziale();

	/**
	 * Ridisegna subito, per dare un riscontro immediato (all'apertura e alla chiusura del riquadro del combattimento, per esempio).
	 */
	protected void rinfresca() {
		if (schermo != null) {
			schermo.ridisegna();
		}
	}

	/**
	 * Un impulso del temporizzatore: durante il logo iniziale fa avanzare il logo, durante l'intro le sue schermate.
	 */
	@Override
	public void tick() {
		if (faseDiGioco == FaseDiGioco.LOGO_INIZIALE) {
			avanzaLogoIniziale();
		} else if (faseDiGioco == FaseDiGioco.INTRO) {
			schermo.avanzaIntro();
		}
	}

	/**
	 * Mostra nella barra delle icone i comandi che il giocatore può dare.
	 */
	protected void impostaAzioni(Collection<Comando> possibilita) {
		ComandiPossibili.set(possibilita);
		schermo.impostaAzioniIcone();
		aggiornaIlTutorial();
	}

	/**
	 * Dice al tutorial che cosa si vede adesso (le icone, il combattimento, un oggetto trovato...) e, se c'è un suggerimento da dare, lo apre. Si
	 * apre solo sulla schermata di gioco, non durante un intermezzo, una mappa a tutto schermo o una scelta: lì il giocatore ha altro da guardare.
	 */
	private void aggiornaIlTutorial() {
		if (schermo == null) {
			return;
		}
		boolean missioneAttiva = !vistaPartita.getMissioniAttive().isEmpty();
		StatoDelloSchermo schermata = schermo.getStato();
		situazioni.segnaPuntiAbilita(isQualcunoConPuntiAbilita());
		situazioni.segnaQualcunoCaduto(isQualcunoCaduto());
		situazioni.segnaMovimentoSpiegato(RegistroSuggerimenti.isVisto(TipoSuggerimento.MOVIMENTO_E_MAPPA));
		situazioni.segnaOggettoSpiegato(RegistroSuggerimenti.isVisto(TipoSuggerimento.OGGETTO_TROVATO));
		situazioni.segnaLocazioneAffollata(schermo.getPilaScorrevole() != null && personaggiInLocazione() >= PERSONAGGI_DI_UNA_LOCAZIONE_AFFOLLATA);
		boolean laMappa = schermata == StatoDelloSchermo.MAPPA;
		situazioni.segnaLaSchermata(schermata, laMappa && schermo.mappaConJoystick(), laMappa && schermo.mappaPiuGrandeDelloSchermo());
		boolean aperto = schermo.getTutor().aggiorna(situazioni.correnti(ComandiPossibili.getComandi(), missioneAttiva, schermo.getTutor().isDito(),
				isQualcunoFeritoConUnaPozione()), schermo::isApribile).isPresent();
		if (aperto) {
			// Finché il fumetto è aperto il motore aspetta (vedi SchermoDelGioco.fineFotogramma, che avvisa quando si chiude)
			schermo.segnalaUiOccupata();
		}
		rinfresca();
	}

	private void mostraFinestra(TipoFinestra finestra) {
		// Sul telefono le statistiche non sono nella pila: il motore chiede lo stesso di mostrarle (bottino, città), e lì non c'è niente da portare davanti
		if (!schermo.getPila().contains(finestra)) {
			return;
		}
		schermo.primoPiano(finestra);
		rinfresca();
	}

	/**
	 * Se qualcuno del gruppo è ferito e c'è una pozione di salute (piccola o grande) da dargli: letto dalla vista della partita, non da un evento.
	 */
	/**
	 * Se è caduto un compagno del capo (anche chi si è unito per un po', come un goblin corrotto), con il capo ancora vivo: letto dalla vista della partita.
	 */
	private boolean isQualcunoCaduto() {
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		if (gruppo == null) {
			return false;
		}
		// Il suggerimento della Resurrezione vale solo per i compagni: se cade il capo la partita è persa, e lui non si può riportare in vita
		VistaPersonaggio capo = gruppo.getCapo();
		if (capo != null && !capo.isVivo()) {
			return false;
		}
		for (VistaPersonaggio personaggio : gruppo.getPersonaggi()) {
			if (personaggio != capo && !personaggio.isVivo()) {
				return true;
			}
		}
		return false;
	}

	private boolean isQualcunoConPuntiAbilita() {
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		if (gruppo == null) {
			return false;
		}
		for (VistaPersonaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (personaggio.getPuntiAbilitaDisponibili() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Quanti personaggi si vedono nella locazione: il gruppo e i suoi ospiti vivi, e gli avversari vivi
	 */
	private int personaggiInLocazione() {
		int quanti = 0;
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		if (gruppo != null) {
			quanti += gruppo.getPersonaggiVivi().size();
			for (VistaPersonaggio ospite : gruppo.getOspiti()) {
				quanti += ospite.isVivo() ? 1 : 0;
			}
		}
		VistaGruppo avversari = vistaPartita.getGruppoAvversario();
		if (avversari != null) {
			quanti += avversari.getPersonaggiVivi().size();
		}
		return quanti;
	}

	private boolean isQualcunoFeritoConUnaPozione() {
		VistaGruppoGiocatore gruppo = vistaPartita.getGruppoGiocatore();
		if (gruppo == null || gruppo.getPozioniSalute() + gruppo.getPozioniSaluteGrande() <= 0) {
			return false;
		}
		for (VistaPersonaggio personaggio : gruppo.getPersonaggiVivi()) {
			// Il suggerimento sulle pozioni quando serve davvero: sotto metà della salute (una graffiatura non vale una pozione)
			if (2 * personaggio.getSalute() < personaggio.getSaluteMassima()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Chi sta agendo e, se c'è, il bersaglio: sono dell'uno o dell'altro gruppo, a turno
	 */
	private void gestisciEventoAzioneInCombattimento(InternoAzioneInCombattimento evento) {
		RiquadroCombattimento combattimento = schermo.getRiquadroCombattimento();
		combattimento.setCombattente(evento.getPersonaggio());
		combattimento.setAvversario(evento.getBersaglio());
		if (!combattimento.haAzionePer(evento.getPersonaggio())) {
			// Fuori dalla tattica: l'azione non ha una decisione, si scrive che attacca il bersaglio (se c'è)
			combattimento.setAzione(evento.getPersonaggio(), evento.getAzione() != null
					? RiquadroCombattimento.testoDellAzione(evento.getAzione(), evento.getPersonaggio(), evento.getBersaglio())
					: RiquadroCombattimento.azionePredefinita(evento.getBersaglio()));
		}
		boolean primaVolta = !combattimento.isVisibile();
		combattimento.setVisible(true);
		rinfresca();
		if (primaVolta) {
			// Anche un attacco singolo apre il riquadro: il tutorial dello scontro punta lì
			situazioni.segnaCombattimentoAperto();
			aggiornaIlTutorial();
		}
	}

	private void gestisciEventoFaseDiGioco(InternoFaseDiGioco evento) {
		faseDiGioco = evento.getFase();
		// La scelta della classe ha il suo suggerimento (vedi tutorial.md §4 quater)
		situazioni.segnaSceltaDellaClasse(faseDiGioco == FaseDiGioco.CLASSE_PERSONAGGIO);
		switch (faseDiGioco) {
			case LOGO_INIZIALE:
				avviaLogoIniziale();
				break;

			case INTRO:
				// Richiama la schermata o animazione di introduzione
				schermo.getTutor().chiudiEDimentica();
				situazioni.segnaFuoriDalGioco();
				schermo.avviaIntro();
				// La prima schermata (loghi o classifica) deve restare per un periodo intero
				temporizzatore.iniziaDopo(PRIMA_SCHERMATA_DELL_INTRO_MS);
				impostaAzioni(evento.getComandiPossibili());
				break;

			case OPZIONI:
				// Dall'intro: l'intro si ferma (riparte quando si torna), e la schermata resta mentre nella barra cambiano le scelte. La barra in basso
				// non si offre dove non c'è posto (sul telefono, sui tablet più bassi)
				temporizzatore.termina();
				List<Comando> comandi = new ArrayList<>(evento.getComandiPossibili());
				if (!barraInBassoPossibile()) {
					comandi.remove(Comando.BARRA_BASSO);
				}
				schermo.opzioni(comandi);
				impostaAzioni(comandi);
				break;

			case NOME_PERSONAGGIO:
				// Comincia una partita nuova: le nuvole della mappa si rigenerano sulla sua area conosciuta
				GestoreNuvole.azzera();
				temporizzatore.termina();
				schermo.scriviGrande(TestiDellaUI.get("SCEGLI_NOME"));
				campo.mostraVuoto();
				impostaAzioni(Collections.emptyList());
				break;

			case SESSO_PERSONAGGIO:
				schermo.scriviGrande(TestiDellaUI.get("SCEGLI_SESSO", campo.getTesto()));
				impostaAzioni(evento.getComandiPossibili());
				break;

			case CLASSE_PERSONAGGIO:
				// Il bestiario dei personaggi giocabili: passando sopra le icone delle classi si legge la scheda
				schermo.sceltaDellaClasse(TestiDellaUI.get("SCEGLI_CLASSE", campo.getTesto()));
				impostaAzioni(evento.getComandiPossibili());
				break;

			case SALVATAGGIO_DA_SCRIVERE:
				// La schermata è già aperta, con le testate, da RichiestaSelezioneSlotPerSalvataggio
				impostaAzioni(evento.getComandiPossibili());
				break;

			default:
				throw new IllegalArgumentException("Fase di gioco non ancora gestita: " + evento.getFase());
		}
	}

	private void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		// Anche i personaggi del gruppo, che si vedono in locazione: lo schermo porta in primo piano il loro riquadro
		VistaPersonaggio personaggio = evento.getPersonaggio();
		TipoAttributo tipo = evento.getTipoAttributo();
		if (tipo == TipoAttributo.SALUTE) {
			schermo.variaSalute(personaggio, (int) (evento.getNuovoValore() - evento.getValorePrecedente()));
			// Chi si ferisce può far scattare il suggerimento sulle pozioni
			aggiornaIlTutorial();
		} else if (tipo == TipoAttributo.MAGIA) {
			schermo.variaMagia(personaggio, (int) (evento.getNuovoValore() - evento.getValorePrecedente()));
		}
	}

	private void gestisciEventoVariazioneEffettoDiStato(NotificaVariazioneEffettoDiStatoPersonaggio evento) {
		switch (evento.getTipo()) {
			case AGGIUNTA:
			case RINFORZO:
				schermo.aggiungiEffettoDiStato(evento.getPersonaggio(), evento.getEffetto(), DoomdarkColorModel.Color.YELLOW);
				// Il primo effetto di stato si spiega subito, nel mezzo dello scontro
				situazioni.segnaEffettoDiStatoApplicato();
				aggiornaIlTutorial();
				break;
			case DECADIMENTO:
				// Solo la durata scende di un turno: nessuno sprite, altrimenti si duplicherebbe quello già mostrato per
				// l'AGGIUNTA/RINFORZO nello stesso round
				break;
			case RIMOZIONE:
				// L'effetto è terminato: sprite in grigio per segnalarlo
				schermo.aggiungiEffettoDiStato(evento.getPersonaggio(), evento.getEffetto(), DoomdarkColorModel.Color.MEDIUM_GRAY);
				break;
			default:
				throw new IllegalArgumentException("TipoVariazioneEffettoDiStato non gestito: " + evento.getTipo());
		}
	}

	/**
	 * Come per gli effetti di stato, uno sprite sul personaggio con la descrizione ("Furtività +5", "Intelligenza a 0"): giallo se
	 * porta l'attributo a un valore, verde se lo alza, rosso se lo abbassa, grigio se l'effetto è finito. Solo per i modificatori a
	 * tempo: quelli permanenti hanno già i loro messaggi.
	 */
	private void gestisciEventoVariazioneModificatore(NotificaVariazioneModificatorePersonaggio evento) {
		ModificatoreAttributoMD modificatore = evento.getModificatore();
		if (!modificatore.isTemporaneo()) {
			return;
		}
		DoomdarkColorModel.Color colore;
		switch (evento.getTipo()) {
			case AGGIUNTA:
				colore = coloreModificatore(modificatore);
				break;
			case MODIFICA:
				// Solo se la durata si allunga (un doppione la rinfresca); se scende di un turno nessuno sprite
				if (evento.getNuovaDurata() <= evento.getDurataPrecedente()) {
					return;
				}
				colore = coloreModificatore(modificatore);
				break;
			case RIMOZIONE:
				colore = DoomdarkColorModel.Color.MEDIUM_GRAY;
				break;
			default:
				throw new IllegalArgumentException("TipoVariazioneModificatore non gestito: " + evento.getTipo());
		}
		schermo.aggiungiModificatore(evento.getPersonaggio(), TestoDeiModificatori.breve(modificatore), colore);
	}

	static DoomdarkColorModel.Color coloreModificatore(ModificatoreAttributoMD modificatore) {
		if (modificatore.getTipoModificatoreAttributo() == TipoModificatore.QUANTITA_ASSOLUTA) {
			return DoomdarkColorModel.Color.YELLOW;
		}
		return modificatore.getQuantita() >= 0 ? DoomdarkColorModel.Color.GREEN : DoomdarkColorModel.Color.RED;
	}
}
