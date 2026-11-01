package com.threeamigos.foresta.abilita;

import java.util.List;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'abilità del Druido che evoca uno sciame di insetti velenosi: danno di Veleno, che avvelena (vedi TipoDanno.VELENO) (vedi {@link AbilitaDiDanno}): danni e costo in ABILITA_SCIAME_*.
 */
public class Sciame extends AbilitaDiDanno {

	public Sciame(Personaggio utilizzatore) {
		super(utilizzatore);
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
	public TipoDanno getTipoDanno() {
		return TipoDanno.VELENO;
	}

	@Override
	public int getDanni() {
		return CostantiAbilitaInnate.ABILITA_SCIAME_DANNI;
	}

	@Override
	protected String getAnnuncio(Personaggio utilizzatore, List<Personaggio> bersagli) {
		return Testi.formatta("messaggi_poteri", "Sciame.annuncio", chi(utilizzatore));
	}
}
