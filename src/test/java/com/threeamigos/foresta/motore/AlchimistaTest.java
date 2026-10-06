package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneAcquistoConsumabile;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoAcquistoConsumabile;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il listino dell'alchimista lo costruisce il motore, che decide anche il prezzo di un acquisto: il comando della
 * UI dice solo che cosa vuole.
 */
class AlchimistaTest {

	/**
	 * Una partita già cominciata, con un gruppo di un personaggio, che registra l'esito degli acquisti
	 */
	private static PartitaDiTest nuova() {
		PartitaDiTest partita = PartitaDiTest.nuova(11);
		partita.eventi().ascolta(NotificaApprovazioneAcquistoConsumabile.class, NotificaRifiutoAcquistoConsumabile.class);
		partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
		return partita;
	}

	@Test
	void ilListinoHaTuttiGliIncantesimiLePozioniEOgniPersonaggio() {
		try (PartitaDiTest partita = nuova()) {
			GruppoGiocatore gruppo = partita.gruppo();
			List<? extends VistaOffertaConsumabile> offerte = OfferteAlchimista.elenco(gruppo);

			for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
				assertEquals(1, offerte.stream().filter(o -> o.getTipo() == TipoConsumabile.INCANTESIMO
						&& o.getClasseIncantesimo() == classe).count(), "un'offerta per " + classe);
			}
			for (TipoConsumabile tipo : new TipoConsumabile[] {TipoConsumabile.POZIONE_SALUTE, TipoConsumabile.POZIONE_SALUTE_GRANDE,
					TipoConsumabile.POZIONE_MAGIA, TipoConsumabile.POZIONE_MAGIA_GRANDE,
					TipoConsumabile.MAPPA_PARZIALE_FORESTA, TipoConsumabile.MAPPA_COMPLETA_FORESTA}) {
				assertEquals(1, offerte.stream().filter(o -> o.getTipo() == tipo).count(), "un'offerta per " + tipo);
			}
			long giocanti = gruppo.getPersonaggiVivi().stream().filter(p -> !p.isPNG()).count();
			assertEquals(giocanti, offerte.stream().filter(o -> o.getTipo() == TipoConsumabile.AUMENTO_MAGIA_SINGOLO).count());
			assertEquals(giocanti > 1 ? 1 : 0, offerte.stream().filter(o -> o.getTipo() == TipoConsumabile.AUMENTO_MAGIA_GRUPPO).count(),
					"l'aumento per tutto il gruppo c'è solo se i personaggi sono più d'uno");
			offerte.forEach(o -> assertTrue(o.getCosto() > 0 && !o.getNome().isEmpty() && !o.getDescrizione().isEmpty()));
		}
	}

	@Test
	void ilPrezzoLoDecideIlMotoreEQuelloPagatoStaNellaNotifica() {
		try (PartitaDiTest partita = nuova()) {
			GruppoGiocatore gruppo = partita.gruppo();
			gruppo.addMonete(1_000);
			int monete = gruppo.getMonete();
			int pozioni = gruppo.getPozioniSalute();

			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.POZIONE_SALUTE, null, null));

			int prezzo = gruppo.prezzoAcquisto(Costanti.COSTO_POZIONE_SALUTE);
			assertEquals(monete - prezzo, gruppo.getMonete());
			assertEquals(pozioni + 1, gruppo.getPozioniSalute());
			assertEquals(prezzo, partita.eventi().ultimo(NotificaApprovazioneAcquistoConsumabile.class).getPrezzoPagato());
		}
	}

	@Test
	void senzaMoneteLAcquistoVieneRifiutato() {
		try (PartitaDiTest partita = nuova()) {
			GruppoGiocatore gruppo = partita.gruppo();
			gruppo.subMonete(gruppo.getMonete());
			int pozioni = gruppo.getPozioniMagia();

			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.POZIONE_MAGIA, null, null));

			assertEquals(pozioni, gruppo.getPozioniMagia());
			assertEquals(1, partita.eventi().tutti(NotificaRifiutoAcquistoConsumabile.class).size());
			assertTrue(partita.eventi().tutti(NotificaApprovazioneAcquistoConsumabile.class).isEmpty());
		}
	}

	@Test
	void unComandoCheNonCorrispondeAUnOffertaSiIgnora() {
		try (PartitaDiTest partita = nuova()) {
			GruppoGiocatore gruppo = partita.gruppo();
			gruppo.addMonete(1_000);
			int monete = gruppo.getMonete();

			// l'aumento di magia di un personaggio senza dire quale; un tipo che l'alchimista non vende
			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, null));
			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.MONETE, null, null));
			// un incantesimo senza dire quale
			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.INCANTESIMO, null, null));

			assertEquals(monete, gruppo.getMonete());
			assertTrue(partita.eventi().tutti(NotificaApprovazioneAcquistoConsumabile.class).isEmpty());
			assertTrue(partita.eventi().tutti(NotificaRifiutoAcquistoConsumabile.class).isEmpty());
		}
	}

	@Test
	void lAumentoDiMagiaVaAlPersonaggioIndicato() {
		try (PartitaDiTest partita = nuova()) {
			GruppoGiocatore gruppo = partita.gruppo();
			gruppo.addMonete(1_000);
			Personaggio personaggio = gruppo.getPersonaggiVivi().stream().filter(p -> !p.isPNG()).findFirst().get();
			int magia = personaggio.getMagiaMassima();

			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, personaggio));

			assertEquals(magia + Costanti.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, personaggio.getMagiaMassima());
		}
	}
}
