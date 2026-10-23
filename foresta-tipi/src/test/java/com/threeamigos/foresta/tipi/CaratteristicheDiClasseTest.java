package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le caratteristiche generali delle classi (debolezze, resistenze, immunità) e le voci del bestiario che le raccontano.
 */
class CaratteristicheDiClasseTest {

	private static Set<String> chiavi(TipoPersonaggio classe) {
		return classe.getCaratteristiche().stream().map(CaratteristicaDiClasse::getChiave).collect(Collectors.toSet());
	}

	@Test
	void loScheletroEImmuneAMorteEVelenoAlSanguinamentoEAllInfezioneAlloSpavento() {
		Set<String> chiavi = chiavi(TipoPersonaggio.SCHELETRO);

		assertTrue(chiavi.contains("INCANTESIMO:MORTE"), chiavi.toString());
		assertTrue(chiavi.contains("INCANTESIMO:VELENO"), chiavi.toString());
		assertTrue(chiavi.contains("EFFETTO:SANGUINAMENTO"), chiavi.toString());
		assertTrue(chiavi.contains("EFFETTO:INFETTATO"), chiavi.toString());
		assertTrue(chiavi.contains("EFFETTO:AVVELENATO"), chiavi.toString());
		assertTrue(chiavi.contains("EFFETTO:SPAVENTATO"), chiavi.toString());
		assertTrue(chiavi.contains("DANNO:CONTUNDENTE"), "le ossa si spezzano a bastonate: " + chiavi);
		assertFalse(chiavi.contains("INCANTESIMO:FUOCO"), chiavi.toString());
	}

