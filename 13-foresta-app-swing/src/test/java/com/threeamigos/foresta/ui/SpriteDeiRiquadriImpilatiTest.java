package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCreazioneSpriteATempo;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.motore.PartitaDiAnteprima;
import com.threeamigos.foresta.motore.StatoDelMotore;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.TipoDiLayout;
import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Lo sprite nato in un riquadro della pila (le scorte di incantesimi e pozioni che cambiano, le statistiche di un personaggio) resta sotto i riquadri che
 * gli passano davanti mentre è ancora a schermo: si vede solo dove il suo riquadro non è coperto.
 */
class SpriteDeiRiquadriImpilatiTest {

	private static final int LARGHEZZA = 842;
	private static final int ALTEZZA = 505;

	private DisplayableCanvas canvas;
	private SchermoDelGioco<?, ?, ?, ?> schermo;

	@BeforeEach
	void creaIlTelefono() {
		assumeFalse(GraphicsEnvironment.isHeadless());
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		StatoDelMotore.registraComponenti();
		ImageCache.init();
		PartitaDiAnteprima.prepara(TipoPersonaggio.GUERRIERO, "Aldric", TipoPersonaggio.BARDO);
		canvas = new DisplayableCanvas(TipoDiLayout.TELEFONO, Orientamento.VERTICALE, LARGHEZZA, ALTEZZA, true, new GestorePunteggiInMemoria(),
				new VistaPartitaMotore());
		canvas.setSize(LARGHEZZA, ALTEZZA);
		schermo = canvas.getSchermo();
		schermo.iniziaGioco();
	}

	@AfterEach
	void ripristina() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
	}

	/** Quanti pixel lasciano gli sprite disegnati da soli su un'immagine vuota */
	private int pixelDegliSprite() {
		BufferedImage immagine = new BufferedImage(LARGHEZZA, ALTEZZA, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		schermo.perOgniSprite(sprite -> ((com.threeamigos.foresta.ui.sprite.SpriteInterface) sprite).anima(g));
		g.dispose();
		int pieni = 0;
		for (int y = 0; y < ALTEZZA; y++) {
			for (int x = 0; x < LARGHEZZA; x++) {
				if ((immagine.getRGB(x, y) >>> 24) != 0) {
					pieni++;
				}
			}
		}
		return pieni;
	}

	@Test
	void loSpriteDegliIncantesimiSiVedeSoloFinoACheIlLoroRiquadroNonVieneCoperto() {
		schermo.primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		Area incantesimi = schermo.getArea(schermo.finestra(TipoFinestra.INCANTESIMI_E_POZIONI));
		// Nel mezzo del riquadro, dove un altro riquadro che gli passa davanti lo copre di sicuro
		Misura icona = new Misura(ImageCache.spriteMappa.getWidth(), ImageCache.spriteMappa.getHeight());
		DescrizioneSpriteATempo descrizione = DescrizioneSpriteATempo.immagine(NomiDelleImmagini.SPRITE_MAPPA, icona,
				incantesimi.getX() + incantesimi.getLarghezza() / 2, incantesimi.getY() + incantesimi.getAltezza() / 2, "Prova")
				.delRiquadro(TipoFinestra.INCANTESIMI_E_POZIONI);
		BusEventi.pubblica(new InternoCreazioneSpriteATempo(descrizione));
		assertTrue(pixelDegliSprite() > 0, "col riquadro davanti lo sprite si vede");

		schermo.primoPiano(TipoFinestra.MAPPA);
		assertEquals(0, pixelDegliSprite(), "la mappa passa davanti: lo sprite resta sotto");

		schermo.primoPiano(TipoFinestra.INCANTESIMI_E_POZIONI);
		assertTrue(pixelDegliSprite() > 0, "di nuovo davanti, lo sprite ricompare");
	}

	@Test
	void unRiquadroCopertoSiVedeSoloNellaStrisciaInCima() {
		schermo.primoPiano(TipoFinestra.MAPPA);
		Area coperto = schermo.parteScopertaDelRiquadro(TipoFinestra.INCANTESIMI_E_POZIONI);
		Area intero = schermo.getArea(schermo.finestra(TipoFinestra.INCANTESIMI_E_POZIONI));
		assertEquals(intero.getY(), coperto.getY());
		assertEquals(schermo.quotaDelSuccessivo(TipoFinestra.INCANTESIMI_E_POZIONI) - intero.getY(), coperto.getAltezza());
		assertTrue(coperto.getAltezza() < intero.getAltezza());
		Area davanti = schermo.parteScopertaDelRiquadro(TipoFinestra.MAPPA);
		assertEquals(schermo.getArea(schermo.finestra(TipoFinestra.MAPPA)).getAltezza(), davanti.getAltezza(), "quello davanti si vede tutto");
	}
}
