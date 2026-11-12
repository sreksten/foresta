package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoDiGioco;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La schermata delle opzioni: una riga per opzione con il pulsante a sinistra e la scritta accanto, il click che manda il comando al motore e la cornice
 * attorno al pulsante della riga che aspetta la scelta nella barra.
 */
class SchermataOpzioniTest {

	private final List<Comando> mandati = new ArrayList<>();
	private SchermataOpzioni schermata;

	@BeforeEach
	void prepara() {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		BusEventi.iscriviti(ComandoDiGioco.class, e -> mandati.add(e.getComando()));
		schermata = new SchermataOpzioni(770, 505, new Misura(64, 64));
	}

	@AfterEach
	void ripristina() {
		Testi.impostaLingua(null);
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
	}

	private static long cornici(SchermataOpzioni schermata) {
		return schermata.contenuto().getElementi().stream().filter(e -> e.getTipo() == Elemento.Tipo.RETTANGOLO).count();
	}

	@Test
	void leRigheSonoLaLinguaIlTutorialLaBarraEleDueCancellazioniUnaSottoLAltraDentroLoSchermo() {
		List<SchermataOpzioni.Riga> righe = schermata.getRighe();
		List<Comando> comandi = new ArrayList<>();
		righe.forEach(r -> comandi.add(r.getComando()));
		assertEquals(Arrays.asList(Comando.OPZIONI_LINGUA, Comando.RIPRISTINA_TUTORIAL, Comando.POSIZIONE_BARRA, Comando.CANCELLA_PUNTEGGI,
				Comando.CANCELLA_SALVATAGGI), comandi);
		for (int i = 0; i < righe.size(); i++) {
			Area area = righe.get(i).getArea();
			assertTrue(area.getX() >= 0 && area.getX() + area.getLarghezza() <= 770, "dentro in larghezza");
			assertTrue(area.getY() + area.getAltezza() <= 505, "dentro in altezza");
			if (i > 0) {
				assertTrue(area.getY() > righe.get(i - 1).getArea().getY() + righe.get(i - 1).getArea().getAltezza() - 1, "una sotto l'altra");
			}
		}
	}

	@Test
	void laRigaDellaLinguaHaLaBandieraEIlNomeDellaLinguaInUso() {
		Testi.impostaLingua("en-GB");
		SchermataOpzioni.Riga lingua = schermata.getRighe().get(0);
		assertEquals(TipoIcona.LINGUA_INGLESE_GB, lingua.getIcona());
		assertEquals(Comando.LINGUA_INGLESE_GB.getDescrizione(), lingua.getTesto());
	}

	@Test
	void unClickSullaRigaMandaIlSuoComandoEIncorniciaIlPulsanteFinoAllaScelta() {
		assertEquals(0, cornici(schermata));
		Area punteggi = schermata.getRighe().get(3).getArea();
		// Anche sulla scritta, non solo sul pulsante
		schermata.processaClick(punteggi.getX() + punteggi.getLarghezza() - 2, punteggi.getY() + 2, Finestra.Tasto.SINISTRO);
		assertEquals(Collections.singletonList(Comando.CANCELLA_PUNTEGGI), mandati);
		assertEquals(4, cornici(schermata), "i quattro lati della cornice");
		schermata.aggiorna(Arrays.asList(Comando.SI, Comando.NO));
		assertEquals(4, cornici(schermata), "mentre aspetta il sì o il no");
		schermata.aggiorna(Collections.singletonList(Comando.ANNULLA));
		assertEquals(0, cornici(schermata), "scelto, la cornice se ne va");
	}

	@Test
	void unClickFuoriDalleRigheNonMandaNiente() {
		schermata.processaClick(2, 2, Finestra.Tasto.SINISTRO);
		assertTrue(mandati.isEmpty());
	}

	@Test
	void laRigaDellaBarraHaLIconaDiDoveStaAdesso() {
		schermata.setPosizioneDellaBarra(com.threeamigos.foresta.tipi.PosizioneDellaBarra.SINISTRA);
		assertEquals(TipoIcona.BARRA_SINISTRA, schermata.getRighe().get(2).getIcona());
		schermata.setPosizioneDellaBarra(com.threeamigos.foresta.tipi.PosizioneDellaBarra.DESTRA);
		assertEquals(TipoIcona.BARRA_DESTRA, schermata.getRighe().get(2).getIcona());
	}

	@Test
	void ilContenutoSiRifaSoloQuandoCambiaQuelCheMostra() {
		// Chi disegna tiene le immagini delle scritte per elemento: un contenuto nuovo a ogni fotogramma riempiva la memoria
		ContenutoDiColonna primo = schermata.contenuto();
		assertSame(primo, schermata.contenuto());
		schermata.setPosizioneDellaBarra(com.threeamigos.foresta.tipi.PosizioneDellaBarra.SINISTRA);
		assertNotSame(primo, schermata.contenuto());
	}

	@Test
	void mentreUnaRigaAspettaGliAltriClickNonMandanoNiente() {
		Area punteggi = schermata.getRighe().get(3).getArea();
		schermata.processaClick(punteggi.getX() + 2, punteggi.getY() + 2, Finestra.Tasto.SINISTRO);
		schermata.aggiorna(Arrays.asList(Comando.SI, Comando.NO));
		schermata.processaClick(punteggi.getX() + 2, punteggi.getY() + 2, Finestra.Tasto.SINISTRO);
		assertEquals(Collections.singletonList(Comando.CANCELLA_PUNTEGGI), mandati);
	}
}
