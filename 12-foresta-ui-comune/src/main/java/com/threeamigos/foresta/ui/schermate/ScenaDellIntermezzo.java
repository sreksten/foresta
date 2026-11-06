package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneFumetto;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.intermezzi.BattutaIntermezzo;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.ElementoIntermezzo;
import com.threeamigos.foresta.intermezzi.ImmagineIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.StatoElemento;
import com.threeamigos.foresta.tipi.TipoStiramento;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.FotogrammiDelFuocoDaCampo;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.immagini.VersiDeiPersonaggi;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * La scena di una {@link PaginaIntermezzo} a un certo istante, senza disegno: lo sfondo (stirato sugli assi indicati dal suo {@link TipoStiramento}, o l'ombra del
 * drago se manca), gli elementi nell'ordine della pagina con il loro stato al secondo corrente (dove stanno, quanto sono grandi, se vanno specchiati, quanto sono
 * opachi), il testo in alto e i fumetti delle battute visibili, con la punta verso la bocca di chi parla. Chi disegna (Swing, libGDX) mostra il {@link Quadro}.
 * <p>
 * Il tempo è quello reale trascorso da quando la pagina è comparsa: è lo stesso metro del timer con cui il motore fa avanzare le pagine, quindi animazioni e
 * dialoghi restano allineati alla durata della pagina anche se qualche fotogramma va perso. Quanto è grande un'immagine lo dice chi disegna (ha le immagini
 * caricate, con lo zoom): se non c'è, l'elemento non si disegna.
 */
public final class ScenaDellIntermezzo {

	// Distanza fra la bocca di chi parla e il bordo del fumetto collocato automaticamente
	private static final int DISTANZA_FUMETTO_VERTICALE = 24;
	private static final int MARGINE_SCHERMO = 4;

	/**
	 * Che immagine disegnare, in modo che non dipenda dal toolkit: una risorsa per nome (quella di un personaggio o di una locazione compresa), un fotogramma di
	 * un foglio di sprite o un'animazione calcolata
	 */
	public static final class Immagine {
		/** Da dove viene, per chi vuole risolverla a modo suo */
		private final ImmagineIntermezzo origine;
		private final String risorsa;
		private final boolean conZoom;
		private final int colonne;
		private final int righe;
		private final int fotogramma;
		private final double secondi;
		private final StatoElemento stato;

		private Immagine(ImmagineIntermezzo origine, String risorsa, boolean conZoom, int colonne, int righe, int fotogramma, double secondi,
					 StatoElemento stato) {
			this.origine = origine;
			this.risorsa = risorsa;
			this.conZoom = conZoom;
			this.colonne = colonne;
			this.righe = righe;
			this.fotogramma = fotogramma;
			this.secondi = secondi;
			this.stato = stato;
		}

		public ImmagineIntermezzo getOrigine() {
			return origine;
		}

		/** Il nome della risorsa (per un foglio di sprite, il foglio); null per un'animazione */
		public String getRisorsa() {
			return risorsa;
		}

		/** Se la risorsa è a metà risoluzione e va ingrandita con lo zoom */
		public boolean isConZoom() {
			return conZoom;
		}

		public boolean isFotogrammaDiUnFoglio() {
			return origine.getTipo() == ImmagineIntermezzo.Tipo.SPRITE_SHEET;
		}

		public boolean isAnimazione() {
			return origine.getTipo() == ImmagineIntermezzo.Tipo.ANIMAZIONE;
		}

		public int getColonne() {
			return colonne;
		}

		public int getRighe() {
			return righe;
		}

		/** Il numero del fotogramma del foglio, riga per riga */
		public int getFotogramma() {
			return fotogramma;
		}

		/** I secondi dall'inizio della pagina: per un'animazione */
		public double getSecondi() {
			return secondi;
		}

		/** Lo stato dell'elemento in quell'istante, o null se l'immagine è lo sfondo */
		public StatoElemento getStato() {
			return stato;
		}
	}

