package com.threeamigos.foresta.ui;

/**
 * La pila dei riquadri del telefono che scorre di lato, senza disegno (vedi {@code layout_telefono.md} §4.6): espansa, com'è di solito, o compattata
 * verso destra, dove se ne vede solo il {@link #PARTE_VISIBILE} di sinistra e la locazione ha più spazio per i personaggi. Ferma sta sempre a una
 * delle due estremità: trascinata segue il dito, e lasciata va fino in fondo nel verso dell'ultimo movimento; {@link #commuta()} (la freccia) e
 * {@link #espandi()} (il tutorial) la fanno scorrere allo stesso modo. Lo scostamento è di quanto la pila sta a destra della sua posizione espansa.
 */
public final class PilaScorrevole {

	/** Quanto della pila si vede da compattata, da sinistra */
	public static final double PARTE_VISIBILE = 0.2;
	/** Quanto ci mette a scorrere da un'estremità all'altra */
	static final double SECONDI_DELLO_SCORRIMENTO = 0.3;

	private final int scostamentoMassimo;
	private double scostamento;
	// Verso dove sta andando da sola: +1 verso compattata, -1 verso espansa, 0 ferma (o trascinata)
	private int verso;
	private boolean trascinata;
	private int xDellaPresa;
	private double scostamentoAllaPresa;
	private int ultimoX;
	private int versoDelTrascinamento;

	/**
	 * @param larghezzaDellaPila la larghezza dei riquadri impilati
	 */
	public PilaScorrevole(int larghezzaDellaPila) {
		scostamentoMassimo = (int) Math.round(larghezzaDellaPila * (1 - PARTE_VISIBILE));
	}

	/** Di quanto la pila sta a destra della sua posizione espansa */
	public int getScostamento() {
		return (int) Math.round(scostamento);
	}

	public int getScostamentoMassimo() {
		return scostamentoMassimo;
	}

	/** Se la pila sta, o sta andando, dalla parte espansa: allora la freccia indica che si può compattare, verso destra */
	public boolean isVersoEspansa() {
		if (verso != 0) {
			return verso < 0;
		}
		return scostamento < scostamentoMassimo / 2.0;
	}

	/** Se la pila si sta muovendo da sola o col dito */
	public boolean isInMovimento() {
		return verso != 0 || trascinata;
	}

	public boolean isTrascinata() {
		return trascinata;
	}

	/** Il dito (o il mouse) prende la pila a quell'ascissa: da qui la pila lo segue */
	public void prendi(int x) {
		trascinata = true;
		verso = 0;
		xDellaPresa = x;
		ultimoX = x;
		scostamentoAllaPresa = scostamento;
		versoDelTrascinamento = 0;
	}

	public void trascina(int x) {
		if (!trascinata) {
			return;
		}
		if (x != ultimoX) {
			versoDelTrascinamento = Integer.signum(x - ultimoX);
			ultimoX = x;
		}
		scostamento = entro(scostamentoAllaPresa + x - xDellaPresa);
	}

	/** Lasciata, va fino in fondo nel verso dell'ultimo movimento (se non si è mossa, verso l'estremità più vicina) */
	public void lascia() {
		if (!trascinata) {
			return;
		}
		trascinata = false;
		verso = versoDelTrascinamento != 0 ? versoDelTrascinamento : (scostamento >= scostamentoMassimo / 2.0 ? 1 : -1);
		fermaSeArrivata();
	}

	/** Va all'altra estremità: compattata se è espansa, espansa se è compattata */
	public void commuta() {
		trascinata = false;
		verso = isVersoEspansa() ? 1 : -1;
		fermaSeArrivata();
	}

	/** Torna espansa (se non lo è già) */
	public void espandi() {
		if (scostamento > 0 || verso > 0 || trascinata) {
			trascinata = false;
			verso = -1;
			fermaSeArrivata();
		}
	}

	/** Fa scorrere la pila che va da sola per quei secondi */
	public void avanza(double secondi) {
		if (verso == 0 || trascinata) {
			return;
		}
		scostamento = entro(scostamento + verso * secondi * scostamentoMassimo / SECONDI_DELLO_SCORRIMENTO);
		fermaSeArrivata();
	}

	private void fermaSeArrivata() {
		if (verso > 0 && scostamento >= scostamentoMassimo || verso < 0 && scostamento <= 0) {
			verso = 0;
		}
	}

	private double entro(double valore) {
		return Math.max(0, Math.min(scostamentoMassimo, valore));
	}
}
