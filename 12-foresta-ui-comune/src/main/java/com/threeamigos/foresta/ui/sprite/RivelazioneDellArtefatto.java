package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.TestoDeiModificatori;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.LongSupplier;

/**
 * La rivelazione di un artefatto trovato in un cofano o in un tempio, senza disegno: un cerchio luminoso con l'oggetto sopra, raggi di luce che girano in senso
 * antiorario allungandosi e ritraendosi, scintille, e sotto un pannello scuro con nome, descrizione ed effetti. Più l'artefatto spicca (vedi {@link #splendore}),
 * più il cerchio è grande e i raggi sono numerosi e lunghi; se ha incantamenti, i raggi hanno i colori dei loro elementi.
 * <p>
 * Tempi: un punto di luce che si gonfia, il cerchio che si apre con un piccolo rimbalzo, la sosta, e l'uscita, in cui cerchio e oggetto volano rimpicciolendo
 * verso il riquadro del gruppo. Il gioco continua sotto. Il tempo passa in secondi veri, non a ogni disegno: la UI ridisegna anche per il mouse che si muove, e la
 * rivelazione non deve accelerare.
 * <p>
 * {@link #fotogramma(float)} dice che cosa disegnare a un istante, con le coordinate dello schermo e i colori come RGB (0xRRGGBB) e alfa da 0 a 1; chi disegna
 * (Swing, libGDX) lo mostra con i suoi mezzi (sfumature radiali, triangoli con il colore ai vertici).
 */
public final class RivelazioneDellArtefatto {

	private static final float DURATA = PaginaIntermezzo.SECONDI_RIVELAZIONE_ARTEFATTO;
	// Con altre rivelazioni in coda (un cofano nei castelli ne vale cinque) si accorcia la sosta
	private static final float DURATA_CON_CODA = 2.2f;
	private static final float ANTICIPAZIONE = 0.25f;
	private static final float APERTURA = 0.35f;
	private static final float USCITA = 0.5f;

	// Per gradino di splendore (0-3): raggio del cerchio, numero di raggi, lunghezza massima in raggi, scintille
	private static final int[] RAGGIO = {64, 76, 88, 100};
	private static final int[] RAGGI = {8, 12, 16, 24};
	private static final float[] LUNGHEZZA = {1.8f, 2.1f, 2.5f, 3.0f};
	private static final int[] SCINTILLE = {0, 6, 12, 22};
	private static final float[] LAMPO = {0f, 0f, 0.35f, 0.55f};
	private static final int[] COLORE = {0xFFECBE, 0xFFD65C, 0xFFBE46, 0xFFE178};
	private static final int BIANCO = 0xFFFFFF;
	private static final Map<TipoDanno, Integer> COLORI_ELEMENTI = new EnumMap<>(TipoDanno.class);

	// Il margine del pannello e lo spazio fra le sue righe
	private static final int MARGINE_PANNELLO = 8;
	private static final int DIVARIO_FRA_LE_RIGHE = 3;
	/** Lo spazio fra il cerchio e il pannello */
	private static final int SPAZIO_SOTTO_IL_CERCHIO = 12;

	static {
		COLORI_ELEMENTI.put(TipoDanno.ARIA, 0xC8EBFF);
		COLORI_ELEMENTI.put(TipoDanno.ACQUA, 0x468CFF);
		COLORI_ELEMENTI.put(TipoDanno.TERRA, 0xBE8746);
		COLORI_ELEMENTI.put(TipoDanno.FUOCO, 0xFF781E);
		COLORI_ELEMENTI.put(TipoDanno.FULMINE, 0xFFF55A);
		COLORI_ELEMENTI.put(TipoDanno.GELO, 0x96DCFF);
		COLORI_ELEMENTI.put(TipoDanno.ACIDO, 0xA0EB28);
		COLORI_ELEMENTI.put(TipoDanno.SONICO, 0xE6C8FF);
		COLORI_ELEMENTI.put(TipoDanno.VELENO, 0x5AC846);
		COLORI_ELEMENTI.put(TipoDanno.NECROTICO, 0x78AA64);
		COLORI_ELEMENTI.put(TipoDanno.SACRO, 0xFFFAD7);
		COLORI_ELEMENTI.put(TipoDanno.PSICHICO, 0xEB6EDC);
		COLORI_ELEMENTI.put(TipoDanno.ARCANO, 0xA573FF);
		COLORI_ELEMENTI.put(TipoDanno.VUOTO, 0x6E50BE);
		COLORI_ELEMENTI.put(TipoDanno.MALEDIZIONE, 0xAA3278);
	}

