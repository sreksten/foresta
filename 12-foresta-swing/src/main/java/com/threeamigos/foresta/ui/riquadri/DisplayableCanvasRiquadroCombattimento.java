package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Il riquadro del combattimento: tutti i personaggi della locazione, dei due gruppi, nell'ordine in cui agiscono nel round (vedi
 * VistaPartita.getOrdineDiCombattimento). Chi è vivo ha il nome e la barra della salute; chi è vivo ma non combatte (in panchina)
 * ha la barra ma il nome grigio; chi è morto ha solo il nome, in grigio scuro. Sotto il personaggio che agisce c'è l'azione che compie (un
 * solo posto, che scorre con lui).
 * <p>
 * La cornice è quella di sempre, tagliata a metà: la metà alta sta in cima e la metà bassa in fondo, e in mezzo c'è lo sfondo nero con i due
 * bordi laterali. Così il riquadro cresce quanto serve. Sta centrato in orizzontale, sotto l'immagine della locazione; se non ci sta sale
 * quanto basta per non coprire la barra delle icone.
 */
public class DisplayableCanvasRiquadroCombattimento extends RiquadroCombattimento {

	private final BufferedImage cornice;
	// Lo stesso oggetto per tutta la vita del riquadro: il canvas lo registra per il mouse e lo vede crescere
	private final Rectangle rettangolo;

	/**
	 * @param yPreferita dove il riquadro comincia di norma: subito sotto l'immagine della locazione
	 */
	public DisplayableCanvasRiquadroCombattimento(int parentWidth, int parentHeight, int yPreferita, VistaPartita vistaPartita) {
		super(parentWidth, parentHeight, yPreferita, ImageCache.corniceCombattimento.getWidth(), ImageCache.corniceCombattimento.getHeight(), vistaPartita);
		cornice = ImageCache.corniceCombattimento;
		rettangolo = new Rectangle(getArea().getX(), getArea().getY(), getArea().getLarghezza(), getArea().getAltezza());
	}

	/** Il riquadro com'è stato disegnato l'ultima volta (cresce con i personaggi). */
	public Rectangle getRettangolo() {
		return rettangolo;
	}

	/**
	 * Disegna il riquadro con tutti i personaggi del combattimento.
	 */
	public void disegnaInfoCombattimento(Graphics2D graphics) {
		Disposizione d = disponi();
		if (d == null) {
			return;
		}
		Area area = d.getArea();
		rettangolo.setBounds(area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
		final int xOffset = area.getX();
		final int yOffset = area.getY();
		final int larghezza = area.getLarghezza();
		final int altezza = area.getAltezza();

		// Lo sfondo nero e i bordi laterali per primi, poi la cornice: la metà alta sopra e quella bassa sotto
		graphics.setColor(new Color(COLORE_SFONDO));
		graphics.fillRect(xOffset, yOffset, larghezza, altezza);
		graphics.setColor(new Color(COLORE_BORDO));
		graphics.fillRect(xOffset, yOffset, d.getBordoLaterale(), altezza);
		graphics.fillRect(xOffset + larghezza - d.getBordoLaterale(), yOffset, d.getBordoLaterale(), altezza);
		graphics.drawImage(cornice.getSubimage(0, 0, larghezza, d.getMetaAlta()), xOffset, yOffset, null);
		graphics.drawImage(cornice.getSubimage(0, d.getMetaAlta(), larghezza, d.getMetaBassa()), xOffset, yOffset + altezza - d.getMetaBassa(), null);

		DoomdarkFont fontMedium = DoomdarkFont.medio();
		for (Riga riga : d.getRighe()) {
			if (riga.getColoreSegnale() != 0 || riga.isMostraLaBarra()) {
				graphics.setColor(new Color(riga.getColoreSegnale()));
				graphics.fillRect(d.getXSegnale(), riga.getY(), LARGHEZZA_SEGNALE, ALTEZZA_BARRA);
			}
			if (riga.isMostraLaBarra()) {
				graphics.setColor(new Color(COLORE_SFONDO_BARRA));
				graphics.fillRect(d.getXBarra() + riga.getLarghezzaBarraPiena(), riga.getY(), d.getLarghezzaBarra() - riga.getLarghezzaBarraPiena(), ALTEZZA_BARRA);
				graphics.setColor(new Color(riga.getColoreBarra()));
				graphics.fillRect(d.getXBarra(), riga.getY(), riga.getLarghezzaBarraPiena(), ALTEZZA_BARRA);
			}
			for (VoceDiTesto voce : riga.getTesti()) {
				Image testo = ImageCache.get(voce.getTesto(), fontMedium, voce.getColore());
				int x = voce.isAllineataADestra() ? voce.getX() - testo.getWidth(null) : voce.getX();
				if (voce.isConOmbra()) {
					graphics.drawImage(ImageCache.get(voce.getTesto(), fontMedium, DoomdarkColorModel.Color.BLACK),
							x + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA, voce.getY() + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA, null);
				}
				graphics.drawImage(testo, x, voce.getY(), null);
			}
		}
	}
}
