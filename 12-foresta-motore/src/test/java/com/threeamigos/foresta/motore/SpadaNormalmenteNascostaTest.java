package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.SpadaNormalmenteNascosta;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoSpadaNascostaImpugnata;
import com.threeamigos.foresta.eventi.interni.InternoTrofeoAcquisito;
import com.threeamigos.foresta.intermezzi.*;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import com.threeamigos.foresta.trofei.RegistroTrofei;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La .spada normalmente nascosta: la rivela l'alchimista a chi ha trovato il Manuale di Unix System V, è sempre al livello del
 * mondo, e impugnarla fa vincere il trofeo Trovarobe.
 */
class SpadaNormalmenteNascostaTest {

	private static final String UNIX = "MANUALE_UNIX_SYSTEM_V";

	private static LibroTecnico manuale() {
		return LibroTecnico.getLibro(UNIX).orElseThrow(AssertionError::new);
	}

	@Test
	void senzaIlManualeL_alchimistaNonLaRivela() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
			});
			Intermezzo intermezzo = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_SPADA_NASCOSTA);
			assertFalse(intermezzo.deveScattare(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
		}
	}

	@Test
	void conIlManualeL_alchimistaLaRivelaUnaVoltaSolaEGliAltriIntermezziCedonoIlPosto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
			});
			IntermezzoLibroTecnico.trovato(manuale());
			Intermezzo intermezzo = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_SPADA_NASCOSTA);
			assertTrue(intermezzo.deveScattare(MomentoIntermezzo.INGRESSO_ALCHIMISTA));
			assertFalse(intermezzo.deveScattare(MomentoIntermezzo.INGRESSO_ARMAIOLO));

			RegistroIntermezzi.nuovoMomento();
			Intermezzo primo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ALCHIMISTA);
			assertEquals(TipoIntermezzo.INTERMEZZO_SPADA_NASCOSTA.name(), primo.getId());
			RegistroIntermezzi.segnaScattato(primo);
			List<PaginaIntermezzo> pagine = primo.getPagine();
			assertEquals(2, pagine.size());
			// La seconda pagina rivela la spada con l'effetto normale, che vola verso il capo
			assertNull(pagine.get(0).getRivelazioneArtefatto());
			assertEquals(AspettoArtefatto.SPADA_NORMALMENTE_NASCOSTA, pagine.get(1).getRivelazioneArtefatto().getAspetto());
			assertEquals("capo", pagine.get(1).getIdDestinazioneRivelazione());

			// Il benvenuto l'ha già dato lui: l'oroscopo, quella volta, non scatta
			assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ALCHIMISTA));

			Artefatto spada = partita.gruppo().getInventario().stream()
					.filter(artefatto -> artefatto.getAspetto() == AspettoArtefatto.SPADA_NORMALMENTE_NASCOSTA)
					.findFirst().orElseThrow(() -> new AssertionError("la spada deve finire nell'inventario del gruppo"));
			assertEquals(TipoArtefatto.SPADA, spada.getTipo());
			assertTrue(spada.getNomeCompleto().contains(".spada normalmente nascosta"), spada.getNomeCompleto());

			// Alla visita successiva scatta l'oroscopo, e la spada non si ripete
			RegistroIntermezzi.nuovoMomento();
			Intermezzo dopo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_ALCHIMISTA);
			assertEquals(TipoIntermezzo.INTERMEZZO_ALCHIMISTA.name(), dopo.getId());
		}
	}

	@Test
	void eSempreAlLivelloDelMondo() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
			});
			Statistiche.impostaLivelloDelMondo(1);
			SpadaNormalmenteNascosta spada = SpadaNormalmenteNascosta.costruisci(com.threeamigos.foresta.artefatti.GeneratoreArtefatti.istanza(), Statistiche.getLivello());
			assertEquals(1, spada.getLivello());
			int danniAlLivello1 = spada.getDanni();

			Statistiche.impostaLivelloDelMondo(6);
			assertEquals(6, spada.getLivello());
			assertEquals(6, Artefatto.di(spada.getModelloDati()).getLivello(), "anche ricostruita dal modello dati");
			assertTrue(spada.getDanni() > danniAlLivello1);
		}
	}

	@Test
	void impugnarlaFaVincereIlTrovarobe() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
			});
			partita.eventi().ascolta(InternoSpadaNascostaImpugnata.class);
			partita.eventi().ascolta(InternoTrofeoAcquisito.class);
			SpadaNormalmenteNascosta spada = SpadaNormalmenteNascosta.costruisci(com.threeamigos.foresta.artefatti.GeneratoreArtefatti.istanza(), Statistiche.getLivello());
			partita.gruppo().addArtefatto(spada);
			assertTrue(partita.eventi().tutti(InternoSpadaNascostaImpugnata.class).isEmpty(), "nell'inventario non è impugnata");

			partita.gruppo().equipaggia(partita.gruppo().getCapo(), spada);
			assertEquals(1, partita.eventi().tutti(InternoSpadaNascostaImpugnata.class).size());

			assertFalse(RegistroTrofei.isVinto(TipoTrofeo.TROVAROBE), "i trofei si controllano solo a fine locazione");
			partita.pubblica(new InternoFineLocazione());
			assertTrue(RegistroTrofei.isVinto(TipoTrofeo.TROVAROBE));
			assertTrue(partita.eventi().tutti(InternoTrofeoAcquisito.class).stream().anyMatch(e -> e.getTrofeo() == TipoTrofeo.TROVAROBE));
		}
	}
}
