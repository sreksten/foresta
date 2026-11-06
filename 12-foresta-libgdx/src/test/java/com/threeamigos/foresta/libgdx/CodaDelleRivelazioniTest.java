package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.sprite.CodaDelleRivelazioni;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La coda delle rivelazioni: una alla volta, la sosta si accorcia se ce n'è un'altra in attesa, un effetto a tutto schermo la fa aspettare.
 */
class CodaDelleRivelazioniTest {

	private static final class Finta implements CodaDelleRivelazioni.InCoda {
		boolean attiva = true;
		int accelerazioni;

		@Override
		public boolean isAttivo() {
			return attiva;
		}

		@Override
		public void accelera() {
			accelerazioni++;
		}
	}

	@Test
	void unaAllaVoltaEConLaSostaAccorciataSeNeAspettaUnAltra() {
		CodaDelleRivelazioni<Finta> coda = new CodaDelleRivelazioni<>();
		Finta prima = new Finta();
		Finta seconda = new Finta();
		coda.aggiungi(prima);
		coda.aggiungi(seconda);
		assertTrue(coda.isOccupata());
		assertSame(prima, coda.attiva(false));
		assertEquals(1, prima.accelerazioni, "ce n'è un'altra in attesa");
		coda.dopoIlDisegno();
		assertSame(prima, coda.attiva(false), "finché non ha finito resta lei");
		prima.attiva = false;
		coda.dopoIlDisegno();
		assertSame(seconda, coda.attiva(false));
		assertEquals(0, seconda.accelerazioni, "l'ultima fa tutta la sua sosta");
		seconda.attiva = false;
		coda.dopoIlDisegno();
		assertNull(coda.attiva(false));
		assertFalse(coda.isOccupata());
	}

	@Test
	void unEffettoATuttoSchermoLeFaAspettare() {
		CodaDelleRivelazioni<Finta> coda = new CodaDelleRivelazioni<>();
		Finta una = new Finta();
		coda.aggiungi(una);
		assertNull(coda.attiva(true));
		assertTrue(coda.isOccupata(), "aspetta, non è sparita");
		assertSame(una, coda.attiva(false));
	}
}