	/**
	 * Un'immagine con la sua posizione sullo schermo (l'angolo in alto a sinistra e la misura), se va specchiata e quanto è opaca
	 */
	public static final class Disegno {
		private final Immagine immagine;
		private final int x;
		private final int y;
		private final int larghezza;
		private final int altezza;
		private final boolean specchiata;
		private final float opacita;

		private Disegno(Immagine immagine, int x, int y, int larghezza, int altezza, boolean specchiata, float opacita) {
			this.immagine = immagine;
			this.x = x;
			this.y = y;
			this.larghezza = larghezza;
			this.altezza = altezza;
			this.specchiata = specchiata;
			this.opacita = opacita;
		}

		public Immagine getImmagine() {
			return immagine;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getLarghezza() {
			return larghezza;
		}

		public int getAltezza() {
			return altezza;
		}

		public boolean isSpecchiata() {
			return specchiata;
		}

		/** Da 0 (invisibile) a 1 */
		public float getOpacita() {
			return opacita;
		}
	}

	/**
	 * Un fumetto già collocato: la nuvola con il testo (angolo in basso a sinistra in (x, y), come per i fumetti dei riquadri) e il punto verso cui va la punta
	 */
	public static final class Fumetto {
		private final DescrizioneFumetto descrizione;
		private final int x;
		private final int y;
		private final int puntaVersoX;
		private final int puntaVersoY;

		private Fumetto(DescrizioneFumetto descrizione, int x, int y, int puntaVersoX, int puntaVersoY) {
			this.descrizione = descrizione;
			this.x = x;
			this.y = y;
			this.puntaVersoX = puntaVersoX;
			this.puntaVersoY = puntaVersoY;
		}

		public DescrizioneFumetto getDescrizione() {
			return descrizione;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getPuntaVersoX() {
			return puntaVersoX;
		}

		public int getPuntaVersoY() {
			return puntaVersoY;
		}
	}

	/**
	 * Quel che si vede a un istante
	 */
	public static final class Quadro {
		private final Disegno sfondo;
		private final Area ritaglio;
		private final List<Disegno> elementi;
		private final String testo;
		private final List<Fumetto> fumetti;

		private Quadro(Disegno sfondo, Area ritaglio, List<Disegno> elementi, String testo, List<Fumetto> fumetti) {
			this.sfondo = sfondo;
			this.ritaglio = ritaglio;
			this.elementi = elementi;
			this.testo = testo;
			this.fumetti = fumetti;
		}

		/** Lo sfondo, o null se si vede solo l'ombra del drago */
		public Disegno getSfondo() {
			return sfondo;
		}

		/** Dove si ritagliano gli elementi (se la pagina lo chiede e c'è uno sfondo), o null */
		public Area getRitaglio() {
			return ritaglio;
		}

		public List<Disegno> getElementi() {
			return elementi;
		}

		public String getTesto() {
			return testo;
		}

		public List<Fumetto> getFumetti() {
			return fumetti;
		}
	}

	private final int width;
	private final int height;
	private final Function<ImmagineIntermezzo, Misura> misure;

	private PaginaIntermezzo pagina;
	private long inizioPaginaNanosecondi;
	// I fumetti delle battute della pagina corrente, costruiti alla prima comparsa; null se il testo non si può disegnare
	private final Map<BattutaIntermezzo, DescrizioneFumetto> nuvole = new IdentityHashMap<>();
	// Dove sono stati disegnati gli elementi in questo quadro, e in che verso, per puntare i fumetti
	private final Map<String, Area> posizioni = new HashMap<>();
	private final Map<String, Boolean> versi = new HashMap<>();

	/**
	 * @param misure quanto è grande un'immagine così com'è disegnata (già con lo zoom), o null se non c'è
	 */
	public ScenaDellIntermezzo(int width, int height, Function<ImmagineIntermezzo, Misura> misure) {
		this.width = width;
		this.height = height;
		this.misure = misure;
	}

