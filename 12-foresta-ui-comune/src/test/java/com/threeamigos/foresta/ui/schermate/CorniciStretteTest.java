package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.componenti.CorniceTagliata;
import com.threeamigos.foresta.ui.componenti.ElementoDelElenco;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sul telefono la colonna centrale delle schermate di scambio (inventario, botteghe, tattiche) deve essere larga almeno quanto un attributo con il suo
 * valore: le due cornici si stringono tagliandole nel mezzo in larghezza, i titoli lunghi lasciano il posto a quelli corti e i separatori più larghi
 * della colonna mostrano la loro parte centrale (layout_telefono.md §6).
 */
class CorniciStretteTest {

	// La cornice dell'inventario con lo zoom 2, e lo schermo del telefono più piccolo senza la barra delle icone
	private static final int CORNICE = 360;
	private static final int TELEFONO = 770;
	private static final int SCHERMO_LARGO = 1500;

	private static final MisureDelleImmagini MISURE = new MisureDelleImmagini() {
		@Override
		public Misura personaggio(TipoPersonaggio classe) {
			return new Misura(40, 60);
		}

		@Override
		public Misura immagine(String risorsa) {
			return new Misura(336, 32);
		}
	};

	private static SchermataDiScambio schermata(int larghezza) {
		return new SchermataDiScambio(larghezza, 505, null, CORNICE, 728, MISURE) {
		};
	}

	@Test
	void sulTelefonoLeCorniciSiStringonoFinoALasciareAlCentroUnAttributoConIlSuoValore() {
		SchermataDiScambio schermata = schermata(TELEFONO);
		int cornice = schermata.getLarghezzaCornice();
		assertTrue(cornice < CORNICE);
		int centrale = TELEFONO - 2 * cornice - 4 * Finestra.SPACING;
		assertTrue(centrale >= ElencoDegliAttributi.larghezzaNecessaria(), centrale + " < " + ElencoDegliAttributi.larghezzaNecessaria());
		assertTrue(centrale - ElencoDegliAttributi.larghezzaNecessaria() <= 4 * Finestra.SPACING, "non più del necessario");

		// Tagliata nel mezzo: la parte sinistra dall'inizio dell'immagine, la destra fino alla sua fine, attaccate
		List<CorniceTagliata.Fascia> colonne = schermata.getColonneDellaCornice();
		assertEquals(2, colonne.size());
		assertEquals(0, colonne.get(0).getYSorgente());
		assertEquals(CORNICE / 2, colonne.get(1).getYSorgente() + colonne.get(1).getAltezzaSorgente(), "finisce con l'ultima colonna dell'immagine");
		assertEquals(colonne.get(0).getAltezzaDestinazione(), colonne.get(1).getYDestinazione());
		assertEquals(cornice, CorniceTagliata.larghezza(colonne));
		assertEquals(cornice - 2 * (SchermataDiScambio.DIMENSIONE_BORDO_INTERNO + Finestra.SPACING), schermata.larghezzaDisponibileInRiquadroInventario());
	}

	@Test
	void doveCeSpazioLaCorniceRestaIntera() {
		SchermataDiScambio schermata = schermata(SCHERMO_LARGO);
		assertEquals(CORNICE, schermata.getLarghezzaCornice());
		assertEquals(1, schermata.getColonneDellaCornice().size());
	}

	@Test
	void unTitoloCheNonCiStaLasciaIlPostoAQuelloCorto() {
		assertEquals(TestiDellaUI.get("INVENTARIO_PERSONAGGIO"), schermata(SCHERMO_LARGO).titoloDellaCornice("INVENTARIO_PERSONAGGIO"));
		assertEquals(TestiDellaUI.get("INVENTARIO_PERSONAGGIO_BREVE"), schermata(TELEFONO).titoloDellaCornice("INVENTARIO_PERSONAGGIO"));
		// Uno che ci sta resta com'è
		assertEquals(TestiDellaUI.get("TATTICHE_TATTICA"), schermata(TELEFONO).titoloDellaCornice("TATTICHE_TATTICA"));
	}

	@Test
	void unImmagineCentrataPiuLargaDelloSpazioMostraLaSuaStrisciaCentrale() {
		Elemento stretta = Elemento.immagineCentrata("fondinon2x2/Separatore.png", 336, 32, 100, 400, 10);
		assertTrue(stretta.isStriscia());
		assertEquals(100, stretta.getLarghezza());
		assertEquals(118, stretta.getXDellaStriscia(), "la parte centrale");
		assertEquals(350, stretta.getX());
		Elemento intera = Elemento.immagineCentrata("fondinon2x2/Separatore.png", 336, 32, 400, 400, 10);
		assertFalse(intera.isStriscia());
		assertEquals(336, intera.getLarghezza());
		assertEquals(400 - 168, intera.getX());
	}

	@Test
	void unSeparatorePiuLargoDellElencoStaCentratoESeNeVedeLaParteCentrale() {
		ElencoScorrevole<Object> elenco = new ElencoScorrevole<>(200, 10, 1);
		elenco.creaSeparatore(new ElencoScorrevole.ImmagineDelNodo("separatore", new Misura(328, 42)));
		ElementoDelElenco separatore = elenco.elementiVisibili(100, 0).get(0);
		assertEquals(ElementoDelElenco.Tipo.ICONA, separatore.getTipo());
		assertEquals((200 - 328) / 2, separatore.getX(), "esce da tutti e due i lati dello stesso tanto: chi disegna ritaglia l'elenco");
	}
}
