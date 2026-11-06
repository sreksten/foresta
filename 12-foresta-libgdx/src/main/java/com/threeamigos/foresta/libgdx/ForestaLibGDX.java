package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoFineLogoIniziale;
import com.threeamigos.foresta.eventi.interni.InternoInterfacciaUtentePronta;
import com.threeamigos.foresta.interfacce.ControlloreDiGioco;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.strumenti.temporizzatore.Temporizzatore;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.ui.DisposizioneDelloSchermo;
import com.threeamigos.foresta.ui.ForestaUI;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.RouterDegliInput;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import com.threeamigos.foresta.ui.sfx.AnimazioneDelLogo;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

/**
 * La classe radice dell'interfaccia in libGDX, gemella di <code>ForestaSwing</code>: l'applicazione libGDX che apre lo schermo
 * del gioco ({@link SchermoGdx}) a una risoluzione virtuale fissa, quella data dalle cornici dei riquadri, dentro un
 * <code>FitViewport</code> con le coordinate di Swing (y verso il basso). Gli eventi del bus li traduce la parte comune
 * ({@link ForestaUI}). In <code>create</code> avvia il thread principale, costruisce schermo e campo di testo e pubblica
 * {@code InternoInterfacciaUtentePronta}, e da lì l'automa parte; quando l'applicazione va in background o torna in primo piano lo
 * dice all'automa.
 * <p>
 * Il logo iniziale ({@link LogoInizialeGdx}) si vede prima dell'intro, a meno che non si chieda di saltarlo (<code>SALTALOGO</code>): in quel caso si
 * passa subito all'intro.
 */
public final class ForestaLibGDX extends ForestaUI implements ApplicationListener {

	private final Orientamento orientamento;
	private final EsecutoreGdx esecutore;
	private final ControlloreDiGioco controlloreDiGioco;
	private final GestorePunteggi gestorePunteggi;
	private final boolean barraDock;
	private final boolean joystickVirtuale;
	private final boolean saltaLogo;
	private final Misura dimensioni;

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Viewport viewport;
	private ImmaginiGdx immagini;
	private TestoGdx piccolo;
	private TestoGdx medio;
	private SchermoGdx schermo;
	private CampoDiTestoGdx campoDiTesto;
	private PianificatoreGdx pianificatore;
	// Solo durante lo stato LOGO_INIZIALE; poi il posto passa allo schermo del gioco
	private LogoInizialeGdx logoIniziale;

	/**
	 * @param orientamento      dove sta la barra delle icone: in fondo (orizzontale) o sul lato destro (verticale, per i telefoni)
	 * @param esecutore         il thread principale che il punto d'ingresso ha dato al motore (ThreadPrincipale): parte in create
	 * @param controlloreDiGioco l'automa, da avvisare quando l'applicazione va in background e torna in primo piano
	 * @param gestorePunteggi   la classifica da mostrare nell'intro (la stessa che riceve l'Automa)
	 * @param vistaPartita      lo stato della partita da mostrare, in sola lettura
	 * @param temporizzatore    il temporizzatore della UI (l'intro)
	 * @param barraDock         vero per la barra delle icone che si ingrandisce sotto il cursore
	 * @param joystickVirtuale  vero per il joystick a schermo sulla mappa
	 * @param saltaLogo         vero per non mostrare il logo iniziale (per esempio nelle partite di prova)
	 */
	public ForestaLibGDX(Orientamento orientamento, EsecutoreGdx esecutore, ControlloreDiGioco controlloreDiGioco, GestorePunteggi gestorePunteggi, VistaPartita vistaPartita,
						 Temporizzatore temporizzatore, boolean barraDock, boolean joystickVirtuale, boolean saltaLogo) {
		super(vistaPartita, temporizzatore);
		this.orientamento = orientamento;
		this.dimensioni = dimensioniDelloSchermo(orientamento);
		this.esecutore = esecutore;
		this.controlloreDiGioco = controlloreDiGioco;
		this.gestorePunteggi = gestorePunteggi;
		this.barraDock = barraDock;
		this.joystickVirtuale = joystickVirtuale;
		this.saltaLogo = saltaLogo;
	}

	/**
	 * La risoluzione virtuale: quella che serve per vedere tutti i riquadri e la barra delle icone, con lo zoom di lavoro. La legge
	 * dai file, senza scheda grafica: serve anche prima di aprire la finestra.
	 */
	public static Misura dimensioniDelloSchermo(Orientamento orientamento) {
		return DisposizioneDelloSchermo.dimensioniDelloSchermo(orientamento, new MisureDaiFile(), LivelloDiZoom.valore());
	}

