package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.libgdx.sviluppo.DatiDiProva;
import com.threeamigos.foresta.libgdx.sviluppo.TatticheDiProva;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra.Tasto;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.immagini.NomiDelleImmagini;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche.Quadro;
import com.threeamigos.foresta.ui.schermate.MenuADiscesa;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmagini;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La schermata delle tattiche e il suo menu a discesa, sui dati di prova: la logica è del modulo comune, qui si prova con la partita finta di libGDX e una
 * schermata di scambio senza disegno.
 */
class EditorDelleTatticheTest {

	private static final MisureDelleImmagini MISURE = new MisureDelleImmagini() {
		@Override
		public Misura personaggio(TipoPersonaggio classe) {
			return new Misura(40, 60);
		}

		@Override
		public Misura immagine(String risorsa) {
			return new Misura(16, 16);
		}
	};

	/** Una schermata di scambio senza disegno, in cui si può muovere il mouse */
	private static final class SchermataDiProva extends SchermataDiScambio {
		SchermataDiProva(VistaPartita partita) {
			super(1024, 768, partita, 360, 700, MISURE);
		}

		int xSinistra() {
			return xMinimaZonaSinistra;
		}
	}

	private SchermataDiProva schermata;
	private EditorDelleTattiche editor;
	private VistaPersonaggio guerriero;

	@BeforeEach
	void prepara() {
		VistaPartita partita = DatiDiProva.partita();
		schermata = new SchermataDiProva(partita);
		editor = new EditorDelleTattiche(schermata, partita, MISURE);
		guerriero = partita.getGruppoGiocatore().getPersonaggio(0);
		editor.impostaTattica(guerriero, TatticheDiProva.tattica(), TatticaMD.VUOTA, null);
	}

	private static List<String> testi(ContenutoDiColonna contenuto) {
		List<String> testi = new ArrayList<>();
		for (Elemento e : contenuto.getElementi()) {
			if (e.getTipo() == Elemento.Tipo.TESTO) {
				testi.addAll(e.getRighe());
			}
		}
		return testi;
	}

	private static long icone(ContenutoDiColonna contenuto, String risorsa) {
		long n = 0;
		for (Elemento e : contenuto.getElementi()) {
			if (e.getTipo() == Elemento.Tipo.IMMAGINE && risorsa.equals(e.getRisorsa())) {
				n++;
			}
		}
		return n;
	}

	@Test
	void laColonnaCentraleHaIlNomeLImmagineIlPostoEPredefinita() {
		ContenutoDiColonna colonna = editor.colonnaCentrale();
		assertTrue(testi(colonna).contains(guerriero.getNome(VistaPersonaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA)));
		assertEquals(1, icone(colonna, NomiDelleImmagini.personaggio(TipoPersonaggio.GUERRIERO)));
		assertEquals(2, icone(colonna, NomiDelleImmagini.ICONA_SINISTRA) + icone(colonna, NomiDelleImmagini.ICONA_DESTRA), "le frecce che spostano nel gruppo");
		assertEquals(0, icone(colonna, NomiDelleImmagini.ICONA_SI), "senza modifiche non ci sono Sì e No");
		assertTrue(testi(colonna).stream().anyMatch(t -> t.toLowerCase().contains("predefinit")));
	}

	@Test
	void conUnaModificaCompaionoSiENo() {
		editor.colonnaCentrale();
		assertTrue(editor.attivaPezzo("Nuova voce"));
		ContenutoDiColonna colonna = editor.colonnaCentrale();
		assertEquals(1, icone(colonna, NomiDelleImmagini.ICONA_SI));
		assertEquals(1, icone(colonna, NomiDelleImmagini.ICONA_NO));
	}

	@Test
	void l_elencoHaUnaRigaPerOgniPezzoDellaTatticaEOgniRigaHaIlSuoContenuto() {
		editor.colonnaCentrale();
		Quadro quadro = editor.quadro();
		assertNull(quadro.menu);
		assertNull(quadro.aiuto, "con il mouse fuori dall'elenco non c'è nessun aiuto");
		ElencoScorrevole<EditorDelleTattiche.Riga> elenco = quadro.elenco;
		java.util.Set<EditorDelleTattiche.Riga> righe = new java.util.LinkedHashSet<>();
		for (int quota = 0; quota < elenco.altezzaDellElenco(100_000); quota++) {
			EditorDelleTattiche.Riga riga = elenco.riferimentoTitoloAllaQuota(quota);
			if (riga != null) {
				righe.add(riga);
			}
		}
		// La voce 1 ha il suo nome, la riga su chi agisce, i filtri, le condizioni, le azioni; due voci e le righe di "Nuova voce" e della riserva
		assertTrue(righe.size() > 15, "le righe: " + righe.size());
		assertTrue(testi(quadro.spiegazione).stream().anyMatch(t -> t.toLowerCase().contains("voci")), "senza una voce sotto il mouse la spiegazione dice le regole");
	}

