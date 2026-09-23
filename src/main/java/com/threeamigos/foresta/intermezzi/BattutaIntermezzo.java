package com.threeamigos.foresta.intermezzi;

/**
 * Una battuta di dialogo, mostrata come fumetto. Chi parla è un elemento della pagina
 * (il fumetto punta alla sua bocca e lo segue se si muove), oppure un punto fisso dello
 * schermo, per chi parla da fuori scena.
 * <p>
 * Se non si indica quando comincia, la battuta parte alla fine della precedente; se non
 * si indica quanto dura, la durata dipende dalla lunghezza del testo. Se non si indica
 * dove mettere il fumetto, la UI lo colloca sopra chi parla, verso il centro dello schermo.
 * <pre>
 *     BattutaIntermezzo.di("eremita", "Chi va là?")
 *     BattutaIntermezzo.di("eroe", "Un amico.").perSecondi(2)
 *     BattutaIntermezzo.daPunto(0.9, 0.1, "Aiuto!").daSecondo(6)
 * </pre>
 */
public final class BattutaIntermezzo {

	static final double DURATA_MINIMA = 2.0;
	static final double SECONDI_BASE = 1.0;
	static final double SECONDI_PER_CARATTERE = 0.06;

	private final String idElemento;
	private final double puntoX;
	private final double puntoY;
	private final String testo;
	private double daSecondo = Double.NaN;
	private double perSecondi = Double.NaN;
	private double nuvolaX = Double.NaN;
	private double nuvolaY = Double.NaN;

	private BattutaIntermezzo(String idElemento, double puntoX, double puntoY, String testo) {
		if (testo == null || testo.isEmpty()) {
			throw new IllegalArgumentException("Una battuta deve avere un testo");
		}
		this.idElemento = idElemento;
		this.puntoX = puntoX;
		this.puntoY = puntoY;
		this.testo = testo;
	}

	/** Una battuta detta dall'elemento con l'identificativo indicato. */
	public static BattutaIntermezzo di(String idElemento, String testo) {
		if (idElemento == null || idElemento.isEmpty()) {
			throw new IllegalArgumentException("Manca chi dice la battuta");
		}
		return new BattutaIntermezzo(idElemento, Double.NaN, Double.NaN, testo);
	}

	/** Una battuta il cui fumetto punta a un punto fisso dello schermo (in frazioni). */
	public static BattutaIntermezzo daPunto(double x, double y, String testo) {
		return new BattutaIntermezzo(null, x, y, testo);
	}

	/** Secondi dall'inizio della pagina a cui compare il fumetto. */
	public BattutaIntermezzo daSecondo(double secondi) {
		daSecondo = secondi;
		return this;
	}

	public BattutaIntermezzo perSecondi(double secondi) {
		perSecondi = secondi;
		return this;
	}

	/** Il centro del fumetto, in frazioni dello schermo, invece della posizione automatica. */
	public BattutaIntermezzo nuvolaA(double x, double y) {
		nuvolaX = x;
		nuvolaY = y;
		return this;
	}

	/** L'identificativo di chi parla, o null se il fumetto punta a un punto fisso. */
	public String getIdElemento() {
		return idElemento;
	}

	public double getPuntoX() {
		return puntoX;
	}

	public double getPuntoY() {
		return puntoY;
	}

	public String getTesto() {
		return testo;
	}

	public boolean hasPosizioneNuvola() {
		return !Double.isNaN(nuvolaX);
	}

	public double getNuvolaX() {
		return nuvolaX;
	}

	public double getNuvolaY() {
		return nuvolaY;
	}

	boolean hasInizio() {
		return !Double.isNaN(daSecondo);
	}

	double getInizio() {
		return daSecondo;
	}

	double getDurata() {
		if (!Double.isNaN(perSecondi)) {
			return perSecondi;
		}
		return Math.max(DURATA_MINIMA, SECONDI_BASE + SECONDI_PER_CARATTERE * testo.length());
	}
}
