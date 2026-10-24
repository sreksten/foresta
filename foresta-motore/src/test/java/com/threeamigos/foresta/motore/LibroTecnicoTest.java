package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.oggetti.RaccoltaBottino;
import com.threeamigos.foresta.intermezzi.FabbricaIntermezzi;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.oggetti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.oggetti.leggendari.LibroTecnico;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I libri tecnici di leggendari.txt: libri magici leggendari, con i valori di un libro normale raddoppiati, che si trovano
 * solo nei cofani, una volta per partita, e che il capo legge in un intermezzo alla fine della locazione.
 */
class LibroTecnicoTest {

	private static final String UNIX = "MANUALE_UNIX_SYSTEM_V";
	private static final String K_AND_R = "THE_C_PROGRAMMING_LANGUAGE";

	@AfterEach
	void togliIlTruccatore() {
		Dado.impostaTruccatore(null);
	}

	@Test
	void sonoDueConLeBattuteDelCapo() {
		assertEquals(2, LibroTecnico.tutti().size());
		LibroTecnico unix = LibroTecnico.getLibro(UNIX).orElseThrow(AssertionError::new);
		assertEquals("il Manuale di Unix System V", unix.getNome());
		assertEquals("\"Il Manuale di Unix System V\", che spiega tutto, tranne come si esce dall'editor", unix.getTitoloDaLeggere());
		LibroTecnico kr = LibroTecnico.getLibro(K_AND_R).orElseThrow(AssertionError::new);
		assertEquals("\"The C Programming Language\", di B. Kernighan e D. Ritchie", kr.getTitoloDaLeggere());
		assertFalse(kr.getBattute().isEmpty());
	}

	@Test
	void eUnLibroMagicoNormaleConIValoriRaddoppiati() {
		// Un generatore che dà sempre lo stesso libro magico, con un nome proprio, un modificatore e un incantamento
		GeneratoreArtefattiTabelle generatore = new GeneratoreArtefattiTabelle(new Random(1)) {
			@Override
			public Artefatto generaArtefatto(TipoArtefatto tipo, int livello) {
				Artefatto libro = super.generaArtefatto(tipo, livello);
				libro.getModelloDati().setNomeProprio("Pagine Sparse");
				new ArrayList<>(libro.getModelloDati().getModificatori()).forEach(libro.getModelloDati()::removeModificatore);
				libro.getModelloDati().rimuoviTuttiGliIncantamenti();
				libro.getModelloDati().addModificatore(TipoAttributo.POTERE_MAGICO, TipoModificatore.VARIAZIONE_PERCENTUALE, 15, "");
				libro.getModelloDati().addIncantamento("Fiammella", TipoDanno.FUOCO, 6, 0.1);
				libro.getModelloDati().setCostoAcquisto(20);
				return libro;
			}
		};
		Artefatto libro = LibroTecnico.getLibro(UNIX).orElseThrow(AssertionError::new).costruisci(generatore, 3);

		assertEquals(TipoArtefatto.LIBRO_MAGICO, libro.getTipo());
		assertEquals("il Manuale di Unix System V", libro.getNome());
		assertNull(libro.getModelloDati().getNomeProprio(), "il titolo è il nome: niente nome proprio");
		assertEquals("che spiega tutto, tranne come si esce dall'editor", libro.getModelloDati().getDescrizione());
		ModificatoreAttributoMD modificatore = libro.getModelloDati().getModificatori().iterator().next();
		assertEquals(30, modificatore.getQuantita(), 1e-9);
		IncantamentoMD incantamento = libro.getModelloDati().getIncantamenti().iterator().next();
		assertEquals(12, incantamento.getDannoBonusFisso());
		assertEquals(0.2, incantamento.getCoefficienteScala(), 1e-9);
		assertEquals(40, libro.getCostoAcquisto());
		assertEquals(TipoRaritaArtefatto.LEGGENDARIO, libro.getRarita());
		assertEquals(TipoOggetto.LIBRO_TECNICO, libro.getAspetto());
		assertEquals(UNIX, LibroTecnico.di(libro).map(LibroTecnico::getChiave).orElse(null));
	}

	@Test
	void unArtefattoQualsiasiNonEUnLibroTecnico() {
		Artefatto libro = new GeneratoreArtefattiTabelle(new Random(1)).generaArtefatto(TipoArtefatto.LIBRO_MAGICO, 1);
		assertFalse(LibroTecnico.di(libro).isPresent());
	}

