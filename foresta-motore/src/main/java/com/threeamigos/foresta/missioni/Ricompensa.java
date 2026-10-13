package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.oggetti.Artefatto;

import java.util.function.Supplier;

/**
 * Che cosa dà una missione con il passo RICOMPENSA (vedi {@link MissioneAPassi#ricompensa(Passo.MomentoControllo,
 * Ricompensa, Supplier)}): monete, preziosi, esperienza e un artefatto, in qualunque combinazione.
 * <pre>
 * Ricompensa.inMonete(20).conEsperienza(50).conArtefatto(() -&gt; generatore.generaArtefattoCasuale(livello));
 * </pre>
 * L'artefatto si crea solo quando la missione lo consegna, al livello di quel momento.
 */
public final class Ricompensa {

	private int monete;
	private int preziosi;
	private int esperienza;
	private Supplier<Artefatto> artefatto;

	private Ricompensa() {
	}

	public static Ricompensa nulla() {
		return new Ricompensa();
	}

	public static Ricompensa inMonete(int monete) {
		return nulla().conMonete(monete);
	}

	public Ricompensa conMonete(int monete) {
		this.monete = nonNegativo(monete);
		return this;
	}

	public Ricompensa conPreziosi(int preziosi) {
		this.preziosi = nonNegativo(preziosi);
		return this;
	}

	public Ricompensa conEsperienza(int esperienza) {
		this.esperienza = nonNegativo(esperienza);
		return this;
	}

	public Ricompensa conArtefatto(Supplier<Artefatto> artefatto) {
		this.artefatto = artefatto;
		return this;
	}

	public int getMonete() {
		return monete;
	}

	public int getPreziosi() {
		return preziosi;
	}

	public int getEsperienza() {
		return esperienza;
	}

	/**
	 * Chi crea l'artefatto della ricompensa, o null se non ce n'è.
	 */
	public Supplier<Artefatto> getArtefatto() {
		return artefatto;
	}

	private static int nonNegativo(int valore) {
		if (valore < 0) {
			throw new IllegalArgumentException("Una ricompensa non toglie niente: " + valore);
		}
		return valore;
	}
}