	/**
	 * Un raggio di luce: un triangolo con la base vicino al cerchio e la punta lontano, il cui colore sfuma con la distanza dal centro del cerchio, dall'alfa
	 * (vicino) a zero (alla punta)
	 */
	public static final class Raggio {
		public final float centroX;
		public final float centroY;
		/** Il raggio del cerchio, dove la sfumatura comincia */
		public final float base;
		/** La distanza della punta dal centro, dove la sfumatura finisce */
		public final float lunghezza;
		/** I tre vertici: i due della base e la punta, nell'ordine {x1, y1, x2, y2, x3, y3} */
		public final float[] vertici;
		public final int rgb;
		public final float alfa;

		private Raggio(float centroX, float centroY, float base, float lunghezza, float[] vertici, int rgb, float alfa) {
			this.centroX = centroX;
			this.centroY = centroY;
			this.base = base;
			this.lunghezza = lunghezza;
			this.vertici = vertici;
			this.rgb = rgb;
			this.alfa = alfa;
		}
	}

	/**
	 * Un cerchio sfumato (l'alone o il cerchio pieno), con la sfumatura radiale che parte dal suo fuoco
	 */
	public static final class Cerchio {
		public final float centroX;
		public final float centroY;
		public final float raggio;
		/** Il punto da cui parte la sfumatura (il centro per l'alone, in alto a sinistra per il cerchio pieno) e il raggio della sfumatura */
		public final float fuocoX;
		public final float fuocoY;
		public final float raggioDellaSfumatura;
		/** Le tre tinte della sfumatura, a 0, 55% e 100% del raggio, come 0xRRGGBB, e la loro alfa */
		public final int[] tinte;
		public final float[] alfe;

		private Cerchio(float centroX, float centroY, float raggio, float fuocoX, float fuocoY, float raggioDellaSfumatura, int[] tinte, float[] alfe) {
			this.centroX = centroX;
			this.centroY = centroY;
			this.raggio = raggio;
			this.fuocoX = fuocoX;
			this.fuocoY = fuocoY;
			this.raggioDellaSfumatura = raggioDellaSfumatura;
			this.tinte = tinte;
			this.alfe = alfe;
		}

		/**
		 * Il colore (ARGB, non premoltiplicato) della sfumatura in quel punto
		 */
		public int argbIn(float px, float py) {
			float d = (float) Math.min(1.0, Math.hypot(px - fuocoX, py - fuocoY) / raggioDellaSfumatura);
			float[] fermate = {0f, 0.55f, 1f};
			int da = d <= fermate[1] ? 0 : 1;
			float u = (d - fermate[da]) / (fermate[da + 1] - fermate[da]);
			float alfa = alfe[da] + (alfe[da + 1] - alfe[da]) * u;
			int r = mescola((tinte[da] >> 16) & 0xFF, (tinte[da + 1] >> 16) & 0xFF, u);
			int g = mescola((tinte[da] >> 8) & 0xFF, (tinte[da + 1] >> 8) & 0xFF, u);
			int b = mescola(tinte[da] & 0xFF, tinte[da + 1] & 0xFF, u);
			return (Math.round(255 * Math.max(0, Math.min(1, alfa))) << 24) | (r << 16) | (g << 8) | b;
		}

		private static int mescola(int a, int b, float u) {
			return Math.round(a + (b - a) * u);
		}
	}

	/**
	 * Una scintilla a stella a quattro punte, con la sua luminosità
	 */
	public static final class Stella {
		public final float x;
		public final float y;
		/** La semidiagonale */
		public final float d;
		public final float alfa;

		private Stella(float x, float y, float d, float alfa) {
			this.x = x;
			this.y = y;
			this.d = d;
			this.alfa = alfa;
		}
	}

	/**
	 * L'oggetto sul cerchio: dov'è e quanto è grande (al più alle sue dimensioni vere: ingrandito, il disegno si sgranerebbe)
	 */
	public static final class Oggetto {
		public final int x;
		public final int y;
		public final int larghezza;
		public final int altezza;
		public final float alfa;

		private Oggetto(int x, int y, int larghezza, int altezza, float alfa) {
			this.x = x;
			this.y = y;
			this.larghezza = larghezza;
			this.altezza = altezza;
			this.alfa = alfa;
		}
	}

