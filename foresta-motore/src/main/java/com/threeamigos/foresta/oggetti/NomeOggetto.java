package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.interfacce.OggettoConArticoli;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.Objects;

/**
 * Il nome di un oggetto che non ha una classe sua, come gli oggetti delle missioni (vedi {@link OggettoMissione}):
 * singolare, plurale e articoli, così le descrizioni delle locazioni lo flettono come gli altri oggetti.
 */
public final class NomeOggetto implements OggettoConArticoli {

	private final String singolare;
	private final String plurale;
	private final String ais;
	private final String aip;
	private final String ads;
	private final String adp;

	public NomeOggetto(String singolare, String plurale, String ais, String aip, String ads, String adp) {
		this.singolare = Objects.requireNonNull(singolare);
		this.plurale = Objects.requireNonNull(plurale);
		this.ais = ais;
		this.aip = aip;
		this.ads = ads;
		this.adp = adp;
	}

	/**
	 * Un nome femminile che comincia per consonante: "una radice di mandragola", "le radici di mandragola".
	 */
	public static NomeOggetto femminile(String singolare, String plurale) {
		return new NomeOggetto(singolare, plurale, Misc.UNA, Misc.ALCUNE, Misc.LA, Misc.LE);
	}

	/**
	 * Un nome maschile che comincia per consonante (non s impura né z): "un fungo", "i funghi".
	 */
	public static NomeOggetto maschile(String singolare, String plurale) {
		return new NomeOggetto(singolare, plurale, Misc.UN, Misc.ALCUNI, Misc.IL, Misc.I);
	}

	public String getSingolare() {
		return singolare;
	}

	public String getPlurale() {
		return plurale;
	}

	@Override
	public String getAIS() {
		return ais;
	}

	@Override
	public String getAIP() {
		return aip;
	}

	@Override
	public String getADS() {
		return ads;
	}

	@Override
	public String getADP() {
		return adp;
	}
}
