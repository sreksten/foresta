package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.ui.MisuraDelloSchermo;

/**
 * Gli schermi su cui provare il gioco dal desktop con {@code SCHERMO=<profilo>} (vedi OpzioniDiAvvio): la misura più piccola che il
 * gioco deve reggere e i telefoni e i tablet più comuni. Le misure sono quelle note al 2026-11 e vanno ricontrollate sui modelli veri:
 * alcuni produttori cambiano la densità con le impostazioni di visualizzazione. La tabella con il layout e la scala di ognuno è in
 * {@code layout_telefono.md} §2.3.
 */
public enum SchermoDiProva {

	MINIMO(800, 480, 1, "800x480, minimo supportato"),
	GALAXY_S2(800, 480, 1.5, "Samsung Galaxy S II"),
	ECONOMICO(1600, 720, 2, "telefono economico HD+, tipo Galaxy A05"),
	GALAXY_S24(2340, 1080, 3, "Samsung Galaxy S24"),
	GALAXY_M31(2340, 1080, 2.625, "Samsung Galaxy M31"),
	PIXEL_8(2400, 1080, 2.625, "Google Pixel 8"),
	PIXEL_8_PRO(2992, 1344, 3, "Google Pixel 8 Pro"),
	IPHONE_SE(1334, 750, 2, "iPhone SE"),
	IPHONE_15(2556, 1179, 3, "iPhone 15"),
	IPHONE_15_PRO_MAX(2796, 1290, 3, "iPhone 15 Pro Max"),
	REALME_PAD(2000, 1200, 1.5, "Realme Pad (tablet di prova)"),
	TABLET_ECONOMICO(1280, 800, 1, "tablet Android economico 8 pollici, 1280x800"),
	TABLET_FULL_HD(1920, 1200, 1.5, "tablet Android 10 pollici Full HD"),
	IPAD_MINI(2266, 1488, 2, "iPad mini 8,3 pollici"),
	IPAD(2360, 1640, 2, "iPad Air 10,9 pollici"),
	IPAD_PRO_13(2752, 2064, 2, "iPad Pro 13 pollici"),
	PORTATILE_HD(1366, 768, 1, "portatile 1366x768"),
	FULL_HD(1920, 1080, 1, "monitor Full HD");

	private final MisuraDelloSchermo misura;
	private final String descrizione;

	SchermoDiProva(int larghezza, int altezza, double densita, String descrizione) {
		this.misura = new MisuraDelloSchermo(larghezza, altezza, densita);
		this.descrizione = descrizione;
	}

	public MisuraDelloSchermo getMisura() {
		return misura;
	}

	/** Una riga per il log, quando l'argomento è sbagliato e si mostra l'elenco */
	public String getDescrizione() {
		return descrizione;
	}
}
