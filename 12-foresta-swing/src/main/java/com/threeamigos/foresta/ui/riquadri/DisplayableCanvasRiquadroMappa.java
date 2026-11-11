package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneConoscenzaMappa;
import com.threeamigos.foresta.mappa.VistaMappa;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.schermate.DisegnatoreMappa;
import com.threeamigos.foresta.ui.sfx.GestoreNuvole;
import com.threeamigos.foresta.ui.sfx.NuvoleAwt;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * La minimappa della schermata di gioco: le caselle conosciute attorno al gruppo.
 */
public class DisplayableCanvasRiquadroMappa extends DisegnatoreMappa implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA = RiquadroMappa.BORDO_INTERNO;

	// La finestra, le tessere e il segnalino sono del modulo comune (RiquadroMappa); qui si blocca l'immagine della mappa già composta (vedi
	// DisegnatoreMappa.ottieniMappaGenerale)
	// Anche la posizione: sul telefono la mappa è impilata e la pila la sposta in verticale
	private final RiquadroMappa modello;
	private final int larghezzaSchermo;
	private final int altezzaSchermo;

	private final int larghezzaRiquadroMappa;

	public DisplayableCanvasRiquadroMappa(int topLeftX, int topLeftY, int larghezzaSchermo, int altezzaSchermo,
								   VistaPartita vistaPartita) {
		super(vistaPartita);
		this.larghezzaSchermo = larghezzaSchermo;
		this.altezzaSchermo = altezzaSchermo;
		modello = new RiquadroMappa(topLeftX, topLeftY, LARGHEZZA_ICONA, ALTEZZA_ICONA, vistaPartita);
		larghezzaRiquadroMappa = modello.getLatoDellaMappa();

		registratiAEventi();
	}

	public RiquadroMappa getModello() {
		return modello;
	}

	private void registratiAEventi() {
		BusEventi.iscriviti(NotificaVariazioneConoscenzaMappa.class, this::gestisciEventoVariazioneMappa);
	}

	/**
	 * Disegna la porzione di mappa attorno al gruppo.
	 */
	public void disegnaMappa(Graphics2D graphics) {
		int topLeftX = modello.getTopLeftX();
		int topLeftY = modello.getTopLeftY();
		graphics.drawImage(ImageCache.corniceMappa, topLeftX, topLeftY, null);
		VistaMappa mappa = vistaPartita.getMappa();
		RiquadroMappa.Veduta veduta = modello.veduta();
		int daX = veduta.getDaX();
		int daY = veduta.getDaY();
		int localXOffset = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA;
		int localYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA;

		graphics.drawImage(ottieniMappaGenerale(),
				localXOffset, localYOffset, localXOffset + larghezzaRiquadroMappa, localYOffset + larghezzaRiquadroMappa,
				daX * LARGHEZZA_ICONA, daY * ALTEZZA_ICONA, (daX + RiquadroMappa.LATO) * LARGHEZZA_ICONA, (daY + RiquadroMappa.LATO) * ALTEZZA_ICONA, null);

		if (isSegnaliniVisibili()) {
			graphics.drawImage(ImageCache.segnalino, veduta.getSegnalino().x, veduta.getSegnalino().y, null);
			for (Punto indicatore : veduta.getIndicatori()) {
				graphics.drawImage(ImageCache.indicatore, indicatore.x, indicatore.y, null);
			}
		}

		// Salva lo stato originale della Clip e del Composite
		Shape originalClip = graphics.getClip();
		Composite originalComposite = graphics.getComposite();

		// Applica la clip: da adesso in poi si colora SOLO dentro il quadrato della mappa
		Rectangle areaDelleNuvole = areaDelleNuvole(topLeftX, topLeftY, larghezzaRiquadroMappa);
		graphics.clipRect(areaDelleNuvole.x, areaDelleNuvole.y, areaDelleNuvole.width, areaDelleNuvole.height);

		// Imposta la trasparenza e disegna le nuvole condivise con la mappa a tutto schermo
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
		GestoreNuvole.assicuraGenerate(mappa, larghezzaSchermo, altezzaSchermo, LARGHEZZA_ICONA, ALTEZZA_ICONA);
		NuvoleAwt.disegna(graphics, daX, daY, localXOffset, localYOffset, LARGHEZZA_ICONA, ALTEZZA_ICONA);

		// Ripristina la clip e la trasparenza per i disegni successivi
		graphics.setComposite(originalComposite);
		graphics.setClip(originalClip);

		// Aggiorna la posizione delle nuvolette
		GestoreNuvole.aggiorna();
	}

	/**
	 * Dove si vedono le nuvole: il quadrato della mappa dentro la cornice, cioè spostato del bordo interno rispetto all'angolo in alto a
	 * sinistra del riquadro, in x con la x dell'angolo e in y con la sua y (che di solito sono diverse).
	 */
	static Rectangle areaDelleNuvole(int topLeftX, int topLeftY, int larghezzaRiquadroMappa) {
		com.threeamigos.foresta.ui.Area area = RiquadroMappa.areaDelleNuvole(topLeftX, topLeftY, larghezzaRiquadroMappa);
		return new Rectangle(area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
	}

	private void gestisciEventoVariazioneMappa(NotificaVariazioneConoscenzaMappa evento) {
		BusEventi.pubblica(new InternoCreazioneSpriteATempo(variaMappa()));
	}

	private DescrizioneSpriteATempo variaMappa() {
		return modello.spriteDellaMappaVariata(new Misura(ImageCache.spriteMappa.getWidth(), ImageCache.spriteMappa.getHeight()),
				new Misura(ImageCache.corniceMappa.getWidth(), ImageCache.corniceMappa.getHeight()));
	}
}
