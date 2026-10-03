package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 * I trofei che il capitano delle guardie chiede come prova della caccia (vedi CacciaAiTrofei): a quale mostro si
 * prendono e come si chiamano. Mostri che si incontrano in più tipi di locazione, così la caccia non dura per sempre.
 */
public enum TrofeoDiCaccia {

	ORECCHIE_DI_GOBLIN(ClassePersonaggio.GOBLIN, "i goblin", true, "orecchia di goblin", "orecchie di goblin"),
	DENTI_DI_HOBGOBLIN(ClassePersonaggio.HOBGOBLIN, "gli hobgoblin", false, "dente di hobgoblin", "denti di hobgoblin"),
	TESCHI_DI_SCHELETRO(ClassePersonaggio.SCHELETRO, "gli scheletri", false, "teschio di scheletro", "teschi di scheletro"),
	CORNI_DI_MINOTAURO(ClassePersonaggio.MINOTAURO, "i minotauri", false, "corno di minotauro", "corni di minotauro"),
	PIUME_DI_ARPIA(ClassePersonaggio.ARPIA, "le arpie", true, "piuma di arpia", "piume di arpia"),
	DENTI_DI_TROLL(ClassePersonaggio.TROLL, "i troll", false, "dente di troll", "denti di troll");

	private final ClassePersonaggio nemico;
	private final String nemici;
	private final boolean femminile;
	private final NomeOggetto nome;

	TrofeoDiCaccia(ClassePersonaggio nemico, String nemici, boolean femminile, String singolare, String plurale) {
		this.nemico = nemico;
		this.nemici = nemici;
		this.femminile = femminile;
		this.nome = femminile ? NomeOggetto.femminile(singolare, plurale) : NomeOggetto.maschile(singolare, plurale);
	}

	public ClassePersonaggio getNemico() {
		return nemico;
	}

	/**
	 * I mostri, al plurale e con l'articolo: "i goblin", "le arpie".
	 */
	public String getNemici() {
		return nemici;
	}

	public NomeOggetto getNome() {
		return nome;
	}

	public boolean isFemminile() {
		return femminile;
	}
}
