package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.DimensioniRisorsa;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestoGrande;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Il bestiario disegnato con Swing, che si apre dall'inventario (comando MOSTRA_BESTIARIO) e si chiude con ANNULLA: a sinistra l'elenco delle voci, ognuna con
 * la sua icona e il suo nome (o "???" finché non si è sbloccata), a destra la scheda della voce scelta con un clic. Le colonne, lo scorrimento, i gesti e la
 * scelta sono di {@link SchermataBestiario} e i contenuti di {@link ContenutiDelBestiario}, nel modulo comune; qui si disegna.
 */
public class DisplayableCanvasBestiario extends SchermataBestiario {

	private static final float OPACITA_OMBRA = 0.4f;
	private static final Color COLORE_SEPARATORE = new Color(90, 90, 90);

	private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

	private final MisureDelleImmagini misure = new MisureDelleImmagini() {
		@Override
		public Misura personaggio(TipoPersonaggio classe) {
			BufferedImage immagine = MappaPersonaggioImmagine.getImmagine(classe);
			return new Misura(immagine.getWidth(), immagine.getHeight());
		}

		@Override
		public Misura immagine(String risorsa) {
			java.awt.Dimension d = DimensioniRisorsa.di(risorsa);
			return new Misura(d.width, d.height);
		}
	};

	public DisplayableCanvasBestiario(int width, int height, VistaPartita vistaPartita) {
		super(width, height, vistaPartita);
	}

	@Override
	protected MisureDelleImmagini getMisure() {
		return misure;
	}

	@Override
	protected Misura misuraDellImmagine(String risorsa) {
		java.awt.Dimension d = DimensioniRisorsa.di(risorsa);
		return new Misura(d.width, d.height);
	}

	/**
	 * Disegna il titolo, quante voci si sono sbloccate, l'elenco e la scheda.
	 */
	public void disegna(Graphics2D graphics) {
		// L'ombra del drago dietro, più tenue che nella pagina dei trofei: qui ci sono due colonne di testo da leggere
		Composite normale = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITA_OMBRA));
		graphics.drawImage(ImageCache.ombraDelDrago, (width - ImageCache.ombraDelDrago.getWidth()) >> 1,
				(height - ImageCache.ombraDelDrago.getHeight()) >> 1, null);
		graphics.setComposite(normale);
		TestoGrande.disegnaRiga(graphics, getTitolo(), getXDelTitolo(), QUOTA_TITOLO);
		int yRiga = getQuotaDelSottotitolo();
		for (String riga : getSottotitolo()) {
			Image sotto = ImageCache.get(riga, DoomdarkFont.medio(), DoomdarkColorModel.Color.MEDIUM_GRAY);
			graphics.drawImage(sotto, (width - sotto.getWidth(null)) >> 1, yRiga, null);
			yRiga += DoomdarkFont.medio().getHeight();
		}
		Image quante = ImageCache.get(getQuanteSbloccate(), DoomdarkFont.medio(), DoomdarkColorModel.Color.MEDIUM_GRAY);
		graphics.drawImage(quante, width - MARGINE - quante.getWidth(null), QUOTA_TITOLO + 6, null);
		graphics.setColor(COLORE_SEPARATORE);
		Area areaElenco = getAreaElenco();
		int xSeparatore = areaElenco.getX() + areaElenco.getLarghezza() + SPAZIO_FRA_I_PANNELLI / 2;
		graphics.drawLine(xSeparatore, areaElenco.getY(), xSeparatore, areaElenco.getY() + areaElenco.getAltezza());
		if (getElenco() != null) {
			disegnaColonna(graphics, getElenco(), getContenutoElenco());
		}
		if (getScheda() != null) {
			disegnaColonna(graphics, getScheda(), getContenutoScheda());
		}
	}

	private void disegnaColonna(Graphics2D graphics, ColonnaScorrevole colonna, ContenutoDiColonna contenuto) {
		colonna.aggiorna();
		Area area = colonna.getArea();
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.clipRect(area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
			// Il contenuto sta centrato in orizzontale nell'area
			int x0 = area.getX() + (area.getLarghezza() - contenuto.getLarghezza()) / 2;
			int y0 = area.getY() + (int) Math.round(colonna.getY());
			disegno.disegna(g, contenuto, x0, y0);
		} finally {
			g.dispose();
		}
		if (colonna.haAltroSopra()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaSu;
			Area a = colonna.areaDellaFrecciaSu(new Misura(freccia.getWidth(null), freccia.getHeight(null)));
			graphics.drawImage(freccia, a.getX(), a.getY(), null);
		}
		if (colonna.haAltroSotto()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaGiu;
			Area a = colonna.areaDellaFrecciaGiu(new Misura(freccia.getWidth(null), freccia.getHeight(null)));
			graphics.drawImage(freccia, a.getX(), a.getY(), null);
		}
	}
}
