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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * L'introduzione del gioco, senza disegno: la storia sale sullo sfondo della storia, i loghi compaiono appena la storia è salita sopra di loro, poi loghi e
 * sfondo spariscono insieme; la classifica sale e si ferma, poi sparisce; i trofei scorrono tutti. Poi si ricomincia. Qui c'è il tempo e che cosa si vede in
 * ogni fotogramma ({@link #avanza()}): chi disegna (Swing, libGDX) mette il {@link Quadro}.
 */
public final class IntroDelGioco {

	// Lo scorrimento dell'intro (vedi ColonnaScorrevole): pixel al secondo, e quanto è alta la fascia di dissolvenza ai bordi
	private static final double VELOCITA = 30;
	public static final int FASCIA = 40;
	// Il logo della Foresta compare quando la storia è salita almeno tanto sopra di lui
	private static final int MARGINE_LOGHI = 20;
	// Il logo 3AM sta in basso a destra, a questa distanza dai due bordi
	private static final int MARGINE_LOGO_3AM = 20;
	// Fra una riga e l'altra della storia, oltre all'altezza di riga normale: un terzo dell'altezza delle lettere
	private static final int INTERLINEA_STORIA = LayoutDelTestoGrande.ALTEZZA_GLIFO / 3;
	private static final double SECONDI_DISSOLVENZA = 1;
	private static final double SECONDI_LOGHI = 3;
	private static final double SECONDI_CLASSIFICA = 5;
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
		private final List<Elemento> loghi;
		private final float opacitaDeiLoghi;

		private Quadro(Fase fase, boolean ombraDelDrago, float opacitaDelloSfondoDellaStoria, ColonnaScorrevole colonna, ContenutoDiColonna contenuto,
					   float opacitaDellaColonna, List<Elemento> loghi, float opacitaDeiLoghi) {
			this.fase = fase;
			this.ombraDelDrago = ombraDelDrago;
			this.opacitaDelloSfondoDellaStoria = opacitaDelloSfondoDellaStoria;
			this.colonna = colonna;
			this.contenuto = contenuto;
			this.opacitaDellaColonna = opacitaDellaColonna;
			this.loghi = loghi;
			this.opacitaDeiLoghi = opacitaDeiLoghi;
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
		 * I due loghi con l'alone (vuoto se non si vedono ancora)
		 */
		public List<Elemento> getLoghi() {
			return loghi;
		}

		public float getOpacitaDeiLoghi() {
			return opacitaDeiLoghi;
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
	// Il tempo della fase: dall'ultimo fotogramma, da quando i loghi compaiono, da quando si sta fermi
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
	 * La classifica in un contenuto, nell'alfabeto grande: il nome a sinistra, il punteggio a destra, una riga ogni {@link SchermateSenzaPartita#RIGA_CLASSIFICA}
	 */
	public static ContenutoDiColonna classifica(GestorePunteggi gestorePunteggi, int width) {
		int righe = gestorePunteggi.getConteggio();
		List<Elemento> elementi = new ArrayList<>();
		for (int posizione = 0; posizione < righe; posizione++) {
			Punteggio punteggio = gestorePunteggi.getPunteggio(posizione);
			int y = posizione * SchermateSenzaPartita.RIGA_CLASSIFICA;
			elementi.add(Elemento.testoGrande(riga(punteggio.getNome()), SchermateSenzaPartita.MARGINE, y));
			String valore = String.valueOf(punteggio.getPunteggio());
			elementi.add(Elemento.testoGrande(riga(valore), width - SchermateSenzaPartita.MARGINE - LayoutDelTestoGrande.larghezza(valore), y));
		}
		return new ContenutoDiColonna(width, righe * SchermateSenzaPartita.RIGA_CLASSIFICA, elementi);
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
		double secondi = secondiDalFotogrammaPrecedente();
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

	private Quadro storia(double secondi) {
		if (colonna == null) {
			contenuto = contenutoDellaStoria();
			colonna = ColonnaScorrevole.dalBasso(schermo(), contenuto.getAltezza(), FASCIA);
		}
		if (!colonna.isUscito()) {
			colonna.avanza(secondi, VELOCITA);
		}
		secondiFase += secondi;
		// Il logo della Foresta arriva quando la storia non ci finirebbe sopra, e resta un poco dopo che è uscita; il suo centro sta a un terzo dello
		// schermo. Il logo 3AM sta in basso a destra, dove la storia non passa mai quando arriva il logo della Foresta, e compare e svanisce insieme a lui
		Misura logo3AM = conLoZoom(RISORSA_LOGO_3AM);
		Misura logoForesta = conLoZoom(RISORSA_LOGO_FORESTA);
		int cimaLoghi = height / 3 - logoForesta.altezza / 2;
		if (secondiLoghi < 0 && colonna.getFondo() <= cimaLoghi - MARGINE_LOGHI) {
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
		List<Elemento> loghi = new ArrayList<>();
		float opacitaLoghi = 0;
		if (secondiLoghi >= 0) {
			opacitaLoghi = (float) Math.min(1, secondiLoghi / SECONDI_DISSOLVENZA) * (1 - uscita);
			loghi.add(Elemento.immagineConAlone(RISORSA_LOGO_3AM, logo3AM.larghezza, logo3AM.altezza, SchermateSenzaPartita.RAGGIO_ALONE,
					width - MARGINE_LOGO_3AM - logo3AM.larghezza, height - MARGINE_LOGO_3AM - logo3AM.altezza));
			loghi.add(Elemento.immagineConAlone(RISORSA_LOGO_FORESTA, logoForesta.larghezza, logoForesta.altezza, SchermateSenzaPartita.RAGGIO_ALONE,
					(width - logoForesta.larghezza) >> 1, cimaLoghi));
		}
		Quadro quadro = new Quadro(Fase.STORIA, false, comparsa * (1 - uscita), colonna, contenuto, 1f, loghi, opacitaLoghi);
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
		Quadro quadro = new Quadro(Fase.CLASSIFICA, true, 0f, colonna, contenuto, 1 - uscita, Collections.<Elemento>emptyList(), 0f);
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
		Quadro quadro = new Quadro(Fase.TROFEI, true, 0f, colonna, contenuto, 1f, Collections.<Elemento>emptyList(), 0f);
		if (colonna.isUscito()) {
			passaA(Fase.STORIA);
		}
		return quadro;
	}
}
