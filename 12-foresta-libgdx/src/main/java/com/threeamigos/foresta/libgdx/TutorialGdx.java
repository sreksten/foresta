package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.tutorial.ContornoDiRettangoli;
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
	/** Fra l'icona e il titolo, e fra la riga dell'intestazione e il testo */
	private static final int SPAZIO_FRA_ICONA_E_TITOLO = 8;
	private static final int SPAZIO_SOTTO_L_INTESTAZIONE = 4;

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
			disegnaIlBordo(batch, schermo.getAreeDaBordare(fumetto.getBersaglio()));
		}

		int larghezzaMassima = Math.min(larghezzaSchermo - 4 * DisposizioneDelFumetto.MARGINE, Math.max(larghezzaSchermo / 3, 300));
		// L'intestazione, se c'è: l'icona (la tessera che quel luogo ha sulla mappa) e il titolo accanto, in cima; il testo scende sotto di lei
		Optional<String> titolo = fumetto.getTitolo();
		java.util.List<String> risorseDelleIcone = fumetto.getRisorseDelleIcone();
		float zoom = (float) LivelloDiZoom.valore();
		int larghezzaIcone = 0;
		int altezzaIcone = 0;
		for (String risorsa : risorseDelleIcone) {
			larghezzaIcone += immagini.larghezza(risorsa, zoom) + (larghezzaIcone > 0 ? SPAZIO_FRA_ICONA_E_TITOLO : 0);
			altezzaIcone = Math.max(altezzaIcone, immagini.altezza(risorsa, zoom));
		}
		int larghezzaTitolo = titolo.isPresent() ? Math.max(1, FontTool.getWidth(testo.getFont(), titolo.get())) : 0;
		boolean conIntestazione = !risorseDelleIcone.isEmpty() || titolo.isPresent();
		int altezzaDellaRiga = Math.max(altezzaIcone, titolo.isPresent() ? testo.getFont().getHeight() : 0);
		int altezzaIntestazione = conIntestazione ? altezzaDellaRiga + SPAZIO_SOTTO_L_INTESTAZIONE : 0;
		int larghezzaIntestazione = larghezzaIcone + (!risorseDelleIcone.isEmpty() && titolo.isPresent() ? SPAZIO_FRA_ICONA_E_TITOLO : 0) + larghezzaTitolo;
		DescrizioneFumetto dati = new DescrizioneFumetto(fumetto.getTesto(), larghezzaMassima, testo.getFont(), larghezzaIntestazione, altezzaIntestazione);
		int larghezza = DisposizioneDelFumetto.larghezza(dati.getLarghezza(), latoDelPulsante);
		int altezza = DisposizioneDelFumetto.altezza(dati.getAltezza(), latoDelPulsante);
		Punto posizione = DisposizioneDelFumetto.posizioneFissa(larghezza, altezza, larghezzaSchermo, altezzaSchermo);
		Punto punta = DisposizioneDelFumetto.puntaVerso(bersaglio);
		NuvolaDiFumettoGdx.disegna(batch, immagini, testo, dati, DoomdarkColorModel.Color.BLACK, posizione.x, posizione.y + altezza, larghezza, altezza,
				punta.x, punta.y, 1f);
		if (conIntestazione) {
			int x = posizione.x + DescrizioneFumetto.SMUSSAMENTO_BORDO;
			int y = posizione.y + DescrizioneFumetto.SMUSSAMENTO_BORDO;
			batch.setColor(Color.WHITE);
			for (String risorsa : risorseDelleIcone) {
				immagini.disegna(batch, risorsa, x, y + (altezzaDellaRiga - immagini.altezza(risorsa, zoom)) / 2, zoom);
				x += immagini.larghezza(risorsa, zoom) + SPAZIO_FRA_ICONA_E_TITOLO;
			}
			if (titolo.isPresent()) {
				testo.disegna(batch, titolo.get(), x, y + (altezzaDellaRiga - testo.getFont().getHeight()) / 2, TestoGdx.coloreConAlfa(DoomdarkColorModel.Color.BLACK, 1f), 1f);
			}
		}

		Area areaPollice = DisposizioneDelFumetto.pollice(posizione, larghezza, altezza, latoDelPulsante);
		Area areaAnnulla = DisposizioneDelFumetto.annulla(posizione, larghezza, altezza, latoDelPulsante);
		batch.setColor(Color.WHITE);
		batch.draw(icone.regione(TipoIcona.SI), areaPollice.getX(), areaPollice.getY(), areaPollice.getLarghezza(), areaPollice.getAltezza());
		batch.draw(icone.regione(TipoIcona.ANNULLA), areaAnnulla.getX(), areaAnnulla.getY(), areaAnnulla.getLarghezza(), areaAnnulla.getAltezza());
		pollice = areaPollice;
		annulla = areaAnnulla;
	}

	private void disegnaIlBordo(Batch batch, java.util.List<Area> bersagli) {
		for (Area segmento : ContornoDiRettangoli.segmenti(bersagli, SPESSORE_DEL_BORDO, SPESSORE_DEL_BORDO)) {
			immagini.riempi(batch, segmento.getX(), segmento.getY(), segmento.getLarghezza(), segmento.getAltezza(), COLORE_DEL_BORDO);
		}
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