	/**
	 * Se un'immagine di una risorsa va ingrandita con lo zoom: le locazioni, i personaggi e gli oggetti stanno nei file a metà risoluzione
	 */
	public static boolean mezzaRisoluzione(String percorso) {
		return percorso.startsWith("locazioni/") || percorso.startsWith("personaggi/") || percorso.startsWith("oggetti/");
	}

	/**
	 * Quanto è grande un'immagine di un intermezzo, per chi la misura sulle risorse: lo zoom lo applica alle risorse a metà risoluzione, ai personaggi e alle
	 * locazioni; un foglio di sprite si divide in fotogrammi; l'animazione ha la misura dei suoi fotogrammi.
	 */
	public static Misura misura(ImmagineIntermezzo immagine, MisureDelleImmagini misure) {
		switch (immagine.getTipo()) {
			case PERSONAGGIO:
				return misure.personaggio(immagine.getClassePersonaggio());
			case LOCAZIONE:
				return conZoom(misure.immagine(NomiDelleImmagini.locazione(immagine.getTipoLocazione())), true);
			case ANIMAZIONE:
				return new Misura(FotogrammiDelFuocoDaCampo.LARGHEZZA, FotogrammiDelFuocoDaCampo.ALTEZZA);
			case SPRITE_SHEET:
				Misura foglio = conZoom(misure.immagine(immagine.getRisorsa()), mezzaRisoluzione(immagine.getRisorsa()));
				return new Misura(foglio.larghezza / immagine.getColonne(), foglio.altezza / immagine.getRighe());
			default:
				return conZoom(misure.immagine(immagine.getRisorsa()), mezzaRisoluzione(immagine.getRisorsa()));
		}
	}

	private static Misura conZoom(Misura misura, boolean zoom) {
		double fattore = zoom ? LivelloDiZoom.valore() : 1.0;
		return new Misura((int) Math.round(misura.larghezza * fattore), (int) Math.round(misura.altezza * fattore));
	}

	/**
	 * Mostra la pagina, facendo ripartire da zero il suo tempo.
	 */
	public void mostra(PaginaIntermezzo pagina) {
		this.pagina = pagina;
		inizioPaginaNanosecondi = System.nanoTime();
		nuvole.clear();
	}

	/**
	 * La pagina continua come se fosse comparsa quei secondi fa: per saltare al fumetto successivo.
	 */
	public void saltaA(double secondi) {
		inizioPaginaNanosecondi = System.nanoTime() - (long) (secondi * 1_000_000_000L);
	}

	/**
	 * Dimentica la pagina, alla fine dell'intermezzo.
	 */
	public void svuota() {
		pagina = null;
		nuvole.clear();
		posizioni.clear();
		versi.clear();
	}

	public PaginaIntermezzo getPagina() {
		return pagina;
	}

	/**
	 * I secondi trascorsi da quando la pagina è comparsa
	 */
	public double secondiTrascorsi() {
		return (System.nanoTime() - inizioPaginaNanosecondi) / 1_000_000_000.0;
	}

	/**
	 * Dov'è stato disegnato l'elemento con quell'id nell'ultimo quadro, o null: per la rivelazione dell'artefatto, che vola verso di lui
	 */
	public Area getPosizione(String idElemento) {
		return posizioni.get(idElemento);
	}

	/**
	 * La pagina come appare adesso, o null se non c'è.
	 */
	public Quadro quadro() {
		return quadroAl(secondiTrascorsi());
	}

