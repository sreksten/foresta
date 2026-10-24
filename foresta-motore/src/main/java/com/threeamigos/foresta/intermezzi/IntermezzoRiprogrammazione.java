package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.List;

/**
 * La prima volta che il gruppo entra dall'incantatore dopo aver trovato in un cofano The C Programming Language (vedi
 * LibroTecnico): il capo lo mostra, "roba da maghi del computer, anche se non so cosa sia un computer", e l'incantatore spiega
 * che serve a riprogrammare gli artefatti ma sono incantesimi arcani, e che un errore nell'esecuzione finisce in un Core
 * Dump (vedi RegoleIncantatura.riprogramma). Poi dà il benvenuto: per questo il benvenuto normale dell'incantatore, quella volta,
 * si considera già dato.
 * <p>
 * Scatta una sola volta per partita, e sta prima del benvenuto nell'ordine di TipoIntermezzo.
 */
public class IntermezzoRiprogrammazione implements Intermezzo {

	private static final String CHIAVE_DEL_LIBRO = "THE_C_PROGRAMMING_LANGUAGE";

	private final TipoIntermezzo tipo;
	private final String saluto;

	IntermezzoRiprogrammazione(TipoIntermezzo tipo, String saluto) {
		this.tipo = tipo;
		this.saluto = saluto;
	}

	@Override
	public String getId() {
		return tipo.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return NegozioInScena.INCANTATORE.isIngresso(momento) && LibroTecnico.getLibro(CHIAVE_DEL_LIBRO)
				.filter(IntermezzoLibroTecnico::isTrovato).isPresent();
	}

	/**
	 * Le pagine; il benvenuto normale si segna come già dato qui, perché l'automa le chiede una volta sola, subito dopo aver
	 * segnato questo intermezzo come scattato.
	 */
	@Override
	public List<PaginaIntermezzo> getPagine() {
		RegistroIntermezzi.segnaScattato(FabbricaIntermezzi.crea(TipoIntermezzo.INTERMEZZO_INCANTATORE));
		return NegozioInScena.INCANTATORE.entraIlGruppo()
				.parlaIlCapo("Abbiamo trovato un libro, The C Programming Language. Deve essere roba da maghi del computer, anche se non so cosa sia un computer.")
				.parlaIlNegoziante("Maghi del computer, sì. Non chiedetemi cosa sia nemmeno a me, ma quel libro lo conosco.")
				.parlaIlNegoziante("Contiene incantesimi per riprogrammare gli artefatti. Sono incantesimi molto arcani, e bisogna fare molta attenzione.")
				.parlaIlCapo("Che cosa può succedere?")
				.parlaIlNegoziante("Un errore nell'esecuzione, e l'artefatto si disfa nelle sue componenti. Si chiama Core Dump.")
				.parlaIlCapo("Capito. Staremo attenti.")
				.parlaIlNegoziante(saluto)
				.getPagine();
	}
}
