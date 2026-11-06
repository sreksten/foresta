package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.schermate.SchermataMappa;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.componenti.JoystickVirtuale;
import com.threeamigos.foresta.ui.immagini.ImmaginiDelJoystick;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.riquadri.RiquadroMappa.Tessera;
import com.threeamigos.foresta.ui.schermate.MappaATuttoSchermo;
import com.threeamigos.foresta.ui.schermate.StrisciaDelleNotizie;
import com.threeamigos.foresta.ui.sfx.GestoreNuvole;

/**
 * La mappa della foresta a tutto schermo in libGDX: dove sta la mappa, le tessere, il segnalino, il nome sotto il mouse, il joystick con la mano e il
 * notiziario sono del modulo comune ({@link MappaATuttoSchermo}, {@link StrisciaDelleNotizie}, gli stessi di Swing); qui c'è il disegno. Le tessere si
 * disegnano una per una (solo quelle che si vedono), non con un'immagine di tutta la foresta come in Swing; la striscia del notiziario pezzo per pezzo,
 * ripetuta affiancata; il disco del joystick è un'immagine calcolata pixel per pixel ({@link ImmaginiDelJoystick}).
 */
public final class SchermataMappaGdx implements SchermataMappa {

	private static final float OSCURAMENTO_DELLE_VISITATE = 0.5f;
	private static final float OPACITA_DELLE_NUVOLE = 0.45f;
	// Il notiziario si ferma a metà della larghezza della sfera magica, che copre l'altra metà
	private static final int PASSI_DELLA_DISSOLVENZA = 20;

	private final int width;
	private final int height;
	private final VistaPartita vistaPartita;
	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final NuvoleGdx nuvole;
	private final MappaATuttoSchermo modello;
	private final StrisciaDelleNotizie striscia;
	private final CartiglioGdx cartiglio;
	private final int larghezzaSferaCoperta;
	private final int altezzaDellaSfera;
	private final Color sfondoDelNotiziario = new Color((StrisciaDelleNotizie.SFONDO << 8) | 0xFF);
	// Dove sta il centro della palla di vetro nello sprite della sfera, dall'alto (il vetro va dalla riga 13 alla 71, il piedistallo dalla 72)
	private static final int CENTRO_DEL_VETRO = 42;
	private TextureRegion disco;
	private TextureRegion indicatore;

	public SchermataMappaGdx(int width, int height, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx medio, NuvoleGdx nuvole) {
		float zoom = (float) LivelloDiZoom.valore();
		String bosco = NomiDelleImmagini.tessera(TipoLocazione.BOSCO);
		this.width = width;
		this.height = height;
		this.vistaPartita = vistaPartita;
		this.immagini = immagini;
		this.medio = medio;
		this.nuvole = nuvole;
		this.cartiglio = new CartiglioGdx(immagini, medio);
		this.larghezzaSferaCoperta = immagini.larghezza(NomiDelleImmagini.SFERA_MAGICA, 1f) / 2;
		this.altezzaDellaSfera = immagini.altezza(NomiDelleImmagini.SFERA_MAGICA, 1f);
		this.modello = new MappaATuttoSchermo(width, height, vistaPartita, immagini.larghezza(bosco, zoom), immagini.altezza(bosco, zoom));
		this.striscia = new StrisciaDelleNotizie(width - larghezzaSferaCoperta, vistaPartita);
	}

	public MappaATuttoSchermo getModello() {
		return modello;
	}

