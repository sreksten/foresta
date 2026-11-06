package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.schermate.PaginaTrofei;
import com.threeamigos.foresta.ui.schermate.SchermataTrofei;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;

import java.util.ArrayList;
import java.util.List;

/**
 * La pagina dei trofei in libGDX: lo scorrimento, i gesti, le frecce e come sono disposti i trofei sono del modulo comune ({@link SchermataTrofei},
 * {@link PaginaTrofei}: gli stessi di Swing); qui c'è il disegno: l'ombra del drago sul fondo, il titolo nell'alfabeto grande, e i trofei ritagliati alla
 * colonna. Le immagini dei nomi si preparano all'apertura e restano di {@link ImmaginiGdx} fino al suo <code>dispose</code>.
 */
public final class SchermataTrofeiGdx extends SchermataTrofei {

	private static final String OMBRA_DEL_DRAGO = "fondi/OmbraDelDrago.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final TestoGrandeGdx testoGrande;
	private final List<TextureRegion> nomi = new ArrayList<>();
	private TextureRegion titolo;

	public SchermataTrofeiGdx(int width, int height, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx medio, TestoGrandeGdx testoGrande) {
		super(width, height, vistaPartita);
		this.immagini = immagini;
		this.medio = medio;
		this.testoGrande = testoGrande;
	}

	@Override
	protected Misura misuraDellImmagine(String risorsa) {
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	@Override
	protected void paginaAperta(PaginaTrofei pagina) {
		nomi.clear();
		for (PaginaTrofei.Voce voce : pagina.getVoci()) {
			nomi.add(immagini.regioneDaPixel(voce.getNome(), false));
		}
		titolo = testoGrande.immagine(getTitolo(), LayoutDelTestoGrande.larghezza(getTitolo()), false, 0);
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		float zoom = (float) LivelloDiZoom.valore();
		int larghezzaOmbra = immagini.larghezza(OMBRA_DEL_DRAGO, zoom);
		int altezzaOmbra = immagini.altezza(OMBRA_DEL_DRAGO, zoom);
		immagini.disegna(batch, OMBRA_DEL_DRAGO, (width - larghezzaOmbra) >> 1, (height - altezzaOmbra) >> 1, zoom);
		if (titolo != null) {
			batch.draw(titolo, getXDelTitolo(), QUOTA_TITOLO);
		}
		ColonnaScorrevole colonna = getColonna();
		PaginaTrofei pagina = getPagina();
		if (colonna == null || pagina == null) {
			return;
		}
		colonna.aggiorna();
		Area area = colonna.getArea();
		DoomdarkFont font = medio.getFont();
		int x0 = area.getX() + (area.getLarghezza() - pagina.getLarghezza()) / 2;
		int y0 = area.getY() + (int) Math.round(colonna.getY());
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
		for (int i = 0; i < pagina.getVoci().size(); i++) {
			PaginaTrofei.Voce voce = pagina.getVoci().get(i);
			batch.draw(nomi.get(i), x0, y0 + voce.getYDelNome());
			int y = y0 + voce.getYDellaDescrizione();
			for (String riga : voce.getRigheDellaDescrizione()) {
				medio.disegna(batch, riga, x0, y, voce.getColore());
				y += font.getHeight();
			}
			medio.disegna(batch, voce.getQuantita(), x0 + voce.getXDellaQuantita(), y0 + voce.getYDellaDescrizione(), voce.getColore());
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
		if (colonna.haAltroSopra()) {
			Area a = colonna.areaDellaFrecciaSu(misuraDellImmagine(NomiDelleImmagini.FRECCIA_SU));
			immagini.disegna(batch, NomiDelleImmagini.FRECCIA_SU, a.getX(), a.getY(), 1f);
		}
		if (colonna.haAltroSotto()) {
			Area a = colonna.areaDellaFrecciaGiu(misuraDellImmagine(NomiDelleImmagini.FRECCIA_GIU));
			immagini.disegna(batch, NomiDelleImmagini.FRECCIA_GIU, a.getX(), a.getY(), 1f);
		}
	}
}
