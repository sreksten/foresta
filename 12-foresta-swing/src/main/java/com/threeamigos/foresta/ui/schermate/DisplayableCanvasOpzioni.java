package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.IconeAwt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * La schermata delle opzioni disegnata con Swing: le righe, i comandi dei pulsanti e la riga che aspetta una scelta sono del modulo comune
 * ({@link SchermataOpzioni}); qui i pulsanti sono le icone come nella barra, e il titolo e le scritte si disegnano con {@link DisegnoDiContenutoAwt}.
 */
public class DisplayableCanvasOpzioni extends SchermataOpzioni {

	private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

	public DisplayableCanvasOpzioni(int width, int height) {
		super(width, height, misuraDelPulsante());
	}

	private static Misura misuraDelPulsante() {
		BufferedImage icona = IconeAwt.icona(TipoIcona.OPZIONI);
		return new Misura(icona.getWidth(), icona.getHeight());
	}

	public void disegna(Graphics2D graphics) {
		for (Riga riga : getRighe()) {
			graphics.drawImage(IconeAwt.icona(riga.getIcona()), riga.getPulsante().getX(), riga.getPulsante().getY(), null);
		}
		disegno.disegna(graphics, contenuto(), 0, 0);
	}
}
