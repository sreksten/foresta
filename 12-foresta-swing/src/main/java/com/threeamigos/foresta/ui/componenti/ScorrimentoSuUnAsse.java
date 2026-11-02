package com.threeamigos.foresta.ui.componenti;

import java.util.function.LongSupplier;

/**
 * Lo scorrimento di un contenuto lungo un solo asse, in pixel, da 0 a un massimo: la parte di un componente scorrevole
 * che non dipende da cosa si disegna né da come arrivano i gesti (mouse o dito). Non conosce AWT: chi la usa le dice
 * dove cade il puntatore sull'asse e legge dove sta lo scorrimento.
 * <p>
 * Tre movimenti:
 * <ul>
 * <li>il trascinamento: premuto il puntatore ({@link #premi}), se si sposta di {@link #SOGLIA_TRASCINAMENTO} pixel o
 * più il gesto è un trascinamento e il contenuto segue il puntatore ({@link #trascina}); sotto la soglia è un tocco,
 * che decide chi usa questa classe al rilascio ({@link #isTrascinando} prima di {@link #rilascia});</li>
 * <li>l'inerzia: lasciando il puntatore con una certa velocità il contenuto prosegue e rallenta, e si ferma quando
 * arriva a un capo;</li>
 * <li>l'elastico: trascinando oltre un capo il contenuto segue ancora il puntatore, ma con una resistenza che cresce
 * fino a {@link #MASSIMO_ELASTICO} pixel; al rilascio torna al capo con la stessa animazione della destinazione. Perciò
 * {@link #getScorrimento} può uscire per un momento da 0..massimo, e chi disegna deve accettarlo;</li>
 * <li>la destinazione ({@link #scorriFinoA}): una breve animazione verso una posizione, per le frecce e la rotella.</li>
 * </ul>
 * Chi la usa deve chiamare {@link #aggiorna} a ogni fotogramma e ridisegnare finché {@link #isInMovimento}.
 * Il tempo lo dà l'orologio passato al costruttore (in nanosecondi), così si può provare senza aspettare.
 */
public final class ScorrimentoSuUnAsse {

	/**
	 * Di quanti pixel va spostato il puntatore, premuto, perché il gesto sia un trascinamento e non un tocco
	 */
	public static final int SOGLIA_TRASCINAMENTO = 8;
	/**
	 * Di quanti pixel, al massimo, il contenuto si può tirare oltre un capo trascinando
	 */
	public static final double MASSIMO_ELASTICO = 48.0d;
	/**
	 * Lasciando il puntatore a meno di questa velocità (pixel al secondo) il contenuto si ferma; a più, prosegue per inerzia
	 */
	public static final double VELOCITA_MINIMA_INERZIA = 200.0d;
	/**
	 * Sotto questa velocità l'inerzia è finita
	 */
	private static final double VELOCITA_DI_ARRESTO = 20.0d;
	/**
	 * Il tempo, in secondi, in cui la velocità dell'inerzia si riduce di un fattore e (circa 37%)
	 */
	private static final double COSTANTE_DI_ATTRITO = 0.35d;
	/**
	 * Come COSTANTE_DI_ATTRITO, per l'animazione verso una destinazione
	 */
	private static final double COSTANTE_DELLA_DESTINAZIONE = 0.08d;
	/**
	 * Se il puntatore è fermo da più di così quando lo si lascia, non c'è inerzia
	 */
	private static final long ETA_MASSIMA_DEL_CAMPIONE_NANOS = 100_000_000L;
	/**
	 * Un fotogramma in ritardo (il sistema era sospeso) non fa scattare lo scorrimento: il tempo trascorso si limita a questo
	 */
	private static final double PASSO_MASSIMO_SECONDI = 0.05d;

	private final LongSupplier orologio;

	/**
	 * Di quanti pixel il contenuto è spostato dall'inizio (0 lo mostra dall'inizio), fino a massimo
	 */
	private double scorrimento = 0.0d;
	private double massimo = 0.0d;
	/**
	 * Se il contenuto scorre davvero (non ci sta tutto): altrimenti inerzia e destinazione non fanno niente
	 */
	private boolean abilitato = false;

	// Il gesto in corso: da dove è iniziato, se ha superato la soglia e la velocità con cui il puntatore si muove
	private boolean premuto = false;
	private boolean puoTrascinare = false;
	private boolean trascinando = false;
	// Se l'ultimo gesto (dall'ultima pressione) è stato un trascinamento: il click che arriva dopo il rilascio non vale
	private boolean gestoTrascinato = false;
	private int premutoAsse;
	private double scorrimentoAllaPressione;
	private int ultimoAsse;
	private long istanteUltimoCampione;
	private double velocitaStimata;

	// Il movimento che prosegue da solo: l'inerzia, oppure lo scorrimento verso una destinazione
	private boolean inerzia = false;
	private double velocita;
	private boolean haDestinazione = false;
	private double destinazione;
	private long istanteAggiornamento;
	// Letto dal thread di animazione (vedi isInMovimento)
	private volatile boolean inMovimento = false;

