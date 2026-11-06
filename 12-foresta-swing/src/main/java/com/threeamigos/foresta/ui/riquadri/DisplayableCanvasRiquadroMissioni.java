package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.barra.Cartiglio;
import com.threeamigos.foresta.ui.componenti.ElementoDelElenco;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Il riquadro con le missioni attive, disegnato con Swing: l'albero, lo scorrimento e i click sono di {@link RiquadroMissioni}, nel modulo comune.
 */
public class DisplayableCanvasRiquadroMissioni extends RiquadroMissioni {

	public DisplayableCanvasRiquadroMissioni(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		super(topLeftX, topLeftY, ImageCache.corniceGrande.getWidth(), ImageCache.corniceGrande.getHeight(), vistaPartita);
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		BufferedImage icona = BufferedImageBuilder.buildBufferedImage(risorsa);
		return new Misura(icona.getWidth(), icona.getHeight());
	}

	/**
	 * Le icone come le tiene ImageCache (già con lo zoom, dove serve)
	 */
	private static Image icona(String risorsa) {
		switch (risorsa) {
			case NomiDelleImmagini.MISSIONE_BIRRA:
				return ImageCache.missioneBirra;
			case NomiDelleImmagini.MISSIONE_GALLO:
				return ImageCache.missioneGallo;
			default:
				return ImageCache.separatore;
		}
	}

	/**
	 * Disegna le missioni attive.
	 */
	public void disegnaMissioni(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, getTopLeftX(), getTopLeftY(), null);
		IntestazioneRiquadro.disegna(graphics, TestiDellaUI.get("RIQUADRO_MISSIONI"), getTopLeftX(), getTopLeftY(), ImageCache.corniceGrande.getWidth());

		sincronizza();
		Area area = getAreaElenco();
		Shape clipOriginale = graphics.getClip();
		graphics.setClip(area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
		int x0 = area.getX();
		int y0 = area.getY() - getOffset();
		for (ElementoDelElenco e : elementiVisibili()) {
			Image immagine;
			switch (e.getTipo()) {
				case ICONA:
				case VALORE_IMMAGINE:
					immagine = icona((String) e.getImmagine());
					break;
				case VALORE_TESTO:
					immagine = ImageCache.get(e.getTesto(), e.getFont(), e.getColore());
					break;
				default:
					// Titolo o descrizione: il testo a capo, già in cache come immagine
					immagine = ImageCache.getTestoACapo(e.getTesto(), e.getFont(), e.getColore(),
							Math.max(e.getLarghezzaDiImpaginazione(), e.getFont().getHeight()), e.getPassoRiga() - e.getFont().getHeight());
					break;
			}
			graphics.drawImage(immagine, x0 + e.getX(), y0 + e.getY(), null);
		}
		// Le frecce, in alto e in basso a destra se la lista continua da quella parte
		if (isFrecciaSuVisibile()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaSu;
			graphics.drawImage(freccia, area.getX() + area.getLarghezza() - freccia.getWidth(null) - ImageCache.SPACING, area.getY() + ImageCache.SPACING, null);
		}
		if (isFrecciaGiuVisibile()) {
			Image freccia = ImageCache.componenteScorrevoleFrecciaGiu;
			graphics.drawImage(freccia, area.getX() + area.getLarghezza() - freccia.getWidth(null) - ImageCache.SPACING,
					area.getY() + area.getAltezza() - ImageCache.SPACING - freccia.getHeight(null), null);
		}
		graphics.setClip(clipOriginale);
	}

	/**
	 * Ad aiuto acceso, i cartigli accanto al mouse dentro l'elenco: sul nome di una missione cosa fa il click (aprire
	 * o chiudere la descrizione), e se l'elenco è più alto del riquadro la rotella. Va chiamato dopo aver disegnato
	 * tutto lo schermo, grande larghezzaSchermo x altezzaSchermo.
	 */
	public void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		List<String> righe = righeDellAiuto();
		if (!righe.isEmpty()) {
			Cartiglio.disegnaAccantoAlMouse(graphics, righe, getTopLeftX() + getMouseX(), getTopLeftY() + getMouseY(), larghezzaSchermo, altezzaSchermo);
		}
	}
}
