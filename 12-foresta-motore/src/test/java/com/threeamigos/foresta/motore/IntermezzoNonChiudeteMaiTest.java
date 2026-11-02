package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La scenetta di chi entra a ore impossibili: "Ma voi ... non chiudete mai?". Scatta una volta per
 * partita per ogni tipo di locale, dopo la chiusura e prima delle 8, e solo se all'ingresso non
 * scatta nessun altro intermezzo; in una locanda, solo dalla terza visita.
 */
class IntermezzoNonChiudeteMaiTest {

	// Domanda e risposta sono estratte a caso fra le varianti di mondo.txt: qui si riconoscono tutte
	private static final Predicate<String> DOMANDA_ALL_ARMAIOLO = domandaA("armaioli");
	private static final Predicate<String> DOMANDA_AL_LOCANDIERE = domandaA("locandieri");
	private static final Set<String> RISPOSTE = new HashSet<>(Arrays.asList(
			"No, perché esistono clienti come te.",
			"Chiudere? Di notte si fanno gli affari migliori: i clienti non guardano il prezzo, guardano dietro di sé.",
			"Di giorno i clienti chiedono lo sconto. Di notte pagano e non fanno domande.",
			"Di notte nessuno contratta: hanno tutti fretta, e la fretta si paga.",
			"Chiudo quando smettono di bussare. I clienti notturni, poi, sono i migliori: pagano il doppio per non farsi vedere.",
			"Chiudere? E chi pagherebbe l'affitto?",
			"Chiudo quando finiscono i clienti. Purtroppo ne arrivano sempre.",
			"Il sonno è per chi non ha debiti.",
			"Il denaro non dorme mai, e nemmeno io."));
	private static final Predicate<String> RISPOSTA = RISPOSTE::contains;

	/** Una delle domande del capo ai {@code negozianti}, qualunque variante. */
	private static Predicate<String> domandaA(String negozianti) {
		return testo -> testo.contains(negozianti) && (testo.contains("non chiudete mai") || testo.contains("non dovreste essere chiusi")
				|| testo.contains("non dormite mai") || testo.contains("non avete una casa"));
	}

	@Test
	void diNotteLaPrimaVoltaInNegozioCeSoloIlBenvenutoPoiLaScenettaUnaVoltaSola() {
		try (PartitaDiTest partita = inCitta()) {
			portaAllOra(21);

			partita.comando(Comando.NEGOZIO_ARMAIOLO);
			assertTrue(isScattato(TipoIntermezzo.INTERMEZZO_ARMAIOLO.name()), "la prima volta il benvenuto");
			assertEquals(0, conta(partita, DOMANDA_ALL_ARMAIOLO), "il benvenuto non si somma alla scenetta");
			partita.comando(Comando.ANNULLA);

			partita.comando(Comando.NEGOZIO_ARMAIOLO);
			assertEquals(1, conta(partita, DOMANDA_ALL_ARMAIOLO));
			assertEquals(1, conta(partita, RISPOSTA));
			partita.comando(Comando.ANNULLA);

			partita.comando(Comando.NEGOZIO_ARMAIOLO);
			assertEquals(1, conta(partita, DOMANDA_ALL_ARMAIOLO), "una volta sola per partita");
		}
	}

	@Test
	void negliOrariDiAperturaLaScenettaNonScatta() {
		try (PartitaDiTest partita = inCitta()) {
			partita.comando(Comando.NEGOZIO_ARMAIOLO);
			partita.comando(Comando.ANNULLA);

			portaAllOra(19);
			partita.comando(Comando.NEGOZIO_ARMAIOLO);
			partita.comando(Comando.ANNULLA);
			assertEquals(0, conta(partita, DOMANDA_ALL_ARMAIOLO), "alle 19 il negozio e' ancora aperto");

			portaAllOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
			partita.comando(Comando.NEGOZIO_ARMAIOLO);
			partita.comando(Comando.ANNULLA);
			assertEquals(0, conta(partita, DOMANDA_ALL_ARMAIOLO), "dalle 8 il negozio e' di nuovo aperto");

			portaAllOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO - 1);
			partita.comando(Comando.NEGOZIO_ARMAIOLO);
			assertEquals(1, conta(partita, DOMANDA_ALL_ARMAIOLO), "prima delle 8 e' ancora notte");
		}
	}

	@Test
	void inLocandaSoloDallaTerzaVisitaEDopoLeUndiciDiSera() {
		try (PartitaDiTest partita = inCitta()) {
			LocazioneMD nyena = locandaDiNyena();
			nyena.aggiungiProprieta(Locande.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);

			portaAllOra(23);
			nyena.aggiungiProprieta(Locande.LOCANDA_VISITE, "1");
			assertFalse(scatterebbeLaScenettaInLocanda(), "alla seconda visita ancora no");

			nyena.aggiungiProprieta(Locande.LOCANDA_VISITE, "2");
			portaAllOra(22);
			assertFalse(scatterebbeLaScenettaInLocanda(), "alle 22 la locanda e' ancora aperta");

			portaAllOra(23);
			partita.comando(Comando.LOCANDA);
			assertEquals(1, conta(partita, DOMANDA_AL_LOCANDIERE));
			assertEquals(1, conta(partita, RISPOSTA));
			assertEquals("3", nyena.ottieniProprieta(Locande.LOCANDA_VISITE), "superata la porta, la visita e' contata");
		}
	}

	@Test
	void laScenettaAspettaSeAllIngressoScattaUnAltroIntermezzo() {
		try (PartitaDiTest partita = inCitta()) {
			// In due: alla terza visita alla locanda scatta ancora la visita successiva, mai vista
			partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno", 1));
			LocazioneMD nyena = locandaDiNyena();
			nyena.aggiungiProprieta(Locande.LOCANDA_VISITATA, LocazioneMD.AFFERMATIVO);
			nyena.aggiungiProprieta(Locande.LOCANDA_VISITE, "2");
			portaAllOra(23);

			partita.comando(Comando.LOCANDA);

			assertTrue(isScattato(TipoIntermezzo.INTERMEZZO_LOCANDA_VISITA_SUCCESSIVA.name() + "_"
					+ nyena.ottieniProprieta(Locande.LOCANDA_IDENTIFICATIVO)));
			assertEquals(0, conta(partita, DOMANDA_AL_LOCANDIERE), "la visita successiva non si somma alla scenetta");
			assertFalse(isScattato(TipoIntermezzo.INTERMEZZO_NOTTE_LOCANDA.name()), "resta per la prossima volta");
		}
	}

	private static PartitaDiTest inCitta() {
		PartitaDiTest partita = PartitaDiTest.nuova(11);
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
				() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
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
		return intermezzo != null && intermezzo.getId().equals(TipoIntermezzo.INTERMEZZO_NOTTE_LOCANDA.name());
	}

	private static LocazioneMD locandaDiNyena() {
		return Foresta.getLocazioneMD(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
	}

	private static boolean isScattato(String id) {
		return ModelloDati.getIstanza().getIntermezziMD().isScattato(id);
	}

	/**
	 * Quante volte e' stata detta una battuta, in tutti gli intermezzi mostrati finora.
	 */
	private static long conta(PartitaDiTest partita, Predicate<String> battuta) {
		return partita.eventi().tutti(NotificaPaginaIntermezzo.class).stream()
				.flatMap(n -> n.getPagina().getBattuteProgrammate().stream())
				.filter(b -> battuta.test(b.getBattuta().getTesto()))
				.count();
	}
}
