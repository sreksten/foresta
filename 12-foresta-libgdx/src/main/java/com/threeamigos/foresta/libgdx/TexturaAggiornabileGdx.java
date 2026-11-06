package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Una texture di dimensione fissa i cui pixel (ARGB, per righe, come quelli di {@link com.threeamigos.foresta.ui.immagini.ImmagineDiPixel}) si
 * riscrivono quando cambiano, senza crearne una nuova: per gli effetti che si ricalcolano a ogni fotogramma (la torcia e le scie del logo). La regione e'
 * capovolta come tutto il resto, per le coordinate con l'asse y verso il basso.
 */
final class TexturaAggiornabileGdx implements Disposable {

	private final Pixmap pixmap;
	private final Texture texture;
	private final TextureRegion regione;
	private final int numeroDiPixel;

	TexturaAggiornabileGdx(int larghezza, int altezza, boolean lineare) {
		this.numeroDiPixel = larghezza * altezza;
		this.pixmap = new Pixmap(larghezza, altezza, Pixmap.Format.RGBA8888);
		this.pixmap.setBlending(Pixmap.Blending.None);
		this.pixmap.setColor(0f, 0f, 0f, 0f);
		this.pixmap.fill();
		this.texture = new Texture(pixmap);
		Texture.TextureFilter filtro = lineare ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
		this.texture.setFilter(filtro, filtro);
		this.regione = new TextureRegion(texture);
		this.regione.flip(false, true);
	}

	/**
	 * Riscrive tutti i pixel
	 *
	 * @param argb i pixel per righe, larghezza × altezza
	 */
	void aggiorna(int[] argb) {
		if (argb.length != numeroDiPixel) {
			throw new IllegalArgumentException("Pixel " + argb.length + " per una texture da " + numeroDiPixel);
		}
		// In memoria i byte sono R, G, B, A: letti come interi little-endian sono 0xAABBGGRR
		ByteBuffer byteBuffer = pixmap.getPixels().duplicate();
		byteBuffer.clear();
		IntBuffer pixel = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		for (int i = 0; i < argb.length; i++) {
			int c = argb[i];
			pixel.put(i, (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c & 0xFF) << 16));
		}
		texture.draw(pixmap, 0, 0);
	}

	TextureRegion getRegione() {
		return regione;
	}

	@Override
	public void dispose() {
		texture.dispose();
		pixmap.dispose();
	}
}
