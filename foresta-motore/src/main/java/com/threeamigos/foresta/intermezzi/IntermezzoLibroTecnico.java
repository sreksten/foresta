package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.leggendari.LibroTecnico;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Il capo del gruppo ha appena trovato un libro tecnico in un cofano (vedi LibroTecnico e Cofano): nella locazione in cui si
 * trova, ne legge il titolo, lo sfoglia e chiude la questione ("Deve essere una roba di quelle di cui parla spesso
 * l'Alchimista"). I testi sono in leggendari.txt.
 * <p>
 * Scatta subito dopo: l'oggetto di una locazione (il cofano) si raccoglie quando la locazione finisce, e subito dopo, controllate
 * le missioni, è il momento degli intermezzi di {@link MomentoIntermezzo#LOCAZIONE_COMPLETATA}. L'identificativo è quello del libro, così IntermezziMD ricorda quali libri
 * sono già stati trovati: un cofano non dà due volte lo stesso libro nella stessa partita (vedi {@link #isTrovato}).
 */
public class IntermezzoLibroTecnico implements Intermezzo {

	private static final String CAPO = "capo";
	private static final String LIBRO = "libro";
	private static final String IMMAGINE_LIBRO = "oggetti/LibroTecnico.gif";

	// I libri trovati e non ancora letti, nell'ordine in cui sono stati trovati: di solito uno, ma un cofano doppio ne può dare due
	private static final List<LibroTecnico> daLeggere = new ArrayList<>();

	private final LibroTecnico libro;

	IntermezzoLibroTecnico() {
		libro = daLeggere.isEmpty() ? null : daLeggere.get(0);
	}

	/**
	 * Il gruppo ha trovato quel libro: l'intermezzo scatterà al prossimo momento buono.
	 */
	public static void trovato(LibroTecnico libro) {
		daLeggere.add(libro);
	}

	/**
	 * Se il libro è già stato trovato in questa partita: letto in un intermezzo, o appena trovato e ancora da leggere.
	 */
	public static boolean isTrovato(LibroTecnico libro) {
		return daLeggere.contains(libro) || RegistroIntermezzi.isScattato(id(libro));
	}

	/**
	 * Dimentica i libri da leggere, se ce n'erano: a una nuova partita o a un caricamento.
	 */
	public static void reimposta() {
		daLeggere.clear();
	}

	private static String id(LibroTecnico libro) {
		return TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO.name() + "_" + libro.getChiave();
	}

	@Override
	public String getId() {
		return libro == null ? TipoIntermezzo.INTERMEZZO_LIBRO_TECNICO.name() : id(libro);
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return libro != null && momento == MomentoIntermezzo.LOCAZIONE_COMPLETATA;
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		daLeggere.remove(libro);
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		List<String> battute = libro.getBattute();
		return Collections.singletonList(new PaginaIntermezzo("Un libro in fondo al cofano.")
				.conSfondo(ImmagineIntermezzo.locazione(gruppo.getTipoLocazioneCorrente()))
				.conElemento(ElementoIntermezzo.personaggio(CAPO, gruppo.getCapo().getClasse(), 0.45, 0.61)
						.conBocca(0.5, -0.15)
						.guarda(Verso.DESTRA))
				.conElemento(ElementoIntermezzo.di(LIBRO, ImmagineIntermezzo.risorsa(IMMAGINE_LIBRO), 0.55, 0.66))
				.conBattuta(BattutaIntermezzo.di(CAPO, ProduttoreDiTestiCasuale.letturaDelLibroTecnico(libro.getTitoloDaLeggere())))
				.conBattuta(BattutaIntermezzo.di(CAPO, battute.get(Dado.tiraAncheAUnaFaccia(battute.size()) - 1)))
				.conBattuta(BattutaIntermezzo.di(CAPO, ProduttoreDiTestiCasuale.conclusioneSulLibroTecnico())));
	}
}