	/**
	 * La pagina come appare a un certo numero di secondi da quando è comparsa, o null se non c'è.
	 */
	public Quadro quadroAl(double secondi) {
		if (pagina == null) {
			return null;
		}
		Disegno sfondo = disposizioneDelloSfondo(secondi);
		Area ritaglio = pagina.isRitaglioSuSfondo() && sfondo != null ? new Area(sfondo.x, sfondo.y, sfondo.larghezza, sfondo.altezza) : null;
		posizioni.clear();
		versi.clear();
		List<Disegno> elementi = new ArrayList<>();
		for (ElementoIntermezzo elemento : pagina.getElementi()) {
			Disegno disegno = disponiElemento(elemento, elemento.getStatoAl(secondi), secondi);
			if (disegno != null) {
				elementi.add(disegno);
			}
		}
		List<Fumetto> fumetti = new ArrayList<>();
		for (BattutaProgrammata battutaProgrammata : pagina.getBattuteProgrammate()) {
			if (battutaProgrammata.isVisibileAl(secondi)) {
				Fumetto fumetto = disponiFumetto(battutaProgrammata.getBattuta());
				if (fumetto != null) {
					fumetti.add(fumetto);
				}
			}
		}
		return new Quadro(sfondo, ritaglio, elementi, pagina.getTesto(), fumetti);
	}

	private Immagine immagine(ImmagineIntermezzo origine, double secondi, StatoElemento stato) {
		switch (origine.getTipo()) {
			case PERSONAGGIO:
				return new Immagine(origine, NomiDelleImmagini.personaggio(origine.getClassePersonaggio()), true, 1, 1, 0, secondi, stato);
			case LOCAZIONE:
				return new Immagine(origine, NomiDelleImmagini.locazione(origine.getTipoLocazione()), true, 1, 1, 0, secondi, stato);
			case SPRITE_SHEET:
				return new Immagine(origine, origine.getRisorsa(), mezzaRisoluzione(origine.getRisorsa()), origine.getColonne(), origine.getRighe(),
						origine.getFotogrammaAl(secondi), secondi, stato);
			case ANIMAZIONE:
				return new Immagine(origine, null, false, 1, 1, 0, secondi, stato);
			default:
				return new Immagine(origine, origine.getRisorsa(), mezzaRisoluzione(origine.getRisorsa()), 1, 1, 0, secondi, stato);
		}
	}

	/**
	 * Lo sfondo è stirato solo sugli assi richiesti da {@link TipoStiramento} per riempire esattamente l'area (anche distorcendo le proporzioni); gli assi non
	 * stirati restano a dimensione nativa, centrati.
	 */
	private Disegno disposizioneDelloSfondo(double secondi) {
		if (pagina.getSfondo() == null) {
			return null;
		}
		Misura misura = misure.apply(pagina.getSfondo());
		if (misura == null) {
			return null;
		}
		TipoStiramento tipoStiramento = pagina.getTipoStiramentoSfondo();
		boolean stiraOrizzontale = tipoStiramento == TipoStiramento.ORIZZONTALE || tipoStiramento == TipoStiramento.ORIZZONTALE_E_VERTICALE;
		boolean stiraVerticale = tipoStiramento == TipoStiramento.VERTICALE || tipoStiramento == TipoStiramento.ORIZZONTALE_E_VERTICALE;
		int larghezzaSfondo = stiraOrizzontale ? width : misura.larghezza;
		int altezzaSfondo = stiraVerticale ? height : misura.altezza;
		return new Disegno(immagine(pagina.getSfondo(), secondi, null), (width - larghezzaSfondo) / 2, (height - altezzaSfondo) / 2, larghezzaSfondo, altezzaSfondo, false, 1f);
	}

	/**
	 * Se l'immagine va rovesciata per far guardare l'elemento dalla parte voluta. Lo sa solo per i personaggi (vedi VersiDeiPersonaggi): le altre immagini sono
	 * disegnate così come sono.
	 */
	private static boolean serveSpecchiare(ImmagineIntermezzo immagine, StatoElemento stato) {
		return stato.getVerso() != null && immagine.getTipo() == ImmagineIntermezzo.Tipo.PERSONAGGIO
				&& VersiDeiPersonaggi.serveSpecchiare(immagine.getClassePersonaggio(), stato.getVerso());
	}

