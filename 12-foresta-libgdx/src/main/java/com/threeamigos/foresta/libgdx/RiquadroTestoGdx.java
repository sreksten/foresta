package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.riquadri.RiquadroTesto;

/**
 * Il riquadro di testo della schermata di gioco in libGDX: lo storico, lo scorrimento e i gesti sono quelli del modulo comune
 * ({@link RiquadroTesto}, gli stessi di Swing), qui c'è solo il disegno: sfondo nero con l'ombra del Drago al 30%, le righe di testo che si spengono
 * verso l'alto (per ogni riga una sfumatura di luminosità dalla sua cima al suo fondo, in base alla quota nel riquadro: nera in cima, bianca dalla
 * fine della sfumatura in giù, vedi {@link RiquadroTesto#getAltezzaDellaSfumatura()}) e le frecce che dicono dove c'è altro testo.
 */
public final class RiquadroTestoGdx extends RiquadroTesto {

	private static final float ALFA_DELL_OMBRA = 0.3f;

	private final ImmaginiGdx immagini;
	private final TestoGdx testo;

	public RiquadroTestoGdx(int x, int y, int larghezza, int altezza, ImmaginiGdx immagini, TestoGdx testo) {
		super(x, y, larghezza, altezza);
		this.immagini = immagini;
		this.testo = testo;
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		sincronizzaScorrimento();
		float x = getTopLeftX();
		float y = getTopLeftY();
		immagini.riempi(batch, x, y, getWidth(), getHeight(), Color.BLACK);

		TextureRegion ombra = immagini.regione("fondi/OmbraDelDrago.png");
		float larghezzaOmbra = ombra.getRegionWidth() * (float) LivelloDiZoom.valore();
		float altezzaOmbra = ombra.getRegionHeight() * (float) LivelloDiZoom.valore();
		Color precedente = new Color(batch.getColor());
		batch.setColor(1f, 1f, 1f, ALFA_DELL_OMBRA);
		batch.draw(ombra, x + (int) (getWidth() - larghezzaOmbra) / 2, y + (int) (getHeight() - altezzaOmbra) / 2, larghezzaOmbra, altezzaOmbra);
		batch.setColor(precedente);

		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, x, y, getWidth(), getHeight());
		int altezzaDellaRiga = getStorico().getFont().getHeight();
		int margine = getStorico().getFont().getPadding();
		getStorico().perOgniRigaVisibile((riga, quota) -> testo.disegnaConSfumatura(batch, riga, x + margine, y + quota,
				luminositaAllaQuota(quota), luminositaAllaQuota(quota + altezzaDellaRiga - 1)));
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}

		int spazio = com.threeamigos.foresta.ui.Finestra.SPACING;
		if (getStorico().isScorribileSu()) {
			TextureRegion freccia = immagini.regione("icone/ComponenteScorrevole-FrecciaSu.png");
			batch.draw(freccia, x + getWidth() - freccia.getRegionWidth() - spazio, y + spazio);
		}
		if (getStorico().isScorribileGiu()) {
			TextureRegion freccia = immagini.regione("icone/ComponenteScorrevole-FrecciaGiu.png");
			batch.draw(freccia, x + getWidth() - freccia.getRegionWidth() - spazio, y + getHeight() - freccia.getRegionHeight() - spazio);
		}
	}
}
