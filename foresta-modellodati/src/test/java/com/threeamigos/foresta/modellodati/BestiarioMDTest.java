package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BestiarioMDTest {

	private static List<String> righe(BestiarioMD bestiario) throws IOException {
		StringWriter scrittura = new StringWriter();
		try (PrintWriter writer = new PrintWriter(scrittura)) {
			bestiario.salva(writer);
		}
		return scrittura.toString().isEmpty() ? java.util.Collections.emptyList() : Arrays.asList(scrittura.toString().split("\\R"));
	}

	@Test
	void unBestiarioNuovoNonHaVociSbloccateNeCaratteristicheScoperte() {
		BestiarioMD bestiario = new BestiarioMD();
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			assertFalse(bestiario.isSbloccata(classe));
			assertTrue(bestiario.getScoperte(classe).isEmpty());
		}
	}

	@Test
	void unaVoceEUnRuoloSbloccareLaMagaSbloccaIlMago() {
		BestiarioMD bestiario = new BestiarioMD();

		assertTrue(bestiario.sblocca(TipoPersonaggio.MAGA));

		assertTrue(bestiario.isSbloccata(TipoPersonaggio.MAGO));
		assertTrue(bestiario.isSbloccata(TipoPersonaggio.MAGA));
		assertFalse(bestiario.sblocca(TipoPersonaggio.MAGO), "è la stessa voce: la seconda volta non è una novità");
		assertEquals(1, bestiario.getSbloccate().size());
		assertTrue(bestiario.getSbloccate().contains(TipoPersonaggio.MAGO), "si ricorda con la classe al maschile");
	}

	@Test
	void leCaratteristicheScopertePassanoDaUnaFormaALAltraEUnaVoltaSolaNonSiRipetono() {
		BestiarioMD bestiario = new BestiarioMD();

		assertTrue(bestiario.scopri(TipoPersonaggio.CHIERICA, "DANNO:NECROTICO"));

		assertTrue(bestiario.isScoperta(TipoPersonaggio.CHIERICO, "DANNO:NECROTICO"));
		assertFalse(bestiario.scopri(TipoPersonaggio.CHIERICO, "DANNO:NECROTICO"));
		assertEquals(1, bestiario.getScoperte(TipoPersonaggio.CHIERICA).size());
	}

	@Test
	void salvaUnaRigaPerVoceCheHaQualcosaDaDireConNomeSbloccataECaratteristiche() throws IOException {
		BestiarioMD bestiario = new BestiarioMD();
		bestiario.sblocca(TipoPersonaggio.SCHELETRO);
		bestiario.scopri(TipoPersonaggio.SCHELETRO, "DANNO:CONTUNDENTE");
		bestiario.scopri(TipoPersonaggio.SCHELETRO, "INCANTESIMO:MORTE");
		// Scoperta, ma la voce non è ancora sbloccata: si ricorda lo stesso
		bestiario.scopri(TipoPersonaggio.DRAGO, "DANNO:FUOCO");

		List<String> righe = righe(bestiario);

		assertEquals(2, righe.size(), righe.toString());
		assertTrue(righe.contains("SCHELETRO|true|DANNO:CONTUNDENTE§INCANTESIMO:MORTE"), righe.toString());
		assertTrue(righe.contains("DRAGO|false|DANNO:FUOCO"), righe.toString());
	}

	@Test
	void rileggeQuelloCheHaScritto() throws IOException {
		BestiarioMD bestiario = new BestiarioMD();
		bestiario.sblocca(TipoPersonaggio.SCHELETRO);
		bestiario.sblocca(TipoPersonaggio.ELFA);
		bestiario.scopri(TipoPersonaggio.SCHELETRO, "EFFETTO:SANGUINAMENTO");
		bestiario.scopri(TipoPersonaggio.DRAGO, "DANNO:FUOCO");
		StringWriter scrittura = new StringWriter();
		try (PrintWriter writer = new PrintWriter(scrittura)) {
			bestiario.salva(writer);
		}

		BestiarioMD riletto = new BestiarioMD();
		riletto.leggi(new BufferedReader(new StringReader(scrittura.toString())));

		assertTrue(riletto.isSbloccata(TipoPersonaggio.SCHELETRO));
		assertTrue(riletto.isSbloccata(TipoPersonaggio.ELFO));
		assertFalse(riletto.isSbloccata(TipoPersonaggio.DRAGO));
		assertTrue(riletto.isScoperta(TipoPersonaggio.SCHELETRO, "EFFETTO:SANGUINAMENTO"));
		assertTrue(riletto.isScoperta(TipoPersonaggio.DRAGO, "DANNO:FUOCO"));
	}

	@Test
	void unFileVuotoNonHaVociNeScoperteELaLetturaSostituisceIlContenutoDiPrima() throws IOException {
		BestiarioMD bestiario = new BestiarioMD();
		bestiario.sblocca(TipoPersonaggio.GOBLIN);

		bestiario.leggi(new BufferedReader(new StringReader("")));

		assertFalse(bestiario.isSbloccata(TipoPersonaggio.GOBLIN));
	}

	@Test
	void unaClasseCheNonEsistePiuSiSaltaSenzaRovinareIlCaricamento() throws IOException {
		BestiarioMD bestiario = new BestiarioMD();

		bestiario.leggi(new BufferedReader(new StringReader("MOSTRO_TOLTO|true|DANNO:FUOCO\n\nGOBLIN|true|\n")));

		assertTrue(bestiario.isSbloccata(TipoPersonaggio.GOBLIN));
		assertTrue(bestiario.getScoperte(TipoPersonaggio.GOBLIN).isEmpty());
	}
}
