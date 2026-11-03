package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Gli articoli, i nomi e il pronome di ogni classe di personaggi non stanno più nelle classi: vengono dai file dei testi
 * ({@code tipo_personaggio_<lingua>.properties}) e dalle regole della lingua.
 */
class NomiGrammaticaliDeiPersonaggiTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(null);
	}

	private static Personaggio modello(TipoPersonaggio tipo) {
		return FabbricaPersonaggi.modello(tipo);
	}

	@Test
	void italiano_articoliPerGenereESuono() {
		Personaggio goblin = modello(TipoPersonaggio.GOBLIN);
		assertEquals("il ", goblin.getADS());
		assertEquals("i ", goblin.getADP());
		assertEquals("un ", goblin.getAIS());
		assertEquals("alcuni ", goblin.getAIP());
		assertEquals("del ", goblin.getDeS());
		assertEquals("dei ", goblin.getDeP());
		assertEquals("da un ", goblin.getDa());
		assertEquals("Esso", goblin.getPronome());
		assertEquals("Goblin", goblin.getNomeSingolare());
		assertEquals("Goblin", goblin.getNomePlurale());

		Personaggio scheletro = modello(TipoPersonaggio.SCHELETRO);
		assertEquals("lo ", scheletro.getADS());
		assertEquals("gli ", scheletro.getADP());
		assertEquals("uno ", scheletro.getAIS());
		assertEquals("dello ", scheletro.getDeS());
		assertEquals("da uno ", scheletro.getDa());

		Personaggio idra = modello(TipoPersonaggio.IDRA);
		assertEquals("l'", idra.getADS());
		assertEquals("un'", idra.getAIS());
		assertEquals("dell'", idra.getDeS());
		assertEquals("da un'", idra.getDa());
		assertEquals("Idre", idra.getNomePlurale());
		assertEquals("Essa", idra.getPronome());

		Personaggio strega = modello(TipoPersonaggio.STREGA);
		assertEquals("la ", strega.getADS());
		assertEquals("le ", strega.getADP());
		assertEquals("una ", strega.getAIS());
		assertEquals("della ", strega.getDeS());
		assertEquals("da una ", strega.getDa());
	}

	@Test
	void italiano_leEccezioniDeiFileDeiTesti() {
		// La h di Hobgoblin non si pronuncia
		assertEquals("l'", modello(TipoPersonaggio.HOBGOBLIN).getADS());
		// Un nome unico o noto è "dal Drago", non "da un drago"
		assertEquals("dal ", modello(TipoPersonaggio.DRAGO).getDa());
		assertEquals("dall'", modello(TipoPersonaggio.ELFO).getDa());
		// "l'Uomo": prima era scritto a mano "il Uomo"
		assertEquals("l'", modello(TipoPersonaggio.UOMO).getADS());
		assertEquals("Uomini", modello(TipoPersonaggio.UOMO).getNomePlurale());
	}

	@Test
	void inglese_articoliENomi() {
		Testi.impostaLingua("en");
		Personaggio elfo = modello(TipoPersonaggio.ELFO);
		assertEquals("the ", elfo.getADS());
		assertEquals("an ", elfo.getAIS());
		assertEquals("some ", elfo.getAIP());
		assertEquals("of the ", elfo.getDeS());
		assertEquals("by the ", elfo.getDa());
		assertEquals("Elf", elfo.getNomeSingolare());
		assertEquals("Elves", elfo.getNomePlurale());
		assertEquals("He", elfo.getPronome());
		Personaggio goblin = modello(TipoPersonaggio.GOBLIN);
		assertEquals("a ", goblin.getAIS());
		assertEquals("by a ", goblin.getDa());
		assertEquals("It", goblin.getPronome());
		assertEquals("Goblins", goblin.getNomePlurale());
	}

	@Test
	void ogniClasseHaIlSuoNomeNellaLinguaScelta() {
		for (String lingua : new String[] {"it", "en", "en-GB"}) {
			Testi.impostaLingua(lingua);
			for (TipoPersonaggio tipo : TipoPersonaggio.values()) {
				Personaggio p = modello(tipo);
				assertFalse(p.getNomeSingolare().isEmpty(), lingua + " " + tipo);
				assertFalse(p.getNomePlurale().isEmpty(), lingua + " " + tipo);
				assertFalse(p.getPronome().isEmpty(), lingua + " " + tipo);
				assertFalse(p.getADS().isEmpty(), lingua + " " + tipo);
			}
		}
	}
}