	public ScorrimentoSuUnAsse(LongSupplier orologio) {
		this.orologio = orologio;
	}

	public double getScorrimento() {
		return scorrimento;
	}

	public double getMassimo() {
		return massimo;
	}

	/**
	 * Dove sta andando il contenuto: la destinazione se c'è un'animazione in corso, altrimenti dov'è adesso
	 */
	public double getPosizioneDiArrivo() {
		return haDestinazione ? destinazione : scorrimento;
	}

	/**
	 * Se si sta trascinando (il puntatore ha superato la soglia e non è stato ancora lasciato)
	 */
	public boolean isTrascinando() {
		return trascinando;
	}

	/**
	 * Se l'ultimo gesto, dalla pressione in poi e anche dopo il rilascio, è stato un trascinamento: chi decide cosa fa
	 * un click lo usa per non contare come click il rilascio di un trascinamento.
	 */
	public boolean isGestoTrascinato() {
		return gestoTrascinato;
	}

	/**
	 * Se il puntatore è premuto da un gesto valido: dalla pressione al rilascio, a meno che si fermi tutto
	 */
	public boolean isPremuto() {
		return premuto;
	}

	/**
	 * Se il contenuto si muove da solo (inerzia o animazione verso una destinazione), anche a puntatore fermo: allora
	 * chi disegna deve farlo a ogni fotogramma. Si può leggere da un altro thread.
	 */
	public boolean isInMovimento() {
		return inMovimento;
	}

	/**
	 * Dice se il contenuto scorre davvero (non ci sta tutto) e fin dove: il massimo, in pixel, è il limite
	 * dello scorrimento. Tiene dentro i limiti lo scorrimento e la destinazione.
	 */
	public void imposta(boolean abilitato, double massimo) {
		this.abilitato = abilitato;
		this.massimo = massimo;
		// Mentre si tira oltre un capo, o si torna indietro, lo scorrimento esce dai limiti di proposito
		if (!trascinando && !haDestinazione) {
			scorrimento = limita(scorrimento);
		}
		if (haDestinazione) {
			destinazione = limita(destinazione);
		}
	}

	/**
	 * Riporta il contenuto all'inizio e ferma tutto
	 */
	public void reimposta() {
		scorrimento = 0.0d;
		ferma();
	}

	/**
	 * Ferma qualunque movimento in corso, trascinamento compreso
	 */
	public void ferma() {
		trascinando = false;
		premuto = false;
		inerzia = false;
		velocita = 0.0d;
		haDestinazione = false;
		inMovimento = false;
	}

	/**
	 * Fa avanzare l'inerzia o l'animazione fino all'istante dell'orologio: va chiamato a ogni disegno, prima di
	 * disegnare. Durante un trascinamento lo scorrimento lo decide il puntatore.
	 *
	 * @return se lo scorrimento è stato fatto avanzare (e quindi chi disegna deve ricalcolare)
	 */
	public boolean aggiorna() {
		long ora = orologio.getAsLong();
		double passo = Math.min((ora - istanteAggiornamento) / 1_000_000_000.0d, PASSO_MASSIMO_SECONDI);
		istanteAggiornamento = ora;
		if (trascinando || !abilitato || passo <= 0.0d) {
			return false;
		}
		boolean avanzato = false;
		if (inerzia) {
			scorrimento += velocita * passo;
			velocita *= Math.exp(-passo / COSTANTE_DI_ATTRITO);
			if (scorrimento < 0.0d || scorrimento > massimo) {
				// Arrivata a un capo l'inerzia finisce: niente rimbalzo
				scorrimento = limita(scorrimento);
				inerzia = false;
				velocita = 0.0d;
			} else if (Math.abs(velocita) < VELOCITA_DI_ARRESTO) {
				inerzia = false;
				velocita = 0.0d;
			}
			avanzato = true;
		} else if (haDestinazione) {
			double distanza = destinazione - scorrimento;
			if (Math.abs(distanza) < 0.5d) {
				scorrimento = destinazione;
				haDestinazione = false;
			} else {
				scorrimento += distanza * (1.0d - Math.exp(-passo / COSTANTE_DELLA_DESTINAZIONE));
			}
			avanzato = true;
		}
		inMovimento = inerzia || haDestinazione;
		return avanzato;
	}

	/**
	 * Il puntatore si preme: ferma l'inerzia e l'animazione e si ricorda da dove parte il gesto.
	 *
	 * @param asse         dove cade il puntatore sull'asse
	 * @param puoTrascinare se da quel punto si può trascinare (le frecce, per esempio, non si trascinano)
	 * @return se il contenuto si stava muovendo da solo: chi lo usa non deve contare il gesto come un tocco, che
	 * serviva solo a fermarlo
	 */
	public boolean premi(int asse, boolean puoTrascinare) {
		boolean eraInMovimento = inerzia || haDestinazione;
		inerzia = false;
		velocita = 0.0d;
		haDestinazione = false;
		inMovimento = false;
		premuto = true;
		trascinando = false;
		gestoTrascinato = false;
		this.puoTrascinare = abilitato && massimo > 0.0d && puoTrascinare;
		premutoAsse = asse;
		scorrimentoAllaPressione = scorrimento;
		ultimoAsse = asse;
		istanteUltimoCampione = orologio.getAsLong();
		velocitaStimata = 0.0d;
		return eraInMovimento;
	}

