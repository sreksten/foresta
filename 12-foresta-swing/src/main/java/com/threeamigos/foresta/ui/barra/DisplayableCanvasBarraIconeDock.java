package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.ui.immagini.IconeAwt;

import java.awt.*;

/**
 * La barra delle icone come il Dock di macOS, disegnata con Swing: la geometria (l'icona sotto il cursore si ingrandisce, le vicine un po' meno) è di
 * {@link BarraIconeDock}, nel modulo comune; qui c'è il disegno ({@link DisegnoBarraIcone}).
 */
public class DisplayableCanvasBarraIconeDock extends BarraIconeDock implements BarraIconeDisegnabile {

	private final DisegnoBarraIcone disegno = new DisegnoBarraIcone(this);

	public DisplayableCanvasBarraIconeDock(Orientamento orientamento, int offsetX, int offsetY, int larghezza, int altezza, VistaPartita vistaPartita) {
		super(orientamento, offsetX, offsetY, larghezza, altezza, IconeAwt.getLarghezzaMassima(), IconeAwt.getAltezzaMassima(), vistaPartita);
	}

	@Override
	public void disegna(Graphics2D graphics) {
		disegno.disegna(graphics);
	}

	@Override
	public void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		disegno.disegnaAiuto(graphics, larghezzaSchermo, altezzaSchermo);
	}
}
