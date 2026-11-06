package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.riquadri.RiquadroCombattimento;
import com.threeamigos.foresta.ui.riquadri.VoceDiTesto;

/**
 * Il riquadro del combattimento in libGDX: chi sta nel round, la barra della salute, i testi e le misure sono del modulo comune
 * ({@link RiquadroCombattimento}, gli stessi di Swing), qui c'è il disegno: lo sfondo nero, i bordi laterali, le due metà della cornice (in cima e in
 * fondo), le strisce del gruppo e le barre.
 */
public final class RiquadroCombattimentoGdx extends RiquadroCombattimento {

	private static final String CORNICE = "fondi/CorniceCombattimento.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx testo;

	public RiquadroCombattimentoGdx(int larghezzaDelloSchermo, int altezzaDelloSchermo, int yPreferita, VistaPartita vistaPartita, ImmaginiGdx immagini,
									TestoGdx testo) {
		super(larghezzaDelloSchermo, altezzaDelloSchermo, yPreferita, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()),
				immagini.altezza(CORNICE, (float) LivelloDiZoom.valore()), vistaPartita);
		this.immagini = immagini;
		this.testo = testo;
	}

	private static Color colore(int rgb) {
		return new Color((rgb << 8) | 0xFF);
	}

	public void disegna(Batch batch) {
		Disposizione d = disponi();
		if (d == null) {
			return;
		}
		Area area = d.getArea();
		float zoom = (float) LivelloDiZoom.valore();
		immagini.riempi(batch, area.getX(), area.getY(), area.getLarghezza(), area.getAltezza(), colore(COLORE_SFONDO));
		immagini.riempi(batch, area.getX(), area.getY(), d.getBordoLaterale(), area.getAltezza(), colore(COLORE_BORDO));
		immagini.riempi(batch, area.getX() + area.getLarghezza() - d.getBordoLaterale(), area.getY(), d.getBordoLaterale(), area.getAltezza(),
				colore(COLORE_BORDO));
		// La cornice tagliata a metà: la metà alta in cima, la bassa in fondo (le coordinate della porzione sono quelle dell'immagine originale, senza zoom)
		// La metà bassa è il resto dell'altezza originale: arrotondandola da sola le due metà potrebbero sommare una riga più dell'immagine
		int altezzaOriginale = immagini.regione(CORNICE).getRegionHeight();
		int larghezzaOriginale = immagini.regione(CORNICE).getRegionWidth();
		int altaOriginale = Math.min(altezzaOriginale - 1, Math.round(d.getMetaAlta() / zoom));
		int bassaOriginale = altezzaOriginale - altaOriginale;
		batch.draw(immagini.porzione(CORNICE, 0, 0, larghezzaOriginale, altaOriginale), area.getX(), area.getY(), area.getLarghezza(), d.getMetaAlta());
		batch.draw(immagini.porzione(CORNICE, 0, altaOriginale, larghezzaOriginale, bassaOriginale), area.getX(),
				area.getY() + area.getAltezza() - d.getMetaBassa(), area.getLarghezza(), d.getMetaBassa());

		for (Riga riga : d.getRighe()) {
			if (riga.getColoreSegnale() != 0 || riga.isMostraLaBarra()) {
				immagini.riempi(batch, d.getXSegnale(), riga.getY(), LARGHEZZA_SEGNALE, ALTEZZA_BARRA, colore(riga.getColoreSegnale()));
			}
			if (riga.isMostraLaBarra()) {
				immagini.riempi(batch, d.getXBarra() + riga.getLarghezzaBarraPiena(), riga.getY(), d.getLarghezzaBarra() - riga.getLarghezzaBarraPiena(),
						ALTEZZA_BARRA, colore(COLORE_SFONDO_BARRA));
				immagini.riempi(batch, d.getXBarra(), riga.getY(), riga.getLarghezzaBarraPiena(), ALTEZZA_BARRA, colore(riga.getColoreBarra()));
			}
			for (VoceDiTesto voce : riga.getTesti()) {
				testo.disegna(batch, voce);
			}
		}
	}
}
