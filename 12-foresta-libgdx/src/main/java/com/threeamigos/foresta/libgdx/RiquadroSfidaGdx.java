package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.riquadri.RiquadroSfida;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;

/**
 * Il riquadro della sfida a carta, forbici e sasso in libGDX: le misure, il movimento delle mani e i punteggi sono del modulo comune ({@link RiquadroSfida},
 * gli stessi di Swing), qui c'è il disegno: la cornice larga (ridotta se non sta entro metà schermo), le mani ritagliate dentro il bordo nero e i punteggi in
 * alfabeto grande con l'alone ({@link TestoGrandeGdx}).
 */
public final class RiquadroSfidaGdx extends RiquadroSfida {

	private final ImmaginiGdx immagini;
	private final TestoGrandeGdx testoGrande;
	private final float zoom = (float) LivelloDiZoom.valore();

	public RiquadroSfidaGdx(int larghezzaContenuto, int altezzaContenuto, ImmaginiGdx immagini, TestoGrandeGdx testoGrande) {
		super(larghezzaContenuto, altezzaContenuto);
		this.immagini = immagini;
		this.testoGrande = testoGrande;
	}

	@Override
	protected Misura misuraDellaCornice() {
		return new Misura(immagini.larghezza(NomiDelleImmagini.CORNICE_LARGA, zoom), immagini.altezza(NomiDelleImmagini.CORNICE_LARGA, zoom));
	}

	@Override
	protected Misura misuraDellaMano(MossaCartaForbiciSasso mossa, boolean sinistra) {
		String risorsa = NomiDelleImmagini.manoDellaSfida(mossa, sinistra);
		return new Misura(immagini.larghezza(risorsa, zoom), immagini.altezza(risorsa, zoom));
	}

	private void disegnaLaMano(Batch batch, MossaCartaForbiciSasso mossa, boolean sinistra, float x, float y, float scala) {
		TextureRegion mano = immagini.regione(NomiDelleImmagini.manoDellaSfida(mossa, sinistra));
		batch.draw(mano, x, y, mano.getRegionWidth() * zoom * scala, mano.getRegionHeight() * zoom * scala);
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		if (!isVisibile()) {
			return;
		}
		disegna(batch, camera, viewport, fotogramma());
	}

	/**
	 * Il riquadro fermo, con le mosse dell'ultima mano, anche se non è più visibile: per lo sprite che lo dissolve (vedi
	 * {@link #spriteCheSiDissolve}). L'opacità è quella del colore del batch.
	 */
	void disegnaFermo(Batch batch, Camera camera, Viewport viewport) {
		disegna(batch, camera, viewport, fotogrammaFermo());
	}

	/**
	 * La sfida finita come uno sprite che resta intero per <code>attesa</code> secondi, per vedere l'ultima mano, e poi si dissolve
	 * in <code>dissolvenza</code> secondi
	 */
	public SpriteGdx spriteCheSiDissolve(Camera camera, Viewport viewport, float attesa, float dissolvenza) {
		Area area = disposizione().getArea();
		return new SpriteGdx(new ModelloDelloSprite(attesa + dissolvenza, attesa, Dissolvenza.LINEARE_CON_SOGLIA, area.getX(), area.getY(),
				area.getX(), area.getY(), 1f, 1f)) {
			@Override
			protected void disegna(Batch batch, float x, float y, float scala, float alpha) {
				batch.setColor(1f, 1f, 1f, alpha);
				disegnaFermo(batch, camera, viewport);
			}
		};
	}

	private void disegna(Batch batch, Camera camera, Viewport viewport, Fotogramma fotogramma) {
		Disposizione d = disposizione();
		Area area = d.getArea();
		batch.draw(immagini.regione(NomiDelleImmagini.CORNICE_LARGA), area.getX(), area.getY(), d.getCornice().larghezza, d.getCornice().altezza);

		Area spazio = d.getSpazio();
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, area.getX() + spazio.getX(), area.getY() + spazio.getY(), spazio.getLarghezza(),
				spazio.getAltezza());
		float scalaDelleMani = (float) d.getScalaDelleMani();
		disegnaLaMano(batch, fotogramma.getSinistra(), true, area.getX() + Math.round(d.getXManoSinistra() + fotogramma.getDx()),
				area.getY() + Math.round(d.getYMani() + fotogramma.getDy()), scalaDelleMani);
		disegnaLaMano(batch, fotogramma.getDestra(), false, area.getX() + Math.round(d.getXManoDestra() - fotogramma.getDx()),
				area.getY() + Math.round(d.getYMani() + fotogramma.getDy()), scalaDelleMani);
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}

		scriviIlPunteggio(batch, getPunteggioDelGiocatore(), area.getX() + d.getXManoSinistra(), d.getLarghezzaManoSinistra(), area.getY() + d.getYDelPunteggio());
		scriviIlPunteggio(batch, getPunteggioDellAvversario(), area.getX() + d.getXManoDestra(), d.getLarghezzaManoDestra(), area.getY() + d.getYDelPunteggio());
	}

	private void scriviIlPunteggio(Batch batch, int punteggio, int xMano, int larghezzaMano, int y) {
		TextureRegion numero = testoGrande.immagine(String.valueOf(punteggio), Math.max(1, larghezzaMano), true, RAGGIO_ALONE);
		batch.draw(numero, xMano + (larghezzaMano - numero.getRegionWidth()) / 2, y);
	}
}
