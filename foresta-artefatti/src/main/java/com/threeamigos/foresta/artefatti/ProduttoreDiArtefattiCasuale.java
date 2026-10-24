package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.artefatti.leggendari.SetLeggendario;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.testi.CaricatoreDiGrammatiche;
import com.threeamigos.foresta.testi.GrammaticaPigra;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * I testi a caso degli artefatti: le righe di {@code leggendari.txt} (gli oggetti leggendari, i loro set e i libri tecnici, che
 * {@code OggettoLeggendario}, {@code SetLeggendario} e {@code LibroTecnico} leggono riga per riga), le battute che li
 * raccontano e i nomi degli oggetti che fanno salire di livello un artefatto ({@code artefatti2.txt}). Vedi GrammarBean.md e
 * artefatti_e_incantamenti.md.
 */
public final class ProduttoreDiArtefattiCasuale {

	private static final GrammaticaPigra LEGGENDARI = new GrammaticaPigra(() -> CaricatoreDiGrammatiche.carica(
			ProduttoreDiArtefattiCasuale.class, "/com/threeamigos/foresta/artefatti/leggendari.txt", null));
	private static final GrammaticaPigra AGGIORNAMENTI = new GrammaticaPigra(() -> CaricatoreDiGrammatiche.carica(
			ProduttoreDiArtefattiCasuale.class, "/com/threeamigos/foresta/artefatti/artefatti2.txt", null));
	private static final String OGGETTO_LEGGENDARIO = "OGGETTO_LEGGENDARIO";
	private static final String SET_LEGGENDARIO = "SET_LEGGENDARIO";
	private static final String LIBRO_TECNICO = "LIBRO_TECNICO";
	private static final String LEGGENDARIO_DI_BOTTINO = "LEGGENDARIO_DI_BOTTINO";

	private ProduttoreDiArtefattiCasuale() {
	}

	/**
	 * Carica subito le grammatiche, invece di aspettare il primo uso (vedi {@link PrecaricatoreDeiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se una grammatica manca o non è valida
	 */
	public static void precarica() {
		LEGGENDARI.get();
		AGGIORNAMENTI.get();
	}

	/**
	 * Il nome a caso, con l'articolo, di un oggetto che fa salire di un livello un artefatto: "la Pergamena dell'Ultima Moda" (vedi
	 * artefatti2.txt e TipoArtefatto.AGGIORNAMENTO).
	 */
	public static String nomeAggiornamento() {
		return AGGIORNAMENTI.get().produce("NOME_AGGIORNAMENTO").get(0).trim();
	}

	/**
	 * Un oggetto leggendario di LEGGENDARI.txt (vedi OggettoLeggendario) che non è già stato pescato, o vuoto se sono
	 * stati pescati tutti. Le righe sono one-shot: si riparte ogni volta da tutte e si scartano quelle già pescate
	 * nella partita, che lo sa dalle sue missioni (la grammatica non si salva con la partita).
	 */
	public static synchronized Optional<String> oggettoLeggendario(Predicate<String> giaPescato) {
		LEGGENDARI.get().reset();
		while (LEGGENDARI.get().canProduce(OGGETTO_LEGGENDARIO)) {
			String riga = String.join(" ", LEGGENDARI.get().produce(OGGETTO_LEGGENDARIO)).trim();
			if (!giaPescato.test(riga)) {
				return Optional.of(riga);
			}
		}
		return Optional.empty();
	}

	/**
	 * La prima battuta della leggenda, che dice di quale oggetto si parla (vedi INTRODUZIONE_LEGGENDA in LEGGENDARI.txt).
	 *
	 * @param nomeBreve il nome breve dell'oggetto, con l'articolo: "lo Scudo Fiscale"
	 */
	public static synchronized String introduzioneDellaLeggenda(String nomeBreve) {
		LEGGENDARI.get().addFixedProduction("DELL_OGGETTO", Articoli.conPreposizione("di", nomeBreve));
		LEGGENDARI.get().addFixedProduction("L_OGGETTO", nomeBreve);
		return LEGGENDARI.get().produce("INTRODUZIONE_LEGGENDA").get(0).trim();
	}

	/**
	 * Tutti gli oggetti leggendari di LEGGENDARI.txt, in ordine casuale.
	 */
	public static List<String> tuttiGliOggettiLeggendari() {
		return tutteLeRigheLeggendarie(OGGETTO_LEGGENDARIO);
	}

	/**
	 * Tutti i leggendari di bottino di LEGGENDARI.txt: quelli che lascia un avversario, e che nessuna leggenda racconta.
	 */
	public static List<String> tuttiIBottiniLeggendari() {
		return tutteLeRigheLeggendarie(LEGGENDARIO_DI_BOTTINO);
	}

	/**
	 * Tutti i set leggendari di LEGGENDARI.txt (vedi SetLeggendario), in ordine casuale.
	 */
	public static List<String> tuttiISetLeggendari() {
		return tutteLeRigheLeggendarie(SET_LEGGENDARIO);
	}

	/**
	 * Tutti i libri tecnici di LEGGENDARI.txt (vedi LibroTecnico), in ordine casuale.
	 */
	public static List<String> tuttiILibriTecnici() {
		return tutteLeRigheLeggendarie(LIBRO_TECNICO);
	}

	/**
	 * Quel che il capo legge sulla copertina di un libro tecnico (vedi LETTURA_LIBRO_TECNICO in LEGGENDARI.txt).
	 *
	 * @param titolo il titolo fra virgolette e la descrizione: "The C Programming Language", di B. Kernighan e D. Ritchie
	 */
	public static synchronized String letturaDelLibroTecnico(String titolo) {
		LEGGENDARI.get().addFixedProduction("TITOLO", titolo);
		return LEGGENDARI.get().produce("LETTURA_LIBRO_TECNICO").get(0).trim();
	}

	/**
	 * Come il capo chiude la questione, dopo aver sfogliato un libro tecnico: ogni libro ha la sua produzione, perché rimanda a chi
	 * può dirgliene di più (vedi CONCLUSIONE_ seguito dalla chiave del libro in LEGGENDARI.txt).
	 *
	 * @param chiaveLibro la chiave del libro (vedi LibroTecnico.getChiave)
	 */
	public static synchronized String conclusioneSulLibroTecnico(String chiaveLibro) {
		return LEGGENDARI.get().produce("CONCLUSIONE_" + chiaveLibro).get(0).trim();
	}

	/**
	 * Quel che dice l'incantatore quando una riprogrammazione va male (vedi ERRORE_RIPROGRAMMAZIONE in LEGGENDARI.txt).
	 */
	public static synchronized String erroreDiRiprogrammazione() {
		return LEGGENDARI.get().produce("ERRORE_RIPROGRAMMAZIONE").get(0).trim();
	}

	/**
	 * Tutte le righe di una produzione one-shot di LEGGENDARI.txt: si pescano finché ce ne sono.
	 */
	private static synchronized List<String> tutteLeRigheLeggendarie(String produzione) {
		List<String> righe = new ArrayList<>();
		LEGGENDARI.get().reset();
		while (LEGGENDARI.get().canProduce(produzione)) {
			righe.add(String.join(" ", LEGGENDARI.get().produce(produzione)).trim());
		}
		return righe;
	}
}
