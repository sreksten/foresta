package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.PartitaDiAnteprima;
import com.threeamigos.foresta.motore.StatoDelMotore;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il fumetto del tutorial in Swing: si disegna con il bordo del bersaglio giallo, e il pollice lo porta avanti fino a chiuderlo.
 */
class DisplayableCanvasTutorialTest {

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
		assertTrue(canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true).isPresent());
	}

	@Test
	void ilFumettoAppareConIlBordoDelBersaglio() {
		assertFalse(haIlBordoGiallo(disegna()));
		canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true);
		assertTrue(haIlBordoGiallo(disegna()), "il bordo del bersaglio deve lampeggiare, acceso appena si apre");
	}

	@Test
	void ilPoliceLoPortaAvantiPoiLoChiudeESegnaVisto() {
		canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true);
		canvas.getSchermo().pollicePremuto();
		assertTrue(canvas.getSchermo().getTutor().isAperto());
		canvas.getSchermo().pollicePremuto();
		assertFalse(canvas.getSchermo().getTutor().isAperto());
		assertTrue(RegistroSuggerimenti.isVisto(TipoSuggerimento.PRIMI_PASSI));
		assertFalse(haIlBordoGiallo(disegna()));
	}

	@Test
	void l_annullaSpegneIlTutorial() {
		canvas.getSchermo().getTutor().aggiorna(EnumSet.of(Situazione.PRIMA_SCHERMATA_DI_GIOCO), true);
		canvas.getSchermo().annullaPremuto();
		assertFalse(canvas.getSchermo().getTutor().isAperto());
		assertFalse(RegistroSuggerimenti.isAttivo());
	}
}
