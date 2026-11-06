package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.threeamigos.foresta.ui.riquadri.PosizioneDellIntestazione;
import com.threeamigos.foresta.ui.riquadri.VoceDiTesto;
import com.threeamigos.foresta.ui.testo.CaratteriDelFont;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Il testo in font bitmap Doomdark, glifo per glifo, senza immagini di scritte. All'avvio i glifi del font (i dati sono nel modulo
 * comune, {@link DoomdarkFont}) si raccolgono in una sola texture bianca la cui alfa è il livello del pixel; una scritta è una fila di sprite,
 * uno per lettera, nello stesso lotto dello {@code SpriteBatch}, tinti del colore del testo ({@code DoomdarkColorModel.Color}): l'alfa dei pixel
 * sfumati si moltiplica con quella del colore, come fa Swing. Niente cache di scritte, e niente da liberare tranne la texture dei glifi.
 * <p>
 * Come in Swing, una riga è alta quanto l'immagine del font (le righe in più per accenti e cediglie comprese): chi posiziona un testo in un punto fisso
 * toglie {@link DoomdarkFont#getRigheInCima()} dalla quota. Le coordinate sono quelle di Swing, con l'asse y verso il basso e (x, y) l'angolo in
 * alto a sinistra della scritta (vedi {@link ImmaginiGdx}).
 */
public final class TestoGdx implements Disposable {

	private static final int LARGHEZZA_MASSIMA_DELLA_PAGINA = 1024;
	private static final Map<DoomdarkColorModel.Color, Color> COLORI = new HashMap<>();

	private final DoomdarkFont font;
	private final Texture pagina;
	private final Map<Character, TextureRegion> glifi = new HashMap<>();

	public TestoGdx(DoomdarkFont font) {
		this.font = font;
		StringBuilder caratteri = new StringBuilder();
		for (String riga : CaratteriDelFont.RIGHE) {
			for (int i = 0; i < riga.length(); i++) {
				if (font.supportaIlCarattere(riga.charAt(i))) {
					caratteri.append(riga.charAt(i));
				}
			}
		}
		int[] larghezze = new int[caratteri.length()];
		for (int i = 0; i < larghezze.length; i++) {
			larghezze[i] = font.getGlyphWidth(caratteri.charAt(i));
		}
		DisposizioneDeiGlifi disposizione = DisposizioneDeiGlifi.di(larghezze, font.getHeight(), LARGHEZZA_MASSIMA_DELLA_PAGINA);

		Pixmap pixmap = new Pixmap(disposizione.getLarghezza(), disposizione.getAltezza(), Pixmap.Format.RGBA8888);
		pixmap.setBlending(Pixmap.Blending.None);
		for (int i = 0; i < larghezze.length; i++) {
			char carattere = caratteri.charAt(i);
			DisposizioneDeiGlifi.Posto posto = disposizione.getPosto(i);
			for (int riga = 0; riga < font.getHeight(); riga++) {
				for (int x = 0; x < larghezze[i]; x++) {
					int livello = font.getLivello(carattere, x, riga);
					if (livello > 0) {
						// Bianco con l'alfa del livello: RGBA
						pixmap.drawPixel(posto.x + x, posto.y + riga, 0xFFFFFF00 | livello);
					}
				}
			}
		}
		pagina = new Texture(pixmap);
		pagina.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		pixmap.dispose();
		for (int i = 0; i < larghezze.length; i++) {
			DisposizioneDeiGlifi.Posto posto = disposizione.getPosto(i);
			TextureRegion regione = new TextureRegion(pagina, posto.x, posto.y, Math.max(1, larghezze[i]), font.getHeight());
			regione.flip(false, true);
			glifi.put(caratteri.charAt(i), regione);
		}
	}

	public DoomdarkFont getFont() {
		return font;
	}

	/**
	 * La larghezza della scritta, in pixel
	 */
	public int larghezza(String testo) {
		return FontTool.getWidth(font, testo);
	}

	/**
	 * Scrive una riga di testo con l'angolo in alto a sinistra in (x, y)
	 */
	public void disegna(Batch batch, String testo, float x, float y, DoomdarkColorModel.Color colore) {
		disegna(batch, testo, x, y, colore(colore), 1f);
	}

	/**
	 * Come sopra, di un colore qualunque (con la sua alfa) e ingrandita di <code>scala</code>: i glifi si allargano senza interpolazione. Serve agli sprite,
	 * che sfumano e si ingrandiscono.
	 */
	public void disegna(Batch batch, String testo, float x, float y, Color colore, float scala) {
		Color precedente = new Color(batch.getColor());
		batch.setColor(colore);
		float colonna = x;
		for (int i = 0; i < testo.length(); i++) {
			TextureRegion glifo = glifi.get(testo.charAt(i));
			if (glifo == null) {
				continue; // un carattere che il font non ha non lascia posto, come in FontTool.getWidth
			}
			batch.draw(glifo, colonna, y, glifo.getRegionWidth() * scala, font.getHeight() * scala);
			colonna += (font.getGlyphWidth(testo.charAt(i)) + font.getSpacing()) * scala;
		}
		batch.setColor(precedente);
	}

	/**
	 * Il colore del font come colore di libGDX, con quell'alfa
	 */
	public static Color coloreConAlfa(DoomdarkColorModel.Color colore, float alfa) {
		Color base = colore(colore);
		return new Color(base.r, base.g, base.b, alfa);
	}

	/**
	 * Scrive una riga di testo bianco la cui luminosità sfuma in verticale da <code>grigioInAlto</code> (cima della riga) a
	 * <code>grigioInBasso</code> (fondo), da 0 (nero) a 1 (bianco): i glifi hanno un colore per vertice, quindi la sfumatura è continua.
	 * Serve al testo scorrevole, che si spegne verso l'alto.
	 */
	public void disegnaConSfumatura(Batch batch, String testo, float x, float y, float grigioInAlto, float grigioInBasso) {
		float alto = Color.toFloatBits(grigioInAlto, grigioInAlto, grigioInAlto, 1f);
		float basso = Color.toFloatBits(grigioInBasso, grigioInBasso, grigioInBasso, 1f);
		float[] vertici = new float[20];
		float colonna = x;
		float altezza = font.getHeight();
		for (int i = 0; i < testo.length(); i++) {
			TextureRegion glifo = glifi.get(testo.charAt(i));
			if (glifo == null) {
				continue;
			}
			float x2 = colonna + glifo.getRegionWidth();
			float y2 = y + altezza;
			// Come SpriteBatch.draw(TextureRegion, x, y, w, h), che per il vertice in (x, y) usa v2 della regione e per quello in
			// (x, y2) usa v: x, y, colore, u, v per ognuno dei quattro vertici
			riempi(vertici, 0, colonna, y, alto, glifo.getU(), glifo.getV2());
			riempi(vertici, 5, colonna, y2, basso, glifo.getU(), glifo.getV());
			riempi(vertici, 10, x2, y2, basso, glifo.getU2(), glifo.getV());
			riempi(vertici, 15, x2, y, alto, glifo.getU2(), glifo.getV2());
			batch.draw(glifo.getTexture(), vertici, 0, 20);
			colonna += glifo.getRegionWidth() + font.getSpacing();
		}
	}

	private static void riempi(float[] vertici, int da, float x, float y, float colore, float u, float v) {
		vertici[da] = x;
		vertici[da + 1] = y;
		vertici[da + 2] = colore;
		vertici[da + 3] = u;
		vertici[da + 4] = v;
	}

	/**
	 * Scrive una scritta di un riquadro così com'è: nella sua posizione (o con il bordo destro lì, se allineata a destra), del suo colore, a capo entro la
	 * sua larghezza massima se ce l'ha, con l'ombra se la vuole
	 */
	public void disegna(Batch batch, VoceDiTesto voce) {
		float x = voce.isAllineataADestra() ? voce.getX() - larghezza(voce.getTesto()) : voce.getX();
		if (voce.isConOmbra()) {
			disegna(batch, voce.getTesto(), x + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA, voce.getY() + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA,
					DoomdarkColorModel.Color.BLACK);
		}
		if (voce.getLarghezzaMassima() > 0) {
			disegnaSuPiuRighe(batch, voce.getTesto(), x, voce.getY(), voce.getColore(), voce.getLarghezzaMassima());
		} else {
			disegna(batch, voce.getTesto(), x, voce.getY(), voce.getColore());
		}
	}

	/**
	 * L'intestazione di un riquadro: scritta grigia chiara con l'ombra nera dietro, centrata sul bordo superiore della cornice
	 */
	public void disegnaIntestazione(Batch batch, String titolo, int xCornice, int yCornice, int larghezzaCornice) {
		int x = PosizioneDellIntestazione.x(xCornice, larghezzaCornice, larghezza(titolo));
		int y = PosizioneDellIntestazione.y(yCornice);
		disegna(batch, titolo, x + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA, y + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA, DoomdarkColorModel.Color.BLACK);
		disegna(batch, titolo, x, y, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	/**
	 * Scrive una riga con l'ombra: prima in nero, spostata di <code>scostamentoOmbra</code> pixel in basso e a destra, poi il testo
	 */
	public void disegnaConOmbra(Batch batch, String testo, float x, float y, DoomdarkColorModel.Color colore, int scostamentoOmbra) {
		disegna(batch, testo, x + scostamentoOmbra, y + scostamentoOmbra, DoomdarkColorModel.Color.BLACK);
		disegna(batch, testo, x, y, colore);
	}

	/**
	 * Scrive il testo su più righe, andando a capo fra le parole entro la larghezza data; restituisce l'altezza occupata
	 */
	public int disegnaSuPiuRighe(Batch batch, String testo, float x, float y, DoomdarkColorModel.Color colore, int larghezzaMassima) {
		List<String> righe = FontTool.split(font, testo, larghezzaMassima);
		for (int r = 0; r < righe.size(); r++) {
			disegna(batch, righe.get(r), x, y + r * font.getHeight(), colore);
		}
		return righe.size() * font.getHeight();
	}

	private static synchronized Color colore(DoomdarkColorModel.Color colore) {
		Color gdx = COLORI.get(colore);
		if (gdx == null) {
			gdx = new Color((colore.getRgb() << 8) | 0xFF);
			COLORI.put(colore, gdx);
		}
		return gdx;
	}

	@Override
	public void dispose() {
		pagina.dispose();
	}
}
