package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;

/**
 * Un contenuto alto quanto si vuole che scorre in verticale dentro un'area, che ne mostra solo la parte che ci sta; senza disegno: dice dove sta il contenuto,
 * quando arriva e quando esce, e le frecce. Chi disegna (Swing, libGDX) mette il contenuto centrato in orizzontale nell'area, spostato in su di
 * {@link #getScorrimento()}, e lo ritaglia all'area. Si usa in due modi:
 * <ul>
 * <li>automatico ({@link #dalBasso}): il contenuto sale a velocità costante ({@link #avanza}), partendo da sotto l'area, finché esce in alto o si ferma a una
 * quota ({@link #fermaA}); ai bordi dell'area, in una fascia alta <code>fascia</code> pixel, sfuma ({@link #opacitaAllaQuota}), così compare e scompare in
 * dissolvenza. Così scorrono la storia, la classifica e i trofei dell'intro;</li>
 * <li>manuale ({@link #ColonnaScorrevole(Area, int)}): il contenuto parte con la cima in cima all'area e si sposta di quanto chiede chi la usa
 * ({@link #scorri}: la rotella del mouse, le frecce) o trascinandolo ({@link #premi}, {@link #trascina}, {@link #rilascia}: col mouse o col dito, con inerzia
 * ed elastico, vedi {@link ScorrimentoSuUnAsse}), senza uscire dall'area più del necessario. Le frecce dicono se c'è altro sopra o sotto. Così scorrono la
 * pagina dei trofei e il bestiario.</li>
 * </ul>
 */
public final class ColonnaScorrevole {

	private static final int MARGINE_FRECCE = 4;

	private final int altezzaContenuto;
	private final Area area;
	private final int fascia;
	// Dov'è la cima del contenuto rispetto alla cima dell'area: positiva sotto, negativa sopra
	private double y;
	private Integer quotaDiArrivo;
	// Lo scorrimento manuale (null in quello automatico): decide dov'è il contenuto, con inerzia ed elastico
	private final ScorrimentoSuUnAsse motore;

	/**
	 * Lo scorrimento manuale: il contenuto parte con la cima in cima all'area, senza dissolvenze.
	 */
	public ColonnaScorrevole(Area area, int altezzaContenuto) {
		this(area, altezzaContenuto, 0, 0, new ScorrimentoSuUnAsse(System::nanoTime));
	}

	private ColonnaScorrevole(Area area, int altezzaContenuto, int fascia, double y, ScorrimentoSuUnAsse motore) {
		this.motore = motore;
		this.altezzaContenuto = altezzaContenuto;
		this.area = area;
		this.fascia = fascia;
		this.y = y;
	}

	/**
	 * Lo scorrimento automatico: il contenuto parte appena sotto l'area e sale, sfumando nella fascia ai bordi.
	 */
	public static ColonnaScorrevole dalBasso(Area area, int altezzaContenuto, int fascia) {
		return new ColonnaScorrevole(area, altezzaContenuto, fascia, area.getAltezza(), null);
	}

	public Area getArea() {
		return area;
	}

	public int getAltezzaContenuto() {
		return altezzaContenuto;
	}

	/**
	 * Salendo, il contenuto si ferma quando la sua cima arriva a quella quota dalla cima dell'area.
	 */
	public ColonnaScorrevole fermaA(int quota) {
		quotaDiArrivo = quota;
		return this;
	}

	/**
	 * Fa salire il contenuto per quei secondi, a quella velocità in pixel al secondo.
	 */
	public void avanza(double secondi, double velocita) {
		y -= secondi * velocita;
		if (quotaDiArrivo != null && y < quotaDiArrivo) {
			y = quotaDiArrivo;
		}
	}

	/**
	 * Se il contenuto si è fermato alla sua quota.
	 */
	public boolean isArrivato() {
		return quotaDiArrivo != null && y <= quotaDiArrivo;
	}

	/**
	 * Se il contenuto è uscito tutto dalla cima dell'area.
	 */
	public boolean isUscito() {
		return y + altezzaContenuto <= 0;
	}

	/**
	 * Dove sta il fondo del contenuto, nelle coordinate di chi disegna.
	 */
	public int getFondo() {
		return area.getY() + (int) Math.round(y) + altezzaContenuto;
	}

	/**
	 * Dov'è la cima del contenuto rispetto alla cima dell'area (positiva sotto, negativa sopra): è quella con cui lo si disegna, anche se è tirato oltre un
	 * capo
	 */
	public double getY() {
		return y;
	}

	/**
	 * Sposta il contenuto di quei pixel (positivi per vedere quello che c'è sotto), senza staccarne la cima dalla cima dell'area né il fondo dal fondo.
	 */
	public void scorri(int pixel) {
		if (motore != null) {
			sincronizzaLimite();
			motore.scorriDi(pixel);
			y = -motore.getScorrimento();
			return;
		}
		y = Math.max(Math.min(0, area.getAltezza() - altezzaContenuto), Math.min(0, y - pixel));
	}

