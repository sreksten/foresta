package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Notizie;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.sfx.CloudManager;

import java.awt.*;

class DisplayableCanvasMappaATuttoSchermo extends DisegnatoreMappa implements Finestra {

	// Il notiziario si ferma a metà della larghezza della sfera magica: l'altra
	// metà è coperta dalla sfera stessa (disegnata in primo piano sull'angolo
	// inferiore sinistro di tutto il canvas, vedi DisplayableCanvas.disegnaSferaMagica),
	// ma essendo un'immagine con trasparenza le notizie continuano a intravedersi
	// dietro di essa fino al centro, dove l'immagine diventa opaca.
	private static final int LARGHEZZA_SFERA_MAGICA_COPERTA = ImageCache.sferaMagica.getWidth() / 2;

	private final int width;
	private final int height;
	private final Notiziario notiziario;
	private int mappaXOffset;
	private int mappaYOffset;

	private boolean stoTrascinando;
	private int ultimaXMouse;
	private int ultimaYMouse;

	DisplayableCanvasMappaATuttoSchermo(int width, int height) {
		this.width = width;
		this.height = height;
		this.notiziario = new Notiziario(width - LARGHEZZA_SFERA_MAGICA_COPERTA, ALTEZZA_ICONA);
	}

	// La fascia del notiziario occupa spazio solo quando ci sono effettivamente
	// notizie da mostrare: altrimenti la mappa può usare tutta l'altezza disponibile.
	private int altezzaMappa() {
		return Notizie.getUltimeNotizie().isEmpty() ? height : height - ALTEZZA_ICONA;
	}

