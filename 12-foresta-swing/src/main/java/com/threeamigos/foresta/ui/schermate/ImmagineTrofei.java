package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Tutti i trofei in un'unica immagine, da far scorrere (vedi ScorrimentoVerticale): nell'intro, e nella pagina dei trofei che si apre dall'inventario.
 * Com'è fatta (dove sta ogni parte, che colore ha) lo dice {@link PaginaTrofei}, nel modulo comune; qui si disegna.
 */
final class ImmagineTrofei {

	private ImmagineTrofei() {
	}

	/**
	 * L'immagine dei trofei così come sono adesso, larga quanto chiesto; con il titolo "trofei" in cima, se serve.
	 */
	static BufferedImage costruisci(VistaPartita vistaPartita, int larghezza, boolean conTitolo) {
		return disegna(PaginaTrofei.crea(vistaPartita, larghezza, conTitolo));
	}

	/**
	 * L'immagine di una pagina di trofei
	 */
	static BufferedImage disegna(PaginaTrofei pagina) {
		DoomdarkFont font = DoomdarkFont.medio();
		BufferedImage immagine = new BufferedImage(Math.max(1, pagina.getLarghezza()), Math.max(1, pagina.getAltezza()), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = immagine.createGraphics();
		try {
			if (pagina.getTitolo() != null) {
				graphics.drawImage(ImmaginiAwt.buffered(pagina.getTitolo()), 0, 0, null);
			}
			for (PaginaTrofei.Voce voce : pagina.getVoci()) {
				graphics.drawImage(ImmaginiAwt.buffered(voce.getNome()), 0, voce.getYDelNome(), null);
				// La descrizione sale a coprire un quarto di lettera del nome, e la quantità le sta accanto, a destra
				int y = voce.getYDellaDescrizione();
				for (String riga : voce.getRigheDellaDescrizione()) {
					graphics.drawImage(ImageCache.get(riga, font, voce.getColore()), 0, y, null);
					y += font.getHeight();
				}
				graphics.drawImage(ImageCache.get(voce.getQuantita(), font, voce.getColore()), voce.getXDellaQuantita(), voce.getYDellaDescrizione(), null);
			}
		} finally {
			graphics.dispose();
		}
		return immagine;
	}
}
