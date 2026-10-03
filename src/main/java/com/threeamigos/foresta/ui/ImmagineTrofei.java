package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.RegistroTrofei;
import com.threeamigos.foresta.motore.tipi.TipoTrofeo;
import com.threeamigos.foresta.trofei.ClasseTrofeo;
import com.threeamigos.foresta.trofei.Trofeo;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Tutti i trofei in un'unica immagine, da far scorrere (vedi ScorrimentoVerticale): nell'intro, e nella pagina dei
 * trofei che si apre dall'inventario. Per tipologia, ognuno con il nome nell'alfabeto grande, a destra quanto ne
 * manca (ottenuti/necessari) e sotto la descrizione nel font medio; bianco se vinto, grigio scuro se manca.
 */
final class ImmagineTrofei {

	private static final DoomdarkColorModel.Color VINTO = DoomdarkColorModel.Color.WHITE;
	private static final DoomdarkColorModel.Color MANCANTE = DoomdarkColorModel.Color.DARK_GRAY;
	private static final int SPAZIO_QUANTITA = 12;
	private static final int SPAZIO_DOPO_IL_TITOLO = 24;

	private ImmagineTrofei() {
	}

	/**
	 * L'immagine dei trofei così come sono adesso, larga quanto chiesto; con il titolo "trofei" in cima, se serve.
	 */
	static BufferedImage costruisci(int larghezza, boolean conTitolo) {
		DoomdarkFont font = DoomdarkFontMedium.getInstance();
		int spazioFraTrofei = font.getHeight();
		List<Image[]> trofei = new ArrayList<>();
		int altezza = 0;
		BufferedImage titolo = null;
		if (conTitolo) {
			titolo = TestoGrande.immagine("trofei", larghezza, true);
			altezza += titolo.getHeight() + SPAZIO_DOPO_IL_TITOLO;
		}
		for (TipoTrofeo tipo : TipoTrofeo.perTipologia()) {
			Trofeo trofeo = ClasseTrofeo.di(tipo);
			boolean vinto = RegistroTrofei.isVinto(tipo);
			int progresso = vinto ? trofeo.getObiettivo() : trofeo.getProgresso();
			DoomdarkColorModel.Color colore = vinto ? VINTO : MANCANTE;
			Image quantita = ImageCache.get(progresso + "/" + trofeo.getObiettivo(), font, colore);
			int larghezzaNome = larghezza - quantita.getWidth(null) - SPAZIO_QUANTITA;
			BufferedImage nome = TestoGrande.immagine(tipo.getNome(), larghezzaNome, false);
			Image descrizione = ImageCache.get(tipo.getDescrizione(), font, colore, larghezza);
			trofei.add(new Image[] {vinto ? nome : TestoGrande.scura(nome), quantita, descrizione});
			altezza += nome.getHeight() + descrizione.getHeight(null) + spazioFraTrofei;
		}
		BufferedImage immagine = new BufferedImage(Math.max(1, larghezza), Math.max(1, altezza), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = immagine.createGraphics();
		try {
			int y = 0;
			if (titolo != null) {
				graphics.drawImage(titolo, 0, y, null);
				y += titolo.getHeight() + SPAZIO_DOPO_IL_TITOLO;
			}
			for (Image[] trofeo : trofei) {
				Image nome = trofeo[0];
				Image quantita = trofeo[1];
				Image descrizione = trofeo[2];
				graphics.drawImage(nome, 0, y, null);
				// La quantità a destra, all'altezza della prima riga del nome
				graphics.drawImage(quantita, larghezza - quantita.getWidth(null), y, null);
				y += nome.getHeight(null);
				graphics.drawImage(descrizione, 0, y, null);
				y += descrizione.getHeight(null) + spazioFraTrofei;
			}
		} finally {
			graphics.dispose();
		}
		return immagine;
	}
}
