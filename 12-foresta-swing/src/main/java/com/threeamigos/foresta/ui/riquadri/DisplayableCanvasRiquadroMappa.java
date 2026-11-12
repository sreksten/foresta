package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.TipoFinestra;
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
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * La minimappa della schermata di gioco: le caselle conosciute attorno al gruppo.
 */
public class DisplayableCanvasRiquadroMappa extends DisegnatoreMappa implements Finestra {

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
		if (modello.isNellaPila()) {
			// Sul telefono: la cornice grande degli altri riquadri impilati, con l'intestazione
			graphics.drawImage(ImageCache.corniceGrande, topLeftX, topLeftY, null);
			IntestazioneRiquadro.disegna(graphics, TestiDellaUI.get("RIQUADRO_MAPPA"), topLeftX, topLeftY, ImageCache.corniceGrande.getWidth());
		} else {
			graphics.drawImage(ImageCache.corniceMappa, topLeftX, topLeftY, null);
		}
		VistaMappa mappa = vistaPartita.getMappa();
		RiquadroMappa.Veduta veduta = modello.veduta();
		int daX = veduta.getDaX();
		int daY = veduta.getDaY();
		com.threeamigos.foresta.ui.Area areaDellaMappa = modello.getAreaDellaMappa();
		int localXOffset = areaDellaMappa.getX();
		int localYOffset = areaDellaMappa.getY();

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
		graphics.clipRect(localXOffset, localYOffset, areaDellaMappa.getLarghezza(), areaDellaMappa.getAltezza());

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
		// Sul telefono la mappa è nella pila: lo sprite resta sotto i riquadri che le passano davanti
		BusEventi.pubblica(new InternoCreazioneSpriteATempo(variaMappa().delRiquadro(TipoFinestra.MAPPA)));
	}

	private DescrizioneSpriteATempo variaMappa() {
		BufferedImage cornice = modello.isNellaPila() ? ImageCache.corniceGrande : ImageCache.corniceMappa;
		return modello.spriteDellaMappaVariata(new Misura(ImageCache.spriteMappa.getWidth(), ImageCache.spriteMappa.getHeight()),
				new Misura(cornice.getWidth(), cornice.getHeight()));
	}
}
