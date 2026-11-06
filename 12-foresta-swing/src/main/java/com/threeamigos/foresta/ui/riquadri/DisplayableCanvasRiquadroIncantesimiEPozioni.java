package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorAlternante;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Gli incantesimi e le pozioni del gruppo: una lista scrollabile con la rotella del mouse, una voce per riga, con il
 * nome per intero (senza abbreviazioni) e la quantità a destra. Gli incantesimi sono nell'ordine di ClasseIncantesimo, poi le pozioni.
 */
public class DisplayableCanvasRiquadroIncantesimiEPozioni extends RiquadroIncantesimiEPozioni {

	public DisplayableCanvasRiquadroIncantesimiEPozioni(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		super(topLeftX, topLeftY, ImageCache.corniceGrande.getWidth(), ImageCache.corniceGrande.getHeight(), vistaPartita);
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		BufferedImage icona = BufferedImageBuilder.buildBufferedImage(risorsa);
		return new Misura(icona.getWidth(), icona.getHeight());
	}

	private Rectangle areaLista() {
		com.threeamigos.foresta.ui.Area area = getAreaLista();
		return new Rectangle(area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
	}

	/**
	 * Disegna gli incantesimi disponibili e le pozioni.
	 */
	public void disegnaIncantesimi(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, getTopLeftX(), getTopLeftY(), null);
		IntestazioneRiquadro.disegna(graphics, TestiDellaUI.get("RIQUADRO_INCANTESIMI_E_POZIONI"), getTopLeftX(), getTopLeftY(), ImageCache.corniceGrande.getWidth());

		sincronizzaScorrimento();
		Rectangle areaLista = areaLista();
		Shape clipOriginale = graphics.getClip();
		graphics.setClip(areaLista);
		DoomdarkFont fontMedium = DoomdarkFont.medio();
		for (RigaVisibile riga : righeVisibili()) {
			graphics.drawImage(icona(riga.getIcona()), riga.getXIcona(), riga.getYIcona(), null);
			graphics.drawImage(ImageCache.get(riga.getNome().getTesto(), fontMedium, riga.getNome().getColore()), riga.getNome().getX(), riga.getNome().getY(), null);
			Image numero = ImageCache.get(riga.getQuantita().getTesto(), fontMedium, riga.getQuantita().getColore());
			graphics.drawImage(numero, riga.getQuantita().getX() - numero.getWidth(null), riga.getQuantita().getY(), null);
		}
		disegnaFrecce(graphics);
		graphics.setClip(clipOriginale);
	}

	private final java.util.Map<String, BufferedImage> icone = new java.util.HashMap<>();

	private BufferedImage icona(String risorsa) {
		return icone.computeIfAbsent(risorsa, BufferedImageBuilder::buildBufferedImage);
	}

	/**
	 * Le frecce delle missioni (vedi ComponenteScorrevole), in alto e in basso a destra se la lista continua da
	 * quella parte
	 */
	private void disegnaFrecce(Graphics2D graphics) {
		Image frecciaSu = ImageCache.componenteScorrevoleFrecciaSu;
		Image frecciaGiu = ImageCache.componenteScorrevoleFrecciaGiu;
		Rectangle areaLista = areaLista();
		if (isScorribileSu()) {
			graphics.drawImage(frecciaSu, areaLista.x + areaLista.width - frecciaSu.getWidth(null) - ImageCache.SPACING,
					areaLista.y + ImageCache.SPACING, null);
		}
		if (isScorribileGiu()) {
			graphics.drawImage(frecciaGiu, areaLista.x + areaLista.width - frecciaGiu.getWidth(null) - ImageCache.SPACING,
					areaLista.y + areaLista.height - ImageCache.SPACING - frecciaGiu.getHeight(null), null);
		}
	}
}
