package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.intermezzi.ImmagineIntermezzo;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.FotogrammiDelFuocoDaCampo;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo;
import com.threeamigos.foresta.ui.schermate.ScenaDellIntermezzo.Quadro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La scena di un intermezzo e il fuoco da campo, sulla pagina di prova: il modello è del modulo comune, qui si prova con una misura fissa delle immagini.
 */
class ScenaDellIntermezzoTest {

	private static final int LARGHEZZA = 800;
	private static final int ALTEZZA = 480;

	private ScenaDellIntermezzo scena;

	@BeforeEach
	void prepara() {
		scena = new ScenaDellIntermezzo(LARGHEZZA, ALTEZZA, immagine -> immagine.getTipo() == ImmagineIntermezzo.Tipo.ANIMAZIONE
				? new Misura(FotogrammiDelFuocoDaCampo.LARGHEZZA, FotogrammiDelFuocoDaCampo.ALTEZZA) : new Misura(100, 150));
		scena.mostra(DatiDiProva.paginaDiIntermezzo());
	}

	@Test
	void loSfondoStiratoRiempieTuttoLoSchermo() {
		Quadro quadro = scena.quadroAl(1.0);
		assertEquals(0, quadro.getSfondo().getX());
		assertEquals(0, quadro.getSfondo().getY());
		assertEquals(LARGHEZZA, quadro.getSfondo().getLarghezza());
		assertEquals(ALTEZZA, quadro.getSfondo().getAltezza());
		assertNull(quadro.getRitaglio(), "la pagina non chiede il ritaglio");
	}

	@Test
	void gliElementiSonoCentratiNelPuntoIndicatoEIlFuocoEUnAnimazione() {
		Quadro quadro = scena.quadroAl(1.0);
		assertEquals(3, quadro.getElementi().size());
		// Il guerriero sta al 30% della larghezza e al 70% dell'altezza, con il centro lì
		ScenaDellIntermezzo.Disegno guerriero = quadro.getElementi().get(0);
		assertEquals((int) Math.round(0.3 * LARGHEZZA) - 50, guerriero.getX());
		assertEquals((int) Math.round(0.7 * ALTEZZA) - 75, guerriero.getY());
		assertEquals(1f, guerriero.getOpacita());
		ScenaDellIntermezzo.Disegno fuoco = quadro.getElementi().get(2);
		assertTrue(fuoco.getImmagine().isAnimazione());
		assertEquals(FotogrammiDelFuocoDaCampo.LARGHEZZA, fuoco.getLarghezza());
		assertEquals("Una sera alla locanda", quadro.getTesto());
	}

	@Test
	void suUnoSchermoDiUnAltraFormaLePosizioniStannoSullaScenaCentrata() {
		// Lo schermo è più largo e più basso della scena su cui è composta la pagina: la scena si centra e si ritaglia sopra e sotto
		int larghezzaSchermo = 1000;
		int altezzaSchermo = 400;
		ScenaDellIntermezzo suUnTelefono = new ScenaDellIntermezzo(larghezzaSchermo, altezzaSchermo, new Misura(LARGHEZZA, ALTEZZA),
				immagine -> immagine.getTipo() == ImmagineIntermezzo.Tipo.ANIMAZIONE
						? new Misura(FotogrammiDelFuocoDaCampo.LARGHEZZA, FotogrammiDelFuocoDaCampo.ALTEZZA) : new Misura(100, 150));
		suUnTelefono.mostra(DatiDiProva.paginaDiIntermezzo());
		Quadro quadro = suUnTelefono.quadroAl(1.0);
		int scenaX = (larghezzaSchermo - LARGHEZZA) / 2;
		int scenaY = (altezzaSchermo - ALTEZZA) / 2;
		ScenaDellIntermezzo.Disegno guerriero = quadro.getElementi().get(0);
		assertEquals(scenaX + (int) Math.round(0.3 * LARGHEZZA) - 50, guerriero.getX());
		assertEquals(scenaY + (int) Math.round(0.7 * ALTEZZA) - 75, guerriero.getY());
		// Il fumetto punta ancora alla bocca di chi parla e resta dentro lo schermo vero
		ScenaDellIntermezzo.Fumetto fumetto = quadro.getFumetti().get(0);
		assertTrue(fumetto.getPuntaVersoX() >= guerriero.getX() && fumetto.getPuntaVersoX() <= guerriero.getX() + guerriero.getLarghezza());
		assertTrue(fumetto.getY() - fumetto.getDescrizione().getAltezza() >= 0 && fumetto.getY() <= altezzaSchermo);
		assertTrue(fumetto.getX() >= 0 && fumetto.getX() + fumetto.getDescrizione().getLarghezza() <= larghezzaSchermo);
	}

