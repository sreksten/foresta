package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.notifiche.NotificaApprovazioneIncantatura;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I trofei dei negozi di città, dell'incantatore e degli artefatti tolti agli avversari.
 */
class TrofeiNegoziTest {

	@Test
	void rigattiereECollezionistaContanoGliArtefattiCompratiDallArmaioloSecondoIlLivello() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			for (int i = 0; i < 100; i++) {
				compra(partita, artefatto(TipoArtefatto.SPADA, 2));
				compra(partita, artefatto(TipoArtefatto.SPADA, 5));
			}
			compra(partita, artefatto(TipoArtefatto.SPADA, 3));
			partita.pubblica(new InternoFineLocazione());

			assertEquals(100, RegistroTrofei.getProgresso(TipoTrofeo.RIGATTIERE), "il livello 3 non conta per nessuno dei due");
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.RIGATTIERE));
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.COLLEZIONISTA));
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.STUDIOSO), "un artefatto dell'armaiolo non e' una pergamena");
		}
	}

	@Test
	void loStudiosoContaLePergameneComprate() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			for (int i = 0; i < 100; i++) {
				compra(partita, artefatto(TipoArtefatto.PERGAMENA, 1));
			}
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.STUDIOSO));
			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.RIGATTIERE), "le pergamene non vengono dall'armaiolo");
		}
	}

	@Test
	void unAcquistoRifiutatoNonConta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.gruppo().subMonete(partita.gruppo().getMonete());
			Artefatto costoso = artefatto(TipoArtefatto.SPADA, 1);
			costoso.getModelloDati().setCostoAcquisto(100);

			compra(partita, costoso);
			partita.pubblica(new InternoFineLocazione());

			assertEquals(0, RegistroTrofei.getProgresso(TipoTrofeo.RIGATTIERE));
		}
	}

	@Test
	void bombaroloECartografoContanoGliAcquistiDallAlchimista() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			for (int i = 0; i < 100; i++) {
				partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.INCANTESIMO, FabbricaIncantesimi.casuale(), null, 0));
			}
			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.POZIONE_SALUTE, null, null, 0));
			partita.pubblica(new InternoFineLocazione());

			assertEquals(100, RegistroTrofei.getProgresso(TipoTrofeo.BOMBAROLO), "una pozione non e' un incantesimo");
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.BOMBAROLO));
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.CARTOGRAFO));

			partita.pubblica(new ComandoAcquistoConsumabile(TipoConsumabile.MAPPA_COMPLETA_FORESTA, null, null, 0));
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.CARTOGRAFO));
		}
	}

	@Test
	void ilTrafficoneContaGliArtefattiIncantati() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			for (int i = 0; i < 49; i++) {
				partita.pubblica(new NotificaApprovazioneIncantatura(artefatto(TipoArtefatto.SPADA, 1), 0));
			}
			partita.pubblica(new InternoFineLocazione());
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.TRAFFICONE));

			partita.pubblica(new NotificaApprovazioneIncantatura(artefatto(TipoArtefatto.SPADA, 1), 0));
			partita.pubblica(new InternoFineLocazione());

			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.TRAFFICONE));
		}
	}

	@Test
	void iCacciatoriDiTesoriContanoGliArtefattiCustoditiSecondoIlLivello() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			for (int i = 0; i < 50; i++) {
				partita.pubblica(new InternoOggettoRaccolto(ClassiOggetto.ARTEFATTO, 1, artefatto(TipoArtefatto.SPADA, 5), true));
				partita.pubblica(new InternoOggettoRaccolto(ClassiOggetto.SPADA, 1, artefatto(TipoArtefatto.SPADA, 4), true));
				partita.pubblica(new InternoOggettoRaccolto(ClassiOggetto.ARTEFATTO, 1, artefatto(TipoArtefatto.SPADA, 5), false));
			}
			partita.pubblica(new InternoOggettoRaccolto(ClassiOggetto.MONETA, 2, null, true));
			partita.pubblica(new InternoFineLocazione());

			assertEquals(100, RegistroTrofei.getProgresso(TipoTrofeo.CACCIATORE_DI_TESORI), "gli artefatti incustoditi e le monete non contano");
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.CACCIATORE_DI_TESORI));
			assertEquals(50, RegistroTrofei.getProgresso(TipoTrofeo.ESPERTO_CACCIATORE_DI_TESORI), "solo quelli di livello 5 o superiore");
			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.ESPERTO_CACCIATORE_DI_TESORI));
		}
	}

	/**
	 * Un acquisto vero: il gruppo elabora il comando e, se le monete bastano, pubblica l'approvazione.
	 */
	private static void compra(PartitaDiTest partita, Artefatto artefatto) {
		partita.pubblica(new ComandoAcquistoArtefatto(partita.gruppo(), new Magazzino(artefatto), artefatto));
	}

	private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(tipo);
		md.setNome("l'oggetto di prova");
		md.setDescrizione("che serve ai test");
		md.setLivello(livello);
		md.setPeso(0.1);
		return Artefatto.di(md);
	}

	/**
	 * Il magazzino di un negozio con un solo artefatto in vendita.
	 */
	private static final class Magazzino implements ScambiatoreArtefatti {

		private final List<Artefatto> inventario = new ArrayList<>();

		Magazzino(Artefatto artefatto) {
			inventario.add(artefatto);
		}

		@Override
		public Collection<Artefatto> getInventario() {
			return inventario;
		}

		@Override
		public void addArtefatto(Artefatto artefatto) {
			inventario.add(artefatto);
		}

		@Override
		public void removeArtefatto(Artefatto artefatto) {
			inventario.remove(artefatto);
		}
	}
}
