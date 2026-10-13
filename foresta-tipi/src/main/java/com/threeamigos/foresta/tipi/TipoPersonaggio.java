package com.threeamigos.foresta.tipi;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
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
	// Quanto danno subisce, per tipo di danno, questa classe in più di quanto dicono le sue categorie: i tipi che mancano valgono 1
	private final Map<TipoDanno, Double> dannoSubito = new EnumMap<>(TipoDanno.class);

	// Le debolezze e le resistenze della classe (quelle comuni a una categoria stanno in CategoriaPersonaggio)
	// Tabella provvisoria, con le sue ragioni in debolezze_e_resistenze.md
	static {
		// Gli spiriti incorporei: aria e terra li attraversano, i colpi fisici fanno la metà, sono creature del gelo e
		// l'energia necrotica non li ferisce (con la categoria NON_MORTO, che li rende deboli al sacro e immuni al veleno)
		for (TipoPersonaggio fantasma : EnumSet.of(FANTASMA, OMBRA_NERA, SPETTRO, SPIRITO)) {
			fantasma.moltiplicaIlDannoSubito(TipoDanno.ARIA, 0.0d);
			fantasma.moltiplicaIlDannoSubito(TipoDanno.TERRA, 0.0d);
			fantasma.moltiplicaIlDannoSubito(TipoDanno.NECROTICO, 0.0d);
			fantasma.moltiplicaIlDannoSubito(TipoDanno.GELO, 0.5d);
			fantasma.moltiplicaIlDannoSubito(TipoDanno.CONTUNDENTE, 0.5d);
			fantasma.moltiplicaIlDannoSubito(TipoDanno.PERFORANTE, 0.5d);
			fantasma.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 0.5d);
		}
		// Lo scheletro: le ossa si spezzano a bastonate, ma lame e punte trovano poco da tagliare
		SCHELETRO.moltiplicaIlDannoSubito(TipoDanno.CONTUNDENTE, 1.5d);
		SCHELETRO.moltiplicaIlDannoSubito(TipoDanno.PERFORANTE, 0.5d);
		SCHELETRO.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 0.75d);
		// Il lich è un negromante che si è fatto di ghiaccio e di tempesta
		LICH.moltiplicaIlDannoSubito(TipoDanno.GELO, 0.5d);
		LICH.moltiplicaIlDannoSubito(TipoDanno.FULMINE, 0.5d);
		// I draghi: il fuoco è casa loro, il gelo li ferisce
		DRAGO.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 0.0d);
		DRAGO.moltiplicaIlDannoSubito(TipoDanno.GELO, 1.5d);
		CHIMERA_DRAGO.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 0.0d);
		CHIMERA_DRAGO.moltiplicaIlDannoSubito(TipoDanno.GELO, 1.5d);
		CHIMERA_DRAGO.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.5d);
		CHIMERA.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 0.5d);
		CHIMERA.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.5d);
		// La viverna è velenosa
		VIVERNA.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.0d);
		// L'idra: palude e veleno; il fuoco e l'acido impediscono alle teste di ricrescere
		IDRA.moltiplicaIlDannoSubito(TipoDanno.TERRA, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.ACQUA, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.ARIA, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 1.5d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.ACIDO, 1.5d);
		// Il troll si rigenera, ma non dal fuoco e dall'acido
		TROLL.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 1.5d);
		TROLL.moltiplicaIlDannoSubito(TipoDanno.ACIDO, 1.5d);
		// Il gargoyle è di pietra: subisce solo il danno fisico, e la pietra si spacca a martellate
		for (TipoDanno tipoDanno : TipoDanno.values()) {
			if (tipoDanno.getSuperTipo() != SupertipoDanno.FISICO) {
				GARGOYLE.moltiplicaIlDannoSubito(tipoDanno, 0.0d);
			}
		}
		GARGOYLE.moltiplicaIlDannoSubito(TipoDanno.CONTUNDENTE, 1.5d);
		GARGOYLE.moltiplicaIlDannoSubito(TipoDanno.PERFORANTE, 0.75d);
		GARGOYLE.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 0.75d);
		// La strega: al rogo, e le maledizioni sono il suo mestiere
		STREGA.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 1.5d);
		STREGA.moltiplicaIlDannoSubito(TipoDanno.MALEDIZIONE, 0.5d);
		// Il folletto teme il ferro: lame e punte lo feriscono di più
		FOLLETTO.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 1.25d);
		FOLLETTO.moltiplicaIlDannoSubito(TipoDanno.PERFORANTE, 1.25d);
	}

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

	private void moltiplicaIlDannoSubito(TipoDanno tipoDanno, double moltiplicatore) {
		dannoSubito.put(tipoDanno, moltiplicatore);
	}

	/**
	 * Per quanto si moltiplica il danno di quel tipo contro questa classe: il prodotto del valore della classe e di quelli delle
	 * sue categorie. 1 se non cambia, 0 se è immune, tra 0 e 1 se resiste, più di 1 se è debole.
	 */
	public double getMoltiplicatoreDannoSubito(TipoDanno tipoDanno) {
		double moltiplicatore = dannoSubito.getOrDefault(tipoDanno, 1.0d);
		for (CategoriaPersonaggio categoria : categorie) {
			moltiplicatore = moltiplicatore * categoria.getMoltiplicatoreDannoSubito(tipoDanno);
		}
		return moltiplicatore;
	}

	/**
	 * Il ruolo della classe, a meno del genere: Mago e Maga, Elfo e Elfa, Ladro e Ladra, Guerriero e Guerriera, Bardo e Cantastorie,
	 * Sacerdote e Sacerdotessa sono lo stesso ruolo (le tattiche non devono distinguerli); ogni altra classe ha il suo.
	 */
	public TipoPersonaggio getRuolo() {
		switch (this) {
			case MAGA:
				return MAGO;
			case ELFA:
				return ELFO;
			case LADRA:
				return LADRO;
			case GUERRIERA:
				return GUERRIERO;
			case CANTASTORIE:
				return BARDO;
			case SACERDOTESSA:
				return SACERDOTE;
			default:
				return this;
		}
	}

	public boolean stessoRuoloDi(TipoPersonaggio altra) {
		return getRuolo() == altra.getRuolo();
	}
}
