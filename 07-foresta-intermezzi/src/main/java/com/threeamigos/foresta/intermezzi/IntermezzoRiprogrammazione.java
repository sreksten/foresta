package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.NegozioInScena;
import com.threeamigos.foresta.artefatti.RegoleIncantatura;

import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.TipoIntermezzo;

import java.util.List;
import com.threeamigos.foresta.testi.Testi;

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
				.parlaIlCapo(Testi.formatta("intermezzi", "IntermezzoRiprogrammazione.1"))
				.parlaIlNegoziante(Testi.formatta("intermezzi", "IntermezzoRiprogrammazione.2"))
				.parlaIlNegoziante(Testi.formatta("intermezzi", "IntermezzoRiprogrammazione.3"))
				.parlaIlCapo(Testi.formatta("intermezzi", "IntermezzoRiprogrammazione.4"))
				.parlaIlNegoziante(Testi.formatta("intermezzi", "IntermezzoRiprogrammazione.5"))
				.parlaIlCapo(Testi.formatta("intermezzi", "IntermezzoRiprogrammazione.6"))
				.parlaIlNegoziante(saluto)
				.getPagine();
	}
}
