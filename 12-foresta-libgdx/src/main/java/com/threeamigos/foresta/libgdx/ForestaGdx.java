package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.libgdx.sviluppo.PaginaDiProva;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.RouterDegliInput;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Il punto di partenza dell'interfaccia libGDX: una finestra con la risoluzione virtuale del layout (1142x656, come Swing con lo zoom
 * di lavoro) e le coordinate di Swing (y verso il basso). All'avvio scrive nel log i limiti della scheda grafica (la dimensione massima
 * delle texture); poi mostra, una pagina alla volta (spazio o tocco per passare alla successiva), una pagina di prova del testo e delle
 * immagini chieste per nome ({@link TestoGdx}, {@link ImmaginiGdx}) e le pagine degli atlanti. Serve a verificare la catena: atlanti dal
 * modulo comune, texture con campionamento NEAREST, glifi dei font, viewport che si adatta alla finestra.
 */
public class ForestaGdx extends ApplicationAdapter {

	public static final int LARGHEZZA_VIRTUALE = 1142;
	public static final int ALTEZZA_VIRTUALE = 656;
	private static final String ETICHETTA_LOG = "Foresta";
	private static final String[] ATLANTI = {"icone", "mappa", "oggetti", "locazioni", "fondi", "fondinon2x2", "personaggi"};
	private static final int MARGINE = 20;

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Viewport viewport;
	private ImmaginiGdx immagini;
	private TestoGdx piccolo;
	private TestoGdx medio;
	private RiquadroTestoGdx riquadroTesto;
	private PianificatoreGdx pianificatore;
	/** 0 è la pagina di prova, 1 il riquadro del testo, poi un atlante per pagina */
	private int corrente;

	@Override
	public void create() {
		Gdx.app.log(ETICHETTA_LOG, "GL_VERSION = " + Gdx.gl.glGetString(GL20.GL_VERSION));
		Gdx.app.log(ETICHETTA_LOG, "GL_RENDERER = " + Gdx.gl.glGetString(GL20.GL_RENDERER));
		Gdx.app.log(ETICHETTA_LOG, "GL_MAX_TEXTURE_SIZE = " + dimensioneMassimaDelleTexture());

		// Per chi sviluppa: -Dforesta.pagina=1 parte dalla pagina indicata
		corrente = Integer.getInteger("foresta.pagina", 0);
		camera = new OrthographicCamera();
		// L'asse y verso il basso, come Swing: le coordinate di riquadri e schermate si riusano tali e quali
		camera.setToOrtho(true, LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE);
		viewport = new FitViewport(LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE, camera);
		batch = new SpriteBatch();
		immagini = new ImmaginiGdx();
		piccolo = new TestoGdx(DoomdarkFont.piccolo());
		medio = new TestoGdx(DoomdarkFont.medio());
		// Il riquadro del testo sotto la locazione, come in Swing (a metà risoluzione, con lo zoom)
		riquadroTesto = new RiquadroTestoGdx(MARGINE, 340, 700, 300, immagini, medio);
		for (String messaggio : PaginaDiProva.messaggiDiEsempio()) {
			riquadroTesto.addString(messaggio);
		}
		// Gli eventi passano dal router del modulo comune, come in Swing: qui la sola finestra in prova è il riquadro del testo
		pianificatore = new PianificatoreGdx();
		RouterDegliInput router = new RouterDegliInput(new RouterDegliInput.Contesto() {
			@Override
			public Finestra getBarraIcone() {
				return null;
			}

			@Override
			public Finestra getFinestraATuttoSchermo() {
				return null;
			}

			@Override
			public List<Finestra> getFinestreImpilate() {
				return corrente == 1 ? Collections.<Finestra>singletonList(riquadroTesto) : Collections.<Finestra>emptyList();
			}

			@Override
			public Area getArea(Finestra finestra) {
				return new Area(riquadroTesto.getTopLeftX(), riquadroTesto.getTopLeftY(), riquadroTesto.getWidth(), riquadroTesto.getHeight());
			}

			@Override
			public boolean portaInPrimoPianoSeImpilato(Finestra finestra) {
				return false;
			}

			@Override
			public Object getStato() {
				return corrente;
			}

			@Override
			public void ridisegna() {
				// si ridisegna a ogni fotogramma
			}
		}, pianificatore);
		Gdx.input.setInputProcessor(new InputGdx(router, viewport));
	}

	private static int dimensioneMassimaDelleTexture() {
		IntBuffer valore = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, valore);
		return valore.get(0);
	}

	@Override
	public void resize(int larghezza, int altezza) {
		viewport.update(larghezza, altezza, true);
	}

	@Override
	public void render() {
		pianificatore.aggiorna();
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
			corrente = (corrente + 1) % (ATLANTI.length + 2);
		}
		Gdx.gl.glClearColor(0.16f, 0.16f, 0.16f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		viewport.apply();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (corrente == 0) {
			PaginaDiProva.disegna(batch, immagini, piccolo, medio);
		} else if (corrente == 1) {
			riquadroTesto.disegna(batch, camera, viewport);
		} else {
			disegnaUnAtlante(ATLANTI[corrente - 2]);
		}
		batch.end();
	}

	private void disegnaUnAtlante(String cartella) {
		String nome = cartella + "/atlante_" + cartella + ".png";
		com.badlogic.gdx.graphics.g2d.TextureRegion pagina = immagini.regione(nome);
		// Intera se ci sta, altrimenti ridotta quanto serve per stare nella finestra virtuale
		float scala = Math.min(2f, Math.min((LARGHEZZA_VIRTUALE - 2f * MARGINE) / pagina.getRegionWidth(),
				(ALTEZZA_VIRTUALE - 2f * MARGINE) / pagina.getRegionHeight()));
		float larghezza = pagina.getRegionWidth() * scala;
		float altezza = pagina.getRegionHeight() * scala;
		batch.draw(pagina, (LARGHEZZA_VIRTUALE - larghezza) / 2f, (ALTEZZA_VIRTUALE - altezza) / 2f, larghezza, altezza);
	}

	@Override
	public void dispose() {
		batch.dispose();
		immagini.dispose();
		piccolo.dispose();
		medio.dispose();
	}
}
