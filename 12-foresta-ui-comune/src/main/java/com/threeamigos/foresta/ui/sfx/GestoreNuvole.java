package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.mappa.VistaMappa;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Genera e mantiene un unico set di nuvole condiviso fra le varie viste della mappa (il riquadro della mappa e la mappa a tutto schermo), in modo che
 * risultino coerenti fra loro quando si passa da una vista all'altra. Le nuvole vagano su tutta la foresta, anche sulle caselle che il gruppo non conosce
 * ancora: l'area su cui nascono e si muovono è quella della mappa intera, disegnata come la mostra la mappa a tutto schermo (centrata, con la casella
 * (0, 0) in alto a sinistra). Non disegna: dà a chi disegna (Swing, libGDX) le immagini e le posizioni ({@link #nuvolePerLaVista}).
 */
public final class GestoreNuvole {

	private static final int NUMERO_NUVOLE = 12;

	/**
	 * Una nuvola da disegnare: la sua immagine e dove, in coordinate dello schermo
	 */
	public static final class NuvolaVisibile {
		private final ImmagineDiPixel immagine;
		private final int x;
		private final int y;

		private NuvolaVisibile(ImmagineDiPixel immagine, int x, int y) {
			this.immagine = immagine;
			this.x = x;
			this.y = y;
		}

		public ImmagineDiPixel getImmagine() {
			return immagine;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	// Da dove viene il caso con cui nascono le nuvole: nel gioco ogni volta un altro; i test lo fissano con un seme
	private static Supplier<Random> fonteDelCaso = Random::new;
	private static List<Nuvola> nuvole;
	// -1 = nessuna chiamata precedente ad aggiorna(): il prossimo impulso non fa avanzare le nuvole, si limita a registrare il timestamp.
	private static long ultimoAggiornamentoNanos = -1;
	private static Area areaCanonica;
	// Cella della foresta (colonna/riga) a cui corrisponde l'angolo in alto a sinistra dell'area canonica
	private static int daXCanonico;
	private static int daYCanonico;
	private static int larghezzaIconaCanonica;
	private static int altezzaIconaCanonica;

	private GestoreNuvole() {
	}

	/**
	 * Per i test: le nuvole nascono dal caso dato (con un seme, sempre le stesse); null rimette quello del gioco.
	 */
	public static void impostaFonteDelCaso(Supplier<Random> fonte) {
		fonteDelCaso = fonte == null ? Random::new : fonte;
	}

	/**
	 * Genera le nuvole se ancora non ci sono: l'insieme è uno solo e lo condividono il riquadro della mappa e la mappa a tutto schermo.
	 */
	public static void assicuraGenerate(VistaMappa mappa, int larghezza, int altezza, int larghezzaIcona, int altezzaIcona) {
		if (nuvole == null) {
			genera(mappa, larghezza, altezza, larghezzaIcona, altezzaIcona);
		}
	}

	/**
	 * Dimentica le nuvole: le prossime si generano sull'area della partita che comincia (o che si carica), non su quella della partita di prima.
	 */
	public static void azzera() {
		nuvole = null;
		areaCanonica = null;
		ultimoAggiornamentoNanos = -1;
	}

	/**
	 * Fa avanzare tutte le nuvole del tempo reale trascorso dall'ultima chiamata.
	 */
	public static void aggiorna() {
		if (nuvole == null) {
			return;
		}
		long ora = System.nanoTime();
		if (ultimoAggiornamentoNanos >= 0) {
			float secondiTrascorsi = (ora - ultimoAggiornamentoNanos) / 1_000_000_000f;
			for (Nuvola nuvola : nuvole) {
				nuvola.aggiorna(secondiTrascorsi);
			}
		}
		ultimoAggiornamentoNanos = ora;
	}

	/**
	 * Dove disegnare le nuvole, traslando le loro coordinate (definite rispetto alla mappa a tutto schermo, la vista "di riferimento") in base alla porzione
	 * di foresta e alla posizione a schermo mostrate dalla vista che chiama. Nessuno scaling: le nuvole hanno la stessa dimensione ovunque, solo la loro
	 * posizione viene riallineata in modo che una nuvola sopra una data cella della foresta appaia nello stesso punto relativo sia nella mappa a tutto
	 * schermo sia nel riquadro mappa in gioco (che ne mostra solo una finestra più piccola, centrata sul gruppo).
	 *
	 * @param daXVista       colonna della foresta mostrata all'angolo in alto a sinistra della vista
	 * @param daYVista       riga della foresta mostrata all'angolo in alto a sinistra della vista
	 * @param xVista         coordinata dello schermo x di quell'angolo in alto a sinistra
	 * @param yVista         coordinata dello schermo y di quell'angolo in alto a sinistra
	 * @param larghezzaIcona larghezza in pixel di una cella della mappa in questa vista
	 * @param altezzaIcona   altezza in pixel di una cella della mappa in questa vista
	 * @return le nuvole, vuoto se non ce ne sono
	 */
	public static List<NuvolaVisibile> nuvolePerLaVista(int daXVista, int daYVista, int xVista, int yVista, int larghezzaIcona, int altezzaIcona) {
		List<NuvolaVisibile> visibili = new ArrayList<>();
		if (nuvole == null || areaCanonica == null) {
			return visibili;
		}
		int deltaX = (xVista - daXVista * larghezzaIcona) - (areaCanonica.getX() - daXCanonico * larghezzaIconaCanonica);
		int deltaY = (yVista - daYVista * altezzaIcona) - (areaCanonica.getY() - daYCanonico * altezzaIconaCanonica);
		for (Nuvola nuvola : nuvole) {
			visibili.add(new NuvolaVisibile(nuvola.getImmagine(), nuvola.getX() + deltaX, nuvola.getY() + deltaY));
		}
		return visibili;
	}

	private static void genera(VistaMappa mappa, int larghezza, int altezza, int larghezzaIcona, int altezzaIcona) {
		Random caso = fonteDelCaso.get();

		// Tutta la foresta, non solo la parte conosciuta
		int larghezzaMappa = mappa.getDimensioneX() * larghezzaIcona;
		int altezzaMappa = mappa.getDimensioneY() * altezzaIcona;
		int coordinataSchermoX = (larghezza - larghezzaMappa) >> 1;
		int coordinataSchermoY = (altezza - altezzaMappa) >> 1;

		areaCanonica = new Area(coordinataSchermoX, coordinataSchermoY, larghezzaMappa, altezzaMappa);
		daXCanonico = 0;
		daYCanonico = 0;
		larghezzaIconaCanonica = larghezzaIcona;
		altezzaIconaCanonica = altezzaIcona;

		nuvole = new ArrayList<>();
		for (int i = 0; i < NUMERO_NUVOLE; i++) {
			// Con un'area conosciuta piccola o a corridoio i limiti non devono scendere sotto uno: nextInt(0) lancia, e un'immagine di zero pixel non si
			// crea. La nuvola, poi, non è più alta dell'area
			int larghezzaMinima = Math.max(1, (int) (areaCanonica.getLarghezza() * 0.3));
			int larghezzaNuvola = larghezzaMinima + caso.nextInt(larghezzaMinima);
			int altezzaNuvola = Math.max(1, Math.min(larghezzaNuvola / 2, areaCanonica.getAltezza()));

			ImmagineDiPixel disegno = GeneratoreDiNuvole.genera(larghezzaNuvola, altezzaNuvola, caso);

			float xIniziale = areaCanonica.getX() + caso.nextInt(areaCanonica.getLarghezza()) - larghezzaNuvola;
			float yIniziale = areaCanonica.getY() + caso.nextInt(Math.max(1, areaCanonica.getAltezza() - altezzaNuvola));

			// Pixel al secondo: le nuvole più grandi (quindi più vicine) scorrono più veloci
			float velocita = 3.5f + ((float) larghezzaNuvola / areaCanonica.getLarghezza()) * 13.5f;

			nuvole.add(new Nuvola(disegno, xIniziale, yIniziale, velocita, areaCanonica));
		}
	}
}
