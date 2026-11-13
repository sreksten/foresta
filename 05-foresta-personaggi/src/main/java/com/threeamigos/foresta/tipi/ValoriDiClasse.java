package com.threeamigos.foresta.tipi;

import java.util.Locale;

/**
 * I valori fissi di una classe di personaggi (il goblin, la maga...), uguali per tutti i personaggi della classe: le caratteristiche
 * (sesso, se si lascia corrompere, se è amichevole, se è un boss), i valori di partenza (salute e magia, l'esperienza che dà, quanti se
 * ne incontrano al massimo, l'intelligenza massima) e i moltiplicatori degli attributi secondari, dei danni e del recupero.
 * <p>
 * La tabella con i valori di ogni classe sta in {@link CostantiPersonaggi#valoriDi(TipoPersonaggio)}, dove si cambiano per bilanciare il
 * gioco; li legge PersonaggioBase, che li dà a chi chiede al personaggio (getSaluteBase, getMoltiplicatoreCritico...). Un moltiplicatore
 * vale 1 nella media: di più è un punto di forza, di meno un punto debole.
 * <p>
 * Si costruiscono con {@link #nuovi()} e una catena di metodi, uno per valore, chiusa da {@link Costruttore#per(TipoPersonaggio)}, che
 * controlla che non ne manchi nessuno:
 * <pre>
 * ValoriDiClasse.nuovi().corrompibile()
 *         .salute(90).magia(0, 0).esperienza(25).quanti(4).intelligenza(8)
 *         .carico(0.9).critico(1.1) ... .recuperoMagico(1.0)
 *         .per(TipoPersonaggio.GOBLIN)
 * </pre>
 *
 * @author Stefano Reksten
 */
public final class ValoriDiClasse {

	private final boolean femmina;
	private final boolean corrompibile;
	private final boolean amichevole;
	private final boolean boss;
	private final double saluteBase;
	private final double magiaBase;
	private final double livellamentoMagia;
	private final int puntiEsperienza;
	private final int quantitaMassima;
	private final double maxIntelligenza;
	private final double carico;
	private final double critico;
	private final double precisione;
	private final double velocita;
	private final double furtivita;
	private final double parata;
	private final double resistenzaMagica;
	private final double percezione;
	private final double soggezione;
	private final double furia;
	private final double coraggio;
	private final double valore;
	private final double contrattazione;
	private final double numeroBersagli;
	private final double stanchezza;
	private final double danniMagici;
	private final double danniFisici;
	private final double recuperoFisico;
	private final double recuperoMagico;

	private ValoriDiClasse(Costruttore c) {
		femmina = c.femmina;
		corrompibile = c.corrompibile;
		amichevole = c.amichevole;
		boss = c.boss;
		saluteBase = c.saluteBase;
		magiaBase = c.magiaBase;
		livellamentoMagia = c.livellamentoMagia;
		puntiEsperienza = (int) c.puntiEsperienza;
		quantitaMassima = (int) c.quantitaMassima;
		maxIntelligenza = c.maxIntelligenza;
		carico = c.carico;
		critico = c.critico;
		precisione = c.precisione;
		velocita = c.velocita;
		furtivita = c.furtivita;
		parata = c.parata;
		resistenzaMagica = c.resistenzaMagica;
		percezione = c.percezione;
		soggezione = c.soggezione;
		furia = c.furia;
		coraggio = c.coraggio;
		valore = c.valore;
		contrattazione = c.contrattazione;
		numeroBersagli = c.numeroBersagli;
		stanchezza = c.stanchezza;
		danniMagici = c.danniMagici;
		danniFisici = c.danniFisici;
		recuperoFisico = c.recuperoFisico;
		recuperoMagico = c.recuperoMagico;
	}

	/**
	 * Comincia la costruzione dei valori di una classe: maschile, non corrompibile, non amichevole, non un boss, senza contrattazione;
	 * tutti gli altri valori vanno dati.
	 */
	public static Costruttore nuovi() {
		return new Costruttore();
	}

	/**
	 * Se la classe è femminile (la Maga, l'Arpia): da questo dipendono gli articoli e i pronomi dei testi
	 */
	public boolean isFemmina() {
		return femmina;
	}

	/**
	 * Se il gruppo può tentare di corromperla
	 */
	public boolean isCorrompibile() {
		return corrompibile;
	}

	/**
	 * Se il gruppo può tentare di farci amicizia
	 */
	public boolean isAmichevole() {
		return amichevole;
	}

	/**
	 * Se è un boss: parte con salute e magia piene, ha Morte nel repertorio e non può essere mietuto
	 */
	public boolean isBoss() {
		return boss;
	}

	public double getSaluteBase() {
		return saluteBase;
	}

	public double getMagiaBase() {
		return magiaBase;
	}

