package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.intermezzi.FabbricaIntermezzi;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.oggetti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The C Programming Language: un ingrediente leggendario che, fuso su un artefatto dall'incantatore, lo spezza negli ingredienti
 * "del Core Dump" dei suoi incantamenti, e che l'incantatore spiega la prima volta che il gruppo entra da lui.
 */
class RiprogrammazioneTest {

	private static final String K_AND_R = "THE_C_PROGRAMMING_LANGUAGE";

	private static Artefatto libro() {
		return LibroTecnico.getLibro(K_AND_R).orElseThrow(AssertionError::new)
				.costruisci(new GeneratoreArtefattiTabelle(new Random(1)), 4);
	}

	private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
		ArtefattoMD md = new ArtefattoMD();
		md.setTipo(tipo);
		md.setNome("l'oggetto di prova");
		md.setDescrizione("che serve ai test");
		md.setLivello(livello);
		md.setPeso(1);
		return Artefatto.di(md);
	}

	private static Artefatto spadaCon(int incantamenti) {
		Artefatto spada = artefatto(TipoArtefatto.SPADA, 5);
		spada.getModelloDati().setDanni(10);
		TipoDanno[] danni = {TipoDanno.FUOCO, TipoDanno.GELO, TipoDanno.ACIDO};
		for (int i = 0; i < incantamenti; i++) {
			spada.getModelloDati().addIncantamento("Incantamento " + i, danni[i], 5 + i, 0.05);
		}
		return spada;
	}

	@Test
	void ilLibroEUnIngredienteLeggendarioNonEquipaggiabileConIlSuoAspetto() {
		Artefatto libro = libro();
		assertEquals(TipoArtefatto.RIPROGRAMMAZIONE, libro.getTipo());
		assertTrue(libro.getTipo().isIngrediente());
		assertEquals("The C Programming Language", libro.getNome());
		assertEquals(TipoOggetto.LIBRO_TECNICO, libro.getAspetto());
		assertEquals(TipoRaritaArtefatto.LEGGENDARIO, libro.getRarita());
		assertEquals(0, libro.getNumeroEffetti());
		assertTrue(LibroTecnico.di(libro).isPresent(), "lo si riconosce anche dopo il salvataggio");
		// Il Manuale di Unix resta un libro magico, che raddoppia i danni magici
		LibroTecnico manuale = LibroTecnico.getLibro("MANUALE_UNIX_SYSTEM_V").orElseThrow(AssertionError::new);
		assertEquals(TipoArtefatto.LIBRO_MAGICO, manuale.getTipo());
		assertEquals(2, manuale.getMoltiplicatoreDanniMagici());
		assertEquals(1, LibroTecnico.getLibro(K_AND_R).orElseThrow(AssertionError::new).getMoltiplicatoreDanniMagici());
	}

	@Test
	void conIlLibroSulBancoCiStaQualunqueIngredienteELaRiprogrammazioneCostaPerOgniIncantamento() {
		Artefatto spada = spadaCon(2);
		Artefatto pergamenaDiPotereMagico = artefatto(TipoArtefatto.PERGAMENA, 1);
		pergamenaDiPotereMagico.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO,
				TipoModificatore.VARIAZIONE_FISSA, 5, "");
		Artefatto libro = libro();
		// Senza il libro quella pergamena su una spada non avrebbe effetto e sarebbe rifiutata
		assertTrue(RegoleIncantatura.puoMettereSulBanco(Collections.singletonList(spada), pergamenaDiPotereMagico).isPresent());
		List<Artefatto> banco = new ArrayList<>(Arrays.asList(spada, libro));
		assertEquals(Optional.empty(), RegoleIncantatura.puoMettereSulBanco(banco, pergamenaDiPotereMagico));
		banco.add(pergamenaDiPotereMagico);

		assertTrue(RegoleIncantatura.conRiprogrammazione(banco));
		assertEquals(2, RegoleIncantatura.effettiDaTrasferire(banco));
		assertEquals(Optional.empty(), RegoleIncantatura.verifica(banco, 100));
		assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.MONETE_INSUFFICIENTI), RegoleIncantatura.verifica(banco, 0));
		assertEquals(Optional.empty(), RegoleIncantatura.avvisoEffettiPersi(banco));
		RegoleIncantatura.Piano piano = RegoleIncantatura.pianoDellaFusione(banco);
		assertEquals(0, piano.numeroDaAggiungere());
		assertEquals(2, piano.numeroDaTogliere());
		spada.getIncantamenti().forEach(incantamento -> assertTrue(piano.esce(incantamento)));
	}

	@Test
	void unArtefattoSenzaIncantamentiNonSiRiprogramma() {
		List<Artefatto> banco = Arrays.asList(spadaCon(0), libro());
		assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NIENTE_DA_RIPROGRAMMARE), RegoleIncantatura.verifica(banco, 100));
	}

	@Test
	void laRiprogrammazioneVaSempreMaleELArtefattoSiSpezza() {
		ModelloDati.setIstanza(new ModelloDati());
		GruppoGiocatore gruppo = new GruppoGiocatore();
		gruppo.subMonete(gruppo.getMonete());
		gruppo.addMonete(100);
		Artefatto spada = spadaCon(3);
		Collection<IncantamentoMD> originali = new ArrayList<>(spada.getIncantamenti());
		Artefatto libro = libro();
		Artefatto gemma = artefatto(TipoArtefatto.GEMMA, 1);
		gemma.getModelloDati().addIncantamento("Fulmine minore", TipoDanno.FULMINE, 5, 0.05);
		BancoDiLavoro banco = new BancoDiLavoro();
		banco.addArtefatto(spada);
		banco.addArtefatto(libro);
		banco.addArtefatto(gemma);
		int costo = gruppo.costoFusione(banco.getInventario());
		assertEquals(Costanti.FUSIONE_COSTO_BASE + 3 * Costanti.FUSIONE_COSTO_PER_EFFETTO,
				RegoleIncantatura.costo(banco.getInventario()));

		List<NotificaAvvisoIncantatura> avvisi = new ArrayList<>();
		BusEventi.iscriviti(NotificaAvvisoIncantatura.class, avvisi::add);
		int monete = gruppo.getMonete();
		assertEquals(Optional.empty(), gruppo.incanta(banco, ""));

		assertEquals(monete - costo, gruppo.getMonete());
		assertTrue(banco.getInventario().isEmpty(), "il banco si svuota");
		assertFalse(avvisi.isEmpty(), "l'incantatore dice che ha sbagliato");
		// L'artefatto è senza incantamenti, mantiene il resto; libro e ingredienti non usati tornano nel gruppo
		assertTrue(spada.getIncantamenti().isEmpty());
		assertEquals(10, spada.getModelloDati().getDanni());
		Collection<Artefatto> inventario = gruppo.getInventario();
		assertTrue(inventario.stream().anyMatch(a -> a.getModelloDati() == spada.getModelloDati()));
		assertTrue(inventario.stream().anyMatch(a -> a.getModelloDati() == libro.getModelloDati()), "il libro non si perde");
		assertTrue(inventario.stream().anyMatch(a -> a.getModelloDati() == gemma.getModelloDati()), "l'ingrediente non è stato usato");
		assertEquals(1, gemma.getIncantamenti().size());
		// Un ingrediente del Core Dump per ogni incantamento, ognuno con il suo
		List<Artefatto> delCoreDump = inventario.stream().filter(a -> a.getNome().endsWith("del Core Dump")).collect(Collectors.toList());
		assertEquals(3, delCoreDump.size());
		for (Artefatto ingrediente : delCoreDump) {
			assertTrue(ingrediente.getTipo().isIngrediente());
			assertFalse(ingrediente.getTipo().isRiprogrammazione());
			assertEquals(1, ingrediente.getIncantamenti().size());
		}
		assertTrue(delCoreDump.stream().flatMap(a -> a.getIncantamenti().stream()).collect(Collectors.toList()).containsAll(originali));
		// Il libro, rimesso sul banco con la spada, si può riusare: e senza più incantamenti la spada non si riprogramma
		assertEquals(Optional.of(TipoMotivoRifiutoIncantatura.NIENTE_DA_RIPROGRAMMARE),
				RegoleIncantatura.verifica(Arrays.asList(spada, libro), 100));
	}

	@Test
	void l_incantatoreSpiegaIlLibroLaPrimaVoltaECedeIlBenvenuto() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
			});
			Intermezzo intermezzo = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_RIPROGRAMMAZIONE);
			assertFalse(intermezzo.deveScattare(MomentoIntermezzo.INGRESSO_INCANTATORE), "senza il libro");

			IntermezzoLibroTecnico.trovato(LibroTecnico.getLibro(K_AND_R).orElseThrow(AssertionError::new));
			assertTrue(intermezzo.deveScattare(MomentoIntermezzo.INGRESSO_INCANTATORE));
			assertFalse(intermezzo.deveScattare(MomentoIntermezzo.INGRESSO_ALCHIMISTA));

			RegistroIntermezzi.nuovoMomento();
			Intermezzo primo = RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_INCANTATORE);
			assertEquals(TipoIntermezzo.INTERMEZZO_RIPROGRAMMAZIONE.name(), primo.getId());
			RegistroIntermezzi.segnaScattato(primo);
			List<PaginaIntermezzo> pagine = primo.getPagine();
			assertEquals(1, pagine.size());
			List<String> battute = pagine.get(0).getBattuteProgrammate().stream()
					.map(b -> b.getBattuta().getTesto()).collect(Collectors.toList());
			assertTrue(battute.stream().anyMatch(b -> b.contains("maghi del computer")), battute.toString());
			assertTrue(battute.stream().anyMatch(b -> b.contains("Core Dump")), battute.toString());
			// Dà lui il benvenuto: quello normale non scatta anche subito dopo
			assertNull(RegistroIntermezzi.getProssimoIntermezzo(MomentoIntermezzo.INGRESSO_INCANTATORE));
			assertTrue(RegistroIntermezzi.isScattato(TipoIntermezzo.INTERMEZZO_INCANTATORE));
		}
	}
}
