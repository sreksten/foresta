package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.oggetti.RaccoltaBottino;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli artefatti trovati in un cofano seguono la strada di tutti gli altri: li prende chi può equipaggiarli (se è uno solo), altrimenti si sceglie; e
 * se i cofani sono più d'uno l'apertura si ferma alla scelta e riprende dal cofano dopo.
 */
class CofaniEScelteTest {

	@AfterEach
	void ripulisci() {
		Dado.impostaTruccatore(null);
		RaccoltaBottino.reimposta();
	}

	private static TruccatoreDiDadi unArtefattoPerCofano() {
		return new TruccatoreDiDadi() {
			@Override
			public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
				return OptionalInt.empty();
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
				return contesto == ContestoLancio.COFANO_OGGETTO ? Optional.of((T) Cofano.OGGETTO_SPECIALE_ARTEFATTO) : Optional.empty();
			}
		};
	}

	private static int quantiArtefatti(com.threeamigos.foresta.gruppi.GruppoGiocatore gruppo) {
		int totale = gruppo.getInventario().size();
		for (Personaggio p : gruppo.getPersonaggi()) {
			totale += p.getInventario().size();
		}
		return totale;
	}

	@Test
	void treCofaniConTreArtefattiSiFermanoAllaSceltaERiprendono() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			partita.gruppo().aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1));
			partita.eventi().ascolta(NotificaArtefattoTrovato.class);
			partita.eventi().ascolta(NotificaTestoFrase.class);
			Dado.impostaTruccatore(unArtefattoPerCofano());
			int prima = quantiArtefatti(partita.gruppo());
			Cofano cofano = new Cofano(3);

			int scelte = 0;
			Comando scelta = null;
			int giri = 0;
			while (!RaccoltaBottino.raccogli(cofano, partita.gruppo(), scelta)) {
				assertTrue(++giri <= 10, "l'apertura non finisce");
				Artefatto inAttesa = RaccoltaBottino.artefattoDaAssegnare(cofano).orElseThrow(AssertionError::new);
				List<Comando> candidati = RaccoltaBottino.candidati(partita.gruppo(), inAttesa);
				assertTrue(candidati.size() > 1, "si ferma solo se c'è da scegliere");
				scelta = candidati.get(candidati.size() - 1);
				scelte++;
			}

			assertEquals(3, partita.eventi().tutti(NotificaArtefattoTrovato.class).size(), "ogni cofano ha il suo artefatto, una volta sola");
			assertEquals(prima + 3, quantiArtefatti(partita.gruppo()));
			assertFalse(RaccoltaBottino.artefattoDaAssegnare(cofano).isPresent(), "a cofani finiti non aspetta più niente");
			StringBuilder racconto = new StringBuilder();
			for (NotificaTestoFrase frase : partita.eventi().tutti(NotificaTestoFrase.class)) {
				racconto.append(frase.getMessaggio()).append('\n');
			}
			assertTrue(racconto.toString().contains("secondo"), "continua con il secondo cofano: " + racconto);
			assertTrue(racconto.toString().contains("terzo"), "e con il terzo: " + racconto);
			assertTrue(scelte >= 1, "con due guerrieri almeno un artefatto lo possono equipaggiare tutti e due");
		}
	}

	@Test
	void conUnSoloCandidatoLoPrendeLuiSenzaChiedere() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			Dado.impostaTruccatore(unArtefattoPerCofano());
			int prima = quantiArtefatti(partita.gruppo());

			assertTrue(RaccoltaBottino.raccogli(new Cofano(1), partita.gruppo(), null), "con un solo personaggio non c'è niente da scegliere");
			assertEquals(prima + 1, quantiArtefatti(partita.gruppo()));
		}
	}
}
