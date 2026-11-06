package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * I contorni di un logo, senza toolkit: le fasi 1-4 della pipeline di {@link AnimazioneDelLogo} (maschera di primo piano, tracciamento del bordo di Moore,
 * buchi interni, ricampionamento a distanza costante, verso di percorrenza). Chi la usa ne chiede i contorni di un'immagine con
 * {@link #di} e li riceve come {@link Forma}, già ricampionate.
 */
final class ContorniDelLogo {

	/** Un pixel dell'immagine */
	private static final class Pixel {
		final int x;
		final int y;

		Pixel(int x, int y) {
			this.x = x;
			this.y = y;
		}

		double distanza(Pixel altro) {
			return Math.hypot(x - altro.x, y - altro.y);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Pixel && ((Pixel) o).x == x && ((Pixel) o).y == y;
		}

		@Override
		public int hashCode() {
			return 31 * x + y;
		}
	}

	/** Il contorno ricampionato di una singola forma connessa (esterna o buco), nei due versi possibili. */
	static final class Forma {
		/** Ordine così come prodotto dal tracciamento */
		final List<AnimazioneDelLogo.Posizione> percorsoNaturale;
		/** Stesso contorno, ordine invertito (verso opposto) */
		final List<AnimazioneDelLogo.Posizione> percorsoInvertito;
		/** Vero se il percorso naturale è orario sullo schermo */
		final boolean naturaleOrario;
		/** Vero per un contorno esterno, falso per un buco interno */
		final boolean esterna;

		Forma(List<AnimazioneDelLogo.Posizione> percorsoNaturale, List<AnimazioneDelLogo.Posizione> percorsoInvertito, boolean naturaleOrario,
			  boolean esterna) {
			this.percorsoNaturale = percorsoNaturale;
			this.percorsoInvertito = percorsoInvertito;
			this.naturaleOrario = naturaleOrario;
			this.esterna = esterna;
		}
	}

	// Gli 8 vicini di un pixel, IN SENSO ORARIO, partendo da Ovest: DX[i]/DY[i] è lo spostamento per arrivare al vicino i-esimo.
	//   i=0 Ovest, i=1 Nord-Ovest, i=2 Nord, i=3 Nord-Est, i=4 Est, i=5 Sud-Est, i=6 Sud, i=7 Sud-Ovest
	private static final int[] DX = {-1, -1, 0, 1, 1, 1, 0, -1};
	private static final int[] DY = {0, -1, -1, -1, 0, 1, 1, 1};

	private ContorniDelLogo() {
	}

	/**
	 * Le forme dell'immagine (prima i contorni esterni, poi i buchi), con i punti nelle coordinate dell'immagine intera.
	 *
	 * @param sfondo  il criterio che dice quali pixel sono sfondo
	 * @param regione dove cercare il logo (null: in tutta l'immagine)
	 * @param punti   quanti punti equidistanti per ogni contorno
	 */
	static List<Forma> di(ImmagineDiPixel immagine, AnimazioneDelLogo.PredicatoSfondo sfondo, Area regione, int punti) {
		int x = regione != null ? regione.getX() : 0;
		int y = regione != null ? regione.getY() : 0;
		int larghezza = regione != null ? regione.getLarghezza() : immagine.getLarghezza();
		int altezza = regione != null ? regione.getAltezza() : immagine.getAltezza();
		boolean[][] maschera = costruisciMaschera(immagine, sfondo, x, y, larghezza, altezza);
		List<List<Pixel>> esterni = tracciaTuttiIContorni(maschera);
		List<List<Pixel>> buchi = tracciaTuttiIContorni(estraiBuchiRacchiusi(maschera));
		List<Forma> forme = new ArrayList<>();
		for (int i = 0; i < esterni.size() + buchi.size(); i++) {
			boolean esterna = i < esterni.size();
			List<Pixel> contorno = esterna ? esterni.get(i) : buchi.get(i - esterni.size());
			List<AnimazioneDelLogo.Posizione> naturale = ricampiona(contorno, punti, x, y);
			// il verso naturale è invariante per traslazione: non importa se si calcola prima o dopo aver sommato lo scostamento
			boolean orario = areaConSegno(naturale) > 0;
			List<AnimazioneDelLogo.Posizione> invertito = new ArrayList<>(naturale);
			java.util.Collections.reverse(invertito);
			forme.add(new Forma(naturale, invertito, orario, esterna));
		}
		return forme;
	}

	/** Quanti sono i contorni esterni fra le forme */
	static int esterne(List<Forma> forme) {
		int n = 0;
		for (Forma forma : forme) {
			if (forma.esterna) {
				n++;
			}
		}
		return n;
	}

	// ---- 1. Maschera primo piano/sfondo ----------------------------------------------------------------------------------------------

	private static boolean[][] costruisciMaschera(ImmagineDiPixel immagine, AnimazioneDelLogo.PredicatoSfondo sfondo, int x0, int y0, int larghezza,
												  int altezza) {
		// indicizzata [riga][colonna] = [y][x]
		boolean[][] maschera = new boolean[altezza][larghezza];
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				maschera[y][x] = !sfondo.isSfondo(x, y, immagine.getArgb(x0 + x, y0 + y));
			}
		}
		return maschera;
	}

	// ---- 2. Tracciamento del bordo di Moore (contorni esterni) -------------------------------------------------------------------

	/**
	 * Trova e traccia il contorno di OGNI forma connessa presente nella maschera (scansione riga per riga; ogni pixel di primo piano non ancora consumato
	 * avvia il tracciamento di una nuova forma, che viene poi marcata interamente come consumata con un riempimento).
	 */
	private static List<List<Pixel>> tracciaTuttiIContorni(boolean[][] maschera) {
		int h = maschera.length;
		int w = maschera[0].length;
		// quali pixel appartengono a una forma già trovata, per non ritracciarla
		boolean[][] consumati = new boolean[h][w];
		List<List<Pixel>> tuttiIContorni = new ArrayList<>();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (maschera[y][x] && !consumati[y][x]) {
					// il primo pixel di una forma mai vista è garantito essere un pixel di bordo: il pixel sopra e quello a sinistra sono sfondo
					List<Pixel> contorno = tracciaContornoDa(maschera, new Pixel(x, y));
					if (contorno.size() >= 4) {
						// scarta le forme degeneri (rumore di pochi pixel isolati)
						tuttiIContorni.add(contorno);
					}
					marcaComponenteConsumata(maschera, consumati, x, y);
				}
			}
		}
		return tuttiIContorni;
	}

	/** Riempimento (8-connesso) che marca come consumati tutti i pixel della forma a cui appartiene (sx, sy). */
	private static void marcaComponenteConsumata(boolean[][] maschera, boolean[][] consumati, int sx, int sy) {
		int h = maschera.length;
		int w = maschera[0].length;
		// con una pila esplicita, invece che ricorsivo: niente StackOverflowError su forme molto grandi
		Deque<Pixel> pila = new ArrayDeque<>();
		pila.push(new Pixel(sx, sy));
		consumati[sy][sx] = true;
		while (!pila.isEmpty()) {
			Pixel p = pila.pop();
			for (int i = 0; i < 8; i++) {
				int nx = p.x + DX[i];
				int ny = p.y + DY[i];
				if (dentroILimiti(nx, ny, w, h) && maschera[ny][nx] && !consumati[ny][nx]) {
					consumati[ny][nx] = true;
					pila.push(new Pixel(nx, ny));
				}
			}
		}
	}

	/**
	 * Traccia il contorno della forma a cui appartiene {@code partenza}, che deve essere già un pixel di bordo. Algoritmo di Moore: dal pixel corrente e
	 * dalla direzione da cui si è arrivati (il «ritorno») si esaminano gli 8 vicini in senso orario e si salta al primo di primo piano, finché non si
	 * torna al pixel di partenza.
	 */
	private static List<Pixel> tracciaContornoDa(boolean[][] maschera, Pixel partenza) {
		List<Pixel> contorno = new ArrayList<>();
		int h = maschera.length;
		int w = maschera[0].length;
		Pixel corrente = partenza;
		// il pixel a Ovest del primo è garantito sfondo: è il «ritorno» virtuale da cui si comincia
		Pixel ritorno = new Pixel(partenza.x - 1, partenza.y);
		contorno.add(partenza);

		// salvaguardia anti-loop-infinito: una maschera patologica non deve poter bloccare il programma
		long passiMassimi = (long) w * h * 2L;
		long passi = 0;
		while (passi++ < passiMassimi) {
			int direzioneIniziale = direzioneVerso(corrente, ritorno);
			Pixel prossimo = null;
			for (int i = 1; i <= 8; i++) {
				int direzione = (direzioneIniziale + i) % 8;
				int nx = corrente.x + DX[direzione];
				int ny = corrente.y + DY[direzione];
				if (dentroILimiti(nx, ny, w, h) && maschera[ny][nx]) {
					// il nuovo ritorno è il vicino appena precedente (l'ultimo che era ancora sfondo)
					prossimo = new Pixel(nx, ny);
					int direzionePrecedente = (direzione - 1 + 8) % 8;
					ritorno = new Pixel(corrente.x + DX[direzionePrecedente], corrente.y + DY[direzionePrecedente]);
					break;
				}
			}
			if (prossimo == null) {
				break; // pixel isolato: forma di un solo pixel
			}
			corrente = prossimo;
			if (corrente.equals(partenza)) {
				break; // il giro si è richiuso
			}
			contorno.add(corrente);
		}
		return contorno;
	}

	private static boolean dentroILimiti(int x, int y, int w, int h) {
		return x >= 0 && x < w && y >= 0 && y < h;
	}

	/** L'indice (0-7, vedi DX/DY) della direzione che porta da un pixel a un suo vicino. */
	private static int direzioneVerso(Pixel da, Pixel a) {
		int dx = a.x - da.x;
		int dy = a.y - da.y;
		for (int i = 0; i < 8; i++) {
			if (DX[i] == dx && DY[i] == dy) {
				return i;
			}
		}
		return 0;
	}

	// ---- 2bis. Buchi interni (aree di sfondo racchiuse dal logo) -----------------------------------------------------------------

	/**
	 * Una maschera con solo i pixel di sfondo che appartengono a una componente connessa (8-connessa) che non tocca mai il bordo dell'immagine: le aree
	 * racchiuse dal logo (il triangolo dentro la «A»). Si traccia come una maschera di primo piano.
	 */
	private static boolean[][] estraiBuchiRacchiusi(boolean[][] maschera) {
		int h = maschera.length;
		int w = maschera[0].length;
		boolean[][] visitati = new boolean[h][w];
		boolean[][] mascheraBuchi = new boolean[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (!maschera[y][x] && !visitati[y][x]) {
					List<Pixel> componente = new ArrayList<>();
					boolean toccaIlBordo = false;
					Deque<Pixel> pila = new ArrayDeque<>();
					pila.push(new Pixel(x, y));
					visitati[y][x] = true;
					while (!pila.isEmpty()) {
						Pixel p = pila.pop();
						componente.add(p);
						if (p.x == 0 || p.x == w - 1 || p.y == 0 || p.y == h - 1) {
							toccaIlBordo = true;
						}
						for (int i = 0; i < 8; i++) {
							int nx = p.x + DX[i];
							int ny = p.y + DY[i];
							if (dentroILimiti(nx, ny, w, h) && !maschera[ny][nx] && !visitati[ny][nx]) {
								visitati[ny][nx] = true;
								pila.push(new Pixel(nx, ny));
							}
						}
					}
					// solo le componenti che non toccano mai il bordo sono buchi
					if (!toccaIlBordo) {
						for (Pixel p : componente) {
							mascheraBuchi[p.y][p.x] = true;
						}
					}
				}
			}
		}
		return mascheraBuchi;
	}

	// ---- 3. Ricampionamento a distanza costante ----------------------------------------------------------------------------------

	/**
	 * Riduce un contorno pixel per pixel a {@code quanti} punti equidistanti lungo il suo perimetro (per lunghezza d'arco), così la velocità del punto
	 * luminoso non dipende dalla risoluzione né dalla forma dei tratti. I punti si portano nelle coordinate dell'immagine intera sommando lo scostamento
	 * della regione.
	 */
	private static List<AnimazioneDelLogo.Posizione> ricampiona(List<Pixel> contorno, int quanti, int scostamentoX, int scostamentoY) {
		int n = contorno.size();
		// lunghezza totale percorsa dal punto 0 fino al punto i (compreso il tratto di chiusura: il contorno è un anello)
		double[] lunghezzaCumulata = new double[n + 1];
		for (int i = 0; i < n; i++) {
			lunghezzaCumulata[i + 1] = lunghezzaCumulata[i] + contorno.get(i).distanza(contorno.get((i + 1) % n));
		}
		double totale = lunghezzaCumulata[n];

		List<AnimazioneDelLogo.Posizione> risultato = new ArrayList<>(quanti);
		int segmento = 0;
		for (int k = 0; k < quanti; k++) {
			double obiettivo = totale * k / quanti;
			while (segmento < n && lunghezzaCumulata[segmento + 1] < obiettivo) {
				segmento++;
			}
			Pixel a = contorno.get(segmento % n);
			Pixel b = contorno.get((segmento + 1) % n);
			double lunghezzaSegmento = lunghezzaCumulata[segmento + 1] - lunghezzaCumulata[segmento];
			double t = lunghezzaSegmento > 0 ? (obiettivo - lunghezzaCumulata[segmento]) / lunghezzaSegmento : 0;
			risultato.add(new AnimazioneDelLogo.Posizione(a.x + (b.x - a.x) * t + scostamentoX, a.y + (b.y - a.y) * t + scostamentoY));
		}
		return risultato;
	}

	// ---- 4. Verso di percorrenza (area di Gauss) ----------------------------------------------------------------------------------

	/**
	 * Area con segno del poligono (formula dell'area di Gauss), in coordinate immagine (y verso il basso): positiva se il percorso è orario sullo
	 * schermo, negativa se antiorario.
	 */
	private static double areaConSegno(List<AnimazioneDelLogo.Posizione> percorso) {
		double somma = 0;
		int n = percorso.size();
		for (int i = 0; i < n; i++) {
			AnimazioneDelLogo.Posizione a = percorso.get(i);
			AnimazioneDelLogo.Posizione b = percorso.get((i + 1) % n);
			somma += a.x * b.y - b.x * a.y;
		}
		return somma / 2.0;
	}
}
