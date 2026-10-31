package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.tipi.AbilitaInnataDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le capacità innate di ogni classe, raccolte con i loro testi per il bestiario.
 */
class AbilitaInnateDiClasseTest {

	private static List<String> nomi(TipoPersonaggio classe) {
		return AbilitaInnateDiClasse.di(classe).stream().map(AbilitaInnataDiClasse::getNome).collect(Collectors.toList());
	}

	@Test
	void ilChiericoHaLeSueTreAbilitaEIlDruidoLeSue() {
		assertEquals(3, nomi(TipoPersonaggio.CHIERICO).size(), nomi(TipoPersonaggio.CHIERICO).toString());
		assertTrue(nomi(TipoPersonaggio.CHIERICA).contains("Imposizione delle mani"));
		assertTrue(nomi(TipoPersonaggio.CHIERICA).contains("Martello sacro"));
		assertEquals(4, nomi(TipoPersonaggio.DRUIDO).size(), nomi(TipoPersonaggio.DRUIDO).toString());
		assertTrue(nomi(TipoPersonaggio.DRUIDA).contains("Pelle di corteccia"));
	}

	@Test
	void ilBardoHaITreCantiDiSempreEIlSacerdoteLAlbaSacraELaLuceDivina() {
		assertEquals(3, nomi(TipoPersonaggio.BARDO).size(), nomi(TipoPersonaggio.BARDO).toString());
		assertEquals(nomi(TipoPersonaggio.BARDO), nomi(TipoPersonaggio.CANTASTORIE));
		assertEquals(2, nomi(TipoPersonaggio.SACERDOTE).size(), nomi(TipoPersonaggio.SACERDOTE).toString());
		assertTrue(nomi(TipoPersonaggio.SACERDOTESSA).contains("Luce Divina"));
	}

	@Test
	void magoEElfoHannoIlDardoArcano() {
		assertEquals(1, nomi(TipoPersonaggio.MAGO).size());
		assertEquals("Dardo arcano", nomi(TipoPersonaggio.ELFA).get(0));
		assertFalse(AbilitaInnateDiClasse.di(TipoPersonaggio.MAGA).get(0).getDescrizione().isEmpty());
	}

	@Test
	void ilLadroHaLAttaccoAlleSpalle() {
		assertEquals(1, nomi(TipoPersonaggio.LADRO).size());
		assertEquals("Attacco alle spalle", nomi(TipoPersonaggio.LADRA).get(0));
	}

	@Test
	void chiNonHaCapacitaInnateNonNeHaNeppureNelBestiario() {
		assertTrue(nomi(TipoPersonaggio.GUERRIERO).isEmpty());
		assertTrue(nomi(TipoPersonaggio.GOBLIN).isEmpty());
	}

	@Test
	void ogniCapacitaHaUnNomeEUnaDescrizione() {
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			for (AbilitaInnataDiClasse abilita : AbilitaInnateDiClasse.di(classe)) {
				assertFalse(abilita.getNome().isEmpty(), classe.name());
				assertTrue(abilita.getDescrizione().length() > 10, classe + " " + abilita.getNome());
			}
		}
	}
}