	/**
	 * Una riga del pannello: del testo, anche a capo entro la sua larghezza massima (le righe dentro sono allineate a sinistra), che sta centrato nel pannello
	 */
	public static final class Riga {
		private final String testo;
		private final DoomdarkColorModel.Color colore;
		private final int larghezzaMassima;
		private final int larghezza;
		private final int altezza;
		private final int x;
		private final int y;

		private Riga(String testo, DoomdarkColorModel.Color colore, int larghezzaMassima, int larghezza, int altezza, int x, int y) {
			this.testo = testo;
			this.colore = colore;
			this.larghezzaMassima = larghezzaMassima;
			this.larghezza = larghezza;
			this.altezza = altezza;
			this.x = x;
			this.y = y;
		}

		public String getTesto() {
			return testo;
		}

		public DoomdarkColorModel.Color getColore() {
			return colore;
		}

		/** La larghezza oltre la quale il testo va a capo */
		public int getLarghezzaMassima() {
			return larghezzaMassima;
		}

		public int getLarghezza() {
			return larghezza;
		}

		public int getAltezza() {
			return altezza;
		}

		/** Dove comincia, relativo all'angolo in alto a sinistra del pannello */
		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	/**
	 * Il pannello scuro sotto il cerchio con nome ed effetti: dov'è, quanto è grande e le sue righe
	 */
	public static final class Pannello {
		public final int x;
		public final int y;
		public final int larghezza;
		public final int altezza;
		/** Il colore del bordo, come 0xRRGGBB, e la sua alfa */
		public final int rgbDelBordo;
		public final float alfaDelBordo;
		private final List<Riga> righe;

		private Pannello(int x, int y, int larghezza, int altezza, int rgbDelBordo, float alfaDelBordo, List<Riga> righe) {
			this.x = x;
			this.y = y;
			this.larghezza = larghezza;
			this.altezza = altezza;
			this.rgbDelBordo = rgbDelBordo;
			this.alfaDelBordo = alfaDelBordo;
			this.righe = righe;
		}

		public List<Riga> getRighe() {
			return righe;
		}
	}

	/**
	 * Quel che si vede a un istante
	 */
	public static final class Fotogramma {
		/** Dove ritagliare (finché non vola via resta nella sua area), o null */
		public final Area ritaglio;
		public final Area area;
		/** Il velo nero sullo sfondo e il lampo bianco dell'apertura, come alfa da 0 a 255 */
		public final int alfaDelVelo;
		public final int alfaDelLampo;
		/** Il cerchio e quel che porta con sé; vuoti se è ancora troppo piccolo per vedersi */
		public final List<Raggio> raggi;
		public final Cerchio alone;
		public final Cerchio sfera;
		public final float centroX;
		public final float centroY;
		public final float raggio;
		/** Il bordo del cerchio: bianco con questa alfa */
		public final float alfaDelBordo;
		public final List<Stella> stelle;
		public final Oggetto oggetto;
		/** Il pannello e la sua opacità (0: non si vede) */
		public final Pannello pannello;
		public final float opacitaDelPannello;

		private Fotogramma(Area ritaglio, Area area, int alfaDelVelo, int alfaDelLampo, List<Raggio> raggi, Cerchio alone, Cerchio sfera, float centroX,
							float centroY, float raggio, float alfaDelBordo, List<Stella> stelle, Oggetto oggetto, Pannello pannello, float opacitaDelPannello) {
			this.ritaglio = ritaglio;
			this.area = area;
			this.alfaDelVelo = alfaDelVelo;
			this.alfaDelLampo = alfaDelLampo;
			this.raggi = raggi;
			this.alone = alone;
			this.sfera = sfera;
			this.centroX = centroX;
			this.centroY = centroY;
			this.raggio = raggio;
			this.alfaDelBordo = alfaDelBordo;
			this.stelle = stelle;
			this.oggetto = oggetto;
			this.pannello = pannello;
			this.opacitaDelPannello = opacitaDelPannello;
		}

		/** Se il cerchio è abbastanza grande da disegnarsi */
		public boolean isCerchioVisibile() {
			return sfera != null;
		}
	}

	private final int gradino;
	private final int raggio;
	private final int colore;
	private final List<Integer> coloriRaggi = new ArrayList<>();
	private final Area area;
	private final int centroX;
	private final int centroY;
	private final Punto destinazione;
	private final Misura misuraDellOggetto;
	private final float[][] scintille;
	private final Pannello pannello;

