package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.tutorial.DisposizioneDelFumetto;
import com.threeamigos.foresta.ui.tutorial.FumettoDelTutorial;

import java.util.Optional;

/**
 * Il fumetto del tutorial in libGDX, gemello di {@code DisplayableCanvasTutorial}: la nuvola con il testo della pagina, il pollice (avanti, «ho capito») e
 * l'annulla (spegne il tutorial) in basso, la coda che punta al bersaglio e il bordo del bersaglio che lampeggia. Dove sta lo decide
 * {@link DisposizioneDelFumetto}, il testo e le pagine il tutor dello schermo; qui c'è solo il disegno e il tocco sui due pulsanti.
 */
final class TutorialGdx {

	private static final Color COLORE_DEL_BORDO = new Color(1f, 215f / 255f, 0f, 1f);
	private static final int SPESSORE_DEL_BORDO = 3;

	private final SchermoGdx schermo;
	private final ImmaginiGdx immagini;
	private final TestoGdx testo;
	private final IconeGdx icone;
	private final int latoDelPulsante;
	// Dove stanno i due pulsanti nell'ultimo fotogramma disegnato, per il tocco
	private volatile Area pollice;
	private volatile Area annulla;

	TutorialGdx(SchermoGdx schermo, ImmaginiGdx immagini, TestoGdx testo) {
		this.schermo = schermo;
		this.immagini = immagini;
		this.testo = testo;
		this.icone = new IconeGdx(immagini);
		this.latoDelPulsante = Math.round(Math.max(icone.larghezza(), icone.altezza()) * (float) LivelloDiZoom.valore());
	}

	/**
	 * Disegna il suggerimento aperto, se c'è.
	 */
	void disegna(Batch batch, int larghezzaSchermo, int altezzaSchermo) {
		Optional<FumettoDelTutorial> aperto = schermo.getTutor().getFumetto();
		if (!aperto.isPresent()) {
			pollice = null;
			annulla = null;
			return;
		}
		FumettoDelTutorial fumetto = aperto.get();
		Area bersaglio = schermo.getAreaDelBersaglio(fumetto.getBersaglio());
		if (schermo.getTutor().isBordoAcceso()) {
			disegnaIlBordo(batch, bersaglio);
		}

		int larghezzaMassima = Math.min(larghezzaSchermo - 4 * DisposizioneDelFumetto.MARGINE, Math.max(larghezzaSchermo / 3, 300));
		DescrizioneFumetto dati = new DescrizioneFumetto(fumetto.getTesto(), larghezzaMassima, testo.getFont());
		int larghezza = DisposizioneDelFumetto.larghezza(dati.getLarghezza(), latoDelPulsante);
		int altezza = DisposizioneDelFumetto.altezza(dati.getAltezza(), latoDelPulsante);
		Punto posizione = DisposizioneDelFumetto.posizione(bersaglio, larghezza, altezza, larghezzaSchermo, altezzaSchermo);
		Punto punta = DisposizioneDelFumetto.puntaVerso(bersaglio);
		NuvolaDiFumettoGdx.disegna(batch, immagini, testo, dati, DoomdarkColorModel.Color.BLACK, posizione.x, posizione.y + altezza, larghezza, altezza,
				punta.x, punta.y, 1f);

		Area areaPollice = DisposizioneDelFumetto.pollice(posizione, larghezza, altezza, latoDelPulsante);
		Area areaAnnulla = DisposizioneDelFumetto.annulla(posizione, larghezza, altezza, latoDelPulsante);
		batch.setColor(Color.WHITE);
		batch.draw(icone.regione(TipoIcona.SI), areaPollice.getX(), areaPollice.getY(), areaPollice.getLarghezza(), areaPollice.getAltezza());
		batch.draw(icone.regione(TipoIcona.ANNULLA), areaAnnulla.getX(), areaAnnulla.getY(), areaAnnulla.getLarghezza(), areaAnnulla.getAltezza());
		pollice = areaPollice;
		annulla = areaAnnulla;
	}

	private void disegnaIlBordo(Batch batch, Area b) {
		int s = SPESSORE_DEL_BORDO;
		int x = b.getX() - s;
		int y = b.getY() - s;
		int l = b.getLarghezza() + 2 * s;
		int a = b.getAltezza() + 2 * s;
		immagini.riempi(batch, x, y, l, s, COLORE_DEL_BORDO);
		immagini.riempi(batch, x, y + a - s, l, s, COLORE_DEL_BORDO);
		immagini.riempi(batch, x, y, s, a, COLORE_DEL_BORDO);
		immagini.riempi(batch, x + l - s, y, s, a, COLORE_DEL_BORDO);
	}

	/**
	 * Se il punto sta su uno dei due pulsanti (il tocco che comincia lì non deve arrivare al resto).
	 */
	boolean suiPulsanti(int x, int y) {
		Area p = pollice;
		Area a = annulla;
		return (p != null && p.contiene(x, y)) || (a != null && a.contiene(x, y));
	}

	/**
	 * Un tocco finito sul pollice o sull'annulla: lo esegue.
	 *
	 * @return vero se era su un pulsante
	 */
	boolean tocco(int x, int y) {
		Area p = pollice;
		Area a = annulla;
		if (p != null && p.contiene(x, y)) {
			schermo.pollicePremuto();
			return true;
		}
		if (a != null && a.contiene(x, y)) {
			schermo.annullaPremuto();
			return true;
		}
		return false;
	}
}
