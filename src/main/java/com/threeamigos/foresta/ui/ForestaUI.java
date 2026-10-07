package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.*;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.FaseDiGioco;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.strumenti.Temporizzabile;
import com.threeamigos.foresta.strumenti.Temporizzatore;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.ui.sfx.TracciatoreLogo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ForestaUI implements Temporizzabile {

	private final Orientamento orientamento;
	private final boolean tuttoSchermo;
	private final boolean saltaLogoIniziale;
	private final Temporizzatore temporizzatore;

	// Spessore della fascia (o della colonna) della barra icone
	static final int SPESSORE_BARRA_ICONE = 72;

	private JFrame jframe;
	private Prompt prompt;
	private DisplayableCanvas displayableCanvas;
	private int larghezza;
	private int altezza;
	// Solo durante lo stato LOGO_INIZIALE; poi il posto passa al displayableCanvas
	private PannelloLogoIniziale pannelloLogoIniziale;
	private volatile boolean interfacciaCompleta;
	private boolean logoInizialeConcluso;
	private final boolean barraDock;
	private final GestorePunteggi gestorePunteggi;
	private final VistaPartita vistaPartita;
	private FaseDiGioco faseDiGioco;

	/**
	 * @param saltaLogoIniziale vero per non mostrare il logo iniziale (per esempio nelle partite di prova): si
	 *                          aspetta solo il caricamento delle risorse e si passa subito all'INTRO
	 * @param barraDock vero per la barra delle icone che si ingrandisce sotto il cursore, come il Dock di macOS
	 *                  (vedi DisplayableCanvasBarraIconeDock; solo in orientamento orizzontale)
	 * @param gestorePunteggi la classifica da mostrare nell'intro (la stessa che riceve l'Automa)
	 * @param vistaPartita lo stato della partita da mostrare, in sola lettura
	 */
	public ForestaUI(Orientamento orientamento, boolean tuttoSchermo, boolean saltaLogoIniziale, boolean barraDock,
					 GestorePunteggi gestorePunteggi, VistaPartita vistaPartita, Temporizzatore temporizzatore) {
		this.barraDock = barraDock;
		this.gestorePunteggi = gestorePunteggi;
		this.vistaPartita = vistaPartita;
		this.orientamento = orientamento;
		this.tuttoSchermo = tuttoSchermo;
		this.saltaLogoIniziale = saltaLogoIniziale;
		this.temporizzatore = temporizzatore;
		temporizzatore.setTemporizzabile(this);

		SwingUtilities.invokeLater(this::creaEMostraInterfacciaUtente);

		BusEventi.iscriviti(InternoCaricamentoCompletato.class, this::gestisciEventoCaricamentoCompletato);
		BusEventi.iscriviti(NotificaErroreCaricamento.class, this::gestisciEventoErroreCaricamento);
		BusEventi.iscriviti(NotificaGlobale.class, this::gestisciEventoNotificaGlobale);
		BusEventi.iscriviti(InternoTrofeoAcquisito.class, this::gestisciEventoTrofeoAcquisito);
		BusEventi.iscriviti(InternoAggiornamentoComandiDisponibili.class, this::gestisciEventoComandiDisponibili);
		BusEventi.iscriviti(NotificaConsumoPuntoAbilitaPersonaggio.class, this::gestisciEventoConsumoPuntoAbilita);
		BusEventi.iscriviti(NotificaFineGioco.class, this::gestisciEventoFineGioco);
		BusEventi.iscriviti(InternoNotificaViaFumettoATempo.class, this::gestisciEventoFumetto);
		BusEventi.iscriviti(NotificaInterazionePersonaggio.class, this::gestisciEventoInterazione);
		BusEventi.iscriviti(NotificaTestoFrase.class, this::gestisciEventoMessaggio);
		BusEventi.iscriviti(InternoMostraFinestraStato.class, evento -> mostraFinestra(TipoFinestra.STATO));
		BusEventi.iscriviti(InternoMostraFinestraStatistiche.class, evento -> mostraFinestra(TipoFinestra.STATISTICHE));
		BusEventi.iscriviti(InternoMostraFinestraIncantesimiEPozioni.class, evento -> mostraFinestra(TipoFinestra.INCANTESIMI_E_POZIONI));
		BusEventi.iscriviti(NotificaMostraPunteggiMigliori.class, this::gestisciEventoMostraPunteggi);
		BusEventi.iscriviti(InternoMostraSchermataGioco.class, this::gestisciEventoMostraSchermataGioco);
		BusEventi.iscriviti(NotificaMostraStatisticheFineGioco.class, this::gestisciEventoMostraStatistiche);
		BusEventi.iscriviti(NotificaTestoParagrafo.class, this::gestisciEventoParagrafo);
		BusEventi.iscriviti(NotificaPaginaIntermezzo.class, this::gestisciEventoPaginaIntermezzo);
		BusEventi.iscriviti(InternoFumettoSuccessivo.class, evento -> displayableCanvas.saltaNellaPaginaIntermezzo(evento.getSecondi()));
		BusEventi.iscriviti(InternoPreparazioneLocazione.class, this::gestisciEventoPreparazioneLocazione);
		BusEventi.iscriviti(InternoAssegnaCoordinateAPersonaggi.class, evento -> displayableCanvas.assegnaCoordinateAPersonaggi());
		BusEventi.iscriviti(InternoRichiestaAperturaFinestraCombattimento.class, this::gestisciEventoRichiestaAperturaFinestraCombattimento);
		BusEventi.iscriviti(RichiestaAperturaInventarioCommerciante.class, this::gestisciEventoRichiestaAperturaInventarioCommerciante);
		BusEventi.iscriviti(RichiestaAperturaIncantatore.class, this::gestisciEventoRichiestaAperturaIncantatore);
		BusEventi.iscriviti(RichiestaAperturaInventarioFornitore.class, this::gestisciEventoRichiestaAperturaInventarioFornitore);
		BusEventi.iscriviti(RichiestaAperturaInventarioGruppo.class, this::gestisciEventoRichiestaAperturaInventarioGruppo);
		BusEventi.iscriviti(RichiestaAperturaTrofei.class, this::gestisciEventoRichiestaAperturaTrofei);
		BusEventi.iscriviti(InternoRichiestaChiusuraFinestraCombattimento.class, this::gestisciEventoRichiestaChiusuraFinestraCombattimento);
		BusEventi.iscriviti(InternoSfidaCartaForbiciSasso.class, this::gestisciEventoSfidaCartaForbiciSasso);
		BusEventi.iscriviti(NotificaRaccoltaOggetti.class, this::gestisciEventoRaccoltaOggetti);
		BusEventi.iscriviti(RichiestaUscitaDalGioco.class, this::gestisciEventoRichiestaConfermaUscita);
		BusEventi.iscriviti(InternoRichiestaRefreshUI.class, this::gestisciEventoRichiestaRefreshUI);
		BusEventi.iscriviti(InternoRichiestaReinizializzazioneUI.class, this::gestisciEventoRichiestaReinizializzazioneUI);
		BusEventi.iscriviti(RichiestaTesto.class, this::gestisciEventoRichiestaTesto);
		BusEventi.iscriviti(RichiestaSelezioneSlotPerRilettura.class, this::gestisciEventoSelezioneSalvataggio);
		BusEventi.iscriviti(RichiestaSelezioneSlotPerSalvataggio.class,
				e -> displayableCanvas.selezioneSlotSalvataggioDaSalvare(e.getSalvataggiDisponibili()));
		BusEventi.iscriviti(RichiestaVisualizzazioneMappa.class, this::gestisciEventoRichiestaVisualizzazioneMappa);
		BusEventi.iscriviti(RichiestaSelezioneDirezione.class, this::gestisciEventoSelezioneDirezione);
		BusEventi.iscriviti(RichiestaSelezioneIncantesimoDaLanciare.class, this::gestisciEventoSelezioneIncantesimoDaLanciare);
		BusEventi.iscriviti(RichiestaSelezioneSiNo.class, this::gestisciEventoSelezioneSiNo);
		BusEventi.iscriviti(RichiestaSelezioneMissione.class, this::gestisciEventoSelezioneMissione);
		BusEventi.iscriviti(InternoFaseDiGioco.class, this::gestisciEventoFaseDiGioco);
		BusEventi.iscriviti(NotificaVariazioneEffettoDiStatoPersonaggio.class, this::gestisciEventoVariazioneEffettoDiStato);
		BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, this::gestisciEventoVariazioneStatoVitalePersonaggio);
	}
	
	/**
	 * Le dimensioni della finestra di gioco: lo schermo intero, oppure quelle date dalle
	 * cornici dei riquadri, senza superare lo schermo. Le legge dalle intestazioni dei file
	 * (DimensioniRisorsa), senza caricare le immagini: la finestra si apre subito, col logo iniziale.
	 */
	static Dimension calcolaDimensioniFinestra(Orientamento orientamento, boolean tuttoSchermo) {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		if (tuttoSchermo) {
			return new Dimension(screenDimension.width, screenDimension.height);
		}
		if (orientamento != Orientamento.ORIZZONTALE) {
			return new Dimension(Math.min(screenDimension.width, 400), Math.min(screenDimension.height, 640));
		}
		Dimension corniceMappa = DimensioniRisorsa.di(ImageCache.RISORSA_CORNICE_MAPPA);
		Dimension bosco = DimensioniRisorsa.di(ImageCache.RISORSA_BOSCO);
		Dimension corniceGrande = DimensioniRisorsa.di(ImageCache.RISORSA_CORNICE_GRANDE);
		Dimension corniceIncantesimi = DimensioniRisorsa.di(ImageCache.RISORSA_CORNICE_INCANTESIMI);
		int width = ImageCache.SPACING +
				corniceMappa.width +
				ImageCache.SPACING +
				bosco.width +
				ImageCache.SPACING +
				corniceGrande.width +
				ImageCache.SPACING;
		int height = ImageCache.SPACING +
				corniceGrande.height +
				ImageCache.SPACING +
				corniceIncantesimi.height +
				ImageCache.SPACING +
				corniceGrande.height +
				ImageCache.SPACING +
				ClasseIcona.getAltezzaMassima() +
				ImageCache.SPACING;
		return new Dimension(Math.min(width, screenDimension.width), Math.min(height, screenDimension.height));
	}

	static int orientamentoCanvas(Orientamento orientamento) {
		return orientamento == Orientamento.ORIZZONTALE
				? DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE
				: DisplayableCanvas.ORIENTAMENTO_VERTICALE;
	}

	/**
	 * Apre subito la finestra con il solo logo iniziale: prompt e DisplayableCanvas, e le immagini di cui hanno
	 * bisogno, arrivano durante l'animazione (vedi avviaLogoIniziale).
	 */
	private void creaEMostraInterfacciaUtente() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		jframe = new JFrame("La Foresta");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dimensioniFinestra = calcolaDimensioniFinestra(orientamento, tuttoSchermo);
		larghezza = dimensioniFinestra.width;
		altezza = dimensioniFinestra.height;

		jframe.setLayout(null);
		Container c = jframe.getContentPane();
		c.setBackground(Color.BLACK);
		c.setPreferredSize(new Dimension(larghezza, altezza));

		if (!saltaLogoIniziale) {
			pannelloLogoIniziale = new PannelloLogoIniziale(larghezza, altezza);
			jframe.add(pannelloLogoIniziale);
			pannelloLogoIniziale.setLocation(0, 0);
		}

		jframe.pack();
		jframe.setResizable(false);
		jframe.setLocation((screenDimension.width - jframe.getSize().width) / 2, (screenDimension.height - jframe.getSize().height) / 2);
		jframe.setVisible(true);

		BusEventi.pubblica(new InternoInterfacciaUtentePronta());
	}

	/**
	 * Stato LOGO_INIZIALE: fa partire l'animazione e, in un altro thread, il caricamento delle immagini; poi, di
	 * nuovo sull'EDT, costruisce prompt e DisplayableCanvas (vedi completaInterfacciaUtente).
	 */
	private void avviaLogoIniziale() {
		temporizzatore.inizia(TracciatoreLogo.INTERVALLO_FOTOGRAMMA_MS);
		Thread caricamento = new Thread(() -> {
			ImageCache.init();
			SwingUtilities.invokeLater(this::completaInterfacciaUtente);
		}, "Precaricamento immagini");
		caricamento.setDaemon(true);
		caricamento.start();
	}

	private void completaInterfacciaUtente() {
		prompt = new Prompt();
		prompt.setLocation((larghezza - prompt.getSize().width) / 2, (altezza - prompt.getSize().height) / 2);

		Logger.log("Orientamento: " + orientamento);
		displayableCanvas = new DisplayableCanvas(larghezza, altezza, orientamentoCanvas(orientamento), SPESSORE_BARRA_ICONE, barraDock,
				gestorePunteggi, vistaPartita);
		interfacciaCompleta = true;
	}

	/**
	 * Un fotogramma del logo iniziale. Quando l'animazione e' finita (subito, se il logo va saltato) e l'interfaccia
	 * e' completa, il logo lascia il posto al DisplayableCanvas e l'Automa viene avvisato.
	 */
	private void avanzaLogoIniziale() {
		if (logoInizialeConcluso) {
			return;
		}
		if (pannelloLogoIniziale != null) {
			pannelloLogoIniziale.avanza();
		}
		boolean animazioneFinita = pannelloLogoIniziale == null || pannelloLogoIniziale.isFinito();
		if (animazioneFinita && interfacciaCompleta) {
			logoInizialeConcluso = true;
			temporizzatore.termina();
			if (pannelloLogoIniziale != null) {
				jframe.remove(pannelloLogoIniziale);
				pannelloLogoIniziale = null;
			}
			// Nel layered pane, non nel content pane: da fratello del canvas la sua cornice
			// verrebbe coperta a ogni repaint() del canvas (es. al movimento del mouse), perché
			// il content pane presume che i figli non si sovrappongano e ridisegna solo il canvas.
			jframe.getLayeredPane().add(prompt, JLayeredPane.PALETTE_LAYER);
			jframe.add(displayableCanvas);
			displayableCanvas.setLocation(0, 0);
			jframe.revalidate();
			jframe.repaint();
			BusEventi.pubblica(new InternoFineLogoIniziale());
		}
	}

	public void tick() {
		if (faseDiGioco == FaseDiGioco.LOGO_INIZIALE) {
			avanzaLogoIniziale();
		} else if (faseDiGioco == FaseDiGioco.INTRO) {
			displayableCanvas.avanzaIntro();
		}
	}

	private void gestisciEventoRichiestaTesto(RichiestaTesto evento) {
		displayableCanvas.scriviGrande(evento.getRichiesta());
		prompt.mostra(evento.getTestoPredefinito());
	}

	public void impostaAzioni(Collection<Comando> possibilita) {
		ComandiPossibili.set(possibilita);
		displayableCanvas.impostaAzioniIcone();
	}

	private void gestisciEventoFumetto(InternoNotificaViaFumettoATempo evento) {
		displayableCanvas.notificaFumetto(evento.getTesto(), evento.getCoordinateFumetto());
	}

	private void gestisciEventoMessaggio(NotificaTestoFrase evento) {
		displayableCanvas.notifica(evento.getMessaggio());
	}

	private void mostraFinestra(TipoFinestra finestra) {
		displayableCanvas.primoPiano(finestra);
		rinfresca();
	}

	private void gestisciEventoMostraPunteggi(NotificaMostraPunteggiMigliori evento) {
		displayableCanvas.mostraPunteggi();
	}

	private void gestisciEventoMostraSchermataGioco(InternoMostraSchermataGioco evento) {
		displayableCanvas.iniziaGioco();
		displayableCanvas.primoPiano(TipoFinestra.GRAFICA);
	}

	private void gestisciEventoMostraStatistiche(NotificaMostraStatisticheFineGioco evento) {
		displayableCanvas.mostraStatistiche();
	}

	private void gestisciEventoCaricamentoCompletato(InternoCaricamentoCompletato evento) {
		displayableCanvas.ripristinaMessaggi(evento.getUltimiMessaggi());
	}

	private void gestisciEventoErroreCaricamento(NotificaErroreCaricamento evento) {
		displayableCanvas.notificaAnnuncioGlobale("Errore", "File caricamento corrotto");
	}

	private void gestisciEventoNotificaGlobale(NotificaGlobale evento) {
		displayableCanvas.notificaAnnuncioGlobale(evento.getEtichetta(), evento.getMessaggio());
	}

	private void gestisciEventoTrofeoAcquisito(InternoTrofeoAcquisito evento) {
		displayableCanvas.notificaAnnuncioGlobale("Trofeo vinto", evento.getTrofeo().getNome());
	}

	private void gestisciEventoPaginaIntermezzo(NotificaPaginaIntermezzo evento) {
		// A tutto schermo, testo e personaggi; il ritorno al gioco arriva con
		// InternoMostraSchermataGioco dopo l'ultima pagina
		displayableCanvas.mostraPaginaIntermezzo(evento.getPagina());
	}

	private void gestisciEventoParagrafo(NotificaTestoParagrafo evento) {
		displayableCanvas.notificaParagrafo(evento.getMessaggio());
	}

	private void gestisciEventoPreparazioneLocazione(InternoPreparazioneLocazione evento) {
		// La sfida a carta, forbici e sasso finita nella locazione prima resta visibile fino a qui
		displayableCanvas.getRiquadroSfida().setVisibile(false);
		displayableCanvas.preparaLocazione();
	}

	private void gestisciEventoRaccoltaOggetti(NotificaRaccoltaOggetti evento) {
		displayableCanvas.raccogliOggetto();
	}

	private void gestisciEventoRichiestaConfermaUscita(RichiestaUscitaDalGioco evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.confermaUscita();
	}

	private void gestisciEventoRichiestaAperturaInventarioCommerciante(RichiestaAperturaInventarioCommerciante evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaScambioCommerciante(evento.getNegozio(), evento.getScambio());
		displayableCanvas.commerciante();
	}

	private void gestisciEventoRichiestaAperturaIncantatore(RichiestaAperturaIncantatore evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaBancoIncantatore(evento.getBanco());
		displayableCanvas.incantatore(evento.getMessaggio());
	}

	private void gestisciEventoRichiestaAperturaInventarioFornitore(RichiestaAperturaInventarioFornitore evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaOfferteAlchimista(evento.getOfferte());
		displayableCanvas.alchimista(evento.getOroscopo());
	}

	private void gestisciEventoRichiestaAperturaInventarioGruppo(RichiestaAperturaInventarioGruppo evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaScambioInventario(evento.getScambio(), evento.getPersonaggio());
		displayableCanvas.inventario();
	}

	private void gestisciEventoRichiestaAperturaTrofei(RichiestaAperturaTrofei evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.trofei();
	}

	private void gestisciEventoSfidaCartaForbiciSasso(InternoSfidaCartaForbiciSasso evento) {
		displayableCanvas.getRiquadroSfida().aggiorna(evento.getMossaDelGiocatore(), evento.getMossaDellAvversario(),
				evento.getPunteggioDelGiocatore(), evento.getPunteggioDellAvversario());
		displayableCanvas.primoPiano(TipoFinestra.SFIDA);
		if (evento.isFinale()) {
			displayableCanvas.dissolviLaSfida();
		}
		rinfresca();
	}

	private void gestisciEventoRichiestaChiusuraFinestraCombattimento(InternoRichiestaChiusuraFinestraCombattimento evento) {
		displayableCanvas.primoPiano(TipoFinestra.STATO);
		displayableCanvas.getRiquadroCombattimento().setVisible(false);
	}

	private void gestisciEventoRichiestaRefreshUI(InternoRichiestaRefreshUI evento) {
		rinfresca();
	}

	private void gestisciEventoRichiestaReinizializzazioneUI(InternoRichiestaReinizializzazioneUI evento) {
		displayableCanvas.reinizializza();
		displayableCanvas.iniziaGioco();
		displayableCanvas.primoPiano(TipoFinestra.GRAFICA);
		rinfresca();
	}

	private void gestisciEventoSelezioneSalvataggio(RichiestaSelezioneSlotPerRilettura evento) {
		temporizzatore.termina();
		Collection<Comando> possibilita = new ArrayList<>();
		evento.getSalvataggiDisponibili().stream().map(TestataSalvataggio::getId).forEach(possibilita::add);
		possibilita.add(Comando.ANNULLA);
		impostaAzioni(possibilita);
		displayableCanvas.selezioneSlotSalvataggioDaCaricare(evento.getSalvataggiDisponibili());
	}

	private void gestisciEventoRichiestaAperturaFinestraCombattimento(InternoRichiestaAperturaFinestraCombattimento evento) {
		DisplayableCanvasRiquadroCombattimento infoCombattimento = displayableCanvas.getRiquadroCombattimento();
		infoCombattimento.setCombattente(evento.getPersonaggio());
		infoCombattimento.setAvversario(evento.getAvversario());
		infoCombattimento.setVisible(true);
		rinfresca();
	}

	private void gestisciEventoRichiestaVisualizzazioneMappa(RichiestaVisualizzazioneMappa evento) {
		displayableCanvas.mappa();
	}

	private void gestisciEventoSelezioneDirezione(RichiestaSelezioneDirezione evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(TipoFinestra.MAPPA);
	}

	private void gestisciEventoSelezioneIncantesimoDaLanciare(RichiestaSelezioneIncantesimoDaLanciare evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
	}

	private void gestisciEventoSelezioneSiNo(RichiestaSelezioneSiNo evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(TipoFinestra.STATO);
	}

	/**
	 * Domanda e opzioni sono nel riquadro del testo: lì va lo sguardo.
	 */
	private void gestisciEventoSelezioneMissione(RichiestaSelezioneMissione evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(TipoFinestra.TESTO);
	}

	private void gestisciEventoFaseDiGioco(InternoFaseDiGioco evento) {
		faseDiGioco = evento.getFase();
		switch (faseDiGioco) {
			case LOGO_INIZIALE:
				avviaLogoIniziale();
				break;

			case INTRO:
				// Richiama la schermata o animazione di introduzione
				displayableCanvas.avviaIntro();
				// La prima schermata (loghi o classifica) deve restare per un periodo intero
				temporizzatore.iniziaDopo(5_000);
				impostaAzioni(evento.getComandiPossibili());
				break;

			case NOME_PERSONAGGIO:
				temporizzatore.termina();
				displayableCanvas.scriviGrande("Scegli il nome del tuo personaggio o lascialo vuoto per un personaggio casuale.");
				prompt.setVisible(true);
				impostaAzioni(Collections.emptyList());
				break;

			case SESSO_PERSONAGGIO:
				displayableCanvas.scriviGrande("Scegli il sesso di " + prompt.getText());
				impostaAzioni(evento.getComandiPossibili());
				break;

			case CLASSE_PERSONAGGIO:
				displayableCanvas.scriviGrande("Scegli la classe di " + prompt.getText());
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

	private void gestisciEventoVariazioneStatoVitalePersonaggio(NotificaVariazioneStatoVitalePersonaggio evento) {
		displayableCanvas.notificaVariazioneStatoVitale(evento.getPersonaggio());
	}

	private void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		VistaPersonaggio personaggio = evento.getPersonaggio();
		if (!personaggio.isPNG()) {
			return;
		}
		TipoAttributo tipo = evento.getTipoAttributo();
		if (tipo == TipoAttributo.SALUTE) {
			displayableCanvas.variaSalute(personaggio, (int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		} else if (tipo == TipoAttributo.MAGIA) {
			displayableCanvas.variaMagia(personaggio, (int)(evento.getNuovoValore() - evento.getValorePrecedente()));
		}
	}

	private void gestisciEventoComandiDisponibili(InternoAggiornamentoComandiDisponibili evento) {
		ComandiPossibili.set(evento.getPossibilita());
		displayableCanvas.impostaAzioniIcone();
	}

	private void gestisciEventoConsumoPuntoAbilita(NotificaConsumoPuntoAbilitaPersonaggio evento) {
		TipoAttributo tipoAttributo = evento.getTipoAttributo();
		displayableCanvas.notificaAnnuncioGlobale("AUMENTO", tipoAttributo.getNome().toUpperCase());
	}

	private void gestisciEventoFineGioco(NotificaFineGioco evento) {
		if (evento.isCompletatoConSuccesso()) {
			displayableCanvas.vinto();
		} else {
			displayableCanvas.perso();
		}
	}

	private void gestisciEventoInterazione(NotificaInterazionePersonaggio evento) {
		VistaPersonaggio personaggio = evento.getPersonaggio();
		TipoInterazioneConEffettiDiStato tipoInterazione = evento.getTipoInterazione();
		switch (tipoInterazione) {
			case ELETTROCUZIONE:
			case CONGELAMENTO:
			case VAPORIZZAZIONE:
			case ALIMENTAZIONE_FIAMMA:
			case ESTINZIONE:
			case SCIOGLIMENTO_TERMICO:
			case ESPLOSIONE_DI_GAS:
			case FRANTUMAZIONE_DEL_GHIACCO:
			case DISGELO_VIOLENTO:
			case SUPERCONDUZIONE:
			case MIETITURA:
			case RIGETTO:
			case COLLASSO_ENTROPICO:
			case PURIFICAZIONE:
			case SIFONE_VITALE:
			case TOSSICITA_SETTICA:
			case COLPO_DI_GRAZIA:
			case SCHIACCIAMENTO:
			case INCIAMPO:
			case SOVRACCARICO_MENTALE:
			case SHOCK_DI_REALTA:
			case FOLLIA_COSMICA:
			case DISPERSIONE:
			case FANGO:
			case DISORIENTAMENTO:
			case RISONANZA_SIGILLATA:
			case ISOLAMENTO_SENSORIALE:
			case VAMPATA_TOSSICA:
			case REAZIONE_TOSSICA:
			case DILUIZIONE_EMATICA:
			case COAGULAZIONE_FORZATA:
			case INCENDIO_LIBERATORIO:
			case IMPATTO_RIGIDO:
				displayableCanvas.aggiungiInterazione(personaggio, tipoInterazione);
				break;
			default:
				throw new IllegalArgumentException("TipoInterazioneConEffettiDiStato non gestito: " + tipoInterazione);
		}
	}

	private void gestisciEventoVariazioneEffettoDiStato(NotificaVariazioneEffettoDiStatoPersonaggio evento) {
		VistaPersonaggio personaggio = evento.getPersonaggio();
		TipoEffettoDiStato tipoEffettoDiStato = evento.getEffetto();
		switch (evento.getTipo()) {
			case AGGIUNTA:
			case RINFORZO:
				displayableCanvas.aggiungiEffettoDiStato(personaggio, tipoEffettoDiStato, DoomdarkColorModel.Color.YELLOW);
				break;
			case DECADIMENTO:
				// Solo la durata scende di un turno: nessuno sprite, altrimenti si duplicherebbe
				// quello già mostrato per l'AGGIUNTA/RINFORZO nello stesso round.
				break;
			case RIMOZIONE:
				// L'effetto è terminato: sprite in grigio per segnalarlo.
				displayableCanvas.aggiungiEffettoDiStato(personaggio, tipoEffettoDiStato, DoomdarkColorModel.Color.MEDIUM_GRAY);
				break;
			default:
				throw new IllegalArgumentException("TipoVariazioneEffettoDiStato non gestito: " + evento.getTipo());
		}
	}

	private void rinfresca() {
		jframe.invalidate();
		jframe.repaint();
	}
}
