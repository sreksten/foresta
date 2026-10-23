package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.incantesimi.AlbaSacra;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.LuceDivina;
import com.threeamigos.foresta.tipi.AbilitaInnataDiClasse;
import com.threeamigos.foresta.tipi.Testi;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Le capacità innate di una classe, tutte insieme e con i loro testi, per il bestiario: le abilità del Chierico e del Druido, i canti del Bardo,
 * l'Alba Sacra e la Luce Divina del Sacerdote, il dardo arcano di Mago ed Elfo. Ognuna sa già chi la conosce (AbilitaInnate, CantoDelBardo, AlbaSacra,
 * LuceDivina, DardoArcano): qui si raccolgono. L'Urlo della Banshee, che si impara per una missione, non c'è.
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
		if (AlbaSacra.conosciutaDa(classe)) {
			abilita.add(daIncantesimo(TipoIncantesimo.ALBA_SACRA));
		}
		if (LuceDivina.conosciutaDa(classe)) {
			abilita.add(daIncantesimo(TipoIncantesimo.LUCE_DIVINA));
		}
		if (DardoArcano.conosciutoDa(classe)) {
			abilita.add(new AbilitaInnataDiClasse(Testi.get("caratteristica_di_classe", "ABILITA.DARDO_ARCANO.NOME").orElse("DARDO_ARCANO"),
					Testi.get("caratteristica_di_classe", "ABILITA.DARDO_ARCANO.DESCRIZIONE").orElse("")));
		}
		return Collections.unmodifiableList(abilita);
	}

	private static AbilitaInnataDiClasse daIncantesimo(TipoIncantesimo incantesimo) {
		return new AbilitaInnataDiClasse(incantesimo.getNome(), incantesimo.getEffetto());
	}
}
