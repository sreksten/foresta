package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.tipi.AspettoArtefatto;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;

import java.util.Collection;
import java.util.Optional;

/**
 * Un artefatto visto dalla UI, in sola lettura (vedi VistaPartita). Lo implementa Artefatto: chi lo cambia è il
 * motore, a cui la UI lo chiede con i comandi (ComandoScambioArtefatto, ComandoCommutazioneElenco).
 */
public interface VistaArtefatto {

	/**
	 * L'identificativo dell'artefatto, lo stesso anche quando il motore lo ricrea dal suo modello dati
	 */
	String getUuid();

	/**
	 * Il tipo di artefatto.
	 */
	TipoArtefatto getTipo();

	/**
	 * La rarità.
	 */
	TipoRaritaArtefatto getRarita();

	/**
	 * Il nome, con l'articolo.
	 */
	String getNome();

	/**
	 * Il nome breve, senza il soprannome.
	 */
	String getNomeBreve();

	/**
	 * La descrizione, "che ...".
	 */
	String getDescrizione();

	/**
	 * La descrizione breve, con il nome se c'è un soprannome.
	 */
	String getDescrizioneBreve();

	/**
	 * Il soprannome, se ce l'ha.
	 */
	Optional<String> getNomeProprio();

	/**
	 * Forma completa da usare nei testi, es. "Diavolina, la spada di fuoco, che brucia i nemici"
	 */
	String getNomeCompleto();

	/**
	 * Il livello.
	 */
	int getLivello();

	/**
	 * I danni base (zero se non è un'arma).
	 */
	int getDanni();

	/**
	 * Il prezzo d'acquisto.
	 */
	int getCostoAcquisto();

	/**
	 * Il peso.
	 */
	double getPeso();

	/**
	 * Se si può incantare.
	 */
	boolean isIncantabile();

	/**
	 * Numero massimo di effetti in totale (incantamenti più modificatori di attributo)
	 */
	int getEffettiMassimi();

	/**
	 * Numero di effetti che ha (incantamenti più modificatori di attributo)
	 */
	default int getNumeroEffetti() {
		return getIncantamenti().size() + getModificatori().size();
	}

	/**
	 * I modificatori permanenti di attributo.
	 */
	Collection<ModificatoreAttributoMD> getModificatori();

	/**
	 * Gli incantamenti.
	 */
	Collection<IncantamentoMD> getIncantamenti();

	/**
	 * Se nelle schermate l'elenco dei modificatori è aperto (si cambia con ComandoCommutazioneElenco)
	 */
	boolean isFigliVisibili();

	/**
	 * La chiave del pezzo di set leggendario, o null se non è un pezzo di un set
	 */
	String getPezzoLeggendario();

	/**
	 * L'aspetto speciale dell'artefatto nelle schermate, se non è quello del suo tipo (i libri tecnici hanno
	 * {@code AspettoArtefatto.LIBRO_TECNICO}); null se è quello del tipo
	 */
	AspettoArtefatto getAspetto();

	/**
	 * Per i testi, il set di cui l'artefatto è un pezzo: "Pezzo del Corredo di RomyJona (bonus x1,5)"; vuoto se non è
	 * un pezzo di un set
	 */
	Optional<String> getDescrizioneSet();

	/**
	 * Per i testi, i tipi dei pezzi del set dell'artefatto: "Spada, Elmo, Maschera, Schinieri"; vuoto se non è un
	 * pezzo di un set
	 */
	Optional<String> getTipiDelSet();

}
