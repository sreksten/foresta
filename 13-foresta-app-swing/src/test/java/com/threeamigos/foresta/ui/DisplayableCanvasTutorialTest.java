package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.PartitaDiAnteprima;
import com.threeamigos.foresta.motore.StatoDelMotore;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.tutorial.FumettoDelTutorial;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il fumetto del tutorial in Swing: si disegna con il bordo del bersaglio giallo, e il pollice lo porta avanti fino a chiuderlo.
 */
class DisplayableCanvasTutorialTest {

	private static final Predicate<TipoSuggerimento> SI = suggerimento -> true;

	private static final int COLORE_DEL_BORDO = 0xFFFFD700;
	private DisplayableCanvas canvas;

	@BeforeEach
	void creaCanvas() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		RegistroSuggerimenti.ripristina();
		// Come in Main: i dati della partita e i componenti che ricordano messaggi e notizie
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		StatoDelMotore.registraComponenti();
		ImageCache.init();
		PartitaDiAnteprima.prepara(TipoPersonaggio.GUERRIERO, "Aldric", TipoPersonaggio.BARDO, TipoPersonaggio.MAGO);
		canvas = new DisplayableCanvas(com.threeamigos.foresta.tipi.Orientamento.ORIZZONTALE, 1024, 768, false,
				new GestorePunteggiInMemoria(), new VistaPartitaMotore());
		canvas.setSize(1024, 768);
		canvas.getSchermo().iniziaGioco();
	}

	@AfterEach
	void ripristina() {
		RegistroSuggerimenti.ripristina();
		RegistroSuggerimenti.setMostrabile(false);
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
	}

	private BufferedImage disegna() {
		BufferedImage immagine = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		canvas.paint(g);
		g.dispose();
		return immagine;
	}

	private static boolean haIlBordoGiallo(BufferedImage immagine) {
		for (int y = 0; y < immagine.getHeight(); y++) {
			for (int x = 0; x < immagine.getWidth(); x++) {
				if (immagine.getRGB(x, y) == COLORE_DEL_BORDO) {
					return true;
				}
			}
		}
		return false;
	}

	@Test
	void ilToolkitAbilitaIlTutorial() {
		assertTrue(canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI).isPresent());
	}

	@Test
	void ilFumettoAppareConIlBordoDelBersaglio() {
		assertFalse(haIlBordoGiallo(disegna()));
		canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI);
		assertTrue(haIlBordoGiallo(disegna()), "il bordo del bersaglio deve lampeggiare, acceso appena si apre");
	}

	@Test
	void ilPoliceLoPortaAvantiPoiLoChiudeESegnaVisto() {
		canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI);
		canvas.getSchermo().pollicePremuto();
		assertTrue(canvas.getSchermo().getTutor().isAperto());
		canvas.getSchermo().pollicePremuto();
		assertFalse(canvas.getSchermo().getTutor().isAperto());
		assertTrue(RegistroSuggerimenti.isVisto(TipoSuggerimento.PRIMI_PASSI));
		assertFalse(haIlBordoGiallo(disegna()));
	}

	@Test
	void andandoNellaMappaEtornandoIlSuggerimentoRiparteDallaPaginaACuiEra() {
		EnumSet<Situazione> movimento = EnumSet.of(Situazione.ICONE_DI_MOVIMENTO_PRESENTI);
		assertEquals(TipoSuggerimento.MOVIMENTO_E_MAPPA, canvas.getSchermo().getTutor().aggiorna(movimento, SI).get().getSuggerimento());
		canvas.getSchermo().pollicePremuto();
		assertEquals(2, canvas.getSchermo().getTutor().getFumetto().get().getPagina());
		// Si apre la mappa a tutto schermo: il fumetto non si vede e, a fotogramma finito, si chiude senza essere segnato letto
		canvas.getSchermo().mappa();
		disegna();
		assertFalse(canvas.getSchermo().getTutor().isAperto());
		assertFalse(RegistroSuggerimenti.isVisto(TipoSuggerimento.MOVIMENTO_E_MAPPA));
		// Si torna al gioco: il suggerimento si riapre dalla seconda pagina, non dalla prima
		canvas.getSchermo().iniziaGioco();
		FumettoDelTutorial riaperto = canvas.getSchermo().getTutor().aggiorna(movimento, SI).orElseThrow(AssertionError::new);
		assertEquals(TipoSuggerimento.MOVIMENTO_E_MAPPA, riaperto.getSuggerimento());
		assertEquals(2, riaperto.getPagina());
	}

	@Test
	void l_annullaSpegneIlTutorial() {
		canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), SI);
		canvas.getSchermo().annullaPremuto();
		assertFalse(canvas.getSchermo().getTutor().isAperto());
		assertFalse(RegistroSuggerimenti.isAttivo());
	}

	@Test
	void nellaBarraVerticaleIlBordoSegueLIconaIngranditaDalDock() throws InterruptedException {
		// Given: la barra a destra, come il Dock, con alcune icone, e il suggerimento sulla barra aperto
		canvas = new DisplayableCanvas(com.threeamigos.foresta.tipi.Orientamento.VERTICALE, 1024, 768, true,
				new GestorePunteggiInMemoria(), new VistaPartitaMotore());
		canvas.setSize(1024, 768);
		canvas.getSchermo().iniziaGioco();
		com.threeamigos.foresta.ui.barra.BarraIcone barra = canvas.getSchermo().barraIcone();
		assertTrue(barra instanceof com.threeamigos.foresta.ui.barra.BarraIconeDock);
		com.threeamigos.foresta.ui.barra.ComandiPossibili.set(java.util.Arrays.asList(com.threeamigos.foresta.tipi.Comando.NORD,
				com.threeamigos.foresta.tipi.Comando.SUD, com.threeamigos.foresta.tipi.Comando.EST));
		try {
			barra.impostaAzioni();
			assertTrue(canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.BARRA_DELLE_ICONE_VISIBILE),
					s -> s == TipoSuggerimento.BARRA_DELLE_ICONE).isPresent());
			java.util.List<Area> aRiposo = canvas.getSchermo().getAreeDaBordare(com.threeamigos.foresta.tipi.BersaglioDelSuggerimento.BARRA_DELLE_ICONE);
			int sinistraARiposo = Integer.MAX_VALUE;
			for (Area area : aRiposo) {
				sinistraARiposo = Math.min(sinistraARiposo, area.getX());
			}
			// When: il cursore si ferma sulla seconda icona e l'effetto finisce di crescere
			Area seconda = barra.iconeVisibili.get(1).rettangolo;
			barra.processaMovimento(seconda.getX() + seconda.getLarghezza() / 2, seconda.getY() + seconda.getAltezza() / 2);
			Thread.sleep(200);
			disegna();
			// Then: le aree da bordare sono le icone come sono disegnate adesso, una per una, e la seconda sporge a sinistra delle altre
			java.util.List<Area> ingrandite = canvas.getSchermo().getAreeDaBordare(com.threeamigos.foresta.tipi.BersaglioDelSuggerimento.BARRA_DELLE_ICONE);
			assertEquals(barra.iconeVisibili.size(), ingrandite.size());
			Area ingrandita = barra.rettangoloDisegnato(1);
			assertEquals(barra.getOffsetX() + ingrandita.getX(), ingrandite.get(1).getX());
			assertTrue(ingrandite.get(1).getX() < sinistraARiposo, "l'icona ingrandita sporge verso il contenuto");
			// E il bordo giallo, disegnato da ContornoDiRettangoli, la segue: a sinistra della barra a riposo c'è del giallo. Il bordo lampeggia: si
			// ridisegna finché non è acceso (al più un secondo, più di un periodo intero)
			boolean gialloFuoriDallaBarra = false;
			int y = ingrandite.get(1).getY() + ingrandite.get(1).getAltezza() / 2;
			long fine = System.currentTimeMillis() + 1000;
			while (!gialloFuoriDallaBarra && System.currentTimeMillis() < fine) {
				BufferedImage immagine = disegna();
				for (int x = ingrandite.get(1).getX() - 10; x < sinistraARiposo - 3; x++) {
					gialloFuoriDallaBarra |= x >= 0 && immagine.getRGB(x, y) == COLORE_DEL_BORDO;
				}
			}
			assertTrue(gialloFuoriDallaBarra, "il bordo del tutorial segue l'icona ingrandita");
		} finally {
			com.threeamigos.foresta.ui.barra.ComandiPossibili.reimposta();
		}
	}

	@Test
	void sulTelefonoIlSuggerimentoSullaMappaLaPortaDavantiEBordaIlSuoPostoNellaPila() {
		// Given: il layout del telefono più stretto, con la mappa nella pila (dietro alle missioni)
		canvas = new DisplayableCanvas(com.threeamigos.foresta.tipi.TipoDiLayout.TELEFONO, com.threeamigos.foresta.tipi.Orientamento.VERTICALE, 842, 505,
				true, new GestorePunteggiInMemoria(), new VistaPartitaMotore());
		canvas.setSize(842, 505);
		canvas.getSchermo().iniziaGioco();
		SchermoDelGioco<?, ?, ?, ?> schermo = canvas.getSchermo();
		java.util.List<com.threeamigos.foresta.tipi.TipoFinestra> pila = schermo.riquadriImpilati();
		assertNotEquals(com.threeamigos.foresta.tipi.TipoFinestra.MAPPA, pila.get(pila.size() - 1), "all'inizio la mappa sta dietro");
		// When: si apre il suggerimento del movimento e si va alla pagina che indica la mappa
		schermo.getTutor().aggiorna(EnumSet.of(Situazione.ICONE_DI_MOVIMENTO_PRESENTI), SI);
		schermo.pollicePremuto();
		assertEquals(com.threeamigos.foresta.tipi.BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA, schermo.getTutor().getFumetto().get().getBersaglio());
		BufferedImage immagine = new BufferedImage(842, 505, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		canvas.paint(g);
		g.dispose();
		// Then: la mappa è davanti, e il bordo è attorno al suo posto nella pila, largo quanto la cornice grande
		pila = schermo.riquadriImpilati();
		assertEquals(com.threeamigos.foresta.tipi.TipoFinestra.MAPPA, pila.get(pila.size() - 1));
		Area bersaglio = schermo.getAreaDelBersaglio(com.threeamigos.foresta.tipi.BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA);
		assertEquals(schermo.getDisposizione().getGruppo().getLarghezza(), bersaglio.getLarghezza());
		assertEquals(schermo.getDisposizione().quotaNellaPila(pila.size() - 1), bersaglio.getY());
	}
}
