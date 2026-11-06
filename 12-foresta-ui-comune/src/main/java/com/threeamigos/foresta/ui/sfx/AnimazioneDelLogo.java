package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * L'animazione del logo, senza disegno: traccia il contorno di un logo con uno o piu' punti luminosi ("corridori") che lo rivelano man mano, poi lo
 * fa apparire per intero con una dissolvenza. Si imposta con il {@link Costruttore}
 * ({@code AnimazioneDelLogo.costruttore(logo)...costruisci()}) e si fa avanzare di un fotogramma alla volta con {@link #avanza()}, ogni
 * {@link #INTERVALLO_FOTOGRAMMA_MS} millisecondi; a ogni fotogramma chi disegna chiede la fase, le opacita' e dove stanno i corridori
 * ({@link #getCorridori()}). Il disegno e' del toolkit: <code>TracciatoreLogo</code> in Swing, <code>LogoInizialeGdx</code> in libGDX.
 * <p>
 * Da riga di comando lo si prova con TracciatoreLogoDaRigaDiComando, tra i sorgenti dei test di Swing.
 *
 * ============================================================================
 *  PRINCIPIO DI FUNZIONAMENTO
 * ============================================================================
 *
 * L'effetto si ottiene in una pipeline di fasi, eseguite in ordine:
 *
 * 1) MASCHERA (quali pixel sono "logo" e quali sono "sfondo da scartare")
 *    -----------------------------------------------------------------------
 *    Non decidiamo staticamente "alpha basso = sfondo": deleghiamo la
 *    decisione a un {@link PredicatoSfondo}, una semplice funzione
 *    (x, y, colore-ARGB) &rarr; booleano. Questo permette di riusare lo stesso
 *    algoritmo sia su PNG con canale alpha (predicato basato sulla
 *    trasparenza, vedi {@link PredicatoSfondo#perAlpha}) sia su immagini
 *    opache con uno sfondo a tinta unita (scontorno per colore, vedi
 *    {@link PredicatoSfondo#perColore}), sia su qualunque altro criterio
 *    che si voglia scrivere come lambda. Il risultato e' una matrice
 *    booleana maschera[y][x] = true se il pixel appartiene al logo (primo piano),
 *    false se va scartato (sfondo).
 *
 * 1bis) AREA DI SCANSIONE (opzionale, {@link Costruttore#regione})
 *    -----------------------------------------------------------------------
 *    Se il logo e' solo un dettaglio dentro un disegno molto piu' grande
 *    (es. la scritta "3AM" nera dentro un'illustrazione con sfondo marrone
 *    chiaro), si puo' limitare la ricerca a un rettangolo con
 *    {@link Costruttore#regione}: maschera, tracciamento e buchi vengono
 *    calcolati SOLO su quel rettangolo, con origine locale (0,0), cosi' il resto del disegno (che magari contiene altri elementi con lo
 *    stesso colore/alpha) viene completamente ignorato. Dopo il
 *    ricampionamento (punto 3), a ogni punto dei contorni trovati si
 *    risomma l'offset (x, y) del rettangolo, riportandolo nel sistema di
 *    coordinate dell'immagine intera. Il rendering finale (punto 7) disegna
 *    sempre l'immagine ORIGINALE per intero, sfondo compreso: la regione
 *    serve solo a decidere DOVE cercare il logo, mai cosa disegnare.
 *
 * 1ter) IMMAGINE DA MOSTRARE (opzionale, {@link Costruttore#immagineDaMostrare})
 *    -----------------------------------------------------------------------
 *    Se il logo da rivelare è un disegno ricco (la scritta nera dentro una pergamena con uccelli e facce) e la scritta
 *    sta in un'altra immagine, a sé, della stessa dimensione e con la scritta ESATTAMENTE nello stesso posto, i
 *    contorni si cercano su quella (il {@code logo} del costruttore, con la sua trasparenza) e la resa, torcia compresa,
 *    usa l'altra. Così non serve nessuna regione e nessun pixel scuro del resto del disegno finisce fra i contorni.
 *
 * 2) TRACCIAMENTO DEL CONTORNO ESTERNO (tracciamento del bordo di Moore, "Moore-Neighbor")
 *    -----------------------------------------------------------------------
 *    Un logo puo' contenere piu' forme non collegate tra loro (es. lettere
 *    separate come "3", "A", "M"): la maschera viene quindi scandita riga
 *    per riga cercando OGNI pixel di primo piano non ancora "consumato". Il
 *    primo trovato di ciascuna zona e' garantito essere un pixel di bordo
 *    (i pixel sopra di lui e a sinistra nella sua riga sono sfondo). Da
 *    li' si "cammina" lungo il perimetro: ad ogni passo si esaminano gli 8
 *    vicini in senso orario partendo dalla direzione opposta a quella di
 *    arrivo (il "ritorno"), e si salta al primo vicino di primo piano trovato.
 *    Si ripete finche' non si torna al pixel di partenza: il risultato e'
 *    una sequenza ORDINATA di pixel di bordo che percorre l'intero contorno
 *    di quella forma una sola volta. Dopo aver tracciato una forma, tutti i
 *    suoi pixel (bordo e interno) vengono marcati come "consumati" con un
 *    riempimento, cosi' la scansione prosegue e trova la forma successiva.
 *
 * 2bis) BUCHI INTERNI (es. il triangolo dentro la "A")
 *    -----------------------------------------------------------------------
 *    Una lettera come la "A" racchiude un'area di sfondo che NON tocca mai
 *    il bordo dell'immagine: e' un "buco". Per tracciarlo si applica un
 *    trucco: si costruisce una seconda maschera booleana contenente SOLO i
 *    pixel di sfondo che appartengono a una componente connessa (8-conn.)
 *    che non tocca mai il bordo dell'immagine (vedi
 *    {@link #estraiBuchiRacchiusi}), e si passa questa maschera allo STESSO
 *    tracciatore usato al punto 2 — dal punto di vista dell'algoritmo, un
 *    buco non e' concettualmente diverso da una qualsiasi altra "forma": e'
 *    semplicemente una regione connessa di cui vogliamo il contorno. Il
 *    risultato e' un contorno aggiuntivo che corre lungo l'interno della
 *    lettera. Nota: usando la stessa connettivita' (8) sia per il logo sia
 *    per lo sfondo puo', in teoria, dare ambiguita' su un pixel di
 *    diagonale singola (paradosso della topologia digitale); per font e
 *    loghi normali, con tratti di almeno 2px, non e' un problema pratico.
 *
 * 3) RICAMPIONAMENTO A DISTANZA COSTANTE
 *    -----------------------------------------------------------------------
 *    Ogni contorno pixel-per-pixel (esterno o buco) viene ridotto a un
 *    numero fisso di punti equidistanti lungo il perimetro (parametrizzazione
 *    per lunghezza d'arco), cosi' la velocita' del punto luminoso e'
 *    uniforme indipendentemente dalla risoluzione dell'immagine.
 *
 * 4) VERSO DI PERCORRENZA (orario / antiorario), PER CORRIDORE
 *    -----------------------------------------------------------------------
 *    L'algoritmo di tracciamento produce un verso "naturale" che dipende
 *    dalla forma (non e' prevedibile a priori, e vale sia per i contorni
 *    esterni sia per i buchi). Per sapere se quel verso naturale sia,
 *    visivamente, orario o antiorario, si calcola l'area con segno del
 *    poligono tracciato (formula dell'area di Gauss, "shoelace") sulle coordinate immagine
 *    (asse Y verso il basso): un'area con segno positivo corrisponde a un
 *    percorso orario sullo schermo, una negativa a un percorso antiorario.
 *    Da qui si costruiscono due liste di punti per ciascuna forma: quella
 *    naturale e la sua inversa (stesso contorno, verso opposto), così ogni
 *    singolo corridore puo' scegliere il proprio verso (vedi
 *    {@link ContornoForma}, {@link Costruttore#corridore} e
 *    {@link Costruttore#versi}, che accetta un valore per corridore).
 *
 * 5) PUNTI DI PARTENZA / ARRIVO E "CORRIDORI" MULTIPLI
 *    -----------------------------------------------------------------------
 *    Un {@link SpecificaCorridore} descrive un singolo punto luminoso indipendente:
 *    un punto di partenza (coordinate pixel), un punto di arrivo opzionale
 *    (se assente, il corridore fa un giro completo tornando al punto di
 *    partenza), un verso (vedi punto 4) e un ritardo (vedi punto 6bis). Il
 *    punto di partenza viene "agganciato" prima alla forma (contorno
 *    esterno o buco) il cui contorno ha il punto piu' vicino, poi, dentro
 *    quella forma, al punto del contorno piu' vicino. Se non viene indicato
 *    nessun corridore esplicito ({@link Costruttore#corridore}), viene creato automaticamente un corridore a
 *    giro completo per OGNI forma rilevata (contorni esterni e buchi
 *    inclusi). Con {@link Costruttore#ambiVersi} i corridori automatici sono invece due per forma, uno
 *    orario e uno antiorario, che partono dallo stesso punto e si incontrano a meta' del contorno: ognuno
 *    fa mezzo giro, e quando si toccano il giro della forma e' completo.
 *
 * 6) SINCRONIZZAZIONE DI PIU' CORRIDORI (quando ce n'e' piu' di uno)
 *    -----------------------------------------------------------------------
 *    Con più corridori di lunghezza diversa (lettere/buchi diversi, o archi
 *    parziali con un arrivo) c'è un'unica domanda che conta: "quando si
 *    considera completato il giro nel suo insieme?". La risposta è sempre
 *    la stessa in questo programma — SOLO quando OGNI corridore ha raggiunto
 *    la fine del proprio percorso (vedi {@link #avanza}, fase TRACCIAMENTO: un
 *    corridore che finisce prima si ferma lì e aspetta gli altri). Quello che
 *    cambia con {@link Costruttore#velocitaAllineata} è SOLO la velocita' di ciascun
 *    corridore:
 *      - predefinito (indipendente): tutti i corridori avanzano alla stessa
 *        velocità "di riferimento", quindi un percorso più corto finisce
 *        prima e resta fermo ad aspettare gli altri;
 *      - matched: la velocità di ogni corridore è ricalcolata in base alla
 *        lunghezza del proprio percorso, così tutti raggiungono la fine
 *        esattamente nello stesso fotogramma.
 *
 * 6bis) PARTENZA POSTICIPATA (ritardo)
 *    -----------------------------------------------------------------------
 *    Ogni {@link SpecificaCorridore} può avere un ritardo iniziale, in fotogrammi o
 *    in secondi (vedi {@link Costruttore#ritardi} e {@link Costruttore#corridore}). Finché il ritardo non è scaduto il
 *    corridore è semplicemente invisibile: {@link #avanza} lo tiene fermo a
 *    inizio percorso e ne decrementa il contatore invece di farlo avanzare,
 *    e chi disegna saltano del
 *    tutto i corridori non ancora partiti (niente torcia, niente scia). Questo
 *    si somma naturalmente alla regola del punto 6: il gruppo non è
 *    "completato" finché anche l'ultimo corridore ritardato non ha concluso
 *    il proprio percorso. Il ritardo residuo viene ripristinato al valore
 *    iniziale ad ogni nuovo ciclo (fase PAUSA &rarr; TRACCIAMENTO), così la partenza
 *    sfalsata si ripete identica ad ogni giro.
 *
 * 7) RESA GRAFICA: TORCIA + DISSOLVENZA FINALE
 *    -----------------------------------------------------------------------
 *    L'intera animazione e' una piccola macchina a stati ({@link Fase}):
 *      TRACCIAMENTO    &rarr; il logo è nascosto; ogni corridore porta con se una
 *                    "torcia" (un cerchio con gradiente radiale morbido)
 *                    che rivela il logo SOLO nel proprio raggio d'azione,
 *                    ricreata da zero ad ogni fotogramma (non si accumula:
 *                    appena la luce si allontana da un punto, quel punto
 *                    torna nascosto). Il modello dice solo dove stanno i
 *                    corridori e quanto e' grande la torcia: chi disegna moltiplica
 *                    l'alpha del logo per quello della torcia pixel per pixel (in Swing
 *                    con {@code AlphaComposite.DstIn}, in libGDX con {@link TorciaDelLogo}).
 *                    La scia bianca e il punto luminoso restano
 *                    sempre disegnati sopra, indipendentemente dal logo.
 *      DISSOLVENZA  &rarr; una volta che TUTTI i corridori hanno completato (vedi
 *                    punto 6), il logo intero viene dissolto in trasparenza
 *                    da invisibile a completamente visibile in
 *                    {@code fotogrammiDissolvenza} fotogrammi (un'opacita' globale,
 *                    {@link #getOpacitaLogo()}, non più localizzata).
 *      PAUSA        &rarr; il logo resta visibile a piena opacità per una pausa.
 *      SCOMPARSA    &rarr; solo se richiesta ({@link Costruttore#fotogrammiScomparsa}): il logo
 *                    svanisce in trasparenza, all'inverso della dissolvenza.
 *    Con {@link Costruttore#sfumaScie} le scie svaniscono durante la dissolvenza, mentre
 *    il logo compare, e le teste luminose non si disegnano piu' dalla fine del
 *    tracciamento: in pausa e in scomparsa si vede il logo da solo.
 *      Poi, se il Costruttore ha chiesto di ripetere ({@link Costruttore#ripeti}), si
 *      torna a TRACCIAMENTO azzerando i corridori; altrimenti l'effetto resta
 *      fermo sull'ultimo fotogramma (logo acceso in PAUSA, o spento dopo la
 *      SCOMPARSA) e {@link #isFinito()} diventa vero.
 *    La modalità "torcia" è quella predefinita; con {@code rivelaConTorcia(false)}
 *    si va al comportamento più semplice (logo sempre visibile).
 *
 */
public final class AnimazioneDelLogo {

	// ---- parametri regolabili: i valori predefiniti del Costruttore ----------------------------------------------------------------
	public static final int SOGLIA_ALPHA_PREDEFINITA = 128;              // soglia alpha predefinita (0-255)
	public static final int DURATA_GIRO_MS_PREDEFINITA = 4000;           // durata di riferimento di un giro completo
	private static final int PUNTI_RICAMPIONAMENTO = 900;         // punti equidistanti in cui viene ricampionato ogni contorno
	/** L'intervallo tra due fotogrammi a cui l'effetto e' pensato: 16ms ~= 60 fotogrammi al secondo. */
	public static final int INTERVALLO_FOTOGRAMMA_MS = 16;
	public static final int FOTOGRAMMI_PAUSA_PREDEFINITI = 45;           // quanti fotogrammi restare fermi in fase PAUSA (logo completo acceso)
	public static final int FOTOGRAMMI_DISSOLVENZA_PREDEFINITI = 40;     // durata predefinita della dissolvenza finale, in fotogrammi

	// =========================================================================
	//  Tipi
	// =========================================================================

	/**
	 * Decide se il pixel (x, y), di colore argb, va considerato sfondo (quindi escluso dal logo e dal tracciamento del contorno).
	 */
	@FunctionalInterface
	public interface PredicatoSfondo {
		boolean isSfondo(int x, int y, int argb);

		/** Scarta i pixel il cui canale alpha e' minore o uguale alla soglia. */
		static PredicatoSfondo perAlpha(int soglia) {
			// >>> 24 sposta l'alpha nei bit bassi senza segno; & 0xFF isola quel byte
			return (x, y, argb) -> ((argb >>> 24) & 0xFF) <= soglia;
		}

		/**
		 * Scarta i pixel il cui colore RGB e' vicino a {@code sfondo} (0xRRGGBB) entro {@code tolleranza} per canale (utile per immagini opache con
		 * uno sfondo a tinta unita ben definito). Il canale alpha viene ignorato.
		 */
		static PredicatoSfondo perColore(int sfondo, int tolleranza) {
			int rossoSfondo = (sfondo >> 16) & 0xFF;
			int verdeSfondo = (sfondo >> 8) & 0xFF;
			int bluSfondo = sfondo & 0xFF;
			return (x, y, argb) -> Math.abs(((argb >> 16) & 0xFF) - rossoSfondo) <= tolleranza
					&& Math.abs(((argb >> 8) & 0xFF) - verdeSfondo) <= tolleranza
					&& Math.abs((argb & 0xFF) - bluSfondo) <= tolleranza;
		}
	}

	/** Il verso in cui un punto luminoso percorre il contorno del logo. */
	public enum Verso {ORARIO, ANTIORARIO}

	/** Le fasi dell'animazione, nell'ordine in cui si susseguono */
	public enum Fase {TRACCIAMENTO, DISSOLVENZA, PAUSA, SCOMPARSA}

	/** Un punto nelle coordinate dell'immagine del logo (non per forza intere) */
	public static final class Posizione {
		public final double x;
		public final double y;

		public Posizione(double x, double y) {
			this.x = x;
			this.y = y;
		}

		double distanzaQuadrata(Posizione altra) {
			double dx = x - altra.x;
			double dy = y - altra.y;
			return dx * dx + dy * dy;
		}
	}

	/** Descrive un singolo punto luminoso indipendente: partenza, arrivo (opzionale), verso e ritardo. */
	private static final class SpecificaCorridore {
		final Posizione partenza;
		final Posizione arrivo; // null => giro completo
		final Verso verso;
		final int fotogrammiRitardo;

		SpecificaCorridore(Posizione partenza, Posizione arrivo, Verso verso, int fotogrammiRitardo) {
			this.partenza = partenza;
			this.arrivo = arrivo;
			this.verso = verso;
			this.fotogrammiRitardo = fotogrammiRitardo;
		}
	}

	/** Un punto luminoso animato lungo il proprio percorso */
	public static final class Corridore {
		private final List<Posizione> percorso;      // percorso concreto, gia' orientato e tagliato
		private final double puntiPerFotogramma;     // «velocita'»: di quanti punti del percorso avanzare a ogni fotogramma
		private final int fotogrammiRitardoIniziale; // per poterlo ripristinare a ogni ciclo
		private int fotogrammiRitardo;               // conto alla rovescia: finche' > 0 il corridore e' fermo e invisibile
		private double avanzamento = 0;              // posizione lungo il percorso, come indice frazionario

		private Corridore(List<Posizione> percorso, double puntiPerFotogramma, int fotogrammiRitardo) {
			this.percorso = percorso;
			this.puntiPerFotogramma = puntiPerFotogramma;
			this.fotogrammiRitardoIniziale = fotogrammiRitardo;
			this.fotogrammiRitardo = fotogrammiRitardo;
		}

		/** Vero se il ritardo iniziale e' scaduto e il corridore e' quindi «in pista» (altrimenti non si disegna). */
		public boolean isPartito() {
			return fotogrammiRitardo <= 0;
		}

		/** L'indice del punto in cui sta adesso (la scia va dal punto 0 a questo) */
		public int getIndice() {
			return (int) Math.min(avanzamento, percorso.size() - 1);
		}

		/** Il percorso concreto, dal punto di partenza a quello di arrivo */
		public List<Posizione> getPercorso() {
			return Collections.unmodifiableList(percorso);
		}

		/** Dove sta adesso */
		public Posizione getPosizione() {
			return percorso.get(getIndice());
		}

		private void riparti() {
			avanzamento = 0;
			fotogrammiRitardo = fotogrammiRitardoIniziale;
		}
	}

	// =========================================================================
	//  L'effetto
	// =========================================================================

	// L'immagine che si disegna: quella da cui si traccia, o l'altra che il costruttore ha chiesto di mostrare al suo posto
	private final ImmagineDiPixel logo;
	private final List<Corridore> corridori;
	private final int fotogrammiPerGiro;
	private final int fotogrammiDissolvenza;
	private final int fotogrammiPausa;
	private final int fotogrammiScomparsa;
	private final boolean sfumaScie;
	private final boolean rivelaConTorcia;
	private final int raggioTorcia;
	private final boolean ripeti;
	private final List<String> diagnostica;

	private Fase fase = Fase.TRACCIAMENTO;
	private int fotogrammaDissolvenza = 0; // quanti fotogrammi sono trascorsi dall'inizio della fase DISSOLVENZA
	private int fotogrammaPausa = 0;       // ...della fase PAUSA
	private int fotogrammaScomparsa = 0;   // ...della fase SCOMPARSA
	private boolean finito = false;        // vero quando, senza ripetizione, l'ultima fase del ciclo e' terminata

	private AnimazioneDelLogo(Costruttore c, List<Corridore> corridori, int fotogrammiPerGiro, int raggioTorcia, List<String> diagnostica) {
		this.logo = c.daMostrare != null ? c.daMostrare : c.logo;
		this.corridori = corridori;
		this.fotogrammiPerGiro = fotogrammiPerGiro;
		this.fotogrammiDissolvenza = c.fotogrammiDissolvenza;
		this.fotogrammiPausa = c.fotogrammiPausa;
		this.fotogrammiScomparsa = c.fotogrammiScomparsa;
		this.sfumaScie = c.sfumaScie;
		this.rivelaConTorcia = c.rivelaConTorcia;
		this.raggioTorcia = raggioTorcia;
		this.ripeti = c.ripeti;
		this.diagnostica = Collections.unmodifiableList(diagnostica);
	}

	/**
	 * Comincia a impostare l'effetto per quel logo. Il logo viene sempre disegnato per intero; dove cercarne il contorno lo decidono il criterio di
	 * sfondo e l'eventuale regione.
	 */
	public static Costruttore costruttore(ImmagineDiPixel logo) {
		return new Costruttore(logo);
	}

	/** L'immagine da disegnare: il logo intero, nella sua dimensione */
	public ImmagineDiPixel getLogo() {
		return logo;
	}

	public int getLarghezza() {
		return logo.getLarghezza();
	}

	public int getAltezza() {
		return logo.getAltezza();
	}

	/** I punti luminosi, per disegnare scie, teste e torcia (quelli che {@link Corridore#isPartito() non sono ancora partiti} non si disegnano) */
	public List<Corridore> getCorridori() {
		return Collections.unmodifiableList(corridori);
	}

	public Fase getFase() {
		return fase;
	}

	/** Il nome della fase corrente (TRACCIAMENTO, DISSOLVENZA, PAUSA, SCOMPARSA), per la diagnostica. */
	public String getNomeFase() {
		return fase.name();
	}

	/** Il raggio della torcia, in pixel */
	public int getRaggioTorcia() {
		return raggioTorcia;
	}

	/** Vero quando il logo si vede solo sotto la torcia dei corridori (in tracciamento, con la torcia accesa), invece che intero con la sua opacita' */
	public boolean isTorciaAttiva() {
		return rivelaConTorcia && fase == Fase.TRACCIAMENTO;
	}

	/** Vero se si disegnano le teste luminose dei corridori: con le scie sfumate spariscono appena finisce il tracciamento */
	public boolean isConTeste() {
		return !sfumaScie || fase == Fase.TRACCIAMENTO;
	}

	/** Vero quando, senza ripetizione, tracciamento, dissolvenza, pausa ed eventuale scomparsa sono terminati. */
	public boolean isFinito() {
		return finito;
	}

	/** Cosa ha trovato e deciso il Costruttore (forme, corridori, avvertimenti): utile per calibrare i parametri. */
	public List<String> getDiagnostica() {
		return diagnostica;
	}

	/** Riporta l'effetto all'inizio, come appena costruito. */
	public void ricomincia() {
		for (Corridore c : corridori) {
			c.riparti();
		}
		fase = Fase.TRACCIAMENTO;
		fotogrammaDissolvenza = 0;
		fotogrammaPausa = 0;
		fotogrammaScomparsa = 0;
		finito = false;
	}

	/**
	 * Quanti fotogrammi dura (per eccesso) un ciclo intero: il tracciamento del corridore piu' lento, in genere un giro completo, piu' il ritardo del
	 * corridore piu' «tardivo», la dissolvenza, la pausa finale e la scomparsa.
	 */
	public int stimaFotogrammiCiclo() {
		int ritardoMassimo = 0;
		for (Corridore c : corridori) {
			ritardoMassimo = Math.max(ritardoMassimo, c.fotogrammiRitardoIniziale);
		}
		return fotogrammiPerGiro + ritardoMassimo + 3 + fotogrammiDissolvenza + fotogrammiPausa + fotogrammiScomparsa;
	}

	/**
	 * L'opacita' (0.0-1.0) a cui disegnare il logo intero, in base alla fase corrente: invisibile durante il tracciamento (la torcia pensa a rivelarlo
	 * localmente), in transizione lineare durante la dissolvenza, piena durante la pausa finale, di nuovo in transizione (all'indietro) durante la
	 * scomparsa. Senza torcia il logo e' pieno fin dall'inizio e cambia solo nella scomparsa.
	 */
	public double getOpacitaLogo() {
		switch (fase) {
			case TRACCIAMENTO:
				return rivelaConTorcia ? 0.0 : 1.0;
			case DISSOLVENZA:
				return rivelaConTorcia ? Math.min(1.0, (double) fotogrammaDissolvenza / fotogrammiDissolvenza) : 1.0;
			case SCOMPARSA:
				return 1.0 - Math.min(1.0, (double) fotogrammaScomparsa / fotogrammiScomparsa);
			default: // PAUSA
				return 1.0;
		}
	}

	/**
	 * L'opacita' (0.0-1.0) delle scie e delle teste dei corridori. Senza sfumatura sono piene per tutto il ciclo, e svaniscono solo con la scomparsa
	 * del logo; con la sfumatura svaniscono durante la dissolvenza, mentre il logo compare, e da li' in poi non si vedono piu'.
	 */
	public double getOpacitaScie() {
		if (sfumaScie) {
			switch (fase) {
				case TRACCIAMENTO:
					return 1.0;
				case DISSOLVENZA:
					return 1.0 - Math.min(1.0, (double) fotogrammaDissolvenza / fotogrammiDissolvenza);
				default: // PAUSA e SCOMPARSA
					return 0.0;
			}
		}
		if (fase == Fase.SCOMPARSA) {
			return 1.0 - Math.min(1.0, (double) fotogrammaScomparsa / fotogrammiScomparsa);
		}
		return 1.0;
	}

	/**
	 * Fa avanzare l'animazione di un fotogramma: tutti i corridori e la macchina a stati globale. In fase TRACCIAMENTO un corridore che raggiunge la
	 * fine del proprio percorso si ferma e aspetta: si passa a DISSOLVENZA solo quando TUTTI hanno finito (vedi punto 6). Dopo la pausa, se c'e', viene
	 * la scomparsa del logo; alla fine del ciclo l'effetto si ferma (vedi {@link #isFinito()}) oppure, con la ripetizione, ricomincia.
	 */
	public void avanza() {
		switch (fase) {
			case TRACCIAMENTO: {
				// false appena un corridore qualsiasi non ha ancora finito (o non e' nemmeno partito)
				boolean tuttiFiniti = true;
				for (Corridore r : corridori) {
					if (r.fotogrammiRitardo > 0) {
						// ancora «in attesa»: il conto alla rovescia scende di uno, ma non si muove lungo il percorso
						r.fotogrammiRitardo--;
						tuttiFiniti = false;
						continue;
					}
					int ultimo = r.percorso.size() - 1;
					if (r.avanzamento < ultimo) {
						// senza mai superare l'ultimo punto del percorso
						r.avanzamento = Math.min(ultimo, r.avanzamento + r.puntiPerFotogramma);
						tuttiFiniti = false;
					}
					// se e' gia' in fondo resta fermo li', aspettando gli altri
				}
				if (tuttiFiniti) {
					fase = Fase.DISSOLVENZA;
					fotogrammaDissolvenza = 0;
				}
				break;
			}
			case DISSOLVENZA:
				fotogrammaDissolvenza++;
				if (fotogrammaDissolvenza >= fotogrammiDissolvenza) {
					fase = Fase.PAUSA;
					fotogrammaPausa = 0;
				}
				break;
			case PAUSA:
				fotogrammaPausa++;
				if (fotogrammaPausa >= fotogrammiPausa && !finito) {
					if (fotogrammiScomparsa > 0) {
						fase = Fase.SCOMPARSA;
						fotogrammaScomparsa = 0;
					} else {
						fineCiclo();
					}
				}
				break;
			case SCOMPARSA:
				if (fotogrammaScomparsa < fotogrammiScomparsa) {
					fotogrammaScomparsa++;
				}
				if (fotogrammaScomparsa >= fotogrammiScomparsa && !finito) {
					fineCiclo();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Alla fine del ciclo: una volta sola l'effetto e' finito e resta fermo sull'ultimo fotogramma (il logo acceso, o spento dopo la scomparsa); con la
	 * ripetizione si ricomincia, riportando ogni corridore all'inizio del proprio percorso E ripristinando il ritardo iniziale (altrimenti al secondo
	 * giro partirebbero tutti insieme).
	 */
	private void fineCiclo() {
		if (!ripeti) {
			finito = true;
			return;
		}
		for (Corridore r : corridori) {
			r.riparti();
		}
		fase = Fase.TRACCIAMENTO;
	}

	// =========================================================================
	//  Costruttore
	// =========================================================================

	/**
	 * Imposta l'effetto: tutti i parametri sono facoltativi, con i valori predefiniti in cima alla classe. {@link #costruisci()} esegue le fasi 1-5 della
	 * pipeline (maschera, contorni, buchi, ricampionamento, corridori).
	 */
	public static final class Costruttore {

		private final ImmagineDiPixel logo;
		private PredicatoSfondo sfondo;
		private Area regione;
		private ImmagineDiPixel daMostrare;
		private final List<SpecificaCorridore> corridoriEspliciti = new ArrayList<>();
		private List<Verso> versi = Collections.emptyList();
		private List<Integer> ritardi = Collections.emptyList();
		private boolean ambiVersi;
		private int durataGiroMs = DURATA_GIRO_MS_PREDEFINITA;
		private boolean velocitaAllineata;
		private boolean rivelaConTorcia = true;
		private Integer raggioTorcia;
		private int fotogrammiDissolvenza = FOTOGRAMMI_DISSOLVENZA_PREDEFINITI;
		private int fotogrammiPausa = FOTOGRAMMI_PAUSA_PREDEFINITI;
		private int fotogrammiScomparsa;
		private boolean sfumaScie;
		private boolean ripeti;

		private Costruttore(ImmagineDiPixel logo) {
			this.logo = Objects.requireNonNull(logo, "logo");
		}

		/** Sono sfondo i pixel con alpha minore o uguale alla soglia (predefinito: trasparenza con soglia 128). */
		public Costruttore sfondoPerAlpha(int soglia) {
			this.sfondo = PredicatoSfondo.perAlpha(soglia);
			return this;
		}

		/** Sono sfondo i pixel di colore vicino a questo (0xRRGGBB), entro la tolleranza per canale (scontorno per colore). */
		public Costruttore sfondoPerColore(int rgb, int tolleranza) {
			this.sfondo = PredicatoSfondo.perColore(rgb, tolleranza);
			return this;
		}

		/** Un criterio di sfondo qualsiasi. */
		public Costruttore sfondo(PredicatoSfondo sfondo) {
			this.sfondo = Objects.requireNonNull(sfondo, "sfondo");
			return this;
		}

		/**
		 * Si disegna questa immagine al posto del logo da cui si traccia (vedi punto 1ter): deve avere la stessa dimensione, con il disegno da tracciare
		 * esattamente nello stesso posto. Null per disegnare il logo stesso.
		 */
		public Costruttore immagineDaMostrare(ImmagineDiPixel daMostrare) {
			this.daMostrare = daMostrare;
			return this;
		}

		/** Cerca il logo solo in questo rettangolo dell'immagine (vedi punto 1bis); null per tutta l'immagine. */
		public Costruttore regione(Area regione) {
			this.regione = regione;
			return this;
		}

		/**
		 * Aggiunge un corridore esplicito: parte dal punto del contorno piu' vicino a {@code partenza} e arriva a quello piu' vicino ad {@code arrivo},
		 * oppure fa un giro completo se {@code arrivo} e' null. Se non se ne aggiunge nessuno, c'e' un corridore a giro completo per ogni forma trovata.
		 */
		public Costruttore corridore(Posizione partenza, Posizione arrivo, Verso verso, int fotogrammiRitardo) {
			corridoriEspliciti.add(new SpecificaCorridore(partenza, arrivo, verso, Math.max(0, fotogrammiRitardo)));
			return this;
		}

		/** Il verso di tutti i corridori automatici (predefinito orario). */
		public Costruttore verso(Verso verso) {
			return versi(Collections.singletonList(verso));
		}

		/** Il verso dei corridori automatici, uno per forma nell'ordine in cui sono trovate; alle forme in piu' si applica il primo. */
		public Costruttore versi(List<Verso> versi) {
			this.versi = new ArrayList<>(versi);
			return this;
		}

		/**
		 * Vero perche' i corridori automatici siano due per ogni forma, uno orario e uno antiorario, che partono dallo stesso punto e si incontrano a
		 * meta' del contorno (ognuno fa mezzo giro, e quando si toccano il giro e' completo); predefinito falso. Il ritardo della forma vale per
		 * entrambi; il verso dato da {@link #verso} o {@link #versi} non conta. Non vale se ci sono corridori espliciti.
		 */
		public Costruttore ambiVersi(boolean ambiVersi) {
			this.ambiVersi = ambiVersi;
			return this;
		}

		/** Il ritardo di partenza dei corridori automatici, in fotogrammi, uno per forma (le altre partono subito). */
		public Costruttore ritardi(List<Integer> ritardi) {
			this.ritardi = new ArrayList<>(ritardi);
			return this;
		}

		/** Durata di riferimento di un giro completo, in millisecondi. */
		public Costruttore durataGiroMs(int durataGiroMs) {
			this.durataGiroMs = durataGiroMs;
			return this;
		}

		/** Vero perche' tutti i corridori arrivino in fondo insieme (vedi punto 6); predefinito falso. */
		public Costruttore velocitaAllineata(boolean velocitaAllineata) {
			this.velocitaAllineata = velocitaAllineata;
			return this;
		}

		/** Falso per mostrare subito il logo intero invece di rivelarlo con la torcia (vedi punto 7). */
		public Costruttore rivelaConTorcia(boolean rivelaConTorcia) {
			this.rivelaConTorcia = rivelaConTorcia;
			return this;
		}

		/** Raggio della torcia in pixel (predefinito circa il 10% del lato piu' corto del logo). */
		public Costruttore raggioTorcia(int raggioTorcia) {
			this.raggioTorcia = raggioTorcia;
			return this;
		}

		/** Durata della dissolvenza finale, in fotogrammi. */
		public Costruttore fotogrammiDissolvenza(int fotogrammiDissolvenza) {
			this.fotogrammiDissolvenza = Math.max(1, fotogrammiDissolvenza);
			return this;
		}

		/** Quanti fotogrammi resta acceso il logo intero alla fine. */
		public Costruttore fotogrammiPausa(int fotogrammiPausa) {
			this.fotogrammiPausa = Math.max(0, fotogrammiPausa);
			return this;
		}

		/**
		 * Vero perche', mentre il logo compare in dissolvenza, le scie dei corridori svaniscano insieme e le loro teste luminose non si disegnino piu':
		 * alla fine resta il logo da solo. Predefinito falso (scie e teste restano fino alla fine del ciclo).
		 */
		public Costruttore sfumaScie(boolean sfumaScie) {
			this.sfumaScie = sfumaScie;
			return this;
		}

		/**
		 * Dopo la pausa, il logo (con quel che resta delle scie) svanisce in questi fotogrammi, e solo allora il ciclo e' finito. Predefinito 0:
		 * nessuna scomparsa, il logo resta acceso.
		 */
		public Costruttore fotogrammiScomparsa(int fotogrammiScomparsa) {
			this.fotogrammiScomparsa = Math.max(0, fotogrammiScomparsa);
			return this;
		}

		/** Vero per ricominciare da capo dopo la pausa, all'infinito; predefinito falso (una volta sola). */
		public Costruttore ripeti(boolean ripeti) {
			this.ripeti = ripeti;
			return this;
		}

		/**
		 * Esegue la pipeline e restituisce l'effetto, fermo al primo fotogramma.
		 *
		 * @throws IllegalArgumentException se la regione esce dall'immagine o l'immagine da mostrare ha un'altra dimensione
		 * @throws IllegalStateException    se non si trova nessun contorno
		 */
		public AnimazioneDelLogo costruisci() {
			List<String> diagnostica = new ArrayList<>();
			if (daMostrare != null && (daMostrare.getLarghezza() != logo.getLarghezza() || daMostrare.getAltezza() != logo.getAltezza())) {
				throw new IllegalArgumentException("l'immagine da mostrare (" + daMostrare.getLarghezza() + "x" + daMostrare.getAltezza()
						+ ") deve avere la dimensione del logo da tracciare (" + logo.getLarghezza() + "x" + logo.getAltezza() + ")");
			}
			PredicatoSfondo predicatoSfondo = sfondo;
			if (predicatoSfondo == null) {
				if (!haTrasparenza(logo)) {
					diagnostica.add("Attenzione: l'immagine non ha canale alpha; va indicato un colore di sfondo per tracciarla correttamente.");
				}
				predicatoSfondo = PredicatoSfondo.perAlpha(SOGLIA_ALPHA_PREDEFINITA);
			}

			// --- area di scansione opzionale (fase 1bis) ----------------------------
			if (regione != null && (regione.getX() < 0 || regione.getY() < 0 || regione.getLarghezza() <= 0 || regione.getAltezza() <= 0
					|| regione.getX() + regione.getLarghezza() > logo.getLarghezza() || regione.getY() + regione.getAltezza() > logo.getAltezza())) {
				throw new IllegalArgumentException("il rettangolo (" + regione.getX() + "," + regione.getY() + "," + regione.getLarghezza() + ","
						+ regione.getAltezza() + ") esce dai limiti dell'immagine (" + logo.getLarghezza() + "x" + logo.getAltezza() + ")");
			}
			if (regione != null) {
				diagnostica.add("Area di scansione: " + regione.getLarghezza() + "x" + regione.getAltezza() + " a partire da (" + regione.getX() + ","
						+ regione.getY() + "); la resa userà comunque l'immagine intera.");
			}

			// --- fasi 1-4: maschera, contorni esterni e buchi, ricampionamento, verso naturale ---
			List<ContorniDelLogo.Forma> forme = ContorniDelLogo.di(logo, predicatoSfondo, regione, PUNTI_RICAMPIONAMENTO);
			if (forme.isEmpty()) {
				throw new IllegalStateException("Nessun contorno trovato: controlla la soglia/il colore di sfondo, l'immagine o la regione.");
			}

			// diagnostica: utile per calibrare le coordinate di partenza e arrivo dei corridori a tentativi
			int esterne = ContorniDelLogo.esterne(forme);
			diagnostica.add(forme.size() + " forma/e rilevate (" + esterne + " contorni esterni, " + (forme.size() - esterne) + " buchi interni):");
			for (int i = 0; i < forme.size(); i++) {
				ContorniDelLogo.Forma forma = forme.get(i);
				diagnostica.add("  forma " + i + " (" + (forma.esterna ? "esterno" : "buco") + "): verso naturale "
						+ (forma.naturaleOrario ? "orario" : "antiorario")
						+ ", partenza naturale " + formattaPunto(forma.percorsoNaturale.get(0)) + ", " + forma.percorsoNaturale.size() + " punti.");
			}

			// --- fase 5: costruzione delle «specifiche» di ciascun corridore -----------
			// Senza corridori espliciti ce n'e' uno a giro completo per ogni forma rilevata, ciascuno con il proprio ritardo/verso se ritardi/versi ne
			// definiscono uno; il primo verso (o quello orario) fa da «riserva» per i corridori in eccesso
			List<SpecificaCorridore> specifiche = new ArrayList<>(corridoriEspliciti);
			if (specifiche.isEmpty()) {
				Verso versoDiRiserva = versi.isEmpty() ? Verso.ORARIO : versi.get(0);
				for (int i = 0; i < forme.size(); i++) {
					int ritardo = i < ritardi.size() ? Math.max(0, ritardi.get(i)) : 0;
					List<Posizione> contorno = forme.get(i).percorsoNaturale;
					if (ambiVersi) {
						// due corridori dallo stesso punto, uno per verso, che si fermano al punto opposto del contorno (a meta' dei punti
						// ricampionati, equidistanti): fanno lo stesso numero di passi e si toccano
						Posizione incontro = contorno.get(contorno.size() / 2);
						specifiche.add(new SpecificaCorridore(contorno.get(0), incontro, Verso.ORARIO, ritardo));
						specifiche.add(new SpecificaCorridore(contorno.get(0), incontro, Verso.ANTIORARIO, ritardo));
					} else {
						Verso direzione = i < versi.size() ? versi.get(i) : versoDiRiserva;
						specifiche.add(new SpecificaCorridore(contorno.get(0), null, direzione, ritardo));
					}
				}
			}

			// fotogrammiPerGiro = quanti fotogrammi servono per completare un giro di riferimento alla velocita' di riferimento
			int fotogrammiPerGiro = Math.max(1, durataGiroMs / INTERVALLO_FOTOGRAMMA_MS);

			List<Corridore> corridori = new ArrayList<>();
			for (SpecificaCorridore specifica : specifiche) {
				// trasforma la specifica astratta (punti pixel) in un percorso concreto di punti ricampionati, agganciato alla forma giusta
				List<Posizione> percorso = costruisciPercorsoCorridore(forme, specifica);
				double puntiPerFotogramma = velocitaAllineata
						// velocita' «su misura»: lunghezza del percorso diviso i fotogrammi disponibili, cosi' ogni corridore arriva in fondo
						// esattamente nello stesso fotogramma (vedi punto 6)
						? (double) percorso.size() / fotogrammiPerGiro
						// velocita' di riferimento uguale per tutti: un percorso piu' corto del giro di riferimento finisce prima e resta fermo
						// ad aspettare gli altri
						: (double) PUNTI_RICAMPIONAMENTO / fotogrammiPerGiro;
				corridori.add(new Corridore(percorso, puntiPerFotogramma, specifica.fotogrammiRitardo));
				diagnostica.add("Corridore: partenza agganciata a " + formattaPunto(percorso.get(0))
						+ (specifica.arrivo != null ? ", arrivo agganciato a " + formattaPunto(percorso.get(percorso.size() - 1)) : ", giro completo")
						+ ", verso " + (specifica.verso == Verso.ORARIO ? "orario" : "antiorario")
						+ ", " + percorso.size() + " punti" + (velocitaAllineata ? " (velocita' sincronizzata)" : "")
						+ (specifica.fotogrammiRitardo > 0 ? String.format(Locale.ROOT, ", ritardo %d fotogrammi (~%.2fs)",
						specifica.fotogrammiRitardo, specifica.fotogrammiRitardo * INTERVALLO_FOTOGRAMMA_MS / 1000.0) : "") + ".");
			}

			// il raggio della torcia e' proporzionato alle dimensioni dell'immagine: un raggio fisso, pensato per loghi grandi, coprirebbe quasi tutto un
			// logo piccolo, facendo sembrare la torcia «sempre spenta»
			int raggio = raggioTorcia != null ? raggioTorcia : Math.max(6, Math.min(logo.getLarghezza(), logo.getAltezza()) / 10);

			return new AnimazioneDelLogo(this, corridori, fotogrammiPerGiro, raggio, diagnostica);
		}
	}

	private static boolean haTrasparenza(ImmagineDiPixel immagine) {
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				if ((immagine.getArgb(x, y) >>> 24) != 0xFF) {
					return true;
				}
			}
		}
		return false;
	}

	/** Formatta un punto per i messaggi diagnostici, arrotondato al pixel. */
	private static String formattaPunto(Posizione p) {
		return "(" + Math.round(p.x) + "," + Math.round(p.y) + ")";
	}

	// =========================================================================
	//  5. Il percorso di un corridore
	// =========================================================================

	/** Indice del punto di {@code percorso} piu' vicino (distanza euclidea) a {@code obiettivo}. */
	private static int indicePiuVicino(List<Posizione> percorso, Posizione obiettivo) {
		int migliore = 0;
		double distanzaMigliore = Double.MAX_VALUE;
		for (int i = 0; i < percorso.size(); i++) {
			// al quadrato: evita una radice per ogni punto, visto che interessa solo QUALE e' il piu' vicino
			double d = percorso.get(i).distanzaQuadrata(obiettivo);
			if (d < distanzaMigliore) {
				distanzaMigliore = d;
				migliore = i;
			}
		}
		return migliore;
	}

	/** Una copia di {@code percorso} «ruotata» in modo che l'elemento {@code indiceIniziale} diventi il primo. */
	private static List<Posizione> ruota(List<Posizione> percorso, int indiceIniziale) {
		int n = percorso.size();
		List<Posizione> ruotato = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			ruotato.add(percorso.get((indiceIniziale + i) % n));
		}
		return ruotato;
	}

	/**
	 * Costruisce il percorso concreto (ordinato) di un corridore: individua a quale forma appartiene il punto di partenza richiesto (quella con il punto
	 * di contorno piu' vicino), sceglie in quella forma la lista con il verso richiesto, la ruota per iniziare nel punto agganciato e, se e' dato un
	 * arrivo, la taglia nel punto piu' vicino a quello; altrimenti richiude il giro completo.
	 */
	private static List<Posizione> costruisciPercorsoCorridore(List<ContorniDelLogo.Forma> forme, SpecificaCorridore specifica) {
		// passo 1: fra TUTTE le forme, quella il cui contorno (verso naturale) ha il punto piu' vicino alla partenza richiesta
		int formaMigliore = 0;
		int indiceMigliore = 0;
		double distanzaMigliore = Double.MAX_VALUE;
		for (int s = 0; s < forme.size(); s++) {
			List<Posizione> np = forme.get(s).percorsoNaturale;
			int indice = indicePiuVicino(np, specifica.partenza);
			double d = np.get(indice).distanzaQuadrata(specifica.partenza);
			if (d < distanzaMigliore) {
				distanzaMigliore = d;
				formaMigliore = s;
				indiceMigliore = indice;
			}
		}

		// passo 2: dentro quella forma, la lista (naturale o invertita) che corrisponde al verso richiesto
		ContorniDelLogo.Forma forma = forme.get(formaMigliore);
		boolean vuoleOrario = specifica.verso == Verso.ORARIO;
		boolean usaNaturale = vuoleOrario == forma.naturaleOrario;
		List<Posizione> scelto = usaNaturale ? forma.percorsoNaturale : forma.percorsoInvertito;
		// con la lista invertita l'indice va rispecchiato: invertito[i] = naturale[n-1-i]
		int indiceIniziale = usaNaturale ? indiceMigliore : (scelto.size() - 1 - indiceMigliore);
		// passo 3: ruota la lista scelta cosi' che inizi esattamente nel punto agganciato
		List<Posizione> ruotato = ruota(scelto, indiceIniziale);

		if (specifica.arrivo == null) {
			// nessun arrivo richiesto: giro completo, il percorso si richiude tornando al punto di partenza
			List<Posizione> chiuso = new ArrayList<>(ruotato);
			chiuso.add(ruotato.get(0));
			return chiuso;
		}

		// arrivo richiesto: taglia il percorso (gia' ruotato, quindi nel verso corretto) al punto piu' vicino all'arrivo
		int indiceFinale = indicePiuVicino(ruotato, specifica.arrivo);
		if (indiceFinale == 0) {
			indiceFinale = ruotato.size() - 1; // evita un arco degenere di lunghezza zero
		}
		return new ArrayList<>(ruotato.subList(0, indiceFinale + 1));
	}
}
