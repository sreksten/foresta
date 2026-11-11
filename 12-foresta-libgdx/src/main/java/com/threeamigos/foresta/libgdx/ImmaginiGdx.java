package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.threeamigos.foresta.ui.componenti.CorniceTagliata;
import com.threeamigos.foresta.ui.immagini.AtlanteDiImmagini;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Le immagini del gioco come regioni di texture, chieste per nome come in Swing (<code>"icone/Aiuto.png"</code>): se la cartella ha un atlante
 * (vedi {@link AtlanteDiImmagini} nel modulo comune) la regione è presa dalla sua pagina, che si carica una volta sola; altrimenti è un file
 * a sé (i loghi, i font, le scene grandi degli intermezzi). Le texture hanno il campionamento <code>NEAREST</code>, perché il pixel-art resti
 * nitido, e vivono nella memoria della scheda grafica: vanno liberate con {@link #dispose()}.
 * <p>
 * <b>Le coordinate sono quelle di Swing</b>, con l'asse y verso il basso e (0, 0) in alto a sinistra: la camera dell'interfaccia è capovolta
 * (<code>setToOrtho(true, …)</code>) e le regioni sono già capovolte in verticale, così riquadri e schermate riusano le loro coordinate.
 * Le immagini dei file sono a metà risoluzione e Swing le ingrandisce con lo zoom (<code>LivelloDiZoom</code>): chi disegna dà la scala.
 */
public final class ImmaginiGdx implements Disposable {

	private static final String RADICE = "com/threeamigos/foresta/img/";

	private final Map<String, Texture> pagine = new HashMap<>();
	private final Map<String, TextureRegion> regioni = new HashMap<>();
	private Texture pixelBianco;
	private final List<Texture> daPixel = new ArrayList<>();

	/**
	 * La regione di quell'immagine
	 *
	 * @param risorsa il nome come in Swing, relativo a <code>img/</code>
	 * @throws IllegalArgumentException se non c'è né nell'atlante né come file
	 */
	public TextureRegion regione(String risorsa) {
		TextureRegion regione = regioni.get(risorsa);
		if (regione == null) {
			regione = costruisci(risorsa);
			regioni.put(risorsa, regione);
		}
		return regione;
	}

	private TextureRegion costruisci(String risorsa) {
		int barra = risorsa.indexOf('/');
		if (barra > 0 && risorsa.endsWith(".png")) {
			String cartella = risorsa.substring(0, barra);
			AtlanteDiImmagini.Riquadro riquadro = AtlanteDiImmagini.di(cartella).getRiquadro(risorsa.substring(barra + 1, risorsa.length() - 4));
			if (riquadro != null) {
				Texture pagina = pagina(RADICE + cartella + "/atlante_" + cartella + ".png");
				return capovolta(new TextureRegion(pagina, riquadro.getX(), riquadro.getY(), riquadro.getLarghezza(), riquadro.getAltezza()));
			}
		}
		if (!Gdx.files.classpath(RADICE + risorsa).exists()) {
			throw new IllegalArgumentException("Immagine non trovata: " + risorsa);
		}
		return capovolta(new TextureRegion(pagina(RADICE + risorsa)));
	}

	private Texture pagina(String percorso) {
		Texture pagina = pagine.get(percorso);
		if (pagina == null) {
			pagina = new Texture(Gdx.files.classpath(percorso));
			pagina.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
			pagine.put(percorso, pagina);
		}
		return pagina;
	}

	private static TextureRegion capovolta(TextureRegion regione) {
		regione.flip(false, true);
		return regione;
	}

	/**
	 * Disegna l'immagine con l'angolo in alto a sinistra in (x, y), ingrandita di <code>scala</code> (2 per le immagini a metà risoluzione)
	 */
	public void disegna(Batch batch, String risorsa, float x, float y, float scala) {
		TextureRegion regione = regione(risorsa);
		batch.draw(regione, x, y, regione.getRegionWidth() * scala, regione.getRegionHeight() * scala);
	}

	/**
	 * Disegna una cornice a fasce (vedi CorniceTagliata): intera, o la sua parte di sopra e quella di sotto attaccate, con la cima in (x, y)
	 */
	public void disegnaAFasce(Batch batch, String risorsa, float x, float y, float scala, List<CorniceTagliata.Fascia> fasce) {
		int larghezza = regione(risorsa).getRegionWidth();
		for (CorniceTagliata.Fascia fascia : fasce) {
			if (fascia.getAltezzaSorgente() <= 0) {
				continue;
			}
			batch.draw(porzione(risorsa, 0, fascia.getYSorgente(), larghezza, fascia.getAltezzaSorgente()), x, y + fascia.getYDestinazione(), larghezza * scala,
					fascia.getAltezzaDestinazione());
		}
	}

	/**
	 * Quanto è grande l'immagine con quella scala (2 per le immagini a metà risoluzione), arrotondata
	 */
	public int larghezza(String risorsa, float scala) {
		return Math.round(regione(risorsa).getRegionWidth() * scala);
	}

	public int altezza(String risorsa, float scala) {
		return Math.round(regione(risorsa).getRegionHeight() * scala);
	}

	/**
	 * Una porzione dell'immagine (coordinate dell'immagine originale, senza zoom), già capovolta come tutto il resto
	 */
	public TextureRegion porzione(String risorsa, int x, int y, int larghezza, int altezza) {
		String chiave = risorsa + "#" + x + "," + y + "," + larghezza + "," + altezza;
		TextureRegion porzione = regioni.get(chiave);
		if (porzione == null) {
			TextureRegion base = regione(risorsa);
			// La base è già capovolta: getRegionY() darebbe il suo bordo basso, quindi l'angolo alto a sinistra si prende dalle coordinate della texture
			int xBase = Math.round(Math.min(base.getU(), base.getU2()) * base.getTexture().getWidth());
			int yBase = Math.round(Math.min(base.getV(), base.getV2()) * base.getTexture().getHeight());
			porzione = capovolta(new TextureRegion(base.getTexture(), xBase + x, yBase + y, larghezza, altezza));
			regioni.put(chiave, porzione);
		}
		return porzione;
	}

	/**
	 * Una regione di texture con i pixel dati (un testo composto, una nuvola, una sagoma), già capovolta come tutto il resto; la texture appartiene a
	 * questo oggetto e si libera con {@link #dispose()}
	 *
	 * @param lineare campionamento lineare (per le sfumature) invece che NEAREST (per il pixel-art)
	 */
	public TextureRegion regioneDaPixel(ImmagineDiPixel immagine, boolean lineare) {
		Pixmap pixmap = new Pixmap(immagine.getLarghezza(), immagine.getAltezza(), Pixmap.Format.RGBA8888);
		pixmap.setBlending(Pixmap.Blending.None);
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				int argb = immagine.getArgb(x, y);
				pixmap.drawPixel(x, y, (argb << 8) | (argb >>> 24));
			}
		}
		Texture texture = new Texture(pixmap);
		pixmap.dispose();
		Texture.TextureFilter filtro = lineare ? Texture.TextureFilter.Linear : Texture.TextureFilter.Nearest;
		texture.setFilter(filtro, filtro);
		daPixel.add(texture);
		return capovolta(new TextureRegion(texture));
	}

	/**
	 * Riempie un triangolo (coordinate dello schermo, angolo in alto a sinistra) con il colore di ogni vertice, che sfuma da uno all'altro: ogni colore è
	 * ARGB non premoltiplicato. Il colore del batch non c'entra.
	 */
	public void riempiTriangolo(Batch batch, float x1, float y1, int argb1, float x2, float y2, int argb2, float x3, float y3, int argb3) {
		riempiQuadrilatero(batch, x1, y1, argb1, x2, y2, argb2, x3, y3, argb3, x3, y3, argb3);
	}

	/**
	 * Riempie un quadrilatero con il colore di ogni vertice (in ordine attorno al bordo), come {@link #riempiTriangolo}
	 */
	public void riempiQuadrilatero(Batch batch, float x1, float y1, int argb1, float x2, float y2, int argb2, float x3, float y3, int argb3, float x4, float y4,
								   int argb4) {
		Texture bianco = pixelBianco();
		float u = 0.5f;
		float[] vertici = {
				x1, y1, bitsDelColore(argb1), u, u,
				x2, y2, bitsDelColore(argb2), u, u,
				x3, y3, bitsDelColore(argb3), u, u,
				x4, y4, bitsDelColore(argb4), u, u};
		batch.draw(bianco, vertici, 0, vertici.length);
	}

	private static float bitsDelColore(int argb) {
		return Color.toFloatBits(((argb >> 16) & 0xFF) / 255f, ((argb >> 8) & 0xFF) / 255f, (argb & 0xFF) / 255f, ((argb >>> 24) & 0xFF) / 255f);
	}

	private Texture pixelBianco() {
		if (pixelBianco == null) {
			Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
			pixmap.setColor(Color.WHITE);
			pixmap.fill();
			pixelBianco = new Texture(pixmap);
			pixmap.dispose();
		}
		return pixelBianco;
	}

	/**
	 * Riempie un rettangolo di un colore (con la sua alfa), con l'angolo in alto a sinistra in (x, y)
	 */
	public void riempi(Batch batch, float x, float y, float larghezza, float altezza, Color colore) {
		Texture pixelBianco = pixelBianco();
		Color precedente = new Color(batch.getColor());
		batch.setColor(colore);
		batch.draw(pixelBianco, x, y, larghezza, altezza);
		batch.setColor(precedente);
	}

	@Override
	public void dispose() {
		if (pixelBianco != null) {
			pixelBianco.dispose();
			pixelBianco = null;
		}
		for (Texture pagina : pagine.values()) {
			pagina.dispose();
		}
		pagine.clear();
		regioni.clear();
		for (Texture texture : daPixel) {
			texture.dispose();
		}
		daPixel.clear();
	}
}