	private float durata = DURATA;
	private float secondi;
	private boolean attivo = true;
	private final LongSupplier orologioNanosecondi;
	private long ultimoFotogrammaNanosecondi = -1;

	/**
	 * @param area                dove sta la rivelazione, centrata: tutto lo schermo
	 * @param destinazione        dove vola all'uscita (il riquadro del gruppo)
	 * @param misuraDellOggetto   quanto è grande l'immagine dell'oggetto (vedi {@link #risorsaDellOggetto}), o null se non ce n'è una
	 * @param orologioNanosecondi l'orologio con cui si misura il tempo che passa fra un disegno e l'altro: nel gioco System.nanoTime
	 */
	public RivelazioneDellArtefatto(VistaArtefatto artefatto, int livelloMondo, Area area, Punto destinazione, Misura misuraDellOggetto,
									LongSupplier orologioNanosecondi) {
		this.orologioNanosecondi = orologioNanosecondi;
		this.gradino = splendore(artefatto, livelloMondo);
		this.raggio = RAGGIO[gradino];
		this.colore = COLORE[gradino];
		this.area = area;
		this.destinazione = destinazione;
		this.misuraDellOggetto = misuraDellOggetto;
		for (IncantamentoMD incantamento : artefatto.getIncantamenti()) {
			coloriRaggi.add(COLORI_ELEMENTI.getOrDefault(incantamento.getTipoDannoElementale(), colore));
		}
		if (coloriRaggi.isEmpty()) {
			coloriRaggi.add(colore);
			if (gradino == 3) {
				coloriRaggi.add(BIANCO);
			}
		}
		int larghezzaMassima = Math.min(area.getLarghezza() - 48, 900);
		List<Riga> righe = righeDelPannello(artefatto, larghezzaMassima);
		int larghezzaInterna = 0;
		int altezzaInterna = 0;
		for (Riga riga : righe) {
			larghezzaInterna = Math.max(larghezzaInterna, riga.larghezza);
			altezzaInterna += riga.altezza + DIVARIO_FRA_LE_RIGHE;
		}
		int larghezzaPannello = larghezzaInterna + 2 * MARGINE_PANNELLO;
		int altezzaPannello = altezzaInterna - DIVARIO_FRA_LE_RIGHE + 2 * MARGINE_PANNELLO;
		int altezzaTotale = 2 * raggio + SPAZIO_SOTTO_IL_CERCHIO + altezzaPannello;
		int alto = area.getY() + Math.max(6, (area.getAltezza() - altezzaTotale) / 2);
		this.centroX = area.getX() + area.getLarghezza() / 2;
		this.centroY = alto + raggio;
		// Il pannello si centra sotto il cerchio, senza uscire dall'area
		int pannelloX = Math.max(area.getX() + 4, Math.min(area.getX() + area.getLarghezza() - 4 - larghezzaPannello, centroX - larghezzaPannello / 2));
		List<Riga> disposte = new ArrayList<>();
		int y = MARGINE_PANNELLO;
		for (Riga riga : righe) {
			disposte.add(new Riga(riga.testo, riga.colore, riga.larghezzaMassima, riga.larghezza, riga.altezza, (larghezzaPannello - riga.larghezza) / 2, y));
			y += riga.altezza + DIVARIO_FRA_LE_RIGHE;
		}
		this.pannello = new Pannello(pannelloX, centroY + raggio + SPAZIO_SOTTO_IL_CERCHIO, larghezzaPannello, altezzaPannello, colore, 0.6f,
				Collections.unmodifiableList(disposte));
		Random random = new Random(artefatto.getNome().hashCode());
		scintille = new float[SCINTILLE[gradino]][];
		for (int i = 0; i < scintille.length; i++) {
			// angolo, distanza in raggi, fase, dimensione
			scintille[i] = new float[]{(float) (random.nextDouble() * Math.PI * 2), 1.15f + random.nextFloat() * 0.9f,
					(float) (random.nextDouble() * Math.PI * 2), 2.5f + random.nextFloat() * 3.5f};
		}
	}

