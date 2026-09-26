package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.AutomaAcquistiArtefatti;
import com.threeamigos.foresta.motore.AutomaIncantatore;
import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.modellodati.Messaggio;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.motore.modellodati.TipoNegozio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import com.threeamigos.foresta.tools.Temporizzatore;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Classe che rappresenta il canvas grafico del gioco.
 * Le animazioni sono gestite dal metodo run() che gira a Temporizzatore.FRAME_PER_SECONDO
 * fotogrammi al secondo.
 */
public class DisplayableCanvas extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	public static final int ORIENTAMENTO_ORIZZONTALE = 0;
	public static final int ORIENTAMENTO_VERTICALE = 1;

	private enum StatoDisplayableCanvas {
		STATO_INTRO,
		STATO_SELEZIONE_SLOT_DA_CARICARE,
		STATO_MESSAGGIO,
		STATO_IN_GIOCO,
		STATO_MAPPA,
		STATO_SELEZIONE_SLOT_DA_SALVARE,
		STATO_CONFERMA_USCITA,
		STATO_PERSO,
		STATO_VINTO,
		STATO_STATISTICHE,
		STATO_PUNTEGGI,
		STATO_INVENTARIO,
		STATO_COMMERCIANTE,
		STATO_ALCHIMISTA,
		STATO_INCANTATORE,
		STATO_INTERMEZZO
	}

	private StatoDisplayableCanvas stato;
	private final ArrayList<InterfacciaUtente.Finestra> stackElementiGrafici;

	private final Map<Finestra, Rectangle> mappaCoordinateElementiGrafici = new HashMap<>();
	
	private final transient DisplayableCanvasIntroOutro riquadroIntroOutro;
	private final transient DisplayableCanvasRiquadroMappa riquadroMappa;
	private final transient DisplayableCanvasRiquadroLocazione riquadroLocazione;
	private final transient DisplayableCanvasRiquadroStatistiche riquadroStatistiche;
	private final transient DisplayableCanvasRiquadroCombattimento riquadroCombattimento;
	private final transient DisplayableCanvasRiquadroTesto riquadroTesto;
	private final transient DisplayableCanvasRiquadroGruppo riquadroGruppo;
	private final transient DisplayableCanvasRiquadroIncantesimiEPozioni riquadroIncantesimiEPozioni;
	private final transient DisplayableCanvasRiquadroMissioni riquadroMissioni;
	private final transient DisplayableCanvasMappaATuttoSchermo mappaATuttoSchermo;
	private final transient DisplayableCanvasInventario inventario;
	// Armaiolo e venditore di pergamene: una sola schermata, che cambia secondo il negozio
	private final transient DisplayableCanvasCommerciante commerciante;
	private final transient DisplayableCanvasScambiatoreConsumabili alchimista;
	private final transient DisplayableCanvasIncantatore incantatore;
	private final transient DisplayableCanvasBarraIcone barraIcone;
	private final transient DisplayableCanvasIntermezzo intermezzo;

	private final ArrayList<SpriteInterface> sprites;
	private final List<SpriteAnnuncioGlobale> codaAnnunciGlobali = new ArrayList<>();
	private SpriteAnnuncioGlobale annuncioGlobaleAttivo;
	private final List<SpriteFumetto> codaFumetti = new ArrayList<>();
	private SpriteFumetto fumettoAttivo;

	private final int larghezzaSchermo;
	private final int altezzaSchermo;
	private final int altezzaTotaleSchermo;

	private transient Thread animatore;
	private boolean animatoreInAzione = false;
	// La prossima intro deve partire dalla classifica invece che dai loghi (vedi mostraPunteggi)
	private boolean introDallaClassifica = false;

	/**
	 * La parte della finestra in cui si disegnano riquadri e schermate a tutto schermo,
	 * esclusa la barra icone.
	 */
	static Dimension calcolaAreaDiContenuto(int width, int height, int orientamento, int dimensioneBarraIcone) {
		if (orientamento == ORIENTAMENTO_ORIZZONTALE) {
			return new Dimension(width, height - dimensioneBarraIcone);
		}
		return new Dimension(640, height);
	}

	/**
	 * @param barraDock vero per la barra delle icone che si ingrandisce sotto il cursore (DisplayableCanvasBarraIconeDock),
	 *                  solo in orientamento orizzontale; altrimenti la barra classica
	 */
	public DisplayableCanvas(int width, int height, int orientamento, int dimensioneBarraIcone, boolean barraDock) {
		super();
		altezzaTotaleSchermo = height;
		Dimension areaDiContenuto = calcolaAreaDiContenuto(width, height, orientamento, dimensioneBarraIcone);
		int larghezzaContenuto = areaDiContenuto.width;
		int altezzaContenuto = areaDiContenuto.height;
		larghezzaSchermo = larghezzaContenuto;
		altezzaSchermo = altezzaContenuto;
		stackElementiGrafici = new ArrayList<>();
		stackElementiGrafici.add(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.STATO);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.MAPPA);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.STATISTICHE);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.GRAFICA);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.TESTO);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.MISSIONI);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.INFO_COMBATTIMENTO);
		// INTRO_OUTRO e MAPPA_A_TUTTO_SCHERMO non appartengono allo stack: occupano
		// da soli tutto lo schermo e sono scelti in base allo stato del canvas.
		stato = StatoDisplayableCanvas.STATO_INTRO;
		setSize(width, height);
		setBackground(Color.black);
		BufferedImage immagineLocazione = ImageCache.locazioni.get(ClassiLocazione.BOSCO);

		riquadroIntroOutro = new DisplayableCanvasIntroOutro(larghezzaContenuto, altezzaContenuto);

		Rectangle riquadroIntroOutroRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(riquadroIntroOutro, riquadroIntroOutroRect);

		intermezzo = new DisplayableCanvasIntermezzo(larghezzaContenuto, altezzaContenuto, riquadroIntroOutro);
		mappaCoordinateElementiGrafici.put(intermezzo, new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto));

		int elementoX = ImageCache.SPACING;
		int elementoY = ImageCache.SPACING;
		int larghezzaElemento = ImageCache.corniceMappa.getWidth();
		int altezzaElemento = ImageCache.corniceMappa.getHeight();

		riquadroMappa = new DisplayableCanvasRiquadroMappa(
				ImageCache.SPACING,
				ImageCache.SPACING,
				larghezzaContenuto,
				altezzaContenuto);

		Rectangle riquadroMappaRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroMappa, riquadroMappaRect);

		elementoX = ImageCache.SPACING + ImageCache.corniceMappa.getWidth() + ImageCache.SPACING;
		elementoY = ImageCache.SPACING;
		larghezzaElemento = immagineLocazione.getWidth();
		altezzaElemento = immagineLocazione.getHeight();

		riquadroLocazione = new DisplayableCanvasRiquadroLocazione(elementoX, elementoY);

		Rectangle riquadroLocazioneRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroLocazione, riquadroLocazioneRect);

		elementoX = ImageCache.SPACING;
		elementoY = ImageCache.SPACING + ImageCache.corniceMappa.getHeight() + ImageCache.SPACING;
		larghezzaElemento = ImageCache.cornicePiccola.getWidth();
		altezzaElemento = ImageCache.cornicePiccola.getHeight();

		riquadroStatistiche = new DisplayableCanvasRiquadroStatistiche(elementoX, elementoY);

		Rectangle riquadroStatisticheRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroStatistiche, riquadroStatisticheRect);

		riquadroCombattimento = new DisplayableCanvasRiquadroCombattimento(larghezzaContenuto, altezzaContenuto);

		// Lo calcola da solo perché è un elemento flottante a differenza degli altri che sono fissi
		mappaCoordinateElementiGrafici.put(riquadroCombattimento, riquadroCombattimento.getRettangolo());

		elementoX = ImageCache.SPACING;
		elementoY = ImageCache.SPACING + immagineLocazione.getHeight() + ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceMappa.getWidth() + ImageCache.SPACING + immagineLocazione.getWidth();
		altezzaElemento = altezzaContenuto - ImageCache.SPACING - immagineLocazione.getHeight() - ImageCache.SPACING;

		riquadroTesto = new DisplayableCanvasRiquadroTesto(elementoX, elementoY, larghezzaElemento, altezzaElemento);

		Rectangle riquadroTestoRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroTesto, riquadroTestoRect);

		elementoX = ImageCache.SPACING + ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING + immagineLocazione.getWidth() + ImageCache.SPACING;
		elementoY = ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceGrande.getWidth();
		altezzaElemento = ImageCache.corniceGrande.getHeight();

		riquadroGruppo = new DisplayableCanvasRiquadroGruppo(elementoX, elementoY);

		Rectangle riquadroGruppoRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroGruppo, riquadroGruppoRect);

		elementoX = ImageCache.SPACING + ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING + immagineLocazione.getWidth() + ImageCache.SPACING;
		elementoY = ImageCache.SPACING + ImageCache.corniceGrande.getHeight() +	ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceIncantesimi.getWidth();
		altezzaElemento = ImageCache.corniceIncantesimi.getHeight();

		riquadroIncantesimiEPozioni = new DisplayableCanvasRiquadroIncantesimiEPozioni(elementoX, elementoY);

		Rectangle riquadroIncantesimiRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroIncantesimiEPozioni, riquadroIncantesimiRect);

		elementoX = ImageCache.SPACING + ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING + immagineLocazione.getWidth() + ImageCache.SPACING;
		elementoY = ImageCache.SPACING + ImageCache.corniceGrande.getHeight() +
				ImageCache.SPACING + ImageCache.corniceIncantesimi.getHeight() + ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceGrande.getWidth();
		altezzaElemento = ImageCache.corniceGrande.getHeight();

		riquadroMissioni = new DisplayableCanvasRiquadroMissioni(elementoX, elementoY);

		Rectangle riquadroMissioniRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroMissioni, riquadroMissioniRect);

		mappaATuttoSchermo = new DisplayableCanvasMappaATuttoSchermo(larghezzaContenuto, altezzaContenuto);

		Rectangle mappaATuttoSchermoRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(mappaATuttoSchermo, mappaATuttoSchermoRect);

		inventario = new DisplayableCanvasInventario(larghezzaContenuto, altezzaContenuto);

		Rectangle inventarioRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(inventario, inventarioRect);

		commerciante = new DisplayableCanvasCommerciante(larghezzaContenuto, altezzaContenuto);

		Rectangle commercianteRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(commerciante, commercianteRect);

		alchimista = new DisplayableCanvasScambiatoreConsumabili(larghezzaContenuto, altezzaContenuto);

		Rectangle alchimistaRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(alchimista, alchimistaRect);

		incantatore = new DisplayableCanvasIncantatore(larghezzaContenuto, altezzaContenuto);

		Rectangle incantatoreRect = new Rectangle(0, 0, larghezzaContenuto, altezzaContenuto);
		mappaCoordinateElementiGrafici.put(incantatore, incantatoreRect);

		int barraLarghezza;
		int barraAltezza;
		int barraX;
		int barraY;
		if (orientamento == ORIENTAMENTO_ORIZZONTALE) {
			barraLarghezza = width;
			barraAltezza = dimensioneBarraIcone;
			barraX = 0;
			barraY = altezzaContenuto;
		} else {
			barraLarghezza = dimensioneBarraIcone;
			barraAltezza = height;
			barraX = width - dimensioneBarraIcone;
			barraY = 0;
		}
		barraIcone = barraDock && orientamento == ORIENTAMENTO_ORIZZONTALE
				? new DisplayableCanvasBarraIconeDock(barraX, barraY, barraLarghezza, barraAltezza)
				: new DisplayableCanvasBarraIcone(orientamento, barraX, barraY, barraLarghezza, barraAltezza);

		Rectangle barraIconeRect = new Rectangle(barraX, barraY, barraLarghezza, barraAltezza);
		mappaCoordinateElementiGrafici.put(barraIcone, barraIconeRect);

		sprites = new ArrayList<>();

		GestoreMouse gestoreMouse = new GestoreMouse();
		addMouseListener(gestoreMouse);
		addMouseMotionListener(gestoreMouse);
		addMouseWheelListener(gestoreMouse);

		registratiAEventi();
	}

	private void registratiAEventi() {
		// Eventi globali
		BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, this::gestisciEventoAumentoLivelloMondo);
		// Eventi interni del motore grafico - i sottopannelli potrebbero richiedere la creazione di sprite da gestire qui
		BusEventi.iscriviti(InternoCreazioneSpriteAnnuncioGlobale.class, this::gestisciEventoCreazioneSpriteAnnuncioGlobale);
		BusEventi.iscriviti(InternoCreazioneSpriteATempo.class, this::gestisciEventoCreazioneSpriteATempo);
		BusEventi.iscriviti(InternoCreazioneSpriteEffettoDiStato.class, this::gestisciEventoCreazioneSpriteEffetto);
		BusEventi.iscriviti(InternoCreazioneSpriteFumettoATempo.class, this::gestisciEventoCreazioneSpriteFumetto);
		BusEventi.iscriviti(InternoCreazioneSpriteInDissolvenza.class, this::gestisciEventoCreazioneSpriteInDissolvenza);
		// Eventi del riquadro gruppo
		BusEventi.iscriviti(NotificaAumentoLivelloPersonaggio.class, this::gestisciEventoAumentoLivelloPersonaggio);
		BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::gestisciEventoVariazioneStatistichePersonaggio);
		// Eventi del riquadro incantesimi
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaIncantesimi.class, this::gestisciEventoVariazioneIncantesimi);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSalute.class, this::gestisciEventoVariazionePozioniSalute);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSaluteGrandi.class, this::gestisciEventoVariazionePozioniSaluteGrandi);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagia.class, this::gestisciEventoVariazionePozioniMagia);
		BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagiaGrandi.class, this::gestisciEventoVariazionePozioniMagiaGrandi);
		// Eventi del riquadro missioni
		BusEventi.iscriviti(NotificaAggiornamentoStatoMissione.class, this::gestisciEventoAggiornamentoStatoMissione);

	}

	// Eventi globali

	private void gestisciEventoAumentoLivelloMondo(NotificaAumentoLivelloMondo evento) {
		notifica("LEVEL UP! Ora il mondo è al livello " + evento.getLivello() + "!");
	}

	private void gestisciEventoCreazioneSpriteAnnuncioGlobale(InternoCreazioneSpriteAnnuncioGlobale evento) {
		if (evento.getSprite() != null) {
			codaAnnunciGlobali.add(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteATempo(InternoCreazioneSpriteATempo evento) {
		if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			aggiungiSprite(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteEffetto(InternoCreazioneSpriteEffettoDiStato evento) {
		aggiungiSprite(evento.getSprite());
	}

	private void gestisciEventoCreazioneSpriteFumetto(InternoCreazioneSpriteFumettoATempo evento) {
		if (evento.getSprite() != null) {
			codaFumetti.add(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteInDissolvenza(InternoCreazioneSpriteInDissolvenza evento) {
		aggiungiSprite(evento.getSprite());
	}

	// Eventi del riquadro del gruppo

	private void gestisciEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
		Personaggio personaggio = evento.getPersonaggio();
		notificaAnnuncioGlobale("LEVEL UP!", personaggio.getNome() + " A LIVELLO " + personaggio.getLivello() + "!");
		notifica("LEVEL UP! Ora " + personaggio.getNome() + " è al livello " + personaggio.getLivello() + "!");
		// La notifica come iconcina è fatta dal riquadro del gruppo
		primoPiano(InterfacciaUtente.Finestra.STATO);
		riquadroGruppo.gestisciEventoAumentoLivelloPersonaggio(evento);
	}

	private void gestisciEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
		primoPiano(InterfacciaUtente.Finestra.STATO);
		riquadroGruppo.gestisciEventoVariazioneStatistichePersonaggio(evento);
	}

	// Eventi del riquadro incantesimi

	private void gestisciEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
		primoPiano(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazioneIncantesimi(evento);
	}

	private void gestisciEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
		primoPiano(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniSalute(evento);
	}

	private void gestisciEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
		primoPiano(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniSaluteGrandi(evento);
	}

	private void gestisciEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
		primoPiano(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniMagia(evento);
	}

	private void gestisciEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
		primoPiano(InterfacciaUtente.Finestra.INCANTESIMI_E_POZIONI);
		riquadroIncantesimiEPozioni.gestisciEventoVariazionePozioniMagiaGrandi(evento);
	}

	private void gestisciEventoAggiornamentoStatoMissione(NotificaAggiornamentoStatoMissione evento) {
		primoPiano(InterfacciaUtente.Finestra.MISSIONI);
		notificaAnnuncioGlobale(evento.getEtichetta(), evento.getDescrizione());
	}

	public void selezioneSlotSalvataggioDaCaricare(Collection<TestataSalvataggio> salvataggiDisponibili) {
		riquadroIntroOutro.setSalvataggiDisponibili(salvataggiDisponibili);
		stato = StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_CARICARE;
		repaint();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		avviaThreadAnimazione();
	}
	
	private void avviaThreadAnimazione() {
		if (animatore == null || !animatoreInAzione) {
			animatore = new Thread(this);
			animatore.setDaemon(true);
			animatore.start();
		}
	}

	public void fermaThreadAnimazione() {
		animatoreInAzione = false;
	}

	public void run() {
		animatoreInAzione = true;
		// Cadenza fissa: la scadenza del fotogramma successivo si calcola dalla precedente,
		// non da quando finisce il sonno, altrimenti i ritardi di sleep() si accumulano e
		// il ritmo effettivo scende sotto FRAME_PER_SECONDO (e diventa irregolare).
		long periodoNanos = 1_000_000_000L / Temporizzatore.FRAME_PER_SECONDO;
		long prossimoFotogramma = System.nanoTime();
		while (animatoreInAzione) {
			if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO || stato == StatoDisplayableCanvas.STATO_MAPPA
					|| stato == StatoDisplayableCanvas.STATO_INVENTARIO || stato == StatoDisplayableCanvas.STATO_COMMERCIANTE
					|| stato == StatoDisplayableCanvas.STATO_ALCHIMISTA
					|| stato == StatoDisplayableCanvas.STATO_INCANTATORE
					|| stato == StatoDisplayableCanvas.STATO_INTERMEZZO
					|| annuncioGlobaleAttivo != null || !codaAnnunciGlobali.isEmpty()
					|| barraIcone.inTransizione()) {
				repaint();
			}
			prossimoFotogramma += periodoNanos;
			long attesaNanos = prossimoFotogramma - System.nanoTime();
			if (attesaNanos <= 0) {
				// In ritardo di oltre un fotogramma (es. il sistema era sospeso): si riparte
				// da adesso invece di tentare di recuperare con una raffica di repaint().
				prossimoFotogramma = System.nanoTime();
				continue;
			}
			try {
				Thread.sleep(attesaNanos / 1_000_000L, (int) (attesaNanos % 1_000_000L));
			} catch (InterruptedException e) {
			}
		}
	}
	
	DisplayableCanvasRiquadroCombattimento getRiquadroCombattimento() {
		return riquadroCombattimento;
	}

	public void reinizializza() {
		riquadroTesto.clear();
	}

	public void primoPiano(InterfacciaUtente.Finestra finestra) {
		if (!stackElementiGrafici.remove(finestra)) {
			throw new IllegalArgumentException("Elemento grafico non valido: " + finestra);
		}
		stackElementiGrafici.add(finestra);
	}

	private void inGioco(Graphics gfx) {
		Graphics2D graphics = (Graphics2D)gfx;

		ArrayList<InterfacciaUtente.Finestra> copiaStack = new ArrayList<>(stackElementiGrafici.size());
		copiaStack.addAll(stackElementiGrafici);

		for (InterfacciaUtente.Finestra finestra : copiaStack) {
			switch (finestra) {
			case MAPPA:
				riquadroMappa.disegnaMappa(graphics);
				break;
			case STATISTICHE:
				riquadroStatistiche.disegnaStatistiche(graphics);
				break;
			case GRAFICA:
				riquadroLocazione.disegnaLocazione(graphics);
				break;
			case STATO:
				riquadroGruppo.disegnaStatus(graphics);
				break;
			case INCANTESIMI_E_POZIONI:
				riquadroIncantesimiEPozioni.disegnaIncantesimi(graphics);
				break;
			case TESTO:
				riquadroTesto.disegnaTesto(graphics);
				break;
			case MISSIONI:
				riquadroMissioni.disegnaMissioni(graphics);
				break;
			case INFO_COMBATTIMENTO:
				riquadroCombattimento.disegnaInfoCombattimento(graphics);
				break;
			default:
				throw new IllegalArgumentException();
			}
		}

//		gfx.setColor(Color.RED);
//		for (Rectangle rectangle : mappaCoordinateElementiGrafici.values()) {
//			gfx.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//		}

		List<SpriteInterface> copiaSprites = new ArrayList<>(sprites);
		List<SpriteInterface> inactiveSprites = new ArrayList<>();
		for (SpriteInterface sprite : copiaSprites) {
			sprite.anima(graphics);
			if (!sprite.isAttivo()) {
				inactiveSprites.add(sprite);
			}
		}
		for (SpriteInterface inactiveSprite : inactiveSprites) {
			sprites.remove(inactiveSprite);
		}
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		aggiornaSchermo((Graphics2D)graphics);
	}


	private void disegnaFumetto(Graphics2D graphics) {
		if (fumettoAttivo == null && !codaFumetti.isEmpty()) {
			fumettoAttivo = codaFumetti.remove(0);
		}
		if (fumettoAttivo == null) {
			return;
		}
		fumettoAttivo.anima(graphics);
		if (!fumettoAttivo.isAttivo()) {
			fumettoAttivo = null;
		}
	}

	/**
	 * A differenza degli altri sprite, ancorati a coordinate di riquadri validi solo in
	 * STATO_IN_GIOCO, l'annuncio globale è centrato sull'intero schermo e va quindi disegnato
	 * sopra il contenuto corrente indipendentemente dallo stato del canvas.
	 */
	private void disegnaAnnuncioGlobale(Graphics2D graphics) {
		if (annuncioGlobaleAttivo == null && !codaAnnunciGlobali.isEmpty()) {
			annuncioGlobaleAttivo = codaAnnunciGlobali.remove(0);
		}
		if (annuncioGlobaleAttivo == null) {
			return;
		}
		annuncioGlobaleAttivo.anima(graphics);
		if (!annuncioGlobaleAttivo.isAttivo()) {
			annuncioGlobaleAttivo = null;
		}
	}

	void preparaLocazione() {
		primoPiano(InterfacciaUtente.Finestra.GRAFICA);
		riquadroLocazione.assegnaCoordinateAgliAvversari();
	}

	// Sprite decorativo statico, visibile solo mentre è mostrata la mappa a
	// tutto schermo, ancorato all'angolo inferiore sinistro.
	private void disegnaSferaMagica(Graphics2D graphics) {
		if (stato == StatoDisplayableCanvas.STATO_MAPPA && !ModelloDati.getIstanza().getNotizieMD().getUltimeNotizie().isEmpty()) {
			graphics.drawImage(ImageCache.sferaMagica, 0, altezzaTotaleSchermo - ImageCache.sferaMagica.getHeight(), null);
		}
	}

	private void aggiornaSchermo(Graphics2D graphics) {

		if (stato == StatoDisplayableCanvas.STATO_INTRO) {
			riquadroIntroOutro.intro(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_CARICARE) {
			riquadroIntroOutro.selezioneSlotDaCaricare(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_MESSAGGIO) {
			riquadroIntroOutro.scrivi(graphics, true);
		} else if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			inGioco(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_MAPPA) {
			mappaATuttoSchermo.disegnaMappaATuttoSchermo(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_SALVARE) {
			riquadroIntroOutro.selezioneSlotDaSalvare(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_CONFERMA_USCITA) {
			riquadroIntroOutro.confermaUscita(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_PERSO) {
			riquadroIntroOutro.perso(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_VINTO) {
			riquadroIntroOutro.vinto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_STATISTICHE) {
			riquadroIntroOutro.statistiche(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_PUNTEGGI) {
			riquadroIntroOutro.hiscore(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_INVENTARIO) {
			inventario.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_COMMERCIANTE) {
			commerciante.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_ALCHIMISTA) {
			alchimista.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_INCANTATORE) {
			incantatore.disegnaInventario(graphics);
			disegnaFumetto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_INTERMEZZO) {
			intermezzo.disegna(graphics);
			disegnaFumetto(graphics);
		}
		disegnaAnnuncioGlobale(graphics);
		barraIcone.disegna(graphics);
		disegnaSferaMagica(graphics);
	}

	/**
	 * Riporta l'unico elemento grafico che occupa tutto lo schermo nello stato corrente,
	 * oppure null se lo schermo è composto dallo stack degli elementi di gioco.
	 * Rispecchia le scelte di aggiornaSchermo().
	 */
	private Finestra finestraATuttoSchermo() {
		if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			return null;
		}
		if (stato == StatoDisplayableCanvas.STATO_MAPPA) {
			return mappaATuttoSchermo;
		}
		if (stato == StatoDisplayableCanvas.STATO_INVENTARIO) {
			return inventario;
		}
		if (stato == StatoDisplayableCanvas.STATO_COMMERCIANTE) {
			return commerciante;
		}
		if (stato == StatoDisplayableCanvas.STATO_ALCHIMISTA) {
			return alchimista;
		}
		if (stato == StatoDisplayableCanvas.STATO_INCANTATORE) {
			return incantatore;
		}
		if (stato == StatoDisplayableCanvas.STATO_INTERMEZZO) {
			return intermezzo;
		}
		return riquadroIntroOutro;
	}

	// Parte della interfaccia UI
	/**
	 * Fa ripartire l'intro: dai loghi, oppure dalla classifica se è appena stato
	 * registrato un punteggio. Va chiamato anche quando il canvas è già in
	 * STATO_INTRO (è lo stato iniziale del canvas, all'avvio del gioco).
	 */
	public void avviaIntro() {
		stato = StatoDisplayableCanvas.STATO_INTRO;
		if (introDallaClassifica) {
			riquadroIntroOutro.posizionaSuPunteggi();
			introDallaClassifica = false;
		} else {
			riquadroIntroOutro.resettaSequenza();
		}
		repaint();
	}

	/**
	 * Passa alla schermata successiva dell'intro, a ogni battito del temporizzatore della UI.
	 */
	public void avanzaIntro() {
		if (stato != StatoDisplayableCanvas.STATO_INTRO) {
			avviaIntro();
			return;
		}
		riquadroIntroOutro.incrementaSequenza(riquadroIntroOutro.lunghezzaIntro());
		repaint();
	}

	public void iniziaGioco() {
		// Se si torna al gioco da un intermezzo, le sue immagini non servono più
		intermezzo.svuota();
		stato = StatoDisplayableCanvas.STATO_IN_GIOCO;
		abortisciFumetto();
		repaint();
	}

	public void mappa() {
		stato = StatoDisplayableCanvas.STATO_MAPPA;
		mappaATuttoSchermo.preparaMappa();
		mappaATuttoSchermo.centraSuGiocatore();
		repaint();
	}

	public void inventario() {
		stato = StatoDisplayableCanvas.STATO_INVENTARIO;
		repaint();
	}

	public void impostaAutomaInventario(AutomaInventario automaInventario) {
		inventario.impostaAutoma(automaInventario);
		repaint();
	}

	public void commerciante() {
		stato = StatoDisplayableCanvas.STATO_COMMERCIANTE;
		notificaFumetto("Benvenuti. Come posso aiutarvi?", commerciante.getCoordinateFumetto());
		repaint();
	}

	public void impostaAutomaCommerciante(TipoNegozio negozio, AutomaAcquistiArtefatti automaAcquistiArtefatti) {
		commerciante.impostaNegozio(negozio);
		commerciante.impostaAutoma(automaAcquistiArtefatti);
		repaint();
	}

	/**
	 * @param messaggio il fumetto con cui accoglie l'incantatore, o null per nessuno
	 */
	public void incantatore(String messaggio) {
		stato = StatoDisplayableCanvas.STATO_INCANTATORE;
		if (messaggio != null) {
			notificaFumetto(messaggio, incantatore.getCoordinateFumetto());
		}
		repaint();
	}

	public void impostaAutomaIncantatore(AutomaIncantatore automaIncantatore) {
		incantatore.impostaAutoma(automaIncantatore);
		repaint();
	}

	public void alchimista() {
		stato = StatoDisplayableCanvas.STATO_ALCHIMISTA;
		// FIXME: capire come gestire l'oroscopo a modo e se si possa allargare il fumetto dinamicamente.
		// FIXME: fatto quello, si può dismettere tutto il vecchio flusso.
		String oroscopo = String.join(" ", ProduttoreDiTestiCasuale.oroscopo());
		notificaFumetto(oroscopo, alchimista.getCoordinateFumetto());
		notificaFumetto("Benvenuti. Cosa posso fare per voi?", alchimista.getCoordinateFumetto());
		repaint();
	}

	public void spostaMappa(Comando direzione) {
		mappaATuttoSchermo.muoviMappa(direzione);
		repaint();
	}

	public void centraMappa() {
		mappaATuttoSchermo.centraSuGiocatore();
	}

	public void impostaAzioniIcone() {
		barraIcone.impostaAzioni();
		repaint();
	}

	public void selezioneSlotSalvataggioDaSalvare() {
		// Le testate si leggono una volta sola all'apertura della schermata: ognuna ricostruisce
		// dal file il gruppo del salvataggio con i suoi personaggi, troppo per farlo a ogni frame.
		// Mentre la schermata è aperta i file non cambiano (si salva solo dopo averla chiusa).
		riquadroIntroOutro.setSalvataggiDisponibili(GestoreSalvataggi.getSalvataggiDisponibili());
		stato = StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_SALVARE;
		repaint();
	}
	
	public void confermaUscita() {
		stato = StatoDisplayableCanvas.STATO_CONFERMA_USCITA;
		repaint();
	}
	
	public void perso() {
		if (stato != StatoDisplayableCanvas.STATO_PERSO) {
			stato = StatoDisplayableCanvas.STATO_PERSO;
			riquadroIntroOutro.resettaSequenza();
		} else {
			riquadroIntroOutro.incrementaSequenza(Misc.PERSO.length);
		}
		repaint();
	}

	public void vinto() {
		if (stato != StatoDisplayableCanvas.STATO_VINTO) {
			stato = StatoDisplayableCanvas.STATO_VINTO;
			riquadroIntroOutro.resettaSequenza();
		} else {
			riquadroIntroOutro.incrementaSequenza(Misc.VINTO.length);
		}
		repaint();
	}

	public void mostraStatistiche() {
		stato = StatoDisplayableCanvas.STATO_STATISTICHE;
		repaint();
	}

	public void mostraPunteggi() {
		stato = StatoDisplayableCanvas.STATO_PUNTEGGI;
		// Il motore torna subito all'intro: che parta dalla classifica appena aggiornata
		introDallaClassifica = true;
		repaint();
	}

	public void mostraPaginaIntermezzo(PaginaIntermezzo pagina) {
		stato = StatoDisplayableCanvas.STATO_INTERMEZZO;
		intermezzo.mostra(pagina);
		repaint();
	}

	public void scriviGrande(String messaggio) {
		stato = StatoDisplayableCanvas.STATO_MESSAGGIO;
		riquadroIntroOutro.impostaMessaggio(messaggio);
		repaint();
	}

	public void notifica(String messaggio) {
		riquadroTesto.addString(messaggio);
		repaint();
	}

	/**
	 * Un nuovo paragrafo è separato dal testo precedente da una riga vuota.
	 */
	public void notificaParagrafo(String messaggio) {
		aggiungiParagrafo(messaggio);
		repaint();
	}

	private void aggiungiParagrafo(String messaggio) {
		riquadroTesto.addString("");
		riquadroTesto.addString(messaggio);
	}

	/**
	 * Ripopola il pannello di testo dopo un caricamento, dal più vecchio al più recente
	 * e con la stessa impaginazione (paragrafi o continuazioni) con cui i messaggi erano
	 * stati mostrati.
	 *
	 * @param messaggiDalPiuRecenteAlPiuVecchio come li restituisce {@code Notizie.getUltimiMessaggi()}
	 */
	public void ripristinaMessaggi(List<Messaggio> messaggiDalPiuRecenteAlPiuVecchio) {
		riquadroTesto.clear();
		for (int i = messaggiDalPiuRecenteAlPiuVecchio.size() - 1; i >= 0; i--) {
			Messaggio messaggio = messaggiDalPiuRecenteAlPiuVecchio.get(i);
			if (messaggio.isParagrafo()) {
				aggiungiParagrafo(messaggio.getTesto());
			} else {
				riquadroTesto.addString(messaggio.getTesto());
			}
		}
		repaint();
	}

	public void notificaVariazioneStatoVitale(Personaggio personaggio) {
		if (personaggio.isPNG()) {
			aggiungiSprite(riquadroLocazione.notificaMorte(personaggio));
		} else {
			primoPiano(InterfacciaUtente.Finestra.STATO);
		}
	}

	public void variaSalute(Personaggio personaggio, int variazione) {
		if (personaggio.isPNG()) {
			aggiungiSprite(riquadroLocazione.variaSalute(personaggio, variazione));
		} else {
			primoPiano(InterfacciaUtente.Finestra.STATO);
		}
	}

	public void variaMagia(Personaggio personaggio, int variazione) {
		if (personaggio.isPNG()) {
			aggiungiSprite(riquadroLocazione.variaMagia(personaggio, variazione));
		} else {
			primoPiano(InterfacciaUtente.Finestra.STATO);
		}
	}

	public void raccogliOggetto() {
		aggiungiSprite(riquadroLocazione.raccogliOggetto());
	}

	void notificaFumetto(String testo, CoordinateFumetto coordinateFumetto) {
		notificaFumetto(testo, coordinateFumetto.getX(), coordinateFumetto.getY(), coordinateFumetto.getPointToX(), coordinateFumetto.getPointToY());
	}

	public void notificaFumetto(String testo, int x, int y, int pointToX, int pointToY) {
		if (fumettoAttivo != null) {
			if (testo.equals(fumettoAttivo.getTesto())) {
				fumettoAttivo.resettaImpulsi();
				return;
			}
			// Un fumetto diverso interrompe immediatamente quello in corso, scartando
			// anche eventuali fumetti già in coda dietro di lui
			fumettoAttivo = null;
			codaFumetti.clear();
		}
		codaFumetti.add(new SpriteFumetto(testo, larghezzaSchermo / 5, x, y, DoomdarkFontMedium.getInstance(), DoomdarkColorModel.Color.BLACK, pointToX, pointToY));
	}

	private void abortisciFumetto() {
		fumettoAttivo = null;
		codaFumetti.clear();
	}

	public void notificaAnnuncioGlobale(String etichetta, String messaggio) {
		codaAnnunciGlobali.add(new SpriteAnnuncioGlobale(etichetta, messaggio, larghezzaSchermo, altezzaSchermo));
	}

	public void aggiungiEffettoDiStato(Personaggio personaggio, TipoEffettoDiStato effettoDiStato) {
		aggiungiSprite(riquadroLocazione.aggiungiEffettoDiStato(personaggio, effettoDiStato));
	}

	public void aggiungiInterazioneElementale(Personaggio personaggio, TipoInterazioneElementale interazioneElementale) {
		aggiungiSprite(riquadroLocazione.aggiungiInterazioneElementale(personaggio, interazioneElementale));
	}

	private void aggiungiSprite(SpriteInterface sprite) {
		if (sprite != null) {
			sprites.add(sprite);
		}
	}

	private Finestra recuperaFinestra(InterfacciaUtente.Finestra finestra) {
		switch (finestra) {
			case INTRO_OUTRO:
				return riquadroIntroOutro;
			case MAPPA:
				return riquadroMappa;
			case STATISTICHE:
				return riquadroStatistiche;
			case GRAFICA:
				return riquadroLocazione;
			case STATO:
				return riquadroGruppo;
			case INCANTESIMI_E_POZIONI:
				return riquadroIncantesimiEPozioni;
			case TESTO:
				return riquadroTesto;
			case MISSIONI:
				return riquadroMissioni;
			case INFO_COMBATTIMENTO:
				return riquadroCombattimento;
			case MAPPA_A_TUTTO_SCHERMO:
				return mappaATuttoSchermo;
			case INVENTARIO:
				return inventario;
			default:
				throw new IllegalArgumentException("Elemento grafico non valido: " + finestra);
		}
	}

	private class GestoreMouse implements MouseListener, MouseMotionListener, MouseWheelListener {

		// AWT segnala entrata e uscita solo per il canvas nel suo complesso: per averle
		// per singolo riquadro va ricordato su quale si trovava il cursore.
		private Finestra finestraSottoIlCursore;

		// mouseClicked scatta una volta per ogni click: su un doppio click arrivano quindi
		// sia un evento con clickCount 1 sia uno con clickCount 2. Il primo va quindi rimandato,
		// per poterlo scartare se nel frattempo arriva il secondo (altrimenti processaClick e
		// processaDoppioClick scatterebbero entrambi per un solo doppio click).
		//
		// Limiti noti, lasciati così perché oggi innocui:
		// - il timer ricorda la finestra cliccata e nessuno lo ferma quando la schermata cambia: se entro
		//   l'intervallo la schermata si chiude, processaClick scatta lo stesso sulla finestra che non si vede
		//   più (oggi apre o chiude al massimo i dettagli di un oggetto). Per correggerlo basterebbe controllare
		//   nel timer che la finestra sia ancora quella mostrata, o fermare il timer a ogni cambio di schermata;
		// - l'intervallo è fisso a 175 ms, mentre Swing riconosce il doppio click con quello del sistema (di
		//   solito 400-500 ms): un doppio click più lento di 175 ms fa scattare sia il click singolo sia il
		//   doppio. Con l'intervallo del sistema il click singolo risponderebbe più lentamente.
		private final int intervalloDoppioClick = intervalloDoppioClick();
		private Timer timerClickSingolo;

		private int intervalloDoppioClick() {
			return 175;
//			Object valore = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
//			return valore instanceof Integer ? (Integer) valore : 500;
		}

		private class RisultatoRicerca {
			final Finestra finestra;
			final int xRelativoAFinestra;
			final int yRelativoAFinestra;
			RisultatoRicerca(Finestra finestra, int xRelativoAFinestra, int yRelativoAFinestra) {
				this.finestra = finestra;
				this.xRelativoAFinestra = xRelativoAFinestra;
				this.yRelativoAFinestra = yRelativoAFinestra;
			}
		}

		private RisultatoRicerca trovaFinestra(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			// La barra delle icone è sempre disegnata in primo piano sopra a tutto
			// il resto (vedi aggiornaSchermo), quindi va controllata per prima.
			RisultatoRicerca risultatoBarraIcone = creaRisultato(barraIcone, x, y);
			if (risultatoBarraIcone != null) {
				return risultatoBarraIcone;
			}
			// Fuori dal gioco lo schermo è occupato da un unico elemento grafico:
			// non c'è nessuno stack da percorrere.
			Finestra finestraATuttoSchermo = finestraATuttoSchermo();
			if (finestraATuttoSchermo != null) {
				return creaRisultato(finestraATuttoSchermo, x, y);
			}
			ArrayList<InterfacciaUtente.Finestra> copiaStack = new ArrayList<>(stackElementiGrafici.size());
			copiaStack.addAll(stackElementiGrafici);
			// Lo stack viene disegnato dal fondo (indice 0) verso il primo piano (ultimo
			// indice), quindi va percorso al contrario: l'evento spetta all'elemento
			// più in primo piano fra quelli che contengono il punto.
			for (int indice = copiaStack.size() - 1; indice >= 0; indice--) {
				RisultatoRicerca risultato = creaRisultato(recuperaFinestra(copiaStack.get(indice)), x, y);
				if (risultato != null) {
					return risultato;
				}
			}
			return null;
		}

		private RisultatoRicerca creaRisultato(Finestra finestra, int x, int y) {
			if (!finestra.isVisibile()) {
				return null;
			}
			Rectangle rettangolo = mappaCoordinateElementiGrafici.get(finestra);
			// La barra decide da sé quali punti sono suoi: le icone ingrandite escono dal suo rettangolo
			boolean dentro = finestra == barraIcone
					? barraIcone.contiene(x - rettangolo.x, y - rettangolo.y)
					: rettangolo.contains(x, y);
			if (!dentro) {
				return null;
			}
			return new RisultatoRicerca(finestra, x - rettangolo.x, y - rettangolo.y);
		}

		/**
		 * Notifica uscita ed entrata quando il cursore passa da un riquadro a un altro.
		 * Le coordinate assolute servono per l'uscita, che va comunicata al riquadro
		 * precedente e cade quindi fuori dal suo rettangolo.
		 */
		private void aggiornaFinestraSottoIlCursore(RisultatoRicerca risultatoRicerca, int x, int y) {
			Finestra nuovaFinestra = risultatoRicerca == null ? null : risultatoRicerca.finestra;
			if (nuovaFinestra == finestraSottoIlCursore) {
				return;
			}
			if (finestraSottoIlCursore != null) {
				Rectangle rettangolo = mappaCoordinateElementiGrafici.get(finestraSottoIlCursore);
				finestraSottoIlCursore.processaUscita(x - rettangolo.x, y - rettangolo.y);
			}
			finestraSottoIlCursore = nuovaFinestra;
			if (risultatoRicerca != null) {
				nuovaFinestra.processaEntrata(risultatoRicerca.xRelativoAFinestra, risultatoRicerca.yRelativoAFinestra);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			Finestra.Tasto tasto;
			if (SwingUtilities.isLeftMouseButton(e)) {
				tasto = Finestra.Tasto.SINISTRO;
			} else if (SwingUtilities.isRightMouseButton(e)) {
				tasto = Finestra.Tasto.DESTRO;
			} else if (SwingUtilities.isMiddleMouseButton(e)) {
				tasto = Finestra.Tasto.CENTRALE;
			} else {
				return;
			}

			if (!finestra.gestisceDoppioClick()) {
				// Nessuna attesa: questa finestra non distingue il doppio click dal singolo,
				// quindi ogni click va processato subito.
				finestra.processaClick(x, y, tasto);
				repaint();
				return;
			}

			if (timerClickSingolo != null) {
				timerClickSingolo.stop();
				timerClickSingolo = null;
			}
			if (e.getClickCount() >= 2) {
				finestra.processaDoppioClick(x, y, tasto);
				repaint();
			} else {
				timerClickSingolo = new Timer(intervalloDoppioClick, evento -> {
					finestra.processaClick(x, y, tasto);
					repaint();
				});
				timerClickSingolo.setRepeats(false);
				timerClickSingolo.start();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			if (SwingUtilities.isLeftMouseButton(e)) {
				finestra.processaPressione(x, y, Finestra.Tasto.SINISTRO);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				finestra.processaPressione(x, y, Finestra.Tasto.DESTRO);
			} else if (SwingUtilities.isMiddleMouseButton(e)) {
				finestra.processaPressione(x, y, Finestra.Tasto.CENTRALE);
			}
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			if (SwingUtilities.isLeftMouseButton(e)) {
				finestra.processaRilascio(x, y, Finestra.Tasto.SINISTRO);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				finestra.processaRilascio(x, y, Finestra.Tasto.DESTRO);
			} else if (SwingUtilities.isMiddleMouseButton(e)) {
				finestra.processaRilascio(x, y, Finestra.Tasto.CENTRALE);
			}
			repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// Entrando nel canvas si stabilisce su quale riquadro si trova il cursore
			aggiornaFinestraSottoIlCursore(trovaFinestra(e), e.getX(), e.getY());
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// Uscendo dal canvas si esce anche dal riquadro su cui si era
			aggiornaFinestraSottoIlCursore(null, e.getX(), e.getY());
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			// Anche trascinando si può passare da un riquadro all'altro: chi viene lasciato riceve processaUscita
			aggiornaFinestraSottoIlCursore(risultatoRicerca, e.getX(), e.getY());
			if (risultatoRicerca == null) {
				repaint();
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			finestra.processaTrascinamento(x, y);
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			aggiornaFinestraSottoIlCursore(risultatoRicerca, e.getX(), e.getY());
			if (risultatoRicerca == null) {
				repaint();
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			finestra.processaMovimento(x, y);
			repaint();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			int rotazioni = e.getWheelRotation();
			if (rotazioni < 0) {
				finestra.processaRotella(x, y, -rotazioni, Finestra.MovimentoRotella.SU);
			} else {
				finestra.processaRotella(x, y, rotazioni, Finestra.MovimentoRotella.GIU);
			}
			repaint();
		}
	}
}
