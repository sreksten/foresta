package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.*;
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
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class ForestaUI implements InterfacciaUtente, Temporizzabile {

	private final Orientamento orientamento;
	private final boolean tuttoSchermo;
	private final Temporizzatore temporizzatore;

	private JFrame jframe;
	private Prompt prompt;
	private DisplayableCanvas displayableCanvas;
	private PannelloIcone pannelloIcone;
	private Stato statoDiGioco;

	public ForestaUI(Orientamento orientamento, boolean tuttoSchermo, Temporizzatore temporizzatore) {
		this.orientamento = orientamento;
		this.tuttoSchermo = tuttoSchermo;
		this.temporizzatore = temporizzatore;
		temporizzatore.setTemporizzabile(this);

		SwingUtilities.invokeLater(this::creaEMostraInterfacciaUtente);

		BusEventi.iscriviti(EventoComandiDisponibili.class, this::gestisciEventoComandiDisponibili);
		BusEventi.iscriviti(EventoConsumoPuntoAbilita.class, this::gestisciEventoConsumoPuntoAbilita);
		BusEventi.iscriviti(EventoFineGioco.class, this::gestisciEventoFineGioco);
		BusEventi.iscriviti(EventoFumetto.class, this::gestisciEventoFumetto);
		BusEventi.iscriviti(EventoInterazioneElementale.class, this::gestisciEventoInterazioneElementale);
		BusEventi.iscriviti(EventoMessaggio.class, this::gestisciEventoMessaggio);
		BusEventi.iscriviti(EventoMostraFinestra.class, this::gestisciEventoMostraFinestra);
		BusEventi.iscriviti(EventoMostraPunteggi.class, this::gestisciEventoMostraPunteggi);
		BusEventi.iscriviti(EventoMostraSchermataGioco.class, this::gestisciEventoMostraSchermataGioco);
		BusEventi.iscriviti(EventoMostraStatistiche.class, this::gestisciEventoMostraStatistiche);
		BusEventi.iscriviti(EventoNotificaGlobale.class, this::gestisciEventoNotificaGlobale);
		BusEventi.iscriviti(EventoParagrafo.class, this::gestisciEventoParagrafo);
		BusEventi.iscriviti(EventoPreparazioneLocazione.class, this::gestisciEventoPreparazioneLocazione);
		BusEventi.iscriviti(EventoRichiestaAperturaFinestraCombattimento.class, this::gestisciEventoRichiestaAperturaFinestraCombattimento);
		BusEventi.iscriviti(EventoRichiestaAperturaInventarioCommerciante.class, this::gestisciEventoRichiestaAperturaInventarioCommerciante);
		BusEventi.iscriviti(EventoRichiestaAperturaInventarioFornitore.class, this::gestisciEventoRichiestaAperturaInventarioFornitore);
		BusEventi.iscriviti(EventoRichiestaAperturaInventarioGruppo.class, this::gestisciEventoRichiestaAperturaInventarioGruppo);
		BusEventi.iscriviti(EventoRichiestaChiusuraFinestraCombattimento.class, this::gestisciEventoRichiestaChiusuraFinestraCombattimento);
		BusEventi.iscriviti(EventoRaccoltaOggetti.class, this::gestisciEventoRaccoltaOggetti);
		BusEventi.iscriviti(EventoSelezioneConfermaUscita.class, this::gestisciEventoRichiestaConfermaUscita);
		BusEventi.iscriviti(EventoRichiestaRefreshUI.class, this::gestisciEventoRichiestaRefreshUI);
		BusEventi.iscriviti(EventoRichiestaReinizializzazioneUI.class, this::gestisciEventoRichiestaReinizializzazioneUI);
		BusEventi.iscriviti(EventoRichiestaTesto.class, this::gestisciEventoRichiestaTesto);
		BusEventi.iscriviti(EventoRichiestaSelezioneSlotPerRilettura.class, this::gestisciEventoSelezioneSalvataggio);
		BusEventi.iscriviti(EventoRichiestaVisualizzazioneMappa.class, this::gestisciEventoRichiestaVisualizzazioneMappa);
		BusEventi.iscriviti(EventoSelezioneDirezione.class, this::gestisciEventoSelezioneDirezione);
		BusEventi.iscriviti(EventoSelezioneIncantesimoDaLanciare.class, this::gestisciEventoSelezioneIncantesimoDaLanciare);
		BusEventi.iscriviti(EventoSelezioneSiNo.class, this::gestisciEventoSelezioneSiNo);
		BusEventi.iscriviti(EventoStatoDiGioco.class, this::gestisciEventoStatoDiGioco);
		BusEventi.iscriviti(EventoVariazioneEffettoDiStato.class, this::gestisciEventoVariazioneEffettoDiStato);
		BusEventi.iscriviti(EventoVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		BusEventi.iscriviti(EventoVariazioneStatoVitalePersonaggio.class, this::gestisciEventoVariazioneStatoVitalePersonaggio);
	}
	
	private void creaEMostraInterfacciaUtente() {
		ImageCache.init();

		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		jframe = new JFrame("La Foresta");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int width;
		int height;
		if (tuttoSchermo) {
			width = screenDimension.width;
			height = screenDimension.height;
		} else {
			if (orientamento == Orientamento.ORIZZONTALE) {
				width = ImageCache.SPACING +
						ImageCache.corniceMappa.getWidth() +
						ImageCache.SPACING + 
						ImageCache.locazioni.get(ClassiLocazione.BOSCO).getWidth() +
						ImageCache.SPACING +
						ImageCache.corniceGrande.getWidth() +
						ImageCache.SPACING;
				if (screenDimension.width < width) {
					width = screenDimension.width;
				}
				height = ImageCache.SPACING +
						ImageCache.corniceGrande.getHeight() +
						ImageCache.SPACING +
						ImageCache.corniceIncantesimi.getHeight() +
						ImageCache.SPACING +
						ImageCache.corniceGrande.getHeight() +
						ImageCache.SPACING +
						ClasseIcona.getAltezzaMassima() +
						ImageCache.SPACING;
				if (screenDimension.height< width) {
					height = screenDimension.height;
				}
			} else {
				width = Math.min(screenDimension.width, 400);
				height = Math.min(screenDimension.height, 640);
			}
		}

		jframe.setLayout(null);
		Container c = jframe.getContentPane();
		c.setBackground(Color.BLACK);
		c.setPreferredSize(new Dimension(width, height));

		prompt = new Prompt();
		jframe.add(prompt);
		prompt.setLocation((width - prompt.getSize().width) / 2, (height - prompt.getSize().height) / 2);

		Logger.log("Orientamento: " + orientamento);
		int altezzaIconPanel = 72;			
		if (orientamento == Orientamento.ORIZZONTALE) {
			displayableCanvas = new DisplayableCanvas(width, height - altezzaIconPanel);
			jframe.add(displayableCanvas);
			displayableCanvas.setLocation(0, 0);
			pannelloIcone = new PannelloIcone(PannelloIcone.ORIENTAMENTO_ORIZZONTALE);
			jframe.add(pannelloIcone);
			pannelloIcone.setSize(width, altezzaIconPanel);
			pannelloIcone.setLocation(0, height - altezzaIconPanel - 1);
		} else {
			displayableCanvas = new DisplayableCanvas(640, height);
			jframe.add(displayableCanvas);
			displayableCanvas.setLocation(0, 0);
			pannelloIcone = new PannelloIcone(PannelloIcone.ORIENTAMENTO_VERTICALE);
			jframe.add(pannelloIcone);
			pannelloIcone.setSize(altezzaIconPanel, height);
			pannelloIcone.setLocation(width - altezzaIconPanel - 1, 0);
		}

		jframe.pack();
		jframe.setResizable(false);		
		jframe.setLocation((screenDimension.width - jframe.getSize().width) / 2, (screenDimension.height - jframe.getSize().height) / 2);
		jframe.setVisible(true);

		BusEventi.pubblica(new EventoInterfacciaUtentePronta());
	}

	public void tick() {
		if (statoDiGioco == Stato.INTRO) {
			displayableCanvas.intro();
		}
	}

	private void gestisciEventoRichiestaTesto(EventoRichiestaTesto evento) {
		displayableCanvas.scriviGrande(evento.getRichiesta());
		prompt.setVisible(true);
	}

	public void impostaAzioni(Collection<Comando> possibilita) {
		ComandiPossibili.set(possibilita);
		pannelloIcone.impostaAzioni();
	}

	private void gestisciEventoFumetto(EventoFumetto evento) {
		displayableCanvas.notificaFumetto(evento.getTesto(), evento.getCoordinateFumetto());
	}

	private void gestisciEventoMessaggio(EventoMessaggio evento) {
		displayableCanvas.notifica(evento.getMessaggio());
	}

	private void gestisciEventoMostraFinestra(EventoMostraFinestra evento) {
		for (InterfacciaUtente.Finestra finestra : evento.getFinestre()) {
			displayableCanvas.primoPiano(finestra);
		}
		rinfresca();
	}

	private void gestisciEventoMostraPunteggi(EventoMostraPunteggi evento) {
		displayableCanvas.mostraPunteggi();
	}

	private void gestisciEventoMostraSchermataGioco(EventoMostraSchermataGioco evento) {
		displayableCanvas.iniziaGioco();
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
	}

	private void gestisciEventoMostraStatistiche(EventoMostraStatistiche evento) {
		displayableCanvas.mostraStatistiche();
	}

	private void gestisciEventoNotificaGlobale(EventoNotificaGlobale evento) {
		displayableCanvas.notificaAnnuncioGlobale(evento.getEtichetta(), evento.getMessaggio());
	}

	private void gestisciEventoParagrafo(EventoParagrafo evento) {
		displayableCanvas.notifica("");
		displayableCanvas.notifica(evento.getMessaggio());
	}

	private void gestisciEventoPreparazioneLocazione(EventoPreparazioneLocazione evento) {
		displayableCanvas.preparaLocazione();
	}

	private void gestisciEventoRaccoltaOggetti(EventoRaccoltaOggetti evento) {
		displayableCanvas.raccogliOggetto();
	}

	private void gestisciEventoRichiestaConfermaUscita(EventoSelezioneConfermaUscita evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.confermaUscita();
	}

	private void gestisciEventoRichiestaAperturaInventarioCommerciante(EventoRichiestaAperturaInventarioCommerciante evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaAutomaArmaiolo(evento.getAutomaAcquistiArtefatti());
		displayableCanvas.armaiolo();
	}

	private void gestisciEventoRichiestaAperturaInventarioFornitore(EventoRichiestaAperturaInventarioFornitore evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.alchimista();
	}

	private void gestisciEventoRichiestaAperturaInventarioGruppo(EventoRichiestaAperturaInventarioGruppo evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.impostaAutomaInventario(evento.getAutomaInventario());
		displayableCanvas.inventario();
	}

	private void gestisciEventoRichiestaChiusuraFinestraCombattimento(EventoRichiestaChiusuraFinestraCombattimento evento) {
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.STATO);
		displayableCanvas.getRiquadroCombattimento().setVisible(false);
	}

	private void gestisciEventoRichiestaRefreshUI(EventoRichiestaRefreshUI evento) {
		rinfresca();
	}

	private void gestisciEventoRichiestaReinizializzazioneUI(EventoRichiestaReinizializzazioneUI evento) {
		displayableCanvas.reinizializza();
		displayableCanvas.iniziaGioco();
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.GRAFICA);
		rinfresca();
	}

	private void gestisciEventoSelezioneSalvataggio(EventoRichiestaSelezioneSlotPerRilettura evento) {
		temporizzatore.termina();
		impostaAzioni(evento.getSalvataggiDisponibili().stream().map(TestataSalvataggio::getId).collect(Collectors.toList()));
		displayableCanvas.selezioneSlotSalvataggioDaCaricare(evento.getSalvataggiDisponibili());
	}

	private void gestisciEventoRichiestaAperturaFinestraCombattimento(EventoRichiestaAperturaFinestraCombattimento evento) {
		DisplayableCanvasRiquadroCombattimento infoCombattimento = displayableCanvas.getRiquadroCombattimento();
		infoCombattimento.setCombattente(evento.getPersonaggio());
		infoCombattimento.setAvversario(evento.getAvversario());
		infoCombattimento.setVisible(true);
		rinfresca();
	}

	private void gestisciEventoRichiestaVisualizzazioneMappa(EventoRichiestaVisualizzazioneMappa evento) {
		displayableCanvas.mappa();
	}

	private void gestisciEventoSelezioneDirezione(EventoSelezioneDirezione evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.MAPPA);
	}

	private void gestisciEventoSelezioneIncantesimoDaLanciare(EventoSelezioneIncantesimoDaLanciare evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
	}

	private void gestisciEventoSelezioneSiNo(EventoSelezioneSiNo evento) {
		impostaAzioni(evento.getPossibilita());
		displayableCanvas.primoPiano(InterfacciaUtente.Finestra.STATO);
	}

	private void gestisciEventoStatoDiGioco(EventoStatoDiGioco evento) {
		statoDiGioco = evento.getStato();
		switch(statoDiGioco) {
			case INTRO:
				 // Richiama la schermata o animazione di introduzione
				displayableCanvas.intro();
				temporizzatore.inizia(5_000);
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
				BusEventi.pubblica(new EventoErroreInterno("Non dovrei arrivare in gestisciEventoStatoDiGioco in stato ATTESA_DIREZIONE"));
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

	private void gestisciEventoVariazioneStatoVitalePersonaggio(EventoVariazioneStatoVitalePersonaggio evento) {
		displayableCanvas.notificaVariazioneStatoVitale(evento.getPersonaggio());
	}

	private void gestisciEventoVariazioneStatistichePersonaggio(EventoVariazioneStatistichePersonaggio evento) {
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

	private void gestisciEventoComandiDisponibili(EventoComandiDisponibili evento) {
		ComandiPossibili.set(evento.getPossibilita());
		pannelloIcone.impostaAzioni();
	}

	private void gestisciEventoConsumoPuntoAbilita(EventoConsumoPuntoAbilita evento) {
		TipoAttributo tipoAttributo = evento.getTipoAttributo();
		displayableCanvas.notificaAnnuncioGlobale("AUMENTO", tipoAttributo.getNome().toUpperCase());
	}

	private void gestisciEventoFineGioco(EventoFineGioco evento) {
		if (evento.isCompletatoConSuccesso()) {
			displayableCanvas.vinto();
		} else {
			displayableCanvas.perso();
		}
	}

	private void gestisciEventoInterazioneElementale(EventoInterazioneElementale evento) {
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

	private void gestisciEventoVariazioneEffettoDiStato(EventoVariazioneEffettoDiStato evento) {
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
