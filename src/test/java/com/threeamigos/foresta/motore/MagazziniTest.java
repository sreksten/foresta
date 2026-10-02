package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.tipi.TipoArtefatto;
import com.threeamigos.foresta.motore.tipi.TipoNegozio;
import com.threeamigos.foresta.oggetti.Artefatto;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I magazzini dei negozi di città: cosa succede a quanto il gruppo vende e all'aumento del livello del mondo.
 */
class MagazziniTest {

	private static final String SALUTO_ARMAIOLO = "Con il materiale che mi hai fornito, farò altre meravigliose creazioni!";
	private static final String SALUTO_VENDITORE = "le rivenderò a qualche mago di passaggio!";

	@Test
	void quantoSiVendeAllArmaioloSparisceAllUscitaDallaCitta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			ScambiatoreArtefatti armaiolo = magazzino(TipoNegozio.ARMAIOLO);
			Artefatto spada = artefatto(TipoArtefatto.SPADA);
			partita.gruppo().addArtefatto(spada);

			partita.pubblica(new ComandoVenditaArtefatto(partita.gruppo(), armaiolo, spada));
			assertTrue(contiene(armaiolo.getInventario(), spada), "fino all'uscita dalla citta' resta in vendita");

			partita.comando(Comando.ESCI_DA_CITTA);

			assertFalse(contiene(armaiolo.getInventario(), spada));
			assertTrue(haDetto(partita, SALUTO_ARMAIOLO));
			assertFalse(haDetto(partita, SALUTO_VENDITORE), "al venditore di pergamene non si e' venduto nulla");
		}
	}

	@Test
	void quantoSiVendeAlVenditoreDiPergameneSparisceAllUscitaDallaCitta() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			ScambiatoreArtefatti venditore = magazzino(TipoNegozio.VENDITORE_DI_PERGAMENE);
			Artefatto pergamena = artefatto(TipoArtefatto.PERGAMENA);
			partita.gruppo().addArtefatto(pergamena);

			partita.pubblica(new ComandoVenditaArtefatto(partita.gruppo(), venditore, pergamena));
			partita.comando(Comando.ESCI_DA_CITTA);

			assertFalse(contiene(venditore.getInventario(), pergamena));
			assertTrue(haDetto(partita, SALUTO_VENDITORE));
			assertFalse(haDetto(partita, SALUTO_ARMAIOLO));
		}
	}

	@Test
	void quantoSiRicompraPrimaDiUscireNonVieneDistrutto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
			ScambiatoreArtefatti armaiolo = magazzino(TipoNegozio.ARMAIOLO);
			Artefatto spada = artefatto(TipoArtefatto.SPADA);
			partita.gruppo().addArtefatto(spada);

			partita.pubblica(new ComandoVenditaArtefatto(partita.gruppo(), armaiolo, spada));
			partita.pubblica(new ComandoAcquistoArtefatto(partita.gruppo(), armaiolo, spada));
			partita.comando(Comando.ESCI_DA_CITTA);

			assertTrue(contiene(partita.gruppo().getInventario(), spada), "la spada ricomprata resta al gruppo");
			assertFalse(haDetto(partita, SALUTO_ARMAIOLO), "l'armaiolo non ha piu' nulla da smaltire");
		}
	}

	@Test
	void allAumentoDelLivelloDelMondoIMagazziniSiAggiornano() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
					() -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
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
		CoordinateMD nyena = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA);
		return RegistroArtefatti.getScambiatorePerNegozio(nyena, negozio);
	}

	private static boolean contiene(Collection<Artefatto> artefatti, Artefatto artefatto) {
		return artefatti.stream().anyMatch(a -> a.getModelloDati() == artefatto.getModelloDati());
	}

	private static boolean haDetto(PartitaDiTest partita, String testo) {
		return partita.eventi().tutti(NotificaTestoParagrafo.class).stream().anyMatch(n -> n.getMessaggio().contains(testo));
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
