package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.barra.DisposizioneDelCartiglio;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * Il cartiglio in libGDX: il nastro arancione con il bordo marrone, il testo e i due capi arrotolati. Com'è fatto lo dice
 * {@link DisposizioneDelCartiglio}, nel modulo comune.
 */
public final class CartiglioGdx {

	private final ImmaginiGdx immagini;
	private final TestoGdx testo;
	private final Color sfondo = new Color((DisposizioneDelCartiglio.SFONDO << 8) | 0xFF);
	private final Color bordo = new Color((DisposizioneDelCartiglio.BORDO << 8) | 0xFF);

	/**
	 * @param testo il testo, nel font medio
	 */
	public CartiglioGdx(ImmaginiGdx immagini, TestoGdx testo) {
		this.immagini = immagini;
		this.testo = testo;
	}

	/**
	 * Com'è fatto un cartiglio con quella scritta
	 */
	public DisposizioneDelCartiglio disposizione(String scritta) {
		return new DisposizioneDelCartiglio(scritta, testo.getFont(), misura(NomiDelleImmagini.CARTIGLIO_SINISTRO), misura(NomiDelleImmagini.CARTIGLIO_DESTRO));
	}

	private Misura misura(String risorsa) {
		return new Misura(immagini.larghezza(risorsa, 1f), immagini.altezza(risorsa, 1f));
	}

	/**
	 * Disegna un cartiglio con quella scritta, con l'angolo in alto a sinistra in (x, y)
	 */
	public void disegna(Batch batch, String scritta, int x, int y) {
		DisposizioneDelCartiglio d = disposizione(scritta);
		int xNastro = x + d.getXDelNastro();
		immagini.riempi(batch, xNastro, y, d.getLarghezzaDelNastro(), DisposizioneDelCartiglio.ALTEZZA_NASTRO, sfondo);
		immagini.riempi(batch, xNastro, y, d.getLarghezzaDelNastro(), 1, bordo);
		immagini.riempi(batch, xNastro, y + DisposizioneDelCartiglio.ALTEZZA_NASTRO - 1, d.getLarghezzaDelNastro(), 1, bordo);
		testo.disegna(batch, scritta, x + d.getXDelTesto(), y + d.getYDelTesto(), DoomdarkColorModel.Color.BROWN);
		immagini.disegna(batch, NomiDelleImmagini.CARTIGLIO_SINISTRO, x, y + d.getYDeiCapi(), 1f);
		immagini.disegna(batch, NomiDelleImmagini.CARTIGLIO_DESTRO, x + d.getXDelCapoDestro(), y + d.getYDeiCapi(), 1f);
	}
}
