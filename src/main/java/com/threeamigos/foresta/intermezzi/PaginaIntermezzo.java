package com.threeamigos.foresta.intermezzi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Una pagina di intermezzo, composta a strati: lo sfondo (un'immagine che copre tutto
 * lo schermo, o in sua assenza l'ombra del drago), gli elementi nell'ordine in cui sono
 * aggiunti (i successivi sopra i precedenti), il testo in alto, i fumetti delle battute.
 * Tutto è facoltativo, anche il testo.
 * <p>
 * Le pagine si costruiscono al momento in cui l'intermezzo scatta, quindi personaggi e
 * dialoghi possono essere scelti in base alla partita:
 * <pre>
 *     new PaginaIntermezzo()
 *             .conSfondo(ImmagineIntermezzo.locazione(ClassiLocazione.RADURA))
 *             .conElemento(ElementoIntermezzo.personaggio("eroe", capo.getClasse(), 0.3, 0.7))
 *             .conElemento(ElementoIntermezzo.personaggio("eremita", ClassePersonaggio.EREMITA, 0.7, 0.7).specchiato())
 *             .conBattuta(BattutaIntermezzo.di("eremita", "Chi va là?"))
 *             .conBattuta(BattutaIntermezzo.di("eroe", "Sono " + nome + '.'))
 * </pre>
 */
public class PaginaIntermezzo {

	/** Pausa fra una battuta e la successiva quando non se ne indica l'inizio. */
	static final double PAUSA_FRA_BATTUTE = 0.3;

	private final String testo;
	private ImmagineIntermezzo sfondo;
	private final Map<String, ElementoIntermezzo> elementi = new LinkedHashMap<>();
	private final List<BattutaIntermezzo> battute = new ArrayList<>();
	private double durata = Double.NaN;

	/** Una pagina senza testo in alto, solo immagini e fumetti. */
	public PaginaIntermezzo() {
		this(null);
	}

	public PaginaIntermezzo(String testo) {
		this.testo = testo;
	}

	public PaginaIntermezzo conSfondo(ImmagineIntermezzo sfondo) {
		this.sfondo = sfondo;
		return this;
	}

	public PaginaIntermezzo conElemento(ElementoIntermezzo elemento) {
		if (elementi.containsKey(elemento.getId())) {
			throw new IllegalArgumentException("Elemento già presente nella pagina: " + elemento.getId());
		}
		elementi.put(elemento.getId(), elemento);
		return this;
	}

	/**
	 * Aggiunge una battuta. Chi la dice deve essere già stato aggiunto alla pagina.
	 */
	public PaginaIntermezzo conBattuta(BattutaIntermezzo battuta) {
		if (battuta.getIdElemento() != null && !elementi.containsKey(battuta.getIdElemento())) {
			throw new IllegalArgumentException("La battuta \"" + battuta.getTesto() + "\" è di un elemento che non è nella pagina: "
					+ battuta.getIdElemento());
		}
		battute.add(battuta);
		return this;
	}

	/**
	 * Fissa quanto resta la pagina prima di avanzare da sola, al posto della durata
	 * calcolata (vedi {@link #getDurataContenuto()}).
	 */
	public PaginaIntermezzo perSecondi(double secondi) {
		durata = secondi;
		return this;
	}

	public String getTesto() {
		return testo;
	}

	public ImmagineIntermezzo getSfondo() {
		return sfondo;
	}

	public List<ElementoIntermezzo> getElementi() {
		return Collections.unmodifiableList(new ArrayList<>(elementi.values()));
	}

	public ElementoIntermezzo getElemento(String id) {
		return elementi.get(id);
	}

	public boolean hasDurata() {
		return !Double.isNaN(durata);
	}

	public double getDurata() {
		return durata;
	}

	/**
	 * Dopo quanti secondi la pagina avanza da sola (0 = solo al click): la durata fissata
	 * con {@link #perSecondi(double)} se c'è; altrimenti, se l'intermezzo avanza da solo,
	 * almeno il suo tempo per pagina e comunque non prima che dialoghi e animazioni
	 * siano finiti.
	 *
	 * @param secondiPerPagina come da {@link Intermezzo#getSecondiPerPagina()}
	 */
	public double getSecondiPrimaDiAvanzare(int secondiPerPagina) {
		if (hasDurata()) {
			return getDurata();
		}
		if (secondiPerPagina <= 0) {
			return 0;
		}
		return Math.max(secondiPerPagina, getDurataContenuto());
	}

	/**
	 * Le battute con il loro intervallo effettivo: quelle senza inizio indicato partono
	 * alla fine della precedente, più una breve pausa.
	 */
	public List<BattutaProgrammata> getBattuteProgrammate() {
		List<BattutaProgrammata> programma = new ArrayList<>();
		double fineUltima = 0;
		for (BattutaIntermezzo battuta : battute) {
			double inizio = battuta.hasInizio() ? battuta.getInizio()
					: (programma.isEmpty() ? 0 : fineUltima + PAUSA_FRA_BATTUTE);
			double fine = inizio + battuta.getDurata();
			programma.add(new BattutaProgrammata(battuta, inizio, fine));
			fineUltima = fine;
		}
		return programma;
	}

	/**
	 * Quanto dura ciò che accade nella pagina: la fine dell'ultima battuta o dell'ultima
	 * animazione che non si ripete. Zero per una pagina statica.
	 */
	public double getDurataContenuto() {
		double fine = 0;
		for (BattutaProgrammata battuta : getBattuteProgrammate()) {
			fine = Math.max(fine, battuta.getFine());
		}
		for (ElementoIntermezzo elemento : elementi.values()) {
			if (!Double.isInfinite(elemento.getDurata())) {
				fine = Math.max(fine, elemento.getDurata());
			}
		}
		return fine;
	}
}
