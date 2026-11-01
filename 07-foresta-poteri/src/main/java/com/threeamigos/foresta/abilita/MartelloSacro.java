package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoDanno;

import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'abilità innata di Chierico e Chierica: un colpo di martello benedetto su un solo avversario, di danno SACRO (il doppio contro i non morti, per regola di
 * categoria, come la luce divina del Sacerdote), con danni propri (ABILITA_MARTELLO_SACRO_DANNI) e un costo di Magia (ABILITA_MARTELLO_SACRO_COSTO_LANCIO).
 * Chi la conosce lo dice {@link AbilitaInnate}.
 */
public class MartelloSacro extends AbilitaDiDanno {

	public MartelloSacro(Personaggio utilizzatore) {
		super(utilizzatore);
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.MARTELLO_SACRO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.SACRO;
	}

	@Override
	public int getDanni() {
		return CostantiAbilitaInnate.ABILITA_MARTELLO_SACRO_DANNI;
	}

	/**
	 * Un solo avversario, sempre.
	 */
	@Override
	protected int getNumeroBersagli(Personaggio utilizzatore) {
		return 1;
	}

	@Override
	protected String getAnnuncio(Personaggio utilizzatore, List<Personaggio> bersagli) {
		return Testi.formatta("messaggi_poteri", "MartelloSacro.annuncio", chi(utilizzatore),
				bersagli.get(0).getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
	}
}
