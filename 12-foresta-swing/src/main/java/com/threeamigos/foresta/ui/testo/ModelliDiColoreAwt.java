package com.threeamigos.foresta.ui.testo;

import java.awt.image.IndexColorModel;
import java.util.EnumMap;
import java.util.Map;

/**
 * Per ogni colore del testo ({@link DoomdarkColorModel.Color}) il modello a due indici di Swing: lo sfondo trasparente e il primo piano,
 * con cui si trasforma il disegno in bit di un carattere in un'immagine.
 */
final class ModelliDiColoreAwt {

	private static final Map<DoomdarkColorModel.Color, IndexColorModel> MODELLI = new EnumMap<>(DoomdarkColorModel.Color.class);

	static {
		for (DoomdarkColorModel.Color colore : DoomdarkColorModel.Color.values()) {
			int rgb = colore.getRgb();
			MODELLI.put(colore, new IndexColorModel(8, 2, canale(rgb >> 16), canale(rgb >> 8), canale(rgb), 0));
		}
	}

	private ModelliDiColoreAwt() {
	}

	private static byte[] canale(int valore) {
		return new byte[]{0, (byte) (valore & 0xFF)};
	}

	static IndexColorModel getColorModel(DoomdarkColorModel.Color colore) {
		return MODELLI.get(colore);
	}
}
