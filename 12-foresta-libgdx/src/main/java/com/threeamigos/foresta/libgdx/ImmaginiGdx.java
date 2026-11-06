package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.threeamigos.foresta.ui.immagini.AtlanteDiImmagini;

import java.util.HashMap;
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
	 * Riempie un rettangolo di un colore (con la sua alfa), con l'angolo in alto a sinistra in (x, y)
	 */
	public void riempi(Batch batch, float x, float y, float larghezza, float altezza, Color colore) {
		if (pixelBianco == null) {
			Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
			pixmap.setColor(Color.WHITE);
			pixmap.fill();
			pixelBianco = new Texture(pixmap);
			pixmap.dispose();
		}
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
	}
}
