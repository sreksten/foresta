package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrofeiMDTest {

	@Test
	void salvaUnaRigaPerTrofeoConNomeVintoEProgresso() throws IOException {
		// Given
		TrofeiMD trofei = new TrofeiMD();
		trofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 1);
		trofei.incrementaProgresso(TipoTrofeo.SBEVAZZONE, 1);
		trofei.aggiungiVinto(TipoTrofeo.SBEVAZZONE);

		// When
		StringWriter scrittura = new StringWriter();
		try (PrintWriter writer = new PrintWriter(scrittura)) {
			trofei.salva(writer);
		}

		// Then
		List<String> righe = Arrays.asList(scrittura.toString().split("\\R"));
		assertEquals(TipoTrofeo.values().length, righe.size(), "una riga per trofeo");
		assertTrue(righe.contains("SBEVAZZONE|true|2"), righe.toString());
		assertTrue(righe.contains("AMMAZZAGOBLIN|false|0"), "anche i trofei mancanti hanno la loro riga");
	}

	@Test
	void rileggeITrofeiVintiEQuelliMancantiConIlLoroProgresso() throws IOException {
		// Given
		TrofeiMD riletto = new TrofeiMD();

		// When
		riletto.leggi(new BufferedReader(new StringReader("SBEVAZZONE|true|100\n")));

		// Then
		assertTrue(riletto.isVinto(TipoTrofeo.SBEVAZZONE));
		assertEquals(100, riletto.getProgresso(TipoTrofeo.SBEVAZZONE));

		// When
		riletto.leggi(new BufferedReader(new StringReader("SBEVAZZONE|false|37\n")));

		// Then
		assertFalse(riletto.isVinto(TipoTrofeo.SBEVAZZONE));
		assertEquals(37, riletto.getProgresso(TipoTrofeo.SBEVAZZONE));
	}

	@Test
	void unFileVuotoNonHaTrofeiVintiNeProgressi() throws IOException {
		TrofeiMD riletto = new TrofeiMD();
		riletto.leggi(new BufferedReader(new StringReader("")));
		for (TipoTrofeo trofeo : TipoTrofeo.values()) {
			assertFalse(riletto.isVinto(trofeo));
			assertEquals(0, riletto.getProgresso(trofeo));
		}
	}
}
