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
import com.threeamigos.foresta.ui.riquadri.RiquadroGruppo;
import com.threeamigos.foresta.ui.riquadri.VoceDiTesto;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Il riquadro con lo stato dei personaggi del gruppo in libGDX: dove sta ogni valore, di che colore e lo scorrimento sono del modulo comune
 * ({@link RiquadroGruppo}, gli stessi di Swing), qui c'è solo il disegno: la cornice grande, l'intestazione con l'ombra, le scritte ritagliate al
 * rettangolo della lista e le frecce. Gli sprite delle variazioni (numeri che salgono sopra un valore) arriveranno con gli sprite.
 */
public final class RiquadroGruppoGdx extends RiquadroGruppo {

	private static final String CORNICE = "fondi/CorniceGrande.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx testo;

	public RiquadroGruppoGdx(int x, int y, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx testo) {
		super(x, y, larghezzaDellaCornice(immagini), altezzaDellaCornice(immagini), vistaPartita);
		this.immagini = immagini;
		this.testo = testo;
	}

	private static int larghezzaDellaCornice(ImmaginiGdx immagini) {
		return Math.round(immagini.regione(CORNICE).getRegionWidth() * (float) LivelloDiZoom.valore());
	}

	private static int altezzaDellaCornice(ImmaginiGdx immagini) {
		return Math.round(immagini.regione(CORNICE).getRegionHeight() * (float) LivelloDiZoom.valore());
	}

	/**
	 * L'area che occupa il riquadro (tutta la cornice)
	 */
	public Area getArea() {
		return new Area(getTopLeftX(), getTopLeftY(), getLarghezzaCornice(), altezzaDellaCornice(immagini));
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		// Le icone non hanno lo zoom
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		immagini.disegna(batch, CORNICE, getTopLeftX(), getTopLeftY(), (float) LivelloDiZoom.valore());

		testo.disegnaIntestazione(batch, TestiDellaUI.get("RIQUADRO_GRUPPO"), getTopLeftX(), getTopLeftY(), getLarghezzaCornice());

		sincronizzaScorrimento();
		Area lista = getAreaLista();
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, lista.getX(), lista.getY(), lista.getLarghezza(), lista.getAltezza());
		for (VoceDiTesto voce : voci()) {
			testo.disegna(batch, voce);
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}

		int spazio = Finestra.SPACING;
		if (isScorribileSu()) {
			TextureRegion freccia = immagini.regione("icone/ComponenteScorrevole-FrecciaSu.png");
			batch.draw(freccia, lista.getX() + lista.getLarghezza() - freccia.getRegionWidth() - spazio, lista.getY() + spazio);
		}
		if (isScorribileGiu()) {
			TextureRegion freccia = immagini.regione("icone/ComponenteScorrevole-FrecciaGiu.png");
			batch.draw(freccia, lista.getX() + lista.getLarghezza() - freccia.getRegionWidth() - spazio,
					lista.getY() + lista.getAltezza() - freccia.getRegionHeight() - spazio);
		}
	}
}
