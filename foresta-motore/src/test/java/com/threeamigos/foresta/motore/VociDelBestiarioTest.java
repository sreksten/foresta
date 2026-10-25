package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.FabbricaLocazioni;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le voci del bestiario coprono tutti i personaggi che si incontrano come avversari (e viceversa): nessuna voce è irraggiungibile, e quindi
 * il trofeo Enciclopedista è sempre vincibile, e nessuna missione o locazione nuova mette in campo un nemico senza voce.
 */
class VociDelBestiarioTest {

	// I boss dei castelli, che non stanno nelle liste degli incontri (vedi i trofei UCCIDI_*)
	private static final Set<TipoPersonaggio> BOSS = EnumSet.of(TipoPersonaggio.DRAGO, TipoPersonaggio.IDRA, TipoPersonaggio.LICH,
			TipoPersonaggio.STREGA, TipoPersonaggio.MINOTAURO_GIGANTE);

	private static final Pattern NEMICO = Pattern.compile("(?:NEMICO|ONDATA_[0-9]+|SFIDANTI|CAMPIONE)=([A-Z_]+)");

	private static Set<TipoPersonaggio> nemiciDelleMissioni() throws IOException {
		Set<TipoPersonaggio> nemici = EnumSet.noneOf(TipoPersonaggio.class);
		try (InputStream flusso = VociDelBestiarioTest.class.getResourceAsStream("/com/threeamigos/foresta/testi/missioni.txt")) {
			assertNotNull(flusso, "missioni.txt");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(flusso, StandardCharsets.UTF_8))) {
				String riga;
				while ((riga = reader.readLine()) != null) {
					Matcher m = NEMICO.matcher(riga);
					while (m.find()) {
						nemici.add(TipoPersonaggio.valueOf(m.group(1)));
					}
				}
			}
		}
		return nemici;
	}

	private static Set<TipoPersonaggio> incontriDelleLocazioni() throws Exception {
		Method metodo = LocazioneBase.class.getDeclaredMethod("getPossibiliIncontri");
		metodo.setAccessible(true);
		Set<TipoPersonaggio> incontri = EnumSet.noneOf(TipoPersonaggio.class);
		for (TipoLocazione tipo : TipoLocazione.values()) {
			Locazione locazione;
			try {
				locazione = FabbricaLocazioni.crea(tipo);
			} catch (RuntimeException e) {
				// Un tipo senza una locazione da costruire (non tutti i tipi ne hanno una)
				continue;
			}
			if (locazione instanceof LocazioneBase) {
				incontri.addAll(Arrays.asList((TipoPersonaggio[]) metodo.invoke(locazione)));
			}
		}
		return incontri;
	}

	@Test
	void ogniNemicoDelleMissioniHaUnaVoceEEUnAvversario() throws IOException {
		Set<TipoPersonaggio> nemici = nemiciDelleMissioni();

		assertFalse(nemici.isEmpty(), "la ricerca nelle missioni non funziona");
		for (TipoPersonaggio nemico : nemici) {
			assertTrue(nemico.hasVoceNelBestiario(), nemico + " compare come nemico in missioni.txt e non ha una voce nel bestiario");
			assertTrue(nemico.isAvversario(), nemico + " compare come nemico in missioni.txt ma non è un avversario di TipoPersonaggio");
		}
	}

	@Test
	void ogniIncontroDelleLocazioniHaUnaVoceEEUnAvversario() throws Exception {
		Set<TipoPersonaggio> incontri = incontriDelleLocazioni();

		assertTrue(incontri.size() > 15, "la ricerca nelle locazioni non funziona: " + incontri);
		for (TipoPersonaggio incontro : incontri) {
			assertTrue(incontro.isAvversario(), incontro + " si incontra in una locazione ma non è un avversario di TipoPersonaggio");
		}
	}

	@Test
	void ogniAvversarioSiIncontraDavveroEQuindiOgniVoceSiPuoSbloccare() throws Exception {
		Set<TipoPersonaggio> raggiungibili = EnumSet.copyOf(incontriDelleLocazioni());
		raggiungibili.addAll(nemiciDelleMissioni());
		raggiungibili.addAll(BOSS);

		Set<TipoPersonaggio> irraggiungibili = Arrays.stream(TipoPersonaggio.values())
				.filter(TipoPersonaggio::isAvversario).filter(c -> !raggiungibili.contains(c)).collect(Collectors.toSet());

		assertTrue(irraggiungibili.isEmpty(), "Avversari che non compaiono mai (e la cui voce non si sbloccherebbe): " + irraggiungibili);
	}

	@Test
	void ogniCompagnoSiSceglieOSiReclutaDavvero() {
		Set<TipoPersonaggio> reclutabili = EnumSet.noneOf(TipoPersonaggio.class);
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			if (classe.isCompagno()) {
				// Si sceglie a inizio partita (Automa) o si recluta in locanda (RegistroPersonaggi): qui basta che si possa creare
				assertNotNull(com.threeamigos.foresta.personaggi.FabbricaPersonaggi.crea(classe, 1), classe.name());
				reclutabili.add(classe.getRuolo());
			}
		}
		assertEquals(8, reclutabili.size(), reclutabili.toString());
	}
}
