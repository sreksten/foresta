package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ScorrimentoVerticale;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.TestoGrande;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * La pagina dei trofei, che si apre dall'inventario (comando MOSTRA_TROFEI) e si chiude con ANNULLA, disegnata con Swing: il titolo in cima e sotto tutti i
 * trofei (vedi ImmagineTrofei), che si scorrono a mano come il riquadro delle missioni, con la rotella o con le frecce, a passi più lunghi perché l'elenco è
 * lungo. Lo scorrimento, i gesti e le frecce sono di {@link SchermataTrofei}, nel modulo comune.
 */
public class DisplayableCanvasTrofei extends SchermataTrofei {

	private ScorrimentoVerticale scorrimento;

	public DisplayableCanvasTrofei(int width, int height, VistaPartita vistaPartita) {
		super(width, height, vistaPartita);
	}

	@Override
	protected Misura misuraDellImmagine(String risorsa) {
		// Una volta sola per risorsa: questo metodo si chiama a ogni ridisegno
		return BufferedImageBuilder.misura(risorsa);
	}

	@Override
	protected void paginaAperta(PaginaTrofei pagina) {
		scorrimento = new ScorrimentoVerticale(ImmagineTrofei.disegna(pagina), getColonna());
	}

	/**
	 * Disegna il titolo e i trofei che scorrono.
	 */
	public void disegna(Graphics2D graphics) {
		graphics.drawImage(ImageCache.ombraDelDrago, (width - ImageCache.ombraDelDrago.getWidth()) >> 1,
				(height - ImageCache.ombraDelDrago.getHeight()) >> 1, null);
		TestoGrande.disegnaRiga(graphics, getTitolo(), getXDelTitolo(), QUOTA_TITOLO);
		if (scorrimento != null) {
			scorrimento.disegna(graphics, 1);
			scorrimento.disegnaFrecce(graphics);
		}
	}
}
