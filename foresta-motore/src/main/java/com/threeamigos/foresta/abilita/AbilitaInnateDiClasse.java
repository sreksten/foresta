package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.tipi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Le capacità innate di una classe, tutte insieme e con i loro testi, per il bestiario: le abilità del Chierico e del Druido, i canti del Bardo,
 * l'Alba Sacra e la Luce Divina del Sacerdote, il dardo arcano di Mago ed Elfo. Ognuna sa già chi la conosce (AbilitaInnate, CantoDelBardo, AlbaSacra,
 * AlbaSacra): qui si raccolgono. L'Urlo della Banshee, che si impara per una missione, non c'è.
 */
public final class AbilitaInnateDiClasse {

	private AbilitaInnateDiClasse() {
	}

	/**
	 * Le capacità innate di quella classe, vuoto per chi non ne ha
	 */
	public static List<AbilitaInnataDiClasse> di(TipoPersonaggio classe) {
		List<AbilitaInnataDiClasse> abilita = new ArrayList<>();
		for (TipoAbilita tipo : TipoAbilita.values()) {
			if (AbilitaInnate.conosciutaDa(classe, tipo)) {
				abilita.add(new AbilitaInnataDiClasse(tipo.getNome(), tipo.getDescrizione()));
			}
		}
		for (TipoCanto tipo : new TipoCanto[]{TipoCanto.CURATIVO, TipoCanto.IPNOTICO, TipoCanto.DI_VALORE}) {
			if (CantoDelBardo.conosciutoDa(classe, tipo)) {
				abilita.add(new AbilitaInnataDiClasse(tipo.getNome(), tipo.getDescrizione()));
			}
		}
		return Collections.unmodifiableList(abilita);
	}
}