	@Test
	void ilGuerrieroEIlMagoSiGuardanoQuindiUnoDeiDueEAlContrarioDiComeEDisegnato() {
		Quadro quadro = scena.quadroAl(1.0);
		ScenaDellIntermezzo.Disegno guerriero = quadro.getElementi().get(0);
		ScenaDellIntermezzo.Disegno mago = quadro.getElementi().get(1);
		assertEquals(TipoPersonaggio.GUERRIERO, guerriero.getImmagine().getOrigine().getClassePersonaggio());
		assertEquals("personaggi/Guerriero.png", guerriero.getImmagine().getRisorsa());
		assertNotEquals(guerriero.isSpecchiata(), mago.isSpecchiata(), "guardano da parti opposte, ma le immagini guardano dalla stessa");
	}

	@Test
	void unaBattutaAllaVoltaConLaPuntaVersoLaBoccaDiChiParla() {
		Quadro primo = scena.quadroAl(1.0);
		assertEquals(1, primo.getFumetti().size());
		ScenaDellIntermezzo.Fumetto fumetto = primo.getFumetti().get(0);
		assertEquals("Che serata, amico mio!", fumetto.getDescrizione().getTesto());
		ScenaDellIntermezzo.Disegno guerriero = primo.getElementi().get(0);
		assertTrue(fumetto.getPuntaVersoX() >= guerriero.getX() && fumetto.getPuntaVersoX() <= guerriero.getX() + guerriero.getLarghezza());
		assertTrue(fumetto.getPuntaVersoY() >= guerriero.getY() && fumetto.getPuntaVersoY() <= guerriero.getY() + guerriero.getAltezza());
		// Il fumetto sta sopra la bocca e dentro lo schermo
		assertTrue(fumetto.getY() < fumetto.getPuntaVersoY());
		assertTrue(fumetto.getX() >= 0 && fumetto.getX() + fumetto.getDescrizione().getLarghezza() <= LARGHEZZA);

		Quadro tra = scena.quadroAl(4.7);
		assertTrue(tra.getFumetti().isEmpty(), "fra le due battute non c'è nessun fumetto");
		Quadro secondo = scena.quadroAl(5.5);
		assertEquals("Il Drago non ci troverà qui, spero.", secondo.getFumetti().get(0).getDescrizione().getTesto());
	}

	@Test
	void unaRisorsaSenzaMisuraNonSiDisegna() {
		ScenaDellIntermezzo senzaImmagini = new ScenaDellIntermezzo(LARGHEZZA, ALTEZZA, immagine -> null);
		senzaImmagini.mostra(DatiDiProva.paginaDiIntermezzo());
		Quadro quadro = senzaImmagini.quadroAl(1.0);
		assertNull(quadro.getSfondo(), "senza sfondo si vede l'ombra del drago");
		assertTrue(quadro.getElementi().isEmpty());
	}

	@Test
	void ilFuocoHaOttoFotogrammiAcuiSiTornaADieciAlSecondo() {
		ImmagineDiPixel[] fotogrammi = FotogrammiDelFuocoDaCampo.fotogrammi();
		assertEquals(FotogrammiDelFuocoDaCampo.FOTOGRAMMI, fotogrammi.length);
		for (ImmagineDiPixel f : fotogrammi) {
			assertEquals(FotogrammiDelFuocoDaCampo.LARGHEZZA, f.getLarghezza());
			assertEquals(FotogrammiDelFuocoDaCampo.ALTEZZA, f.getAltezza());
			int opachi = 0;
			for (int y = 0; y < f.getAltezza(); y++) {
				for (int x = 0; x < f.getLarghezza(); x++) {
					if ((f.getArgb(x, y) >>> 24) != 0) {
						opachi++;
					}
				}
			}
			assertTrue(opachi > 300, "il fuoco si vede");
		}
		assertEquals(0, FotogrammiDelFuocoDaCampo.indiceAl(0.05));
		assertEquals(1, FotogrammiDelFuocoDaCampo.indiceAl(0.15));
		assertEquals(0, FotogrammiDelFuocoDaCampo.indiceAl(0.85), "dopo otto fotogrammi si ricomincia");
		boolean diversi = false;
		for (int y = 0; y < ImmagineDiPixelAltezza(fotogrammi[0]) && !diversi; y++) {
			for (int x = 0; x < fotogrammi[0].getLarghezza(); x++) {
				if (fotogrammi[0].getArgb(x, y) != fotogrammi[3].getArgb(x, y)) {
					diversi = true;
					break;
				}
			}
		}
		assertTrue(diversi, "le fiamme si muovono");
	}

	private static int ImmagineDiPixelAltezza(ImmagineDiPixel immagine) {
		return immagine.getAltezza();
	}
}
