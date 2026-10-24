package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoOggetto;

import java.util.Objects;
import java.util.Optional;

/**
 * Un artefatto già fatto che il gruppo trova in una locazione, come quello custodito in un tempio: il bottino lo avvolge, ma
 * l'artefatto resta il pezzo di equipaggiamento vero, che si conserva e si salva con il suo {@code ArtefattoMD}. Si trova uno
 * alla volta, e prenderlo non assegna punti (vedi {@link RaccoltaBottino}).
 * <p>
 * Diverso da {@link OggettoArtefatto}, che è un bottino di un certo tipo (una spada, uno scudo) il cui artefatto nasce
 * insieme a lui.
 *
 * @author Stefano Reksten
 */
public class BottinoArtefatto implements Bottino {

	private final Artefatto artefatto;

	public BottinoArtefatto(Artefatto artefatto) {
		this.artefatto = Objects.requireNonNull(artefatto);
	}

	@Override
	public Optional<Artefatto> getArtefatto() {
		return Optional.of(artefatto);
	}

	@Override
	public int getQuantita() {
		return 1;
	}

	@Override
	public String getNomePlurale() {
		return "artefatti";
	}

	@Override
	public String getNomeSingolare() {
		return "artefatto";
	}

	@Override
	public String getAIS() {
		return Articoli.UN;
	}

	@Override
	public String getAIP() {
		return Articoli.ALCUNI;
	}

	@Override
	public String getADS() {
		return Articoli.L_APOSTROFO;
	}

	@Override
	public String getADP() {
		return Articoli.GLI;
	}

	@Override
	public TipoOggetto getClasse() {
		return TipoOggetto.ARTEFATTO;
	}
}
