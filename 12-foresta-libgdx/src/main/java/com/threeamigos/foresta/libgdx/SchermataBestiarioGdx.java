package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.AtlanteDiImmagini;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.SagomeDiPixel;
import com.threeamigos.foresta.ui.schermate.ContenutiDelBestiario;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.SchermataBestiario;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.util.HashMap;
import java.util.Map;

/**
 * Il bestiario in libGDX: le colonne, lo scorrimento, i gesti e la scelta sono del modulo comune ({@link SchermataBestiario}, {@link ContenutiDelBestiario}:
 * gli stessi di Swing); qui c'è il disegno: l'ombra del drago, il titolo, il sottotitolo, quante voci si sono sbloccate e le due colonne ritagliate alla loro
 * area, con le frecce. Le texture delle sagome, delle immagini specchiate e dei nomi grandi si fanno una volta per chiave e restano di {@link ImmaginiGdx} fino
 * al suo <code>dispose</code>.
 */
public final class SchermataBestiarioGdx extends SchermataBestiario {

	private static final String OMBRA_DEL_DRAGO = "fondi/OmbraDelDrago.png";
	private static final float OPACITA_OMBRA = 0.4f;
	private static final Color COLORE_SEPARATORE = new Color(90 / 255f, 90 / 255f, 90 / 255f, 1f);

	private final ImmaginiGdx immagini;
	private final TestoGdx piccolo;
	private final TestoGdx medio;
	private final DisegnoDiContenutoGdx disegno;
	private final float zoom = (float) LivelloDiZoom.valore();

	private final MisureDelleImmagini misure = new MisureDelleImmagini() {
		@Override
		public Misura personaggio(TipoPersonaggio classe) {
			TextureRegion regione = immagini.regione(NomiDelleImmagini.personaggio(classe));
			return new Misura(Math.round(regione.getRegionWidth() * zoom), Math.round(regione.getRegionHeight() * zoom));
		}

		@Override
		public Misura immagine(String risorsa) {
			return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
		}
	};

	public SchermataBestiarioGdx(int width, int height, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		super(width, height, vistaPartita);
		this.immagini = immagini;
		this.piccolo = piccolo;
		this.medio = medio;
		this.disegno = new DisegnoDiContenutoGdx(immagini, piccolo, medio);
	}

	@Override
	protected MisureDelleImmagini getMisure() {
		return misure;
	}

	@Override
	protected Misura misuraDellImmagine(String risorsa) {
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport, com.threeamigos.foresta.libgdx.TestoGrandeGdx testoGrande) {
		// L'ombra del drago dietro, più tenue che nella pagina dei trofei: qui ci sono due colonne di testo da leggere
		Color precedente = new Color(batch.getColor());
		batch.setColor(1f, 1f, 1f, OPACITA_OMBRA);
		immagini.disegna(batch, OMBRA_DEL_DRAGO, (width - immagini.larghezza(OMBRA_DEL_DRAGO, zoom)) >> 1, (height - immagini.altezza(OMBRA_DEL_DRAGO, zoom)) >> 1,
				zoom);
		batch.setColor(precedente);
		TextureRegion titolo = testoGrande.immagine(getTitolo(), com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande.larghezza(getTitolo()), false, 0);
		batch.draw(titolo, getXDelTitolo(), QUOTA_TITOLO);
		DoomdarkFont font = medio.getFont();
		int yRiga = getQuotaDelSottotitolo();
		for (String riga : getSottotitolo()) {
			medio.disegna(batch, riga, (width - medio.larghezza(riga)) >> 1, yRiga, DoomdarkColorModel.Color.MEDIUM_GRAY);
			yRiga += font.getHeight();
		}
		String quante = getQuanteSbloccate();
		medio.disegna(batch, quante, width - MARGINE - medio.larghezza(quante), QUOTA_TITOLO + 6, DoomdarkColorModel.Color.MEDIUM_GRAY);
		Area areaElenco = getAreaElenco();
		int xSeparatore = areaElenco.getX() + areaElenco.getLarghezza() + SPAZIO_FRA_I_PANNELLI / 2;
		immagini.riempi(batch, xSeparatore, areaElenco.getY(), 1, areaElenco.getAltezza(), COLORE_SEPARATORE);
		if (getElenco() != null) {
			disegnaColonna(batch, camera, viewport, getElenco(), getContenutoElenco());
		}
		if (getScheda() != null) {
			disegnaColonna(batch, camera, viewport, getScheda(), getContenutoScheda());
		}
	}

	private void disegnaColonna(Batch batch, Camera camera, Viewport viewport, ColonnaScorrevole colonna, ContenutoDiColonna contenuto) {
		colonna.aggiorna();
		Area area = colonna.getArea();
		int x0 = area.getX() + (area.getLarghezza() - contenuto.getLarghezza()) / 2;
		int y0 = area.getY() + (int) Math.round(colonna.getY());
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
		disegno.disegna(batch, contenuto, x0, y0, 1f);
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
