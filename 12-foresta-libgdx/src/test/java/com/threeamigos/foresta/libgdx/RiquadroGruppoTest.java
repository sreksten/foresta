package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.ui.riquadri.RiquadroGruppo;
import com.threeamigos.foresta.ui.riquadri.VoceDiTesto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il modello del riquadro del gruppo (nel modulo comune), provato con la partita finta di chi sviluppa: sette personaggi, uno caduto, senza ospiti
 */
class RiquadroGruppoTest {

	private static class InProva extends RiquadroGruppo {
		InProva() {
			super(20, 20, 368, 288, DatiDiProva.partita());
		}

		@Override
		protected com.threeamigos.foresta.ui.Misura misuraDellIcona(String risorsa) {
			return new com.threeamigos.foresta.ui.Misura(16, 16);
		}

		void sincronizza() {
			sincronizzaScorrimento();
		}
	}

	@Test
	void conSettePersonaggiLaListaScorreEAllInizioSiVedonoSoloIPrimi() {
		InProva riquadro = new InProva();
		riquadro.sincronizza();
		assertTrue(riquadro.isScorrevole());
		assertFalse(riquadro.isScorribileSu());
		assertTrue(riquadro.isScorribileGiu());
		List<String> testi = new ArrayList<>();
		for (VoceDiTesto voce : riquadro.voci()) {
			testi.add(voce.getTesto());
		}
		assertTrue(testi.get(0).contains("Guerriero"));
		assertTrue(testi.stream().anyMatch(t -> t.contains("Ladro")));
		// Il settimo personaggio, in fondo, non si vede ancora
		assertFalse(testi.stream().anyMatch(t -> t.contains("Chierico")));
	}

	@Test
	void iValoriSiAllineanoADestraELeEtichetteASinistra() {
		InProva riquadro = new InProva();
		riquadro.sincronizza();
		VoceDiTesto etichetta = null;
		VoceDiTesto valore = null;
		for (VoceDiTesto voce : riquadro.voci()) {
			if (etichetta == null && voce.getTesto().equals("Sl:")) {
				etichetta = voce;
			} else if (etichetta != null && valore == null) {
				valore = voce;
			}
		}
		assertFalse(etichetta.isAllineataADestra());
		assertTrue(valore.isAllineataADestra());
		assertEquals(etichetta.getY(), valore.getY(), "sulla stessa riga");
		assertTrue(valore.getX() > etichetta.getX());
	}

	@Test
	void unCadutoSiScrivePiuScuroConLaCausaDellaMorteACapo() {
		InProva riquadro = new InProva();
		riquadro.sincronizza();
		boolean trovato = false;
		for (VoceDiTesto voce : riquadro.voci()) {
			if (voce.getTesto().equals("Ucciso da un goblin")) {
				assertEquals(DoomdarkColorModel.Color.DARK_GRAY, voce.getColore());
				assertTrue(voce.getLarghezzaMassima() > 0, "la causa può andare a capo");
				trovato = true;
			}
		}
		assertTrue(trovato);
	}

	@Test
	void scorrendoLaListaCambianoLeScritteVisibili() {
		InProva riquadro = new InProva();
		riquadro.sincronizza();
		int prima = riquadro.voci().size();
		riquadro.processaRotella(0, 0, 200, com.threeamigos.foresta.ui.Finestra.MovimentoRotella.GIU);
		for (int i = 0; i < 400; i++) {
			riquadro.sincronizza();
		}
		boolean ultimoVisibile = false;
		for (VoceDiTesto voce : riquadro.voci()) {
			ultimoVisibile |= voce.getTesto().contains("Chierico");
		}
		assertTrue(ultimoVisibile, "in fondo alla lista si vede l'ultimo personaggio (voci prima: " + prima + ")");
		assertTrue(riquadro.isScorribileSu());
	}
}
