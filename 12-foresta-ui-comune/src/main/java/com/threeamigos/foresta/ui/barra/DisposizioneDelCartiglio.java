package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

/**
 * Com'è fatto un cartiglio, senza disegno: un nastro alto 24 pixel (un bordo marrone sopra e uno sotto, 22 pixel arancioni in mezzo) con il testo, e ai
 * lati i due capi arrotolati, che coprono di un pixel le estremità del nastro e stanno un pixel più in basso, come se la pergamena si abbassasse ai lati.
 * Lo usano il nome della casella sotto il mouse sulla mappa a tutto schermo, l'aiuto della barra delle icone e quello delle schermate di scambio
 * (inventario, negozi, incantatore). Coordinate relative all'angolo in alto a sinistra del cartiglio.
 */
public final class DisposizioneDelCartiglio {

	private static final int MARGINE_TESTO = 4;
	public static final int ALTEZZA_NASTRO = 24;
	private static final int SOVRAPPOSIZIONE_CAPI = 1;
	private static final int ABBASSAMENTO_CAPI = 1;
	/**
	 * Il colore del nastro (RGB) e del suo bordo sopra e sotto
	 */
	public static final int SFONDO = 0xFFAD00;
	public static final int BORDO = 0x8C4210;

	private final int larghezzaDelTesto;
	private final int altezzaDelTesto;
	private final Misura capoSinistro;
	private final Misura capoDestro;

	/**
	 * @param testo        quel che c'è scritto
	 * @param capoSinistro la misura del capo sinistro (<code>fondinon2x2/Cartiglio-sinistro.png</code>)
	 */
	public DisposizioneDelCartiglio(String testo, DoomdarkFont font, Misura capoSinistro, Misura capoDestro) {
		this.larghezzaDelTesto = Math.max(1, FontTool.getWidth(font, testo));
		this.altezzaDelTesto = font.getHeight();
		this.capoSinistro = capoSinistro;
		this.capoDestro = capoDestro;
	}

	/**
	 * Quanto sporge a sinistra il capo, prima del nastro
	 */
	private int sporgenzaSinistra() {
		return capoSinistro.larghezza - SOVRAPPOSIZIONE_CAPI;
	}

	public int getLarghezzaDelNastro() {
		return larghezzaDelTesto + MARGINE_TESTO * 2;
	}

	public int getXDelNastro() {
		return sporgenzaSinistra();
	}

	public int getLarghezza() {
		return sporgenzaSinistra() + getLarghezzaDelNastro() + (capoDestro.larghezza - SOVRAPPOSIZIONE_CAPI);
	}

	public int getAltezza() {
		return ABBASSAMENTO_CAPI + Math.max(capoSinistro.altezza, capoDestro.altezza);
	}

	public int getXDelTesto() {
		return getXDelNastro() + MARGINE_TESTO;
	}

	public int getYDelTesto() {
		return (ALTEZZA_NASTRO - altezzaDelTesto) / 2;
	}

	public int getXDelCapoDestro() {
		return getXDelNastro() + getLarghezzaDelNastro() - SOVRAPPOSIZIONE_CAPI;
	}

	/**
	 * La quota dei due capi: un pixel più in basso del nastro
	 */
	public int getYDeiCapi() {
		return ABBASSAMENTO_CAPI;
	}
}
