package com.threeamigos.foresta.personaggi;


import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La guerriera: il Guerriero al femminile, con lo stesso ruolo e le stesse capacità (vedi {@link Guerriero}).
 */
public class Guerriera extends PersonaggioBase implements Personaggio {

	public Guerriera(int livello) {
		super(TipoPersonaggio.GUERRIERA, livello);
	}

	public Guerriera(String nome, int livello) {
		super(nome, TipoPersonaggio.GUERRIERA, livello);
	}

	@Override
	protected boolean isImmuneAEffettoPerIstanza(TipoEffettoDiStato effettoStato) {
		return (effettoStato == TipoEffettoDiStato.CONFUSO ||
				effettoStato == TipoEffettoDiStato.SPAVENTATO) &&
				hasEffettoDiStato(TipoEffettoDiStato.BERSERK);
	}
}
