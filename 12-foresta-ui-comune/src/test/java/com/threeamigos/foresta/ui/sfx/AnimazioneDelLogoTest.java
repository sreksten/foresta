package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.immagini.LetturaPng;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AnimazioneDelLogoTest {

	private static final String RADICE = "/com/threeamigos/foresta/img/";

	private static ImmagineDiPixel logo3AM;

	@BeforeAll
	static void caricaLogo() {
		logo3AM = leggi("Logo3AM.png");
	}

	private static ImmagineDiPixel leggi(String nome) {
		return LetturaPng.leggiRisorsa(RADICE + nome);
	}

	@Test
	void nelLogo3AMTrovaTreLettereEIlBucoDellaA() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).costruisci();
		assertTrue(effetto.getDiagnostica().get(0).startsWith("4 forma/e rilevate (3 contorni esterni, 1 buchi interni)"), effetto.getDiagnostica().get(0));
		assertEquals(4, effetto.getCorridori().size());
	}

	@Test
	void conAmbiVersiCeUnCorridoreOrarioEUnoAntiorarioPerOgniForma() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).ambiVersi(true).costruisci();
		List<String> corridori = effetto.getDiagnostica().stream().filter(d -> d.startsWith("Corridore:")).collect(Collectors.toList());
		assertEquals(8, corridori.size(), corridori.toString());
		assertEquals(4, corridori.stream().filter(d -> d.contains("verso orario")).count());
		assertEquals(4, corridori.stream().filter(d -> d.contains("verso antiorario")).count());
		// la coppia parte dallo stesso punto e ognuno fa lo stesso numero di passi, cosi' si toccano a meta' del contorno
		for (int i = 0; i < corridori.size(); i += 2) {
			assertEquals(punti(corridori.get(i)), punti(corridori.get(i + 1)), corridori.get(i) + " / " + corridori.get(i + 1));
			assertEquals(partenza(corridori.get(i)), partenza(corridori.get(i + 1)));
		}
	}

	@Test
	void conAmbiVersiIlTracciamentoDuraMezzoGiro() {
		int normale = fotogrammiDiTracciamento(AnimazioneDelLogo.costruttore(logo3AM).costruisci());
		int ambi = fotogrammiDiTracciamento(AnimazioneDelLogo.costruttore(logo3AM).ambiVersi(true).costruisci());
		// Ognuno dei due fa mezza forma: in circa la meta' del tempo il giro e' completo (poi la dissolvenza, come sempre)
		assertTrue(ambi < normale * 0.6, ambi + " contro " + normale);
		assertTrue(ambi > normale * 0.4, ambi + " contro " + normale);
	}

	@Test
	void conAmbiVersiTuttoIlCicloFinisce() {
		AnimazioneDelLogo ambi = AnimazioneDelLogo.costruttore(logo3AM).ambiVersi(true).costruisci();
		int fotogrammi = 0;
		while (!ambi.isFinito() && fotogrammi < 3 * ambi.stimaFotogrammiCiclo()) {
			ambi.avanza();
			fotogrammi++;
		}
		assertTrue(ambi.isFinito());
	}

	private static int fotogrammiDiTracciamento(AnimazioneDelLogo effetto) {
		int fotogrammi = 0;
		while (effetto.getFase() == AnimazioneDelLogo.Fase.TRACCIAMENTO && fotogrammi < 10 * effetto.stimaFotogrammiCiclo()) {
			effetto.avanza();
			fotogrammi++;
		}
		return fotogrammi;
	}

	private static int punti(String corridore) {
		Matcher m = Pattern.compile("(\\d+) punti").matcher(corridore);
		assertTrue(m.find(), corridore);
		return Integer.parseInt(m.group(1));
	}

	private static String partenza(String corridore) {
		Matcher m = Pattern.compile("partenza agganciata a (\\([^)]*\\))").matcher(corridore);
		assertTrue(m.find(), corridore);
		return m.group(1);
	}

	@Test
	void senzaRipetizioneFinisceDopoUnCiclo() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).costruisci();
		int fotogrammi = 0;
		while (!effetto.isFinito()) {
			effetto.avanza();
			fotogrammi++;
			assertTrue(fotogrammi <= effetto.stimaFotogrammiCiclo(), "non finisce entro la stima");
		}
		assertEquals("PAUSA", effetto.getNomeFase());
		effetto.avanza();
		assertTrue(effetto.isFinito(), "finito resta finito");

		effetto.ricomincia();
		assertFalse(effetto.isFinito());
		assertEquals("TRACCIAMENTO", effetto.getNomeFase());
	}

	@Test
	void conLaRipetizioneNonFinisceMai() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).ripeti(true).costruisci();
		for (int i = 0; i < 3 * effetto.stimaFotogrammiCiclo(); i++) {
			effetto.avanza();
			assertFalse(effetto.isFinito());
		}
	}

	@Test
	void conLaScomparsaLoSpegneEPoiFinisce() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).sfumaScie(true).fotogrammiPausa(10).fotogrammiScomparsa(20).costruisci();
		while (!effetto.isFinito()) {
			effetto.avanza();
		}
		assertEquals(AnimazioneDelLogo.Fase.SCOMPARSA, effetto.getFase());
		assertEquals(0.0, effetto.getOpacitaLogo(), 1e-9);
		assertEquals(0.0, effetto.getOpacitaScie(), 1e-9);
	}

	@Test
	void leOpacitaSeguonoLeFasi() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).sfumaScie(true).costruisci();
		assertTrue(effetto.isTorciaAttiva());
		assertTrue(effetto.isConTeste());
		assertEquals(0.0, effetto.getOpacitaLogo(), 1e-9);
		assertEquals(1.0, effetto.getOpacitaScie(), 1e-9);
		while (effetto.getFase() == AnimazioneDelLogo.Fase.TRACCIAMENTO) {
			effetto.avanza();
		}
		assertFalse(effetto.isTorciaAttiva());
		assertFalse(effetto.isConTeste(), "con le scie sfumate le teste spariscono appena finisce il tracciamento");
		while (effetto.getFase() == AnimazioneDelLogo.Fase.DISSOLVENZA) {
			assertTrue(effetto.getOpacitaLogo() + effetto.getOpacitaScie() <= 1.0 + 1e-9);
			effetto.avanza();
		}
		assertEquals(1.0, effetto.getOpacitaLogo(), 1e-9);
		assertEquals(0.0, effetto.getOpacitaScie(), 1e-9);
	}

	@Test
	void senzaTorciaIlLogoSiVedeSubito() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).rivelaConTorcia(false).costruisci();
		assertFalse(effetto.isTorciaAttiva());
		assertEquals(1.0, effetto.getOpacitaLogo(), 1e-9);
	}

	@Test
	void unCorridoreRitardatoNonSiVedeFinoAllaPartenza() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).ritardi(java.util.Arrays.asList(0, 5)).costruisci();
		assertTrue(effetto.getCorridori().get(0).isPartito());
		assertFalse(effetto.getCorridori().get(1).isPartito());
		for (int i = 0; i < 5; i++) {
			effetto.avanza();
		}
		assertTrue(effetto.getCorridori().get(1).isPartito());
	}

	@Test
	void ilCorridoreAvanzaLungoIlPercorsoFinoInFondo() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).costruisci();
		AnimazioneDelLogo.Corridore corridore = effetto.getCorridori().get(0);
		assertEquals(0, corridore.getIndice());
		effetto.avanza();
		assertTrue(corridore.getIndice() > 0);
		while (effetto.getFase() == AnimazioneDelLogo.Fase.TRACCIAMENTO) {
			effetto.avanza();
		}
		assertEquals(corridore.getPercorso().size() - 1, corridore.getIndice());
	}

	@Test
	void unaRegioneFuoriDallImmagineERifiutata() {
		assertThrows(IllegalArgumentException.class, () -> AnimazioneDelLogo.costruttore(logo3AM).regione(new Area(200, 0, 50, 50)).costruisci());
	}

	@Test
	void unImmagineVuotaNonHaContorni() {
		ImmagineDiPixel vuota = new ImmagineDiPixel(20, 20, new int[400]);
		assertThrows(IllegalStateException.class, () -> AnimazioneDelLogo.costruttore(vuota).costruisci());
	}

	@Test
	void nelLogoSenzaOmbraTrovaLeStesseQuattroFormeEIlLogoCompletoSiMostraAlSuoPosto() {
		ImmagineDiPixel scritta = leggi("3AM_logo_senza_ombra.png");
		ImmagineDiPixel completo = leggi("3AM_logo_completo.png");
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(scritta).immagineDaMostrare(completo).costruisci();
		assertTrue(effetto.getDiagnostica().get(0).startsWith("4 forma/e rilevate (3 contorni esterni, 1 buchi interni)"), effetto.getDiagnostica().get(0));
		assertSame(completo, effetto.getLogo());
		assertEquals(completo.getLarghezza(), effetto.getLarghezza());
		assertEquals(completo.getAltezza(), effetto.getAltezza());
	}

	@Test
	void limmagineDaMostrareDeveAvereLaDimensioneDelLogoDaTracciare() {
		ImmagineDiPixel altra = new ImmagineDiPixel(logo3AM.getLarghezza() + 1, logo3AM.getAltezza(), new int[(logo3AM.getLarghezza() + 1) * logo3AM.getAltezza()]);
		assertThrows(IllegalArgumentException.class, () -> AnimazioneDelLogo.costruttore(logo3AM).immagineDaMostrare(altra).costruisci());
	}

	@Test
	void laTorciaRivelaIlLogoSoloVicinoAiCorridori() {
		AnimazioneDelLogo effetto = AnimazioneDelLogo.costruttore(logo3AM).costruisci();
		for (int i = 0; i < 30; i++) {
			effetto.avanza();
		}
		int[] rivelato = new TorciaDelLogo(effetto.getLogo()).rivela(effetto.getCorridori(), effetto.getRaggioTorcia());
		int w = effetto.getLarghezza();
		int visibili = 0;
		for (int i = 0; i < rivelato.length; i++) {
			int alpha = rivelato[i] >>> 24;
			if (alpha > 0) {
				visibili++;
				assertTrue(alpha <= TorciaDelLogo.ALPHA_MASSIMA, "l'alpha del logo sotto la torcia non supera " + TorciaDelLogo.ALPHA_MASSIMA);
				assertTrue(vicinoAUnCorridore(effetto, i % w, i / w), "pixel " + (i % w) + "," + (i / w) + " rivelato lontano da ogni corridore");
			}
		}
		assertTrue(visibili > 0, "la torcia non rivela niente");
		assertTrue(visibili < rivelato.length / 4, "la torcia rivela troppo: " + visibili);
	}

	private static boolean vicinoAUnCorridore(AnimazioneDelLogo effetto, int x, int y) {
		for (AnimazioneDelLogo.Corridore c : effetto.getCorridori()) {
			if (c.isPartito() && Math.hypot(x + 0.5 - c.getPosizione().x, y + 0.5 - c.getPosizione().y) < effetto.getRaggioTorcia()) {
				return true;
			}
		}
		return false;
	}

	@Test
	void leScieSiAccumulanoESonoPiuOpacheSulTratto() {
		ScieDelLogo scie = new ScieDelLogo(40, 20);
		assertTrue(scie.isCambiato());
		scie.segnaLetto();
		scie.aggiungi(new AnimazioneDelLogo.Posizione(5, 9.5), new AnimazioneDelLogo.Posizione(30, 9.5));
		assertTrue(scie.isCambiato());
		int[] pixel = scie.getPixel();
		int sulTratto = pixel[9 * 40 + 15] >>> 24;
		int vicino = pixel[6 * 40 + 15] >>> 24;
		int lontano = pixel[0] >>> 24;
		assertEquals(255, sulTratto, "il nucleo, al centro di una riga di pixel, e' bianco pieno");
		assertTrue(vicino > 0 && vicino < sulTratto, "l'alone e' piu' trasparente del nucleo: " + vicino);
		assertEquals(0, lontano);
		scie.azzera();
		assertEquals(0, scie.getPixel()[9 * 40 + 15]);
	}

	@Test
	void laTestaDellaCometaHaIlNucleoPienoELAloneCheSvanisce() {
		ImmagineDiPixel testa = TestaDellaCometa.immagine();
		int lato = 2 * TestaDellaCometa.RAGGIO;
		assertEquals(lato, testa.getLarghezza());
		assertEquals(lato, testa.getAltezza());
		int centro = testa.getArgb(TestaDellaCometa.RAGGIO, TestaDellaCometa.RAGGIO) >>> 24;
		int meta = testa.getArgb(TestaDellaCometa.RAGGIO + 8, TestaDellaCometa.RAGGIO) >>> 24;
		int angolo = testa.getArgb(0, 0) >>> 24;
		assertEquals(255, centro);
		assertTrue(meta > 0 && meta < 255, "meta' raggio: " + meta);
		assertEquals(0, angolo);
	}

	@Test
	void ilLogoInizialeSiTracciaSullaScrittaESiMostraCompletoEFinisceDopoLaScomparsa() {
		AnimazioneDelLogo effetto = LogoIniziale.animazione();
		assertEquals(4, effetto.getCorridori().size());
		assertEquals(584, effetto.getLarghezza());
		assertEquals(225, effetto.getAltezza());
		assertEquals(LogoIniziale.RAGGIO_TORCIA, effetto.getRaggioTorcia());
		int fotogrammi = 0;
		while (!effetto.isFinito()) {
			effetto.avanza();
			assertTrue(++fotogrammi <= effetto.stimaFotogrammiCiclo(), "non finisce entro la stima");
		}
		assertEquals(AnimazioneDelLogo.Fase.SCOMPARSA, effetto.getFase());
		assertEquals(0.0, effetto.getOpacitaLogo(), 1e-9);
	}
}
