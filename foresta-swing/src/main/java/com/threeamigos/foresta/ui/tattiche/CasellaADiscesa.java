package com.threeamigos.foresta.ui.tattiche;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import java.util.function.Consumer;
import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;

/**
 * Il menu a discesa della schermata delle tattiche: un popup che si apre sotto (o sopra, se sotto non c'è posto) il punto cliccato, con le scelte nel
 * font Doomdark in un {@link ComponenteScorrevole}, che se sono troppe si scorre con la rotella o con le frecce.
 * <p>
 * Finché è aperta cattura tutti i click: uno su una scelta la applica e la chiude, uno fuori la chiude senza cambiare niente. Chi la usa
 * (la schermata) le passa i click prima di tutto il resto e la disegna per ultima.
 *
 * @param <T> il tipo del valore di ogni scelta
 */
final class CasellaADiscesa<T> {

	/**
	 * Una scelta del menu: il testo e il valore che dà
	 */
	static final class Scelta<T> {
		final String testo;
		final T valore;
		/** Che cosa vuol dire questa scelta, per il riquadro di spiegazione; null se non c'è */
		final String spiegazione;

		Scelta(String testo, T valore) {
			this(testo, valore, null);
		}

		Scelta(String testo, T valore, String spiegazione) {
			this.testo = testo;
			this.valore = valore;
			this.spiegazione = spiegazione;
		}
	}

	private static final int MARGINE = 4;
	private static final int INTERLINEA = 2;
	/** Di quanti pixel scorre un click su una freccia */
	private static final int PASSO_FRECCIA = 40;
	/** Di quanti pixel scorre uno scatto della rotella */
	private static final int PASSO_ROTELLA = 12;
	private static final Color SFONDO = new Color(0x20, 0x16, 0x0C);
	private static final Color BORDO = new Color(0xC8, 0xC8, 0xC8);

	private final List<Scelta<T>> scelte;
	private final T corrente;
	private final Consumer<T> alloScegliere;
	private final DoomdarkFont font = DoomdarkFontMedium.getInstance();
	private final int x;
	private final int y;
	private final int larghezza;
	private final int altezza;
	private int offset;
	private boolean aperta = true;

	/**
	 * @param ancoraX    la x a cui allineare il lato sinistro del menu
	 * @param ancoraSotto la quota del bordo alto della riga che lo apre, e sotto di essa si apre
	 * @param ancoraSopra la quota del bordo basso della riga che lo apre, e sopra di essa si apre se sotto non c'è posto
	 */
	CasellaADiscesa(List<Scelta<T>> scelte, T corrente, int ancoraX, int ancoraSotto, int ancoraSopra, int larghezzaSchermo, int altezzaSchermo,
					Consumer<T> alloScegliere) {
		this.scelte = scelte;
		this.corrente = corrente;
		this.alloScegliere = alloScegliere;
		int larghezzaTesto = 0;
		for (Scelta<T> scelta : scelte) {
			larghezzaTesto = Math.max(larghezzaTesto, ImageCache.get(scelta.testo, font, DoomdarkColorModel.Color.WHITE).getWidth(null));
		}
		int larghezzaFrecce = ImageCache.componenteScorrevoleFrecciaSu.getWidth(null) + ImageCache.SPACING;
		this.larghezza = Math.min(larghezzaSchermo - 2 * MARGINE, larghezzaTesto + 2 * MARGINE + larghezzaFrecce);
		int altezzaContenuto = altezzaDelContenuto();
		int massima = Math.max(font.getHeight() * 3, altezzaSchermo - 2 * MARGINE);
		int sotto = altezzaSchermo - ancoraSotto - MARGINE;
		int sopra = ancoraSopra - MARGINE;
		int altezzaScelta;
		int yScelta;
		if (altezzaContenuto + 2 * MARGINE <= sotto || sotto >= sopra) {
			altezzaScelta = Math.min(altezzaContenuto + 2 * MARGINE, Math.min(massima, sotto));
			yScelta = ancoraSotto;
		} else {
			altezzaScelta = Math.min(altezzaContenuto + 2 * MARGINE, Math.min(massima, sopra));
			yScelta = ancoraSopra - altezzaScelta;
		}
		this.altezza = altezzaScelta;
		this.y = yScelta;
		this.x = Math.max(MARGINE, Math.min(ancoraX, larghezzaSchermo - larghezza - MARGINE));
	}