	/**
	 * Quanto cresce la magia massima con il livello (per la radice quadrata di livello - 1); 0 per chi non ha magia
	 */
	public double getLivellamentoMagia() {
		return livellamentoMagia;
	}

	/**
	 * I punti esperienza che il personaggio dà a chi lo sconfigge
	 */
	public int getPuntiEsperienza() {
		return puntiEsperienza;
	}

	/**
	 * Quanti personaggi della classe si incontrano al massimo in una locazione
	 */
	public int getQuantitaMassima() {
		return quantitaMassima;
	}

	/**
	 * L'intelligenza massima della classe: il riferimento per l'astuzia delle sue tattiche (Astuzia), non l'intelligenza vera
	 */
	public double getMaxIntelligenza() {
		return maxIntelligenza;
	}

	public double getMoltiplicatoreCarico() {
		return carico;
	}

	public double getMoltiplicatoreCritico() {
		return critico;
	}

	public double getMoltiplicatorePrecisione() {
		return precisione;
	}

	public double getMoltiplicatoreVelocita() {
		return velocita;
	}

	public double getMoltiplicatoreFurtivita() {
		return furtivita;
	}

	public double getMoltiplicatoreParata() {
		return parata;
	}

	public double getMoltiplicatoreResistenzaMagica() {
		return resistenzaMagica;
	}

	public double getMoltiplicatorePercezione() {
		return percezione;
	}

	public double getMoltiplicatoreSoggezione() {
		return soggezione;
	}

	public double getMoltiplicatoreFuria() {
		return furia;
	}

	public double getMoltiplicatoreCoraggio() {
		return coraggio;
	}

	public double getMoltiplicatoreValore() {
		return valore;
	}

	/**
	 * Il moltiplicatore della CONTRATTAZIONE: 0 per chi non tratta con i mercanti (i mostri che non entrano mai nel gruppo)
	 */
	public double getMoltiplicatoreContrattazione() {
		return contrattazione;
	}

	public double getMoltiplicatoreNumeroBersagli() {
		return numeroBersagli;
	}

	public double getMoltiplicatoreStanchezza() {
		return stanchezza;
	}

	public double getMoltiplicatoreDanniMagici() {
		return danniMagici;
	}

	public double getMoltiplicatoreDanniFisici() {
		return danniFisici;
	}

	/**
	 * Il moltiplicatore del recupero della salute (a fine locazione e riposando): 0 per chi non guarisce, come i non morti
	 */
	public double getMoltiplicatoreRecuperoFisico() {
		return recuperoFisico;
	}

	public double getMoltiplicatoreRecuperoMagico() {
		return recuperoMagico;
	}

	/**
	 * Raccoglie i valori uno per uno: ogni metodo ne imposta uno e restituisce il costruttore stesso, così si scrivono in catena.
	 * Un valore che non è stato dato resta NaN ("non un numero"), e {@link #per(TipoPersonaggio)} se ne accorge.
	 */
	public static final class Costruttore {

		private boolean femmina;
		private boolean corrompibile;
		private boolean amichevole;
		private boolean boss;
		private double saluteBase = Double.NaN;
		private double magiaBase = Double.NaN;
		private double livellamentoMagia = Double.NaN;
		private double puntiEsperienza = Double.NaN;
		private double quantitaMassima = Double.NaN;
		private double maxIntelligenza = Double.NaN;
		private double carico = Double.NaN;
		private double critico = Double.NaN;
		private double precisione = Double.NaN;
		private double velocita = Double.NaN;
		private double furtivita = Double.NaN;
		private double parata = Double.NaN;
		private double resistenzaMagica = Double.NaN;
		private double percezione = Double.NaN;
		private double soggezione = Double.NaN;
		private double furia = Double.NaN;
		private double coraggio = Double.NaN;
		private double valore = Double.NaN;
		private double contrattazione;
		private double numeroBersagli = Double.NaN;
		private double stanchezza = Double.NaN;
		private double danniMagici = Double.NaN;
		private double danniFisici = Double.NaN;
		private double recuperoFisico = Double.NaN;
		private double recuperoMagico = Double.NaN;

		private Costruttore() {
		}

		/** La classe è femminile */
		public Costruttore femmina() {
			femmina = true;
			return this;
		}

		/** Il gruppo può tentare di corromperla */
		public Costruttore corrompibile() {
			corrompibile = true;
			return this;
		}

		/** Il gruppo può tentare di farci amicizia */
		public Costruttore amichevole() {
			amichevole = true;
			return this;
		}

		/** È un boss: parte con salute e magia piene */
		public Costruttore boss() {
			boss = true;
			return this;
		}

		/** La salute di base, a livello 1 */
		public Costruttore salute(double base) {
			saluteBase = base;
			return this;
		}

