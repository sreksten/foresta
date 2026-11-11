package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.SceltaDelLayout;
import com.threeamigos.foresta.ui.DisposizioneDelloSchermo;
import com.threeamigos.foresta.ui.schermate.SchermataTrofei;
import com.threeamigos.foresta.ui.schermate.SchermateSenzaPartita;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sullo schermo più stretto (il minimo del telefono, layout_telefono.md §5.1) i testi nell'alfabeto grande non escono dal bordo, in nessuna lingua: i nomi
 * dei trofei vanno a capo (e una parola troppo lunga si spezza col trattino), i titoli delle pagine ci stanno.
 */
class TestiGrandiSulTelefonoTest {

	private static final String[] LINGUE = {"it", "en", "en-GB"};
	// Il quantitativo più largo accanto al nome di un trofeo
	private static final String QUANTITA_PIU_LARGA = "999/999";
	private static final int SPAZIO_QUANTITA = 12;

	@AfterEach
	void rimettiLItaliano() {
		Testi.impostaLingua(Testi.LINGUA_PREDEFINITA);
	}

	private static int larghezzaDelContenutoMinima() {
		return SceltaDelLayout.minimoDelTelefono(new MisureDaiFile(), LivelloDiZoom.valore()).larghezza - DisposizioneDelloSchermo.SPESSORE_BARRA_ICONE;
	}

	@Test
	void unaParolaTroppoLungaSiSpezzaColTrattinoEOgniRigaCiSta() {
		int larghezza = LayoutDelTestoGrande.larghezza("ABCDEF");
		List<String> righe = LayoutDelTestoGrande.righe("XY ABCDEFGHIJKLMNOP Z", larghezza);
		assertEquals("XY", righe.get(0));
		assertTrue(righe.get(1).endsWith("-"), righe.toString());
		for (String riga : righe) {
			assertTrue(LayoutDelTestoGrande.larghezza(riga) <= larghezza, riga);
		}
		assertEquals("XYABCDEFGHIJKLMNOPZ", String.join("", righe).replace("-", "").replace(" ", ""), "non si perde niente");
	}

	@Test
	void iNomiDeiTrofeiNonEsconoDalBordoInNessunaLingua() {
		int colonna = larghezzaDelContenutoMinima() - 2 * Math.max(SchermataTrofei.MARGINE, SchermateSenzaPartita.MARGINE);
		int larghezzaDelNome = colonna - FontTool.getWidth(DoomdarkFont.medio(), QUANTITA_PIU_LARGA) - SPAZIO_QUANTITA;
		List<String> fuori = new ArrayList<>();
		for (String lingua : LINGUE) {
			Testi.impostaLingua(lingua);
			for (TipoTrofeo trofeo : TipoTrofeo.values()) {
				for (String riga : LayoutDelTestoGrande.righe(trofeo.getNome(), larghezzaDelNome)) {
					if (LayoutDelTestoGrande.larghezza(riga) > larghezzaDelNome) {
						fuori.add(lingua + ": " + riga);
					}
				}
			}
		}
		assertTrue(fuori.isEmpty(), String.join("\n", fuori));
	}

	@Test
	void iTitoliDellePagineCiStannoSuUnaRiga() {
		int larghezza = larghezzaDelContenutoMinima() - 2 * SchermataTrofei.MARGINE;
		List<String> fuori = new ArrayList<>();
		for (String lingua : LINGUE) {
			Testi.impostaLingua(lingua);
			for (String chiave : new String[]{"TITOLO_TROFEI", "TITOLO_BESTIARIO", "TITOLO_GRIMORIO", "TITOLO_CLASSIFICA"}) {
				String titolo = TestiDellaUI.get(chiave);
				if (LayoutDelTestoGrande.larghezza(titolo) > larghezza) {
					fuori.add(lingua + ": " + titolo + " (" + LayoutDelTestoGrande.larghezza(titolo) + " > " + larghezza + ")");
				}
			}
		}
		assertTrue(fuori.isEmpty(), String.join("\n", fuori));
	}
}
