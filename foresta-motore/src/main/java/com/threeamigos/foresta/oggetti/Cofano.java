package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.oggetti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.oggetti.leggendari.LibroTecnico;
import com.threeamigos.foresta.oggetti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.strumenti.ContestoLancio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tipi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Un cofano: il gruppo che lo apre trova qualcosa a caso fra una pergamena, tre pergamene, una pozione (della salute, della salute
 * grande o della magia), monete o pietre preziose. Con una piccola probabilità c'è anche un ingrediente magico o un artefatto, e
 * molto di rado un libro tecnico (vedi LibroTecnico), che il capo legge subito in un intermezzo.
 */
public class Cofano extends OggettoBase implements Oggetto {

	public Cofano() {
		super();
	}
	
	public Cofano(int q) {
		quantita = q;
	}

	public String getAIS() {
		return Articoli.UN;
	}

	public String getAIP() {
		return Articoli.ALCUNI;
	}

	public String getADS() {
		return Articoli.IL;
	}

	public String getADP() {
		return Articoli.I;
	}

	public String getNomeSingolare() {
		return "cofano";
	}

	public String getNomePlurale() {
		return "cofani";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.COFANO;
	}

	/**
	 * Con probabilità 10% un ingrediente magico, con un altro 10% un artefatto casuale, con un altro 3% un oggetto che fa salire di
	 * livello un artefatto (si trova solo qui), con un altro 0,5% un libro tecnico non ancora trovato (si trova solo qui, ed è null
	 * se sono già stati trovati tutti), altrimenti null (vedi Costanti).
	 */
	static Artefatto artefattoRaro(double tiro, GeneratoreArtefatti generatore, int livello) {
		double soglia = Costanti.COFANO_PROBABILITA_PERGAMENA;
		if (tiro < soglia) {
			return generatore.generaIngrediente(livello);
		}
		soglia += Costanti.COFANO_PROBABILITA_ARTEFATTO;
		if (tiro < soglia) {
			return generatore.generaArtefattoCasuale(livello);
		}
		soglia += Costanti.COFANO_PROBABILITA_AGGIORNAMENTO;
		if (tiro < soglia) {
			return generatore.generaAggiornamento(livello);
		}
		soglia += Costanti.COFANO_PROBABILITA_LIBRO_TECNICO;
		if (tiro < soglia) {
			return libroTecnicoDaTrovare(generatore, livello);
		}
		return null;
	}

	/**
	 * Un libro tecnico a caso fra quelli non ancora trovati nella partita, o null se sono stati trovati tutti.
	 */
	private static Artefatto libroTecnicoDaTrovare(GeneratoreArtefatti generatore, int livello) {
		List<LibroTecnico> daTrovare = LibroTecnico.tutti().stream()
				.filter(libro -> !IntermezzoLibroTecnico.isTrovato(libro))
				.collect(Collectors.toList());
		if (daTrovare.isEmpty()) {
			return null;
		}
		return daTrovare.get(Dado.tiraAncheAUnaFaccia(daTrovare.size()) - 1).costruisci(generatore, livello);
	}

	/**
	 * Chi non vuole un oggetto speciale preciso in un cofano (la scelta del contesto COFANO_OGGETTO): il cofano va come sempre.
	 */
	public static final String OGGETTO_SPECIALE_CASUALE = "CASUALE";
	/** Nessun oggetto speciale: solo l'esito solito. */
	public static final String OGGETTO_SPECIALE_NESSUNO = "NESSUNO";
	public static final String OGGETTO_SPECIALE_INGREDIENTE = "INGREDIENTE";
	public static final String OGGETTO_SPECIALE_ARTEFATTO = "ARTEFATTO";
	public static final String OGGETTO_SPECIALE_AGGIORNAMENTO = "AGGIORNAMENTO";
	/** Seguito dalla chiave del leggendario nel CatalogoLeggendari. */
	public static final String PREFISSO_LEGGENDARIO = "LEGGENDARIO:";
	/** Seguito dalla chiave del libro tecnico (vedi LibroTecnico). */
	public static final String PREFISSO_LIBRO_TECNICO = "LIBRO_TECNICO:";

	/**
	 * Gli oggetti speciali che un cofano può essere costretto a dare (vedi ContestoLancio.COFANO_OGGETTO): quelli generati
	 * dal livello, ogni leggendario del catalogo e ogni libro tecnico (anche se già trovato).
	 */
	public static List<String> opzioniOggettoSpeciale() {
		List<String> opzioni = new ArrayList<>(Arrays.asList(OGGETTO_SPECIALE_CASUALE, OGGETTO_SPECIALE_NESSUNO,
				OGGETTO_SPECIALE_INGREDIENTE, OGGETTO_SPECIALE_ARTEFATTO, OGGETTO_SPECIALE_AGGIORNAMENTO));
		for (String chiave : CatalogoLeggendari.getTuttiILeggendari().keySet()) {
			opzioni.add(PREFISSO_LEGGENDARIO + chiave);
		}
		for (LibroTecnico libro : LibroTecnico.tutti()) {
			opzioni.add(PREFISSO_LIBRO_TECNICO + libro.getChiave());
		}
		return opzioni;
	}

	/**
	 * L'oggetto speciale del cofano: quello che chi sviluppa ha voluto (vedi {@link #opzioniOggettoSpeciale}), altrimenti
	 * quello del tiro di probabilità.
	 */
	static Artefatto artefattoRaro() {
		String voluto = Dado.sceltaVoluta(ContestoLancio.COFANO_OGGETTO, opzioniOggettoSpeciale())
				.orElse(OGGETTO_SPECIALE_CASUALE);
		GeneratoreArtefatti generatore = GeneratoreArtefatti.istanza();
		int livello = Statistiche.getLivello();
		switch (voluto) {
			case OGGETTO_SPECIALE_NESSUNO:
				return null;
			case OGGETTO_SPECIALE_INGREDIENTE:
				return generatore.generaIngrediente(livello);
			case OGGETTO_SPECIALE_ARTEFATTO:
				return generatore.generaArtefattoCasuale(livello);
			case OGGETTO_SPECIALE_AGGIORNAMENTO:
				return generatore.generaAggiornamento(livello);
			case OGGETTO_SPECIALE_CASUALE:
				return artefattoRaro(Dado.probabilita(), generatore, livello);
			default:
				if (voluto.startsWith(PREFISSO_LIBRO_TECNICO)) {
					return LibroTecnico.getLibro(voluto.substring(PREFISSO_LIBRO_TECNICO.length()))
							.map(libro -> libro.costruisci(generatore, livello))
							.orElseGet(() -> artefattoRaro(Dado.probabilita(), generatore, livello));
				}
				return CatalogoLeggendari.getLeggendario(voluto.substring(PREFISSO_LEGGENDARIO.length()))
						.map(OggettoLeggendario::costruisci)
						.orElseGet(() -> artefattoRaro(Dado.probabilita(), generatore, livello));
		}
	}
}
