package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.testi.Testi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La mappa (tipo di personaggio, attributo) → nota, letta da note_personaggi_it.properties.
 */
class NoteDeiPersonaggiTest {

	// Gli attributi per cui ogni tipo di personaggio ha una nota, più i due moltiplicatori dei danni
	private static final List<String> CHIAVI = Arrays.asList("CARICO_MASSIMO", "CRITICO", "PRECISIONE", "VELOCITA", "FURTIVITA",
			"PARATA", "RESISTENZA_MAGICA", "PERCEZIONE", "SOGGEZIONE", "FURIA", "CORAGGIO", "VALORE", "CONTRATTAZIONE",
			"NUMERO_BERSAGLI", "STANCHEZZA", "RIGENERAZIONE_SALUTE", "RIGENERAZIONE_MAGIA",
			NoteDeiPersonaggi.DANNI_MAGICI, NoteDeiPersonaggi.DANNI_FISICI);

	@Test
	void ogniTipoDiPersonaggioHaUnaNotaPerOgniMoltiplicatore() {
		for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
			for (String chiave : CHIAVI) {
				assertTrue(NoteDeiPersonaggi.nota(tipo, chiave).filter(n -> !n.trim().isEmpty()).isPresent(),
						"manca la nota " + tipo + "." + chiave);
			}
		}
	}

	@Test
	void unaNotaSiLeggeConAccentiEVirgolette() {
		assertTrue(NoteDeiPersonaggi.nota(TipoPersonaggio.GOBLIN, TipoAttributo.CARICO_MASSIMO).get().startsWith("Più piccolo di un umano"));
		assertTrue(NoteDeiPersonaggi.nota(TipoPersonaggio.ELFO, TipoAttributo.RIGENERAZIONE_MAGIA).get().contains("\"trance\""),
				NoteDeiPersonaggi.nota(TipoPersonaggio.ELFO, TipoAttributo.RIGENERAZIONE_MAGIA).get());
	}

	@Test
	void unTipoPuoUsareLeNoteDiUnAltroOdiUnGruppo() {
		for (String chiave : CHIAVI) {
			assertEquals(NoteDeiPersonaggi.nota(TipoPersonaggio.GUERRIERO, chiave), NoteDeiPersonaggi.nota(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, chiave));
			assertEquals(NoteDeiPersonaggi.nota(TipoPersonaggio.BARDO, chiave), NoteDeiPersonaggi.nota(TipoPersonaggio.BARDO_DELLE_LOCANDE, chiave));
			assertEquals(NoteDeiPersonaggi.nota(TipoPersonaggio.ALCHIMISTA, chiave), NoteDeiPersonaggi.nota(TipoPersonaggio.LOCANDIERE, chiave));
			assertEquals(NoteDeiPersonaggi.nota(TipoPersonaggio.ALCHIMISTA, chiave), NoteDeiPersonaggi.nota(TipoPersonaggio.DONNA, chiave));
		}
	}

	@Test
	void chiNonHaUnaNotaPropriaUsaQuellaPredefinita() {
		assertEquals("Non entra mai nel gruppo: non tratta con i mercanti.",
				NoteDeiPersonaggi.nota(TipoPersonaggio.DRAGO, TipoAttributo.CONTRATTAZIONE).get());
	}

	@Test
	void unAttributoSenzaNoteNonHaNiente() {
		assertFalse(NoteDeiPersonaggi.nota(TipoPersonaggio.GOBLIN, TipoAttributo.FORZA).isPresent());
		assertFalse(NoteDeiPersonaggi.nota(TipoPersonaggio.GOBLIN, "NON_ESISTE").isPresent());
	}

	@Test
	void ilFileNonHaChiaviConErroriDiBattitura() throws IOException {
		Properties note = new Properties();
		try (InputStream flusso = Testi.class.getResourceAsStream("note_personaggi_it.properties")) {
			note.load(new InputStreamReader(flusso, StandardCharsets.UTF_8));
		}
		Set<String> tipi = new HashSet<>(Arrays.asList("NON_COMBATTENTE", "PREDEFINITA"));
		for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
			tipi.add(tipo.name());
		}
		Set<String> suffissi = new HashSet<>(CHIAVI);
		suffissi.add("COME");
		for (String chiave : note.stringPropertyNames()) {
			int punto = chiave.indexOf('.');
			assertTrue(punto > 0 && tipi.contains(chiave.substring(0, punto)), "tipo sconosciuto nella chiave " + chiave);
			assertTrue(suffissi.contains(chiave.substring(punto + 1)), "attributo sconosciuto nella chiave " + chiave);
			if (chiave.endsWith(".COME")) {
				assertTrue(tipi.contains(note.getProperty(chiave)), "rimando a un tipo sconosciuto: " + chiave);
			}
			assertFalse(note.getProperty(chiave).trim().isEmpty(), "nota vuota: " + chiave);
		}
	}

	@Test
	void unGiroDiRimandiSiFermaESiUsaLaNotaPredefinita() {
		// Nel file di prova della lingua "zz" l'arpia rimanda al centauro e il centauro all'arpia
		Testi.impostaLingua("zz");
		try {
			assertEquals("La nota predefinita di prova", NoteDeiPersonaggi.nota(TipoPersonaggio.ARPIA, "CHIAVE_DI_PROVA").get());
		} finally {
			Testi.impostaLingua(null);
		}
	}
}
