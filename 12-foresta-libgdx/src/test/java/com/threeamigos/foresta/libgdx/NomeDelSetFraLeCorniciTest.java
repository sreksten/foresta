package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.FontTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nell'inventario il nome di un set leggendario completo sta fra le due cornici, e non ci passa sopra: se è troppo lungo va a capo e le righe che seguono
 * scendono.
 */
class NomeDelSetFraLeCorniciTest {

	private static final String SET_LUNGO = "Set Leggendario dell'Antico Custode delle Sette Foreste Dimenticate";

	private SchermataDiProva schermata;
	private VistaPersonaggio personaggio;

	private static final class SchermataDiProva extends SchermataDiScambio {
		SchermataDiProva(VistaPartita partita, MisureDelleImmagini misure) {
			// Due cornici da 300 su 800: la zona fra loro e' larga 800 - 2 * (4 + 300 + 4) = 184
			super(800, 480, partita, 300, 400, misure);
		}

		@Override
		public TipoPersonaggio getMestiereAlCentro() {
			return TipoPersonaggio.ALCHIMISTA;
		}

		@Override
		public String getEtichettaMestiere() {
			return "Alchimista";
		}
	}

	@BeforeEach
	void prepara() {
		VistaPartita partita = DatiDiProva.partita();
		MisureDelleImmagini misure = new MisureDelleImmagini() {
			@Override
			public Misura personaggio(TipoPersonaggio classe) {
				return new Misura(40, 60);
			}

			@Override
			public Misura immagine(String risorsa) {
				return new Misura(27, 28);
			}
		};
		schermata = new SchermataDiProva(partita, misure);
		VistaPersonaggio vero = partita.getGruppoGiocatore().getPersonaggio(0);
		personaggio = conSet(vero, SET_LUNGO, "Set Breve");
	}

	/** Lo stesso personaggio, ma con quei set completi */
	private static VistaPersonaggio conSet(VistaPersonaggio vero, String... set) {
		return (VistaPersonaggio) Proxy.newProxyInstance(VistaPersonaggio.class.getClassLoader(), new Class<?>[]{VistaPersonaggio.class},
				(proxy, metodo, argomenti) -> metodo.getName().equals("getDescrizioniSetCompleti") ? Arrays.asList(set) : metodo.invoke(vero, argomenti));
	}

	private List<Elemento> righeDeiSet(ContenutoDiColonna testata) {
		List<Elemento> righe = new ArrayList<>();
		for (Elemento elemento : testata.getElementi()) {
			if (elemento.getTipo() == Elemento.Tipo.TESTO && elemento.getColore() == DoomdarkColorModel.Color.YELLOW) {
				righe.add(elemento);
			}
		}
		return righe;
	}

	@Test
	void ilNomeLungoVaACapoEOgniRigaStaFraLeDueCornici() {
		int zona = schermata.xMassimaZonaCentrale - schermata.xMinimaZonaCentrale;
		assertTrue(FontTool.getWidth(schermata.font, SET_LUNGO) > zona, "la prova presume un nome piu' largo della zona");
		List<Elemento> righe = righeDeiSet(schermata.testataDelPersonaggio(personaggio));
		assertTrue(righe.size() > 2, "il nome lungo si spezza (e il set breve e' una riga): " + righe.size());
		for (Elemento riga : righe) {
			int larghezza = FontTool.getWidth(schermata.font, riga.getRighe().get(0));
			assertTrue(riga.getX() >= schermata.xMinimaZonaCentrale, "non passa sulla cornice di sinistra: " + riga.getRighe());
			assertTrue(riga.getX() + larghezza <= schermata.xMassimaZonaCentrale, "non passa sulla cornice di destra: " + riga.getRighe());
		}
	}

	@Test
	void leRigheSuccessiveScendonoDiQuantoServe() {
		ContenutoDiColonna senzaSet = schermata.testataDelPersonaggio(conSet(personaggio));
		ContenutoDiColonna conUnSetBreve = schermata.testataDelPersonaggio(conSet(personaggio, "Set Breve"));
		ContenutoDiColonna conSetLungo = schermata.testataDelPersonaggio(conSet(personaggio, SET_LUNGO));
		int altezzaDiUnaRiga = conUnSetBreve.getAltezza() - senzaSet.getAltezza();
		assertTrue(altezzaDiUnaRiga > 0, "un set breve occupa una riga");
		int righeDelSetLungo = schermata.righeFraLeDueCornici(SET_LUNGO).size();
		assertTrue(righeDelSetLungo > 1);
		assertEquals(righeDelSetLungo * altezzaDiUnaRiga, conSetLungo.getAltezza() - senzaSet.getAltezza(),
				"ogni riga in piu' del nome fa scendere quel che segue di una riga");
	}

	@Test
	void unNomeCheCiStaRestaSuUnaRigaCentrata() {
		List<Elemento> righe = righeDeiSet(schermata.testataDelPersonaggio(conSet(personaggio, "Set Breve")));
		assertEquals(1, righe.size());
		int larghezza = FontTool.getWidth(schermata.font, "Set Breve");
		assertEquals((800 - larghezza) / 2, righe.get(0).getX());
	}
}
