package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Costanti;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * La pozione dell'aumento di livello fa salire di un livello tenendo la stessa percentuale che mancava al successivo.
 */
class PozioneAumentoLivelloTest {

	@Test
	void laPercentualeMancanteResta() {
		// Livello 2 (100 XP), il 3 è a 400 e il 4 a 900; a 250 XP manca la metà (150 su 300)
		int aggiunta = GestoreProgressione.getXpPerAumentoDiLivello(2, 250);
		assertEquals(250 + aggiunta, 650, "al livello 3 manca ancora la metà: 900 - 250");
		assertEquals(3, GestoreProgressione.calcolaLivelloDaXp(250 + aggiunta));
	}

	@Test
	void unSoloLivelloAncheConPochissimoDaFare() {
		int xp = GestoreProgressione.getXpNecessariPerLivello(5) + 1; // a un soffio dal livello, ma 1 XP dopo la soglia
		int xpPrimaDelLivello = GestoreProgressione.getXpPerProssimoLivello(5) - 1;
		for (int attuali : new int[] {xp, xpPrimaDelLivello, GestoreProgressione.getXpNecessariPerLivello(5)}) {
			int aggiunta = GestoreProgressione.getXpPerAumentoDiLivello(5, attuali);
			assertEquals(6, GestoreProgressione.calcolaLivelloDaXp(attuali + aggiunta), "da " + attuali + " XP");
		}
	}

	@Test
	void alLivelloMassimoNonFaNulla() {
		assertEquals(0, GestoreProgressione.getXpPerAumentoDiLivello(Costanti.MASSIMO_LIVELLO, 0));
		int aggiunta = GestoreProgressione.getXpPerAumentoDiLivello(Costanti.MASSIMO_LIVELLO - 1, 0);
		assertEquals(Costanti.MASSIMO_LIVELLO,
				GestoreProgressione.calcolaLivelloDaXp(aggiunta + GestoreProgressione.getXpNecessariPerLivello(Costanti.MASSIMO_LIVELLO - 1)));
	}

	@Test
	void bereLaPozioneDaUnPuntoAbilitaInPiu() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
			GruppoGiocatore gruppo = partita.gruppo();
			Personaggio personaggio = gruppo.getPersonaggiVivi().get(0);
			gruppo.addPozioniAumentoLivello(2);
			int pozioni = gruppo.getPozioniAumentoLivello();
			int livello = personaggio.getLivello();
			int punti = personaggio.getPuntiAbilitaDisponibili();

			gruppo.consumaPozioneAumentoLivello(personaggio);

			assertEquals(pozioni - 1, gruppo.getPozioniAumentoLivello());
			assertEquals(livello + 1, personaggio.getLivello());
			assertEquals(punti + 1, personaggio.getPuntiAbilitaDisponibili());
		}
	}
}
