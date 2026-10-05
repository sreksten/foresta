package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoNegozio;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I magazzini dei negozi di città: cosa succede a quanto il gruppo vende e all'aumento del livello del mondo.
 */
class MagazziniTest {

	@Test
	void quantoSiVendeAllArmaioloSparisceAllUscitaDallaCitta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			ScambiatoreArtefatti armaiolo = magazzino(TipoNegozio.ARMAIOLO);
			Artefatto spada = artefatto(TipoArtefatto.SPADA);
			partita.gruppo().addArtefatto(spada);

			partita.pubblica(new ComandoVenditaArtefatto(partita.gruppo(), armaiolo, spada));
			assertTrue(contiene(armaiolo.getInventario(), spada), "fino all'uscita dalla citta' resta in vendita");

			partita.comando(Comando.ESCI_DA_CITTA);

			assertFalse(contiene(armaiolo.getInventario(), spada));
		}
	}

	@Test
	void quantoSiVendeAlVenditoreDiPergameneSparisceAllUscitaDallaCitta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			ScambiatoreArtefatti venditore = magazzino(TipoNegozio.VENDITORE_DI_PERGAMENE);
			Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA);
			partita.gruppo().addArtefatto(pergamena);

			partita.pubblica(new ComandoVenditaArtefatto(partita.gruppo(), venditore, pergamena));
			partita.comando(Comando.ESCI_DA_CITTA);

			assertFalse(contiene(venditore.getInventario(), pergamena));
		}
	}

	@Test
	void quantoSiRicompraPrimaDiUscireNonVieneDistrutto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			ScambiatoreArtefatti armaiolo = magazzino(TipoNegozio.ARMAIOLO);
			Artefatto spada = artefatto(TipoArtefatto.SPADA);
			partita.gruppo().addArtefatto(spada);

			partita.pubblica(new ComandoVenditaArtefatto(partita.gruppo(), armaiolo, spada));
			partita.pubblica(new ComandoAcquistoArtefatto(partita.gruppo(), armaiolo, spada));
			partita.comando(Comando.ESCI_DA_CITTA);

			assertTrue(contiene(partita.gruppo().getInventario(), spada), "la spada ricomprata resta al gruppo");
			assertFalse(contiene(armaiolo.getInventario(), spada), "ricomprata, non è più dell'armaiolo");
		}
	}

	@Test
	void allAumentoDelLivelloDelMondoIMagazziniSiAggiornano() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
			int livelloMondo = 6;
			int scartatiSotto = livelloMondo - Costanti.MAGAZZINO_SCARTO_SOTTO_LIVELLO;

			partita.pubblica(new NotificaAumentoLivelloMondo(livelloMondo));

			Collection<Artefatto> armaiolo = magazzino(TipoNegozio.ARMAIOLO).getInventario();
			assertTrue(armaiolo.stream().allMatch(a -> a.getLivello() >= scartatiSotto), "gli artefatti troppo deboli sono scartati");
			long nuovi = armaiolo.stream()
					.filter(a -> a.getLivello() >= livelloMondo - Costanti.MAGAZZINO_DIVARIO_LIVELLO
							&& a.getLivello() <= livelloMondo + Costanti.MAGAZZINO_DIVARIO_LIVELLO)
					.count();
			assertTrue(nuovi >= Costanti.MAGAZZINO_ARTEFATTI_ARMAIOLO, "arriva merce attorno al nuovo livello del mondo");
			assertEquals(Costanti.MAGAZZINO_PERGAMENE, magazzino(TipoNegozio.VENDITORE_DI_PERGAMENE).getInventario().size(),
					"il venditore rinnova le pergamene");
		}
	}

	private static ScambiatoreArtefatti magazzino(TipoNegozio negozio) {
		CoordinateMD nyena = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA);
		return RegistroArtefatti.getScambiatorePerNegozio(nyena, negozio);
	}

	private static boolean contiene(Collection<Artefatto> artefatti, Artefatto artefatto) {
		return artefatti.stream().anyMatch(a -> a.getModelloDati() == artefatto.getModelloDati());
	}

	private static Artefatto artefatto(TipoArtefatto tipo) {
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(tipo);
		md.setNome("l'oggetto di prova");
		md.setDescrizione("che serve ai test");
		md.setLivello(1);
		md.setPeso(0.1);
		return Artefatto.di(md);
	}
}
