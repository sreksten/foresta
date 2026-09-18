package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.*;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class DisplayableCanvas extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

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
		STATO_ARMAIOLO,
		STATO_ALCHIMISTA
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
	private final transient DisplayableCanvasRiquadroIncantesimi riquadroIncantesimi;
	private final transient DisplayableCanvasRiquadroMissioni riquadroMissioni;
	private final transient DisplayableCanvasMappaATuttoSchermo mappaATuttoSchermo;
	private final transient DisplayableCanvasInventario inventario;
	private final transient DisplayableCanvasArmaiolo armaiolo;
	private final transient DisplayableCanvasScambiatoreConsumabili alchimista;

	private final ArrayList<SpriteInterface> sprites;
	private final List<SpriteAnnuncioGlobale> codaAnnunciGlobali = new ArrayList<>();
	private SpriteAnnuncioGlobale annuncioGlobaleAttivo;
	private final List<SpriteFumetto> codaFumetti = new ArrayList<>();
	private SpriteFumetto fumettoAttivo;

	private final int larghezzaSchermo;
	private final int altezzaSchermo;

	private transient Thread animatore;
	private boolean animatoreInAzione = false;

	public DisplayableCanvas(int width, int height) {
		super();

		larghezzaSchermo = width;
		altezzaSchermo = height;
		stackElementiGrafici = new ArrayList<>();
		stackElementiGrafici.add(InterfacciaUtente.Finestra.INCANTESIMI);
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

		riquadroIntroOutro = new DisplayableCanvasIntroOutro(width, height);

		Rectangle riquadroIntroOutroRect = new Rectangle(0, 0, width, height);
		mappaCoordinateElementiGrafici.put(riquadroIntroOutro, riquadroIntroOutroRect);

		int elementoX = ImageCache.SPACING;
		int elementoY = ImageCache.SPACING;
		int larghezzaElemento = ImageCache.corniceMappa.getWidth();
		int altezzaElemento = ImageCache.corniceMappa.getHeight();

		riquadroMappa = new DisplayableCanvasRiquadroMappa(
				ImageCache.SPACING,
				ImageCache.SPACING,
				width,
				height);

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

		riquadroCombattimento = new DisplayableCanvasRiquadroCombattimento(width, height);

		// Lo calcola da solo perché è un elemento flottante a differenza degli altri che sono fissi
		mappaCoordinateElementiGrafici.put(riquadroCombattimento, riquadroCombattimento.getRettangolo());

		elementoX = ImageCache.SPACING;
		elementoY = ImageCache.SPACING + immagineLocazione.getHeight() + ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceMappa.getWidth() + ImageCache.SPACING + immagineLocazione.getWidth();
		altezzaElemento = height - ImageCache.SPACING - immagineLocazione.getHeight() - ImageCache.SPACING;

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

		riquadroIncantesimi = new DisplayableCanvasRiquadroIncantesimi(elementoX, elementoY);

		Rectangle riquadroIncantesimiRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroIncantesimi, riquadroIncantesimiRect);

		elementoX = ImageCache.SPACING + ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING + immagineLocazione.getWidth() + ImageCache.SPACING;
		elementoY = ImageCache.SPACING + ImageCache.corniceGrande.getHeight() +
				ImageCache.SPACING + ImageCache.corniceIncantesimi.getHeight() + ImageCache.SPACING;
		larghezzaElemento = ImageCache.corniceGrande.getWidth();
		altezzaElemento = ImageCache.corniceGrande.getHeight();

		riquadroMissioni = new DisplayableCanvasRiquadroMissioni(elementoX, elementoY);

		Rectangle riquadroMissioniRect = new Rectangle(elementoX, elementoY, larghezzaElemento, altezzaElemento);
		mappaCoordinateElementiGrafici.put(riquadroMissioni, riquadroMissioniRect);

		mappaATuttoSchermo = new DisplayableCanvasMappaATuttoSchermo(width, height);

		Rectangle mappaATuttoSchermoRect = new Rectangle(0, 0, width, height);
		mappaCoordinateElementiGrafici.put(mappaATuttoSchermo, mappaATuttoSchermoRect);

		inventario = new DisplayableCanvasInventario(width, height);

		Rectangle inventarioRect = new Rectangle(0, 0, width, height);
		mappaCoordinateElementiGrafici.put(inventario, inventarioRect);

		armaiolo = new DisplayableCanvasArmaiolo(width, height);

		Rectangle armaioloRect = new Rectangle(0, 0, width, height);
		mappaCoordinateElementiGrafici.put(armaiolo, armaioloRect);

		alchimista = new DisplayableCanvasScambiatoreConsumabili(width, height);

		Rectangle alchimistaRect = new Rectangle(0, 0, width, height);
		mappaCoordinateElementiGrafici.put(alchimista, alchimistaRect);

		sprites = new ArrayList<>();

		GestoreMouse gestoreMouse = new GestoreMouse();
		addMouseListener(gestoreMouse);
		addMouseMotionListener(gestoreMouse);
		addMouseWheelListener(gestoreMouse);

		registratiAEventi();
	}

	private void registratiAEventi() {
		BusEventi.iscriviti(EventoAumentoLivelloMondo.class, this::gestisciEventoAumentoLivelloMondo);
		BusEventi.iscriviti(EventoAumentoLivelloPersonaggio.class, this::gestisciEventoAumentoLivelloPersonaggio);
		BusEventi.iscriviti(EventoCreazioneSpriteAnnuncioGlobale.class, this::gestisciEventoCreazioneSpriteAnnuncioGlobale);
		BusEventi.iscriviti(EventoCreazioneSpriteATempo.class, this::gestisciEventoCreazioneSpriteATempo);
		BusEventi.iscriviti(EventoCreazioneSpriteEffetto.class, this::gestisciEventoCreazioneSpriteEffetto);
		BusEventi.iscriviti(EventoCreazioneSpriteFumetto.class, this::gestisciEventoCreazioneSpriteFumetto);
		BusEventi.iscriviti(EventoCreazioneSpriteInDissolvenza.class, this::gestisciEventoCreazioneSpriteInDissolvenza);
	}

	private void gestisciEventoAumentoLivelloMondo(EventoAumentoLivelloMondo evento) {
		notifica("LEVEL UP! Ora il mondo è al livello " + evento.getLivello() + "!");
	}

	private void gestisciEventoAumentoLivelloPersonaggio(EventoAumentoLivelloPersonaggio evento) {
		Personaggio personaggio = evento.getPersonaggio();
		notificaAnnuncioGlobale("LEVEL UP!", personaggio.getNome() + " A LIVELLO " + personaggio.getLivello() + "!");
		notifica("LEVEL UP! Ora " + personaggio.getNome() + " è al livello " + personaggio.getLivello() + "!");
		// La notifica come iconcina è fatta dal riquadro del gruppo
	}

	private void gestisciEventoCreazioneSpriteAnnuncioGlobale(EventoCreazioneSpriteAnnuncioGlobale evento) {
		if (evento.getSprite() != null) {
			codaAnnunciGlobali.add(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteATempo(EventoCreazioneSpriteATempo evento) {
		aggiungiSprite(evento.getSprite());
	}

	private void gestisciEventoCreazioneSpriteEffetto(EventoCreazioneSpriteEffetto evento) {
		aggiungiSprite(evento.getSprite());
	}

	private void gestisciEventoCreazioneSpriteFumetto(EventoCreazioneSpriteFumetto evento) {
		if (evento.getSprite() != null) {
			codaFumetti.add(evento.getSprite());
		}
	}

	private void gestisciEventoCreazioneSpriteInDissolvenza(EventoCreazioneSpriteInDissolvenza evento) {
		aggiungiSprite(evento.getSprite());
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
		while (animatoreInAzione) {
			if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO || stato == StatoDisplayableCanvas.STATO_MAPPA
					|| stato == StatoDisplayableCanvas.STATO_INVENTARIO || stato == StatoDisplayableCanvas.STATO_ARMAIOLO
					|| stato == StatoDisplayableCanvas.STATO_ALCHIMISTA) {
				repaint();
			}
			try {
				Thread.sleep(100);
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
			case INCANTESIMI:
				riquadroIncantesimi.disegnaIncantesimi(graphics);
				break;
			case TESTO:
				riquadroTesto.disegnaTesto(graphics, createImage(riquadroTesto.getImageSource()));
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
			sprite.animate(graphics);
			if (!sprite.isActive()) {
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
		fumettoAttivo.animate(graphics);
		if (!fumettoAttivo.isActive()) {
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
		annuncioGlobaleAttivo.animate(graphics);
		if (!annuncioGlobaleAttivo.isActive()) {
			annuncioGlobaleAttivo = null;
		}
	}

	void preparaLocazione() {
		riquadroLocazione.assegnaCoordinateAgliAvversari();
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
			disegnaAnnuncioGlobale(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_MAPPA) {
			mappaATuttoSchermo.disegnaMappaATuttoSchermo(graphics);
			disegnaFumetto(graphics);
			disegnaAnnuncioGlobale(graphics);
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
			disegnaAnnuncioGlobale(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_ARMAIOLO) {
			armaiolo.disegnaInventario(graphics);
			disegnaFumetto(graphics);
			disegnaAnnuncioGlobale(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_ALCHIMISTA) {
			alchimista.disegnaInventario(graphics);
			disegnaFumetto(graphics);
			disegnaAnnuncioGlobale(graphics);
		}
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
		if (stato == StatoDisplayableCanvas.STATO_ARMAIOLO) {
			return armaiolo;
		}
		if (stato == StatoDisplayableCanvas.STATO_ALCHIMISTA) {
			return alchimista;
		}
		return riquadroIntroOutro;
	}

	// Parte della interfaccia UI
	public void intro() {
		if (stato != StatoDisplayableCanvas.STATO_INTRO) {
			stato = StatoDisplayableCanvas.STATO_INTRO;
			riquadroIntroOutro.resettaSequenza();
		} else {
			// + 2 per permettere i titoli di testa e i punteggi
			riquadroIntroOutro.incrementaSequenza(Misc.STORIA.length + 2);
		}
		repaint();
	}

	public void iniziaGioco() {
		stato = StatoDisplayableCanvas.STATO_IN_GIOCO;
		repaint();
	}

	public void mappa() {
		stato = StatoDisplayableCanvas.STATO_MAPPA;
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

	public void armaiolo() {
		stato = StatoDisplayableCanvas.STATO_ARMAIOLO;
		notificaFumetto("Benvenuti. Come posso aiutarvi?", armaiolo.getCoordinateFumetto());
		repaint();
	}

	public void impostaAutomaArmaiolo(AutomaAcquistiArtefatti automaAcquistiArtefatti) {
		armaiolo.impostaAutoma(automaAcquistiArtefatti);
		repaint();
	}

	public void alchimista() {
		stato = StatoDisplayableCanvas.STATO_ALCHIMISTA;
		// FIXME: capire come gestire l'oroscopo a modo e se si possa allargare il fumetto dinamicamente.
		// FIXME: inoltre, quando si esce dalla locazione alchimista/armaiolo occorre interrompere subito il fumetto,
		// se sempre attivo.
		// FIXME: fatto quello, si può dismettere tutto il vecchio flusso.
		String oroscopo = String.join(" ", ProduttoreDiTestiCasuale.oroscopo());
		notificaFumetto(oroscopo, alchimista.getCoordinateFumetto());
		notificaFumetto("Benvenuti. Cosa posso fare per voi?", alchimista.getCoordinateFumetto());
		repaint();
	}

	public void muoviMappa(Comando direzione) {
		mappaATuttoSchermo.muoviMappa(direzione);
		repaint();
	}

	public void centraMappa() {
		mappaATuttoSchermo.centraMappa();
	}

	public void selezioneSlotSalvataggioDaSalvare() {
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
		repaint();
	}

	public void scriviGrande(String messaggio) {
		stato = StatoDisplayableCanvas.STATO_MESSAGGIO;
		riquadroIntroOutro.impostaMessaggio(messaggio);
		repaint();
	}

	public void notifica(String messaggio) {
		riquadroTesto.addString(messaggio);
		Logger.log("Nuovo messaggio: " + messaggio);
		repaint();
	}

	public void notificaMorte(Personaggio personaggio) {
		if (personaggio.isPNG()) {
			aggiungiSprite(riquadroLocazione.notificaMorte(personaggio));
		}
	}

	public void variaSalute(Personaggio personaggio, int variazione) {
		if (personaggio.isPNG()) {
			aggiungiSprite(riquadroLocazione.variaSalute(personaggio, variazione));
		}
	}

	public void variaMagia(Personaggio personaggio, int variazione) {
		if (personaggio.isPNG()) {
			aggiungiSprite(riquadroLocazione.variaMagia(personaggio, variazione));
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
				fumettoAttivo.resetTicks();
				return;
			}
			// Un fumetto diverso interrompe immediatamente quello in corso
			fumettoAttivo = null;
		}
		codaFumetti.add(new SpriteFumetto(testo, larghezzaSchermo / 5, x, y, DoomdarkFontMedium.getInstance(), DoomdarkColorModel.Color.BLACK, pointToX, pointToY));
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
			case INCANTESIMI:
				return riquadroIncantesimi;
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
			if (!rettangolo.contains(x, y)) {
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

			if (timerClickSingolo != null) {
				timerClickSingolo.stop();
				timerClickSingolo = null;
			}
			if (e.getClickCount() >= 2) {
				finestra.processaDoppioClick(x, y, tasto);
			} else {
				timerClickSingolo = new Timer(intervalloDoppioClick, evento -> finestra.processaClick(x, y, tasto));
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
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// Entrando nel canvas si stabilisce su quale riquadro si trova il cursore
			aggiornaFinestraSottoIlCursore(trovaFinestra(e), e.getX(), e.getY());
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// Uscendo dal canvas si esce anche dal riquadro su cui si era
			aggiornaFinestraSottoIlCursore(null, e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			finestra.processaTrascinamento(x, y);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			RisultatoRicerca risultatoRicerca = trovaFinestra(e);
			aggiornaFinestraSottoIlCursore(risultatoRicerca, e.getX(), e.getY());
			if (risultatoRicerca == null) {
				return;
			}
			Finestra finestra = risultatoRicerca.finestra;
			int x = risultatoRicerca.xRelativoAFinestra;
			int y = risultatoRicerca.yRelativoAFinestra;

			finestra.processaMovimento(x, y);
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
		}
	}
}
