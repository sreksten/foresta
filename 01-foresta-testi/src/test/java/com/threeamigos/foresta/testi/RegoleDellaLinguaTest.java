package com.threeamigos.foresta.testi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole grammaticali dell'italiano e dell'inglese.
 */
class RegoleDellaLinguaTest {

	private static final RegoleDellaLingua IT = Lingue.per("it");
	private static final RegoleDellaLingua EN = Lingue.per("en");

	private static Nome m(String nome) {
		return new Nome(nome, nome + "i", Genere.MASCHILE, null, null);
	}

	private static Nome f(String nome) {
		return new Nome(nome, nome + "e", Genere.FEMMINILE, null, null);
	}

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	@Test
	void ilGenereELaScelta() {
		assertSame(IT, Lingue.corrente());
		Testi.impostaLingua("en-GB");
		assertSame(EN, Lingue.corrente());
		Testi.impostaLingua("zz");
		assertSame(IT, Lingue.corrente());
	}

	@Test
	void italiano_determinativo_singolare() {
		assertEquals("il ", IT.articoloDeterminativo(m("Goblin"), false));
		assertEquals("lo ", IT.articoloDeterminativo(m("Scheletro"), false));
		assertEquals("lo ", IT.articoloDeterminativo(m("Zombi"), false));
		assertEquals("lo ", IT.articoloDeterminativo(m("Gnomo"), false));
		assertEquals("l'", IT.articoloDeterminativo(m("Elfo"), false));
		assertEquals("la ", IT.articoloDeterminativo(f("Strega"), false));
		assertEquals("la ", IT.articoloDeterminativo(f("Sciamana"), false));
		assertEquals("l'", IT.articoloDeterminativo(f("Idra"), false));
	}

	@Test
	void italiano_determinativo_plurale_e_indeterminativo() {
		assertEquals("i ", IT.articoloDeterminativo(m("Goblin"), true));
		assertEquals("gli ", IT.articoloDeterminativo(m("Elfo"), true));
		assertEquals("gli ", IT.articoloDeterminativo(m("Spettro"), true));
		assertEquals("le ", IT.articoloDeterminativo(f("Strega"), true));
		assertEquals("un ", IT.articoloIndeterminativo(m("Goblin"), false));
		assertEquals("uno ", IT.articoloIndeterminativo(m("Spirito"), false));
		assertEquals("una ", IT.articoloIndeterminativo(f("Strega"), false));
		assertEquals("un'", IT.articoloIndeterminativo(f("Idra"), false));
		assertEquals("alcuni ", IT.articoloIndeterminativo(m("Goblin"), true));
		assertEquals("alcune ", IT.articoloIndeterminativo(f("Strega"), true));
	}

	@Test
	void italiano_un_nome_puo_dichiarare_il_suo_suono() {
		// "l'Hobgoblin": la h non si pronuncia
		Nome hobgoblin = new Nome("Hobgoblin", "Hobgoblin", Genere.MASCHILE, Suono.VOCALE, null);
		assertEquals("l'", IT.articoloDeterminativo(hobgoblin, false));
		assertEquals("un ", IT.articoloIndeterminativo(hobgoblin, false));
	}

	@Test
	void unNomeConArticoliSuoiLiUsaAlPostoDellaRegola() {
		Nome schinieri = new Nome("schinieri", "schinieri", Genere.MASCHILE, null, null, false,
				new String[] {"gli ", "gli ", "un paio di ", "alcune paia di "});
		assertEquals("gli ", IT.articoloDeterminativo(schinieri, false));
		assertEquals("un paio di ", IT.articoloIndeterminativo(schinieri, false));
		assertEquals("alcune paia di ", IT.articoloIndeterminativo(schinieri, true));
		assertEquals("degli ", IT.preposizioneArticolata(Preposizione.DI, schinieri, false));
		// Senza eccezioni vale la regola
		assertEquals("il ", IT.articoloDeterminativo(m("Goblin"), false));
	}

	@Test
	void italiano_preposizioni_articolate() {
		assertEquals("del ", IT.preposizioneArticolata(Preposizione.DI, m("Goblin"), false));
		assertEquals("dello ", IT.preposizioneArticolata(Preposizione.DI, m("Scheletro"), false));
		assertEquals("dell'", IT.preposizioneArticolata(Preposizione.DI, f("Idra"), false));
		assertEquals("della ", IT.preposizioneArticolata(Preposizione.DI, f("Strega"), false));
		assertEquals("dei ", IT.preposizioneArticolata(Preposizione.DI, m("Goblin"), true));
		assertEquals("delle ", IT.preposizioneArticolata(Preposizione.DI, f("Strega"), true));
		assertEquals("da un ", IT.daAgente(m("Goblin")));
		assertEquals("da un'", IT.daAgente(f("Idra")));
		assertEquals("dal ", IT.daAgente(new Nome("Drago", "Draghi", Genere.MASCHILE, null, null, true)));
		assertEquals("dall'", IT.daAgente(new Nome("Elfo", "Elfi", Genere.MASCHILE, null, null, true)));
		assertEquals("by the ", EN.daAgente(new Nome("Dragon", "Dragons", Genere.NEUTRO, null, null, true)));
		assertEquals("nel Tempio del Sole", IT.conPreposizione(Preposizione.IN, "il Tempio del Sole"));
		assertEquals("all'Eremo", IT.conPreposizione(Preposizione.A, "l'Eremo"));
	}

