package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.tutorial.DisposizioneDelFumetto;
import com.threeamigos.foresta.ui.tutorial.FumettoDelTutorial;
import com.threeamigos.foresta.ui.tutorial.IntestazioneDelFumetto;
import com.threeamigos.foresta.ui.tutorial.PulsantiDelFumetto;

import java.util.List;
import java.util.Optional;

/**
 * Il fumetto del tutorial in libGDX, gemello di {@code DisplayableCanvasTutorial}: la nuvola con il testo della pagina, il pollice (avanti, «ho capito») e
 * l'annulla (spegne il tutorial) in basso, la coda che punta al bersaglio e il bordo del bersaglio che lampeggia. Dove sta lo decide
 * {@link DisposizioneDelFumetto}, l'intestazione {@link IntestazioneDelFumetto}, i pulsanti {@link PulsantiDelFumetto}, il testo e le pagine il tutor
 * dello schermo; qui c'è solo il disegno.
 */
final class TutorialGdx {

	private static final Color COLORE_DEL_BORDO = new Color((DisposizioneDelFumetto.COLORE_DEL_BORDO_RGB << 8) | 0xFF);

	private final SchermoGdx schermo;
	private final ImmaginiGdx immagini;
	private final TestoGdx testo;
	private final IconeGdx icone;
	private final int latoDelPulsante;
	private final PulsantiDelFumetto pulsanti;

	TutorialGdx(SchermoGdx schermo, ImmaginiGdx immagini, TestoGdx testo) {
		this.schermo = schermo;
		this.immagini = immagini;
		this.testo = testo;
		this.icone = new IconeGdx(immagini);
		this.latoDelPulsante = Math.round(Math.max(icone.larghezza(), icone.altezza()) * (float) LivelloDiZoom.valore());
		this.pulsanti = new PulsantiDelFumetto(schermo);
	}

	/**
	 * Disegna il suggerimento aperto, se c'è.
	 */
	void disegna(Batch batch, int larghezzaSchermo, int altezzaSchermo) {
		Optional<FumettoDelTutorial> aperto = schermo.getTutor().getFumetto();
		if (!aperto.isPresent()) {
			pulsanti.nascondi();
			return;
		}
		FumettoDelTutorial fumetto = aperto.get();
		Area bersaglio = schermo.getAreaDelBersaglio(fumetto.getBersaglio());
		if (schermo.getTutor().isBordoAcceso()) {
			disegnaIlBordo(batch, schermo.getAreeDaBordare(fumetto.getBersaglio()));
		}

		Optional<String> titolo = fumetto.getTitolo();
		List<String> risorseDelleIcone = fumetto.getRisorseDelleIcone();
		float zoom = (float) LivelloDiZoom.valore();
		int[] larghezze = new int[risorseDelleIcone.size()];
		int[] altezze = new int[risorseDelleIcone.size()];
		for (int i = 0; i < risorseDelleIcone.size(); i++) {
			larghezze[i] = immagini.larghezza(risorseDelleIcone.get(i), zoom);
			altezze[i] = immagini.altezza(risorseDelleIcone.get(i), zoom);
		}
		IntestazioneDelFumetto intestazione = new IntestazioneDelFumetto(larghezze, altezze, titolo.isPresent(),
				titolo.map(t -> FontTool.getWidth(testo.getFont(), t)).orElse(0), testo.getFont().getHeight());
		DescrizioneFumetto dati = new DescrizioneFumetto(fumetto.getTesto(), DisposizioneDelFumetto.larghezzaMassima(larghezzaSchermo), testo.getFont(),
				intestazione.getLarghezza(), intestazione.getAltezza());
		int larghezza = DisposizioneDelFumetto.larghezza(dati.getLarghezza(), latoDelPulsante);
		int altezza = DisposizioneDelFumetto.altezza(dati.getAltezza(), latoDelPulsante);
		Punto posizione = DisposizioneDelFumetto.posizioneFissa(larghezza, altezza, larghezzaSchermo, altezzaSchermo);
		Punto punta = DisposizioneDelFumetto.puntaVerso(bersaglio);
		NuvolaDiFumettoGdx.disegna(batch, immagini, testo, dati, DoomdarkColorModel.Color.BLACK, posizione.x, posizione.y + altezza, larghezza, altezza,
				punta.x, punta.y, 1f);
		batch.setColor(Color.WHITE);
		for (int i = 0; i < risorseDelleIcone.size(); i++) {
			// La tessera di un luogo ha parti trasparenti: come sulla mappa, sta sul nero
			if (NomiDelleImmagini.isTesseraDellaMappa(risorseDelleIcone.get(i))) {
				immagini.riempi(batch, posizione.x + intestazione.getXDellIcona(i), posizione.y + intestazione.getYDellIcona(i), larghezze[i], altezze[i], Color.BLACK);
			}
			immagini.disegna(batch, risorseDelleIcone.get(i), posizione.x + intestazione.getXDellIcona(i), posizione.y + intestazione.getYDellIcona(i), zoom);
		}
		if (titolo.isPresent()) {
			testo.disegna(batch, titolo.get(), posizione.x + intestazione.getXDelTitolo(), posizione.y + intestazione.getYDelTitolo(),
					TestoGdx.coloreConAlfa(DoomdarkColorModel.Color.BLACK, 1f), 1f);
		}

		pulsanti.posiziona(posizione, larghezza, altezza, latoDelPulsante);
		Area pollice = pulsanti.getPollice();
		Area annulla = pulsanti.getAnnulla();
		batch.setColor(Color.WHITE);
		batch.draw(icone.regione(TipoIcona.SI), pollice.getX(), pollice.getY(), pollice.getLarghezza(), pollice.getAltezza());
		batch.draw(icone.regione(TipoIcona.ANNULLA), annulla.getX(), annulla.getY(), annulla.getLarghezza(), annulla.getAltezza());
	}

	private void disegnaIlBordo(Batch batch, List<Area> bersagli) {
		for (Area segmento : DisposizioneDelFumetto.segmentiDelBordo(bersagli)) {
			immagini.riempi(batch, segmento.getX(), segmento.getY(), segmento.getLarghezza(), segmento.getAltezza(), COLORE_DEL_BORDO);
		}
	}

	/**
	 * Se il punto sta su uno dei due pulsanti (il tocco che comincia lì non deve arrivare al resto).
	 */
	boolean suiPulsanti(int x, int y) {
		return pulsanti.suiPulsanti(x, y);
	}

	/**
	 * Un tocco finito sul pollice o sull'annulla: lo esegue.
	 *
	 * @return vero se era su un pulsante
	 */
	boolean tocco(int x, int y) {
		return pulsanti.premi(x, y);
	}
}
