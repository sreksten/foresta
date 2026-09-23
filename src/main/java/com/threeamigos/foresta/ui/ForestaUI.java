package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioFornitore;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioGruppo;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVisualizzazioneMappa;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Temporizzatore;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ForestaUI implements InterfacciaUtente, Temporizzabile {

	private final Orientamento orientamento;
	private final boolean tuttoSchermo;
	private final Temporizzatore temporizzatore;

	// Spessore della fascia (o della colonna) della barra icone
	static final int SPESSORE_BARRA_ICONE = 72;

	private JFrame jframe;
	private Prompt prompt;
	private DisplayableCanvas displayableCanvas;
	private Stato statoDiGioco;

	public ForestaUI(Orientamento orientamento, boolean tuttoSchermo, Temporizzatore temporizzatore) {
		this.orientamento = orientamento;
		this.tuttoSchermo = tuttoSchermo;
		this.temporizzatore = temporizzatore;
		temporizzatore.setTemporizzabile(this);

		SwingUtilities.invokeLater(this::creaEMostraInterfacciaUtente);

		BusEventi.iscriviti(InternoCaricamentoCompletato.class, this::gestisciEventoCaricamentoCompletato);
		BusEventi.iscriviti(NotificaErroreCaricamento.class, this::gestisciEventoErroreCaricamento);
		BusEventi.iscriviti(NotificaGlobale.class, this::gestisciEventoNotificaGlobale);
		BusEventi.iscriviti(InternoAggiornamentoComandiDisponibili.class, this::gestisciEventoComandiDisponibili);
		BusEventi.iscriviti(NotificaConsumoPuntoAbilitaPersonaggio.class, this::gestisciEventoConsumoPuntoAbilita);
		BusEventi.iscriviti(NotificaFineGioco.class, this::gestisciEventoFineGioco);
		BusEventi.iscriviti(InternoNotificaViaFumettoATempo.class, this::gestisciEventoFumetto);
		BusEventi.iscriviti(NotificaInterazioneElementalePersonaggio.class, this::gestisciEventoInterazioneElementale);
		BusEventi.iscriviti(NotificaTestoFrase.class, this::gestisciEventoMessaggio);
		BusEventi.iscriviti(InternoPortaInPrimoPiano.class, this::gestisciEventoMostraFinestra);
		BusEventi.iscriviti(NotificaMostraPunteggiMigliori.class, this::gestisciEventoMostraPunteggi);
		BusEventi.iscriviti(InternoMostraSchermataGioco.class, this::gestisciEventoMostraSchermataGioco);
		BusEventi.iscriviti(NotificaMostraStatisticheFineGioco.class, this::gestisciEventoMostraStatistiche);
		BusEventi.iscriviti(NotificaTestoParagrafo.class, this::gestisciEventoParagrafo);
		BusEventi.iscriviti(NotificaPaginaIntermezzo.class, this::gestisciEventoPaginaIntermezzo);
		BusEventi.iscriviti(InternoPreparazioneLocazione.class, this::gestisciEventoPreparazioneLocazione);
		BusEventi.iscriviti(InternoRichiestaAperturaFinestraCombattimento.class, this::gestisciEventoRichiestaAperturaFinestraCombattimento);
		BusEventi.iscriviti(ComandoAperturaInventarioCommerciante.class, this::gestisciEventoRichiestaAperturaInventarioCommerciante);
		BusEventi.iscriviti(ComandoAperturaInventarioFornitore.class, this::gestisciEventoRichiestaAperturaInventarioFornitore);
		BusEventi.iscriviti(ComandoAperturaInventarioGruppo.class, this::gestisciEventoRichiestaAperturaInventarioGruppo);
		BusEventi.iscriviti(InternoRichiestaChiusuraFinestraCombattimento.class, this::gestisciEventoRichiestaChiusuraFinestraCombattimento);
		BusEventi.iscriviti(NotificaRaccoltaOggetti.class, this::gestisciEventoRaccoltaOggetti);
		BusEventi.iscriviti(RichiestaUscitaDalGioco.class, this::gestisciEventoRichiestaConfermaUscita);
		BusEventi.iscriviti(InternoRichiestaRefreshUI.class, this::gestisciEventoRichiestaRefreshUI);
		BusEventi.iscriviti(InternoRichiestaReinizializzazioneUI.class, this::gestisciEventoRichiestaReinizializzazioneUI);
		BusEventi.iscriviti(RichiestaTesto.class, this::gestisciEventoRichiestaTesto);
		BusEventi.iscriviti(RichiestaSelezioneSlotPerRilettura.class, this::gestisciEventoSelezioneSalvataggio);
		BusEventi.iscriviti(ComandoVisualizzazioneMappa.class, this::gestisciEventoRichiestaVisualizzazioneMappa);
		BusEventi.iscriviti(RichiestaSelezioneDirezione.class, this::gestisciEventoSelezioneDirezione);
		BusEventi.iscriviti(RichiestaSelezioneIncantesimoDaLanciare.class, this::gestisciEventoSelezioneIncantesimoDaLanciare);
		BusEventi.iscriviti(RichiestaSelezioneSiNo.class, this::gestisciEventoSelezioneSiNo);
		BusEventi.iscriviti(InternoStatoDiGioco.class, this::gestisciEventoStatoDiGioco);
		BusEventi.iscriviti(NotificaVariazioneEffettoDiStatoPersonaggio.class, this::gestisciEventoVariazioneEffettoDiStato);
		BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, this::gestisciEventoVariazioneStatoVitalePersonaggio);
	}
	
	/**
	 * Le dimensioni della finestra di gioco: lo schermo intero, oppure quelle date dalle
	 * cornici dei riquadri, senza superare lo schermo. Richiede ImageCache già inizializzata.
	 */
	static Dimension calcolaDimensioniFinestra(Orientamento orientamento, boolean tuttoSchermo) {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		if (tuttoSchermo) {
			return new Dimension(screenDimension.width, screenDimension.height);
		}
		if (orientamento != Orientamento.ORIZZONTALE) {
			return new Dimension(Math.min(screenDimension.width, 400), Math.min(screenDimension.height, 640));
		}
		int width = ImageCache.SPACING +
				ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING +
				ImageCache.locazioni.get(ClassiLocazione.BOSCO).getWidth() +
				ImageCache.SPACING +
				ImageCache.corniceGrande.getWidth() +
				ImageCache.SPACING;
		int height = ImageCache.SPACING +
				ImageCache.corniceGrande.getHeight() +
				ImageCache.SPACING +
				ImageCache.corniceIncantesimi.getHeight() +
				ImageCache.SPACING +
				ImageCache.corniceGrande.getHeight() +
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

	private void creaEMostraInterfacciaUtente() {
		ImageCache.init();

		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		jframe = new JFrame("La Foresta");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension dimensioniFinestra = calcolaDimensioniFinestra(orientamento, tuttoSchermo);
		int width = dimensioniFinestra.width;
		int height = dimensioniFinestra.height;

		jframe.setLayout(null);
		Container c = jframe.getContentPane();
		c.setBackground(Color.BLACK);
		c.setPreferredSize(new Dimension(width, height));

		prompt = new Prompt();
		jframe.add(prompt);
		prompt.setLocation((width - prompt.getSize().width) / 2, (height - prompt.getSize().height) / 2);

		Logger.log("Orientamento: " + orientamento);
		displayableCanvas = new DisplayableCanvas(width, height, orientamentoCanvas(orientamento), SPESSORE_BARRA_ICONE);
		jframe.add(displayableCanvas);
		displayableCanvas.setLocation(0, 0);

		jframe.pack();
		jframe.setResizable(false);		
		jframe.setLocation((screenDimension.width - jframe.getSize().width) / 2, (screenDimension.height - jframe.getSize().height) / 2);
		jframe.setVisible(true);

		BusEventi.pubblica(new InternoInterfacciaUtentePronta());
	}

	public void tick() {
		if (statoDiGioco == Stato.INTRO) {
			displayableCanvas.avanzaIntro();
		}
	}

	private void gestisciEventoRichiestaTesto(RichiestaTesto evento) {
		displayableCanvas.scriviGrande(evento.getRichiesta());
		prompt.setVisible(true);
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

	private void gestisciEventoMostraFinestra(InternoPortaInPrimoPiano evento) {
		for (InterfacciaUtente.Finestra finestra : evento.getFinestre()) {
			displayableCanvas.primoPiano(finestra);
		}
		rinfresca();
	}

	private void gestisciEventoMostraPunteggi(NotificaMostraPunteggiMigliori evento) {
		displayableCanvas.mostraPunteggi();
	}

	private void gestisciEventoMostraSchermataGioco(InternoMostraSchermataGioco evento) {
		displayableCanvas.iniziaGioco();
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
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

	private void gestisciEventoPaginaIntermezzo(NotificaPaginaIntermezzo evento) {
		// A tutto schermo, testo e personaggi; il ritorno al gioco arriva con
		// InternoMostraSchermataGioco dopo l'ultima pagina
		displayableCanvas.mostraPaginaIntermezzo(evento.getPagina());
	}

	private void gestisciEventoParagrafo(NotificaTestoParagrafo evento) {
		displayableCanvas.notificaParagrafo(evento.getMessaggio());
	}

	private void gestisciEventoPreparazioneLocazione(InternoPreparazioneLocazione evento) {
		displayableCanvas.preparaLocazione();
	}

	private void gestisciEventoRaccoltaOggetti(NotificaRaccoltaOggetti evento) {
		displayableCanvas.raccogliOggetto();
	}

	private void gestisciEventoRichiestaConfermaUscita(RichiestaUscitaDalGioco evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.confermaUscita();
	}

	private void gestisciEventoRichiestaAperturaInventarioCommerciante(ComandoAperturaInventarioCommerciante evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaAutomaArmaiolo(evento.getAutomaAcquistiArtefatti());
		displayableCanvas.armaiolo();
	}

	private void gestisciEventoRichiestaAperturaInventarioFornitore(ComandoAperturaInventarioFornitore evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.alchimista();
	}

	private void gestisciEventoRichiestaAperturaInventarioGruppo(ComandoAperturaInventarioGruppo evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaAutomaInventario(evento.getAutomaInventario());
		displayableCanvas.inventario();
	}

	private void gestisciEventoRichiestaChiusuraFinestraCombattimento(InternoRichiestaChiusuraFinestraCombattimento evento) {
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.STATO);
		displayableCanvas.getRiquadroCombattimento().setVisible(false);
	}

	private void gestisciEventoRichiestaRefreshUI(InternoRichiestaRefreshUI evento) {
		rinfresca();
	}

	private void gestisciEventoRichiestaReinizializzazioneUI(InternoRichiestaReinizializzazioneUI evento) {
		displayableCanvas.reinizializza();
		displayableCanvas.iniziaGioco();
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
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

	private void gestisciEventoRichiestaVisualizzazioneMappa(ComandoVisualizzazioneMappa evento) {
		displayableCanvas.mappa();
	}

	private void gestisciEventoSelezioneDirezione(RichiestaSelezioneDirezione evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.MAPPA);
	}

	private void gestisciEventoSelezioneIncantesimoDaLanciare(RichiestaSelezioneIncantesimoDaLanciare evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
	}

	private void gestisciEventoSelezioneSiNo(RichiestaSelezioneSiNo evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.STATO);
	}

	private void gestisciEventoStatoDiGioco(InternoStatoDiGioco evento) {
		statoDiGioco = evento.getStato();
		switch(statoDiGioco) {
			case INTRO:
				 // Richiama la schermata o animazione di introduzione
				displayableCanvas.avviaIntro();
				// La prima schermata (loghi o classifica) deve restare per un periodo intero
				temporizzatore.iniziaDopo(5_000);
				impostaAzioni(evento.getComandiPossibili());
				break;

			case FILE_DI_SALVATAGGIO_NON_VALIDO:
				displayableCanvas.scriviGrande("File di salvataggio non valido.");
				impostaAzioni(evento.getComandiPossibili());
				break;

			case PRE_GAME_ATTESA_NOME_PERSONAGGIO:
				temporizzatore.termina();
				displayableCanvas.scriviGrande("Scegli il nome del tuo personaggio o lascialo vuoto per un personaggio casuale.");
				prompt.setVisible(true);
				impostaAzioni(Collections.emptyList());
				break;

			case PRE_GAME_ATTESA_SESSO_PERSONAGGIO:
				displayableCanvas.scriviGrande("Scegli il sesso di " + prompt.getText());
				impostaAzioni(evento.getComandiPossibili());
				break;

			case PRE_GAME_ATTESA_CLASSE_PERSONAGGIO:
				displayableCanvas.scriviGrande("Scegli la classe di " + prompt.getText());
				impostaAzioni(evento.getComandiPossibili());
				break;

			case ATTESA_DIREZIONE:
				BusEventi.pubblica(new InternoErrore("Non dovrei arrivare in gestisciEventoStatoDiGioco in stato ATTESA_DIREZIONE"));
				impostaAzioni(evento.getComandiPossibili());
				break;

			case SELEZIONE_SALVATAGGIO_DA_SCRIVERE:
				displayableCanvas.selezioneSlotSalvataggioDaSalvare();
				impostaAzioni(evento.getComandiPossibili());
				break;

			default:
				throw new IllegalArgumentException("Stato di gioco non ancora gestito: " + evento.getStato());
		}
	}

	private void gestisciEventoVariazioneStatoVitalePersonaggio(NotificaVariazioneStatoVitalePersonaggio evento) {
		displayableCanvas.notificaVariazioneStatoVitale(evento.getPersonaggio());
	}

	private void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		Personaggio personaggio = evento.getPersonaggio();
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

	private void gestisciEventoInterazioneElementale(NotificaInterazioneElementalePersonaggio evento) {
		Personaggio personaggio = evento.getPersonaggio();
		TipoInterazioneElementale tipoInterazioneElementale = evento.getTipoInterazioneElementale();
		switch (tipoInterazioneElementale) {
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
			case PURIFICAZIONE:
				displayableCanvas.aggiungiInterazioneElementale(personaggio, tipoInterazioneElementale);
				break;
			default:
				throw new IllegalArgumentException("TipoInterazioneElementale non gestito: " + tipoInterazioneElementale);
		}
	}

	private void gestisciEventoVariazioneEffettoDiStato(NotificaVariazioneEffettoDiStatoPersonaggio evento) {
		Personaggio personaggio = evento.getPersonaggio();
		TipoEffettoDiStato tipoEffettoDiStato = evento.getEffetto();
		switch (evento.getTipo()) {
			case AGGIUNTA:
			case VARIAZIONE:
				displayableCanvas.aggiungiEffettoDiStato(personaggio, tipoEffettoDiStato);
				break;
            case RIMOZIONE:
				// L'effetto è terminato.
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
