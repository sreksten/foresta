package com.threeamigos.foresta.tipi;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Le classi dei personaggi, giocanti e mostri: l'identificativo che salva il modello dati (PersonaggioMD,
 * StatisticheMD...) e che leggono motore e UI. Il personaggio vero, i suoi nomi e quanti se ne incontrano al massimo
 * in una locazione li dà personaggi.FabbricaPersonaggi. Ogni classe ha le sue categorie di creatura (CategoriaPersonaggio).
 */
public enum TipoPersonaggio {

	ARPIA(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.IBRIDO, CategoriaPersonaggio.VOLANTE),
	CENTAURO(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.IBRIDO),
	CHIMERA(CategoriaPersonaggio.BESTIA, CategoriaPersonaggio.IBRIDO),
	CHIMERA_DRAGO(CategoriaPersonaggio.BESTIA, CategoriaPersonaggio.IBRIDO, CategoriaPersonaggio.DRACONICO),
	DRAGO(CategoriaPersonaggio.BESTIA, CategoriaPersonaggio.DRACONICO, CategoriaPersonaggio.VOLANTE, CategoriaPersonaggio.GIGANTESCO),
	EREMITA(CategoriaPersonaggio.UMANOIDE),
	FANTASMA(CategoriaPersonaggio.NON_MORTO, CategoriaPersonaggio.VOLANTE),
	FOLLETTO(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.VOLANTE),
	GARGOYLE(CategoriaPersonaggio.COSTRUTTO, CategoriaPersonaggio.VOLANTE),
	GIGANTE(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.GIGANTESCO),
	GOBLIN(CategoriaPersonaggio.UMANOIDE),
	HOBGOBLIN(CategoriaPersonaggio.UMANOIDE),
	IDRA(CategoriaPersonaggio.BESTIA, CategoriaPersonaggio.GIGANTESCO),
	LICH(CategoriaPersonaggio.NON_MORTO),
	MINOTAURO(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.IBRIDO),
	MINOTAURO_GIGANTE(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.IBRIDO, CategoriaPersonaggio.GIGANTESCO),
	OMBRA_NERA(CategoriaPersonaggio.NON_MORTO, CategoriaPersonaggio.VOLANTE),
	SCHELETRO(CategoriaPersonaggio.NON_MORTO),
	SPETTRO(CategoriaPersonaggio.NON_MORTO, CategoriaPersonaggio.VOLANTE),
	SPIRITO(CategoriaPersonaggio.NON_MORTO, CategoriaPersonaggio.VOLANTE),
	STREGA(CategoriaPersonaggio.UMANOIDE),
	TITANO(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.GIGANTESCO),
	TROLL(CategoriaPersonaggio.UMANOIDE, CategoriaPersonaggio.GIGANTESCO),
	VIVERNA(CategoriaPersonaggio.BESTIA, CategoriaPersonaggio.DRACONICO, CategoriaPersonaggio.VOLANTE),

	BARDO(CategoriaPersonaggio.UMANOIDE),
	CANTASTORIE(CategoriaPersonaggio.UMANOIDE),
	ELFA(CategoriaPersonaggio.UMANOIDE),
	ELFO(CategoriaPersonaggio.UMANOIDE),
	GUERRIERA(CategoriaPersonaggio.UMANOIDE),
	GUERRIERO(CategoriaPersonaggio.UMANOIDE),
	LADRA(CategoriaPersonaggio.UMANOIDE),
	LADRO(CategoriaPersonaggio.UMANOIDE),
	MAGA(CategoriaPersonaggio.UMANOIDE),
	MAGO(CategoriaPersonaggio.UMANOIDE),
	OMBRAFIAMMA(CategoriaPersonaggio.UMANOIDE),
	SACERDOTE(CategoriaPersonaggio.UMANOIDE),
	SACERDOTESSA(CategoriaPersonaggio.UMANOIDE),

	// Non combattenti: compaiono nelle scene e come ospiti delle missioni di scorta; non si incontrano e non si reclutano
	LOCANDIERE(CategoriaPersonaggio.UMANOIDE),
	ARMAIOLO(CategoriaPersonaggio.UMANOIDE),
	ALCHIMISTA(CategoriaPersonaggio.UMANOIDE),
	VENDITORE_DI_PERGAMENE(CategoriaPersonaggio.UMANOIDE),
	INCANTATORE(CategoriaPersonaggio.UMANOIDE),
	MOGLIE_DEL_BARDO(CategoriaPersonaggio.UMANOIDE),
	BARDO_LOCANDA(CategoriaPersonaggio.UMANOIDE),
	CAPITANO_DELLE_GUARDIE(CategoriaPersonaggio.UMANOIDE),
	// Gli abitanti senza una classe propria (il borgomastro, la levatrice, i mandanti qualunque): hanno l'aspetto del locandiere e della moglie del bardo
	UOMO(CategoriaPersonaggio.UMANOIDE),
	DONNA(CategoriaPersonaggio.UMANOIDE),
	// Solo per le missioni di scorta: non si incontra e non si recluta
	VIANDANTE(CategoriaPersonaggio.UMANOIDE);

	private final Set<CategoriaPersonaggio> categorie;

	TipoPersonaggio(CategoriaPersonaggio... categorie) {
		this.categorie = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(categorie)));
	}

	/**
	 * Le categorie della classe: almeno una, più d'una per chi è di due generi (lo Spirito è NON_MORTO e VOLANTE)
	 */
	public Set<CategoriaPersonaggio> getCategorie() {
		return categorie;
	}

	public boolean isDellaCategoria(CategoriaPersonaggio categoria) {
		return categorie.contains(categoria);
	}
}
