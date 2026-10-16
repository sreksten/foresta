package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I testi dei modificatori hanno sempre le cifre latine, qualunque sia la lingua del dispositivo: il font bitmap del
 * gioco non sa disegnare le altre.
 */
class ModificatoreAttributoMDTest {

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

		assertEquals(TipoAttributo.FORTUNA.getNome() + " +5", fisso.descrizioneBreve());
		assertEquals(TipoAttributo.CORAGGIO.getNome() + " -20%", percentuale.descrizioneBreve());
	}
}