	/**
	 * Il puntatore si sposta, premuto: oltre la soglia il contenuto comincia a seguirlo.
	 */
	public void trascina(int asse) {
		if (!premuto) {
			return;
		}
		if (!trascinando) {
			if (!puoTrascinare || Math.abs(asse - premutoAsse) < SOGLIA_TRASCINAMENTO) {
				return;
			}
			// Da qui il contenuto segue il puntatore: si parte da dov'è adesso, senza lo scatto della soglia
			trascinando = true;
			gestoTrascinato = true;
			premutoAsse = asse;
			scorrimentoAllaPressione = scorrimento;
			ultimoAsse = asse;
			istanteUltimoCampione = orologio.getAsLong();
			velocitaStimata = 0.0d;
			return;
		}
		long ora = orologio.getAsLong();
		double secondi = (ora - istanteUltimoCampione) / 1_000_000_000.0d;
		if (secondi > 0.001d) {
			// Il puntatore va verso l'inizio quando il contenuto scorre verso la fine
			double istantanea = -(asse - ultimoAsse) / secondi;
			velocitaStimata = 0.5d * velocitaStimata + 0.5d * istantanea;
			ultimoAsse = asse;
			istanteUltimoCampione = ora;
		}
		scorrimento = conElastico(scorrimentoAllaPressione - (asse - premutoAsse));
	}

	/**
	 * Il puntatore si lascia: finisce il gesto e, se era un trascinamento e il puntatore si muoveva abbastanza in
	 * fretta, il contenuto prosegue per inerzia. Per sapere se era un trascinamento o un tocco si legge
	 * {@link #isTrascinando} prima.
	 */
	public void rilascia() {
		boolean eraTrascinamento = trascinando;
		trascinando = false;
		premuto = false;
		if (eraTrascinamento && (scorrimento < 0.0d || scorrimento > massimo)) {
			// Tirato oltre un capo: ci torna, senza inerzia
			inerzia = false;
			velocita = 0.0d;
			destinazione = limita(scorrimento);
			haDestinazione = true;
			istanteAggiornamento = orologio.getAsLong();
			inMovimento = true;
		} else if (eraTrascinamento) {
			boolean fermo = orologio.getAsLong() - istanteUltimoCampione > ETA_MASSIMA_DEL_CAMPIONE_NANOS;
			if (!fermo && Math.abs(velocitaStimata) >= VELOCITA_MINIMA_INERZIA) {
				velocita = velocitaStimata;
				inerzia = true;
				istanteAggiornamento = orologio.getAsLong();
				inMovimento = true;
			}
		}
	}

	/**
	 * Sposta subito il contenuto di quei pixel (positivi verso la fine), dentro i limiti, fermando inerzia e
	 * animazione: per la rotella, che chi la consegna distribuisce già in più passi.
	 */
	public void scorriDi(double pixel) {
		inerzia = false;
		velocita = 0.0d;
		haDestinazione = false;
		inMovimento = false;
		scorrimento = limita(scorrimento + pixel);
	}

	/**
	 * Fa scorrere il contenuto fino a questa posizione (in pixel, dentro i limiti) con una breve animazione.
	 *
	 * @param margineAiCapi vicino a un capo, a meno di così, si arriva fino al capo: niente avanzi
	 */
	public void scorriFinoA(double posizione, double margineAiCapi) {
		double limitata = limita(posizione);
		if (limitata < margineAiCapi) {
			limitata = 0.0d;
		} else if (limitata > massimo - margineAiCapi) {
			limitata = massimo;
		}
		if (Math.abs(limitata - scorrimento) < 0.5d) {
			return;
		}
		inerzia = false;
		velocita = 0.0d;
		destinazione = limitata;
		haDestinazione = true;
		istanteAggiornamento = orologio.getAsLong();
		inMovimento = true;
	}

	/**
	 * Dentro i limiti la posizione è quella chiesta; oltre, il contenuto la segue con una resistenza che cresce, senza
	 * superare MASSIMO_ELASTICO pixel oltre il capo
	 */
	private double conElastico(double posizione) {
		if (posizione < 0.0d) {
			return -MASSIMO_ELASTICO * (1.0d - Math.exp(posizione / MASSIMO_ELASTICO));
		}
		if (posizione > massimo) {
			return massimo + MASSIMO_ELASTICO * (1.0d - Math.exp((massimo - posizione) / MASSIMO_ELASTICO));
		}
		return posizione;
	}

	private double limita(double posizione) {
		return Math.max(0.0d, Math.min(posizione, massimo));
	}
}
