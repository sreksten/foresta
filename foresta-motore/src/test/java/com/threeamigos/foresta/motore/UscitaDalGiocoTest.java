package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaUscitaDalGioco;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il motore non chiude il processo: quando il giocatore conferma l'uscita chiede alla piattaforma di terminare con un
 * evento. (Una chiamata a {@code System.exit} qui farebbe chiudere la JVM dei test.)
 */
class UscitaDalGiocoTest {

	private static PartitaDiTest finoAllaConfermaDiUscita(PartitaDiTest partita) {
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
		partita.comando(Comando.ESCI_DA_CITTA);
		partita.comando(Comando.FLOPPY_SALVA);
		partita.comando(Comando.NUMERO_1);
		partita.assertStato(Stato.CONFERMA_USCITA);
		return partita;
	}

	@Test
	void confermandoLUscitaIlMotoreChiedeLaTerminazioneENonChiudeIlProcesso() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(151)) {
			List<NotificaUscitaDalGioco> uscite = new ArrayList<>();
			BusEventi.iscriviti(NotificaUscitaDalGioco.class, uscite::add);
			finoAllaConfermaDiUscita(partita);
			assertTrue(uscite.isEmpty(), "finche' il giocatore non risponde non si esce");

			partita.comando(Comando.SI);

			assertEquals(1, uscite.size());
			assertEquals(0, uscite.get(0).getCodice());
			assertFalse(uscite.get(0).isErrore());
			assertNull(uscite.get(0).getMotivo());
			partita.assertStato(Stato.CONFERMA_USCITA);
		}
	}

	@Test
	void rispondendoNoSiTornaAlGiocoESenzaUscire() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(152)) {
			List<NotificaUscitaDalGioco> uscite = new ArrayList<>();
			BusEventi.iscriviti(NotificaUscitaDalGioco.class, uscite::add);
			finoAllaConfermaDiUscita(partita);

			partita.comando(Comando.NO);

			assertTrue(uscite.isEmpty());
			assertTrue(partita.stato() == Stato.ATTESA_DIREZIONE || partita.stato() == Stato.SCELTA_DIREZIONE, "" + partita.stato());
		}
	}
}
