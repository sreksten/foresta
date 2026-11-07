package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto;
import com.threeamigos.foresta.ui.sprite.RivelazioneDellArtefatto.Fotogramma;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La rivelazione di un artefatto, su una spada generata: il modello è del modulo comune, qui si prova l'andamento nel tempo e la geometria.
 */
class RivelazioneDellArtefattoTest {

	private static final Area AREA = new Area(0, 0, 1000, 700);
	private static final Punto DESTINAZIONE = new Punto(100, 100);

	private VistaArtefatto spada;
	private long[] ora;
	private RivelazioneDellArtefatto rivelazione;

	@BeforeEach
	void prepara() {
		spada = new GeneratoreArtefattiTabelle(new Random(5)).generaArtefatto(TipoArtefatto.SPADA, 6);
		ora = new long[]{0};
		rivelazione = new RivelazioneDellArtefatto(spada, 3, AREA, DESTINAZIONE, new Misura(40, 60), () -> ora[0]);
	}

	@Test
	void ilCerchioNascePiccolissimoEDopoLApertura_ComparisceIntero() {
		assertFalse(rivelazione.fotogramma(0f).isCerchioVisibile(), "al primo istante è un punto che ancora non si vede");
		Fotogramma aperto = rivelazione.fotogramma(1.5f);
		assertTrue(aperto.isCerchioVisibile());
		assertEquals(AREA.getLarghezza() / 2f, aperto.centroX, 0.5f);
		assertEquals(aperto.raggio, aperto.sfera.raggio, 0.001f);
		assertFalse(aperto.raggi.isEmpty(), "a metà rivelazione i raggi sono fuori");
		assertNotNull(aperto.ritaglio, "finché non vola via resta nella sua area");
		assertEquals(1f, aperto.opacitaDelPannello, 0.001f);
	}

	@Test
	void ilPannelloHaLeRigheDelloNomeEStaDentroLArea() {
		RivelazioneDellArtefatto.Pannello pannello = rivelazione.getPannello();
		assertFalse(pannello.getRighe().isEmpty());
		assertTrue(pannello.getRighe().get(0).getTesto().toLowerCase().contains(spada.getNome().toLowerCase().substring(0, 3)) || pannello.getRighe().size() > 1);
		assertTrue(pannello.x >= AREA.getX() && pannello.x + pannello.larghezza <= AREA.getX() + AREA.getLarghezza());
		assertTrue(pannello.y > rivelazione.fotogramma(1.5f).centroY, "il pannello sta sotto il cerchio");
		for (RivelazioneDellArtefatto.Riga riga : pannello.getRighe()) {
			assertTrue(riga.getX() >= 0 && riga.getX() + riga.getLarghezza() <= pannello.larghezza, "ogni riga sta dentro il pannello");
		}
	}

	@Test
	void l_oggettoNonSiIngrandisceOltreLeSueDimensioniVere() {
		RivelazioneDellArtefatto.Oggetto oggetto = rivelazione.fotogramma(1.5f).oggetto;
		assertNotNull(oggetto);
		assertTrue(oggetto.larghezza <= 40 && oggetto.altezza <= 60);
		assertEquals(1f, oggetto.alfa, 0.001f);
	}

	@Test
	void all_uscitaVolaVersoLaDestinazioneRimpicciolendo() {
		Fotogramma sosta = rivelazione.fotogramma(2.5f);
		Fotogramma uscita = rivelazione.fotogramma(PaginaIntermezzo.SECONDI_RIVELAZIONE_ARTEFATTO - 0.05f);
		assertNull(uscita.ritaglio, "uscendo non è più ritagliato");
		assertTrue(uscita.raggio < sosta.raggio / 2);
		assertTrue(Math.hypot(uscita.centroX - DESTINAZIONE.x, uscita.centroY - DESTINAZIONE.y) < Math.hypot(sosta.centroX - DESTINAZIONE.x, sosta.centroY - DESTINAZIONE.y));
		assertEquals(0f, uscita.opacitaDelPannello, 0.001f, "il pannello sfuma per primo");
	}

	@Test
	void ilTempoPassaInSecondiVeriEFinisceDopoLaDurata() {
		for (int i = 0; i < 100; i++) {
			rivelazione.avanza();
			ora[0] += 1_000_000L;
		}
		assertEquals(0.099f, rivelazione.getSecondi(), 0.001f, "cento disegni in un decimo di secondo non fanno passare più tempo");
		assertTrue(rivelazione.isAttivo());
		ora[0] += 6_000_000_000L;
		rivelazione.avanza();
		assertFalse(rivelazione.isAttivo());
	}

	@Test
	void conAltreInCodaLaSostaSiAccorcia() {
		rivelazione.avanza();
		rivelazione.accelera();
		ora[0] += 2_100_000_000L;
		rivelazione.avanza();
		assertTrue(rivelazione.isAttivo(), "dopo 2,1 secondi non ha ancora finito");
		ora[0] += 200_000_000L;
		rivelazione.avanza();
		assertFalse(rivelazione.isAttivo(), "dopo 2,3 secondi ha finito invece dei 5 normali");
	}

	@Test
	void laSfumaturaDelCerchioParteBiancaDalFuocoEFiniscePiuScuraDelColore() {
		Fotogramma f = rivelazione.fotogramma(1.5f);
		int alFuoco = f.sfera.argbIn(f.sfera.fuocoX, f.sfera.fuocoY);
		assertEquals(0xFFFFFF, alFuoco & 0xFFFFFF);
		int alBordo = f.sfera.argbIn(f.sfera.fuocoX + f.sfera.raggioDellaSfumatura, f.sfera.fuocoY);
		assertTrue((alBordo & 0xFF) < 0xFF || ((alBordo >> 8) & 0xFF) < 0xFF, "al bordo è più scura del bianco");
	}

	@Test
	void unaRisorsaDellOggettoPerLaSpada() {
		assertNotNull(RivelazioneDellArtefatto.risorsaDellOggetto(spada));
	}

	@Test
	void l_immagineDiUnIngredienteComeLAggiornamentoDiLivelloVieneIngrandita() {
		VistaArtefatto aggiornamento = new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.AGGIORNAMENTO, 4);
		String risorsa = RivelazioneDellArtefatto.risorsaDellOggetto(aggiornamento);
		assertEquals("oggetti/Pergamena.png", risorsa, "l'aggiornamento di livello ha l'immagine della pergamena");
		// Gli oggetti e le icone sono a metà risoluzione: vanno ingranditi dello zoom
		assertEquals(com.threeamigos.foresta.ui.LivelloDiZoom.valore(), RivelazioneDellArtefattoGdx.zoomDellImmagine(risorsa), 0.0001d);
		assertEquals(com.threeamigos.foresta.ui.LivelloDiZoom.valore(), RivelazioneDellArtefattoGdx.zoomDellImmagine("icone/Pergamena.png"), 0.0001d);
		assertEquals(com.threeamigos.foresta.ui.LivelloDiZoom.valore(), RivelazioneDellArtefattoGdx.zoomDellImmagine("oggetti/Gemma.png"), 0.0001d);
		// Il resto (i fondi, per esempio) non si ingrandisce
		assertEquals(1.0d, RivelazioneDellArtefattoGdx.zoomDellImmagine("fondi/Qualcosa.png"), 0.0001d);
	}
}