	/**
	 * Quanto spicca l'artefatto, da 0 (spoglio) a 3: un punto per effetto, due se è raro e quattro se leggendario, più i livelli sopra quello del mondo.
	 */
	public static int splendore(VistaArtefatto artefatto, int livelloMondo) {
		int punti = artefatto.getModificatori().size() + artefatto.getIncantamenti().size();
		if (artefatto.getRarita() == TipoRaritaArtefatto.RARO) {
			punti += 2;
		} else if (artefatto.getRarita() == TipoRaritaArtefatto.LEGGENDARIO) {
			punti += 4;
		}
		punti += Math.max(0, artefatto.getLivello() - livelloMondo);
		if (punti == 0) {
			return 0;
		}
		return punti <= 2 ? 1 : punti <= 4 ? 2 : 3;
	}

	/**
	 * Il nome della risorsa dell'immagine dell'oggetto, dove c'è: gli ingredienti magici hanno la loro, i tipi senza immagine mostrano solo il cerchio (null).
	 * Un artefatto con un aspetto suo (i libri tecnici) ha l'immagine di quello; gli ingredienti senza una propria immagine hanno l'icona della pergamena (l'aggiornamento di livello usa quella della pergamena).
	 */
	public static String risorsaDellOggetto(VistaArtefatto artefatto) {
		if (artefatto.getAspetto() != null) {
			return NomiDelleImmagini.oggetto(artefatto.getAspetto());
		}
		switch (artefatto.getTipo()) {
			case PERGAMENA:
			case AGGIORNAMENTO:
				// L'aggiornamento di livello è una pergamena anche per l'occhio
				return "oggetti/Pergamena.png";
			case GEMMA:
				return "oggetti/Gemma.png";
			case MONILE:
				return "oggetti/Monile.png";
			case GINGILLO:
				return "oggetti/Gingillo.png";
			case SIGILLO:
				return "oggetti/Sigillo.png";
			case VESTE:
				// Per ora la veste è quella del mago, anche per il ladro e il sacerdote
				return "oggetti/Veste.png";
			case TALISMANO:
				return "oggetti/Talismano.png";
			case NINNOLO:
				return "oggetti/Ninnolo.png";
			case SPADA:
				return NomiDelleImmagini.oggetto(TipoBottino.SPADA);
			case SPADONE:
				return NomiDelleImmagini.oggetto(TipoBottino.SPADONE);
			case FALCE:
				return NomiDelleImmagini.oggetto(TipoBottino.FALCE);
			case MAZZA:
				return NomiDelleImmagini.oggetto(TipoBottino.MAZZA);
			case ASCIA:
				return NomiDelleImmagini.oggetto(TipoBottino.ASCIA);
			case LANCIA:
				return NomiDelleImmagini.oggetto(TipoBottino.LANCIA);
			case BASTONE_MAGICO:
				return NomiDelleImmagini.oggetto(TipoBottino.BASTONE_MAGICO);
			case LIBRO_MAGICO:
				return NomiDelleImmagini.oggetto(TipoBottino.LIBRO_MAGICO);
			case SCUDO:
				return NomiDelleImmagini.oggetto(TipoBottino.SCUDO);
			case ELMO:
				return NomiDelleImmagini.oggetto(TipoBottino.ELMO);
			case MASCHERA:
				return NomiDelleImmagini.oggetto(TipoBottino.MASCHERA);
			case ARMATURA:
				return NomiDelleImmagini.oggetto(TipoBottino.ARMATURA);
			case SCHINIERI:
				return NomiDelleImmagini.oggetto(TipoBottino.SCHINIERI);
			case ANELLO:
				return NomiDelleImmagini.oggetto(TipoBottino.ANELLO);
			default:
				return artefatto.getTipo().isIngrediente() ? NomiDelleImmagini.ICONA_PERGAMENA : null;
		}
	}

	/**
	 * Con altre rivelazioni in coda la sosta si accorcia, se non è già finita.
	 */
	public void accelera() {
		if (secondi < DURATA_CON_CODA - USCITA) {
			durata = DURATA_CON_CODA;
		}
	}

	/**
	 * La chiude subito, senza l'uscita: quando chi guarda salta avanti.
	 */
	public void concludi() {
		attivo = false;
	}

	public boolean isAttivo() {
		return attivo;
	}

	/**
	 * Da quanti secondi è cominciata
	 */
	public float getSecondi() {
		return secondi;
	}

	/**
	 * Fa passare il tempo vero trascorso dall'ultima volta; da chiamare dopo aver disegnato il fotogramma, a ogni disegno.
	 */
	public void avanza() {
		long adesso = orologioNanosecondi.getAsLong();
		if (ultimoFotogrammaNanosecondi >= 0) {
			secondi += (adesso - ultimoFotogrammaNanosecondi) / 1_000_000_000f;
		}
		ultimoFotogrammaNanosecondi = adesso;
		if (secondi >= durata) {
			attivo = false;
		}
	}

