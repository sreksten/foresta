package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Arma;
import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Collection;
import java.util.Collections;

/**
 * L'incantesimo innato di Mago ed Elfo: un dardo di energia ARCANA su un solo bersaglio, che costa MAGIA ma non
 * consuma pergamene, così chi vive di magia ha sempre un modo di combattere. Al Mago rende di più e costa meno
 * che all'Elfo (Costanti.DARDO_ARCANO_*); il resto lo fanno l'INTELLIGENZA e il moltiplicatore magico di chi lo lancia.
 * <p>
 * Non è fra le {@link ClasseIncantesimo}, perché non è una pergamena: non si compra, non si trova e non si conta
 * nell'inventario del gruppo.
 */
public class DardoArcano implements Arma {

	private final int livello;
	private final boolean delMago;

	/**
	 * Il dardo lanciato da quel personaggio, al suo livello
	 */
	public DardoArcano(Personaggio formulante) {
		this.livello = formulante.getLivello();
		this.delMago = isMago(formulante.getClasse());
	}

	private static boolean isMago(ClassePersonaggio classe) {
		return classe == ClassePersonaggio.MAGO || classe == ClassePersonaggio.MAGA;
	}

	/**
	 * @return true se la classe sa lanciare il dardo arcano (Mago/Maga, Elfo/Elfa)
	 */
	public static boolean conosciutoDa(ClassePersonaggio classe) {
		switch (classe) {
			case MAGO:
			case MAGA:
			case ELFO:
			case ELFA:
				return true;
			default:
				return false;
		}
	}

	/**
	 * @return true se il personaggio è vivo, conosce il dardo arcano e ha abbastanza MAGIA per lanciarlo
	 */
	public static boolean puoLanciarlo(Personaggio personaggio) {
		return personaggio.isVivo() && conosciutoDa(personaggio.getClasse())
				&& personaggio.getMagia() >= costoLancio(personaggio.getClasse());
	}

	private static int costoLancio(ClassePersonaggio classe) {
		return isMago(classe) ? Costanti.DARDO_ARCANO_COSTO_LANCIO_MAGO : Costanti.DARDO_ARCANO_COSTO_LANCIO_ELFO;
	}

	public int getCostoLancio() {
		return delMago ? Costanti.DARDO_ARCANO_COSTO_LANCIO_MAGO : Costanti.DARDO_ARCANO_COSTO_LANCIO_ELFO;
	}

	@Override
	public int getDanni() {
		return delMago ? Costanti.DARDO_ARCANO_DANNI_MAGO : Costanti.DARDO_ARCANO_DANNI_ELFO;
	}

	@Override
	public int getLivello() {
		return livello;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.ARCANO;
	}

	@Override
	public boolean isIncantata() {
		return false;
	}

	@Override
	public Collection<Incantamento> getIncantamenti() {
		return Collections.emptyList();
	}
}
