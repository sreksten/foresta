package com.threeamigos.foresta.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

public class DisplayableCanvas extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	private enum StatoDisplayableCanvas {
		STATO_INTRO,
		STATO_SELEZIONE_NUOVO_GIOCO_O_CARICA,
		STATO_SELEZIONE_SLOT_DA_CARICARE,
		STATO_MESSAGGIO,
		STATO_IN_GIOCO,
		STATO_MAPPA,
		STATO_SELEZIONE_SLOT_DA_SALVARE,
		STATO_CONFERMA_USCITA,
		STATO_PERSO,
		STATO_VINTO,
		STATO_STATISTICHE,
		STATO_HISCORE
	}

	private StatoDisplayableCanvas stato;
	private ArrayList<InterfacciaUtente.Finestra> stackElementiGrafici;
	
	private transient DisplayableCanvasIntroOutro introOutro;
	private transient DisplayableCanvasRiquadroMappa riquadroMappa;
	private transient DisplayableCanvasRiquadroLocazione riquadroLocazione;
	private transient DisplayableCanvasRiquadroStatistiche riquadroStatistiche;
	private transient DisplayableCanvasRiquadroCombattimento riquadroCombattimento;
	private transient DisplayableCanvasRiquadroTesto riquadroTesto;
	private transient DisplayableCanvasRiquadroGruppo riquadroGruppo;
	private transient DisplayableCanvasRiquadroIncantesimi riquadroIncantesimi;
	private transient DisplayableCanvasRiquadroMissioni riquadroMissioni;
	private transient DisplayableCanvasMappaATuttoSchermo mappaATuttoSchermo;
	
	private ArrayList<SpriteInterface> sprites;
	
	private transient Thread animator;
	private boolean animatorRunning = false;
	
	public DisplayableCanvas(int width, int height) {
		super();
		stackElementiGrafici = new ArrayList<>();
		stackElementiGrafici.add(InterfacciaUtente.Finestra.INCANTESIMI);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.STATO);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.MAPPA);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.STATISTICHE);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.GRAFICA);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.TESTO);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.MISSIONI);
		stackElementiGrafici.add(InterfacciaUtente.Finestra.INFO_COMBATTIMENTO);
		stato = StatoDisplayableCanvas.STATO_INTRO;
		setSize(width, height);
		setBackground(Color.black);
		BufferedImage immagineLocazione = ImageCache.locazioni.get(ClassiLocazione.BOSCO);

		introOutro = new DisplayableCanvasIntroOutro(width, height);
		riquadroMappa = new DisplayableCanvasRiquadroMappa(
				ImageCache.SPACING,
				ImageCache.SPACING);
		riquadroLocazione = new DisplayableCanvasRiquadroLocazione(
				ImageCache.SPACING + ImageCache.corniceMappa.getWidth() + ImageCache.SPACING,
				ImageCache.SPACING);
		riquadroStatistiche = new DisplayableCanvasRiquadroStatistiche(
				ImageCache.SPACING,
				ImageCache.SPACING + ImageCache.corniceMappa.getHeight() + ImageCache.SPACING);
		riquadroCombattimento = new DisplayableCanvasRiquadroCombattimento(width, height);
		riquadroTesto = new DisplayableCanvasRiquadroTesto(
				ImageCache.SPACING,
				ImageCache.SPACING + ImageCache.locazioni.get(ClassiLocazione.BOSCO).getHeight() + ImageCache.SPACING,
				ImageCache.corniceMappa.getWidth() + ImageCache.SPACING + immagineLocazione.getWidth(),
				height - ImageCache.SPACING - immagineLocazione.getHeight() - ImageCache.SPACING);
		riquadroGruppo = new DisplayableCanvasRiquadroGruppo(
				ImageCache.SPACING + ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING + ImageCache.locazioni.get(ClassiLocazione.BOSCO).getWidth() + ImageCache.SPACING,
				ImageCache.SPACING);
		riquadroIncantesimi = new DisplayableCanvasRiquadroIncantesimi(
				ImageCache.SPACING + ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING + ImageCache.locazioni.get(ClassiLocazione.BOSCO).getWidth() + ImageCache.SPACING,
				ImageCache.SPACING + ImageCache.corniceGrande.getHeight() +	ImageCache.SPACING);
		riquadroMissioni = new DisplayableCanvasRiquadroMissioni(
				ImageCache.SPACING + ImageCache.corniceMappa.getWidth() +
				ImageCache.SPACING + ImageCache.locazioni.get(ClassiLocazione.BOSCO).getWidth() + ImageCache.SPACING,
				ImageCache.SPACING + ImageCache.corniceGrande.getHeight() +
				ImageCache.SPACING + ImageCache.corniceIncantesimi.getHeight() + ImageCache.SPACING);
		mappaATuttoSchermo = new DisplayableCanvasMappaATuttoSchermo(width, height);

		sprites = new ArrayList<>();
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		startAnimationThread();
	}
	
	private void startAnimationThread() {
		if (animator == null || !animatorRunning) {
			animator = new Thread(this);
			animator.setDaemon(true);
			animator.start();
		}
	}
	
	public void stopAnimationThread() {
		animatorRunning = false;
	}
	
	public void run() {
		animatorRunning = true;
		while (animatorRunning) {
			if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
				//gameUpdate();
				//gameRender();
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
		stato = StatoDisplayableCanvas.STATO_IN_GIOCO;
		switch(finestra) {
		case GRAFICA:
			stackElementiGrafici.remove(InterfacciaUtente.Finestra.GRAFICA);
			stackElementiGrafici.add(InterfacciaUtente.Finestra.GRAFICA);
			break;
		case STATO:
			stackElementiGrafici.remove(InterfacciaUtente.Finestra.STATO);
			stackElementiGrafici.add(InterfacciaUtente.Finestra.STATO);
			break;
		case INCANTESIMI:
			stackElementiGrafici.remove(InterfacciaUtente.Finestra.INCANTESIMI);
			stackElementiGrafici.add(InterfacciaUtente.Finestra.INCANTESIMI);
			break;
		case MAPPA:
			stackElementiGrafici.remove(InterfacciaUtente.Finestra.MAPPA);
			stackElementiGrafici.add(InterfacciaUtente.Finestra.MAPPA);
			break;
		case STATISTICHE:
			stackElementiGrafici.remove(InterfacciaUtente.Finestra.STATISTICHE);
			stackElementiGrafici.add(InterfacciaUtente.Finestra.STATISTICHE);
			break;
		case MISSIONI:
			stackElementiGrafici.remove(InterfacciaUtente.Finestra.MISSIONI);
			stackElementiGrafici.add(InterfacciaUtente.Finestra.MISSIONI);
			break;
		case INFO_COMBATTIMENTO:
			stackElementiGrafici.remove(InterfacciaUtente.Finestra.INFO_COMBATTIMENTO);
			stackElementiGrafici.add(InterfacciaUtente.Finestra.INFO_COMBATTIMENTO);
			break;
		default:
			throw new IllegalArgumentException();
		}
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
		
		List<SpriteInterface> inactiveSprites = new ArrayList<>();
		for (SpriteInterface sprite : sprites) {
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

	void preparaLocazione() {
		riquadroLocazione.assegnaCoordinateAgliAvversari();
	}

	private void aggiornaSchermo(Graphics2D graphics) {

		if (stato == StatoDisplayableCanvas.STATO_INTRO) {
			introOutro.intro(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_SELEZIONE_NUOVO_GIOCO_O_CARICA) {
			introOutro.selezioneNuovoGiocoOCarica(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_CARICARE) {
			introOutro.selezioneSlotDaCaricare(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_MESSAGGIO) {
			introOutro.scrivi(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_IN_GIOCO) {
			inGioco(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_MAPPA) {
			mappaATuttoSchermo.disegnaMappaATuttoSchermo(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_SALVARE) {
			introOutro.selezioneSlotDaSalvare(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_CONFERMA_USCITA) {
			introOutro.confermaUscita(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_PERSO) {
			introOutro.perso(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_VINTO) {
			introOutro.vinto(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_STATISTICHE) {
			introOutro.statistiche(graphics);
		} else if (stato == StatoDisplayableCanvas.STATO_HISCORE) {
			introOutro.hiscore(graphics);
		}
	}
	
	// Parte della interfaccia UI
	public void intro() {
		if (stato != StatoDisplayableCanvas.STATO_INTRO) {
			stato = StatoDisplayableCanvas.STATO_INTRO;
			introOutro.resettaSequenza();
		} else {
			// + 2 per permettere i titoli di testa e gli highscore
			introOutro.incrementaSequenza(Misc.STORIA.length + 2);
		}
		repaint();
	}
	
	public void nuovoGiocoOCaricaPrecedente() {
		stato = StatoDisplayableCanvas.STATO_SELEZIONE_NUOVO_GIOCO_O_CARICA;
		repaint();
	}

	public void selezioneSlotSalvataggioDaCaricare() {
		stato = StatoDisplayableCanvas.STATO_SELEZIONE_SLOT_DA_CARICARE;
		repaint();
	}

	public void mappa() {
		stato = StatoDisplayableCanvas.STATO_MAPPA;
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
			introOutro.resettaSequenza();
		} else {
			introOutro.incrementaSequenza(Misc.PERSO.length);
		}
		repaint();
	}

	public void vinto() {
		if (stato != StatoDisplayableCanvas.STATO_VINTO) {
			stato = StatoDisplayableCanvas.STATO_VINTO;
			introOutro.resettaSequenza();
		} else {
			introOutro.incrementaSequenza(Misc.VINTO.length);
		}
		repaint();
	}

	public void statistiche() {
		stato = StatoDisplayableCanvas.STATO_STATISTICHE;
		repaint();
	}

	public void hiscore() {
		stato = StatoDisplayableCanvas.STATO_HISCORE;
		repaint();
	}

	public void scriviGrande(String messaggio) {
		stato = StatoDisplayableCanvas.STATO_MESSAGGIO;
		introOutro.impostaMessaggio(messaggio);
		repaint();
	}

	public void notifica(String messaggio) {
		riquadroTesto.addString(messaggio);
		Logger.log("Nuovo messaggio: " + messaggio);
		repaint();
	}
	
	public void notificaMorte(Personaggio personaggio) {
		if (personaggio.isPNG()) {
			addSprite(riquadroLocazione.notificaMorte(personaggio));
		}
	}
	
	public void variaForza(Personaggio personaggio, int variazione) {
		if (personaggio.isPNG()) {
			addSprite(riquadroLocazione.variaForza(personaggio, variazione));
		} else {
			addSprite(riquadroGruppo.variaForza(personaggio, variazione));
		}
	}

	public void variaForzaMassima(Personaggio personaggio, int variazione) {
		addSprite(riquadroGruppo.variaForzaMassima(personaggio, variazione));
	}

	public void variaMagia(Personaggio personaggio, int variazione) {
		if (personaggio.isPNG()) {
			addSprite(riquadroLocazione.variaMagia(personaggio, variazione));
		} else {
			addSprite(riquadroGruppo.variaMagia(personaggio, variazione));
		}
	}
	
	public void variaMagiaMassima(Personaggio personaggio, int variazione) {
		addSprite(riquadroGruppo.variaMagiaMassima(personaggio, variazione));
	}
	
	public void variaCoraggio(Personaggio personaggio, int variazione) {
		addSprite(riquadroGruppo.variaCoraggio(personaggio, variazione));
	}
	
	public void variaValore(Personaggio personaggio, int variazione) {
		addSprite(riquadroGruppo.variaValore(personaggio, variazione));
	}
	
	public void variaCarisma(Personaggio personaggio, int variazione) {
		addSprite(riquadroGruppo.variaCarisma(personaggio, variazione));
	}
	
	public void variaStanchezza(Personaggio personaggio, int variazione) {
		addSprite(riquadroGruppo.variaStanchezza(personaggio, variazione));
	}

	public void variaTempo(Personaggio personaggio, int variazione) {
		addSprite(riquadroGruppo.variaTempo(personaggio, variazione));
	}

	public void variaMonete(int variazione) {
		addSprite(riquadroStatistiche.variaMonete(variazione));
	}
	
	public void variaGemme(int variazione) {
		addSprite(riquadroStatistiche.variaGemme(variazione));
	}

	public void variaPunti(int variazione) {
		addSprite(riquadroStatistiche.variaPunti(variazione));
	}

	public void variaIncantesimi(ClassiIncantesimo classeIncantesimo, int variazione) {
		addSprite(riquadroIncantesimi.variaIncantesimi(classeIncantesimo, variazione));
	}

	public void variaPozioniForza(int variazione) {
		addSprite(riquadroIncantesimi.variaPozioniForza(variazione));
	}

	public void variaPozioniMagia(int variazione) {
		addSprite(riquadroIncantesimi.variaPozioniGrandeForza(variazione));
	}

	public void variaPozioniGrandeForza(int variazione) {
		addSprite(riquadroIncantesimi.variaPozioniMagia(variazione));
	}

	public void variaMappa() {
		addSprite(riquadroMappa.variaMappa());
	}

	public void raccogliOggetto() {
		addSprite(riquadroLocazione.raccogliOggetto());
	}
	
	private void addSprite(SpriteInterface sprite) {
		if (sprite != null) {
			sprites.add(sprite);
		}
	}
}
