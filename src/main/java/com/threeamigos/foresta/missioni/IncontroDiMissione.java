package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gli avversari che una missione mette in una locazione al posto di quelli che ci sarebbero stati (vedi
 * {@link MissioneAPassi#combatti}): quanti, di che classe, ed eventualmente il loro capo con un nome proprio, un
 * livello sopra gli altri. Il capo può essere uno della banda o, di un'altra classe, in più (vedi
 * {@link MissioneAPassi#combattiIlCapo}).
 * <pre>
 * IncontroDiMissione.di(ClassePersonaggio.HOBGOBLIN, 3).conCapo("Sgranf");
 * IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 3).conCapo("Grumolo", ClassePersonaggio.HOBGOBLIN);
 * </pre>
 */
public final class IncontroDiMissione {

	private final ClassePersonaggio classe;
	private final int numero;
	private String nomeDelCapo;
	// La classe del capo se non è uno della banda, altrimenti null
	private ClassePersonaggio classeDelCapo;

	private IncontroDiMissione(ClassePersonaggio classe, int numero) {
		if (numero < 1) {
			throw new IllegalArgumentException("Serve almeno un avversario: " + numero);
		}
		this.classe = Objects.requireNonNull(classe);
		this.numero = numero;
	}

	public static IncontroDiMissione di(ClassePersonaggio classe, int numero) {
		return new IncontroDiMissione(classe, numero);
	}

	/**
	 * Il primo degli avversari è il capo: ha un nome proprio e un livello in più.
	 */
	public IncontroDiMissione conCapo(String nome) {
		this.nomeDelCapo = Objects.requireNonNull(nome);
		return this;
	}

	/**
	 * Un capo in più, di un'altra classe (un hobgoblin a capo di una banda di goblin): ha un nome proprio e un
	 * livello in più della banda.
	 */
	public IncontroDiMissione conCapo(String nome, ClassePersonaggio classe) {
		this.nomeDelCapo = Objects.requireNonNull(nome);
		this.classeDelCapo = Objects.requireNonNull(classe);
		return this;
	}

	/**
	 * La classe del capo: la sua, se non è uno della banda, altrimenti quella della banda.
	 */
	public ClassePersonaggio getClasseDelCapo() {
		return classeDelCapo != null ? classeDelCapo : classe;
	}

	public ClassePersonaggio getClasse() {
		return classe;
	}

	public int getNumero() {
		return numero;
	}

	public String getNomeDelCapo() {
		return nomeDelCapo;
	}

	/**
	 * Gli avversari, al livello del mondo. Un capo di un'altra classe viene dopo la banda, così il gruppo avversario
	 * si presenta con la classe della banda ("quattro goblin").
	 */
	public List<Personaggio> crea() {
		List<Personaggio> avversari = new ArrayList<>();
		int livello = Statistiche.getLivello();
		for (int i = 0; i < numero; i++) {
			boolean capo = i == 0 && nomeDelCapo != null && classeDelCapo == null;
			Personaggio avversario = classe.getIstanza(capo ? livello + 1 : livello);
			if (capo) {
				avversario.getModelloDati().setNome(nomeDelCapo);
			}
			avversario.setOrdinale(i + 1);
			avversari.add(avversario);
		}
		if (classeDelCapo != null) {
			Personaggio capo = classeDelCapo.getIstanza(livello + 1);
			capo.getModelloDati().setNome(nomeDelCapo);
			capo.setOrdinale(1);
			avversari.add(capo);
		}
		return avversari;
	}
}