		/** La magia di base, a livello 1, e quanto cresce con il livello (0 e 0 per chi non ha magia) */
		public Costruttore magia(double base, double livellamento) {
			magiaBase = base;
			livellamentoMagia = livellamento;
			return this;
		}

		/** I punti esperienza che dà a chi lo sconfigge */
		public Costruttore esperienza(int punti) {
			puntiEsperienza = punti;
			return this;
		}

		/** Quanti se ne incontrano al massimo in una locazione */
		public Costruttore quanti(int massimo) {
			quantitaMassima = massimo;
			return this;
		}

		/** L'intelligenza massima della classe, per l'astuzia delle tattiche */
		public Costruttore intelligenza(double massima) {
			maxIntelligenza = massima;
			return this;
		}

		public Costruttore carico(double moltiplicatore) {
			carico = moltiplicatore;
			return this;
		}

		public Costruttore critico(double moltiplicatore) {
			critico = moltiplicatore;
			return this;
		}

		public Costruttore precisione(double moltiplicatore) {
			precisione = moltiplicatore;
			return this;
		}

		public Costruttore velocita(double moltiplicatore) {
			velocita = moltiplicatore;
			return this;
		}

		public Costruttore furtivita(double moltiplicatore) {
			furtivita = moltiplicatore;
			return this;
		}

		public Costruttore parata(double moltiplicatore) {
			parata = moltiplicatore;
			return this;
		}

		public Costruttore resistenzaMagica(double moltiplicatore) {
			resistenzaMagica = moltiplicatore;
			return this;
		}

		public Costruttore percezione(double moltiplicatore) {
			percezione = moltiplicatore;
			return this;
		}

		public Costruttore soggezione(double moltiplicatore) {
			soggezione = moltiplicatore;
			return this;
		}

		public Costruttore furia(double moltiplicatore) {
			furia = moltiplicatore;
			return this;
		}

		public Costruttore coraggio(double moltiplicatore) {
			coraggio = moltiplicatore;
			return this;
		}

		public Costruttore valore(double moltiplicatore) {
			valore = moltiplicatore;
			return this;
		}

		/** Facoltativo: chi non lo dà non tratta con i mercanti (0) */
		public Costruttore contrattazione(double moltiplicatore) {
			contrattazione = moltiplicatore;
			return this;
		}

		public Costruttore numeroBersagli(double moltiplicatore) {
			numeroBersagli = moltiplicatore;
			return this;
		}

		public Costruttore stanchezza(double moltiplicatore) {
			stanchezza = moltiplicatore;
			return this;
		}

		public Costruttore danniMagici(double moltiplicatore) {
			danniMagici = moltiplicatore;
			return this;
		}

		public Costruttore danniFisici(double moltiplicatore) {
			danniFisici = moltiplicatore;
			return this;
		}

		public Costruttore recuperoFisico(double moltiplicatore) {
			recuperoFisico = moltiplicatore;
			return this;
		}

		public Costruttore recuperoMagico(double moltiplicatore) {
			recuperoMagico = moltiplicatore;
			return this;
		}

		/**
		 * I valori della classe, se non ne manca nessuno; altrimenti un errore che dice la classe e il primo valore mancante.
		 */
		public ValoriDiClasse per(TipoPersonaggio classe) {
			controlla(classe, "salute", saluteBase);
			controlla(classe, "magia", magiaBase);
			controlla(classe, "esperienza", puntiEsperienza);
			controlla(classe, "quanti", quantitaMassima);
			controlla(classe, "intelligenza", maxIntelligenza);
			controlla(classe, "carico", carico);
			controlla(classe, "critico", critico);
			controlla(classe, "precisione", precisione);
			controlla(classe, "velocita", velocita);
			controlla(classe, "furtivita", furtivita);
			controlla(classe, "parata", parata);
			controlla(classe, "resistenzaMagica", resistenzaMagica);
			controlla(classe, "percezione", percezione);
			controlla(classe, "soggezione", soggezione);
			controlla(classe, "furia", furia);
			controlla(classe, "coraggio", coraggio);
			controlla(classe, "valore", valore);
			controlla(classe, "numeroBersagli", numeroBersagli);
			controlla(classe, "stanchezza", stanchezza);
			controlla(classe, "danniMagici", danniMagici);
			controlla(classe, "danniFisici", danniFisici);
			controlla(classe, "recuperoFisico", recuperoFisico);
			controlla(classe, "recuperoMagico", recuperoMagico);
			return new ValoriDiClasse(this);
		}

		private static void controlla(TipoPersonaggio classe, String valore, double numero) {
			if (Double.isNaN(numero)) {
				throw new IllegalStateException(String.format(Locale.ROOT, "Ai valori della classe %s manca %s", classe, valore));
			}
		}
	}
}
