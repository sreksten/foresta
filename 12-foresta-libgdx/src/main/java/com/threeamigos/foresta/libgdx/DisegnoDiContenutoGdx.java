package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.AtlanteDiImmagini;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.immagini.LetturaPng;
import com.threeamigos.foresta.ui.immagini.SagomeDiPixel;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Il disegno in libGDX degli elementi di un {@link ContenutoDiColonna} (le schermate a tutto schermo: bestiario, intro, messaggi…). Le texture che non sono
 * quelle degli atlanti (testi grandi, sagome, specchi, alone) si fanno una volta per chiave (o per elemento, se non ne hanno una) e restano di
 * {@link ImmaginiGdx} fino al suo <code>dispose</code>.
 */
public final class DisegnoDiContenutoGdx {

	private final ImmaginiGdx immagini;
	private final TestoGdx piccolo;
	private final TestoGdx medio;
	private final Map<String, TextureRegion> perChiave = new HashMap<>();
	private final Map<Elemento, TextureRegion> perElemento = new IdentityHashMap<>();

	public DisegnoDiContenutoGdx(ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		this.immagini = immagini;
		this.piccolo = piccolo;
		this.medio = medio;
	}

	/**
	 * Disegna gli elementi del contenuto con il suo angolo in alto a sinistra in (x0, y0), con quell'opacità (da 0 a 1)
	 */
	public void disegna(Batch batch, ContenutoDiColonna contenuto, int x0, int y0, float opacita) {
		for (Elemento e : contenuto.getElementi()) {
			disegnaElemento(batch, e, x0 + e.getX(), y0 + e.getY(), opacita);
		}
	}

	/**
	 * Disegna un elemento dov'è, con quell'opacità
	 */
	public void disegnaElemento(Batch batch, Elemento e, float opacita) {
		disegnaElemento(batch, e, e.getX(), e.getY(), opacita);
	}

	/**
	 * Disegna un elemento con l'angolo in alto a sinistra in (x, y) (anziché dov'è), con quell'opacità
	 */
	public void disegnaElemento(Batch batch, Elemento e, int x, int y, float opacita) {
		if (opacita <= 0f) {
			return;
		}
		Color precedente = new Color(batch.getColor());
		try {
			switch (e.getTipo()) {
				case RETTANGOLO:
					int argb = e.getArgb();
					Color colore = new Color(((argb & 0xFFFFFF) << 8) | ((argb >>> 24) & 0xFF));
					colore.a *= opacita;
					immagini.riempi(batch, x, y, e.getLarghezza(), e.getAltezza(), colore);
					break;
				case TESTO:
					TestoGdx testo = e.getFont() == piccolo.getFont() ? piccolo : medio;
					int yRiga = y;
					for (String riga : e.getRighe()) {
						testo.disegna(batch, riga, x, yRiga, TestoGdx.coloreConAlfa(e.getColore(), opacita), 1f);
						yRiga += e.getFont().getHeight();
					}
					break;
				case TESTO_GRANDE:
					batch.setColor(1f, 1f, 1f, opacita);
					batch.draw(testoGrande(e), x, y);
					break;
				default:
					batch.setColor(1f, 1f, 1f, opacita);
					int raggio = e.getRaggioDellAlone();
					if (raggio > 0) {
						// Con l'alone l'immagine è più grande di raggio pixel per lato: la si sposta, e il disegno resta al suo posto
						TextureRegion conAlone = immagine(e);
						batch.draw(conAlone, x - raggio, y - raggio);
					} else {
						batch.draw(immagine(e), x, y, e.getLarghezza(), e.getAltezza());
					}
					break;
			}
		} finally {
			batch.setColor(precedente);
		}
	}

	/**
	 * La texture di un testo grande (per chi la vuole disegnare a fette)
	 */
	public TextureRegion testoGrande(Elemento e) {
		if (e.getChiave() != null) {
			return perChiave.computeIfAbsent(e.getChiave(), k -> immagini.regioneDaPixel(e.getPixel(), false));
		}
		return perElemento.computeIfAbsent(e, el -> immagini.regioneDaPixel(el.getPixel(), false));
	}

	/**
	 * L'immagine di un elemento: quella della risorsa, specchiata, ridotta a una sagoma o con l'alone se serve
	 */
	private TextureRegion immagine(Elemento e) {
		if (!e.isSpecchiata() && e.getSagomaRgb() == null && e.getRaggioDellAlone() == 0) {
			return immagini.regione(e.getRisorsa());
		}
		String chiave = e.getRisorsa() + "|" + e.isSpecchiata() + "|" + e.getSagomaRgb() + "|" + e.getRaggioDellAlone();
		return perChiave.computeIfAbsent(chiave, k -> {
			ImmagineDiPixel pixel = AtlanteDiImmagini.cerca(e.getRisorsa());
			if (pixel == null) {
				pixel = LetturaPng.leggiRisorsa("/com/threeamigos/foresta/img/" + e.getRisorsa());
			}
			if (e.getRaggioDellAlone() > 0) {
				// Le immagini a metà risoluzione si ingrandiscono prima: l'alone è di pixel dello schermo
				pixel = SagomeDiPixel.ingrandisci(pixel, LivelloDiZoom.valore());
				pixel = LayoutDelTestoGrande.conAlone(pixel, e.getRaggioDellAlone());
			}
			if (e.getSagomaRgb() != null) {
				pixel = SagomeDiPixel.sagoma(pixel, e.getSagomaRgb());
			}
			if (e.isSpecchiata()) {
				pixel = SagomeDiPixel.specchia(pixel);
			}
			return immagini.regioneDaPixel(pixel, false);
		});
	}
}
