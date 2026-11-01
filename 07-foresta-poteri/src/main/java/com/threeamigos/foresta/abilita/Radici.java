package com.threeamigos.foresta.abilita;

import java.util.List;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'abilità del Druido che fa spuntare radici contro gli avversari: danno di Terra, che immobilizza o acceca (vedi TipoDanno.TERRA) (vedi {@link AbilitaDiDanno}): danni e costo in ABILITA_RADICI_*.
 */
public class Radici extends AbilitaDiDanno {

	public Radici(Personaggio utilizzatore) {
		super(utilizzatore);
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.RADICI;
	}

	@Override
	protected TipoIncantesimo getTipoIncantesimo() {
		return TipoIncantesimo.TERRA;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.TERRA;
	}

	@Override
	public int getDanni() {
		return CostantiAbilitaInnate.ABILITA_RADICI_DANNI;
	}

	@Override
	protected String getAnnuncio(Personaggio utilizzatore, List<Personaggio> bersagli) {
		return Testi.formatta("messaggi_poteri", "Radici.annuncio", chi(utilizzatore));
	}
}
