package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.artefatti.leggendari.RegoleSetLeggendari;
import com.threeamigos.foresta.artefatti.tipi.AspettoArtefatto;
import com.threeamigos.foresta.artefatti.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.artefatti.tipi.TipoArtefatto;
import com.threeamigos.foresta.artefatti.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;

import java.util.Collection;
import java.util.Optional;

/**
 * Un artefatto: un pezzo di equipaggiamento vero (arma, scudo, elmo, armatura, anello, ingrediente magico...), con un nome, un
 * livello, un costo, un peso e, se ce ne sono, modificatori di attributo e incantamenti.
 * <p>
 * È un involucro sul suo {@link ArtefattoMD}, che ne tiene i dati e si salva; qui stanno le regole: cosa succede quando lo si
 * raccoglie, a chi può andare e come lo si dà a un personaggio lo decide {@code RaccoltaBottino}, nel motore.
 * Lo si ricrea dal modello dati con {@link #di(ArtefattoMD)}, che sceglie la sottoclasse giusta.
 */
public class Artefatto implements OggettoConCosto, OggettoConPeso, VistaArtefatto {

	/**
	 * I dati dell'artefatto, che si salvano con la partita.
	 */
	protected final ArtefattoMD md;

	/**
	 * Un artefatto sui dati dati.
	 */
	public Artefatto(ArtefattoMD artefattoMD) {
		this.md = artefattoMD;
	}

	/**
	 * Ricostruisce la sottoclasse corretta (es. ArmaFisica) in base al supertipo,
	 * così da non perdere il tipo concreto quando un Artefatto viene rimaterializzato dal suo ArtefattoMD.
	 */
	public static Artefatto di(ArtefattoMD md) {
		if (SpadaNormalmenteNascosta.isQuesta(md)) {
			return new SpadaNormalmenteNascosta(md);
		}
		if (md.getTipo().getCategoria() == CategoriaArtefatto.ARMA) {
			return new ArmaFisica(md);
		}
		return new Artefatto(md);
	}

	/**
	 * L'artefatto di una vista che la UI passa come argomento di una domanda (per esempio se un personaggio può
	 * equipaggiarlo): le viste degli artefatti sono artefatti. I comandi della UI non portano viste ma uuid, e il
	 * motore ritrova l'artefatto da sé.
	 */
	public static Artefatto da(VistaArtefatto vista) {
		return (Artefatto) vista;
	}

	@Override
	public final String getUuid() {
		return md.getUuid();
	}

	@Override
	public final TipoArtefatto getTipo() {
		return md.getTipo();
	}

	public final String getNome() {
		return md.getNome();
	}

	public final String getDescrizione() {
		return md.getDescrizione();
	}

	@Override
	public final String getNomeBreve() {
		return md.getNomeBreve();
	}

	@Override
	public final String getDescrizioneBreve() {
		return md.getDescrizioneBreve();
	}

	@Override
	public int getDanni() {
		return md.getDanni();
	}

	@Override
	public final String getPezzoLeggendario() {
		return md.getPezzoLeggendario();
	}

	@Override
	public final AspettoArtefatto getAspetto() {
		return md.getAspetto();
	}

	@Override
	public final Optional<String> getDescrizioneSet() {
		return RegoleSetLeggendari.descrizioneSet(md);
	}

	@Override
	public final Optional<String> getTipiDelSet() {
		return RegoleSetLeggendari.tipiDelSet(md);
	}

	public final Optional<String> getNomeProprio() {
		return Optional.ofNullable(md.getNomeProprio());
	}

	/**
	 * Forma completa da usare nei testi, es. "Diavolina, la spada di fuoco, che brucia i nemici".
	 */
	public final String getNomeCompleto() {
		return md.getNomeCompleto();
	}

	/**
	 * Armi, scudi, elmi, maschere, armature e schinieri, e il libro magico. Accessori e pergamene non si incantano.
	 */
	public boolean isIncantabile() {
		switch (getTipo().getCategoria()) {
			case ARMA:
			case SCUDO:
			case ELMO:
			case ARMATURA:
			case SCHINIERI:
			case POTENZIAMENTO_POTERE_MAGICO:
				return true;
			default:
				return false;
		}
	}

	public final TipoRaritaArtefatto getRarita() {
		return md.getRarita();
	}

	/**
	 * Numero massimo di effetti in totale (incantamenti più modificatori di attributo), secondo la rarità:
	 * livello − 1 per un comune (quindi nessuno a livello 1), livello per un raro, livello + 1 per un
	 * leggendario, fino a un tetto di 3, 4 o 5.
	 */
	public int getEffettiMassimi() {
		if (!isIncantabile()) {
			return 0;
		}
		return getRarita().getEffettiMassimi(getLivello());
	}

	public int getLivello() {
		return md.getLivello();
	}

	public final int getCostoAcquisto() {
		return md.getCostoAcquisto();
	}

	public double getPeso() {
		return md.getPeso();
	}

	/**
	 * Modificatori permanenti agli attributi dei personaggi
	 */
	public Collection<ModificatoreAttributoMD> getModificatori() {
		return md.getModificatori();
	}

	/**
	 * Incantamenti fatti sull'artefatto
	 */
	public Collection<IncantamentoMD> getIncantamenti() {
		return md.getIncantamenti();
	}

	public boolean isFigliVisibili() {
		return md.isFigliVisibili();
	}

	/**
	 * Apre l'elenco dei modificatori e degli incantamenti nell'inventario.
	 */
	public void mostraFigli() {
		md.setFigliVisibili(true);
	}

	/**
	 * Chiude l'elenco dei modificatori e degli incantamenti nell'inventario.
	 */
	public void nascondiFigli() {
		md.setFigliVisibili(false);
	}

	/**
	 * I dati dell'artefatto, per salvarlo o modificarlo.
	 */
	public ArtefattoMD getModelloDati() {
		return md;
	}

}
