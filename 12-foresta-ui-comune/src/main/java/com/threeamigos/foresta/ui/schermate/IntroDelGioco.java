package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.strumenti.Punteggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * L'introduzione del gioco, senza disegno: la storia sale sullo sfondo della storia, e il logo della Foresta la segue (compare sotto l'ultima riga e sale
 * con lei) finché non arriva al suo posto, dove si ferma mentre la storia continua a salire; a quel punto compare anche il logo 3AM, poi loghi e sfondo
 * spariscono insieme; la classifica sale e si ferma, poi sparisce; i trofei scorrono tutti. Poi si ricomincia. Qui c'è il tempo e che cosa si vede in ogni
 * fotogramma ({@link #avanza()}): chi disegna (Swing, libGDX) mette il {@link Quadro}.
 */
public final class IntroDelGioco {

	// Lo scorrimento dell'intro (vedi ColonnaScorrevole): pixel al secondo, e quanto è alta la fascia di dissolvenza ai bordi
	private static final double VELOCITA = 30;
	public static final int FASCIA = 40;
	// Il logo della Foresta segue la storia a questa distanza sotto l'ultima riga, e si ferma al suo posto quando ci arriva
	private static final int MARGINE_LOGHI = 20;
	// Il logo della Foresta non si pianta di colpo: quando la storia lo porterebbe a questa distanza dal suo posto comincia a rallentare, con una frenata
	// costante, e si ferma quando la storia è salita di altrettanto oltre il suo posto (vedi frenata)
	static final int DISTANZA_DI_FRENATA = 50;
	// Il logo 3AM sta in basso a destra, a questa distanza dai due bordi
	private static final int MARGINE_LOGO_3AM = 20;
	// Fra una riga e l'altra della storia, oltre all'altezza di riga normale: un terzo dell'altezza delle lettere
	private static final int INTERLINEA_STORIA = LayoutDelTestoGrande.ALTEZZA_GLIFO / 3;
	private static final double SECONDI_DISSOLVENZA = 1;
	private static final double SECONDI_LOGHI = 3;
	private static final double SECONDI_CLASSIFICA = 5;
	/** Lo spazio fra il titolo della classifica e la prima riga */
	private static final int SPAZIO_DOPO_IL_TITOLO_DELLA_CLASSIFICA = 24;
	// Il secondo fotogramma più lento che si accetta: dopo una pausa lunga lo scorrimento non salta
	private static final double SECONDI_FOTOGRAMMA_MASSIMI = 0.1;
	public static final String RISORSA_LOGO_3AM = "Logo3AM.png";
	public static final String RISORSA_LOGO_FORESTA = "LogoForesta.png";

	/**
	 * Le parti dell'intro: la storia che scorre (poi i loghi), la classifica che sale e si ferma, i trofei che scorrono.
	 */
	public enum Fase {
		STORIA, CLASSIFICA, TROFEI
	}

	/**
	 * Un logo con il suo alone, nelle coordinate dello schermo, e la sua opacità in questo fotogramma: ognuno ha la sua, perché il logo della Foresta sale
	 * con la storia (sfumando ai bordi) e quello 3AM compare solo quando il primo si è fermato.
	 */
	public static final class Logo {
		private final Elemento elemento;
		private final float opacita;

		private Logo(Elemento elemento, float opacita) {
			this.elemento = elemento;
			this.opacita = opacita;
		}

		public Elemento getElemento() {
			return elemento;
		}

		public float getOpacita() {
			return opacita;
		}
	}

	/**
	 * Che cosa si vede in un fotogramma: prima lo sfondo (l'ombra del drago o lo sfondo della storia, di quell'opacità), poi la colonna che scorre, centrata
	 * in orizzontale, di quell'opacità e sfumata ai bordi ({@link ColonnaScorrevole#opacitaAllaQuota}), poi i loghi con il loro alone
	 */
	public static final class Quadro {
		private final Fase fase;
		private final boolean ombraDelDrago;
		private final float opacitaDelloSfondoDellaStoria;
		private final ColonnaScorrevole colonna;
		private final ContenutoDiColonna contenuto;
		private final float opacitaDellaColonna;
		private final List<Logo> loghi;

		private Quadro(Fase fase, boolean ombraDelDrago, float opacitaDelloSfondoDellaStoria, ColonnaScorrevole colonna, ContenutoDiColonna contenuto,
					   float opacitaDellaColonna, List<Logo> loghi) {
			this.fase = fase;
			this.ombraDelDrago = ombraDelDrago;
			this.opacitaDelloSfondoDellaStoria = opacitaDelloSfondoDellaStoria;
			this.colonna = colonna;
			this.contenuto = contenuto;
			this.opacitaDellaColonna = opacitaDellaColonna;
			this.loghi = loghi;
		}

		public Fase getFase() {
			return fase;
		}

		public boolean isOmbraDelDrago() {
			return ombraDelDrago;
		}

		/**
		 * L'opacità dello sfondo della storia (<code>fondinon2x2/SfondoStoria.png</code>, centrato), 0 se non c'è
		 */
		public float getOpacitaDelloSfondoDellaStoria() {
			return opacitaDelloSfondoDellaStoria;
		}

		public ColonnaScorrevole getColonna() {
			return colonna;
		}

		public ContenutoDiColonna getContenuto() {
			return contenuto;
		}

		public float getOpacitaDellaColonna() {
			return opacitaDellaColonna;
		}

		/**
		 * I loghi che si vedono, con l'alone e ognuno con la sua opacità (vuoto se non si vede ancora nessuno)
		 */
		public List<Logo> getLoghi() {
			return loghi;
		}
	}

	private final int width;
	private final int height;
	private final VistaPartita vistaPartita;
	private final GestorePunteggi gestorePunteggi;
	private final MisureDelleImmagini misure;

	private Fase fase = Fase.STORIA;
	private ColonnaScorrevole colonna;
	private ContenutoDiColonna contenuto;
	private ContenutoDiColonna contenutoDellaStoria;
	// Il tempo della fase: dall'ultimo fotogramma, da quando il logo della Foresta si è fermato (e compare il 3AM), da quando si sta fermi
	private long ultimoFotogramma;
	private double secondiLoghi = -1;
	private double secondiFermo;
	private double secondiFase;

	/**
	 * @param gestorePunteggi la classifica, che l'intro mostra fra le sue pagine
	 * @param vistaPartita    la partita appena finita, per i trofei
	 * @param misure          per la misura dei loghi
	 */
	public IntroDelGioco(int width, int height, GestorePunteggi gestorePunteggi, VistaPartita vistaPartita, MisureDelleImmagini misure) {
		this.width = width;
		this.height = height;
		this.gestorePunteggi = gestorePunteggi;
		this.vistaPartita = vistaPartita;
		this.misure = misure;
	}

	/**
	 * Fa ripartire l'intro dalla storia.
	 */
	public void resettaSequenza() {
		passaA(Fase.STORIA);
	}

	/**
	 * Porta l'intro alla classifica: da lì si passa ai trofei, poi si riparte dalla storia.
	 */
	public void posizionaSuPunteggi() {
		passaA(Fase.CLASSIFICA);
	}

	public Fase getFase() {
		return fase;
	}

	private void passaA(Fase nuovaFase) {
		fase = nuovaFase;
		colonna = null;
		contenuto = null;
		secondiLoghi = -1;
		secondiFermo = 0;
		secondiFase = 0;
		ultimoFotogramma = 0;
	}

	/**
	 * La classifica in un contenuto, nell'alfabeto grande: il titolo («intrepidi avventurieri») centrato in cima, poi il nome a sinistra e il punteggio a
	 * destra, una riga ogni {@link SchermateSenzaPartita#RIGA_CLASSIFICA}
	 */
	public static ContenutoDiColonna classifica(GestorePunteggi gestorePunteggi, int width) {
		int righe = gestorePunteggi.getConteggio();
		List<Elemento> elementi = new ArrayList<>();
		ImmagineDiPixel titolo = LayoutDelTestoGrande.immagine(TestiDellaUI.get("TITOLO_CLASSIFICA"), Math.max(1, width - 2 * SchermateSenzaPartita.MARGINE), true);
		elementi.add(Elemento.testoGrande(titolo, SchermateSenzaPartita.MARGINE, 0));
		int cimaDelleRighe = titolo.getAltezza() + SPAZIO_DOPO_IL_TITOLO_DELLA_CLASSIFICA;
		for (int posizione = 0; posizione < righe; posizione++) {
			Punteggio punteggio = gestorePunteggi.getPunteggio(posizione);
			int y = cimaDelleRighe + posizione * SchermateSenzaPartita.RIGA_CLASSIFICA;
			elementi.add(Elemento.testoGrande(riga(punteggio.getNome()), SchermateSenzaPartita.MARGINE, y));
			String valore = String.valueOf(punteggio.getPunteggio());
			elementi.add(Elemento.testoGrande(riga(valore), width - SchermateSenzaPartita.MARGINE - LayoutDelTestoGrande.larghezza(valore), y));
		}
		return new ContenutoDiColonna(width, cimaDelleRighe + righe * SchermateSenzaPartita.RIGA_CLASSIFICA, elementi);
	}

	private static ImmagineDiPixel riga(String testo) {
		return LayoutDelTestoGrande.immagine(testo, Math.max(1, LayoutDelTestoGrande.larghezza(testo)), false);
	}

	/**
	 * Tutta la storia in un contenuto, come un unico testo che va a capo dove serve (i paragrafi di VistaPartita.getStoria, che erano le pagine dell'intro di
	 * una volta), con un alone nero attorno alle lettere. Non cambia mai: si fa una volta sola.
	 */
	private ContenutoDiColonna contenutoDellaStoria() {
		if (contenutoDellaStoria == null) {
			int raggio = SchermateSenzaPartita.RAGGIO_ALONE;
			ImmagineDiPixel storia = LayoutDelTestoGrande.conAlone(
					LayoutDelTestoGrande.immagine(String.join(" ", vistaPartita.getStoria()), width - 2 * raggio, true, INTERLINEA_STORIA), raggio);
			contenutoDellaStoria = new ContenutoDiColonna(width, storia.getAltezza(),
					Collections.singletonList(Elemento.testoGrande(storia, "storia|" + width + "|" + vistaPartita.getStoria().size(), 0, 0)));
		}
		return contenutoDellaStoria;
	}

	private Area schermo() {
		return new Area(0, 0, width, height);
	}

	private double secondiDalFotogrammaPrecedente() {
		long adesso = System.nanoTime();
		double secondi = ultimoFotogramma == 0 ? 0 : (adesso - ultimoFotogramma) / 1_000_000_000d;
		ultimoFotogramma = adesso;
		return Math.min(secondi, SECONDI_FOTOGRAMMA_MASSIMI);
	}

	/**
	 * Un fotogramma dell'intro: fa avanzare il tempo di quello che è passato dall'ultimo e dice che cosa si vede.
	 */
	public Quadro avanza() {
		return avanzaDi(secondiDalFotogrammaPrecedente());
	}

	/**
	 * Come {@link #avanza()}, ma con i secondi dati invece di quelli misurati: per provare l'intro senza aspettarla.
	 */
	public Quadro avanzaDi(double secondi) {
		switch (fase) {
			case STORIA:
				return storia(secondi);
			case CLASSIFICA:
				return classifica(secondi);
			default:
				return trofei(secondi);
		}
	}

	private Misura conLoZoom(String risorsa) {
		Misura m = misure.immagine(risorsa);
		double zoom = LivelloDiZoom.valore();
		return new Misura((int) Math.round(m.larghezza * zoom), (int) Math.round(m.altezza * zoom));
	}

	/**
	 * Di quanto il logo della Foresta sta sotto al suo posto, data la distanza a cui lo terrebbe la storia (negativa se l'ha già superato). Lontano dal
	 * posto il logo la segue; da {@link #DISTANZA_DI_FRENATA} pixel in poi rallenta con una frenata costante: la sua velocità cala da quella della storia a
	 * zero, e si ferma quando la storia ha superato il suo posto di altrettanto. La curva è (d + L)² / 4L, che si raccorda alla retta d = d senza scatti di
	 * velocità. Durante la frenata il logo resta sotto a dove lo terrebbe la storia, mai a ridosso della sua ultima riga.
	 */
	static int frenata(int distanza) {
		if (distanza >= DISTANZA_DI_FRENATA) {
			return distanza;
		}
		if (distanza <= -DISTANZA_DI_FRENATA) {
			return 0;
		}
		double dentro = distanza + DISTANZA_DI_FRENATA;
		return (int) Math.round(dentro * dentro / (4.0 * DISTANZA_DI_FRENATA));
	}

	/**
	 * I due loghi come l'intro li disegna, per prepararne le immagini (con l'alone, che costa) prima che servano: la misura e il posto non contano, l'immagine
	 * si riconosce dalla risorsa e dal raggio dell'alone.
	 */
	public static List<Elemento> loghiDaPrecaricare() {
		return Arrays.asList(
				Elemento.immagineConAlone(RISORSA_LOGO_FORESTA, 1, 1, SchermateSenzaPartita.RAGGIO_ALONE, 0, 0),
				Elemento.immagineConAlone(RISORSA_LOGO_3AM, 1, 1, SchermateSenzaPartita.RAGGIO_ALONE, 0, 0));
	}

	private Quadro storia(double secondi) {
		if (colonna == null) {
			contenuto = contenutoDellaStoria();
			colonna = ColonnaScorrevole.dalBasso(schermo(), contenuto.getAltezza(), FASCIA);
		}
		if (!colonna.isUscito()) {
			colonna.avanza(secondi, VELOCITA);
		}
		secondiFase += secondi;
		// Il logo della Foresta segue la storia: sta sotto la sua ultima riga e sale con lei, finché non arriva al suo posto (il suo centro è a un terzo
		// dello schermo): lì si ferma, e la storia continua a salire. Dal momento in cui si ferma compare anche il logo 3AM, in basso a destra, dove la
		// storia non passa mai; poi, uscita la storia, loghi e sfondo spariscono insieme
		Misura logo3AM = conLoZoom(RISORSA_LOGO_3AM);
		Misura logoForesta = conLoZoom(RISORSA_LOGO_FORESTA);
		int cimaFerma = height / 3 - logoForesta.altezza / 2;
		int cimaConLaStoria = colonna.getFondo() + MARGINE_LOGHI;
		int cimaLogo = cimaFerma + frenata(cimaConLaStoria - cimaFerma);
		boolean fermo = cimaLogo <= cimaFerma;
		if (secondiLoghi < 0 && fermo) {
			secondiLoghi = 0;
		}
		float uscita = 0;
		if (secondiLoghi >= 0) {
			secondiLoghi += secondi;
			if (colonna.isUscito() && secondiLoghi >= SECONDI_DISSOLVENZA) {
				secondiFermo += secondi;
			}
			uscita = (float) Math.max(0, Math.min(1, (secondiFermo - SECONDI_LOGHI) / SECONDI_DISSOLVENZA));
		}
		// Lo sfondo della storia compare in dissolvenza, e sparisce insieme ai loghi
		float comparsa = (float) Math.min(1, secondiFase / SECONDI_DISSOLVENZA);
		List<Logo> loghi = new ArrayList<>();
		// Mentre sale il logo della Foresta sfuma ai bordi come la storia (per l'opacità conta il suo centro); fermo è pieno
		float opacitaForesta = fermo ? 1f : colonna.opacitaAllaQuota(cimaLogo + logoForesta.altezza / 2 - colonna.getCima());
		if (opacitaForesta * (1 - uscita) > 0) {
			loghi.add(new Logo(Elemento.immagineConAlone(RISORSA_LOGO_FORESTA, logoForesta.larghezza, logoForesta.altezza, SchermateSenzaPartita.RAGGIO_ALONE,
					(width - logoForesta.larghezza) >> 1, cimaLogo), opacitaForesta * (1 - uscita)));
		}
		if (secondiLoghi >= 0) {
			float opacita3AM = (float) Math.min(1, secondiLoghi / SECONDI_DISSOLVENZA) * (1 - uscita);
			if (opacita3AM > 0) {
				loghi.add(new Logo(Elemento.immagineConAlone(RISORSA_LOGO_3AM, logo3AM.larghezza, logo3AM.altezza, SchermateSenzaPartita.RAGGIO_ALONE,
						width - MARGINE_LOGO_3AM - logo3AM.larghezza, height - MARGINE_LOGO_3AM - logo3AM.altezza), opacita3AM));
			}
		}
		Quadro quadro = new Quadro(Fase.STORIA, false, comparsa * (1 - uscita), colonna, contenuto, 1f, loghi);
		if (uscita >= 1) {
			passaA(Fase.CLASSIFICA);
		}
		return quadro;
	}

	private Quadro classifica(double secondi) {
		if (colonna == null) {
			contenuto = classifica(gestorePunteggi, width);
			colonna = ColonnaScorrevole.dalBasso(schermo(), contenuto.getAltezza(), FASCIA).fermaA(SchermateSenzaPartita.QUOTA_CLASSIFICA);
		}
		colonna.avanza(secondi, VELOCITA);
		if (colonna.isArrivato()) {
			secondiFermo += secondi;
		}
		float uscita = (float) Math.max(0, Math.min(1, (secondiFermo - SECONDI_CLASSIFICA) / SECONDI_DISSOLVENZA));
		Quadro quadro = new Quadro(Fase.CLASSIFICA, true, 0f, colonna, contenuto, 1 - uscita, Collections.<Logo>emptyList());
		if (uscita >= 1) {
			passaA(Fase.TROFEI);
		}
		return quadro;
	}

	private Quadro trofei(double secondi) {
		if (colonna == null) {
			contenuto = PaginaTrofei.crea(vistaPartita, width - 2 * SchermateSenzaPartita.MARGINE, true).comeContenuto();
			colonna = ColonnaScorrevole.dalBasso(schermo(), contenuto.getAltezza(), FASCIA);
		}
		colonna.avanza(secondi, VELOCITA);
		Quadro quadro = new Quadro(Fase.TROFEI, true, 0f, colonna, contenuto, 1f, Collections.<Logo>emptyList());
		if (colonna.isUscito()) {
			passaA(Fase.STORIA);
		}
		return quadro;
	}
}