	@Test
	void sopraUnPezzoCliccabileCompareLAiutoESiVedeLaSpiegazioneDellaVoce() {
		editor.colonnaCentrale();
		Quadro iniziale = editor.quadro();
		// Cerco un punto dell'elenco su cui il mouse dia un aiuto: scorro le quote con il mouse
		String aiuto = null;
		for (int y = 40; y < 400 && aiuto == null; y += 4) {
			for (int x = schermata.xSinistra() + 20; x < schermata.xSinistra() + 300 && aiuto == null; x += 12) {
				schermata.processaMovimento(x, y);
				editor.colonnaCentrale();
				aiuto = editor.quadro().aiuto;
			}
		}
		assertNotNull(aiuto, "da qualche parte sopra l'elenco c'è un pezzo che dà un aiuto");
		assertNotNull(iniziale);
	}

	@Test
	void unMenuSiApreCatturaIClickESiChiudeConUnClickFuori() {
		editor.colonnaCentrale();
		assertTrue(editor.attivaPezzo("(aggiungi un'azione)") || editor.attivaPezzo("(aggiungi una condizione)"));
		assertTrue(editor.isMenuAperto());
		Quadro quadro = editor.quadro();
		assertNotNull(quadro.menu);
		// Con il menu aperto pressione e rotella sono per lui
		assertTrue(editor.processaPressione(10, 10, Tasto.SINISTRO));
		assertTrue(editor.processaRilascio(10, 10, Tasto.SINISTRO));
		// Un click fuori lo chiude senza cambiare niente
		int vociPrima = editor.getBozza().getVoci().size();
		editor.processaClick(1, 1, Tasto.SINISTRO);
		assertFalse(editor.isMenuAperto());
		assertEquals(vociPrima, editor.getBozza().getVoci().size());
		assertFalse(editor.processaPressione(10, 10, Tasto.SINISTRO), "senza menu la pressione non è sua");
	}

	@Test
	void ilMenuAcquistaLaScelta() {
		String[] scelto = new String[1];
		List<MenuADiscesa.Scelta<String>> scelte = Arrays.asList(new MenuADiscesa.Scelta<>("Uno", "1"), new MenuADiscesa.Scelta<>("Due", "2", "Il due"),
				new MenuADiscesa.Scelta<>("Tre", "3"));
		MenuADiscesa<String> menu = new MenuADiscesa<>(scelte, "2", 100, 100, 80, 1024, 768, MISURE, s -> scelto[0] = s);
		assertTrue(menu.isAperto());
		assertTrue(menu.getX() >= MenuADiscesa.MARGINE && menu.getX() + menu.getLarghezza() <= 1024);
		assertEquals(100, menu.getY(), "c'è posto sotto: si apre sotto");
		// La seconda scelta sta nella seconda riga
		String vistaDallAlto = null;
		for (int y = menu.getY(); y < menu.getY() + menu.getAltezza(); y++) {
			String valore = menu.sceltaAlla(menu.getX() + 10, y);
			if ("2".equals(valore)) {
				assertEquals("Il due", menu.sceltaSotto(menu.getX() + 10, y).spiegazione);
				menu.click(menu.getX() + 10, y);
				vistaDallAlto = valore;
				break;
			}
		}
		assertEquals("2", vistaDallAlto);
		assertEquals("2", scelto[0]);
		assertFalse(menu.isAperto());
	}

	@Test
	void ilMenuSiApreSopraSeSottoNonCePosto() {
		List<MenuADiscesa.Scelta<Integer>> scelte = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			scelte.add(new MenuADiscesa.Scelta<>("Scelta numero " + i, i));
		}
		MenuADiscesa<Integer> menu = new MenuADiscesa<>(scelte, 15, 100, 700, 680, 1024, 768, MISURE, s -> { });
		assertTrue(menu.getY() + menu.getAltezza() <= 680, "sopra la riga che lo apre");
		assertTrue(menu.elenco(null).isScorrevole(menu.getAltezzaInterna()), "trenta scelte non stanno");
		// Si apre con la scelta corrente (la 15) a metà altezza: lo scorrimento non è a zero
		assertTrue(menu.getScorrimento().getScorrimento() > 0);
	}

	@Test
	void unMenuSenzaSceltePuoDisegnarsi() {
		MenuADiscesa<String> menu = new MenuADiscesa<>(new ArrayList<>(), null, 100, 100, 80, 640, 480, MISURE, s -> { });
		assertNull(menu.sceltaAlla(0, 0));
		assertDoesNotThrow(() -> menu.elenco(null).getMassimoScorrimento(menu.getAltezzaInterna()));
	}
}
