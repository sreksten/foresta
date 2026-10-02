package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.oggetti.NomeOggetto;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Gli oggetti che un passo di raccolta chiede di trovare (vedi {@link MissioneAPassi#raccogli(Passo.MomentoControllo,
 * OggettiDaRaccogliere)}): quanti, come si chiamano, sotto quale chiave la missione li conta, in quali locazioni
 * possono comparire e quanto spesso.
 * <pre>
 * OggettiDaRaccogliere.di("MANDRAGOLA", NomeOggetto.femminile("radice di mandragola", "radici di mandragola"), 4)
 *     .in(ClassiLocazione.RADURA, ClassiLocazione.BOSCO)
 *     .conProbabilita(30)
 *     .alPiuPerLocazione(2);
 * </pre>
 * Compaiono solo alla prima visita di una locazione, mai più di quanti ne mancano, e prendono il posto
 * dell'oggetto che la locazione avrebbe avuto (non di un artefatto del registro).
 */
public final class OggettiDaRaccogliere {

	private final String chiave;
	private final NomeOggetto nome;
	private final int quantita;
	private Set<ClassiLocazione> locazioni = Collections.emptySet();
	private int probabilita = 100;
	private int massimoPerLocazione = 1;

	private OggettiDaRaccogliere(String chiave, NomeOggetto nome, int quantita) {
		if (quantita < 1) {
			throw new IllegalArgumentException("Servono almeno un oggetto da raccogliere: " + quantita);
		}
		this.chiave = Objects.requireNonNull(chiave);
		this.nome = Objects.requireNonNull(nome);
		this.quantita = quantita;
	}

	public static OggettiDaRaccogliere di(String chiave, NomeOggetto nome, int quantita) {
		return new OggettiDaRaccogliere(chiave, nome, quantita);
	}

	public OggettiDaRaccogliere in(ClassiLocazione prima, ClassiLocazione... altre) {
		locazioni = EnumSet.of(prima, altre);
		return this;
	}

	/**
	 * La probabilità, in percentuale, che una locazione adatta visitata per la prima volta li abbia.
	 */
	public OggettiDaRaccogliere conProbabilita(int percentuale) {
		if (percentuale < 1 || percentuale > 100) {
			throw new IllegalArgumentException("Probabilità fuori da 1-100: " + percentuale);
		}
		probabilita = percentuale;
		return this;
	}

	public OggettiDaRaccogliere alPiuPerLocazione(int massimo) {
		if (massimo < 1) {
			throw new IllegalArgumentException("Almeno uno per locazione: " + massimo);
		}
		massimoPerLocazione = massimo;
		return this;
	}

	public String getChiave() {
		return chiave;
	}

	public NomeOggetto getNome() {
		return nome;
	}

	public int getQuantita() {
		return quantita;
	}

	public Set<ClassiLocazione> getLocazioni() {
		return Collections.unmodifiableSet(locazioni);
	}

	public int getProbabilita() {
		return probabilita;
	}

	public int getMassimoPerLocazione() {
		return massimoPerLocazione;
	}

	@Override
	public String toString() {
		return quantita + " " + nome.getPlurale() + " in " + Arrays.toString(locazioni.toArray());
	}
}
