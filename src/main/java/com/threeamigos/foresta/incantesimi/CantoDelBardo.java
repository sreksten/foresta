package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Chi sa cantare e quali canti può cantare adesso. Le capacità dipendono dal {@link TipoPersonaggio}, non dalla classe
 * Java: nessun altro personaggio, nemmeno il Viandante, canta.
 */
public final class CantoDelBardo {

	private CantoDelBardo() {
	}

	/**
	 * @return true se la classe sa cantare (Bardo, Cantastorie e il bardo della locanda, che è un bardo a tutti gli effetti)
	 */
	public static boolean conosciutoDa(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.BARDO || classe == TipoPersonaggio.CANTASTORIE
				|| classe == TipoPersonaggio.BARDO_LOCANDA;
	}

	/**
	 * @return true se il personaggio è vivo, sa cantare, non è {@code SILENZIATO} e ha la MAGIA per quel canto
	 */
	public static boolean puoCantare(Personaggio personaggio, TipoCanto tipo) {
		return personaggio.isVivo() && conosciutoDa(personaggio.getClasse())
				&& !personaggio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO)
				&& personaggio.getMagia() >= FabbricaCanti.crea(tipo, personaggio).getCostoLancio();
	}

	/**
	 * I canti che il personaggio può cantare adesso, nell'ordine di {@link TipoCanto}
	 */
	public static List<TipoCanto> cantiPossibili(Personaggio personaggio) {
		List<TipoCanto> canti = new ArrayList<>();
		for (TipoCanto tipo : TipoCanto.values()) {
			if (puoCantare(personaggio, tipo)) {
				canti.add(tipo);
			}
		}
		return canti;
	}
}
