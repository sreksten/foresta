package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ClasseIntermezzo;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La scenetta di chi entra a ore impossibili: "Ma voi ... non chiudete mai?". Scatta una volta per
 * partita per ogni tipo di locale, dopo la chiusura e prima delle 8, e solo se all'ingresso non
 * scatta nessun altro intermezzo; in una locanda, solo dalla terza visita.
 */
class IntermezzoNonChiudeteMaiTest {

	private static final String DOMANDA_ALL_ARMAIOLO = "Ma voi armaioli non chiudete mai?";
	private static final String DOMANDA_AL_LOCANDIERE = "Ma voi locandieri non chiudete mai?";
	private static final String RISPOSTA = "No, perché esistono clienti come te.";

	@Test
	void diNotteLaPrimaVoltaInNegozioCeSoloIlBenvenutoPoiLaScenettaUnaVoltaSola() {
		try (PartitaDiTest partita = inCitta()) {
			portaAllOra(21);

			partita.comando(Comando.ARMAIOLO);
			assertTrue(isScattato(ClasseIntermezzo.INTERMEZZO_ARMAIOLO.name()), "la prima volta il benvenuto");
			assertEquals(0, conta(partita, DOMANDA_ALL_ARMAIOLO), "il benvenuto non si somma alla scenetta");
			partita.comando(Comando.ANNULLA);

			partita.comando(Comando.ARMAIOLO);
			assertEquals(1, conta(partita, DOMANDA_ALL_ARMAIOLO));
			assertEquals(1, conta(partita, RISPOSTA));
			partita.comando(Comando.ANNULLA);

			partita.comando(Comando.ARMAIOLO);
			assertEquals(1, conta(partita, DOMANDA_ALL_ARMAIOLO), "una volta sola per partita");
		}
	}

	@Test
	void negliOrariDiAperturaLaScenettaNonScatta() {
		try (PartitaDiTest partita = inCitta()) {
			partita.comando(Comando.ARMAIOLO);
			partita.comando(Comando.ANNULLA);

			portaAllOra(19);
			partita.comando(Comando.ARMAIOLO);
			partita.comando(Comando.ANNULLA);
			assertEquals(0, conta(partita, DOMANDA_ALL_ARMAIOLO), "alle 19 il negozio e' ancora aperto");

			portaAllOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
			partita.comando(Comando.ARMAIOLO);
			partita.comando(Comando.ANNULLA);
			assertEquals(0, conta(partita, DOMANDA_ALL_ARMAIOLO), "dalle 8 il negozio e' di nuovo aperto");

			portaAllOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO - 1);
			partita.comando(Comando.ARMAIOLO);
			assertEquals(1, conta(partita, DOMANDA_ALL_ARMAIOLO), "prima delle 8 e' ancora notte");
		}
	}

	@Test
	void inLocandaSoloDallaTerzaVisitaEDopoLeUndiciDiSera() {
		try (PartitaDiTest partita = inCitta()) {
			LocazioneMD nyena = locandaDiNyena();
			nyena.aggiungiProprieta(Locanda.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);

			portaAllOra(23);
			nyena.aggiungiProprieta(Locanda.LOCANDA_VISITE, "1");
			assertFalse(scatterebbeLaScenettaInLocanda(), "alla seconda visita ancora no");

			nyena.aggiungiProprieta(Locanda.LOCANDA_VISITE, "2");
			portaAllOra(22);
			assertFalse(scatterebbeLaScenettaInLocanda(), "alle 22 la locanda e' ancora aperta");

			portaAllOra(23);
			partita.comando(Comando.LOCANDA);
			assertEquals(1, conta(partita, DOMANDA_AL_LOCANDIERE));
			assertEquals(1, conta(partita, RISPOSTA));
			assertEquals("3", nyena.ottieniProprieta(Locanda.LOCANDA_VISITE), "superata la porta, la visita e' contata");
		}
	}

	@Test
	void laScenettaAspettaSeAllIngressoScattaUnAltroIntermezzo() {
		try (PartitaDiTest partita = inCitta()) {
			// In due: alla terza visita alla locanda scatta ancora la seconda visita, mai vista
			partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
			LocazioneMD nyena = locandaDiNyena();
			nyena.aggiungiProprieta(Locanda.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
			nyena.aggiungiProprieta(Locanda.LOCANDA_VISITE, "2");
			portaAllOra(23);

			partita.comando(Comando.LOCANDA);

			assertTrue(isScattato(ClasseIntermezzo.INTERMEZZO_LOCANDA_SECONDA_VISITA.name() + "_"
					+ nyena.ottieniProprieta(Locanda.LOCANDA_IDENTIFICATIVO)));
			assertEquals(0, conta(partita, DOMANDA_AL_LOCANDIERE), "la seconda visita non si somma alla scenetta");
			assertFalse(isScattato(ClasseIntermezzo.INTERMEZZO_NOTTE_LOCANDA.name()), "resta per la prossima volta");
		}
	}

	private static PartitaDiTest inCitta() {
		PartitaDiTest partita = PartitaDiTest.nuova(11);
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
				() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
		partita.assertStato(Stato.IN_LOCAZIONE);
		return partita;
	}

	private static void portaAllOra(int ora) {
		while (LineaTemporale.getOra() != ora) {
			LineaTemporale.aggiungiOre(1);
		}
	}

	/**
	 * Se entrando ora nella locanda della citta' scatterebbe la scenetta.
	 */
	private static boolean scatterebbeLaScenettaInLocanda() {
		RegistroIntermezzi.nuovoMomento();
		Intermezzo intermezzo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA);
		return intermezzo != null && intermezzo.getId().equals(ClasseIntermezzo.INTERMEZZO_NOTTE_LOCANDA.name());
	}

	private static LocazioneMD locandaDiNyena() {
		return Foresta.getLocazioneMD(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
	}

	private static boolean isScattato(String id) {
		return ModelloDati.getIstanza().getIntermezziMD().isScattato(id);
	}

	/**
	 * Quante volte e' stata detta una battuta, in tutti gli intermezzi mostrati finora.
	 */
	private static long conta(PartitaDiTest partita, String battuta) {
		return partita.eventi().tutti(NotificaPaginaIntermezzo.class).stream()
				.flatMap(n -> n.getPagina().getBattuteProgrammate().stream())
				.filter(b -> battuta.equals(b.getBattuta().getTesto()))
				.count();
	}
}
