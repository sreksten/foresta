package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.schermate.SchermataOpzioni;

/**
 * La schermata delle opzioni in libGDX: le righe, i comandi dei pulsanti e la riga che aspetta una scelta sono del modulo comune
 * ({@link SchermataOpzioni}, gli stessi di Swing); qui i pulsanti sono le icone come nella barra, e il titolo e le scritte si disegnano con
 * {@link DisegnoDiContenutoGdx}.
 */
public final class SchermataOpzioniGdx extends SchermataOpzioni {

	private final IconeGdx icone;
	private final DisegnoDiContenutoGdx disegno;

	public SchermataOpzioniGdx(int width, int height, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		this(width, height, new IconeGdx(immagini), new DisegnoDiContenutoGdx(immagini, piccolo, medio));
	}

	private SchermataOpzioniGdx(int width, int height, IconeGdx icone, DisegnoDiContenutoGdx disegno) {
		// Le icone sono a metà risoluzione: come nella barra, si disegnano con lo zoom
		super(width, height, new Misura(Math.round(icone.larghezza() * (float) LivelloDiZoom.valore()),
				Math.round(icone.altezza() * (float) LivelloDiZoom.valore())));
		this.icone = icone;
		this.disegno = disegno;
	}

	public void disegna(Batch batch) {
		for (Riga riga : getRighe()) {
			batch.draw(icone.regione(riga.getIcona()), riga.getPulsante().getX(), riga.getPulsante().getY(), riga.getPulsante().getLarghezza(),
					riga.getPulsante().getAltezza());
		}
		disegno.disegna(batch, contenuto(), 0, 0, 1f);
	}
}
