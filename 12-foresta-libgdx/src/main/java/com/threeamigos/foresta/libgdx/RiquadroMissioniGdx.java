package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ElementoDelElenco;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.riquadri.RiquadroMissioni;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Il riquadro delle missioni in libGDX: l'albero, lo scorrimento e i click sono del modulo comune ({@link RiquadroMissioni}, gli stessi di Swing), qui ci
 * sono la cornice grande, l'intestazione, gli elementi ritagliati all'area dell'elenco e le frecce.
 */
public final class RiquadroMissioniGdx extends RiquadroMissioni {

	private static final String CORNICE = "fondi/CorniceGrande.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx piccolo;
	private final TestoGdx medio;

	public RiquadroMissioniGdx(int x, int y, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		super(x, y, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()), immagini.altezza(CORNICE, (float) LivelloDiZoom.valore()), vistaPartita);
		this.immagini = immagini;
		this.piccolo = piccolo;
		this.medio = medio;
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		immagini.disegna(batch, CORNICE, getTopLeftX(), getTopLeftY(), (float) LivelloDiZoom.valore());
		medio.disegnaIntestazione(batch, TestiDellaUI.get("RIQUADRO_MISSIONI"), getTopLeftX(), getTopLeftY(), getLarghezzaCornice());

		sincronizza();
		Area area = getAreaElenco();
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
		int x0 = area.getX();
		int y0 = area.getY() - getOffset();
		for (ElementoDelElenco e : elementiVisibili()) {
			switch (e.getTipo()) {
				case ICONA:
				case VALORE_IMMAGINE:
					String risorsa = (String) e.getImmagine();
					immagini.disegna(batch, risorsa, x0 + e.getX(), y0 + e.getY(), scalaDellIcona(risorsa));
					break;
				default:
					TestoGdx testo = e.getFont() == DoomdarkFont.piccolo() ? piccolo : medio;
					int riga = 0;
					for (String parte : e.getRighe()) {
						testo.disegna(batch, parte, x0 + e.getX(), y0 + e.getY() + riga * e.getPassoRiga(), e.getColore());
						riga++;
					}
					break;
			}
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
		int spazio = Finestra.SPACING;
		if (isFrecciaSuVisibile()) {
			TextureRegion freccia = immagini.regione(NomiDelleImmagini.FRECCIA_SU);
			batch.draw(freccia, area.getX() + area.getLarghezza() - freccia.getRegionWidth() - spazio, area.getY() + spazio);
		}
		if (isFrecciaGiuVisibile()) {
			TextureRegion freccia = immagini.regione(NomiDelleImmagini.FRECCIA_GIU);
			batch.draw(freccia, area.getX() + area.getLarghezza() - freccia.getRegionWidth() - spazio,
					area.getY() + area.getAltezza() - freccia.getRegionHeight() - spazio);
		}
	}
}