	@Test
	void italiano_numeri_ed_elenchi() {
		assertEquals("una", IT.cardinale(1, Genere.FEMMINILE));
		assertEquals("uno", IT.cardinale(1, Genere.MASCHILE));
		assertEquals("11", IT.cardinale(11, Genere.MASCHILE));
		assertEquals("la terza", IT.ordinale(3, Genere.FEMMINILE, true));
		assertEquals("l'undicesimo", IT.ordinale(11, Genere.MASCHILE, true));
		assertEquals("", IT.elenco(Collections.<String>emptyList()));
		assertEquals("A", IT.elenco(Arrays.asList("A")));
		assertEquals("A e B", IT.elenco(Arrays.asList("A", "B")));
		assertEquals("A, B e C", IT.elenco(Arrays.asList("A", "B", "C")));
		assertEquals("Il goblin", IT.inizialeMaiuscola("il goblin"));
	}

	@Test
	void inglese_articoli() {
		assertEquals("the ", EN.articoloDeterminativo(m("Goblin"), false));
		assertEquals("the ", EN.articoloDeterminativo(m("Goblin"), true));
		assertEquals("a ", EN.articoloIndeterminativo(Nome.di("goblin", "goblins"), false));
		assertEquals("an ", EN.articoloIndeterminativo(Nome.di("elf", "elves"), false));
		assertEquals("an ", EN.articoloIndeterminativo(Nome.di("hour", "hours"), false));
		assertEquals("a ", EN.articoloIndeterminativo(Nome.di("unicorn", "unicorns"), false));
		assertEquals("an ", EN.articoloIndeterminativo(Nome.di("umbrella", "umbrellas"), false));
		assertEquals("some ", EN.articoloIndeterminativo(Nome.di("goblin", "goblins"), true));
		// L'eccezione dichiarata dal nome vince
		assertEquals("an ", EN.articoloIndeterminativo(new Nome("heirloom", "heirlooms", Genere.NEUTRO, Suono.VOCALE, null), false));
		assertEquals("by a ", EN.daAgente(Nome.di("goblin", "goblins")));
	}

	@Test
	void inglese_preposizioni_numeri_ed_elenchi() {
		assertEquals("of the ", EN.preposizioneArticolata(Preposizione.DI, m("Goblin"), false));
		assertEquals("in the Temple of the Sun", EN.conPreposizione(Preposizione.IN, "the Temple of the Sun"));
		assertEquals("three", EN.cardinale(3, Genere.NEUTRO));
		assertEquals("15", EN.cardinale(15, Genere.NEUTRO));
		assertEquals("third", EN.ordinale(3, Genere.NEUTRO, false));
		assertEquals("the twelfth", EN.ordinale(12, Genere.NEUTRO, true));
		assertEquals("twentieth", EN.ordinale(20, Genere.NEUTRO, false));
		assertEquals("twenty-first", EN.ordinale(21, Genere.NEUTRO, false));
		assertEquals("forty-first", EN.ordinale(41, Genere.NEUTRO, false));
		assertEquals("A, B, and C", EN.elenco(Arrays.asList("A", "B", "C")));
		assertEquals("A and B", EN.elenco(Arrays.asList("A", "B")));
		assertEquals("The goblin", EN.inizialeMaiuscola("the goblin"));
	}

	@Test
	void arrivoEVicinoSiDiconoComeServeInOgniLingua() {
		// Dove si arriva: in italiano «a», in inglese «in»
		assertEquals("a Fleena", IT.conPreposizione(Preposizione.ARRIVO, "Fleena"));
		assertEquals("alle Rovine", IT.conPreposizione(Preposizione.ARRIVO, "le Rovine"));
		assertEquals("in the city of Fleena", EN.conPreposizione(Preposizione.ARRIVO, "the city of Fleena"));
		// Vicino a un luogo: in italiano resta «a» (dopo «vicino»), in inglese non vuole preposizione ("near the Ruins")
		assertEquals("alle Rovine", IT.conPreposizione(Preposizione.VICINO, "le Rovine"));
		assertEquals("the Ruins", EN.conPreposizione(Preposizione.VICINO, "the Ruins"));
		assertEquals("the ", EN.preposizioneArticolata(Preposizione.VICINO, m("tempio"), false));
	}
}
