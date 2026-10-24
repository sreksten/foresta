package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.serializzazione.MappaProprieta;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il salvataggio è a righe con i campi separati da "|": i testi liberi non devono poter spezzare la riga né spostare i
 * campi, e a parità di dati il testo scritto è sempre lo stesso.
 */
class TestiNeiSalvataggiTest {

	@Test
	void unNomeConACapoPipeESeparatoreDiMappeSiRileggeSenzaSpostareICampi() throws IOException {
		// Given
		PersonaggioMD personaggio = new PersonaggioMD();
		personaggio.setClasse(TipoPersonaggio.LADRO);
		personaggio.setVivo(false);
		personaggio.setNome("Ar\nse|nio");
		personaggio.setCausaTrapasso("ucciso\r\nda un|goblin");
		personaggio.set(TipoAttributo.FORZA, 12);
		personaggio.setLivello(3);

		// When
		PersonaggioMD riletto = new PersonaggioMD();
		riletto.leggi(lettore(scrivi(personaggio)));

		// Then
		assertEquals("Ar senio", riletto.getNome());
		assertEquals("ucciso da ungoblin", riletto.getCausaTrapasso());
		assertEquals(3, riletto.getLivello());
		assertEquals(12.0, riletto.get(TipoAttributo.FORZA));
		assertEquals(TipoPersonaggio.LADRO, riletto.getClasse());
	}

	@Test
	void vivoEMortoSonoUnCampoASeEUnaCausaChiamataVivoNonCreaConfusione() throws IOException {
		// Given
		PersonaggioMD morto = new PersonaggioMD();
		morto.setClasse(TipoPersonaggio.MAGO);
		morto.setVivo(false);
		morto.setCausaTrapasso("vivo");
		PersonaggioMD vivoConUnaCausa = new PersonaggioMD();
		vivoConUnaCausa.setClasse(TipoPersonaggio.MAGO);
		vivoConUnaCausa.setVivo(true);
		vivoConUnaCausa.setCausaTrapasso("avanzo di una morte precedente");

		// When
		PersonaggioMD mortoRiletto = new PersonaggioMD();
		mortoRiletto.leggi(lettore(scrivi(morto)));
		PersonaggioMD vivoRiletto = new PersonaggioMD();
		vivoRiletto.leggi(lettore(scrivi(vivoConUnaCausa)));

		// Then
        assertFalse(mortoRiletto.isVivo());
		assertEquals("vivo", mortoRiletto.getCausaTrapasso());
        assertTrue(vivoRiletto.isVivo());
		assertNull(vivoRiletto.getCausaTrapasso(), "chi e' vivo non ha una causa di morte");
	}

	@Test
	void gliAttributiSiScrivonoNellOrdineDellEnumQualunqueSiaQuelloDiInserimento() throws IOException {
		// Given
		TipoAttributo[] tutti = TipoAttributo.values();
		List<TipoAttributo> alContrario = new ArrayList<>(Arrays.asList(tutti));
		java.util.Collections.reverse(alContrario);
		PersonaggioMD personaggio = new PersonaggioMD();
		personaggio.setClasse(TipoPersonaggio.GUERRIERO);
		personaggio.setVivo(true);
		for (TipoAttributo tipo : alContrario) {
			personaggio.set(tipo, 1);
			personaggio.setMassimo(tipo, 5.0);
		}

		// When
		String[] righe = scrivi(personaggio).split("\\R");

		// Then: la riga 0 e' il personaggio, poi minimi, massimi e attributi
		assertEquals(nomiInOrdine(tutti), nomiDellaRiga(righe[2]), "i massimi");
		assertEquals(nomiInOrdine(tutti), nomiDellaRiga(righe[3]), "gli attributi");
	}

	@Test
	void unMessaggioConACapoEPipeRestaSuUnaRigaSola() throws IOException {
		// Given
		MessaggioMD messaggio = new MessaggioMD("uno\ndue|tre\r\nquattro", true);
		StringWriter scritto = new StringWriter();
		PrintWriter stream = new PrintWriter(scritto);

		// When
		messaggio.salva(stream);
		stream.flush();
		MessaggioMD riletto = new MessaggioMD();
		riletto.leggi(lettore(scritto.toString()));

		// Then
		assertEquals(1, scritto.toString().split("\\R").length, "una riga sola");
		assertEquals("uno due|tre quattro", riletto.getTesto());
        assertTrue(riletto.isParagrafo());
	}

	private static String scrivi(PersonaggioMD personaggio) throws IOException {
		StringWriter scritto = new StringWriter();
		PrintWriter stream = new PrintWriter(scritto);
		personaggio.salva(stream);
		stream.flush();
		return scritto.toString();
	}

	private static BufferedReader lettore(String testo) {
		return new BufferedReader(new StringReader(testo));
	}

	private static List<String> nomiInOrdine(TipoAttributo[] tipi) {
		List<String> nomi = new ArrayList<>();
		for (TipoAttributo tipo : tipi) {
			nomi.add(tipo.name());
		}
		return nomi;
	}

	private static List<String> nomiDellaRiga(String riga) {
		List<String> nomi = new ArrayList<>();
		for (String campo : riga.split("\\|")) {
			nomi.add(campo.substring(0, campo.indexOf(MappaProprieta.SEPARATORE)));
		}
		return nomi;
	}
}
