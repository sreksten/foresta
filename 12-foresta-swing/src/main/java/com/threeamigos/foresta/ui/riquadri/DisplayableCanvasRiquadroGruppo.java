package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloPersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.barra.Cartiglio;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * Il riquadro con lo stato dei personaggi del gruppo: nome, salute, magia e le altre statistiche. Si aggiorna ascoltando gli eventi di variazione.
 * Le posizioni, i colori e lo scorrimento sono del modulo comune ({@link RiquadroGruppo}); qui c'è il disegno su Graphics2D e gli sprite delle
 * variazioni.
 */
public class DisplayableCanvasRiquadroGruppo extends RiquadroGruppo {

	public DisplayableCanvasRiquadroGruppo(int topLeftX, int topLeftY, VistaPartita vistaPartita) {
		super(topLeftX, topLeftY, ImageCache.corniceGrande.getWidth(), ImageCache.corniceGrande.getHeight(), vistaPartita);
	}

	@Override
	protected Misura misuraDellIcona(String risorsa) {
		BufferedImage icona = BufferedImageBuilder.buildBufferedImage(risorsa);
		return new Misura(icona.getWidth(), icona.getHeight());
	}

	/**
	 * Disegna lo stato dei personaggi del gruppo.
	 */
	public void disegnaStatus(Graphics2D graphics) {
		graphics.drawImage(ImageCache.corniceGrande, getTopLeftX(), getTopLeftY(), null);
		IntestazioneRiquadro.disegna(graphics, TestiDellaUI.get("RIQUADRO_GRUPPO"), getTopLeftX(), getTopLeftY(), ImageCache.corniceGrande.getWidth());

		sincronizzaScorrimento();
		Rectangle areaLista = areaLista();
		Shape clipOriginale = graphics.getClip();
		graphics.setClip(areaLista);
		for (VoceDiTesto voce : voci()) {
			Image immagine = voce.getLarghezzaMassima() > 0
					? ImageCache.get(voce.getTesto(), fontMedium, voce.getColore(), voce.getLarghezzaMassima())
					: ImageCache.get(voce.getTesto(), fontMedium, voce.getColore());
			int x = voce.isAllineataADestra() ? voce.getX() - immagine.getWidth(null) : voce.getX();
			graphics.drawImage(immagine, x, voce.getY(), null);
		}
		disegnaFrecce(graphics);
		graphics.setClip(clipOriginale);
	}

	private Rectangle areaLista() {
		com.threeamigos.foresta.ui.Area area = getAreaLista();
		return new Rectangle(area.getX(), area.getY(), area.getLarghezza(), area.getAltezza());
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

	/**
	 * Ad aiuto acceso, sopra il riquadro il cartiglio della rotella, se i personaggi (ospiti compresi) non ci stanno
	 * tutti. Va chiamato dopo aver disegnato tutto lo schermo, grande
	 * larghezzaSchermo x altezzaSchermo.
	 */
	public void disegnaAiuto(Graphics2D graphics, int larghezzaSchermo, int altezzaSchermo) {
		if (!vistaPartita.isAiutoAbilitato() || getMouseX() < 0) {
			return;
		}
		if (isScorrevole()) {
			Cartiglio.disegnaAccantoAlMouse(graphics, Collections.singletonList(Cartiglio.AIUTO_ROTELLA),
					getTopLeftX() + getMouseX(), getTopLeftY() + getMouseY(), larghezzaSchermo, altezzaSchermo);
		}
	}
}
