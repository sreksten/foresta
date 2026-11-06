package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.intermezzi.StatoElemento;
import com.threeamigos.foresta.ui.immagini.FotogrammiDelFuocoDaCampo;
import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;

import java.awt.image.BufferedImage;

/**
 * Un fuoco da campo disegnato via codice, senza risorse grafiche: i fotogrammi li calcola {@link FotogrammiDelFuocoDaCampo}, nel modulo comune (due ciocchi
 * incrociati, tre strati di fiamma, qualche scintilla; otto fotogrammi a dieci al secondo); qui diventano immagini di Swing, una volta sola.
 */
public class AnimazioneFuocoDaCampo implements AnimazioneImmagine {

	private final BufferedImage[] fotogrammi = new BufferedImage[FotogrammiDelFuocoDaCampo.FOTOGRAMMI];

	public AnimazioneFuocoDaCampo() {
		for (int i = 0; i < fotogrammi.length; i++) {
			fotogrammi[i] = ImmaginiAwt.buffered(FotogrammiDelFuocoDaCampo.fotogrammi()[i]);
		}
	}

	@Override
	public BufferedImage getFotogramma(double secondi, StatoElemento stato) {
		return fotogrammi[FotogrammiDelFuocoDaCampo.indiceAl(secondi)];
	}
}
