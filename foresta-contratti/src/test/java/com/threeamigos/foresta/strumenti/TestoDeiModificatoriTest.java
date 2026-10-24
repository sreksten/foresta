package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I testi dei modificatori hanno sempre le cifre latine, qualunque sia la lingua del dispositivo: il font bitmap del
 * gioco non sa disegnare le altre.
 */
class TestoDeiModificatoriTest {

	private Locale lingua;

	@BeforeEach
	void mettiIlTelefonoInArabo() {
		lingua = Locale.getDefault();
		Locale.setDefault(new Locale("ar", "EG"));
	}

	@AfterEach
	void ripristinaLaLingua() {
		Locale.setDefault(lingua);
	}

	@Test
	void ilTestoUsaLeCifreLatine() {
		ModificatoreAttributoMD fisso = new ModificatoreAttributoMD(TipoAttributo.FORTUNA,
				TipoModificatore.VARIAZIONE_FISSA, 5);
		ModificatoreAttributoMD percentuale = new ModificatoreAttributoMD(TipoAttributo.CORAGGIO,
				TipoModificatore.VARIAZIONE_PERCENTUALE, -20);

		assertEquals(TipoAttributo.FORTUNA.getNome() + " +5", TestoDeiModificatori.breve(fisso));
		assertEquals(TipoAttributo.FORTUNA.getNome() + " +5", TestoDeiModificatori.effetto(fisso));
		assertEquals(TipoAttributo.CORAGGIO.getNome() + " -20%", TestoDeiModificatori.breve(percentuale));
		assertEquals(TipoAttributo.CORAGGIO.getNome() + " -20%", TestoDeiModificatori.effetto(percentuale));
	}

	@Test
	void portaAZeroDurataESulBersaglioSiScrivonoCosi() {
		ModificatoreAttributoMD azzera = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA,
				0, "", UnitaDurataModificatore.TURNI, 5, BersaglioModificatore.BERSAGLIO);

		assertEquals("Fortuna porta a 0", TestoDeiModificatori.effetto(azzera));
		assertEquals("per 5 turni", TestoDeiModificatori.durata(azzera));
		assertEquals("Fortuna porta a 0 sul bersaglio per 5 turni", TestoDeiModificatori.completo(azzera));
	}
}
