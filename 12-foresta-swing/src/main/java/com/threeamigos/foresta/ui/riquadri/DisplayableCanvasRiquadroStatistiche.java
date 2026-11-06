package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaMonete;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPreziosi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePuntiEsperienzaPersonaggio;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Il riquadro con le statistiche del gruppo: monete, preziosi e punti esperienza.
 */
public class DisplayableCanvasRiquadroStatistiche extends RiquadroStatistiche {

	public DisplayableCanvasRiquadroStatistiche(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		super(topLeftX, topLeftY, ImageCache.cornicePiccola.getWidth(), vistaPartita);
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		BufferedImage icona = BufferedImageBuilder.buildBufferedImage(risorsa);
		return new Misura(icona.getWidth(), icona.getHeight());
	}

	/**
	 * Disegna le statistiche del gruppo.
	 */
	public void disegnaStatistiche(Graphics2D graphics) {
		graphics.drawImage(ImageCache.cornicePiccola, topLeftX, topLeftY, null);
		for (VoceDiTesto voce : voci()) {
			Image image = ImageCache.get(voce.getTesto(), fontMedium, voce.getColore());
			graphics.drawImage(image, voce.isAllineataADestra() ? voce.getX() - image.getWidth(null) : voce.getX(), voce.getY(), null);
		}
	}
}
