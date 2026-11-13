package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il guerriero giocante, al maschile: umanoide, corrompibile e amichevole. Quando è in berserk è immune a confusione e
 * spavento. Il femminile è la {@link Guerriera}.
 */
public class Guerriero extends PersonaggioBase implements Personaggio {

	public Guerriero(int livello) {
		super(TipoPersonaggio.GUERRIERO, livello);
	}

	public Guerriero(String nome, int livello) {
		super(nome, TipoPersonaggio.GUERRIERO, livello);
	}

	/**
	 * Per chi ha le caratteristiche del guerriero sotto un'altra classe (vedi CapitanoDelleGuardie).
	 */
	protected Guerriero(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}

	@Override
	protected boolean isImmuneAEffettoPerIstanza(TipoEffettoDiStato effettoStato) {
		return (effettoStato == TipoEffettoDiStato.CONFUSO ||
				effettoStato == TipoEffettoDiStato.SPAVENTATO) &&
				hasEffettoDiStato(TipoEffettoDiStato.BERSERK);
	}
}