	@Override
	public void create() {
		esecutore.avviato();
		camera = new OrthographicCamera();
		// L'asse y verso il basso, come Swing: le coordinate di riquadri e schermate si riusano tali e quali
		camera.setToOrtho(true, dimensioni.larghezza, dimensioni.altezza);
		viewport = new FitViewport(dimensioni.larghezza, dimensioni.altezza, camera);
		batch = new SpriteBatch();
		immagini = new ImmaginiGdx();
		piccolo = new TestoGdx(DoomdarkFont.piccolo());
		medio = new TestoGdx(DoomdarkFont.medio());
		DisposizioneDelloSchermo disposizione = new DisposizioneDelloSchermo(orientamento, dimensioni.larghezza, dimensioni.altezza,
				new MisureDaiFile(), LivelloDiZoom.valore());
		schermo = new SchermoGdx(disposizione, barraDock, gestorePunteggi, vistaPartita, immagini, piccolo, medio, camera, viewport);
		schermo.setJoystickVirtuale(joystickVirtuale);
		campoDiTesto = new CampoDiTestoGdx(immagini, medio, dimensioni.larghezza, dimensioni.altezza);
		interfacciaCompleta(schermo, campoDiTesto.getCampo());
		pianificatore = new PianificatoreGdx();
		RouterDegliInput router = new RouterDegliInput(schermo.contestoDegliInput(), pianificatore);
		// Il campo di testo, quando si vede, prende i tasti e i tocchi prima dello schermo
		// ...e durante il logo iniziale nessuno li riceve (in Swing il logo e' un pannello a se', che li assorbe)
		Gdx.input.setInputProcessor(new InputMultiplexer(campoDiTesto, new AssorbiDuranteIlLogo(), new InputGdx(router, viewport)));
		BusEventi.pubblica(new InternoInterfacciaUtentePronta());
	}

	/**
	 * Mentre il logo iniziale si vede assorbe tutti gli input, così non arrivano allo schermo che c'è sotto
	 */
	private final class AssorbiDuranteIlLogo extends InputAdapter {
		@Override
		public boolean keyDown(int keycode) {
			return logoIniziale != null;
		}

		@Override
		public boolean keyUp(int keycode) {
			return logoIniziale != null;
		}

		@Override
		public boolean keyTyped(char character) {
			return logoIniziale != null;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			return logoIniziale != null;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return logoIniziale != null;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return logoIniziale != null;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return logoIniziale != null;
		}

		@Override
		public boolean scrolled(float amountX, float amountY) {
			return logoIniziale != null;
		}
	}

	/**
	 * Fase LOGO_INIZIALE: fa partire l'animazione del logo (o, se va saltato, passa subito oltre). L'interfaccia è già completa: le immagini
	 * si caricano quando servono.
	 */
	@Override
	protected void avviaLogoIniziale() {
		if (saltaLogo) {
			BusEventi.pubblica(new InternoFineLogoIniziale());
			return;
		}
		logoIniziale = new LogoInizialeGdx(immagini, dimensioni.larghezza, dimensioni.altezza);
		temporizzatore.inizia(AnimazioneDelLogo.INTERVALLO_FOTOGRAMMA_MS);
	}

	/**
	 * Un fotogramma del logo iniziale. Quando l'animazione è finita il logo lascia il posto allo schermo e l'Automa viene avvisato.
	 */
	@Override
	protected void avanzaLogoIniziale() {
		if (logoIniziale == null) {
			return;
		}
		logoIniziale.avanza();
		if (logoIniziale.isFinito()) {
			temporizzatore.termina();
			logoIniziale.dispose();
			logoIniziale = null;
			BusEventi.pubblica(new InternoFineLogoIniziale());
		}
	}

	@Override
	public void resize(int larghezza, int altezza) {
		viewport.update(larghezza, altezza, true);
	}

	@Override
	public void render() {
		pianificatore.aggiorna();
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		viewport.apply();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		if (logoIniziale != null) {
			logoIniziale.disegna(batch);
		} else {
			schermo.disegna(batch);
			campoDiTesto.disegna(batch);
		}
		batch.end();
	}

	@Override
	public void pause() {
		temporizzatore.sospendi();
		controlloreDiGioco.inBackground();
	}

	@Override
	public void resume() {
		temporizzatore.riprendi();
		controlloreDiGioco.inPrimoPiano();
	}

	@Override
	public void dispose() {
		if (logoIniziale != null) {
			logoIniziale.dispose();
		}
		batch.dispose();
		immagini.dispose();
		piccolo.dispose();
		medio.dispose();
	}
}