	/** Abilita il joystick virtuale: un disco in basso a destra che, premuto, muove una mano-puntatore. */
	public void abilitaJoystickVirtuale() {
		modello.abilitaJoystickVirtuale();
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		float zoom = (float) LivelloDiZoom.valore();
		int larghezzaIcona = modello.getLarghezzaIcona();
		int altezzaIcona = modello.getAltezzaIcona();
		modello.rivalidaLOffset();
		int altezzaMappa = modello.altezzaMappa();
		int xOffset = modello.getMappaXOffset();
		int yOffset = modello.getMappaYOffset();

		// La mappa e le nuvole stanno sopra il notiziario, senza debordare nella fascia in basso
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, 0, 0, width, altezzaMappa);
		for (Tessera tessera : modello.getTessere()) {
			int x = xOffset + tessera.getX();
			int y = yOffset + tessera.getY();
			if (x + larghezzaIcona <= 0 || x >= width || y + altezzaIcona <= 0 || y >= altezzaMappa) {
				continue;
			}
			String risorsa = tessera.getTipo() == TipoLocazione.BOSCO ? NomiDelleImmagini.tesseraDelBosco(tessera.getVariante())
					: NomiDelleImmagini.tessera(tessera.getTipo());
			immagini.disegna(batch, risorsa, x, y, zoom);
			if (tessera.isVisitata()) {
				immagini.riempi(batch, x, y, larghezzaIcona, altezzaIcona, new Color(0f, 0f, 0f, OSCURAMENTO_DELLE_VISITATE));
			}
		}
		if (modello.isSegnaliniVisibili()) {
			Punto segnalino = modello.getSegnalino();
			immagini.disegna(batch, NomiDelleImmagini.SEGNALINO, segnalino.x, segnalino.y, zoom);
			for (Punto indicatoreDellaCasella : modello.getIndicatori()) {
				immagini.disegna(batch, NomiDelleImmagini.INDICATORE, indicatoreDellaCasella.x, indicatoreDellaCasella.y, zoom);
			}
		}
		disegnaLeNuvole(batch, camera, viewport, xOffset, yOffset, larghezzaIcona, altezzaIcona);
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}

		if (modello.isNotiziarioVisibile()) {
			// La fascia si alza verso la palla di vetro della sfera, quanto basta ad averne il centro all'altezza del centro del vetro, ma senza toccare
			// la mappa: si ferma al suo bordo inferiore piu' il gap (se sotto la mappa non c'e' spazio resta in fondo)
			int sollevamentoMassimo = altezzaDellaSfera - CENTRO_DEL_VETRO - modello.getAltezzaIcona() / 2;
			disegnaIlNotiziario(batch, camera, viewport, modello.yDelNotiziario(sollevamentoMassimo));
		}
		disegnaIlJoystick(batch);
		cartiglio.disegnaAccantoAlMouse(batch, modello.righeDelCartiglio(), modello.getXMouse(), modello.getYMouse(), width, altezzaMappa);
		if (modello.isNotiziarioVisibile()) {
			// La sfera per ultima, in primo piano come in Swing: il notiziario comincia dal centro della palla e le notizie si vedono dentro il vetro
			immagini.disegna(batch, NomiDelleImmagini.SFERA_MAGICA, 0, height - altezzaDellaSfera, 1f);
		}
	}

	private void disegnaLeNuvole(Batch batch, Camera camera, Viewport viewport, int xOffset, int yOffset, int larghezzaIcona, int altezzaIcona) {
		int larghezzaMappa = vistaPartita.getMappa().getDimensioneX() * larghezzaIcona;
		int altezzaMappaIntera = vistaPartita.getMappa().getDimensioneY() * altezzaIcona;
		// Le nuvole non si vedono fuori dalla mappa quando questa è più piccola dello schermo
		boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, xOffset, yOffset, larghezzaMappa, altezzaMappaIntera);
		Color precedente = new Color(batch.getColor());
		batch.setColor(1f, 1f, 1f, OPACITA_DELLE_NUVOLE);
		GestoreNuvole.assicuraGenerate(vistaPartita.getMappa(), width, height, larghezzaIcona, altezzaIcona);
		nuvole.disegna(batch, 0, 0, xOffset, yOffset, larghezzaIcona, altezzaIcona);
		batch.setColor(precedente);
		if (ritagliato) {
			RitaglioGdx.esci(batch);
		}
		GestoreNuvole.aggiorna();
	}

	private void disegnaIlNotiziario(Batch batch, Camera camera, Viewport viewport, int yBanda) {
		striscia.avanza(System.nanoTime());
		int altezza = modello.getAltezzaIcona();
		int larghezza = striscia.getLarghezza();
		immagini.riempi(batch, larghezzaSferaCoperta, yBanda, larghezza, altezza, sfondoDelNotiziario);
		if (striscia.getLarghezzaTotale() > 0) {
			boolean ritagliato = RitaglioGdx.entra(batch, camera, viewport, larghezzaSferaCoperta, yBanda, larghezza, altezza);
			int yTesto = yBanda + (altezza - striscia.getFont().getHeight()) / 2;
			// Tiling: la stessa striscia ripetuta affiancata finché riempie la fascia
			for (int x = striscia.getX(); x < larghezza; x += striscia.getLarghezzaTotale()) {
				for (StrisciaDelleNotizie.Pezzo pezzo : striscia.getPezzi()) {
					medio.disegna(batch, pezzo.getTesto(), larghezzaSferaCoperta + x + pezzo.getX(), yTesto, pezzo.getColore());
				}
			}
			if (ritagliato) {
				RitaglioGdx.esci(batch);
			}
		}
		// A destra la striscia si dissolve nello sfondo
		int larghezzaDellaZona = StrisciaDelleNotizie.LARGHEZZA_ZONA_DI_DISSOLVENZA;
		int passo = larghezzaDellaZona / PASSI_DELLA_DISSOLVENZA;
		for (int i = 0; i < PASSI_DELLA_DISSOLVENZA; i++) {
			Color colore = new Color(sfondoDelNotiziario);
			colore.a = (i + 1f) / PASSI_DELLA_DISSOLVENZA;
			immagini.riempi(batch, larghezzaSferaCoperta + larghezza - larghezzaDellaZona + i * passo, yBanda, passo, altezza, colore);
		}
	}

	private void disegnaIlJoystick(Batch batch) {
		JoystickVirtuale joystick = modello.getJoystick();
		if (joystick == null) {
			return;
		}
		modello.aggiornaLaMano(System.nanoTime());
		if (disco == null) {
			disco = immagini.regioneDaPixel(ImmaginiDelJoystick.disco(), true);
			indicatore = immagini.regioneDaPixel(ImmaginiDelJoystick.indicatore(), true);
		}
		batch.draw(disco, joystick.getCentroX() - JoystickVirtuale.RAGGIO, joystick.getCentroY() - JoystickVirtuale.RAGGIO);
		if (joystick.isAttivo()) {
			double[] scostamento = joystick.getScostamentoDellIndicatore();
			batch.draw(indicatore, (float) (joystick.getCentroX() + scostamento[0] - JoystickVirtuale.RAGGIO_INDICATORE),
					(float) (joystick.getCentroY() + scostamento[1] - JoystickVirtuale.RAGGIO_INDICATORE));
		}
		if (modello.isManoVisibile()) {
			immagini.disegna(batch, NomiDelleImmagini.MANO_PUNTATORE, modello.getXMouse() - MappaATuttoSchermo.PUNTA_DELLA_MANO_X,
					modello.getYMouse() - MappaATuttoSchermo.PUNTA_DELLA_MANO_Y, 1f);
		}
	}

	@Override
	public void processaMovimento(int x, int y) {
		modello.processaMovimento(x, y);
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		modello.processaPressione(x, y, tasto);
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		modello.processaRilascio(x, y, tasto);
	}

	@Override
	public void processaUscita(int x, int y) {
		modello.processaUscita(x, y);
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		modello.processaTrascinamento(x, y);
	}

	@Override
	public boolean catturaIlTrascinamento() {
		return modello.catturaIlTrascinamento();
	}

	/**
	 * Nel disco del joystick la pressione serve subito (vedi {@link MappaATuttoSchermo#vuoleLaPressioneSubito}): senza questo inoltro il router, che parla
	 * con questa classe e non con il modello, avrebbe il valore predefinito (falso) e col dito il joystick non si attiverebbe.
	 */
	@Override
	public boolean vuoleLaPressioneSubito(int x, int y) {
		return modello.vuoleLaPressioneSubito(x, y);
	}

	@Override
	public boolean gestisceDoppioClick() {
		return modello.gestisceDoppioClick();
	}

	@Override
	public void processaDoppioClick(int x, int y, Tasto tasto) {
		modello.processaDoppioClick(x, y, tasto);
	}
}
