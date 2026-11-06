package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Quanto sono grandi le immagini in Swing: i personaggi come li tiene {@link MappaPersonaggioImmagine} (già con lo zoom) e le altre dall'intestazione del file
 */
public final class MisureDelleImmaginiAwt implements MisureDelleImmagini {

	@Override
	public Misura personaggio(TipoPersonaggio classe) {
		BufferedImage immagine = MappaPersonaggioImmagine.getImmagine(classe);
		return new Misura(immagine.getWidth(), immagine.getHeight());
	}

	@Override
	public Misura immagine(String risorsa) {
		return new MisureDaiFile().immagine(risorsa);
	}
}
