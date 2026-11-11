package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.mappa.VistaMappa;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Punto;

import java.util.ArrayList;
import java.util.List;

/**
 * La minimappa della schermata di gioco, senza disegno: la finestra di 7 per 7 caselle attorno al gruppo (spostata quanto serve perché non esca dalla
 * foresta), le tessere conosciute (la casella del gruppo resta vuota: il segnalino lampeggia sopra), il segnalino, gli indicatori delle caselle da
 * segnalare e l'area in cui si vedono le nuvole. {@link #veduta()} dà tutto con le posizioni a schermo.
 */
public class RiquadroMappa implements Finestra {

	/**
	 * Lo spessore del bordo interno della cornice della mappa
	 */
	public static final int BORDO_INTERNO = 12;
	/**
	 * Il lato della finestra, in caselle
	 */
	public static final int LATO = 7;

	/**
	 * Una casella conosciuta da disegnare
	 */
	public static final class Tessera {
		private final int x;
		private final int y;
		private final TipoLocazione tipo;
		private final int variante;
		private final boolean visitata;

		public Tessera(int x, int y, TipoLocazione tipo, int variante, boolean visitata) {
			this.x = x;
			this.y = y;
			this.tipo = tipo;
			this.variante = variante;
			this.visitata = visitata;
		}

		/**
		 * Dove va l'angolo in alto a sinistra, in coordinate dello schermo
		 */
		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public TipoLocazione getTipo() {
			return tipo;
		}

		/**
		 * La variante del bosco (da 1), per le altre locazioni 0
		 */
		public int getVariante() {
			return variante;
		}

		/**
		 * Se il gruppo ci è già stato: la tessera si scurisce
		 */
		public boolean isVisitata() {
			return visitata;
		}
	}

	/**
	 * Quel che si vede nella minimappa adesso
	 */
	public static final class Veduta {
		private final int daX;
		private final int daY;
		private final List<Tessera> tessere;
		private final Punto segnalino;
		private final List<Punto> indicatori;
		private final boolean segnaliniVisibili;
		private final Area areaDelleNuvole;

		private Veduta(int daX, int daY, List<Tessera> tessere, Punto segnalino, List<Punto> indicatori, boolean segnaliniVisibili, Area areaDelleNuvole) {
			this.daX = daX;
			this.daY = daY;
			this.tessere = tessere;
			this.segnalino = segnalino;
			this.indicatori = indicatori;
			this.segnaliniVisibili = segnaliniVisibili;
			this.areaDelleNuvole = areaDelleNuvole;
		}

		/**
		 * La colonna e la riga della foresta all'angolo in alto a sinistra della finestra
		 */
		public int getDaX() {
			return daX;
		}

		public int getDaY() {
			return daY;
		}

		public List<Tessera> getTessere() {
			return tessere;
		}

		/**
		 * Dove disegnare il segnalino del gruppo e gli indicatori (solo se {@link #isSegnaliniVisibili()}: lampeggiano)
		 */
		public Punto getSegnalino() {
			return segnalino;
		}

		public List<Punto> getIndicatori() {
			return indicatori;
		}

		public boolean isSegnaliniVisibili() {
			return segnaliniVisibili;
		}

		/**
		 * Dove si vedono le nuvole: il quadrato della mappa dentro la cornice
		 */
		public Area getAreaDelleNuvole() {
			return areaDelleNuvole;
		}
	}

	protected final int topLeftX;
	// Sul telefono la mappa è impilata e la pila la sposta in verticale
	protected int topLeftY;
	protected final int larghezzaIcona;
	protected final int altezzaIcona;
	protected final VistaPartita vistaPartita;

	/**
	 * @param larghezzaIcona larghezza di una tessera della mappa, già con lo zoom
	 * @param altezzaIcona   altezza di una tessera
	 */
	public RiquadroMappa(int topLeftX, int topLeftY, int larghezzaIcona, int altezzaIcona, VistaPartita vistaPartita) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.larghezzaIcona = larghezzaIcona;
		this.altezzaIcona = altezzaIcona;
		this.vistaPartita = vistaPartita;
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	/**
	 * Sposta il riquadro in verticale: topLeftY è la nuova quota del bordo superiore della cornice
	 */
	public void spostaInVerticale(int topLeftY) {
		this.topLeftY = topLeftY;
	}