	void centraSuGiocatore() {
		// Ho una mappa che può essere più o meno grande rispetto a uno schermo.
		// Le dimensioni dello schermo sono width e height.
		// L'immagine che rappresenta la mappa ha dimensioni:
		int dimensioneMappaX = Foresta.getDimensioneX() * LARGHEZZA_ICONA;
		int dimensioneMappaY = Foresta.getDimensioneY() * ALTEZZA_ICONA;
		// Il giocatore rappresentato sulla mappa si trova in posizione:
		int posizioneXGiocatoreSuMappa = GruppoGiocatore.getIstanza().getX() * LARGHEZZA_ICONA +
				LARGHEZZA_ICONA / 2;
		int posizioneYGiocatoreSuMappa = GruppoGiocatore.getIstanza().getY() * ALTEZZA_ICONA +
				ALTEZZA_ICONA / 2;
		// Vogliamo rappresentare la mappa a video inizialmente con il giocatore
		// posizionato al centro.
		if (dimensioneMappaX <= width) {
			// Se la larghezza della mappa è inferiore a quella dello schermo allora
			// l'offset x è centrato rispetto allo schermo.
			mappaXOffset = (width - dimensioneMappaX) / 2;
		} else {
			// 1. Calcoliamo la posizione teorica per mettere il giocatore al centro esatto della width dello schermo
			int offsetTeoricoX = (width / 2) - posizioneXGiocatoreSuMappa;

			// 2. Blocchiamo l'offset in modo che non superi lo 0 (bordo sinistro)
			// e non scenda sotto la differenza minima (bordo destro)
			mappaXOffset = Math.max(width - dimensioneMappaX, Math.min(0, offsetTeoricoX));
		}

		int altezzaMappa = altezzaMappa();
		if (dimensioneMappaY <= altezzaMappa) {
			// Se l'altezza della mappa è inferiore a quella dell'area disponibile allora
			// l'offset y è centrato rispetto ad essa.
			mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
		} else {
			// 1. Calcoliamo la posizione teorica per mettere il giocatore al centro esatto dell'area disponibile
			int offsetTeoricoY = (altezzaMappa / 2) - posizioneYGiocatoreSuMappa;

			// 2. Blocchiamo l'offset in modo che non superi lo 0 (bordo superiore)
			// e non scenda sotto la differenza minima (bordo inferiore)
			mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, offsetTeoricoY));
		}
	}

	/**
	 * Il segnalino del gruppo, acceso un secondo sì e uno no, sulla casella lasciata
	 * vuota nell'immagine della mappa.
	 */
	private void disegnaSegnalino(Graphics2D graphics) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		graphics.drawImage(ImageCache.segnalino,
				mappaXOffset + gruppo.getX() * LARGHEZZA_ICONA,
				mappaYOffset + gruppo.getY() * ALTEZZA_ICONA, null);
	}

	/**
	 * Le caselle segnalate (artefatti di cui si è saputo, bersagli di missione), lampeggianti
	 * come il segnalino del gruppo: vanno ridisegnate a ogni frame perché non fanno parte
	 * dell'immagine statica della mappa (vedi DisegnatoreMappa.ottieniMappaGenerale).
	 */
	private void disegnaIndicatori(Graphics2D graphics) {
		for (CoordinateMD coordinate : Foresta.getCoordinateDaSegnalare()) {
			graphics.drawImage(ImageCache.indicatore,
					mappaXOffset + coordinate.getX() * LARGHEZZA_ICONA,
					mappaYOffset + coordinate.getY() * ALTEZZA_ICONA, null);
		}
	}

	void disegnaMappaATuttoSchermo(Graphics2D graphics) {

		int altezzaMappa = altezzaMappa();
		int dimensioneMappaX = Foresta.getDimensioneX() * LARGHEZZA_ICONA;
		int dimensioneMappaY = Foresta.getDimensioneY() * ALTEZZA_ICONA;

		// L'altezza disponibile può essere cambiata da un frame all'altro (es. è appena
		// arrivata la prima notizia, o si è svuotato il notiziario): l'offset va rivalidato
		// prima di disegnare, con lo stesso clamping usato durante il trascinamento.
		if (dimensioneMappaY <= altezzaMappa) {
			mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
		} else {
			mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, mappaYOffset));
		}

		// Salva lo stato originale della Clip e del Composite
		Shape originalClip = graphics.getClip();
		Composite originalComposite = graphics.getComposite();

		// Limita il disegno della mappa (immagine + nuvole) alla sola fascia sopra il
		// notiziario, così non può mai debordare nella fascia in basso
		graphics.clipRect(0, 0, width, altezzaMappa);

		graphics.drawImage(ottieniMappaGenerale(), mappaXOffset, mappaYOffset, null);

		if (isSegnaliniVisibili()) {
			disegnaSegnalino(graphics);
			disegnaIndicatori(graphics);
		}

		CloudManager.assicuraGenerate(width, height, LARGHEZZA_ICONA, ALTEZZA_ICONA);

		// Applica la clip sull'area occupata dalla mappa, in modo che le nuvole non
		// vengano disegnate al di fuori di essa quando la mappa è più piccola dello schermo
		graphics.clipRect(mappaXOffset, mappaYOffset, dimensioneMappaX, dimensioneMappaY);

		// Imposta la trasparenza e disegna le nuvole condivise con il riquadro mappa,
		// allineate rispetto alla cella (0,0) della foresta che qui corrisponde sempre
		// all'angolo in alto a sinistra dell'immagine disegnata a (mappaXOffset, mappaYOffset)
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
		CloudManager.disegna(graphics, 0, 0, mappaXOffset, mappaYOffset, LARGHEZZA_ICONA, ALTEZZA_ICONA);

		// Ripristina la clip e il composite originali
		graphics.setComposite(originalComposite);
		graphics.setClip(originalClip);

		// Aggiorna la posizione delle nuvolette
		CloudManager.aggiorna();

		if (altezzaMappa < height) {
			notiziario.disegna(graphics, LARGHEZZA_SFERA_MAGICA_COPERTA, altezzaMappa);
		}
	}

	@Override
	public void processaPressione(int x, int y, Finestra.Tasto tasto) {
		if (tasto == Tasto.SINISTRO && y < altezzaMappa()) {
			stoTrascinando = true;
			ultimaXMouse = x;
			ultimaYMouse = y;
		}
	}

	@Override
	public void processaRilascio(int x, int y, Finestra.Tasto tasto) {
		if (tasto == Tasto.SINISTRO) {
			stoTrascinando = false;
		}
	}

	@Override
	public void processaUscita(int x, int y) {
		// Uscendo dal riquadro (sulla barra icone o fuori dal canvas) il rilascio del tasto potrebbe
		// arrivare a un'altra finestra o andare perso: si chiude il trascinamento come se fosse avvenuto qui
		stoTrascinando = false;
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		if (stoTrascinando && y >= altezzaMappa()) {
			// Sceso sul notiziario: si esce dalla zona della mappa, come per processaUscita
			stoTrascinando = false;
		}
		if (stoTrascinando) {
			// 1. Calcoliamo il delta (differenza rispetto alla posizione precedente)
			int deltaX = x - ultimaXMouse;
			int deltaY = y - ultimaYMouse;

			// Aggiorniamo la posizione precedente
			ultimaXMouse = x;
			ultimaYMouse = y;

			// 2. Applichiamo lo spostamento del mouse: il punto della mappa sotto il
			// cursore deve restare sotto il cursore, quindi l'offset segue il delta.
			mappaXOffset += deltaX;
			mappaYOffset += deltaY;

			// Ricalcoliamo al volo le dimensioni reali della mappa
			int dimensioneMappaX = Foresta.getDimensioneX() * LARGHEZZA_ICONA;
			int dimensioneMappaY = Foresta.getDimensioneY() * ALTEZZA_ICONA;

			// 3. APPLICAZIONE DEL CLAMPING SULL'ASSE X
			if (dimensioneMappaX <= width) {
				// Se la mappa è più piccola dello schermo, costringiamo l'offset al centro fisso
				mappaXOffset = (width - dimensioneMappaX) / 2;
			} else {
				// Se è più grande, impediamo di trascinare oltre il bordo sinistro (0) o destro (width - dimensioneMappaX)
				mappaXOffset = Math.max(width - dimensioneMappaX, Math.min(0, mappaXOffset));
			}

			// 4. APPLICAZIONE DEL CLAMPING SULL'ASSE Y
			int altezzaMappa = altezzaMappa();
			if (dimensioneMappaY <= altezzaMappa) {
				// Se la mappa è più bassa dell'area disponibile, la costringiamo al centro fisso
				mappaYOffset = (altezzaMappa - dimensioneMappaY) / 2;
			} else {
				// Se è più grande, impediamo di trascinare oltre il bordo superiore (0) o inferiore (altezzaMappa - dimensioneMappaY)
				mappaYOffset = Math.max(altezzaMappa - dimensioneMappaY, Math.min(0, mappaYOffset));
			}
		}
	}

	@Override
	public boolean gestisceDoppioClick() {
		return true;
	}

	@Override
	public void processaDoppioClick(int x, int y, Finestra.Tasto tasto) {
		centraSuGiocatore();
	}
}
