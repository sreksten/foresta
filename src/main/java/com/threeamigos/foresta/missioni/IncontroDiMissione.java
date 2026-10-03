package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.Ondata;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
 * Può arrivare a ondate (vedi {@link #poi}): sconfitti tutti quelli in campo, la locazione si riempie di nuovo, fino a
 * {@value #ONDATE_MASSIME} ondate in tutto. Con delle ondate in arrivo non si corrompe, non si fa amicizia e non si
 * passa inosservati; chi fugge, alla visita dopo ricomincia dalla prima.
 * <pre>
 * IncontroDiMissione.di(ClassePersonaggio.SCHELETRO, 3)
 *         .poi(IncontroDiMissione.di(ClassePersonaggio.MAGO, 1).conCapo("Mortimer"), "Il negromante esce dall'ombra!");
 * </pre>
 */
public final class IncontroDiMissione {

	private final ClassePersonaggio classe;
	private final int numero;
	private String nomeDelCapo;
	// La classe del capo se non è uno della banda, altrimenti null
	private ClassePersonaggio classeDelCapo;
	private boolean finoAllaResa;
	private boolean aDuello;
	private boolean aggirabile;
	private final List<IncontroDiMissione> ondateSuccessive = new ArrayList<>();
	private final List<String> arrivi = new ArrayList<>();

	/**
	 * Quante ondate al massimo, contando la prima.
	 */
	public static final int ONDATE_MASSIME = 3;

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
	 * Gli avversari combattono fino alla resa e non all'ultimo sangue (vedi Personaggio.isFinoAllaResa): sconfitti,
	 * si arrendono; e se si arrende tutto il gruppo, il gruppo se ne va e torna per la rivincita.
	 */
	public IncontroDiMissione finoAllaResa() {
		this.finoAllaResa = true;
		return this;
	}

	public boolean isFinoAllaResa() {
		return finoAllaResa;
	}

	/**
	 * L'avversario sfida a duello: uno contro uno, gli altri del gruppo in panchina (vedi Personaggio.isSfidante).
	 * Solo per un avversario solo.
	 */
	public IncontroDiMissione aDuello() {
		if (numero != 1 || classeDelCapo != null) {
			throw new IllegalStateException("A duello si sfida da soli");
		}
		this.aDuello = true;
		return this;
	}

	public boolean isADuello() {
		return aDuello;
	}

	/**
	 * Un'ondata in più, che arriva quando quelli di prima sono sconfitti tutti, con che cosa si scrive quando arriva.
	 * Non con un duello; al massimo {@value #ONDATE_MASSIME} ondate in tutto.
	 */
	public IncontroDiMissione poi(IncontroDiMissione ondata, String arrivo) {
		if (aDuello || ondata.aDuello) {
			throw new IllegalStateException("Un duello non arriva a ondate");
		}
		if (ondateSuccessive.size() + 1 >= ONDATE_MASSIME) {
			throw new IllegalStateException("Al massimo " + ONDATE_MASSIME + " ondate");
		}
		ondateSuccessive.add(Objects.requireNonNull(ondata));
		arrivi.add(Objects.requireNonNull(arrivo));
		return this;
	}

	/**
	 * Quante ondate in tutto, contando la prima.
	 */
	public int getNumeroDiOndate() {
		return 1 + ondateSuccessive.size();
	}

	/**
	 * Quanti avversari di ogni classe vanno sconfitti, in tutte le ondate: quelli della banda di ognuna (vedi
	 * {@link MissioneAPassi#combatti}).
	 */
	public Map<ClassePersonaggio, Integer> getSconfittiRichiesti() {
		Map<ClassePersonaggio, Integer> richiesti = new EnumMap<>(ClassePersonaggio.class);
		richiesti.merge(classe, numero, Integer::sum);
		ondateSuccessive.forEach(ondata -> richiesti.merge(ondata.classe, ondata.numero, Integer::sum));
		return richiesti;
	}

	/**
	 * Le ondate dopo la prima, al livello del mondo (vedi {@link #crea}).
	 */
	public List<Ondata> creaOndateSuccessive() {
		List<Ondata> ondate = new ArrayList<>();
		for (int i = 0; i < ondateSuccessive.size(); i++) {
			IncontroDiMissione ondata = ondateSuccessive.get(i);
			ondata.aggirabile = aggirabile;
			ondata.finoAllaResa = ondata.finoAllaResa || finoAllaResa;
			ondate.add(new Ondata(ondata.crea(), arrivi.get(i)));
		}
		return ondate;
	}

	/**
	 * Gli avversari si possono evitare passando inosservati (vedi LocazioneBase): le guardie di un colpo. Gli altri
	 * avversari di una missione, che la missione vuole sconfitti, vanno affrontati.
	 */
	public IncontroDiMissione aggirabile() {
		this.aggirabile = true;
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
			avversario.setFinoAllaResa(finoAllaResa);
			avversario.setSfidante(aDuello);
			avversario.setDaAffrontare(!aggirabile);
			avversari.add(avversario);
		}
		if (classeDelCapo != null) {
			Personaggio capo = classeDelCapo.getIstanza(livello + 1);
			capo.getModelloDati().setNome(nomeDelCapo);
			capo.setOrdinale(1);
			capo.setFinoAllaResa(finoAllaResa);
			capo.setDaAffrontare(!aggirabile);
			avversari.add(capo);
		}
		return avversari;
	}
}
