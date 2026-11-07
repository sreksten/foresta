package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestoGrande;

import java.awt.*;

/**
 * Il grimorio disegnato con Swing, che si apre dall'inventario (comando MOSTRA_GRIMORIO) e si chiude con ANNULLA: a sinistra l'elenco degli incantesimi, a
 * destra la scheda di quello scelto con un clic. Le colonne, lo scorrimento, i gesti e la scelta sono di {@link SchermataGrimorio} e i contenuti di
 * {@link ContenutiDelGrimorio}, nel modulo comune; qui si disegna.
 */
public class DisplayableCanvasGrimorio extends SchermataGrimorio {

	private static final float OPACITA_OMBRA = 0.4f;
	private static final Color COLORE_SEPARATORE = new Color(90, 90, 90);

	private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

	public DisplayableCanvasGrimorio(int width, int height) {
		super(width, height);
	}

	@Override
	protected Misura misuraDellImmagine(String risorsa) {
		return new MisureDaiFile().immagine(risorsa);
	}

	/**
	 * Disegna il titolo, il sottotitolo, l'elenco e la scheda.
	 */
	public void disegna(Graphics2D graphics) {
		// L'ombra del drago dietro, tenue come nel bestiario: ci sono due colonne di testo da leggere
		Composite normale = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACITA_OMBRA));
		graphics.drawImage(ImageCache.ombraDelDrago, (width - ImageCache.ombraDelDrago.getWidth()) >> 1,
				(height - ImageCache.ombraDelDrago.getHeight()) >> 1, null);
		graphics.setComposite(normale);
		TestoGrande.disegnaRiga(graphics, getTitolo(), getXDelTitolo(), SchermataBestiario.QUOTA_TITOLO);
		int yRiga = getQuotaDelSottotitolo();
		for (String riga : getSottotitolo()) {
			Image sotto = ImageCache.get(riga, DoomdarkFont.medio(), DoomdarkColorModel.Color.MEDIUM_GRAY);
			graphics.drawImage(sotto, (width - sotto.getWidth(null)) >> 1, yRiga, null);
			yRiga += DoomdarkFont.medio().getHeight();
		}
		graphics.setColor(COLORE_SEPARATORE);
		Area areaElenco = getAreaElenco();
		int xSeparatore = areaElenco.getX() + areaElenco.getLarghezza() + SchermataBestiario.SPAZIO_FRA_I_PANNELLI / 2;
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
