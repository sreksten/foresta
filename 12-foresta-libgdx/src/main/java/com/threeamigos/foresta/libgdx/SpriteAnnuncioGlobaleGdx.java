package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.sprite.AnnuncioDelloSchermo;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.ui.sprite.DescrizioneAnnuncioGlobale;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;

/**
 * Lo sprite di annuncio globale in libGDX: l'etichetta e il messaggio, nell'alfabeto grande e con l'ombra, che restano al centro dello schermo e si
 * ingrandiscono mentre sfumano. Che cosa mostra lo dice {@link DescrizioneAnnuncioGlobale}, nel modulo comune, che compone anche i pixel; la loro texture
 * appartiene a {@link ImmaginiGdx} e si libera con lui.
 */
public final class SpriteAnnuncioGlobaleGdx extends SpriteGdx implements AnnuncioDelloSchermo {

	private final DescrizioneAnnuncioGlobale dati;
	private final TextureRegion regione;

	public SpriteAnnuncioGlobaleGdx(DescrizioneAnnuncioGlobale dati, ImmaginiGdx immagini) {
		super(new ModelloDelloSprite(DescrizioneAnnuncioGlobale.DURATA_IN_SECONDI, DescrizioneAnnuncioGlobale.ATTACCO_DISSOLVENZA_DOPO_SECONDI,
				Dissolvenza.LINEARE_CON_SOGLIA, 0, 0, 0, 0, DescrizioneAnnuncioGlobale.SCALA_INIZIALE, DescrizioneAnnuncioGlobale.SCALA_FINALE));
		this.dati = dati;
		this.regione = immagini.regioneDaPixel(dati.immagineConOmbra(), false);
	}

	public boolean isSostituibile() {
		return dati.isSostituibile();
	}

	@Override
	protected void disegna(Batch batch, float x, float y, float scala, float alpha) {
		int larghezza = regione.getRegionWidth();
		int altezza = regione.getRegionHeight();
		batch.setColor(1f, 1f, 1f, alpha);
		batch.draw(regione, dati.xAllaScala(scala, larghezza), dati.yAllaScala(scala, altezza), Math.round(larghezza * scala), Math.round(altezza * scala));
	}
}