	public Pannello getPannello() {
		return pannello;
	}

	/**
	 * Il pannello: il nome proprio (o il nome), il nome comune se c'è un nome proprio, la descrizione, livello e rarità, il set leggendario e i tipi dei suoi
	 * pezzi, poi gli incantamenti e i modificatori, verdi i bonus e rossi i malus. Tutto con il font medio, che si legge meglio del piccolo.
	 */
	private List<Riga> righeDelPannello(VistaArtefatto artefatto, int larghezzaMassima) {
		DoomdarkFont font = DoomdarkFont.medio();
		List<Riga> righe = new ArrayList<>();
		Optional<String> nomeProprio = artefatto.getNomeProprio();
		if (nomeProprio.isPresent()) {
			aggiungi(righe, nomeProprio.get(), DoomdarkColorModel.Color.YELLOW, font, larghezzaMassima);
			aggiungi(righe, maiuscola(artefatto.getNome()), DoomdarkColorModel.Color.WHITE, font, larghezzaMassima);
		} else {
			aggiungi(righe, maiuscola(artefatto.getNome()), DoomdarkColorModel.Color.YELLOW, font, larghezzaMassima);
		}
		if (artefatto.getDescrizione() != null) {
			aggiungi(righe, maiuscola(artefatto.getDescrizione()), DoomdarkColorModel.Color.LIGHT_GRAY, font, larghezzaMassima);
		}
		StringBuilder dati = new StringBuilder(TestiDellaUI.get("LIVELLO_ARTEFATTO", artefatto.getLivello()));
		if (artefatto.getRarita() != TipoRaritaArtefatto.COMUNE) {
			dati.append(", ").append(artefatto.getRarita().getNome());
		}
		if (artefatto.getDanni() > 0) {
			dati.append(", ").append(TestiDellaUI.get("DANNI_ARTEFATTO", artefatto.getDanni()));
		}
		aggiungi(righe, dati.toString(), DoomdarkColorModel.Color.WHITE, font, larghezzaMassima);
		artefatto.getDescrizioneSet().ifPresent(set -> aggiungi(righe, set, DoomdarkColorModel.Color.YELLOW, font, larghezzaMassima));
		artefatto.getTipiDelSet().ifPresent(tipi -> aggiungi(righe, tipi, DoomdarkColorModel.Color.YELLOW, font, larghezzaMassima));
		List<String> incantamenti = new ArrayList<>();
		for (IncantamentoMD incantamento : artefatto.getIncantamenti()) {
			incantamenti.add(incantamento(incantamento));
		}
		List<String> bonus = new ArrayList<>();
		List<String> malus = new ArrayList<>();
		for (ModificatoreAttributoMD modificatore : artefatto.getModificatori()) {
			(modificatore.getQuantita() >= 0 || modificatore.getBersaglio() == BersaglioModificatore.BERSAGLIO ? bonus : malus)
					.add(TestoDeiModificatori.completo(modificatore));
		}
		aggiungiElenco(righe, incantamenti, font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima);
		aggiungiElenco(righe, bonus, font, DoomdarkColorModel.Color.GREEN, larghezzaMassima);
		aggiungiElenco(righe, malus, font, DoomdarkColorModel.Color.RED, larghezzaMassima);
		return righe;
	}

	private static void aggiungiElenco(List<Riga> righe, List<String> voci, DoomdarkFont font, DoomdarkColorModel.Color colore, int larghezzaMassima) {
		if (!voci.isEmpty()) {
			aggiungi(righe, String.join(", ", voci), colore, font, larghezzaMassima);
		}
	}

	/**
	 * Una riga del pannello, con la larghezza che ha il suo testo a capo e l'altezza di una riga di font per ogni riga a capo
	 */
	private static void aggiungi(List<Riga> righe, String testo, DoomdarkColorModel.Color colore, DoomdarkFont font, int larghezzaMassima) {
		List<String> parti = FontTool.split(font, testo, larghezzaMassima);
		int larghezza = 0;
		for (String parte : parti) {
			larghezza = Math.max(larghezza, FontTool.getWidth(font, parte));
		}
		righe.add(new Riga(testo, colore, larghezzaMassima, Math.max(1, larghezza), font.getHeight() * parti.size(), 0, 0));
	}

