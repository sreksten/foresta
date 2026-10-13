package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;

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

	TipoArtefatto getTipo();

	TipoRaritaArtefatto getRarita();

	String getNome();

	String getNomeBreve();

	String getDescrizione();

	String getDescrizioneBreve();

	Optional<String> getNomeProprio();

	/**
	 * Forma completa da usare nei testi, es. "Diavolina, la spada di fuoco, che brucia i nemici"
	 */
	String getNomeCompleto();

	int getLivello();

	int getDanni();

	int getCostoAcquisto();

	double getPeso();

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

	Collection<ModificatoreAttributoMD> getModificatori();

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
