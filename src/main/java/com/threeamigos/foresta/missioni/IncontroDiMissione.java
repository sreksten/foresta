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
 * livello sopra gli altri.
 * <pre>
 * IncontroDiMissione.di(ClassePersonaggio.HOBGOBLIN, 3).conCapo("Sgranf");
 * </pre>
 */
public final class IncontroDiMissione {

	private final ClassePersonaggio classe;
	private final int numero;
	private String nomeDelCapo;

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
	 * Gli avversari, al livello del mondo.
	 */
	public List<Personaggio> crea() {
		List<Personaggio> avversari = new ArrayList<>();
		int livello = Statistiche.getLivello();
		for (int i = 0; i < numero; i++) {
			boolean capo = i == 0 && nomeDelCapo != null;
			Personaggio avversario = classe.getIstanza(capo ? livello + 1 : livello);
			if (capo) {
				avversario.getModelloDati().setNome(nomeDelCapo);
			}
			avversario.setOrdinale(i + 1);
			avversari.add(avversario);
		}
		return avversari;
	}
}