	private static String incantamento(IncantamentoMD incantamento) {
		StringBuilder sb = new StringBuilder(maiuscola(incantamento.getNomeIncantamento()));
		List<String> parti = new ArrayList<>();
		if (incantamento.getDannoBonusFisso() != 0) {
			parti.add(String.format(Locale.ROOT, "%+d", incantamento.getDannoBonusFisso()));
		}
		if (incantamento.getCoefficienteScala() != 0) {
			parti.add(String.format(Locale.ROOT, "%+.0f%%", incantamento.getCoefficienteScala() * 100));
		}
		if (!parti.isEmpty()) {
			sb.append(' ').append(Lingue.corrente().elenco(parti));
		}
		return sb.toString();
	}

	private static String maiuscola(String testo) {
		return testo.isEmpty() ? testo : Character.toUpperCase(testo.charAt(0)) + testo.substring(1);
	}

	private static float uscitaMorbida(float u) {
		return 1f - (1f - u) * (1f - u);
	}

	/**
	 * Arriva un po' oltre 1 e torna indietro (easing "back")
	 */
	private static float rimbalzo(float u) {
		float c = 1.7f;
		float v = u - 1f;
		return 1f + (c + 1f) * v * v * v + c * v * v;
	}

	/**
	 * Quel che si vede all'istante {@code t} dall'inizio: il cerchio, i raggi, le scintille e il pannello.
	 */
	public Fotogramma fotogramma(float t) {
		float inizioUscita = durata - USCITA;
		float uscita = t > inizioUscita ? Math.min(1f, (t - inizioUscita) / USCITA) : 0f;
		float voloUscita = uscita * uscita;

		// Scala del cerchio: punto che si gonfia, apertura con rimbalzo, poi rimpicciolisce uscendo
		float scala;
		if (t < ANTICIPAZIONE) {
			scala = 0.35f * uscitaMorbida(t / ANTICIPAZIONE);
		} else if (t < ANTICIPAZIONE + APERTURA) {
			scala = 0.35f + 0.65f * rimbalzo((t - ANTICIPAZIONE) / APERTURA);
		} else {
			scala = 1f;
		}
		scala *= 1f - 0.85f * voloUscita;
		float x = centroX + (destinazione.x - centroX) * voloUscita;
		float y = centroY + (destinazione.y - centroY) * voloUscita;
		float alfa = Math.min(1f, t / ANTICIPAZIONE) * (1f - 0.7f * uscita);
		// Finché non vola via resta nella sua area: i raggi lunghi non devono coprire il riquadro del testo
		Area ritaglio = uscita == 0f ? area : null;

		// Lo sfondo si scurisce un po', così l'artefatto spicca
		float velo = Math.min(1f, t / 0.3f) * (1f - uscita);
		int alfaDelVelo = (int) (110 * velo);

		// Lampo all'apertura, solo per gli artefatti che spiccano di più
		int alfaDelLampo = 0;
		float lampo = LAMPO[gradino];
		if (lampo > 0 && t >= ANTICIPAZIONE && t < ANTICIPAZIONE + 0.3f) {
			alfaDelLampo = (int) (255 * lampo * (1f - (t - ANTICIPAZIONE) / 0.3f));
		}

		float r = raggio * scala;
		if (r < 1) {
			return new Fotogramma(ritaglio, area, alfaDelVelo, alfaDelLampo, Collections.<Raggio>emptyList(), null, null, x, y, r, 0f,
					Collections.<Stella>emptyList(), null, null, 0f);
		}

		// Raggi: si allungano fino al massimo e si ritraggono nel corso della rivelazione
		float inviluppo = 0f;
		if (t > ANTICIPAZIONE && t < inizioUscita) {
			inviluppo = (float) Math.pow(Math.sin(Math.PI * (t - ANTICIPAZIONE) / (inizioUscita - ANTICIPAZIONE)), 0.8);
		}
		float lunghezzaMassima = raggio * LUNGHEZZA[gradino];
		List<Raggio> raggi = new ArrayList<>();
		if (inviluppo > 0) {
			// Il giro secondario, tenue e in senso orario, dà profondità
			if (gradino > 0) {
				raggiDiLuce(raggi, x, y, r, r + (lunghezzaMassima * 0.85f - r) * inviluppo, RAGGI[gradino] / 2, (float) (0.25 * t), BIANCO, 0.35f * alfa, t, false);
			}
			raggiDiLuce(raggi, x, y, r, r + (lunghezzaMassima - r) * inviluppo, RAGGI[gradino], (float) (-(0.5 + 0.15 * gradino) * t), -1, 0.85f * alfa, t,
					gradino >= 2);
		}

		// Alone, cerchio pieno e bordo
		float alone = r * 1.9f;
		Cerchio cerchioDellAlone = new Cerchio(x, y, alone, x, y, alone, new int[]{colore, colore, colore}, new float[]{0.65f * alfa, 0.65f * alfa * 0.45f, 0f});
		Cerchio sfera = new Cerchio(x, y, r, x - r * 0.25f, y - r * 0.25f, r * 1.3f, new int[]{BIANCO, colore, piuScuro(colore)}, new float[]{alfa, alfa, alfa});

		// Scintille che brillano attorno al cerchio
		List<Stella> stelle = new ArrayList<>();
		for (float[] scintilla : scintille) {
			float brillio = (float) Math.max(0, Math.sin(scintilla[2] + t * 5)) * inviluppo * alfa;
			if (brillio > 0.05f) {
				double angolo = scintilla[0] - 0.3 * t;
				stelle.add(new Stella((float) (x + Math.cos(angolo) * r * scintilla[1]), (float) (y + Math.sin(angolo) * r * scintilla[1]),
						scintilla[3] * (0.6f + 0.4f * brillio), brillio));
			}
		}

		// L'oggetto sul cerchio, al più alle sue dimensioni vere: ingrandito, il disegno si sgranerebbe
		Oggetto oggetto = null;
		if (misuraDellOggetto != null) {
			float lato = raggio * 1.6f;
			float s = Math.min(1f, Math.min(lato / misuraDellOggetto.larghezza, lato / misuraDellOggetto.altezza)) * scala;
			int w = Math.round(misuraDellOggetto.larghezza * s);
			int h = Math.round(misuraDellOggetto.altezza * s);
			oggetto = new Oggetto(Math.round(x - w / 2f), Math.round(y - h / 2f), w, h, Math.max(0f, Math.min(1f, alfa)));
		}

		// Il pannello con nome ed effetti resta al suo posto e sfuma per primo
		float testo = Math.min(1f, Math.max(0f, (t - ANTICIPAZIONE - APERTURA + 0.1f) / 0.3f)) * (1f - Math.min(1f, uscita * 2f));
		return new Fotogramma(ritaglio, area, alfaDelVelo, alfaDelLampo, raggi, cerchioDellAlone, sfera, x, y, r, 0.8f * alfa, stelle, oggetto, pannello, testo);
	}

