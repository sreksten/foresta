package com.threeamigos.foresta.ui.barra;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.IconeAwt;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;

/**
 * Il disegno di una barra delle icone con Graphics2D: lo sfondo nero, le icone (ritagliate alla fascia se scorrono, con quella sotto il cursore
 * evidenziata), le due frecce di scorrimento, il copyright e il cartiglio dell'aiuto. Che icone ci sono, dove stanno e che cosa fanno lo dice il modello
 * ({@link BarraIcone}), nel modulo comune.
 */
final class DisegnoBarraIcone {

	private final BarraIcone barra;
	private final Image[] copyrightImages = new Image[3];
	private final int[] copyrightImagesXOffset = new int[3];
	private final int[] copyrightImagesYOffset = new int[3];

	DisegnoBarraIcone(BarraIcone barra) {
		this.barra = barra;
		DoomdarkFont fontSmall = DoomdarkFont.piccolo();
		DoomdarkColorModel.Color color = DoomdarkColorModel.Color.VERY_DARK_GRAY;
		copyrightImages[0] = ImageCache.get("La Foresta", fontSmall, color);
		copyrightImages[1] = ImageCache.get("copyright 1984-2026", fontSmall, color);
		copyrightImages[2] = ImageCache.get("Stefano Reksten", fontSmall, color);
		for (int i = 0; i < 3; i++) {
			copyrightImagesXOffset[i] = barra.getLarghezza() - copyrightImages[i].getWidth(null) - 1;
			copyrightImagesYOffset[i] = barra.getAltezza() - fontSmall.getHeight() * (3 - i) - 1;
		}
	}

	/**
	 * L'immagine di un'icona: normale, del capo (sfondo rosso) o in ricarica
	 */
	private static Image immagine(BarraIcone.IconaVisibile icona) {
		if (icona.isInRicarica()) {
			return IconeAwt.iconaInRicarica(icona.getTipo(), icona.getTurniRimasti(), icona.getDurata());
		}
		return icona.isCapo() ? IconeAwt.iconaCapo(icona.getTipo()) : IconeAwt.icona(icona.getTipo());
	}

	void disegna(Graphics2D graphics) {
		barra.preparaPerDisegno();
		int offsetX = barra.getOffsetX();
		int offsetY = barra.getOffsetY();
		graphics.setColor(Color.black);
		graphics.fillRect(offsetX, offsetY, barra.getLarghezza(), barra.getAltezza());
		// Una copia del contesto: su un Graphics2D nuovo l'hint di interpolazione vale null e non si potrebbe ripristinare
		Graphics2D icone = (Graphics2D) graphics.create();
		try {
			// Pixel netti: niente sfocatura sulle icone ingrandite
			icone.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			Graphics2D ritagliato = (Graphics2D) icone.create();
			try {
				if (barra.isScorrevole()) {
					Area fascia = barra.fasciaDiRitaglio();
					ritagliato.clipRect(offsetX + fascia.getX(), offsetY + fascia.getY(), fascia.getLarghezza(), fascia.getAltezza());
				}
				for (int i = 0; i < barra.iconeVisibili.size(); i++) {
					disegnaIcona(ritagliato, i);
				}
			} finally {
				ritagliato.dispose();
			}
		} finally {
			icone.dispose();
		}
		disegnaIndicatoriDiScorrimento(graphics);
		copyright(graphics);
	}

	private void disegnaIcona(Graphics2D graphics, int indice) {
		BarraIcone.IconaVisibile icona = barra.iconeVisibili.get(indice);
		Area r = barra.rettangoloDisegnato(indice);
		int x = barra.getOffsetX() + r.getX();
		int y = barra.getOffsetY() + r.getY();
		graphics.drawImage(immagine(icona), x, y, r.getLarghezza(), r.getAltezza(), null);
		// Un comando in ricarica non si può dare: sotto il cursore non si evidenzia
		if (barra.isDaEvidenziare(indice)) {
			graphics.setColor(barra.isMousePremuto() ? Color.white : Color.lightGray);
			graphics.drawRect(x, y, r.getLarghezza() - 1, r.getAltezza() - 1);
		}
	}

	/**
	 * Le due freccine blu sopra le icone: quella verso l'inizio se le icone sono scorse, quella verso la fine se ce ne sono ancora
	 */
	private void disegnaIndicatoriDiScorrimento(Graphics2D graphics) {
		boolean orizzontale = barra.getOrientamento() == com.threeamigos.foresta.tipi.Orientamento.ORIZZONTALE;
		Image prima = orizzontale ? ImageCache.componenteScorrevoleFrecciaSinistra : ImageCache.componenteScorrevoleFrecciaSu;
		Image dopo = orizzontale ? ImageCache.componenteScorrevoleFrecciaDestra : ImageCache.componenteScorrevoleFrecciaGiu;
		if (barra.isAltroPrima()) {
			Punto p = barra.posizioneDellaFreccia(true, new Misura(prima.getWidth(null), prima.getHeight(null)));
			graphics.drawImage(prima, barra.getOffsetX() + p.x, barra.getOffsetY() + p.y, null);
		}
		if (barra.isAltroDopo()) {
			Punto p = barra.posizioneDellaFreccia(false, new Misura(dopo.getWidth(null), dopo.getHeight(null)));
			graphics.drawImage(dopo, barra.getOffsetX() + p.x, barra.getOffsetY() + p.y, null);
		}
	}

	private void copyright(Graphics2D graphics) {
		// In verticale la barra è stretta e in fondo ha la freccia: il copyright non ci sta
		if (barra.getOrientamento() != com.threeamigos.foresta.tipi.Orientamento.ORIZZONTALE) {
			return;
		}
		for (int i = 0; i < 3; i++) {
			graphics.drawImage(copyrightImages[i], barra.getOffsetX() + copyrightImagesXOffset[i], barra.getOffsetY() + copyrightImagesYOffset[i], null);
		}
	}

	void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		int i = barra.getIndiceDelCartiglio();
		if (i < 0) {
			return;
		}
		Area r = barra.rettangoloDisegnato(i);
		String descrizione = barra.descrizioneAiuto(barra.iconeVisibili.get(i).comando);
		if (descrizione != null) {
			Image testo = Cartiglio.testo(descrizione);
			Punto posizione = BarraIcone.posizioneCartiglio(barra.getOrientamento(),
					new Area(barra.getOffsetX() + r.getX(), barra.getOffsetY() + r.getY(), r.getLarghezza(), r.getAltezza()),
					Cartiglio.larghezza(testo), Cartiglio.altezza(), larghezzaSchermo, altezzaSchermo);
			Cartiglio.disegna(graphics, testo, posizione.x, posizione.y);
		}
	}
}