	/**
	 * La larghezza (e l'altezza) del quadrato della mappa dentro la cornice
	 */
	public int getLatoDellaMappa() {
		return LATO * larghezzaIcona;
	}

	/**
	 * Se i segnalini sono visibili in questo secondo: lampeggiano, un secondo sì e uno no
	 */
	public static boolean segnaliniVisibili() {
		return (System.currentTimeMillis() / 1000) % 2 == 0;
	}

	/**
	 * La prima colonna (o riga) della finestra di sette attorno al gruppo, spostata quanto serve per restare dentro la foresta
	 */
	public static int primaCasella(int gruppo, int dimensione) {
		int da = gruppo - 3;
		int a = gruppo + 3;
		if (da < 0) {
			da = 0;
			a = 6;
		}
		if (a >= dimensione) {
			da = dimensione - 7;
		}
		return da;
	}

	/**
	 * Dove si vedono le nuvole: il quadrato della mappa dentro la cornice, cioè spostato del bordo interno rispetto all'angolo in alto a sinistra del
	 * riquadro, in x con la x dell'angolo e in y con la sua y (che di solito sono diverse).
	 */
	public static Area areaDelleNuvole(int topLeftX, int topLeftY, int latoDellaMappa) {
		return new Area(topLeftX + BORDO_INTERNO, topLeftY + BORDO_INTERNO, latoDellaMappa, latoDellaMappa);
	}

	/**
	 * Quel che si vede adesso: la finestra attorno al gruppo, le tessere conosciute con la loro posizione a schermo, il segnalino e gli indicatori
	 */
	public Veduta veduta() {
		VistaMappa mappa = vistaPartita.getMappa();
		CoordinateMD gruppo = mappa.getPosizioneDelGruppo();
		int daX = primaCasella(gruppo.getX(), mappa.getDimensioneX());
		int daY = primaCasella(gruppo.getY(), mappa.getDimensioneY());
		int xOrigine = topLeftX + BORDO_INTERNO;
		int yOrigine = topLeftY + BORDO_INTERNO;

		List<Tessera> tessere = new ArrayList<>();
		for (int y = daY; y < daY + LATO; y++) {
			for (int x = daX; x < daX + LATO; x++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				if (coordinate.equals(gruppo) || !mappa.isLocazioneConosciuta(coordinate)) {
					continue;
				}
				TipoLocazione tipo = mappa.getLocazione(coordinate);
				int variante = tipo == TipoLocazione.BOSCO ? mappa.getVarianteBosco(coordinate) : 0;
				tessere.add(new Tessera(xOrigine + (x - daX) * larghezzaIcona, yOrigine + (y - daY) * altezzaIcona, tipo, variante,
						mappa.isLocazioneVisitata(coordinate)));
			}
		}
		Punto segnalino = new Punto(xOrigine + (gruppo.getX() - daX) * larghezzaIcona, yOrigine + (gruppo.getY() - daY) * altezzaIcona);
		List<Punto> indicatori = new ArrayList<>();
		for (CoordinateMD coordinate : mappa.getCoordinateDaSegnalare()) {
			int x = coordinate.getX();
			int y = coordinate.getY();
			if (x >= daX && x < daX + LATO && y >= daY && y < daY + LATO) {
				indicatori.add(new Punto(xOrigine + (x - daX) * larghezzaIcona, yOrigine + (y - daY) * altezzaIcona));
			}
		}
		return new Veduta(daX, daY, tessere, segnalino, indicatori, segnaliniVisibili(), areaDelleNuvole(topLeftX, topLeftY, getLatoDellaMappa()));
	}

	/**
	 * Lo sprite che segnala che la mappa è cambiata: l'icona della mappa (con la sua misura) al centro della cornice (con la sua misura), ferma e poi su
	 */
	public DescrizioneSpriteATempo spriteDellaMappaVariata(Misura icona, Misura cornice) {
		int x = topLeftX + ((cornice.larghezza - icona.larghezza) >> 1);
		int y = topLeftY + ((cornice.altezza - icona.altezza) >> 1);
		return DescrizioneSpriteATempo.immagine(NomiDelleImmagini.SPRITE_MAPPA, icona, x, y, "Mappa variata");
	}
}
