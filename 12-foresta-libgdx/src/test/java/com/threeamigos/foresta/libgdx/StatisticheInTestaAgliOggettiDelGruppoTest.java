package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sul telefono monete, preziosi e punti stanno in cima alla colonna destra dell'inventario, con un separatore: l'elenco degli oggetti del gruppo comincia
 * sotto e si accorcia, quello di sinistra no (layout_telefono.md §4.3).
 */
class StatisticheInTestaAgliOggettiDelGruppoTest {

	private static final int SEPARATORE_ALTO = 32;

	private SchermataDiScambio schermata;
	private VistaPartita partita;

	@BeforeEach
	void prepara() {
		partita = DatiDiProva.partita();
		MisureDelleImmagini misure = new MisureDelleImmagini() {
			@Override
			public Misura personaggio(TipoPersonaggio classe) {
				return new Misura(40, 60);
			}

			@Override
			public Misura immagine(String risorsa) {
				return new Misura(336, SEPARATORE_ALTO);
			}
		};
		schermata = new SchermataDiScambio(1094, 505, partita, 368, 728, misure) {
		};
	}

	private int xDestra() {
		return 1094 - Finestra.SPACING - 368 + SchermataDiScambio.DIMENSIONE_BORDO_INTERNO;
	}

	private int xSinistra() {
		return Finestra.SPACING + SchermataDiScambio.DIMENSIONE_BORDO_INTERNO;
	}

	@Test
	void senzaLaTestataGliElenchiSonoQuelliDiSempre() {
		assertNull(schermata.testataADestra());
		assertEquals(0, schermata.altezzaTestataADestra());
		assertEquals(schermata.yElenco(xSinistra()), schermata.yElenco(xDestra()));
		assertEquals(schermata.altezzaDisponibileInRiquadroInventario(), schermata.altezzaElenco(xDestra()));
	}

	@Test
	void conLaTestataLElencoDiDestraCominciaSottoESiAccorcia() {
		schermata.setStatisticheInTestaADestra(true);
		int testata = schermata.altezzaTestataADestra();
		assertEquals(3 * schermata.fontHeight + Finestra.SPACING + SEPARATORE_ALTO + Finestra.SPACING, testata);
		assertEquals(schermata.yElenco(xSinistra()) + testata, schermata.yElenco(xDestra()));
		assertEquals(schermata.altezzaDisponibileInRiquadroInventario() - testata, schermata.altezzaElenco(xDestra()));
		assertEquals(schermata.altezzaDisponibileInRiquadroInventario(), schermata.altezzaElenco(xSinistra()), "a sinistra niente cambia");

		ContenutoDiColonna contenuto = schermata.testataADestra();
		// Come le altre colonne, il contenuto è nelle coordinate dello schermo: la sua altezza è la quota a cui finisce
		assertEquals(schermata.yElenco(xDestra()), contenuto.getAltezza(), "l'elenco comincia dove finisce la testata");
		List<String> testi = contenuto.getElementi().stream().filter(e -> e.getTipo() == Elemento.Tipo.TESTO).map(e -> e.getRighe().get(0))
				.collect(Collectors.toList());
		assertTrue(testi.contains(String.valueOf(partita.getCassa().getMonete())), "le monete");
		assertTrue(testi.contains(String.valueOf(partita.getCassa().getPreziosi())), "i preziosi");
		for (Elemento elemento : contenuto.getElementi()) {
			assertTrue(elemento.getX() >= xDestra() - Finestra.SPACING, "tutto nella colonna destra");
		}
	}
}
