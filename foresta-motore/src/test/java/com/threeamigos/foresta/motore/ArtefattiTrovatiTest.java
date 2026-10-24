package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoArtefattoTrovato;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.locazioni.OggettiDiProva;
import com.threeamigos.foresta.oggetti.BottinoArtefatto;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.oggetti.FormaDelBottino;
import com.threeamigos.foresta.oggetti.RaccoltaBottino;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni artefatto che il gruppo ottiene (per terra, in un tempio, in un cofano) lo dice con un evento, che serve ai trofei dei
 * tesori degli avversari: conta se era custodito, e gli ingredienti magici non contano. Solo l'artefatto già fatto di un tempio si
 * mostra con la rivelazione.
 */
class ArtefattiTrovatiTest {

	@AfterEach
	void togliIlTruccatore() {
		Dado.impostaTruccatore(null);
	}

	private static void sconfiggiGliAvversari() {
		for (Personaggio avversario : GruppoAvversario.getIstanza().getPersonaggiVivi()) {
			avversario.subSalute(avversario.getSalute(), GruppoGiocatore.getIstanza().getCapo(), Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
		}
	}

	@Test
	void unaSpadaPresaAGliAvversariConta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(25)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
			partita.eventi().ascolta(InternoArtefattoTrovato.class, InternoOggettoRaccolto.class, NotificaArtefattoTrovato.class);
			OggettiDiProva.impostaOggetto(partita.gruppo().getLocazioneCorrente(), BottinoArtefatto.nuovo(FormaDelBottino.SPADA));
			sconfiggiGliAvversari();

			partita.comando(Comando.SINGOLO_ATTACCO);

			List<InternoArtefattoTrovato> trovati = partita.eventi().tutti(InternoArtefattoTrovato.class);
			assertEquals(1, trovati.size());
			assertTrue(trovati.get(0).isCustodito());
			assertEquals(TipoArtefatto.SPADA, trovati.get(0).getArtefatto().getTipo());
			assertEquals(TipoBottino.SPADA, partita.eventi().ultimo(InternoOggettoRaccolto.class).getClasse());
			assertFalse(partita.eventi().haRicevuto(NotificaArtefattoTrovato.class), "una spada per terra non si rivela");
		}
	}

	@Test
	void unaSpadaInCittaNonEraCustodita() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			partita.eventi().ascolta(InternoArtefattoTrovato.class);
			OggettiDiProva.impostaOggetto(partita.gruppo().getLocazioneCorrente(), BottinoArtefatto.nuovo(FormaDelBottino.SPADA));

			partita.comando(Comando.ESCI_DA_CITTA);

			assertFalse(partita.eventi().ultimo(InternoArtefattoTrovato.class).isCustodito());
		}
	}

	@Test
	void unArtefattoGiaFattoDiUnTempioSiRivela() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(25)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CASTELLO_IDRA));
			partita.eventi().ascolta(InternoArtefattoTrovato.class, InternoOggettoRaccolto.class, NotificaArtefattoTrovato.class);
			OggettiDiProva.impostaOggetto(partita.gruppo().getLocazioneCorrente(), BottinoArtefatto.giaFatto(
					new GeneratoreArtefattiTabelle(new Random(1)).generaArtefatto(TipoArtefatto.SCUDO, 3)));
			sconfiggiGliAvversari();

			partita.comando(Comando.SINGOLO_ATTACCO);

			assertTrue(partita.eventi().haRicevuto(NotificaArtefattoTrovato.class), "l'artefatto già fatto si rivela");
			assertEquals(TipoBottino.SCUDO, partita.eventi().ultimo(InternoOggettoRaccolto.class).getClasse());
			assertTrue(partita.eventi().ultimo(InternoArtefattoTrovato.class).isCustodito());
		}
	}

	@Test
	void unArtefattoInUnCofanoConta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			partita.eventi().ascolta(InternoArtefattoTrovato.class);
			Dado.impostaTruccatore(cofanoCon(Cofano.OGGETTO_SPECIALE_ARTEFATTO));

			RaccoltaBottino.raccogli(new Cofano(1), partita.gruppo(), Comando.GRUPPO);

			assertEquals(1, partita.eventi().tutti(InternoArtefattoTrovato.class).size());
		}
	}

	@Test
	void unIngredienteMagicoInUnCofanoNonConta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			partita.eventi().ascolta(InternoArtefattoTrovato.class);
			int esperienza = Statistiche.getPuntiEsperienza();
			Dado.impostaTruccatore(cofanoCon(Cofano.OGGETTO_SPECIALE_INGREDIENTE));

			RaccoltaBottino.raccogli(new Cofano(1), partita.gruppo(), Comando.GRUPPO);

			assertFalse(partita.eventi().haRicevuto(InternoArtefattoTrovato.class));
			assertEquals(esperienza, Statistiche.getPuntiEsperienza());
		}
	}

	/**
	 * Un truccatore che fa dare al cofano quell'oggetto speciale, e lascia il resto al caso.
	 */
	private static TruccatoreDiDadi cofanoCon(String oggettoSpeciale) {
		return new TruccatoreDiDadi() {
			@Override
			public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
				return OptionalInt.empty();
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
				return contesto == ContestoLancio.COFANO_OGGETTO ? Optional.of((T) oggettoSpeciale) : Optional.empty();
			}
		};
	}
}
