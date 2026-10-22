package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'abilità del Druido che evoca uno sciame di insetti velenosi: danno di Veleno, che avvelena (vedi TipoDanno.VELENO) (vedi {@link AbilitaDiDanno}): danni e costo in ABILITA_SCIAME_*.
 */
public class Sciame extends AbilitaDiDanno {

	public Sciame(Personaggio utilizzatore) {
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.SCIAME;
	}

	@Override
	protected TipoIncantesimo getTipoIncantesimo() {
		return TipoIncantesimo.VELENO;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.ABILITA_SCIAME_COSTO_LANCIO;
	}

	@Override
	protected TipoDanno getTipoDanno() {
		return TipoDanno.VELENO;
	}

	@Override
	protected int getDanni() {
		return Costanti.ABILITA_SCIAME_DANNI;
	}

	@Override
	protected String getAnnuncio() {
		return "evoca uno sciame di insetti velenosi.";
	}
}
