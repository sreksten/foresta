package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdinamentoArtefattiTest {

	@Test
	void dentroLoStessoTipoIMiglioriVengonoPrima() {
		Artefatto spada1 = artefatto(TipoArtefatto.SPADA, 1, "b");
		Artefatto spada5 = artefatto(TipoArtefatto.SPADA, 5, "a");
		Artefatto spada3a = artefatto(TipoArtefatto.SPADA, 3, "a");
		Artefatto spada3b = artefatto(TipoArtefatto.SPADA, 3, "b");
		Artefatto elmo = artefatto(TipoArtefatto.ELMO, 9, "z");

		List<Artefatto> ordinati = new ArrayList<>(DisplayableCanvasScambiatoreArtefatti.ordinaArtefattiDaDisegnare(
				Arrays.asList(spada1, elmo, spada3b, spada5, spada3a)));

		List<Artefatto> spade = new ArrayList<>(ordinati);
		spade.remove(elmo);
		assertEquals(Arrays.asList(spada5, spada3a, spada3b, spada1), spade, "livello decrescente, poi nome");
		int posizioneElmo = ordinati.indexOf(elmo);
		assertTrue(posizioneElmo == 0 || posizioneElmo == ordinati.size() - 1, "il tipo resta il primo criterio");
	}

	private static Artefatto artefatto(TipoArtefatto tipo, int livello, String nome) {
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(tipo);
		md.setNome(nome);
		md.setDescrizione("che serve ai test");
		md.setLivello(livello);
		md.setPeso(0.1);
		return Artefatto.di(md);
	}
}
