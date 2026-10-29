package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;

/**
 * L'abilità innata di Mago ed Elfo: un dardo di energia ARCANA su un solo bersaglio, che costa MAGIA ma non consuma pergamene, così chi vive di
 * magia ha sempre un modo di combattere. Al Mago rende di più e costa meno che all'Elfo (vedi {@link CostantiAbilitaInnate}); il resto lo fanno
 * l'INTELLIGENZA e il moltiplicatore magico di chi lo lancia. Chi lo conosce lo dice {@link AbilitaInnate}.
 */
public class DardoArcano extends AbilitaDiDanno {

	private final boolean delMago;

	/**
	 * Il dardo lanciato da quel personaggio, al suo livello
	 */
	public DardoArcano(Personaggio utilizzatore) {
		super(utilizzatore);
		this.delMago = isMago(utilizzatore.getClasse());
	}

	private static boolean isMago(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.MAGO || classe == TipoPersonaggio.MAGA;
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.DARDO_ARCANO;
	}

	@Override
	public int getCostoLancio() {
		return delMago ? CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO : CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_ELFO;
	}

	@Override
	public int getDanni() {
		return delMago ? CostantiAbilitaInnate.DARDO_ARCANO_DANNI_MAGO
				: CostantiAbilitaInnate.DARDO_ARCANO_DANNI_ELFO + CostantiAbilitaInnate.DARDO_ARCANO_DANNI_ELFO_PER_LIVELLO * (getLivello() - 1);
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ARCANO;
	}

	/**
	 * Il dardo colpisce un solo bersaglio.
	 */
	@Override
	protected int getNumeroBersagli(Personaggio utilizzatore) {
		return 1;
	}

	@Override
	protected String getAnnuncio(List<Personaggio> bersagli) {
		return "scaglia un dardo arcano contro "
				+ bersagli.get(0).getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + ".";
	}
}
