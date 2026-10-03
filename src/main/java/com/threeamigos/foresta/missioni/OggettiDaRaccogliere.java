package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

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
 * <p>
 * Con {@link #daiNemici} sono invece trofei (orecchie di goblin, pelli di lupo...): compaiono dovunque ci siano
 * avversari di quelle classi, anche nelle locazioni già visitate, e li custodiscono loro; per prenderli bisogna
 * sconfiggerli. Il loro ripiego è un bosco con i mostri che portano i trofei mancanti.
 * <p>
 * Con {@link #nellaCasella} stanno invece in una casella sola, scelta dalla missione (un oggetto smarrito): lì
 * compaiono tutti quelli che mancano, anche se la casella è già stata visitata, e altrove mai. Non hanno ripiego.
 * <p>
 * Se dopo {@link #getOreAlRipiego()} ore di gioco il gruppo non li ha ancora trovati tutti (magari ha già visitato
 * quasi tutte le locazioni adatte), la missione ripiega: si procura una locazione adatta, la segna sulla mappa e lì
 * mette tutti quelli che mancano (vedi MissioneAPassi.getRipiego).
 */
public final class OggettiDaRaccogliere {

	private final String chiave;
	private final NomeOggetto nome;
	private final int quantita;
	private Set<ClassiLocazione> locazioni = Collections.emptySet();
	private int probabilita = 100;
	private int massimoPerLocazione = 1;
	private int oreAlRipiego = ORE_AL_RIPIEGO;
	private Set<ClassePersonaggio> nemici = Collections.emptySet();
	private CoordinateMD casella;

	/**
	 * Dopo quante ore di gioco senza averli trovati tutti la missione ripiega, se non si dice altrimenti: tre giorni.
	 */
	public static final int ORE_AL_RIPIEGO = 72;

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
	 * Sono trofei che si prendono a quei nemici: compaiono dove ci sono, in qualunque locazione.
	 */
	public OggettiDaRaccogliere daiNemici(ClassePersonaggio primo, ClassePersonaggio... altri) {
		nemici = EnumSet.of(primo, altri);
		return this;
	}

	/**
	 * Stanno tutti in quella casella, e in nessun'altra.
	 */
	public OggettiDaRaccogliere nellaCasella(CoordinateMD casella) {
		this.casella = Objects.requireNonNull(casella);
		return this;
	}

	/**
	 * La casella in cui stanno, o null se compaiono nelle locazioni di certe classi o ai nemici.
	 */
	public CoordinateMD getCasella() {
		return casella;
	}

	/**
	 * I nemici a cui si prendono questi oggetti, vuoto se non sono trofei.
	 */
	public Set<ClassePersonaggio> getNemici() {
		return Collections.unmodifiableSet(nemici);
	}

	public boolean isTrofeo() {
		return !nemici.isEmpty();
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

	/**
	 * Dopo quante ore di gioco, da quando il passo è diventato corrente, la missione ripiega su una locazione segnata
	 * sulla mappa.
	 */
	public OggettiDaRaccogliere conRipiegoDopoOre(int ore) {
		if (ore < 1) {
			throw new IllegalArgumentException("Almeno un'ora prima del ripiego: " + ore);
		}
		oreAlRipiego = ore;
		return this;
	}

	public int getOreAlRipiego() {
		return oreAlRipiego;
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