	@Test
	void ognunoRimandaAChiNeSaDiPiu() {
		// Si alternano i libri a ogni estrazione: il personaggio di uno non deve restare nell'altro
		LibroTecnico unix = LibroTecnico.getLibro(UNIX).orElseThrow(AssertionError::new);
		LibroTecnico kr = LibroTecnico.getLibro(K_AND_R).orElseThrow(AssertionError::new);
		boolean alchimista = false;
		boolean incantatore = false;
		for (int i = 0; i < 300; i++) {
			String perUnix = ProduttoreDiTestiCasuale.conclusioneSulLibroTecnico(unix.getChiave());
			String perKr = ProduttoreDiTestiCasuale.conclusioneSulLibroTecnico(kr.getChiave());
			assertFalse(perUnix.isEmpty());
			assertFalse(perKr.isEmpty());
			// Il Manuale di Unix ha a che fare con l'Alchimista, il libro sul C con l'Incantatore: mai l'altro
			assertFalse(perUnix.contains("Incantatore"), perUnix);
			assertFalse(perKr.contains("Alchimista"), perKr);
			assertFalse(perUnix.contains("PERSONAGGIO") || perKr.contains("PERSONAGGIO"), perUnix + " / " + perKr);
			alchimista |= perUnix.contains("Alchimista");
			incantatore |= perKr.contains("Incantatore");
		}
		assertTrue(alchimista, "il Manuale rimanda all'Alchimista");
		assertTrue(incantatore, "il libro sul C rimanda all'Incantatore");
	}

	@Test
	void ilCapoLoLeggeAFineLocazioneEPoiNonSiTrovaPiu() {
		try (PartitaDiTest partita = PartitaDiTest.nuova(11)) {
			partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
			});
			assertEquals(0, Dado.trucchiRimasti());
			Dado.impostaTruccatore(cofanoCon(Cofano.PREFISSO_LIBRO_TECNICO + K_AND_R));
			LibroTecnico kr = LibroTecnico.getLibro(K_AND_R).orElseThrow(AssertionError::new);
			assertFalse(IntermezzoLibroTecnico.isTrovato(kr));

			RaccoltaBottino.raccogli(new Cofano(1), partita.gruppo(), Comando.GRUPPO);

			Artefatto trovato = partita.gruppo().getInventario().stream()
					.filter(artefatto -> artefatto.getAspetto() == TipoOggetto.LIBRO_TECNICO)
					.findFirst().orElseThrow(() -> new AssertionError("il libro deve finire nell'inventario del gruppo"));
			assertEquals("The C Programming Language", trovato.getNome());
			assertTrue(IntermezzoLibroTecnico.isTrovato(kr), "appena trovato, un cofano non lo dà più");
			assertFalse(IntermezzoLibroTecnico.isTrovato(LibroTecnico.getLibro(UNIX).orElseThrow(AssertionError::new)));

			// L'intermezzo scatta a fine locazione, con il libro come identificativo
			Intermezzo intermezzo = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO);
			assertEquals("INTERMEZZO_LIBRO_TECNICO_" + K_AND_R, intermezzo.getId());
			assertTrue(intermezzo.deveScattare(MomentoIntermezzo.LOCAZIONE_COMPLETATA));
			assertFalse(intermezzo.deveScattare(MomentoIntermezzo.INIZIO_LOCAZIONE));
			RegistroIntermezzi.nuovoMomento();
			assertTrue(intermezziDelMomento(MomentoIntermezzo.LOCAZIONE_COMPLETATA).contains(intermezzo.getId()));

			// Il capo legge il titolo, lo sfoglia e chiude la questione (l'Automa lo segna prima di chiederne le pagine)
			RegistroIntermezzi.segnaScattato(intermezzo);
			List<PaginaIntermezzo> pagine = intermezzo.getPagine();
			assertEquals(1, pagine.size());
			List<String> battute = pagine.get(0).getBattuteProgrammate().stream()
					.map(battuta -> battuta.getBattuta().getTesto()).collect(Collectors.toList());
			assertEquals(3, battute.size());
			assertTrue(battute.get(0).contains(kr.getTitoloDaLeggere()), battute.get(0));
			assertTrue(kr.getBattute().contains(battute.get(1)), battute.get(1));
			assertFalse(battute.get(2).isEmpty());

			// Letto, non scatta più, e il libro resta trovato per tutta la partita
			assertTrue(IntermezzoLibroTecnico.isTrovato(kr));
			Intermezzo dopo = FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO);
			assertFalse(dopo.deveScattare(MomentoIntermezzo.LOCAZIONE_COMPLETATA));
		}
	}

	/**
	 * Gli identificativi degli intermezzi che scattano in quel momento, uno dopo l'altro, come li mostra l'Automa.
	 */
	private static List<String> intermezziDelMomento(MomentoIntermezzo momento) {
		List<String> id = new ArrayList<>();
		Intermezzo intermezzo;
		while ((intermezzo = RegistroIntermezzi.getProssimoIntermezzo(momento)) != null) {
			id.add(intermezzo.getId());
			if (intermezzo instanceof IntermezzoLibroTecnico) {
				// Non lo si segna: lo si legge dopo, a mano
				break;
			}
			RegistroIntermezzi.segnaScattato(intermezzo);
		}
		return id;
	}

	/**
	 * Un truccatore che fa dare al cofano quell'oggetto speciale (vedi Cofano.opzioniOggettoSpeciale), e lascia il resto al caso.
	 */
	private static TruccatoreDiDadi cofanoCon(String oggettoSpeciale) {
		return new TruccatoreDiDadi() {
			@Override
			public OptionalInt lancio(ContestoLancio contesto, int min, int max) {
				return OptionalInt.empty();
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
				return contesto == ContestoLancio.COFANO_OGGETTO ? Optional.of((T) oggettoSpeciale) : Optional.empty();
			}
		};
	}
}