	/**
	 * Dice allo scorrimento manuale di quanto il contenuto può scorrere nell'area
	 */
	private void sincronizzaLimite() {
		double massimo = Math.max(0, altezzaContenuto - area.getAltezza());
		motore.imposta(massimo > 0, massimo);
	}

	/**
	 * Fa avanzare l'inerzia di un trascinamento fino a adesso (solo scorrimento manuale): va chiamato a ogni fotogramma, prima di disegnare
	 */
	public void aggiorna() {
		if (motore != null) {
			sincronizzaLimite();
			motore.aggiorna();
			y = -motore.getScorrimento();
		}
	}

	/**
	 * Il puntatore si preme sul contenuto, a quella quota (nelle coordinate di chi disegna): comincia un possibile trascinamento. Solo scorrimento manuale.
	 */
	public void premi(int quota) {
		if (motore != null) {
			sincronizzaLimite();
			motore.premi(quota, true);
		}
	}

	/**
	 * Il puntatore si sposta, premuto: oltre la soglia il contenuto lo segue
	 */
	public void trascina(int quota) {
		if (motore != null) {
			motore.trascina(quota);
			y = -motore.getScorrimento();
		}
	}

	/**
	 * Il puntatore si lascia: se si trascinava con una certa velocità il contenuto prosegue per inerzia
	 */
	public void rilascia() {
		if (motore != null) {
			motore.rilascia();
		}
	}

	/**
	 * Se l'ultimo gesto è stato un trascinamento: il click che segue il rilascio non vale
	 */
	public boolean isGestoTrascinato() {
		return motore != null && motore.isGestoTrascinato();
	}

	/**
	 * Se il contenuto si muove da solo (inerzia di un trascinamento, o ritorno dall'elastico): chi lo mostra deve ridisegnare a ogni fotogramma
	 */
	public boolean isInMovimento() {
		return motore != null && motore.isInMovimento();
	}

	/**
	 * Di quanti pixel il contenuto è scorso dalla cima (0 se è in cima): per risalire da un punto dell'area a un punto del contenuto, per esempio la riga di un
	 * elenco su cui si è cliccato
	 */
	public int getScorrimento() {
		return (int) Math.round(-y);
	}

	/**
	 * Se il punto (nelle coordinate di chi disegna) è dentro l'area
	 */
	public boolean contiene(int x, int y) {
		return area.contiene(x, y);
	}

	/**
	 * La cima dell'area, nelle coordinate di chi disegna
	 */
	public int getCima() {
		return area.getY();
	}

	/**
	 * Se sopra l'area c'è ancora un pezzo di contenuto.
	 */
	public boolean haAltroSopra() {
		return y < 0;
	}

	/**
	 * Se sotto l'area c'è ancora un pezzo di contenuto.
	 */
	public boolean haAltroSotto() {
		return y + altezzaContenuto > area.getAltezza();
	}

	/**
	 * Quanto è opaco il contenuto a quella quota dalla cima dell'area (da 0 a 1): in una fascia ai bordi sfuma da trasparente a pieno; senza fascia sempre
	 * pieno
	 */
	public float opacitaAllaQuota(int quota) {
		if (fascia <= 0) {
			return 1f;
		}
		if (quota < fascia) {
			return Math.max(0f, (float) quota / fascia);
		}
		int dalFondo = area.getAltezza() - quota;
		if (dalFondo < fascia) {
			return Math.max(0f, (float) dalFondo / fascia);
		}
		return 1f;
	}

	/**
	 * Il rettangolo della freccia verso l'alto (nelle coordinate di chi disegna): a destra in alto nell'area, come nel riquadro delle missioni
	 */
	public Area areaDellaFrecciaSu(Misura freccia) {
		return new Area(xFrecce(freccia), area.getY() + MARGINE_FRECCE, freccia.larghezza, freccia.altezza);
	}

	/**
	 * Il rettangolo della freccia verso il basso: a destra in basso nell'area
	 */
	public Area areaDellaFrecciaGiu(Misura freccia) {
		return new Area(xFrecce(freccia), area.getY() + area.getAltezza() - freccia.altezza - MARGINE_FRECCE, freccia.larghezza, freccia.altezza);
	}

	/**
	 * Se il punto (nelle coordinate di chi disegna) è sulla freccia verso l'alto, che c'è solo se sopra c'è altro.
	 */
	public boolean isSullaFrecciaSu(int x, int y, Misura freccia) {
		return haAltroSopra() && areaDellaFrecciaSu(freccia).contiene(x, y);
	}

	/**
	 * Se il punto è sulla freccia verso il basso, che c'è solo se sotto c'è altro.
	 */
	public boolean isSullaFrecciaGiu(int x, int y, Misura freccia) {
		return haAltroSotto() && areaDellaFrecciaGiu(freccia).contiene(x, y);
	}

	private int xFrecce(Misura freccia) {
		return area.getX() + area.getLarghezza() - freccia.larghezza - MARGINE_FRECCE;
	}
}