	@Test
	void ilGargoyleEImmuneATuttiGliIncantesimiEQuestoESolamenteUnaCaratteristica() {
		List<CaratteristicaDiClasse> caratteristiche = TipoPersonaggio.GARGOYLE.getCaratteristiche();

		assertTrue(TipoPersonaggio.GARGOYLE.isImmuneATuttiGliIncantesimi());
		assertEquals(1, caratteristiche.stream().filter(c -> c.getGenere() == CaratteristicaDiClasse.Genere.INCANTESIMO).count());
		assertTrue(chiavi(TipoPersonaggio.GARGOYLE).contains("INCANTESIMO:TUTTI"));
		for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
			assertTrue(TipoPersonaggio.GARGOYLE.isImmuneAIncantesimo(incantesimo), incantesimo.name());
		}
	}

	@Test
	void unGoblinNonHaNienteDiParticolare() {
		assertTrue(TipoPersonaggio.GOBLIN.getCaratteristiche().isEmpty(), TipoPersonaggio.GOBLIN.getCaratteristiche().toString());
	}

	@Test
	void leImmunitaDellaClasseSonoQuelleDiSempre() {
		// Le regole che stavano scritte a mano nelle classi dei personaggi
		assertTrue(TipoPersonaggio.LICH.isImmuneAEffetto(TipoEffettoDiStato.SILENZIATO));
		assertTrue(TipoPersonaggio.LICH.isImmuneAIncantesimo(TipoIncantesimo.MORTE), "i non morti, dalla categoria");
		assertFalse(TipoPersonaggio.LICH.isImmuneAIncantesimo(TipoIncantesimo.FUOCO));
		assertTrue(TipoPersonaggio.DRAGO.isImmuneAIncantesimo(TipoIncantesimo.FUOCO));
		assertTrue(TipoPersonaggio.DRAGO.isImmuneAEffetto(TipoEffettoDiStato.BRUCIATO));
		assertTrue(TipoPersonaggio.IDRA.isImmuneAEffetto(TipoEffettoDiStato.STORDITO));
		assertFalse(TipoPersonaggio.IDRA.isImmuneAEffetto(TipoEffettoDiStato.SANGUINAMENTO), "l'idra non è un non morto");
		assertTrue(TipoPersonaggio.STREGA.isImmuneAIncantesimo(TipoIncantesimo.ACQUA));
		assertTrue(TipoPersonaggio.TROLL.isImmuneAIncantesimo(TipoIncantesimo.TERRA));
		assertTrue(TipoPersonaggio.ARPIA.isImmuneAIncantesimo(TipoIncantesimo.ARIA));
		assertTrue(TipoPersonaggio.VIVERNA.isImmuneAIncantesimo(TipoIncantesimo.FUOCO));
		for (TipoPersonaggio fantasma : new TipoPersonaggio[]{TipoPersonaggio.FANTASMA, TipoPersonaggio.OMBRA_NERA, TipoPersonaggio.SPETTRO, TipoPersonaggio.SPIRITO}) {
			assertTrue(fantasma.isImmuneAIncantesimo(TipoIncantesimo.ARIA), fantasma.name());
			assertTrue(fantasma.isImmuneAIncantesimo(TipoIncantesimo.TERRA), fantasma.name());
			assertTrue(fantasma.isImmuneAEffetto(TipoEffettoDiStato.ATTERRATO), fantasma.name());
		}
		// Il Guerriero non si spaventa solo in berserk: non è una caratteristica della classe
		assertFalse(TipoPersonaggio.GUERRIERO.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO));
	}

	@Test
	void ilMoltiplicatoreDelDannoEUnaCaratteristicaSoloSeNonEIlNormale() {
		CaratteristicaDiClasse fuoco = TipoPersonaggio.DRAGO.getCaratteristiche().stream()
				.filter(c -> c.getChiave().equals("DANNO:FUOCO")).findFirst().orElseThrow(AssertionError::new);
		CaratteristicaDiClasse gelo = TipoPersonaggio.DRAGO.getCaratteristiche().stream()
				.filter(c -> c.getChiave().equals("DANNO:GELO")).findFirst().orElseThrow(AssertionError::new);

		assertTrue(fuoco.isImmunita());
		assertEquals(1.5d, gelo.getMoltiplicatore(), 0.0001d);
		assertFalse(gelo.isImmunita());
	}

	@Test
	void leCaratteristicheSiScrivonoAParole() {
		assertEquals("Immune all'incantesimo di Morte", CaratteristicaDiClasse.immuneAIncantesimo(TipoIncantesimo.MORTE).getDescrizione());
		assertEquals("Immune a ogni incantesimo", CaratteristicaDiClasse.immuneATuttiGliIncantesimi().getDescrizione());
		assertEquals("Immune all'effetto \"Sanguinamento\"", CaratteristicaDiClasse.immuneAEffetto(TipoEffettoDiStato.SANGUINAMENTO).getDescrizione());
		assertEquals("Immune ai danni di tipo Fuoco", CaratteristicaDiClasse.danno(TipoDanno.FUOCO, 0.0d).getDescrizione());
		assertEquals("Debole ai danni di tipo Gelo (x1,5)", CaratteristicaDiClasse.danno(TipoDanno.GELO, 1.5d).getDescrizione());
		assertEquals("Resiste ai danni di tipo Gelo (x0,5)", CaratteristicaDiClasse.danno(TipoDanno.GELO, 0.5d).getDescrizione());
		assertEquals("Resiste ai danni di tipo " + TipoDanno.TAGLIENTE.getNome() + " (x0,75)",
				CaratteristicaDiClasse.danno(TipoDanno.TAGLIENTE, 0.75d).getDescrizione());
	}

	@Test
	void ogniCaratteristicaHaUnaChiaveECheNonCambiaConLaLingua() {
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			for (CaratteristicaDiClasse caratteristica : classe.getCaratteristiche()) {
				assertTrue(caratteristica.getChiave().matches("(DANNO|INCANTESIMO|EFFETTO):[A-Z_]+"), caratteristica.getChiave());
				assertFalse(caratteristica.getDescrizione().isEmpty());
				// Un testo che manca in un file si vedrebbe come la chiave grezza (la cerca Testi.get e, senza, usa la chiave)
				assertFalse(caratteristica.getDescrizione().startsWith("DANNO.") || caratteristica.getDescrizione().startsWith("EFFETTO.")
						|| caratteristica.getDescrizione().startsWith("INCANTESIMO."), caratteristica.getDescrizione());
			}
		}
	}

	// --- Le voci del bestiario

	@Test
	void leVociSonoUnaPerRuoloEPartonoDaiMostriEDaiGiocanti() {
		List<TipoPersonaggio> voci = TipoPersonaggio.getVociDelBestiario();

		assertEquals(32, voci.size(), voci.toString());
		assertEquals(voci.size(), new HashSet<>(voci).size(), "nessuna voce ripetuta");
		for (TipoPersonaggio voce : voci) {
			assertEquals(voce, voce.getRuolo(), voce + " non è la classe al maschile del suo ruolo");
			assertTrue(voce.hasVoceNelBestiario(), voce.name());
		}
		assertTrue(voci.contains(TipoPersonaggio.MAGO));
		assertFalse(voci.contains(TipoPersonaggio.MAGA), "Mago e Maga sono la stessa voce");
		assertTrue(voci.contains(TipoPersonaggio.BARDO));
		assertTrue(voci.contains(TipoPersonaggio.CHIERICO));
		assertTrue(voci.contains(TipoPersonaggio.DRUIDO));
		assertTrue(voci.contains(TipoPersonaggio.GOBLIN));
	}

	@Test
	void iNonCombattentiELOmbraFiammaNonHannoUnaVoce() {
		for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.LOCANDIERE, TipoPersonaggio.ARMAIOLO, TipoPersonaggio.ALCHIMISTA,
				TipoPersonaggio.VENDITORE_DI_PERGAMENE, TipoPersonaggio.INCANTATORE, TipoPersonaggio.MOGLIE_DEL_BARDO,
				TipoPersonaggio.BARDO_DELLE_LOCANDE, TipoPersonaggio.CAPITANO_DELLE_GUARDIE, TipoPersonaggio.UOMO, TipoPersonaggio.BORGOMASTRO,
				TipoPersonaggio.DONNA, TipoPersonaggio.VIANDANTE, TipoPersonaggio.OMBRAFIAMMA}) {
			assertFalse(classe.hasVoceNelBestiario(), classe.name());
		}
	}

	@Test
	void chiPuoEntrareNelGruppoEChiSiIncontraComeNemicoSonoDueCose() {
		assertTrue(TipoPersonaggio.CHIERICO.isCompagno());
		assertFalse(TipoPersonaggio.CHIERICO.isAvversario(), "oggi nessuna missione mette un chierico fra i nemici");
		assertTrue(TipoPersonaggio.GUERRIERO.isCompagno());
		assertTrue(TipoPersonaggio.GUERRIERO.isAvversario(), "i campioni dei tornei sono guerrieri");
		assertTrue(TipoPersonaggio.GOBLIN.isAvversario());
		assertFalse(TipoPersonaggio.GOBLIN.isCompagno());
	}

	@Test
	void leFormeFemminiliSiTrovanoDalMaschile() {
		assertEquals(TipoPersonaggio.MAGA, TipoPersonaggio.MAGO.getFormaFemminile());
		assertEquals(TipoPersonaggio.CANTASTORIE, TipoPersonaggio.BARDO.getFormaFemminile());
		assertEquals(TipoPersonaggio.SACERDOTESSA, TipoPersonaggio.SACERDOTE.getFormaFemminile());
		assertNull(TipoPersonaggio.GOBLIN.getFormaFemminile());
		assertEquals("Mago e Maga", TipoPersonaggio.MAGA.getNomeDellaVoce());
		assertEquals("Goblin", TipoPersonaggio.GOBLIN.getNomeDellaVoce());
	}

	@Test
	void ogniVoceHaLaSuaDescrizioneEPerLaFormaFemminileEQuellaDelMaschile() {
		for (TipoPersonaggio voce : TipoPersonaggio.getVociDelBestiario()) {
			assertTrue(Testi.testo(voce, "DESCRIZIONE").isPresent(), "manca la descrizione di " + voce);
			assertTrue(voce.getDescrizione().length() > 40, voce.name());
		}
		assertEquals(TipoPersonaggio.MAGO.getDescrizione(), TipoPersonaggio.MAGA.getDescrizione());
	}
}