	/**
	 * @param rgbUnico un colore per tutti i raggi, o -1 per prendere quelli degli elementi, uno dopo l'altro
	 */
	private void raggiDiLuce(List<Raggio> raggi, float x, float y, float base, float lunghezza, int numero, float rotazione, int rgbUnico, float alfa, float t,
							 boolean alterni) {
		double semiampiezza = Math.PI / numero * 0.45;
		for (int i = 0; i < numero; i++) {
			double angolo = rotazione + i * 2 * Math.PI / numero;
			float l = lunghezza * (alterni && i % 2 == 1 ? 0.65f : 1f) * (1f + 0.06f * (float) Math.sin(4 * t + i));
			if (l <= base) {
				continue;
			}
			float[] vertici = {
					(float) (x + Math.cos(angolo - semiampiezza) * base * 0.9), (float) (y + Math.sin(angolo - semiampiezza) * base * 0.9),
					(float) (x + Math.cos(angolo) * l), (float) (y + Math.sin(angolo) * l),
					(float) (x + Math.cos(angolo + semiampiezza) * base * 0.9), (float) (y + Math.sin(angolo + semiampiezza) * base * 0.9)};
			int rgb = rgbUnico >= 0 ? rgbUnico : coloriRaggi.get(i % coloriRaggi.size());
			raggi.add(new Raggio(x, y, base, l, vertici, rgb, alfa));
		}
	}

	/** Come {@code Color.darker()}: ogni canale al 70% */
	private static int piuScuro(int rgb) {
		int r = (int) (((rgb >> 16) & 0xFF) * 0.7);
		int g = (int) (((rgb >> 8) & 0xFF) * 0.7);
		int b = (int) ((rgb & 0xFF) * 0.7);
		return (r << 16) | (g << 8) | b;
	}
}
