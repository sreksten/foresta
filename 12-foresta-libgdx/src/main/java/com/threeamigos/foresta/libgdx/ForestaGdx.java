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
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.ui.barra.ComandiPossibili;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.libgdx.sviluppo.PaginaDegliSprite;
import com.threeamigos.foresta.libgdx.sviluppo.PaginaDiProva;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.RouterDegliInput;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.nio.IntBuffer;
import java.util.Arrays;
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
	// La pagina di prova del testo e i riquadri provati (testo, gruppo, locazione, sfida)
	private static final int PAGINE_DI_PROVA = 9;

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Viewport viewport;
	private ImmaginiGdx immagini;
	private TestoGdx piccolo;
	private TestoGdx medio;
	private RiquadroTestoGdx riquadroTesto;
	private RiquadroGruppoGdx riquadroGruppo;
	private RiquadroLocazioneGdx riquadroLocazione;
	private RiquadroStatisticheGdx riquadroStatistiche;
	private RiquadroMappaGdx riquadroMappa;
	private NuvoleGdx nuvole;
	private TestoGrandeGdx testoGrande;
	private RiquadroSfidaGdx riquadroSfida;
	private RiquadroIncantesimiEPozioniGdx riquadroIncantesimi;
	private RiquadroCombattimentoGdx riquadroCombattimento;
	private RiquadroMissioniGdx riquadroMissioni;
	private PaginaDegliSprite paginaDegliSprite;
	private BarraIconeGdx barraIcone;
	private SchermataTrofeiGdx schermataTrofei;
	private SchermataBestiarioGdx schermataBestiario;
	private PianificatoreGdx pianificatore;
	/** 0 è la pagina di prova, 1 il riquadro del testo, 2 i riquadri del gruppo, delle statistiche, di incantesimi e pozioni e della mappa, 3 la locazione con il combattimento, 4 la sfida, 5 gli sprite, 6 la barra delle icone, 7 la pagina dei trofei, 8 il bestiario, poi un atlante per pagina */
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
		VistaPartita partita = DatiDiProva.partita();
		riquadroGruppo = new RiquadroGruppoGdx(MARGINE, MARGINE, partita, immagini, medio);
		riquadroLocazione = new RiquadroLocazioneGdx(Finestra.SPACING, Finestra.SPACING, 800, partita, immagini);
		riquadroLocazione.assegnaCoordinateAgliAvversari();
		riquadroIncantesimi = new RiquadroIncantesimiEPozioniGdx(MARGINE + 368 + 10, MARGINE, partita, immagini, medio);
		nuvole = new NuvoleGdx(immagini);
		testoGrande = new TestoGrandeGdx(immagini);
		riquadroSfida = new RiquadroSfidaGdx(LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE, immagini, testoGrande);
		riquadroSfida.aggiorna(MossaCartaForbiciSasso.SASSO, MossaCartaForbiciSasso.SASSO, 3, 1, true);
		// La barra delle icone in fondo allo schermo, come il Dock; i comandi sono tanti, perché scorrano
		ComandiPossibili.set(Arrays.asList(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2, Comando.PERSONAGGIO_3, Comando.NORD, Comando.EST, Comando.SUD,
				Comando.OVEST, Comando.MAPPA, Comando.INVENTARIO, Comando.FUOCO, Comando.GELO, Comando.POZIONE_SALUTE, Comando.POZIONE_MAGIA,
				Comando.FUGA, Comando.SI, Comando.NO, Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4, Comando.NUMERO_5,
				Comando.CARTA, Comando.FORBICE, Comando.SASSO));
		schermataTrofei = new SchermataTrofeiGdx(LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE, partita, immagini, medio, testoGrande);
		schermataTrofei.apri();
		schermataBestiario = new SchermataBestiarioGdx(LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE, partita, immagini, piccolo, medio);
		schermataBestiario.apri();
		barraIcone = new BarraIconeGdx(Orientamento.ORIZZONTALE, true, 0, ALTEZZA_VIRTUALE - 76, LARGHEZZA_VIRTUALE, 76, partita, immagini, piccolo, medio);
		barraIcone.getModello().impostaAzioni();
		paginaDegliSprite = new PaginaDegliSprite(immagini, piccolo, medio, LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE);
		riquadroMissioni = new RiquadroMissioniGdx(MARGINE + 368 + 10, MARGINE + 288 + 10, partita, immagini, piccolo, medio);
		riquadroMappa = new RiquadroMappaGdx(MARGINE + 2 * (368 + 10), MARGINE, partita, immagini, nuvole);
		riquadroStatistiche = new RiquadroStatisticheGdx(MARGINE, MARGINE + 288 + 10, partita, immagini, medio);
		riquadroCombattimento = new RiquadroCombattimentoGdx(LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE, Finestra.SPACING + 320 + Finestra.SPACING, partita, immagini, medio);
		riquadroCombattimento.setVisible(true);
		riquadroCombattimento.setCombattente(partita.getGruppoGiocatore().getPersonaggio(0));
		riquadroCombattimento.setAzione(partita.getGruppoGiocatore().getPersonaggio(0), "Attacca il goblin");
		// Gli eventi passano dal router del modulo comune, come in Swing: qui la sola finestra in prova è il riquadro del testo
		pianificatore = new PianificatoreGdx();
		RouterDegliInput router = new RouterDegliInput(new RouterDegliInput.Contesto() {
			@Override
			public Finestra getBarraIcone() {
				return corrente == 6 ? barraIcone.getModello() : null;
			}

			@Override
			public boolean contiene(Finestra finestra, int xLocale, int yLocale) {
				// La barra decide da sé quali punti sono suoi: le icone ingrandite escono dal suo rettangolo
				if (finestra == barraIcone.getModello()) {
					return barraIcone.getModello().contiene(xLocale, yLocale);
				}
				return RouterDegliInput.Contesto.super.contiene(finestra, xLocale, yLocale);
			}

			@Override
			public Finestra getFinestraATuttoSchermo() {
				return corrente == 7 ? schermataTrofei : corrente == 8 ? schermataBestiario : null;
			}

			@Override
			public List<Finestra> getFinestreImpilate() {
				return finestreDellaPagina();
			}

			@Override
			public Area getArea(Finestra finestra) {
				if (finestra == schermataTrofei || finestra == schermataBestiario) {
					return new Area(0, 0, LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE);
				}
				if (finestra == barraIcone.getModello()) {
					return new Area(barraIcone.getModello().getOffsetX(), barraIcone.getModello().getOffsetY(), barraIcone.getModello().getLarghezza(),
							barraIcone.getModello().getAltezza());
				}
				if (finestra == riquadroLocazione) {
					return new Area(Finestra.SPACING, Finestra.SPACING, 800, 320);
				} else if (finestra == riquadroCombattimento) {
					return riquadroCombattimento.getArea();
				} else if (finestra == riquadroGruppo) {
					return riquadroGruppo.getArea();
				} else if (finestra == riquadroMissioni) {
					return new Area(riquadroMissioni.getTopLeftX(), riquadroMissioni.getTopLeftY(), riquadroMissioni.getLarghezzaCornice(), 288);
				} else if (finestra == riquadroIncantesimi) {
					return new Area(riquadroIncantesimi.getTopLeftX(), riquadroIncantesimi.getTopLeftY(), riquadroIncantesimi.getLarghezzaCornice(), 288);
				}
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

	/**
	 * I riquadri della pagina corrente, dal fondo al primo piano (nelle pagine senza riquadri, quello del testo che non si vede: gli eventi non lo cambiano)
	 */
	private List<Finestra> finestreDellaPagina() {
		switch (corrente) {
			case 2:
				return Arrays.<Finestra>asList(riquadroGruppo, riquadroIncantesimi, riquadroMissioni);
			case 3:
				return Arrays.<Finestra>asList(riquadroLocazione, riquadroCombattimento);
			default:
				return Collections.<Finestra>singletonList(riquadroTesto);
		}
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
			corrente = (corrente + 1) % (ATLANTI.length + PAGINE_DI_PROVA);
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
		} else if (corrente == 2) {
			riquadroGruppo.disegna(batch, camera, viewport);
			riquadroIncantesimi.disegna(batch, camera, viewport);
			riquadroStatistiche.disegna(batch);
			riquadroMissioni.disegna(batch, camera, viewport);
			riquadroMappa.disegna(batch, camera, viewport, LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE);
		} else if (corrente == 3) {
			riquadroLocazione.disegna(batch, null, null);
			riquadroCombattimento.disegna(batch);
		} else if (corrente == 4) {
			riquadroSfida.disegna(batch, camera, viewport);
		} else if (corrente == 5) {
			paginaDegliSprite.disegna(batch, camera, viewport);
		} else if (corrente == 6) {
			barraIcone.disegna(batch, camera, viewport);
			barraIcone.disegnaAiuto(batch, LARGHEZZA_VIRTUALE, ALTEZZA_VIRTUALE);
		} else if (corrente == 7) {
			schermataTrofei.disegna(batch, camera, viewport);
		} else if (corrente == 8) {
			schermataBestiario.disegna(batch, camera, viewport, testoGrande);
		} else {
			disegnaUnAtlante(ATLANTI[corrente - PAGINE_DI_PROVA]);
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
