package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.MoltiplicatoreDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I moltiplicatori di classe con la loro nota, come li mostra il bestiario.
 */
class MoltiplicatoriDiClasseTest {

	private static MoltiplicatoreDiClasse di(List<MoltiplicatoreDiClasse> elenco, String chiave) {
		return elenco.stream().filter(m -> m.getChiave().equals(chiave)).findFirst().orElseThrow(() -> new AssertionError(chiave));
	}

	@Test
	void ogniVoceDelBestiarioHaTuttiIMoltiplicatoriEOgnunoHaLaSuaNota() {
		for (TipoPersonaggio voce : TipoPersonaggio.getVociDelBestiario()) {
			List<MoltiplicatoreDiClasse> moltiplicatori = MoltiplicatoriDiClasse.di(voce);

			assertEquals(19, moltiplicatori.size(), voce.name());
			for (MoltiplicatoreDiClasse moltiplicatore : moltiplicatori) {
				assertFalse(moltiplicatore.getNota().trim().isEmpty(), "manca la nota di " + voce + "." + moltiplicatore.getChiave());
				assertFalse(moltiplicatore.getNome().isEmpty(), voce + "." + moltiplicatore.getChiave());
				// Anche 0: l'arpia non sa contrattare
				assertTrue(moltiplicatore.getMoltiplicatore() >= 0.0d, voce + "." + moltiplicatore.getChiave());
			}
		}
	}

	@Test
	void iValoriSonoQuelliDichiaratiInCostantiEIDueRecuperiVengonoDaiMoltiplicatoriDiRecupero() {
		List<MoltiplicatoreDiClasse> bardo = MoltiplicatoriDiClasse.di(TipoPersonaggio.BARDO);

		assertEquals(CostantiPersonaggi.BARDO_MOLTIPLICATORE_RECUPERO_MAGICO, di(bardo, "RIGENERAZIONE_MAGIA").getMoltiplicatore(), 0.0001d);
		assertEquals(CostantiPersonaggi.BARDO_MOLTIPLICATORE_RECUPERO_FISICO, di(bardo, "RIGENERAZIONE_SALUTE").getMoltiplicatore(), 0.0001d);
		assertEquals(CostantiPersonaggi.BARDO_MOLTIPLICATORE_CARICO, di(bardo, "CARICO_MASSIMO").getMoltiplicatore(), 0.0001d);
		assertEquals("Danni magici", di(bardo, "DANNI_MAGICI").getNome());
		assertEquals("Carico massimo", di(bardo, "CARICO_MASSIMO").getNome());
	}

	@Test
	void ilValoreSiScriveSenzaZeriInutiliEConLaVirgola() {
		List<MoltiplicatoreDiClasse> chierico = MoltiplicatoriDiClasse.di(TipoPersonaggio.CHIERICO);

		assertEquals("1,3", di(chierico, "PARATA").getMoltiplicatoreInTesto());
		assertEquals("1", di(chierico, "CRITICO").getMoltiplicatoreInTesto());
		assertEquals(1, di(chierico, "PARATA").getSegno());
		assertEquals(0, di(chierico, "CRITICO").getSegno());
		assertEquals(-1, di(chierico, "FURTIVITA").getSegno(), "0,8");
	}
}