	private Disegno disponiElemento(ElementoIntermezzo elemento, StatoElemento stato, double secondi) {
		Misura misura = misure.apply(elemento.getImmagine());
		if (misura == null) {
			return null;
		}
		int larghezza = (int) Math.round(misura.larghezza * stato.getScala());
		int altezza = (int) Math.round(misura.altezza * stato.getScala());
		int x = (int) Math.round(stato.getX() * width) - larghezza / 2;
		int y = (int) Math.round(stato.getY() * height) - altezza / 2;
		posizioni.put(elemento.getId(), new Area(x, y, larghezza, altezza));
		boolean specchiato = serveSpecchiare(elemento.getImmagine(), stato);
		versi.put(elemento.getId(), specchiato);
		float opacita = (float) Math.max(0, Math.min(1, stato.getOpacita()));
		if (opacita == 0 || larghezza <= 0 || altezza <= 0) {
			return null;
		}
		return new Disegno(immagine(elemento.getImmagine(), secondi, stato), x, y, larghezza, altezza, specchiato, opacita);
	}

	private Fumetto disponiFumetto(BattutaIntermezzo battuta) {
		int[] punta = puntaDellaBattuta(battuta);
		if (punta == null) {
			return null;
		}
		DescrizioneFumetto descrizione = nuvola(battuta);
		if (descrizione == null) {
			return null;
		}
		int larghezza = descrizione.getLarghezza();
		int altezza = descrizione.getAltezza();
		// (x, y) è l'angolo in basso a sinistra, come per i fumetti dei riquadri
		int x;
		int y;
		if (battuta.hasPosizioneNuvola()) {
			x = (int) Math.round(battuta.getNuvolaX() * width) - larghezza / 2;
			y = (int) Math.round(battuta.getNuvolaY() * height) + altezza / 2;
		} else {
			y = punta[1] - DISTANZA_FUMETTO_VERTICALE;
			if (battuta.isCentrataOrizzontalmente()) {
				x = (width - larghezza) / 2;
			} else {
				// Sopra chi parla
				x = punta[0] - larghezza / 2;
			}
		}
		x = Math.max(MARGINE_SCHERMO, Math.min(x, width - larghezza - MARGINE_SCHERMO));
		y = Math.max(altezza + MARGINE_SCHERMO, Math.min(y, height - MARGINE_SCHERMO));
		return new Fumetto(descrizione, x, y, punta[0], punta[1]);
	}

	/**
	 * La bocca di chi parla nella posizione che ha in questo quadro, o il punto fisso; null se l'elemento non c'è.
	 */
	private int[] puntaDellaBattuta(BattutaIntermezzo battuta) {
		if (battuta.getIdElemento() == null) {
			return new int[]{(int) Math.round(battuta.getPuntoX() * width), (int) Math.round(battuta.getPuntoY() * height)};
		}
		Area rettangolo = posizioni.get(battuta.getIdElemento());
		if (rettangolo == null) {
			return null;
		}
		ElementoIntermezzo elemento = pagina.getElemento(battuta.getIdElemento());
		double boccaX = Boolean.TRUE.equals(versi.get(battuta.getIdElemento())) ? 1 - elemento.getBoccaX() : elemento.getBoccaX();
		return new int[]{rettangolo.getX() + (int) Math.round(boccaX * rettangolo.getLarghezza()),
				rettangolo.getY() + (int) Math.round(elemento.getBoccaY() * rettangolo.getAltezza())};
	}

	private DescrizioneFumetto nuvola(BattutaIntermezzo battuta) {
		if (!nuvole.containsKey(battuta)) {
			BusEventi.pubblica(new InternoCreazioneFumetto(battuta.getTesto()));
			DescrizioneFumetto descrizione = null;
			int larghezzaNuvola = battuta.hasLarghezza() ? (int) Math.round(battuta.getLarghezza() * width) : width / 3;
			try {
				descrizione = new DescrizioneFumetto(battuta.getTesto(), larghezzaNuvola, DoomdarkFont.medio());
			} catch (DoomdarkFont.UnsupportedCharacterException e) {
				BusEventi.pubblica(new InternoErrore("Battuta non disegnabile: \"" + battuta.getTesto() + "\" - " + e.getMessage()));
			}
			nuvole.put(battuta, descrizione);
		}
		return nuvole.get(battuta);
	}
}
