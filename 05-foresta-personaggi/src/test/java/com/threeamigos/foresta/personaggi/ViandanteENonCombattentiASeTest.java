package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.gruppi.DatiDiProva;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il Viandante e i personaggi non combattenti non sono Bardi (nessuna capacità del Bardo gli arriva per eredità), ma
 * all'inizio hanno le stesse caratteristiche di prima (vedi personaggi_giocabili.md).
 */
class ViandanteENonCombattentiASeTest {

	private static final TipoAttributo[] PRIMARI = {TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE,
			TipoAttributo.INTELLIGENZA, TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA};

	@BeforeEach
	void preparaModelloDati() {
		DatiDiProva.nuovaPartita();
	}

	@Test
	void soloIlBardoELaSuaVarianteDellaLocandaSonoBardi() {
		assertFalse(Bardo.class.isAssignableFrom(Viandante.class));
		assertFalse(Bardo.class.isAssignableFrom(Locandiere.class));
		assertFalse(Bardo.class.isAssignableFrom(MoglieDelBardo.class));
		assertFalse(Bardo.class.isAssignableFrom(Armaiolo.class));
		assertFalse(Bardo.class.isAssignableFrom(Alchimista.class));
		assertFalse(Bardo.class.isAssignableFrom(VenditoreDiPergamene.class));
		assertFalse(Bardo.class.isAssignableFrom(Incantatore.class));
		assertTrue(Bardo.class.isAssignableFrom(Bardo.class));
		assertTrue(Bardo.class.isAssignableFrom(BardoLocanda.class));
		assertTrue(PersonaggioBase.class.isAssignableFrom(Viandante.class));
		assertTrue(PersonaggioNonCombattente.class.isAssignableFrom(Locandiere.class));
	}

	@Test
	void ilViandanteHaLeCaratteristicheDelBardoDiPrima() {
		Bardo bardo = new Bardo("Orfeo", 4);
		Viandante viandante = new Viandante("Anselmo", 4);
		for (TipoAttributo attributo : PRIMARI) {
			assertEquals(bardo.getMaxStatistica(attributo), viandante.getMaxStatistica(attributo), attributo.name());
		}
		assertEquals(bardo.getSaluteBase(), viandante.getSaluteBase());
		assertEquals(bardo.getMagiaBase(), viandante.getMagiaBase());
		assertEquals(bardo.getLivellamentoMagia(), viandante.getLivellamentoMagia());
		assertEquals(bardo.getMoltiplicatoreSoggezione(), viandante.getMoltiplicatoreSoggezione());
		assertEquals(bardo.getMoltiplicatoreDanniFisici(), viandante.getMoltiplicatoreDanniFisici());
		assertEquals(bardo.getMoltiplicatoreRecuperoMagico(), viandante.getMoltiplicatoreRecuperoMagico());
		assertEquals(bardo.getPuntiEsperienza(), viandante.getPuntiEsperienza());
		assertEquals(TipoPersonaggio.VIANDANTE, viandante.getClasse());
		assertEquals("Viandante", viandante.getNomeSingolare());
		assertTrue(viandante.isAmichevole());
		assertTrue(viandante.isCorrompibile());
	}

	@Test
	void iNonCombattentiHannoLeStatisticheDelBardoPerZeroVirgolaOttantacinque() {
		Bardo bardo = new Bardo("Orfeo", 4);
		Locandiere locandiere = new Locandiere("Gigio", 4);
		for (TipoAttributo attributo : PRIMARI) {
			assertEquals(bardo.getMaxStatistica(attributo) * 0.85, locandiere.getMaxStatistica(attributo), 1e-9, attributo.name());
		}
		assertEquals(90, locandiere.getSaluteBase());
		assertEquals(30, locandiere.getMagiaBase());
		assertEquals(14, locandiere.getLivellamentoMagia());
		assertEquals(bardo.getMoltiplicatoreContrattazione(), locandiere.getMoltiplicatoreContrattazione());
		assertFalse(locandiere.isAmichevole());
		assertFalse(locandiere.isCorrompibile());
		assertEquals(Personaggio.Sesso.MASCHIO, locandiere.getSesso());
		assertEquals(Personaggio.Sesso.FEMMINA, new MoglieDelBardo("Rosina", 4).getSesso());
	}
}
