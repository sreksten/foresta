package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoSuggerimento;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class SuggerimentiMDTest {

	private static String salvato(SuggerimentiMD md) throws IOException {
		StringWriter scritto = new StringWriter();
		PrintWriter stream = new PrintWriter(scritto);
		md.salva(stream);
		stream.flush();
		return scritto.toString();
	}

	private static SuggerimentiMD letto(String contenuto) throws IOException {
		SuggerimentiMD md = new SuggerimentiMD();
		md.leggi(new BufferedReader(new StringReader(contenuto)));
		return md;
	}

	@Test
	void salvaERilegge() throws IOException {
		SuggerimentiMD md = new SuggerimentiMD();
		md.setAttivo(false);
		md.segnaVisto(TipoSuggerimento.CORRUZIONE);
		md.segnaVisto(TipoSuggerimento.PRIMI_PASSI);
		SuggerimentiMD riletto = letto(salvato(md));
		assertFalse(riletto.isAttivo());
		assertEquals(md.getVisti(), riletto.getVisti());
	}

	@Test
	void unSuggerimentoCheNonEsistePiuSiSalta() throws IOException {
		SuggerimentiMD md = letto("true\nCOSTANTE_TOLTA\nMISSIONI\n");
		assertTrue(md.isAttivo());
		assertTrue(md.isVisto(TipoSuggerimento.MISSIONI));
		assertEquals(1, md.getVisti().size());
	}

	@Test
	void unFileVuotoDaIlTutorialAccesoESenzaVisti() throws IOException {
		SuggerimentiMD md = letto("");
		assertTrue(md.isAttivo());
		assertTrue(md.getVisti().isEmpty());
	}

	@Test
	void segnareVistoDueVolteDiceCheNonEUnaNovita() {
		SuggerimentiMD md = new SuggerimentiMD();
		assertTrue(md.segnaVisto(TipoSuggerimento.ACCAMPAMENTO));
		assertFalse(md.segnaVisto(TipoSuggerimento.ACCAMPAMENTO));
	}
}