	boolean isAperta() {
		return aperta;
	}

	void chiudi() {
		aperta = false;
	}

	private int altezzaInterna() {
		return altezza - 2 * MARGINE;
	}

	private int larghezzaInterna() {
		return larghezza - 2 * MARGINE;
	}

	private int altezzaDelContenuto() {
		// Ogni nodo del componente è alto quanto il testo più l'interlinea del testo e l'interlinea fra i nodi
		return scelte.size() * (font.getHeight() + 2 * INTERLINEA);
	}

	private ComponenteScorrevole<T> costruisci(T evidenziato) {
		ComponenteScorrevole<T> componente = new ComponenteScorrevole<>(Math.max(1, larghezza - 2 * MARGINE), 0, INTERLINEA);
		for (Scelta<T> scelta : scelte) {
			DoomdarkColorModel.Color colore = scelta.valore.equals(evidenziato) ? DoomdarkColorModel.Color.WHITE
					: scelta.valore.equals(corrente) ? DoomdarkColorModel.Color.YELLOW : DoomdarkColorModel.Color.LIGHT_GRAY;
			componente.creaNodo(scelta.testo, font, colore, null, null, null, scelta.valore);
		}
		return componente;
	}

	private T sceltaAlla(int px, int py) {
		int xi = px - (x + MARGINE);
		int yi = py - (y + MARGINE);
		if (xi < 0 || xi >= larghezzaInterna() || yi < 0 || yi >= altezzaInterna()) {
			return null;
		}
		return costruisci(null).riferimentoTitoloAllaQuota(yi + offset);
	}

	/**
	 * La scelta sotto il mouse, o null
	 */
	Scelta<T> sceltaSotto(int px, int py) {
		T valore = sceltaAlla(px, py);
		if (valore == null) {
			return null;
		}
		for (Scelta<T> scelta : scelte) {
			if (scelta.valore.equals(valore)) {
				return scelta;
			}
		}
		return null;
	}

	private boolean dentro(int px, int py) {
		return px >= x && px < x + larghezza && py >= y && py < y + altezza;
	}

	public void disegna(Graphics2D graphics, int mouseX, int mouseY) {
		graphics.setColor(SFONDO);
		graphics.fillRect(x, y, larghezza, altezza);
		graphics.setColor(BORDO);
		graphics.drawRect(x, y, larghezza - 1, altezza - 1);
		ComponenteScorrevole<T> componente = costruisci(sceltaAlla(mouseX, mouseY));
		offset = componente.limitaOffset(altezzaInterna(), offset);
		Image immagine = componente.produci(altezzaInterna(), offset);
		graphics.drawImage(immagine, x + MARGINE, y + MARGINE, null);
	}

	/**
	 * Un click: sulle frecce scorre, su una scelta la applica e chiude, fuori chiude. Sempre consumato finché è aperta.
	 */
	void click(int px, int py) {
		if (!aperta) {
			return;
		}
		if (!dentro(px, py)) {
			aperta = false;
			return;
		}
		ComponenteScorrevole<T> componente = costruisci(null);
		if (componente.isScorrevole(altezzaInterna())) {
			int larghezzaFreccia = ImageCache.componenteScorrevoleFrecciaSu.getWidth(null);
			boolean suColonnaFrecce = px >= x + larghezza - MARGINE - larghezzaFreccia - 2 * ImageCache.SPACING;
			if (suColonnaFrecce) {
				int meta = y + altezza / 2;
				scorri(py < meta ? -PASSO_FRECCIA : PASSO_FRECCIA);
				return;
			}
		}
		T scelta = sceltaAlla(px, py);
		if (scelta != null) {
			aperta = false;
			alloScegliere.accept(scelta);
		}
	}

	void rotella(int scatti, boolean versoSu) {
		scorri((versoSu ? -1 : 1) * scatti * PASSO_ROTELLA);
	}

	private void scorri(int pixel) {
		offset = costruisci(null).limitaOffset(altezzaInterna(), offset + pixel);
	}
}
