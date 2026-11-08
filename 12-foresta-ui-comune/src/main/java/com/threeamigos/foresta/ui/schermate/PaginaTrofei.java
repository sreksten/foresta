package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import com.threeamigos.foresta.ui.testo.FontTool;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tutti i trofei in un'unica colonna, da far scorrere, senza disegno: nell'intro, e nella pagina dei trofei che si apre dall'inventario. Per tipologia, ognuno
 * con il nome nell'alfabeto grande, a destra quanto ne manca (ottenuti/necessari) e sotto la descrizione nel font medio; bianco se vinto, grigio scuro se
 * manca. Dice dove sta ogni parte (coordinate relative all'angolo in alto a sinistra della colonna) e quanto è alta. Lo usano Swing e libGDX.
 */
public final class PaginaTrofei {

	private static final DoomdarkColorModel.Color VINTO = DoomdarkColorModel.Color.WHITE;
	private static final DoomdarkColorModel.Color MANCANTE = DoomdarkColorModel.Color.DARK_GRAY;
	private static final int SPAZIO_QUANTITA = 12;
	private static final int SPAZIO_DOPO_IL_TITOLO = 24;

	/**
	 * Un trofeo: il nome (già scuro se manca), la descrizione (le righe, a capo entro la larghezza dei testi) e la quantità, con i loro posti
	 */
	public static final class Voce {
		private final ImmagineDiPixel nome;
		private final int yDelNome;
		private final List<String> righeDellaDescrizione;
		private final int yDellaDescrizione;
		private final String quantita;
		private final int xDellaQuantita;
		private final DoomdarkColorModel.Color colore;

		private Voce(ImmagineDiPixel nome, int yDelNome, List<String> righeDellaDescrizione, int yDellaDescrizione, String quantita, int xDellaQuantita,
					 DoomdarkColorModel.Color colore) {
			this.nome = nome;
			this.yDelNome = yDelNome;
			this.righeDellaDescrizione = righeDellaDescrizione;
			this.yDellaDescrizione = yDellaDescrizione;
			this.quantita = quantita;
			this.xDellaQuantita = xDellaQuantita;
			this.colore = colore;
		}

		/**
		 * Il nome nell'alfabeto grande, a sinistra
		 */
		public ImmagineDiPixel getNome() {
			return nome;
		}

		public int getYDelNome() {
			return yDelNome;
		}

		public List<String> getRigheDellaDescrizione() {
			return righeDellaDescrizione;
		}

		/**
		 * Dove comincia la descrizione (a sinistra), e dove sta la quantità, che le sta accanto a destra
		 */
		public int getYDellaDescrizione() {
			return yDellaDescrizione;
		}

		public String getQuantita() {
			return quantita;
		}

		public int getXDellaQuantita() {
			return xDellaQuantita;
		}

		/**
		 * Il colore della descrizione e della quantità
		 */
		public DoomdarkColorModel.Color getColore() {
			return colore;
		}
	}

	private final int larghezza;
	private final int altezza;
	private final ImmagineDiPixel titolo;
	private final List<Voce> voci;

	private PaginaTrofei(int larghezza, int altezza, ImmagineDiPixel titolo, List<Voce> voci) {
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.titolo = titolo;
		this.voci = voci;
	}

	/**
	 * I trofei così come sono adesso, in una colonna larga quanto chiesto; con il titolo "trofei" in cima, se serve.
	 */
	public static PaginaTrofei crea(VistaPartita vistaPartita, int larghezza, boolean conTitolo) {
		DoomdarkFont font = DoomdarkFont.medio();
		int spazioFraTrofei = font.getHeight();
		// Di quanto la descrizione sale sul nome: un quarto di lettera del suo font
		int sovrapposizione = font.getHeight() / 4;
		int y = 0;
		ImmagineDiPixel titolo = null;
		if (conTitolo) {
			titolo = LayoutDelTestoGrande.immagine(TestiDellaUI.get("TITOLO_TROFEI"), larghezza, true);
			y += titolo.getAltezza() + SPAZIO_DOPO_IL_TITOLO;
		}
		List<Voce> voci = new ArrayList<>();
		for (TipoTrofeo tipo : TipoTrofeo.values()) {
			boolean vinto = vistaPartita.isTrofeoVinto(tipo);
			int obiettivo = vistaPartita.getObiettivoTrofeo(tipo);
			int progresso = vinto ? obiettivo : vistaPartita.getProgressoTrofeo(tipo);
			DoomdarkColorModel.Color colore = vinto ? VINTO : MANCANTE;
			String quantita = progresso + "/" + obiettivo;
			int larghezzaDellaQuantita = Math.max(1, FontTool.getWidth(font, quantita));
			int larghezzaTesti = larghezza - larghezzaDellaQuantita - SPAZIO_QUANTITA;
			ImmagineDiPixel nome = LayoutDelTestoGrande.immagine(tipo.getNome(), larghezzaTesti, false);
			List<String> righe = FontTool.split(font, tipo.getDescrizione(), Math.max(larghezzaTesti, font.getHeight()));
			int yDelNome = y;
			y += nome.getAltezza() - sovrapposizione;
			voci.add(new Voce(vinto ? nome : LayoutDelTestoGrande.scura(nome), yDelNome, righe, y, quantita, larghezza - larghezzaDellaQuantita, colore));
			y += righe.size() * font.getHeight() + spazioFraTrofei;
		}
		return new PaginaTrofei(larghezza, Math.max(1, y), titolo, Collections.unmodifiableList(voci));
	}

	public int getLarghezza() {
		return larghezza;
	}

	/**
	 * Quanto è alta la colonna
	 */
	public int getAltezza() {
		return altezza;
	}

	/**
	 * Il titolo nell'alfabeto grande, centrato nella colonna, in cima (null se non c'è)
	 */
	public ImmagineDiPixel getTitolo() {
		return titolo;
	}

	public List<Voce> getVoci() {
		return voci;
	}

	/**
	 * La pagina come contenuto di una colonna: il titolo, i nomi grandi, le descrizioni e le quantità
	 */
	public ContenutoDiColonna comeContenuto() {
		DoomdarkFont font = DoomdarkFont.medio();
		List<ContenutoDiColonna.Elemento> elementi = new ArrayList<>();
		if (titolo != null) {
			elementi.add(ContenutoDiColonna.Elemento.testoGrande(titolo, 0, 0));
		}
		for (Voce voce : voci) {
			elementi.add(ContenutoDiColonna.Elemento.testoGrande(voce.getNome(), 0, voce.getYDelNome()));
			elementi.add(ContenutoDiColonna.Elemento.testo(voce.getRigheDellaDescrizione(), font, voce.getColore(), 0, voce.getYDellaDescrizione()));
			elementi.add(ContenutoDiColonna.Elemento.testo(Collections.singletonList(voce.getQuantita()), font, voce.getColore(), voce.getXDellaQuantita(),
					voce.getYDellaDescrizione()));
		}
		return new ContenutoDiColonna(larghezza, altezza, elementi);
	}
}
