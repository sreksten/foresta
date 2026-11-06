package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.ImmagineDelNodo;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole.TestoDelNodo;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElencoScorrevoleTest {

	private static final DoomdarkFont FONT = DoomdarkFont.piccolo();
	private static final DoomdarkColorModel.Color COLORE = DoomdarkColorModel.Color.values()[0];

	/** Un nodo di una riga: riga (font più interlinea) più l'interlinea sotto */
	private static final int PASSO = FONT.getHeight() + 2 * 2;

	private ElencoScorrevole<String> elenco(int nodi) {
		ElencoScorrevole<String> e = new ElencoScorrevole<>(200, 10, 2);
		for (int i = 0; i < nodi; i++) {
			e.creaNodo(new TestoDelNodo("Nodo " + i, FONT, COLORE), null, null, null, null, "n" + i);
		}
		return e;
	}

	@Test
	void unNodoAltoComeIlFontPiuLInterlinea() {
		ElencoScorrevole<String> e = elenco(1);
		assertEquals(0, e.getMassimoScorrimento(PASSO));
		assertEquals(1, e.getMassimoScorrimento(PASSO - 1));
	}

	@Test
	void siScorreSoloSeLElencoNonStaNelloSpazio() {
		ElencoScorrevole<String> e = elenco(20);
		assertTrue(e.isScorrevole(50));
		assertFalse(e.isScorrevole(10000));
		assertEquals(0, e.limitaOffset(50, -5));
		assertEquals(e.getMassimoScorrimento(50), e.limitaOffset(50, 100000));
	}

	@Test
	void ilRiferimentoAllaQuotaESuoIlNodoGiusto() {
		ElencoScorrevole<String> e = elenco(3);
		assertEquals("n0", e.riferimentoTitoloAllaQuota(0));
		assertEquals("n1", e.riferimentoTitoloAllaQuota(PASSO));
		assertEquals("n2", e.riferimentoTitoloAllaQuota(PASSO * 2 + 1));
		assertNull(e.riferimentoTitoloAllaQuota(PASSO * 3));
		assertNull(e.riferimentoTitoloAllaQuota(-1));
	}

	@Test
	void laPosizioneDelTitoloEQuellaDelPrimoNodoCheCorrisponde() {
		ElencoScorrevole<String> e = elenco(3);
		Punto p = e.posizioneTitolo("n2"::equals);
		assertEquals(0, p.x);
		assertEquals(2 * PASSO, p.y);
		assertNull(e.posizioneTitolo("x"::equals));
	}

	@Test
	void sidisegnanoSoloINodiVisibili() {
		ElencoScorrevole<String> e = elenco(30);
		List<ElementoDelElenco> visibili = e.elementiVisibili(3 * PASSO, 0);
		assertEquals(3, visibili.size());
		assertEquals("Nodo 0", visibili.get(0).getTesto());
		assertFalse(e.isFrecciaSuVisibile(0));
		assertTrue(e.isFrecciaGiuVisibile(50, 0));
	}

	@Test
	void laDescrizioneSiVedeSoloSeIFigliSonoVisibiliEFaAlzareIlNodo() {
		ElencoScorrevole<String> e = new ElencoScorrevole<>(200, 10, 2);
		ElencoScorrevole<String>.Nodo n = e.creaNodo(new TestoDelNodo("T", FONT, COLORE), null, null, new TestoDelNodo("D", FONT, COLORE), null, "t");
		int aperto = n.getAltezzaSenzaFigli();
		n.setFigliVisibili(false);
		assertTrue(aperto > n.getAltezzaSenzaFigli());
		assertEquals(1, e.elementiVisibili(500, 0).size());
	}

	@Test
	void unaIconaAlzaIlNodoELaChiaveSiSposta() {
		ElencoScorrevole<String> e = new ElencoScorrevole<>(200, 10, 2);
		e.creaNodo(new TestoDelNodo("T", FONT, COLORE), null, null, null, new ImmagineDelNodo("icona", new Misura(32, 32)), "t");
		assertEquals(34, e.altezzaDellElenco(0));
		List<ElementoDelElenco> v = e.elementiVisibili(100, 0);
		assertEquals(ElementoDelElenco.Tipo.ICONA, v.get(0).getTipo());
		assertEquals(32 + 4, v.get(1).getX());
	}

	@Test
	void unValoreImmagineStaAllineatoADestra() {
		ElencoScorrevole<String> e = new ElencoScorrevole<>(200, 10, 2);
		e.creaNodo(new TestoDelNodo("T", FONT, COLORE), null, new ImmagineDelNodo("v", new Misura(50, 8)), null, null, "t");
		List<ElementoDelElenco> v = e.elementiVisibili(100, 0);
		assertEquals(150, v.get(1).getX());
	}
}
