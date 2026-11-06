package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.sprite.CodaDelleRivelazioni;

/**
 * Gli artefatti trovati nei cofani e nei templi durante il gioco, mostrati uno alla volta con {@link RivelazioneDellArtefattoGdx}: la coda e le sue regole sono
 * di {@link CodaDelleRivelazioni} (nel comune, come in Swing). Chi ascolta la notifica di un artefatto trovato chiama {@link #aggiungi}; chi disegna la
 * schermata di gioco chiama {@link #disegna} dopo il resto. Il collegamento con il bus del motore arriva con il collegamento del motore.
 */
public final class RivelazioniInGiocoGdx {

	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final CodaDelleRivelazioni<RivelazioneDellArtefattoGdx> coda = new CodaDelleRivelazioni<>();

	public RivelazioniInGiocoGdx(ImmaginiGdx immagini, TestoGdx medio) {
		this.immagini = immagini;
		this.medio = medio;
	}

	/**
	 * @param area         tutto lo schermo
	 * @param destinazione dove vola all'uscita: il centro del riquadro del gruppo
	 */
	public void aggiungi(VistaArtefatto artefatto, int livelloMondo, Area area, Punto destinazione) {
		coda.aggiungi(new RivelazioneDellArtefattoGdx(artefatto, livelloMondo, area, destinazione, immagini, medio));
	}

	/**
	 * Disegna la rivelazione in corso (se un effetto a tutto schermo è in corso, aspetta) e passa alla prossima quando finisce.
	 */
	public void disegna(Batch batch, Camera camera, Viewport viewport, boolean altroEffettoInCorso) {
		RivelazioneDellArtefattoGdx attiva = coda.attiva(altroEffettoInCorso);
		if (attiva != null) {
			attiva.anima(batch, camera, viewport);
			coda.dopoIlDisegno();
		}
	}

	/** Se c'è una rivelazione in corso o in attesa: l'interfaccia è occupata. */
	public boolean isOccupata() {
		return coda.isOccupata();
	}
}
