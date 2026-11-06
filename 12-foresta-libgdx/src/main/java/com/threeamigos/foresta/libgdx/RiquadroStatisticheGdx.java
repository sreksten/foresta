package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.riquadri.RiquadroStatistiche;
import com.threeamigos.foresta.ui.riquadri.VoceDiTesto;

/**
 * Il riquadro con le statistiche del gruppo (monete, preziosi, punti esperienza) in libGDX: le posizioni sono del modulo comune ({@link RiquadroStatistiche},
 * le stesse di Swing), qui ci sono la cornice piccola e le scritte. Gli sprite delle variazioni arriveranno con gli sprite.
 */
public final class RiquadroStatisticheGdx extends RiquadroStatistiche {

	private static final String CORNICE = "fondi/CornicePiccola.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx testo;

	public RiquadroStatisticheGdx(int x, int y, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx testo) {
		super(x, y, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()), vistaPartita);
		this.immagini = immagini;
		this.testo = testo;
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		// Le icone non hanno lo zoom
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	public void disegna(Batch batch) {
		immagini.disegna(batch, CORNICE, getTopLeftX(), getTopLeftY(), (float) LivelloDiZoom.valore());
		for (VoceDiTesto voce : voci()) {
			testo.disegna(batch, voce);
		}
	}
}
