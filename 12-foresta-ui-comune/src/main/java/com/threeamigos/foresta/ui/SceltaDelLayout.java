package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.TipoDiLayout;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;

import java.util.Locale;

/**
 * Quale layout usare su uno schermo vero, contando in dp (vedi {@code layout_telefono.md} §2):
 * <ul>
 * <li>il layout del <b>tablet</b>, quello di sempre, se lo schermo lo contiene a scala 1: con la barra delle icone in basso se ci sta (e il giocatore non
 * l'ha voluta di lato), altrimenti a destra (o a sinistra, se l'ha voluta lì); l'area virtuale è quella di
 * {@link DisposizioneDelloSchermo#dimensioniDelloSchermo}, e la scala quella con cui ci entra;</li>
 * <li>altrimenti il layout del <b>telefono</b>, con la barra a destra (o a sinistra, se il giocatore l'ha voluta lì): l'area virtuale è grande quanto lo schermo in dp ma mai sotto il minimo; se lo schermo
 * è più piccolo si riduce tutto della stessa scala finché il minimo ci sta, e la larghezza si allunga fino al rapporto dello schermo,
 * senza bande.</li>
 * </ul>
 * Il minimo è largo quanto la locazione, i riquadri impilati e la barra con i loro spazi, e ha il rapporto dello schermo più piccolo
 * che il gioco regge, 800×480.
 */
public final class SceltaDelLayout {

	private static final int SP = Finestra.SPACING;
	// Il rapporto dello schermo più piccolo che il gioco regge (800x480)
	private static final double RAPPORTO_DEL_MINIMO = 480.0 / 800.0;

	private final TipoDiLayout tipo;
	private final double scala;
	private final Misura areaVirtuale;
	private final Orientamento orientamento;
	private final boolean barraASinistra;

	private SceltaDelLayout(TipoDiLayout tipo, double scala, Misura areaVirtuale, Orientamento orientamento, boolean barraASinistra) {
		this.tipo = tipo;
		this.scala = scala;
		this.areaVirtuale = areaVirtuale;
		this.orientamento = orientamento;
		this.barraASinistra = barraASinistra;
	}

	/**
	 * @param misure le misure delle immagini, senza zoom
	 * @param zoom   lo zoom con cui si disegnano le immagini
	 */
	public static SceltaDelLayout per(MisuraDelloSchermo schermo, MisureDelleImmagini misure, double zoom) {
		return per(schermo, misure, zoom, null);
	}

	/**
	 * @param preferita dove il giocatore vuole la barra (dalle opzioni o dagli argomenti), o null per lasciarlo decidere qui: in basso sul tablet se ci
	 *                  sta, se no a destra; sul telefono la barra in basso non c'è, e va a destra
	 */
	public static SceltaDelLayout per(MisuraDelloSchermo schermo, MisureDelleImmagini misure, double zoom, PosizioneDellaBarra preferita) {
		double larghezzaDp = schermo.getLarghezzaDp();
		double altezzaDp = schermo.getAltezzaDp();
		boolean aSinistra = preferita == PosizioneDellaBarra.SINISTRA;
		if (preferita == null || preferita == PosizioneDellaBarra.BASSO) {
			Misura inBasso = DisposizioneDelloSchermo.dimensioniDelloSchermo(Orientamento.ORIZZONTALE, misure, zoom);
			if (larghezzaDp >= inBasso.larghezza && altezzaDp >= inBasso.altezza) {
				return new SceltaDelLayout(TipoDiLayout.TABLET, Math.min(larghezzaDp / inBasso.larghezza, altezzaDp / inBasso.altezza), inBasso,
						Orientamento.ORIZZONTALE, false);
			}
		}
		Misura tablet = DisposizioneDelloSchermo.dimensioniDelloSchermo(Orientamento.VERTICALE, misure, zoom);
		if (larghezzaDp >= tablet.larghezza && altezzaDp >= tablet.altezza) {
			return new SceltaDelLayout(TipoDiLayout.TABLET, Math.min(larghezzaDp / tablet.larghezza, altezzaDp / tablet.altezza), tablet,
					Orientamento.VERTICALE, aSinistra);
		}
		Misura minimo = minimoDelTelefono(misure, zoom);
		double scala = Math.min(1, Math.min(altezzaDp / minimo.altezza, larghezzaDp / minimo.larghezza));
		Misura area = new Misura((int) Math.round(larghezzaDp / scala), (int) Math.round(altezzaDp / scala));
		return new SceltaDelLayout(TipoDiLayout.TELEFONO, scala, area, Orientamento.VERTICALE, aSinistra);
	}

	/**
	 * Se su quello schermo la barra può stare in basso: solo il tablet (o il desktop), e solo se in basso ci sta
	 */
	public static boolean barraInBassoPossibile(MisuraDelloSchermo schermo, MisureDelleImmagini misure, double zoom) {
		return per(schermo, misure, zoom, PosizioneDellaBarra.BASSO).getOrientamento() == Orientamento.ORIZZONTALE;
	}

	/**
	 * L'area virtuale più piccola del layout del telefono: in larghezza la locazione, i riquadri impilati e la barra delle icone, con i
	 * loro spazi; in altezza quella che ha con la larghezza il rapporto di 800×480.
	 */
	public static Misura minimoDelTelefono(MisureDelleImmagini misure, double zoom) {
		int locazione = DisposizioneDelloSchermo.conZoom(misure, DisposizioneDelloSchermo.RISORSA_BOSCO, zoom).larghezza;
		int pila = DisposizioneDelloSchermo.conZoom(misure, DisposizioneDelloSchermo.RISORSA_CORNICE_GRANDE, zoom).larghezza;
		int larghezza = SP + locazione + SP + pila + SP + DisposizioneDelloSchermo.SPESSORE_BARRA_ICONE;
		return new Misura(larghezza, (int) Math.round(larghezza * RAPPORTO_DEL_MINIMO));
	}

	public TipoDiLayout getTipo() {
		return tipo;
	}

	/** Dove sta la barra delle icone: in basso (orizzontale) o di lato (verticale) */
	public Orientamento getOrientamento() {
		return orientamento;
	}

	/** Se, di lato, la barra sta a sinistra */
	public boolean isBarraASinistra() {
		return barraASinistra;
	}

	/** Quanti dp vale un pixel virtuale */
	public double getScala() {
		return scala;
	}

	/** La misura dell'area virtuale in cui si disegna, barra delle icone compresa */
	public Misura getAreaVirtuale() {
		return areaVirtuale;
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "layout %s, barra %s, scala %.2f, area virtuale %dx%d", tipo,
				orientamento == Orientamento.ORIZZONTALE ? "in basso" : barraASinistra ? "a sinistra" : "a destra", scala, areaVirtuale.larghezza,
				areaVirtuale.altezza);
	}
}
