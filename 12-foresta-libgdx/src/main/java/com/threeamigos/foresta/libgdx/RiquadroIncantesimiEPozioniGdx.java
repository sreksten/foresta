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
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.riquadri.RiquadroIncantesimiEPozioni;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Gli incantesimi e le pozioni del gruppo in libGDX: le righe, i colori e lo scorrimento sono del modulo comune ({@link RiquadroIncantesimiEPozioni}, gli
 * stessi di Swing), qui ci sono la cornice grande, l'intestazione, le righe ritagliate alla lista e le frecce.
 */
public final class RiquadroIncantesimiEPozioniGdx extends RiquadroIncantesimiEPozioni {

	private static final String CORNICE = "fondi/CorniceGrande.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx testo;

	public RiquadroIncantesimiEPozioniGdx(int x, int y, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx testo) {
		super(x, y, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()), immagini.altezza(CORNICE, (float) LivelloDiZoom.valore()), vistaPartita);
		this.immagini = immagini;
		this.testo = testo;
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		// Le icone non hanno lo zoom
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		immagini.disegna(batch, CORNICE, getTopLeftX(), getTopLeftY(), (float) LivelloDiZoom.valore());
		testo.disegnaIntestazione(batch, TestiDellaUI.get("RIQUADRO_INCANTESIMI_E_POZIONI"), getTopLeftX(), getTopLeftY(), getLarghezzaCornice());

		sincronizzaScorrimento();
		Area lista = getAreaLista();
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, lista.getX(), lista.getY(), lista.getLarghezza(), lista.getAltezza());
		for (RigaVisibile riga : righeVisibili()) {
			immagini.disegna(batch, riga.getIcona(), riga.getXIcona(), riga.getYIcona(), 1f);
			testo.disegna(batch, riga.getNome());
			testo.disegna(batch, riga.getQuantita());
		}
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
		int spazio = Finestra.SPACING;
		if (isScorribileSu()) {
			TextureRegion freccia = immagini.regione(NomiDelleImmagini.FRECCIA_SU);
			batch.draw(freccia, lista.getX() + lista.getLarghezza() - freccia.getRegionWidth() - spazio, lista.getY() + spazio);
		}
		if (isScorribileGiu()) {
			TextureRegion freccia = immagini.regione(NomiDelleImmagini.FRECCIA_GIU);
			batch.draw(freccia, lista.getX() + lista.getLarghezza() - freccia.getRegionWidth() - spazio,
					lista.getY() + lista.getAltezza() - freccia.getRegionHeight() - spazio);
		}
	}
}
