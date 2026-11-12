package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Il contenuto di una colonna che scorre ({@link ColonnaScorrevole}), come descrizione senza immagini: un elenco di {@link Elemento} con la loro posizione
 * (coordinate relative all'angolo in alto a sinistra del contenuto) e le misure del contenuto. Chi disegna (Swing, libGDX) mette il contenuto centrato in
 * orizzontale nell'area, spostato in su dello scorrimento, e ne disegna gli elementi.
 */
public final class ContenutoDiColonna {

	/**
	 * Una cosa da disegnare
	 */
	public static final class Elemento {

		/**
		 * Che cos'è
		 */
		public enum Tipo {
			/**
			 * Un testo a capo, nel font piccolo o medio (<code>righe</code>, <code>font</code>, <code>colore</code>)
			 */
			TESTO,
			/**
			 * Un testo nell'alfabeto grande, già in pixel (<code>pixel</code>)
			 */
			TESTO_GRANDE,
			/**
			 * Un'immagine per nome (<code>risorsa</code>), alla misura data, eventualmente specchiata o ridotta a una sagoma di un colore
			 */
			IMMAGINE,
			/**
			 * Un rettangolo pieno di un colore (ARGB)
			 */
			RETTANGOLO
		}

		private final Tipo tipo;
		private final int x;
		private final int y;
		private int larghezza;
		private int altezza;
		private List<String> righe;
		private DoomdarkFont font;
		private DoomdarkColorModel.Color colore;
		private ImmagineDiPixel pixel;
		private String chiave;
		private String risorsa;
		private boolean specchiata;
		private Integer sagomaRgb;
		private int argb;
		private int raggioDellAlone;
		private float opacita = 1f;
		// Di un'immagine troppo larga si disegna solo una striscia: da dove comincia (nell'immagine) e quanto è larga l'immagine intera (0: tutta)
		private int xDellaStriscia;
		private int larghezzaIntera;

		private Elemento(Tipo tipo, int x, int y) {
			this.tipo = tipo;
			this.x = x;
			this.y = y;
		}

		public static Elemento testo(List<String> righe, DoomdarkFont font, DoomdarkColorModel.Color colore, int x, int y) {
			Elemento e = new Elemento(Tipo.TESTO, x, y);
			e.righe = righe;
			e.font = font;
			e.colore = colore;
			e.altezza = righe.size() * font.getHeight();
			return e;
		}

		public static Elemento testoGrande(ImmagineDiPixel pixel, int x, int y) {
			return testoGrande(pixel, null, x, y);
		}

		/**
		 * @param chiave una chiave che dice che i pixel sono sempre gli stessi per quella chiave: chi disegna può tenere la sua texture e non rifarla ogni
		 *               volta che il contenuto si ricostruisce (null se non serve)
		 */
		public static Elemento testoGrande(ImmagineDiPixel pixel, String chiave, int x, int y) {
			Elemento e = new Elemento(Tipo.TESTO_GRANDE, x, y);
			e.chiave = chiave;
			e.pixel = pixel;
			e.larghezza = pixel.getLarghezza();
			e.altezza = pixel.getAltezza();
			return e;
		}

		/**
		 * @param sagomaRgb il colore della sagoma, o null per l'immagine com'è
		 */
		public static Elemento immagine(String risorsa, int larghezza, int altezza, boolean specchiata, Integer sagomaRgb, int x, int y) {
			Elemento e = new Elemento(Tipo.IMMAGINE, x, y);
			e.risorsa = risorsa;
			e.larghezza = larghezza;
			e.altezza = altezza;
			e.specchiata = specchiata;
			e.sagomaRgb = sagomaRgb;
			return e;
		}

		/**
		 * Un'immagine con attorno un alone nero sfumato di quel raggio (come le lettere della storia e i loghi dell'intro: si leggono anche su uno sfondo
		 * chiaro): l'angolo in alto a sinistra dell'immagine sta in (x, y), l'alone esce di raggio pixel per lato
		 */
		public static Elemento immagineConAlone(String risorsa, int larghezza, int altezza, int raggioDellAlone, int x, int y) {
			Elemento e = immagine(risorsa, larghezza, altezza, false, null, x, y);
			e.raggioDellAlone = raggioDellAlone;
			return e;
		}

		/**
		 * Un'immagine per nome, alla misura data, opaca solo in parte (da 0 a 1): per le icone che non si possono usare
		 */
		public static Elemento immagineConOpacita(String risorsa, int larghezza, int altezza, float opacita, int x, int y) {
			Elemento e = immagine(risorsa, larghezza, altezza, false, null, x, y);
			e.opacita = opacita;
			return e;
		}

		/**
		 * Un'immagine centrata su xCentro, che non esce da larghezzaMassima: se è più larga se ne disegna solo la striscia centrale (i separatori, che hanno
		 * il motivo nel mezzo, in una colonna più stretta di loro)
		 */
		public static Elemento immagineCentrata(String risorsa, int larghezza, int altezza, int larghezzaMassima, int xCentro, int y) {
			if (larghezza <= larghezzaMassima) {
				return immagine(risorsa, larghezza, altezza, false, null, xCentro - larghezza / 2, y);
			}
			int visibile = Math.max(1, larghezzaMassima);
			Elemento e = immagine(risorsa, visibile, altezza, false, null, xCentro - visibile / 2, y);
			e.larghezzaIntera = larghezza;
			e.xDellaStriscia = (larghezza - visibile) / 2;
			return e;
		}

		public static Elemento rettangolo(int larghezza, int altezza, int argb, int x, int y) {
			Elemento e = new Elemento(Tipo.RETTANGOLO, x, y);
			e.larghezza = larghezza;
			e.altezza = altezza;
			e.argb = argb;
			return e;
		}

		/** Se dell'immagine si disegna solo una striscia (vedi {@link #immagineCentrata}): allora {@link #getLarghezza()} è quella della striscia */
		public boolean isStriscia() {
			return larghezzaIntera > 0;
		}

		/** Da dove comincia la striscia, nell'immagine intera */
		public int getXDellaStriscia() {
			return xDellaStriscia;
		}

		public Tipo getTipo() {
			return tipo;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		/**
		 * La larghezza (non per i testi) e l'altezza dell'elemento, nelle coordinate del contenuto
		 */
		public int getLarghezza() {
			return larghezza;
		}

		public int getAltezza() {
			return altezza;
		}

		public List<String> getRighe() {
			return righe;
		}

		public DoomdarkFont getFont() {
			return font;
		}

		public DoomdarkColorModel.Color getColore() {
			return colore;
		}

		public ImmagineDiPixel getPixel() {
			return pixel;
		}

		/**
		 * La chiave dei pixel di un testo grande, se c'è (vedi {@link #testoGrande(ImmagineDiPixel, String, int, int)})
		 */
		public String getChiave() {
			return chiave;
		}

		public String getRisorsa() {
			return risorsa;
		}

		public boolean isSpecchiata() {
			return specchiata;
		}

		/**
		 * Il colore (RGB) della sagoma, o null se l'immagine si disegna com'è
		 */
		public Integer getSagomaRgb() {
			return sagomaRgb;
		}

		/**
		 * Il raggio dell'alone attorno all'immagine (0 se non ce l'ha)
		 */
		public int getRaggioDellAlone() {
			return raggioDellAlone;
		}

		public int getArgb() {
			return argb;
		}

		/**
		 * Quanto è opaco l'elemento (1 se non si è detto altro), per chi lo disegna
		 */
		public float getOpacita() {
			return opacita;
		}
	}

	private final int larghezza;
	private final int altezza;
	private final List<Elemento> elementi;

	public ContenutoDiColonna(int larghezza, int altezza, List<Elemento> elementi) {
		this.larghezza = Math.max(1, larghezza);
		this.altezza = Math.max(1, altezza);
		this.elementi = Collections.unmodifiableList(new ArrayList<>(elementi));
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	public List<Elemento> getElementi() {
		return elementi;
	}
}
