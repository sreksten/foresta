package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.MagazziniDeiNegozi;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.RegistroPersonaggiMD;
import com.threeamigos.foresta.modellodati.ResidenteMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I nomi dei personaggi non combattenti del mondo (vedi nomi.md): il bardo e sua moglie, le persone di ogni città e i
 * campioni delle sfide, creati con il mondo, diversi fra loro e uguali dopo un salvataggio.
 */
class NomiDeiPersonaggiDelMondoTest {

	@BeforeEach
	void nuovaPartita() {
		BusEventi.azzera();
		Dado.ripristina();
		Dado.impostaSeme(7);
		ModelloDati.setIstanza(new ModelloDati());
		GruppoGiocatore.azzeraIstanza();
		GruppoAvversario.azzeraIstanza();
		Notizie.registrati();
		Statistiche.registrati();
		MagazziniDeiNegozi.registrati();
		RegistroMissioni.registrati();
		CostruttoreDellaForesta.reimposta();
	}

	private static List<TipoLocazione> citta() {
		List<TipoLocazione> citta = new ArrayList<>();
		for (TipoLocazione tipo : TipoLocazione.values()) {
			if (tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
				citta.add(tipo);
			}
		}
		return citta;
	}

	@Test
	void ogniCittaHaIlSuoPersonaleConLaProvenienza() {
		for (TipoLocazione citta : citta()) {
			for (PersonaggioNoto noto : PersonaggioNoto.values()) {
				if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA) {
					ResidenteMD residente = RegistroPersonaggi.getResidente(noto, citta);
					assertNotNull(residente.getNome(), noto + " di " + citta);
					assertEquals(citta.getNomeDellaCitta(), residente.getProvenienza());
					assertTrue(RegistroPersonaggi.getNomeCompleto(residente).endsWith(" da " + citta.getNomeDellaCitta()),
							RegistroPersonaggi.getNomeCompleto(residente));
				}
			}
		}
	}

	@Test
	void gliArtigianiSonoMastri() {
		for (TipoLocazione citta : citta()) {
			assertTrue(RegistroPersonaggi.getResidente(PersonaggioNoto.ARMAIOLO, citta).getNome().startsWith("Mastro "));
			assertTrue(RegistroPersonaggi.getResidente(PersonaggioNoto.INCANTATORE, citta).getNome().startsWith("Mastro "));
		}
	}

	@Test
	void ilBardoELaMoglieNonHannoProvenienza() {
		ResidenteMD bardo = RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE);
		ResidenteMD moglie = RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO);
		assertNull(bardo.getProvenienza());
		assertNull(moglie.getProvenienza());
		assertEquals(TipoPersonaggio.BARDO_DELLE_LOCANDE, bardo.getClasse());
		assertEquals(TipoPersonaggio.MOGLIE_DEL_BARDO, moglie.getClasse());
		assertEquals(bardo.getNome(), RegistroPersonaggi.getNomeCompleto(bardo));
	}

	@Test
	void nessunNomeDelMondoSiRipete() {
		Set<String> nomi = new HashSet<>();
		int quanti = 0;
		for (TipoLocazione citta : citta()) {
			for (PersonaggioNoto noto : PersonaggioNoto.values()) {
				if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA) {
					nomi.add(RegistroPersonaggi.getResidente(noto, citta).getNome());
					quanti++;
				}
			}
		}
		nomi.add(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome());
		nomi.add(RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome());
		assertEquals(quanti + 2, nomi.size(), "i nomi del mondo dovrebbero essere tutti diversi: " + nomi);
	}

	@Test
	void nessunNomeDiBattesimoDelMondoVieneDaiLocandieri() {
		Set<String> paroleDeiLocandieri = new HashSet<>();
		for (int x = 0; x < Foresta.getDimensioneX(); x++) {
			for (int y = 0; y < Foresta.getDimensioneY(); y++) {
				String locandiere = Foresta.getLocazioneMD(new CoordinateMD(x, y)).ottieniProprieta(Locanda.LOCANDA_NOME_LOCANDIERE);
				if (locandiere != null) {
                    Collections.addAll(paroleDeiLocandieri, locandiere.split("[ ']"));
				}
			}
		}
		assertFalse(paroleDeiLocandieri.isEmpty(), "dovrebbero esserci dei locandieri");
		List<ResidenteMD> residenti = new ArrayList<>();
		for (TipoLocazione citta : citta()) {
			for (PersonaggioNoto noto : PersonaggioNoto.values()) {
				if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA) {
					residenti.add(RegistroPersonaggi.getResidente(noto, citta));
				}
			}
		}
		residenti.add(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE));
		residenti.add(RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO));
		for (ResidenteMD residente : residenti) {
			String nomeDiBattesimo = residente.getNome().replaceFirst("^Mastro ", "").split(" ")[0];
			assertFalse(paroleDeiLocandieri.contains(nomeDiBattesimo), nomeDiBattesimo + " è anche di un locandiere");
		}
	}

	@Test
	void nessunNomeSiRipeteConMoltiSemi() {
		for (int seme = 1; seme <= 40; seme++) {
			Dado.impostaSeme(seme);
			CostruttoreDellaForesta.reimposta();
			Set<String> nomi = new HashSet<>();
			int quanti = 0;
			for (TipoLocazione citta : citta()) {
				for (PersonaggioNoto noto : PersonaggioNoto.values()) {
					if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA) {
						nomi.add(RegistroPersonaggi.getResidente(noto, citta).getNome());
						quanti++;
					}
				}
			}
			nomi.add(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome());
			nomi.add(RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome());
			assertEquals(quanti + 2, nomi.size(), "seme " + seme + ": " + nomi);
		}
	}

	@Test
	void ilBardoDellaMissioneEQuelloDellaLocanda() {
		String nome = RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome();
		assertEquals(nome, new NonSparateSulPianista().getBardo());
	}

	@Test
	void ilPersonaggioCostruitoHaIlNomeDelResidente() {
		ResidenteMD bardo = RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE);
		Personaggio personaggio = RegistroPersonaggi.costruisciPersonaggio(bardo);
		assertEquals(TipoPersonaggio.BARDO_DELLE_LOCANDE, personaggio.getClasse());
		assertEquals(bardo.getNome(), personaggio.getNomeProprio().orElse(null));
	}

	@Test
	void ilBorgomastroHaLaClasseSuaELaLevatriceEUnaDonna() {
		ResidenteMD borgomastro = RegistroPersonaggi.getResidente(PersonaggioNoto.BORGOMASTRO, citta().get(0));
		assertEquals(TipoPersonaggio.BORGOMASTRO, borgomastro.getClasse());
		assertEquals(borgomastro.getNome(), RegistroPersonaggi.costruisciPersonaggio(borgomastro).getNomeProprio().orElse(null));
		ResidenteMD levatrice = RegistroPersonaggi.getResidente(PersonaggioNoto.LEVATRICE);
		assertEquals(TipoPersonaggio.DONNA, levatrice.getClasse());
		assertEquals(Personaggio.Sesso.FEMMINA, RegistroPersonaggi.costruisciPersonaggio(levatrice).getSesso());
	}

	@Test
	void laLevataceCompareAlVoloEResta() {
		ResidenteMD levatrice = RegistroPersonaggi.getResidente(PersonaggioNoto.LEVATRICE);
		assertNotNull(levatrice.getNome());
		assertSame(levatrice, RegistroPersonaggi.getResidente(PersonaggioNoto.LEVATRICE));
	}

	@Test
	void iCampioniRestanoNelRegistroDopoLaSconfitta() {
		RegistroPersonaggi.registraCampione("Bradamante la Fulva", TipoPersonaggio.GUERRIERA);
		assertEquals(1, RegistroPersonaggi.getCampioni().size());
		assertFalse(RegistroPersonaggi.getCampioni().get(0).isSconfitto());
		RegistroPersonaggi.segnaCampioneSconfitto("Bradamante la Fulva");
		assertTrue(RegistroPersonaggi.getCampioni().get(0).isSconfitto());
		assertEquals("Bradamante la Fulva", RegistroPersonaggi.getCampioni().get(0).getNome());
	}

	@Test
	void iResidentiSopravvivonoAlSalvataggio() throws IOException {
		RegistroPersonaggi.registraCampione("Ferraguto il Tremendo", TipoPersonaggio.GUERRIERO);
		RegistroPersonaggi.segnaCampioneSconfitto("Ferraguto il Tremendo");
		RegistroPersonaggiMD originale = ModelloDati.getIstanza().getRegistroPersonaggiMD();

		StringWriter scritto = new StringWriter();
		PrintWriter stream = new PrintWriter(scritto);
		originale.salva(stream);
		stream.flush();
		RegistroPersonaggiMD riletto = new RegistroPersonaggiMD();
		riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));

		assertEquals(originale.getResidenti().keySet(), riletto.getResidenti().keySet());
		for (String chiave : originale.getResidenti().keySet()) {
			ResidenteMD prima = originale.getResidente(chiave);
			ResidenteMD dopo = riletto.getResidente(chiave);
			assertEquals(prima.getNome(), dopo.getNome(), chiave);
			assertEquals(prima.getProvenienza(), dopo.getProvenienza(), chiave);
			assertEquals(prima.getClasse(), dopo.getClasse(), chiave);
			assertEquals(prima.isSconfitto(), dopo.isSconfitto(), chiave);
		}
		assertTrue(riletto.getResidente(PersonaggioNoto.CAMPIONE.chiave("Ferraguto il Tremendo")).isSconfitto());
	}

	@Test
	void iNomiSopravvivonoAlSalvataggioEAllaRilettura() throws IOException {
		List<String> prima = nomiCompletiDeiResidenti();
		String bardoPrima = RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome();
		int quanti = ModelloDati.getIstanza().getRegistroPersonaggiMD().getResidenti().size();

		StringWriter scritto = new StringWriter();
		PrintWriter stream = new PrintWriter(scritto);
		ModelloDati.getIstanza().salva(stream);
		stream.flush();
		ModelloDati riletto = new ModelloDati();
		riletto.leggi(new BufferedReader(new StringReader(scritto.toString())));
		// Una partita nuova pesca altri nomi: se la rilettura funziona, quelli salvati tornano uguali
		CostruttoreDellaForesta.reimposta();
		assertNotEquals(prima, nomiCompletiDeiResidenti(), "la partita nuova dovrebbe avere altri nomi");
		ModelloDati.setIstanza(riletto);

		assertEquals(quanti, riletto.getRegistroPersonaggiMD().getResidenti().size());
		assertEquals(prima, nomiCompletiDeiResidenti());
		assertEquals(quanti, riletto.getRegistroPersonaggiMD().getResidenti().size(), "nessun residente è stato ripescato");
		assertEquals(bardoPrima, RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE).getNome());
		assertEquals(bardoPrima, new NonSparateSulPianista().getBardo());
	}

	private static List<String> nomiCompletiDeiResidenti() {
		List<String> nomi = new ArrayList<>();
		for (TipoLocazione citta : citta()) {
			for (PersonaggioNoto noto : PersonaggioNoto.values()) {
				if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA) {
					nomi.add(RegistroPersonaggi.getNomeCompleto(RegistroPersonaggi.getResidente(noto, citta)));
				}
			}
		}
		nomi.add(RegistroPersonaggi.getNomeCompleto(RegistroPersonaggi.getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE)));
		nomi.add(RegistroPersonaggi.getNomeCompleto(RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO)));
		return nomi;
	}

	@Test
	void unaNuovaPartitaDimenticaIResidenti() {
		RegistroPersonaggi.registraCampione("Uberto l'Invitto", TipoPersonaggio.GUERRIERO);
		CostruttoreDellaForesta.reimposta();
		assertTrue(RegistroPersonaggi.getCampioni().isEmpty());
	}
}
