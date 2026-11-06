package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.riquadri.RiquadroLocazione;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteEffetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli sprite della locazione (salute, magia, oggetto raccolto, effetti) creati dal modello comune, provati con la partita finta di chi sviluppa e con
 * immagini di misure fisse.
 */
class SpriteDellaLocazioneTest {

	private static final int LARGHEZZA = 800;

	private final VistaPartita partita = DatiDiProva.partita();
	private RiquadroLocazione riquadro;

	private static class InProva extends RiquadroLocazione {
		InProva(VistaPartita partita) {
			super(10, 10, LARGHEZZA, partita);
		}

		@Override
		protected Misura misuraDelPersonaggio(TipoPersonaggio classe) {
			return new Misura(60, 100);
		}

		@Override
		protected Misura misuraDellIcona(String risorsa) {
			return new Misura(16, 16);
		}

		@Override
		protected Misura misuraDelBottino(TipoBottino tipo) {
			return new Misura(40, 30);
		}

		@Override
		protected int altezzaDelloSfondo() {
			return 300;
		}

		@Override
		protected int larghezzaDelCentro() {
			return 40;
		}
	}

	@BeforeEach
	void dispone() {
		riquadro = new InProva(partita);
		riquadro.assegnaCoordinateAgliAvversari();
	}

	private VistaPersonaggio unPersonaggioDelGruppo() {
		return partita.getGruppoGiocatore().getPersonaggio(0);
	}

	@Test
	void laVariazioneDiSaluteDiUnPersonaggioInLocazioneDaLoSprite() {
		DescrizioneSpriteATempo sprite = riquadro.spriteDellaSalute(unPersonaggioDelGruppo(), -12);
		assertNotNull(sprite);
		assertEquals("-12", sprite.getTestoVariazione());
		assertEquals(NomiDelleImmagini.SPRITE_COMBATTIMENTO, sprite.getIcona());
		assertEquals(DoomdarkColorModel.Color.RED, sprite.getColore());
		// Dentro il riquadro
		assertTrue(sprite.getXIniziale() >= 10);
		assertTrue(sprite.getXIniziale() + sprite.getLarghezza() <= 10 + LARGHEZZA);
	}

	@Test
	void senzaVariazioneONonInLocazioneNonCeLoSprite() {
		assertNull(riquadro.spriteDellaSalute(unPersonaggioDelGruppo(), 0));
		VistaPersonaggio estraneo = partita.getGruppoGiocatore().getPersonaggio(3); // il caduto: non è in locazione
		assertNull(riquadro.spriteDellaMagia(estraneo, 5));
	}

	@Test
	void unSecondoSpriteVicinoPartePiuInBasso() {
		DescrizioneSpriteATempo primo = riquadro.spriteDellaSalute(unPersonaggioDelGruppo(), 5);
		DescrizioneSpriteATempo secondo = riquadro.spriteDellaMagia(unPersonaggioDelGruppo(), 5);
		assertTrue(secondo.getYIniziale() > primo.getYIniziale());
	}

	@Test
	void loggettoRaccoltoStaAlCentroInBassoConLaScalaDelloZoom() {
		DescrizioneSpriteATempo sprite = riquadro.spriteDelloOggettoRaccolto();
		assertNotNull(sprite);
		assertEquals(NomiDelleImmagini.oggetto(TipoBottino.COFANO), sprite.getIcona());
		assertEquals(10 + (LARGHEZZA - 40) / 2, sprite.getXIniziale(), 1e-6);
		assertEquals(40, sprite.getLarghezza());
		assertTrue(sprite.getScalaDellIcona() > 0);
	}

	@Test
	void gliEffettiVicinoSiSpostanoAlternativamenteSottoESopra() {
		VistaPersonaggio p = unPersonaggioDelGruppo();
		DescrizioneSpriteEffetto a = riquadro.spriteDellEffettoDiStato(p, "Avvelenato", DoomdarkColorModel.Color.GREEN);
		DescrizioneSpriteEffetto b = riquadro.spriteDellEffettoDiStato(p, "Lento", DoomdarkColorModel.Color.RED);
		DescrizioneSpriteEffetto c = riquadro.spriteDellEffettoDiStato(p, "Cieco", DoomdarkColorModel.Color.RED);
		assertEquals(a.getY() + 20, b.getY(), 1e-6);
		assertEquals(a.getY() - 20, c.getY(), 1e-6);
		assertNotNull(a.getColore());
	}

	@Test
	void unTestoDiUnColoreQualunqueHaIlColoreRgb() {
		DescrizioneSpriteEffetto s = riquadro.spriteDelTestoColoratoSulPersonaggio(unPersonaggioDelGruppo(), "Furtività +5", 0xFFFFA040);
		assertNull(s.getColore());
		assertEquals(0xFFFFA040, (int) s.getColoreRgb());
		DescrizioneSpriteEffetto piccolo = riquadro.spriteDelTestoSulPersonaggio(unPersonaggioDelGruppo(), "Mancato", DoomdarkFont.piccolo(),
				DoomdarkColorModel.Color.MEDIUM_GRAY);
		assertEquals(DoomdarkFont.piccolo(), piccolo.getFont());
	}
}
