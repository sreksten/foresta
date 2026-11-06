package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.ui.DisplayableCanvas;
import com.threeamigos.foresta.ui.immagini.IconeAwt;

import java.awt.*;

/**
 * La barra delle icone di comando disegnata con Swing: sostituisce le vecchie PannelloIcone/ImageButton, veri componenti Swing che convivevano male con
 * l'animazione continua di DisplayableCanvas (vedi DisplayableCanvas.run). Le icone, lo scorrimento, i tocchi e l'aiuto sono di {@link BarraIcone}, nel
 * modulo comune; qui c'è il disegno ({@link DisegnoBarraIcone}).
 */
public class DisplayableCanvasBarraIcone extends BarraIcone implements BarraIconeDisegnabile {

	private final DisegnoBarraIcone disegno = new DisegnoBarraIcone(this);

	public DisplayableCanvasBarraIcone(int orientamento, int offsetX, int offsetY, int larghezza, int altezza, VistaPartita vistaPartita) {
		super(orientamento == DisplayableCanvas.ORIENTAMENTO_ORIZZONTALE ? Orientamento.ORIZZONTALE : Orientamento.VERTICALE, offsetX, offsetY, larghezza,
				altezza, IconeAwt.getLarghezzaMassima(), IconeAwt.getAltezzaMassima(), vistaPartita);
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
