package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.oggetti.FormaDelBottino;
import com.threeamigos.foresta.locazioni.OggettiDiProva;
import com.threeamigos.foresta.locazioni.Radura;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.oggetti.BottinoArtefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.oggetti.Moneta;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Come la locazione dice ciò che il gruppo vede: un artefatto già fatto (quello di un tempio) con il suo nome completo, le
 * altre cose con il loro nome e il loro articolo.
 */
class DescrizioneDelBottinoTest {

	// Una radura vuota con quel bottino, descritta al gruppo della partita: senza avversari non c'è altro da dire
	private static String descrizione(PartitaDiTest partita, Bottino bottino) {
		Radura radura = new Radura();
		OggettiDiProva.impostaOggetto(radura, bottino);
		return OggettiDiProva.descrizioneMostriEOggetti(radura, partita.gruppo(), GruppoAvversario.getIstanza());
	}

	@Test
	void unArtefattoGiaFattoSiDescriveConIlSuoNomeCompleto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			Artefatto spada = new GeneratoreArtefattiTabelle(new Random(1)).generaArtefatto(TipoArtefatto.SPADA, 1);

			String testo = descrizione(partita, BottinoArtefatto.giaFatto(spada));

			assertTrue(testo.contains(spada.getNomeCompleto()), testo);
			assertFalse(testo.contains("un artefatto"), testo);
		}
	}

	@Test
	void ilBottinoDiUnaFormaSiDescriveConIlNomeEL_Articolo() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });

			assertTrue(descrizione(partita, BottinoArtefatto.nuovo(FormaDelBottino.SPADA)).contains("una spada"));
			assertTrue(descrizione(partita, new Moneta()).matches(".*(una moneta|\\w+ monete).*"));
		}
	}
}
