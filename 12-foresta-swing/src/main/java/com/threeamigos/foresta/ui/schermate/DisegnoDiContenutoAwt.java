package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Il disegno con Graphics2D degli elementi di un {@link ContenutoDiColonna} (le schermate a tutto schermo: bestiario, intro, messaggi…). Le immagini che non
 * sono testo (testi grandi, sagome, specchi, alone) si preparano una volta per elemento: finché il contenuto è lo stesso, gli elementi sono gli stessi.
 */
final class DisegnoDiContenutoAwt {

	// Le immagini degli elementi che non sono testo, preparate una volta per elemento (le sagome, gli specchi e gli aloni costano a ogni fotogramma)
	private final Map<Elemento, Image> pronte = new IdentityHashMap<>();

	/**
	 * Disegna gli elementi del contenuto con il suo angolo in alto a sinistra in (x0, y0)
	 */
	void disegna(Graphics2D graphics, ContenutoDiColonna contenuto, int x0, int y0) {
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			for (Elemento e : contenuto.getElementi()) {
				disegnaElemento(g, e, x0 + e.getX(), y0 + e.getY());
			}
		} finally {
			g.dispose();
		}
	}

	/**
	 * Disegna un elemento (un'immagine, per esempio un logo con l'alone) dov'è, con quell'opacità (da 0 a 1)
	 */
	void disegnaConOpacita(Graphics2D graphics, Elemento e, float opacita) {
		if (opacita <= 0) {
			return;
		}
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, opacita)));
			disegnaElemento(g, e, e.getX(), e.getY());
		} finally {
			g.dispose();
		}
	}

	/**
	 * Il contenuto intero in un'immagine, per farla scorrere (vedi ScorrimentoVerticale)
	 */
	BufferedImage comeImmagine(ContenutoDiColonna contenuto) {
		BufferedImage immagine = new BufferedImage(contenuto.getLarghezza(), contenuto.getAltezza(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		try {
			disegna(g, contenuto, 0, 0);
		} finally {
			g.dispose();
		}
		return immagine;
	}

	private void disegnaElemento(Graphics2D g, Elemento e, int x, int y) {
		switch (e.getTipo()) {
			case RETTANGOLO:
				g.setColor(new Color(e.getArgb(), true));
				g.fillRect(x, y, e.getLarghezza(), e.getAltezza());
				break;
			case TESTO:
				int yRiga = y;
				for (String riga : e.getRighe()) {
					g.drawImage(ImageCache.get(riga, e.getFont(), e.getColore()), x, yRiga, null);
					yRiga += e.getFont().getHeight();
				}
				break;
			case TESTO_GRANDE:
				g.drawImage(pronte.computeIfAbsent(e, el -> ImmaginiAwt.buffered(el.getPixel())), x, y, null);
				break;
			default:
				Image immagine = pronte.computeIfAbsent(e, DisegnoDiContenutoAwt::preparaLImmagine);
				int raggio = e.getRaggioDellAlone();
				if (raggio > 0) {
					// Con l'alone l'immagine è più grande di raggio pixel per lato: la si sposta, e il disegno resta al suo posto
					g.drawImage(immagine, x - raggio, y - raggio, null);
				} else {
					g.drawImage(immagine, x, y, e.getLarghezza(), e.getAltezza(), null);
				}
				break;
		}
	}

	/**
	 * L'immagine di un elemento: quella della risorsa (con lo zoom, per i personaggi, i fondi e i loghi), specchiata, ridotta a una sagoma o con l'alone se serve
	 */
	private static Image preparaLImmagine(Elemento e) {
		String risorsa = e.getRisorsa();
		boolean conZoom = risorsa.startsWith("personaggi/") || risorsa.startsWith("fondi/") || !risorsa.contains("/");
		BufferedImage immagine = BufferedImageBuilder.buildBufferedImage(risorsa, conZoom ? LivelloDiZoom.valore() : 1.0);
		if (e.isSpecchiata()) {
			immagine = ImmaginiAwt.specchia(immagine);
		}
		if (e.getSagomaRgb() != null) {
			immagine = ImageCache.sagomaScura(immagine, new Color(e.getSagomaRgb()));
		}
		if (e.getRaggioDellAlone() > 0) {
			immagine = ImmaginiAwt.buffered(LayoutDelTestoGrande.conAlone(ImmaginiAwt.pixel(immagine), e.getRaggioDellAlone()));
		}
		return immagine;
	}
}
