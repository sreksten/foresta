package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.ui.CampoDiTesto;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * Il campo in cui il giocatore scrive un testo, in libGDX: la cornice del combattimento al centro dello schermo con il testo scritto
 * nel font Doomdark e il cursore. Prende i tasti come primo processore degli input (solo mentre si vede) e, sui telefoni, apre la
 * tastiera di sistema. Chi aspetta la risposta, l'invio, la rinuncia e i caratteri ammessi sono del {@link CampoDiTesto} comune
 * ({@link #getCampo()}), lo stesso del <code>Prompt</code> di Swing.
 */
public final class CampoDiTestoGdx extends InputAdapter implements CampoDiTesto.Campo {

	private static final String CORNICE = "fondi/CorniceCombattimento.png";
	private static final int MARGINE_ORIZZONTALE = 10;
	private static final int MARGINE_VERTICALE = 5;
	// Il cursore lampeggia due volte al secondo
	private static final long MEZZO_PERIODO_DEL_CURSORE_MS = 250;
	private static final int LARGHEZZA_DEL_CURSORE = 10;

	private final CampoDiTesto campo = new CampoDiTesto();
	private final ImmaginiGdx immagini;
	private final TestoGdx testo;
	private final StringBuilder scritto = new StringBuilder();
	private final int x;
	private final int y;
	private final int larghezza;
	private final int altezza;
	private boolean visibile;

	/**
	 * Il campo sta al centro dello schermo di quelle misure.
	 */
	public CampoDiTestoGdx(ImmaginiGdx immagini, TestoGdx testo, int larghezzaSchermo, int altezzaSchermo) {
		this.immagini = immagini;
		this.testo = testo;
		float zoom = (float) LivelloDiZoom.valore();
		larghezza = immagini.larghezza(CORNICE, zoom);
		altezza = immagini.altezza(CORNICE, zoom);
		x = (larghezzaSchermo - larghezza) / 2;
		y = (altezzaSchermo - altezza) / 2;
		campo.setCampo(this);
	}

	/**
	 * Il campo comune che comanda questo
	 */
	public CampoDiTesto getCampo() {
		return campo;
	}

	@Override
	public void mostra(String testoIniziale) {
		scritto.setLength(0);
		scritto.append(testoIniziale);
		visibile = true;
		Gdx.input.setOnscreenKeyboardVisible(true);
	}

	@Override
	public void nascondi() {
		visibile = false;
		Gdx.input.setOnscreenKeyboardVisible(false);
	}

	@Override
	public String getTestoScritto() {
		return scritto.toString();
	}

	@Override
	public boolean keyDown(int tasto) {
		if (!visibile) {
			return false;
		}
		switch (tasto) {
			case Input.Keys.ENTER:
			case Input.Keys.NUMPAD_ENTER:
				campo.invio();
				break;
			case Input.Keys.ESCAPE:
			case Input.Keys.BACK:
				campo.esc();
				break;
			case Input.Keys.BACKSPACE:
				if (scritto.length() > 0) {
					scritto.setLength(scritto.length() - 1);
				}
				break;
			default:
				break;
		}
		return true;
	}

	@Override
	public boolean keyTyped(char carattere) {
		if (!visibile) {
			return false;
		}
		// Invio, cancellazione ed Esc li gestisce keyDown; qui solo i caratteri che il font sa disegnare
		if (!Character.isISOControl(carattere) && CampoDiTesto.ammesso(carattere)) {
			scritto.append(carattere);
		}
		return true;
	}

	/**
	 * Mentre il campo si vede, i tocchi non vanno allo schermo sotto
	 */
	@Override
	public boolean touchDown(int schermoX, int schermoY, int dito, int pulsante) {
		return visibile;
	}

	public void disegna(Batch batch) {
		if (!visibile) {
			return;
		}
		immagini.disegna(batch, CORNICE, x, y, (float) LivelloDiZoom.valore());
		immagini.riempi(batch, x + MARGINE_ORIZZONTALE, y + MARGINE_VERTICALE, larghezza - 2f * MARGINE_ORIZZONTALE,
				altezza - 2f * MARGINE_VERTICALE, Color.WHITE);
		// Se il testo è più lungo del campo se ne vede la fine (il font non ha "_": il cursore è un rettangolo)
		String daScrivere = scritto.toString();
		int larghezzaUtile = larghezza - 4 * MARGINE_ORIZZONTALE - LARGHEZZA_DEL_CURSORE;
		while (!daScrivere.isEmpty() && testo.larghezza(daScrivere) > larghezzaUtile) {
			daScrivere = daScrivere.substring(1);
		}
		int altezzaRiga = testo.getFont().getHeight();
		float xTesto = x + 2f * MARGINE_ORIZZONTALE;
		float yTesto = y + (altezza - altezzaRiga) / 2f;
		if (!daScrivere.isEmpty()) {
			testo.disegna(batch, daScrivere, xTesto, yTesto, DoomdarkColorModel.Color.BLACK);
		}
		if ((System.currentTimeMillis() / MEZZO_PERIODO_DEL_CURSORE_MS) % 2 == 0) {
			immagini.riempi(batch, xTesto + (daScrivere.isEmpty() ? 0 : testo.larghezza(daScrivere)) + 1, yTesto + altezzaRiga - 3,
					LARGHEZZA_DEL_CURSORE, 2, Color.BLACK);
		}
		batch.setColor(Color.WHITE);
	}
}
