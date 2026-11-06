package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Un testo su un cartiglio: un nastro alto 24 pixel (un bordo marrone sopra e uno sotto, 22 pixel arancioni in mezzo)
 * con ai lati i due capi arrotolati (ImageCache.cartiglioSinistro e cartiglioDestro), che coprono di un pixel le
 * estremità del nastro e stanno un pixel più in basso, come se la pergamena si abbassasse ai lati. Lo usano il nome
 * della casella sotto il mouse sulla mappa a tutto schermo, l'aiuto della barra delle icone e quello delle schermate
 * di scambio (inventario, negozi, incantatore).
 */
public final class Cartiglio {

	private static final int MARGINE_TESTO = 4;
	private static final int ALTEZZA_NASTRO = 24;
	private static final int SOVRAPPOSIZIONE_CAPI = 1;
	private static final int ABBASSAMENTO_CAPI = 1;
	private static final Color SFONDO = new Color(0xFF, 0xAD, 0x00);
	private static final Color BORDO = new Color(0x8C, 0x42, 0x10);

	private Cartiglio() {
	}

	/**
	 * Il testo da mettere su un cartiglio: marrone, nel font medio, con l'iniziale come è stata scritta
	 */
	static Image testo(String testo) {
		return ImageCache.get(testo, DoomdarkFont.medio(), DoomdarkColorModel.Color.BROWN);
	}

	static int larghezza(Image testo) {
		int larghezzaNastro = testo.getWidth(null) + MARGINE_TESTO * 2;
		return (ImageCache.cartiglioSinistro.getWidth() - SOVRAPPOSIZIONE_CAPI) + larghezzaNastro
				+ (ImageCache.cartiglioDestro.getWidth() - SOVRAPPOSIZIONE_CAPI);
	}

	static int altezza() {
		return ABBASSAMENTO_CAPI + Math.max(ImageCache.cartiglioSinistro.getHeight(), ImageCache.cartiglioDestro.getHeight());
	}

	/**
	 * Disegna un cartiglio con quel testo, con l'angolo in alto a sinistra in (x, y).
	 */
	public static void disegna(Graphics2D graphics, Image testo, int x, int y) {
		BufferedImage capoSinistro = ImageCache.cartiglioSinistro;
		BufferedImage capoDestro = ImageCache.cartiglioDestro;
		int larghezzaNastro = testo.getWidth(null) + MARGINE_TESTO * 2;
		int sporgenzaSinistra = capoSinistro.getWidth() - SOVRAPPOSIZIONE_CAPI;
		// Il nastro: arancione, con il bordo marrone solo sopra e sotto
		int xNastro = x + sporgenzaSinistra;
		Color coloreOriginale = graphics.getColor();
		graphics.setColor(SFONDO);
		graphics.fillRect(xNastro, y, larghezzaNastro, ALTEZZA_NASTRO);
		graphics.setColor(BORDO);
		graphics.fillRect(xNastro, y, larghezzaNastro, 1);
		graphics.fillRect(xNastro, y + ALTEZZA_NASTRO - 1, larghezzaNastro, 1);
		graphics.setColor(coloreOriginale);
		graphics.drawImage(testo, xNastro + MARGINE_TESTO, y + (ALTEZZA_NASTRO - testo.getHeight(null)) / 2, null);
		// I capi arrotolati, sopra le estremità del nastro e un pixel più in basso
		graphics.drawImage(capoSinistro, x, y + ABBASSAMENTO_CAPI, null);
		graphics.drawImage(capoDestro, xNastro + larghezzaNastro - SOVRAPPOSIZIONE_CAPI, y + ABBASSAMENTO_CAPI, null);
	}

	/**
	 * Uno o più cartigli, uno sotto l'altro e centrati fra loro, accanto al puntatore: in basso a destra del mouse,
	 * oppure sopra se sotto non c'è posto, e comunque dentro l'area larga e alta così (al peggio allineati a un suo
	 * bordo).
	 */
	public static void disegnaAccantoAlMouse(Graphics2D graphics, List<String> righe, int xMouse, int yMouse,
									  int larghezzaArea, int altezzaArea) {
		if (righe.isEmpty()) {
			return;
		}
		List<Image> testi = new ArrayList<>();
		List<DisposizioneDelCartiglio> disposizioni = new ArrayList<>();
		for (String riga : righe) {
			testi.add(testo(riga));
			disposizioni.add(new DisposizioneDelCartiglio(riga, DoomdarkFont.medio(),
					new Misura(ImageCache.cartiglioSinistro.getWidth(), ImageCache.cartiglioSinistro.getHeight()),
					new Misura(ImageCache.cartiglioDestro.getWidth(), ImageCache.cartiglioDestro.getHeight())));
		}
		// Dove stanno lo dice il modulo comune
		List<Punto> posizioni = DisposizioneDelCartiglio.accantoAlMouse(disposizioni, xMouse, yMouse, larghezzaArea, altezzaArea);
		for (int i = 0; i < testi.size(); i++) {
			disegna(graphics, testi.get(i), posizioni.get(i).x, posizioni.get(i).y);
		}
	}
}
