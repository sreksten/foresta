package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.GruppoDiBersagli;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.incantesimi.Fuoco;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Un incantesimo a più bersagli ripartisce il danno per posizione: il bersaglio principale lo subisce intero, ogni successivo la metà
 * del precedente. Il critico è neutralizzato e i bersagli sono storditi (il tiro per colpire riesce sempre), così il
 * danno è deterministico.
 */
class IncantesimiAPiuBersagliTest {

	private static final double DELTA = 1e-9;

	private Mago mago;

	@BeforeEach
	void preparaIlMago() {
		StatoDelMotore.azzera();
		mago = new Mago("Merlino", 5);
		mago.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
		mago.addModificatore(new ModificatoreAttributoMD(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.QUANTITA_ASSOLUTA, 5));
		assertEquals(5, mago.getBersagli());
	}

	private static Guerriero bersaglio(String nome) {
		Guerriero bersaglio = new Guerriero(nome, 5);
		bersaglio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
		bersaglio.addEffettoDiStato(TipoEffettoDiStato.STORDITO, 1, 0);
		return bersaglio;
	}

	@Test
	void laQuotaCalaDiUnQuartoPerPosizioneFinoAUnQuarto() {
		assertEquals(1.0, CalcolatoreCombattimento.quotaDelDannoPerPosizione(0), DELTA);
		assertEquals(0.5, CalcolatoreCombattimento.quotaDelDannoPerPosizione(1), DELTA);
		assertEquals(0.25, CalcolatoreCombattimento.quotaDelDannoPerPosizione(2), DELTA);
		assertEquals(0.125, CalcolatoreCombattimento.quotaDelDannoPerPosizione(3), DELTA);
		assertEquals(0.0625, CalcolatoreCombattimento.quotaDelDannoPerPosizione(4), DELTA);
		assertEquals(0.0625, CalcolatoreCombattimento.quotaDelDannoPerPosizione(20), DELTA, "dalla quinta posizione la quota minima");
		assertEquals(1.0, CalcolatoreCombattimento.quotaDelDannoPerPosizione(-1), DELTA, "una posizione assurda vale come la prima");
	}

	@Test
	void ognunoSubisceLaQuotaDellaSuaPosizione() {
		List<Personaggio> bersagli = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			bersagli.add(bersaglio("Bersaglio " + i));
		}
		Fuoco fuoco = new Fuoco(5);
		int[] attesi = new int[bersagli.size()];
		for (int i = 0; i < attesi.length; i++) {
			attesi[i] = CalcolatoreCombattimento.calcolaDannoRisultante(mago, bersagli.get(i), fuoco,
					CalcolatoreCombattimento.quotaDelDannoPerPosizione(i)).getDanno();
		}
		int[] salutePrima = bersagli.stream().mapToInt(Personaggio::getSalute).toArray();

		fuoco.formula(mago, null, new GruppoDiBersagli(bersagli));

		int[] persi = new int[bersagli.size()];
		for (int i = 0; i < persi.length; i++) {
			persi[i] = salutePrima[i] - bersagli.get(i).getSalute();
			assertEquals(attesi[i], persi[i], "bersaglio " + i);
		}
		assertTrue(persi[0] > persi[1] && persi[1] > persi[2] && persi[2] > persi[3], java.util.Arrays.toString(persi));
		assertTrue(persi[3] > persi[4], java.util.Arrays.toString(persi));
		assertTrue(persi[0] > 0);
	}

	@Test
	void unBersaglioSoloSubisceIlDannoPieno() {
		Guerriero uno = bersaglio("Solo");
		Fuoco fuoco = new Fuoco(5);
		int pieno = CalcolatoreCombattimento.calcolaDannoRisultante(mago, uno, fuoco).getDanno();
		int prima = uno.getSalute();

		fuoco.formula(mago, uno, null);

		assertEquals(pieno, prima - uno.getSalute());
	}

	@Test
	void ilDannoAttesoSegueLaStessaQuota() {
		Guerriero uno = bersaglio("Atteso");
		Fuoco fuoco = new Fuoco(5);
		double pieno = CalcolatoreCombattimento.dannoAtteso(mago, uno, fuoco);
		assertTrue(pieno > 0);
		for (int posizione = 0; posizione < 5; posizione++) {
			double quota = CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione);
			assertEquals(pieno * quota, CalcolatoreCombattimento.dannoAtteso(mago, uno, fuoco, quota), DELTA, "posizione " + posizione);
		}
	}
}
