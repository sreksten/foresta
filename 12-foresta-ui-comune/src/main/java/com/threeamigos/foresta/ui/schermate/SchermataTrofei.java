package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

/**
 * La pagina dei trofei, senza disegno: si apre dall'inventario (comando MOSTRA_TROFEI) e si chiude con ANNULLA. Il titolo in cima e sotto tutti i trofei
 * ({@link PaginaTrofei}), che si scorrono a mano come il riquadro delle missioni, con la rotella, trascinando o con le frecce, a passi più lunghi perché
 * l'elenco è lungo. Chi disegna (Swing, libGDX) mette il fondo, il {@link #getTitolo() titolo} e la colonna ({@link #getColonna()}), e dice quanto sono
 * grandi le frecce ({@link #misuraDellImmagine}).
 */
public abstract class SchermataTrofei implements Finestra {

	/**
	 * Di quanti pixel scorre l'elenco a ogni scatto della rotella: il triplo del riquadro delle missioni.
	 */
	public static final int PASSO_SCORRIMENTO = 6;
	/**
	 * Quanti scatti vale un clic su una freccia.
	 */
	private static final int SCATTI_PER_CLIC = 10;
	public static final int MARGINE = 50;
	public static final int QUOTA_TITOLO = 20;
	private static final int SPAZIO_SOTTO_IL_TITOLO = 24;

	protected final int width;
	protected final int height;
	private ColonnaScorrevole colonna;
	private PaginaTrofei pagina;

	protected final VistaPartita vistaPartita;

	protected SchermataTrofei(int width, int height, VistaPartita vistaPartita) {
		this.vistaPartita = vistaPartita;
		this.width = width;
		this.height = height;
	}

	/**
	 * Quanto è grande l'immagine (le frecce <code>icone/ComponenteScorrevole-FrecciaSu.png</code>…), come la disegna chi disegna
	 */
	protected abstract Misura misuraDellImmagine(String risorsa);

	/**
	 * Prepara la pagina con i trofei come sono adesso, dalla cima dell'elenco.
	 */
	public void apri() {
		int cimaElenco = QUOTA_TITOLO + LayoutDelTestoGrande.ALTEZZA_RIGA + SPAZIO_SOTTO_IL_TITOLO;
		pagina = PaginaTrofei.crea(vistaPartita, width - 2 * MARGINE, false);
		colonna = new ColonnaScorrevole(new Area(0, cimaElenco, width, height - cimaElenco), pagina.getAltezza());
		paginaAperta(pagina);
	}

	/**
	 * La pagina è stata aperta con i trofei di adesso: chi disegna prepara le sue immagini
	 */
	protected void paginaAperta(PaginaTrofei pagina) {
	}

	/**
	 * La pagina aperta (null se non lo è)
	 */
	public PaginaTrofei getPagina() {
		return pagina;
	}

	/**
	 * La colonna che scorre (null finché la pagina non è aperta)
	 */
	public ColonnaScorrevole getColonna() {
		return colonna;
	}

	public String getTitolo() {
		return TestiDellaUI.get("TITOLO_TROFEI");
	}

	/**
	 * Dove sta il titolo, centrato: la x del suo angolo in alto a sinistra (la y è {@link #QUOTA_TITOLO})
	 */
	public int getXDelTitolo() {
		return (width - LayoutDelTestoGrande.larghezza(getTitolo())) >> 1;
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (colonna != null) {
			colonna.scorri((movimentoRotella == MovimentoRotella.SU ? -1 : 1) * numeroRotazioni * PASSO_SCORRIMENTO);
		}
	}

	/**
	 * Se la pagina si muove da sola (l'inerzia dopo un trascinamento): va ridisegnata a ogni fotogramma
	 */
	public boolean inTransizione() {
		return colonna != null && colonna.isInMovimento();
	}

	/**
	 * Il gesto che comincia sulla pagina prosegue finché non si lascia il tasto, anche fuori da essa
	 */
	@Override
	public boolean catturaIlTrascinamento() {
		return true;
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (colonna != null && tasto == Tasto.SINISTRO) {
			colonna.premi(y);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		if (colonna != null) {
			colonna.trascina(y);
		}
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (colonna != null && tasto == Tasto.SINISTRO) {
			colonna.rilascia();
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		// Il rilascio di un trascinamento non è un click (sulle frecce, per esempio)
		if (colonna == null || colonna.isGestoTrascinato()) {
			return;
		}
		if (colonna.isSullaFrecciaSu(x, y, misuraDellImmagine(NomiDelleImmagini.FRECCIA_SU))) {
			colonna.scorri(-SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		} else if (colonna.isSullaFrecciaGiu(x, y, misuraDellImmagine(NomiDelleImmagini.FRECCIA_GIU))) {
			colonna.scorri(SCATTI_PER_CLIC * PASSO_SCORRIMENTO);
		}
	}
}
