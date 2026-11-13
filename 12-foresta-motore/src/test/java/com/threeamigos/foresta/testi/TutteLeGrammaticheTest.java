package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.artefatti.ProduttoreDiArtefattiCasuale;
import com.threeamigos.foresta.artefatti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni grammatica del gioco si carica, in ogni lingua: i testi (fiabe, locande, missioni, nomi...), gli artefatti e gli
 * ingredienti, i leggendari, e i cataloghi che si leggono dai leggendari (set, oggetti, libri tecnici), che controllano le
 * loro righe al caricamento. Una grammatica rotta impedisce al gioco di partire (vedi {@link PrecaricatoreDeiTesti}): questo
 * test se ne accorge prima, e siccome {@code mvn package} esegue i test, con una grammatica rotta il jar non si costruisce.
 */
class TutteLeGrammaticheTest {

	@AfterEach
	void tornaAllItaliano() {
		Testi.impostaLingua(Testi.LINGUA_PREDEFINITA);
	}

	@ParameterizedTest
	@ValueSource(strings = {"it", "en", "en-GB"})
	void ogniGrammaticaDelGiocoSiCaricaInOgniLingua(String lingua) {
		Testi.impostaLingua(lingua);
		// Le stesse grammatiche che il gioco carica all'avvio, ma qui un errore fa fallire il test invece di chiudere il gioco
		assertDoesNotThrow(ProduttoriDiTesti::precarica, lingua);
		assertDoesNotThrow(ProduttoreDiArtefattiCasuale::precarica, lingua);
		// I cataloghi leggono le righe dei leggendari e le controllano tutte: un campo sbagliato è un errore che dice la riga
		assertFalse(CatalogoLeggendari.getTuttiISet().isEmpty(), lingua);
		assertFalse(CatalogoLeggendari.getTuttiILeggendari().isEmpty(), lingua);
		assertFalse(LibroTecnico.tutti().isEmpty(), lingua);
	}
}
