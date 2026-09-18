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

		SwingUtilities.invokeLater(this::createAndShowGUI);

		// EventoCombattimento non ci interessa, solo mostrare i suoi effetti eventuali che vengono pubblicati dal personaggio interessato
		BusEventi.iscriviti(EventoComandiDisponibili.class, this::gestisciEventoComandiDisponibili);
		BusEventi.iscriviti(EventoConsumoPuntoAbilita.class, this::gestisciEventoConsumoPuntoAbilita);
		// EventoCreazionePersonaggio non ci interessa, riguarda il motore
		BusEventi.iscriviti(EventoFumetto.class, this::gestisciEventoFumetto);
		BusEventi.iscriviti(EventoInterazioneElementale.class, this::gestisciEventoInterazioneElementale);
		BusEventi.iscriviti(EventoMessaggio.class, this::gestisciEventoMessaggio);
		BusEventi.iscriviti(EventoNotificaGlobale.class, this::gestisciEventoNotificaGlobale);
		BusEventi.iscriviti(EventoParagrafo.class, this::gestisciEventoParagrafo);
		BusEventi.iscriviti(EventoRichiestaReinizializzazioneUI.class, this::gestisciEventoRichiestaReinizializzazioneUI);
		BusEventi.iscriviti(EventoRichiestaTesto.class, this::gestisciEventoRichiestaTesto);
		// EventoValutazioneAttaccante non ci interessa, è il motore AI degli avversari che informa sul suo stato di progressione
		BusEventi.iscriviti(EventoRichiestaSelezioneSlotPerRilettura.class, this::gestisciEventoSelezioneSalvataggio);
		BusEventi.iscriviti(EventoStatoDiGioco.class, this::gestisciEventoStatoDiGioco);
		BusEventi.iscriviti(EventoVariazioneEffettoDiStato.class, this::gestisciEventoVariazioneEffettoDiStato);
		BusEventi.iscriviti(EventoVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		BusEventi.iscriviti(EventoVariazioneStatoVitalePersonaggio.class, this::gestisciEventoVariazioneStatoVitalePersonaggio);
	}
	
	private void createAndShowGUI() {
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
		// Per ora non fa niente, in realtà dovrebbe gestire la intro per adesso
		if (statoDiGioco == Stato.INTRO) {
			displayableCanvas.intro();
		}
	}

	@Override
	public void mostraSchermataGioco() {
		displayableCanvas.iniziaGioco();
	}

	@Override
	public void mappa() {
		displayableCanvas.mappa();
	}

	@Override
	public void inventario() {
		displayableCanvas.inventario();
	}

	@Override
	public void impostaAutomaInventario(AutomaInventario automaInventario) {
		displayableCanvas.impostaAutomaInventario(automaInventario);
	}

	@Override
	public void armaiolo() {
		displayableCanvas.armaiolo();
	}

	@Override
	public void impostaAutomaArmaiolo(AutomaAcquistiArtefatti automaAcquistiArtefatti) {
		displayableCanvas.impostaAutomaArmaiolo(automaAcquistiArtefatti);
	}

	@Override
	public void alchimista() {
		displayableCanvas.alchimista();
	}

	@Override
	public void centraMappa() {
		displayableCanvas.centraMappa();
	}

	@Override
	public void selezioneSlotSalvataggioDaSalvare() {
		displayableCanvas.selezioneSlotSalvataggioDaSalvare();
	}

	@Override
	public void confermaUscita() {
		displayableCanvas.confermaUscita();
	}

	@Override
	public void muoviMappa(Comando direzione) {
		displayableCanvas.muoviMappa(direzione);
	}

	@Override
	public void perso() {
		displayableCanvas.perso();
	}

	@Override
	public void vinto() {
		displayableCanvas.vinto();
	}

	@Override
	public void statistiche() {
		displayableCanvas.statistiche();
	}

	@Override
	public void punteggi() {
		displayableCanvas.hiscore();
	}

	@Override
	public void scriviGrande(String messaggio) {
		displayableCanvas.scriviGrande(messaggio);
	}

	@Override
	public void primoPiano(InterfacciaUtente.Finestra finestra) {
		displayableCanvas.primoPiano(finestra);
	}

	@Override
	public void secondoPiano(InterfacciaUtente.Finestra finestra) {
		displayableCanvas.secondoPiano(finestra);
	}

	private void gestisciEventoRichiestaTesto(EventoRichiestaTesto evento) {
		chiediTesto();
	}

	@Override
	public void chiediTesto() {
		prompt.setVisible(true);
	}

	@Override
	public void riceviTesto(String testo) {
		prompt.setVisible(false);
		BusEventi.pubblica(new EventoTestoDisponibile(testo));
	}

	@Override
	public void impostaAzioni() {
		pannelloIcone.impostaAzioni();
	}

	@Override
	public void preparaLocazione() {
		displayableCanvas.preparaLocazione();
	}

	@Override
	public void infoCombattimento(boolean mostra, Personaggio combattente, Personaggio avversario) {
		Logger.log("ForestaApplet::infoCombattimento(" + mostra + ")");
		if (!mostra) {
			displayableCanvas.getRiquadroCombattimento().setVisible(false);
		} else {
			DisplayableCanvasRiquadroCombattimento infoCombattimento = displayableCanvas.getRiquadroCombattimento();
			infoCombattimento.setCombattente(combattente);
			infoCombattimento.setAvversario(avversario);
			infoCombattimento.setVisible(true);
		}
		rinfresca();
	}

	private void gestisciEventoFumetto(EventoFumetto evento) {
		displayableCanvas.notificaFumetto(evento.getTesto(), evento.getCoordinateFumetto());
	}

	private void gestisciEventoMessaggio(EventoMessaggio evento) {
		displayableCanvas.notifica(evento.getMessaggio());
	}

	private void gestisciEventoNotificaGlobale(EventoNotificaGlobale evento) {
		displayableCanvas.notificaAnnuncioGlobale(evento.getEtichetta(), evento.getMessaggio());
	}

	private void gestisciEventoParagrafo(EventoParagrafo evento) {
		displayableCanvas.notifica("");
		displayableCanvas.notifica(evento.getMessaggio());
	}

	private void gestisciEventoRichiestaReinizializzazioneUI(EventoRichiestaReinizializzazioneUI evento) {
		displayableCanvas.reinizializza();
		mostraSchermataGioco();
		primoPiano(InterfacciaUtente.Finestra.GRAFICA);
		rinfresca();
	}

	private void gestisciEventoSelezioneSalvataggio(EventoRichiestaSelezioneSlotPerRilettura evento) {
		temporizzatore.termina();
		ComandiPossibili.reimposta();
		evento.getSalvataggiDisponibili().stream().map(TestataSalvataggio::getId).forEach(ComandiPossibili::add);
		impostaAzioni();
		displayableCanvas.selezioneSlotSalvataggioDaCaricare(evento.getSalvataggiDisponibili());
	}

	private void gestisciEventoStatoDiGioco(EventoStatoDiGioco evento) {
		statoDiGioco = evento.getStato();
		switch(statoDiGioco) {
			case INTRO:
				 // Richiama la schermata o animazione di introduzione
				displayableCanvas.intro();
				temporizzatore.inizia(5_000);
				ComandiPossibili.set(evento.getComandiPossibili());
				impostaAzioni();
				break;

			case FILE_DI_SALVATAGGIO_NON_VALIDO:
				displayableCanvas.scriviGrande("File di salvataggio non valido.");
				ComandiPossibili.set(evento.getComandiPossibili());
				impostaAzioni();
				break;

			case PRE_GAME_ATTESA_NOME_PERSONAGGIO:
				temporizzatore.termina();
				displayableCanvas.scriviGrande("Scegli il nome del tuo personaggio o lascialo vuoto per un personaggio casuale.");
				prompt.setVisible(true);
				ComandiPossibili.reimposta();
				impostaAzioni();
				break;

			case PRE_GAME_ATTESA_SESSO_PERSONAGGIO:
				displayableCanvas.scriviGrande("Scegli il sesso di " + prompt.getText());
				ComandiPossibili.set(evento.getComandiPossibili());
				impostaAzioni();
				break;

			case PRE_GAME_ATTESA_CLASSE_PERSONAGGIO:
				displayableCanvas.scriviGrande("Scegli la classe di " + prompt.getText());
				ComandiPossibili.set(evento.getComandiPossibili());
				impostaAzioni();
				break;

			case ATTESA_DIREZIONE:
				ComandiPossibili.set(evento.getComandiPossibili());
				impostaAzioni();
				break;

			default:
				throw new IllegalArgumentException("Stato di gioco non ancora gestito: " + evento.getStato());
		}
	}

	private void gestisciEventoVariazioneStatoVitalePersonaggio(EventoVariazioneStatoVitalePersonaggio evento) {
		displayableCanvas.notificaMorte(evento.getPersonaggio());
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
		ComandiPossibili.set(evento.getComandiDisponibili());
		pannelloIcone.impostaAzioni();
	}

	private void gestisciEventoConsumoPuntoAbilita(EventoConsumoPuntoAbilita evento) {
		TipoAttributo tipoAttributo = evento.getTipoAttributo();
		displayableCanvas.notificaAnnuncioGlobale("AUMENTO", tipoAttributo.getNome().toUpperCase());
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

	//FIXME ancora non li gestiamo a livello grafico
	private void gestisciEventoVariazioneEffettoDiStato(EventoVariazioneEffettoDiStato evento) {
		Personaggio personaggio = evento.getPersonaggio();
		TipoEffettoDiStato tipoEffettoDiStato = evento.getEffetto();
		switch (evento.getTipo()) {
			case AGGIUNTA:
			case VARIAZIONE:
				displayableCanvas.aggiungiEffettoDiStato(personaggio, tipoEffettoDiStato);
				break;
            case RIMOZIONE:
				break;
			default:
				throw new IllegalArgumentException("TipoVariazioneEffettoDiStato non gestito: " + evento.getTipo());
		}
	}

	@Override
	public void raccogliOggetto() {
		displayableCanvas.raccogliOggetto();
	}

	@Override
	public void rinfresca() {
		Logger.log("ForestaApplet::rinfresca()");
		jframe.invalidate();
		jframe.repaint();
	}
}
