package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.sprite.ModelloDelloSprite.Dissolvenza;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Annuncia a schermo, con le lettere di {@link ImageCache}, che una missione è
 * disponibile o è stata completata. A differenza degli altri sprite non si sposta:
 * resta centrato sullo schermo e si ingrandisce mentre sfuma.
 */
public class SpriteAnnuncioGlobale extends SpriteBase {

	private static final Color COLORE_OMBRA = new Color(DescrizioneAnnuncioGlobale.OMBRA_ARGB, true);

	private final DescrizioneAnnuncioGlobale dati;

	public SpriteAnnuncioGlobale(String titolo, String descrizione, int larghezzaSchermo, int altezzaSchermo) {
		this(new DescrizioneAnnuncioGlobale(titolo, descrizione, larghezzaSchermo, altezzaSchermo, false));
	}

	/**
	 * Uno sprite che mostra quel che dice la descrizione
	 */
	public SpriteAnnuncioGlobale(DescrizioneAnnuncioGlobale dati) {
		this.dati = dati;
		inizializza(buildImage(),
				DescrizioneAnnuncioGlobale.DURATA_IN_SECONDI, DescrizioneAnnuncioGlobale.ATTACCO_DISSOLVENZA_DOPO_SECONDI, Dissolvenza.LINEARE_CON_SOGLIA,
				COLORE_OMBRA, DescrizioneAnnuncioGlobale.SCOSTAMENTO_OMBRA,
				0, 0,
				0, 0,
				DescrizioneAnnuncioGlobale.SCALA_INIZIALE, DescrizioneAnnuncioGlobale.SCALA_FINALE);
	}

	public boolean isSostituibile() {
		return dati.isSostituibile();
	}

	public String getTitolo() {
		return dati.getTitolo();
	}

	public String getDescrizione() {
		return dati.getDescrizione();
	}

	@Override
	protected BufferedImage buildImage() {
		BufferedImage testo = new BufferedImage(Math.max(1, dati.getLarghezza()), Math.max(1, dati.getAltezza()), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = testo.createGraphics();
		for (DescrizioneAnnuncioGlobale.Riga riga : dati.getRighe()) {
			disegnaRiga(g, riga.getTesto(), riga.getX(), riga.getY());
		}
		g.dispose();
		return testo;
	}

	private static void disegnaRiga(Graphics2D g, String riga, int x, int y) {
		for (int i = 0; i < riga.length(); i++) {
			char c = riga.charAt(i);
			// L'alfabeto grande ha solo le maiuscole (come in TestoGrande.glifo): un nome come "Defeat the Dragon" non deve perdere le minuscole
			BufferedImage glifo = ImageCache.glifoGrande(Character.toUpperCase(c));
			if (glifo != null) {
				g.drawImage(glifo, x, y, null);
			}
			x += LayoutDelTestoGrande.larghezzaCarattere(c) + LayoutDelTestoGrande.SPAZIO_FRA_CARATTERI;
		}
	}

	@Override
	public void disegna(Graphics2D g, float x, float y, float scala) {
		int larghezzaScalata = Math.round(immagine.getWidth() * scala);
		int altezzaScalata = Math.round(immagine.getHeight() * scala);
		g.drawImage(immagine, dati.xAllaScala(scala, immagine.getWidth()), dati.yAllaScala(scala, immagine.getHeight()), larghezzaScalata, altezzaScalata, null);
	}
}
