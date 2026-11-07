package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il grimorio: tutti gli incantesimi nell'elenco, e nella scheda quel che fa l'incantesimo scelto e le reazioni del suo danno con gli stati alterati.
 */
class GrimorioTest {

	private static final int LARGHEZZA = 600;
	private static final Function<String, Misura> MISURE = risorsa -> new Misura(32, 32);

	private static String testo(ContenutoDiColonna contenuto) {
		return contenuto.getElementi().stream().filter(e -> e.getRighe() != null).flatMap(e -> e.getRighe().stream()).collect(Collectors.joining(" "));
	}

	private static SchermataGrimorio schermata() {
		return new SchermataGrimorio(1280, 720) {
			@Override
			protected Misura misuraDellImmagine(String risorsa) {
				return new Misura(16, 16);
			}
		};
	}

	@Test
	void lElencoHaTuttiGliIncantesimi() {
		List<TipoIncantesimo> voci = ContenutiDelGrimorio.voci();
		assertEquals(TipoIncantesimo.values().length, voci.size());

		String elenco = testo(ContenutiDelGrimorio.elenco(voci, 0, 300, MISURE));

		for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
			assertTrue(elenco.contains(incantesimo.getNome()), incantesimo.getNome());
		}
	}

	@Test
	void laSchedaDelFulmineDiceLeReazioniDelSuoDanno() {
		String scheda = testo(ContenutiDelGrimorio.scheda(TipoIncantesimo.FULMINE, LARGHEZZA, MISURE));

		assertTrue(scheda.contains(TipoIncantesimo.FULMINE.getEffetto()), "l'effetto");
		assertTrue(scheda.contains("Elettrocuzione"), "il fulmine su chi è bagnato");
		assertTrue(scheda.contains("Superconduzione"), "il fulmine su chi è congelato");
		assertTrue(scheda.contains("x1,5"), "il moltiplicatore con la virgola");
	}

	@Test
	void unIncantesimoSenzaDannoNonHaReazioniNeTipoDiDanno() {
		String scheda = testo(ContenutiDelGrimorio.scheda(TipoIncantesimo.RESURREZIONE, LARGHEZZA, MISURE));

		assertTrue(scheda.contains(TipoIncantesimo.RESURREZIONE.getEffetto()));
		assertFalse(scheda.contains("Elettrocuzione"));
	}

	@Test
	void ognunoDegliIncantesimiHaUnaScheda() {
		for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
			assertTrue(ContenutiDelGrimorio.scheda(incantesimo, LARGHEZZA, MISURE).getAltezza() > 0, incantesimo.name());
		}
	}

	@Test
	void ognunoDegliIncantesimiHaLaSuaIconaNellElencoENellaSchedaIngrandita() {
		for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
			String risorsa = ContenutiDelGrimorio.risorsaDellIcona(incantesimo);
			List<Elemento> inElenco = ContenutiDelGrimorio.elenco(ContenutiDelGrimorio.voci(), 0, 300, MISURE).getElementi();
			assertTrue(inElenco.stream().anyMatch(e -> risorsa.equals(e.getRisorsa()) && e.getLarghezza() == 32), "elenco " + incantesimo);
			List<Elemento> inScheda = ContenutiDelGrimorio.scheda(incantesimo, LARGHEZZA, MISURE).getElementi();
			assertTrue(inScheda.stream().anyMatch(e -> risorsa.equals(e.getRisorsa()) && e.getLarghezza() == 64), "scheda " + incantesimo);
		}
	}

	@Test
	void iDanniBaseCiSonoSoloPerGliIncantesimiCheNeFanno() {
		assertEquals(45, ContenutiDelGrimorio.danniBase(TipoIncantesimo.ACQUA));
		assertEquals(0, ContenutiDelGrimorio.danniBase(TipoIncantesimo.RESURREZIONE));
		assertTrue(testo(ContenutiDelGrimorio.scheda(TipoIncantesimo.ACQUA, LARGHEZZA, MISURE)).contains("Danni base: 45."));
		assertFalse(testo(ContenutiDelGrimorio.scheda(TipoIncantesimo.RESURREZIONE, LARGHEZZA, MISURE)).contains("Danni base"));
		assertTrue(testo(ContenutiDelGrimorio.scheda(TipoIncantesimo.ACQUA, LARGHEZZA, MISURE)).contains("Aumentano con il livello"));
		assertFalse(testo(ContenutiDelGrimorio.scheda(TipoIncantesimo.RESURREZIONE, LARGHEZZA, MISURE)).contains("Aumentano"));
	}

	@Test
	void laDescrizioneVieneSubitoDopoIlNomeESeguonoPortataECosti() {
		String scheda = testo(ContenutiDelGrimorio.scheda(TipoIncantesimo.ACQUA, LARGHEZZA, MISURE));

		int effetto = scheda.indexOf(TipoIncantesimo.ACQUA.getEffetto().substring(0, 20));
		assertTrue(effetto >= 0 && effetto < scheda.indexOf("Colpisce"), "prima la descrizione, poi la portata");
		assertTrue(scheda.indexOf("Danni base") < scheda.indexOf("Costo di lancio"), "i danni base prima dei costi");
		assertTrue(scheda.contains("Costo di lancio: 1 Magia."));
	}

	@Test
	void ilNumeroDelMoltiplicatoreNonHaZeriInutili() {
		assertEquals("1,5", ContenutiDelGrimorio.numero(1.5d));
		assertEquals("1", ContenutiDelGrimorio.numero(1.0d));
		assertEquals("0,5", ContenutiDelGrimorio.numero(0.5d));
		assertEquals("1,15", ContenutiDelGrimorio.numero(1.15d));
	}

	@Test
	void apertoParteDalPrimoIncantesimoEUnClicSuUnaRigaLoCambia() {
		SchermataGrimorio schermata = schermata();
		schermata.apri();

		assertEquals(TipoIncantesimo.values()[0], schermata.getVoceScelta());
		assertEquals(TipoIncantesimo.values().length * ContenutiDelGrimorio.ALTEZZA_RIGA_ELENCO, schermata.getContenutoElenco().getAltezza());

		schermata.selezionaVoce(TipoIncantesimo.FULMINE);

		assertEquals(TipoIncantesimo.FULMINE, schermata.getVoceScelta());
		assertTrue(testo(schermata.getContenutoScheda()).contains("Elettrocuzione"));
	}
}
