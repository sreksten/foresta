package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;

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
 */
public final class Passo {

	/**
	 * L'id che chiude la missione: dopo questo non c'è nessun passo.
	 */
	public static final String FINE = "FINE";

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
	private Runnable azione = () -> {
	};
	private Supplier<String> prossimoPasso = () -> FINE;
	private MomentoIntermezzo momentoIntermezzo;
	private Supplier<List<PaginaIntermezzo>> pagine;

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
	 * Che cosa fare, una volta sola, quando il passo si conclude.
	 */
	public Passo esegui(Runnable azione) {
		this.azione = Objects.requireNonNull(azione);
		return this;
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

	public MomentoControllo getMomento() {
		return momento;
	}

	public boolean isConcluso() {
		return condizione.getAsBoolean();
	}

	void eseguiAzione() {
		azione.run();
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
