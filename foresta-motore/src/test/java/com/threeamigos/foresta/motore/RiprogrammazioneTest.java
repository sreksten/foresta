package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.artefatti.RegoleIncantatura;

import com.threeamigos.foresta.artefatti.CostantiArtefatti;

import com.threeamigos.foresta.artefatti.BancoDiLavoro;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaAvvisoIncantatura;
import com.threeamigos.foresta.intermezzi.*;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The C Programming Language: un ingrediente leggendario che, fuso su un artefatto dall'incantatore, lo spezza negli ingredienti
 * "del Core Dump" dei suoi incantamenti, e che l'incantatore spiega la prima volta che il gruppo entra da lui.
 */
class RiprogrammazioneTest {

    // Le regole non sanno cos'è il mondo né lo sconto: i test le chiamano come le chiama il gioco, con il livello del mondo e senza sconto
    private static java.util.Optional<TipoMotivoRifiutoIncantatura> verifica(java.util.Collection<Artefatto> banco, int monete) {
        return RegoleIncantatura.verifica(banco, monete, RegoleIncantatura.costo(banco), Statistiche.getLivello());
    }

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
		assertEquals(AspettoArtefatto.LIBRO_TECNICO, libro.getAspetto());
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
		int costo = CommercioDelGruppo.costoFusione(gruppo, banco.getInventario());
		assertEquals(CostantiArtefatti.FUSIONE_COSTO_BASE + 3 * CostantiArtefatti.FUSIONE_COSTO_PER_EFFETTO,
				RegoleIncantatura.costo(banco.getInventario()));

		List<NotificaAvvisoIncantatura> avvisi = new ArrayList<>();
		BusEventi.iscriviti(NotificaAvvisoIncantatura.class, avvisi::add);
		int monete = gruppo.getMonete();
		assertEquals(Optional.empty(), CommercioDelGruppo.incanta(gruppo, banco, ""));

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
				verifica(Arrays.asList(spada, libro), 100));
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
