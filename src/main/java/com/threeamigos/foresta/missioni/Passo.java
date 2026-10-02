package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Un passo di una {@link MissioneAPassi} (vedi gestione_missioni.md, §3): in quale controllo dell'automa si
 * valuta, quando è concluso, che cosa fa una volta concluso e quale passo viene dopo. Non è una Missione e non
 * entra nell'albero delle missioni secondarie: è la logica di avanzamento dentro una sola missione.
 * <p>
 * Il passo successivo non è una posizione in una lista ma un id restituito da {@link #prossimoPasso}, calcolato
 * dopo l'azione: una catena lineare restituisce una costante, una diramazione legge lo stato di gioco o una
 * proprietà della missione. {@link #FINE} vuol dire che la missione è finita.
 * <p>
 * Si costruisce con un piccolo builder fluente:
 * <pre>
 * Passo.quando(MomentoControllo.POST_LOCAZIONE, () -&gt; grottaCompletata())
 *      .esegui(() -&gt; annunciaRecupero())
 *      .poi("RITORNO")
 *      .conIntermezzo(MomentoIntermezzo.LOCAZIONE_COMPLETATA, () -&gt; pagineRecupero());
 * </pre>
 * I passi non si salvano: la missione li ricostruisce dal loro id ogni volta che servono
 * ({@link MissioneAPassi#costruisciPasso}), quindi condizioni e azioni possono essere lambda qualsiasi.
 * <p>
 * Un passo può anche porre una domanda al giocatore ({@link #chiediConferma}, {@link #chiediScelta}): allora la
 * condizione dice quando la domanda si può porre, e il passo si conclude quando il giocatore ha risposto. La
 * risposta si legge con {@link MissioneAPassi#getRisposta(String)}, tipicamente nel {@code poi} della diramazione.
 */
public final class Passo {

	/**
	 * L'id che chiude la missione: dopo questo non c'è nessun passo.
	 */
	public static final String FINE = "FINE";

	/**
	 * Le risposte a una conferma (vedi {@link #chiediConferma}); quelle a una scelta sono "1", "2"... fino al
	 * numero delle opzioni.
	 */
	public static final String SI = "SI";
	public static final String NO = "NO";
	public static final int OPZIONI_MINIME = 2;
	public static final int OPZIONI_MASSIME = 5;

	/**
	 * In quale dei tre controlli delle missioni (vedi {@link Missione#controllaPreLocazione()} e seguenti) si
	 * valuta il passo.
	 */
	public enum MomentoControllo {
		PRE_LOCAZIONE,
		IN_LOCAZIONE,
		POST_LOCAZIONE
	}

	private final MomentoControllo momento;
	private final BooleanSupplier condizione;
	private final List<Runnable> azioni = new ArrayList<>();
	private BooleanSupplier guardia;
	private Supplier<String> testoFallimento;
	private Supplier<String> prossimoPasso = () -> FINE;
	private MomentoIntermezzo momentoIntermezzo;
	private Supplier<List<PaginaIntermezzo>> pagine;
	private String domanda;
	private List<String> opzioni;
	private OggettiDaRaccogliere oggettiDaSeminare;

	private Passo(MomentoControllo momento, BooleanSupplier condizione) {
		this.momento = Objects.requireNonNull(momento);
		this.condizione = Objects.requireNonNull(condizione);
	}

	/**
	 * Un passo valutato in quel controllo e concluso quando la condizione è vera.
	 */
	public static Passo quando(MomentoControllo momento, BooleanSupplier condizione) {
		return new Passo(momento, condizione);
	}

	/**
	 * Che cosa fare, una volta sola, quando il passo si conclude. Si può chiamare più volte: le azioni si eseguono
	 * nell'ordine in cui sono state aggiunte, così si può comporre un passo già pronto (un dialogo, una ricompensa)
	 * con altro.
	 */
	public Passo esegui(Runnable azione) {
		azioni.add(Objects.requireNonNull(azione));
		return this;
	}

	/**
	 * Finché questo è il passo corrente, se la condizione diventa vera la missione fallisce, scrivendo il testo
	 * (vedi {@link MissioneAPassi}): per esempio "il villaggio da difendere è stato distrutto". Si controlla in
	 * tutti e tre i controlli, prima di vedere se il passo è concluso.
	 */
	public Passo falliscoSe(BooleanSupplier condizione, Supplier<String> testo) {
		this.guardia = Objects.requireNonNull(condizione);
		this.testoFallimento = Objects.requireNonNull(testo);
		return this;
	}

	/**
	 * Se la guardia di {@link #falliscoSe} è scattata.
	 */
	boolean isFallito() {
		return guardia != null && guardia.getAsBoolean();
	}

	String getTestoFallimento() {
		return testoFallimento.get();
	}

	/**
	 * Finché questo è il passo corrente, la missione mette questi oggetti nelle locazioni (vedi
	 * {@link MissioneAPassi#getOggettoInLocazione}).
	 */
	public Passo semina(OggettiDaRaccogliere oggetti) {
		this.oggettiDaSeminare = Objects.requireNonNull(oggetti);
		return this;
	}

	/**
	 * Gli oggetti da mettere nelle locazioni finché questo è il passo corrente, o null.
	 */
	public OggettiDaRaccogliere getOggettiDaSeminare() {
		return oggettiDaSeminare;
	}

	/**
	 * Il passo successivo, sempre lo stesso.
	 */
	public Passo poi(String idProssimoPasso) {
		Objects.requireNonNull(idProssimoPasso);
		return poi(() -> idProssimoPasso);
	}

	/**
	 * Il passo successivo, calcolato dopo l'azione: per una diramazione.
	 */
	public Passo poi(Supplier<String> prossimoPasso) {
		this.prossimoPasso = Objects.requireNonNull(prossimoPasso);
		return this;
	}

	/**
	 * Un intermezzo da mostrare in quel momento dopo che il passo si è concluso. Le pagine si costruiscono quando
	 * scatta, così possono leggere lo stato di gioco di allora.
	 */
	public Passo conIntermezzo(MomentoIntermezzo momentoIntermezzo, Supplier<List<PaginaIntermezzo>> pagine) {
		this.momentoIntermezzo = Objects.requireNonNull(momentoIntermezzo);
		this.pagine = Objects.requireNonNull(pagine);
		return this;
	}

	/**
	 * Il passo chiede al giocatore una conferma: risposte {@link #SI} e {@link #NO}.
	 */
	public Passo chiediConferma(String domanda) {
		this.domanda = Objects.requireNonNull(domanda);
		this.opzioni = null;
		return this;
	}

	/**
	 * Il passo chiede al giocatore di scegliere fra 2 e 5 opzioni: risposte "1", "2"... nell'ordine.
	 */
	public Passo chiediScelta(String domanda, List<String> opzioni) {
		if (opzioni.size() < OPZIONI_MINIME || opzioni.size() > OPZIONI_MASSIME) {
			throw new IllegalArgumentException("Una scelta ha da " + OPZIONI_MINIME + " a " + OPZIONI_MASSIME
					+ " opzioni, non " + opzioni.size());
		}
		this.domanda = Objects.requireNonNull(domanda);
		this.opzioni = Collections.unmodifiableList(new ArrayList<>(opzioni));
		return this;
	}

	public boolean isDomanda() {
		return domanda != null;
	}

	/**
	 * Una domanda sì/no ({@link #chiediConferma}) e non una scelta fra opzioni.
	 */
	public boolean isConferma() {
		return domanda != null && opzioni == null;
	}

	public String getDomanda() {
		return domanda;
	}

	/**
	 * Le opzioni di una scelta, nell'ordine; vuota per una conferma o un passo senza domanda.
	 */
	public List<String> getOpzioni() {
		return opzioni == null ? Collections.emptyList() : opzioni;
	}

	/**
	 * Le risposte valide: {@link #SI} e {@link #NO} per una conferma, "1".."N" per una scelta.
	 */
	public List<String> getRispostePossibili() {
		if (!isDomanda()) {
			return Collections.emptyList();
		}
		if (isConferma()) {
			return Arrays.asList(SI, NO);
		}
		List<String> risposte = new ArrayList<>();
		for (int i = 1; i <= opzioni.size(); i++) {
			risposte.add(String.valueOf(i));
		}
		return risposte;
	}

	public MomentoControllo getMomento() {
		return momento;
	}

	/**
	 * Per un passo senza domanda, se è concluso; per un passo con una domanda, se la domanda si può porre.
	 */
	public boolean isConcluso() {
		return condizione.getAsBoolean();
	}

	void eseguiAzione() {
		azioni.forEach(Runnable::run);
	}

	String getProssimoPasso() {
		return prossimoPasso.get();
	}

	public boolean haIntermezzo() {
		return momentoIntermezzo != null;
	}

	/**
	 * Il momento dell'intermezzo, o null se il passo non ne ha.
	 */
	public MomentoIntermezzo getMomentoIntermezzo() {
		return momentoIntermezzo;
	}

	/**
	 * Le pagine dell'intermezzo, costruite adesso; il passo deve averne uno ({@link #haIntermezzo()}).
	 */
	public List<PaginaIntermezzo> getPagine() {
		if (pagine == null) {
			throw new IllegalStateException("Il passo non ha un intermezzo");
		}
		return pagine.get();
	}
}
