package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.ComandiAssociati;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni abilità innata di chierico e druido ha il suo comando e la sua icona, e le quattro classi hanno la loro immagine (per ora copie di quelle di
 * guerriero e bardo, e le icone delle abilità sono copie della pergamena: vedi todo.md).
 */
class IconeDelleAbilitaTest {

	@Test
	void ognunaHaUnComandoDiversoEUnIconaChePuoEssereLetta() {
		Set<Comando> comandi = new HashSet<>();
		for (TipoAbilita tipo : TipoAbilita.values()) {
			Comando comando = ComandiAssociati.di(tipo);
			assertNotNull(comando, tipo.name());
			assertTrue(comandi.add(comando), "comando ripetuto: " + comando);
			assertSame(tipo, ComandiAssociati.abilita(comando));
			assertNotNull(TipoIcona.ofComando(comando).getIcona(), comando.name());
		}
		assertEquals(TipoAbilita.values().length, comandi.size());
	}

	@Test
	void leQuattroClassiHannoIcona() {
		for (Comando comando : Arrays.asList(Comando.CHIERICO, Comando.CHIERICA, Comando.DRUIDO, Comando.DRUIDA)) {
			assertNotNull(TipoIcona.ofComando(comando).getIcona(), comando.name());
		}
	}

	@Test
	void leQuattroClassiHannoLaLoroImmagine() {
		for (TipoPersonaggio classe : Arrays.asList(TipoPersonaggio.CHIERICO, TipoPersonaggio.CHIERICA, TipoPersonaggio.DRUIDO, TipoPersonaggio.DRUIDA)) {
			assertNotNull(MappaPersonaggioImmagine.getImmagine(classe), classe.name());
			assertNotNull(MappaPersonaggioImmagine.getIcona(classe), classe.name());
		}
	}
}
