package com.threeamigos.foresta.tipi;

import java.util.*;

/**
 * Le classi dei personaggi, giocanti e mostri: l'identificativo che salva il modello dati (PersonaggioMD,
 * StatisticheMD...) e che leggono motore e UI. Il personaggio vero, i suoi nomi e quanti se ne incontrano al massimo
 * in una locazione li dà personaggi.FabbricaPersonaggi. Ogni classe ha le sue categorie di creatura (CategoriaPersonaggio).
 *
 * @author Stefano Reksten
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
	CHIERICA(CategoriaPersonaggio.UMANOIDE),
	CHIERICO(CategoriaPersonaggio.UMANOIDE),
	DRUIDA(CategoriaPersonaggio.UMANOIDE),
	DRUIDO(CategoriaPersonaggio.UMANOIDE),

	// Non combattenti: compaiono nelle scene e come ospiti delle missioni di scorta; non si incontrano e non si reclutano
	LOCANDIERE(CategoriaPersonaggio.UMANOIDE),
	ARMAIOLO(CategoriaPersonaggio.UMANOIDE),
	ALCHIMISTA(CategoriaPersonaggio.UMANOIDE),
	VENDITORE_DI_PERGAMENE(CategoriaPersonaggio.UMANOIDE),
	INCANTATORE(CategoriaPersonaggio.UMANOIDE),
	MOGLIE_DEL_BARDO(CategoriaPersonaggio.UMANOIDE),
	BARDO_DELLE_LOCANDE(CategoriaPersonaggio.UMANOIDE),
	CAPITANO_DELLE_GUARDIE(CategoriaPersonaggio.UMANOIDE),
	// Gli abitanti senza una classe propria (la levatrice, i mandanti qualunque): hanno l'aspetto del locandiere e della moglie del bardo
	UOMO(CategoriaPersonaggio.UMANOIDE),
	// Ha un'immagine sua, ma per il resto è un Uomo
	BORGOMASTRO(CategoriaPersonaggio.UMANOIDE),
	DONNA(CategoriaPersonaggio.UMANOIDE),
	// Solo per le missioni di scorta: non si incontra e non si recluta
	VIANDANTE(CategoriaPersonaggio.UMANOIDE);

	// Il ruolo di chi lo condivide con un'altra classe (quella al maschile). Se non c'è, si assume il proprio ruolo
	private static final Map<TipoPersonaggio, TipoPersonaggio> RUOLI = new EnumMap<>(TipoPersonaggio.class);

	// Chi compare nel bestiario (vedi todo.md e bestiario.md): chi si incontra come avversario e chi può entrare nel gruppo. I mostri sono il primo
	// blocco dell'enum; fra i giocanti, quelli che le missioni mettono fra i nemici (NEMICO=, ONDATA_n=, SFIDANTI=, CAMPIONE= di missioni.txt).
	private static final Set<TipoPersonaggio> AVVERSARI = EnumSet.range(ARPIA, VIVERNA);
	static {
		AVVERSARI.addAll(EnumSet.of(GUERRIERO, GUERRIERA, LADRO, MAGO, MAGA, ELFO, ELFA));
	}
	// Chi si sceglie a inizio partita o si recluta in locanda (l'OmbraFiamma per ora sta fuori)
	private static final Set<TipoPersonaggio> COMPAGNI = EnumSet.of(BARDO, CANTASTORIE, ELFA, ELFO, GUERRIERA, GUERRIERO, LADRA, LADRO,
			MAGA, MAGO, SACERDOTE, SACERDOTESSA, CHIERICA, CHIERICO, DRUIDA, DRUIDO);

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
			// Non si possono immobilizzare, e aria e terra li attraversano anche sotto forma di incantesimo
			fantasma.immuneAIncantesimi.addAll(EnumSet.of(TipoIncantesimo.ARIA, TipoIncantesimo.TERRA));
			fantasma.immuneAEffetti.add(TipoEffettoDiStato.ATTERRATO);
		}
		// Lo scheletro: le ossa si spezzano a bastonate, ma lame e punte trovano poco da tagliare
		SCHELETRO.moltiplicaIlDannoSubito(TipoDanno.CONTUNDENTE, 1.5d);
		SCHELETRO.moltiplicaIlDannoSubito(TipoDanno.PERFORANTE, 0.5d);
		SCHELETRO.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 0.75d);
		SCHELETRO.immuneAEffetti.addAll(EnumSet.of(TipoEffettoDiStato.AVVELENATO, TipoEffettoDiStato.SPAVENTATO));
		// Il lich è un necromante che si è fatto di ghiaccio e di tempesta
		LICH.moltiplicaIlDannoSubito(TipoDanno.GELO, 0.5d);
		LICH.moltiplicaIlDannoSubito(TipoDanno.FULMINE, 0.5d);
		LICH.immuneAEffetti.addAll(EnumSet.of(TipoEffettoDiStato.AVVELENATO, TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.SILENZIATO));
		// I draghi: il fuoco è casa loro, il gelo li ferisce
		DRAGO.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 0.0d);
		DRAGO.moltiplicaIlDannoSubito(TipoDanno.GELO, 1.5d);
		DRAGO.immuneAIncantesimi.add(TipoIncantesimo.FUOCO);
		DRAGO.immuneAEffetti.add(TipoEffettoDiStato.BRUCIATO);
		CHIMERA_DRAGO.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 0.0d);
		CHIMERA_DRAGO.moltiplicaIlDannoSubito(TipoDanno.GELO, 1.5d);
		CHIMERA_DRAGO.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.5d);
		CHIMERA_DRAGO.immuneAIncantesimi.add(TipoIncantesimo.FUOCO);
		CHIMERA_DRAGO.immuneAEffetti.add(TipoEffettoDiStato.BRUCIATO);
		CHIMERA.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 0.5d);
		CHIMERA.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.5d);
		// La viverna è velenosa
		VIVERNA.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.0d);
		VIVERNA.immuneAIncantesimi.add(TipoIncantesimo.FUOCO);
		// L'idra: palude e veleno; il fuoco e l'acido impediscono alle teste di ricrescere
		IDRA.moltiplicaIlDannoSubito(TipoDanno.TERRA, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.ACQUA, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.ARIA, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.0d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 1.5d);
		IDRA.moltiplicaIlDannoSubito(TipoDanno.ACIDO, 1.5d);
		IDRA.immuneAIncantesimi.addAll(EnumSet.of(TipoIncantesimo.TERRA, TipoIncantesimo.ACQUA, TipoIncantesimo.ARIA));
		IDRA.immuneAEffetti.addAll(EnumSet.of(TipoEffettoDiStato.CONFUSO, TipoEffettoDiStato.STORDITO));
		// Il troll si rigenera, ma non dal fuoco e dall'acido
		TROLL.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 1.5d);
		TROLL.moltiplicaIlDannoSubito(TipoDanno.ACIDO, 1.5d);
		TROLL.immuneAIncantesimi.add(TipoIncantesimo.TERRA);
		// Il gargoyle è di pietra: subisce solo il danno fisico, e la pietra si spacca a martellate
		for (TipoDanno tipoDanno : TipoDanno.values()) {
			if (tipoDanno.getCategoriaDanno() != CategoriaDanno.FISICO) {
				GARGOYLE.moltiplicaIlDannoSubito(tipoDanno, 0.0d);
			}
		}
		GARGOYLE.moltiplicaIlDannoSubito(TipoDanno.CONTUNDENTE, 1.5d);
		GARGOYLE.moltiplicaIlDannoSubito(TipoDanno.PERFORANTE, 0.75d);
		GARGOYLE.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 0.75d);
		// Nessun incantesimo lo tocca, e la pietra non sanguina, non si infetta, non si avvelena e non si immobilizza
		GARGOYLE.immuneATuttiGliIncantesimi = true;
		GARGOYLE.immuneAEffetti.addAll(EnumSet.of(TipoEffettoDiStato.SANGUINAMENTO, TipoEffettoDiStato.INFETTATO,
				TipoEffettoDiStato.AVVELENATO, TipoEffettoDiStato.IMMOBILIZZATO));
		// La strega: al rogo, e le maledizioni sono il suo mestiere
		STREGA.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 1.5d);
		STREGA.moltiplicaIlDannoSubito(TipoDanno.MALEDIZIONE, 0.5d);
		STREGA.immuneAIncantesimi.addAll(EnumSet.of(TipoIncantesimo.ACQUA, TipoIncantesimo.ARIA, TipoIncantesimo.FUOCO, TipoIncantesimo.TERRA));
		// Il folletto teme il ferro: lame e punte lo feriscono di più
		FOLLETTO.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 1.25d);
		FOLLETTO.moltiplicaIlDannoSubito(TipoDanno.PERFORANTE, 1.25d);
		// L'arpia vive nell'aria
		ARPIA.immuneAIncantesimi.add(TipoIncantesimo.ARIA);

		// Il chierico serve il sacro e ne è protetto: il necrotico e le maledizioni lo feriscono meno
		for (TipoPersonaggio chierico : EnumSet.of(CHIERICO, CHIERICA)) {
			chierico.moltiplicaIlDannoSubito(TipoDanno.NECROTICO, 0.75d);
			chierico.moltiplicaIlDannoSubito(TipoDanno.MALEDIZIONE, 0.75d);
		}
		// Il druido vive di terra, acqua e veleni, ma soffre il fuoco e le lame
		for (TipoPersonaggio druido : EnumSet.of(DRUIDO, DRUIDA)) {
			druido.moltiplicaIlDannoSubito(TipoDanno.ACQUA, 0.75d);
			druido.moltiplicaIlDannoSubito(TipoDanno.TERRA, 0.75d);
			druido.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.75d);
			druido.moltiplicaIlDannoSubito(TipoDanno.FUOCO, 1.25d);
			druido.moltiplicaIlDannoSubito(TipoDanno.TAGLIENTE, 1.1d);
		}

		// Assegnazione dell'equivalenza dei ruoli
		RUOLI.put(MAGA, MAGO);
		RUOLI.put(ELFA, ELFO);
		RUOLI.put(LADRA, LADRO);
		RUOLI.put(GUERRIERA, GUERRIERO);
		RUOLI.put(CANTASTORIE, BARDO);
		RUOLI.put(SACERDOTESSA, SACERDOTE);
		RUOLI.put(CHIERICA, CHIERICO);
		RUOLI.put(DRUIDA, DRUIDO);
	}

	// Le categorie della classe: almeno una, più d'una per chi è di due generi (lo Spirito è NON_MORTO e VOLANTE)
	private final Set<CategoriaPersonaggio> categorie;

	// Quanto danno subisce, per tipo di danno, questa classe in più di quanto dicono le sue categorie: i tipi che
	// mancano valgono 1. La mappa è modificabile ma solo da qui: la riempie il blocco static tramite un metodo privato,
	// e da fuori nessuno può cambiarla.
	private final Map<TipoDanno, Double> dannoSubito = new EnumMap<>(TipoDanno.class);

	// Gli incantesimi e gli effetti di stato a cui la classe è immune in più di quanto dicono le sue categorie (CategoriaPersonaggio); come
	// per i danni, li riempie solo il blocco static. Il gargoyle è immune a tutti gli incantesimi.
	private final Set<TipoIncantesimo> immuneAIncantesimi = EnumSet.noneOf(TipoIncantesimo.class);
	private boolean immuneATuttiGliIncantesimi;
	private final Set<TipoEffettoDiStato> immuneAEffetti = EnumSet.noneOf(TipoEffettoDiStato.class);

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
	 * Se la classe, come tale, è immune a quell'incantesimo: lo dicono la classe e le sue categorie (i non morti lo sono a Morte e Veleno).
	 * Le immunità di un singolo personaggio, che non sono della classe, le aggiunge il personaggio (vedi PersonaggioBase.isImmuneAIncantesimo).
	 */
	public boolean isImmuneAIncantesimo(TipoIncantesimo tipoIncantesimo) {
		if (immuneATuttiGliIncantesimi || immuneAIncantesimi.contains(tipoIncantesimo)) {
			return true;
		}
		for (CategoriaPersonaggio categoria : categorie) {
			if (categoria.isImmuneAIncantesimo(tipoIncantesimo)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Se la classe, come tale, è immune a tutti gli incantesimi (il gargoyle)
	 */
	public boolean isImmuneATuttiGliIncantesimi() {
		return immuneATuttiGliIncantesimi;
	}

	/**
	 * Se la classe, come tale, è immune a quell'effetto di stato: lo dicono la classe e le sue categorie (i non morti non sanguinano e non si
	 * infettano). Le immunità di un singolo personaggio le aggiunge il personaggio (vedi PersonaggioBase.isImmuneAEffetto).
	 */
	public boolean isImmuneAEffetto(TipoEffettoDiStato tipoEffettoDiStato) {
		if (immuneAEffetti.contains(tipoEffettoDiStato)) {
			return true;
		}
		for (CategoriaPersonaggio categoria : categorie) {
			if (categoria.isImmuneAEffetto(tipoEffettoDiStato)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Le caratteristiche generali della classe, quelle che il bestiario scopre: i tipi di danno che subisce in modo diverso dal normale (debole,
	 * resistente, immune), gli incantesimi (o tutti) a cui è immune e gli effetti di stato a cui è immune. Vuoto per chi non ha niente di particolare.
	 */
	public List<CaratteristicaDiClasse> getCaratteristiche() {
		List<CaratteristicaDiClasse> caratteristiche = new ArrayList<>();
		for (TipoDanno tipoDanno : TipoDanno.values()) {
			double moltiplicatore = getMoltiplicatoreDannoSubito(tipoDanno);
			if (moltiplicatore != 1.0d) {
				caratteristiche.add(CaratteristicaDiClasse.danno(tipoDanno, moltiplicatore));
			}
		}
		if (immuneATuttiGliIncantesimi) {
			caratteristiche.add(CaratteristicaDiClasse.immuneATuttiGliIncantesimi());
		} else {
			for (TipoIncantesimo tipoIncantesimo : TipoIncantesimo.values()) {
				if (isImmuneAIncantesimo(tipoIncantesimo)) {
					caratteristiche.add(CaratteristicaDiClasse.immuneAIncantesimo(tipoIncantesimo));
				}
			}
		}
		for (TipoEffettoDiStato tipoEffettoDiStato : TipoEffettoDiStato.values()) {
			if (isImmuneAEffetto(tipoEffettoDiStato)) {
				caratteristiche.add(CaratteristicaDiClasse.immuneAEffetto(tipoEffettoDiStato));
			}
		}
		return caratteristiche;
	}

	/**
	 * Il ruolo della classe, a meno del genere: Mago e Maga, Elfo e Elfa, Ladro e Ladra, Guerriero e Guerriera, Bardo e Cantastorie,
	 * Sacerdote e Sacerdotessa, Chierico e Chierica, Druido e Druida sono lo stesso ruolo (le tattiche non devono distinguerli); ogni altra classe ha il suo.
	 */
	public TipoPersonaggio getRuolo() {
		return RUOLI.getOrDefault(this, this);
	}

	/**
	 * Dice se le due classi hanno lo stesso ruolo (vedi {@link #getRuolo()}): per esempio il Mago e la Maga.
	 */
	public boolean stessoRuoloDi(TipoPersonaggio altra) {
		return getRuolo() == altra.getRuolo();
	}

	/**
	 * Se la classe compare come avversaria: i mostri e i giocanti che le missioni mettono fra i nemici (duelli, tornei, banditi...)
	 */
	public boolean isAvversario() {
		return AVVERSARI.contains(this);
	}

	/**
	 * Se la classe può entrare nel gruppo: si sceglie a inizio partita o si recluta in locanda
	 */
	public boolean isCompagno() {
		return COMPAGNI.contains(this);
	}

	/**
	 * Se la classe ha una voce nel bestiario: compare come avversaria o può entrare nel gruppo
	 */
	public boolean hasVoceNelBestiario() {
		return isAvversario() || isCompagno();
	}

	/**
	 * Le voci del bestiario, nell'ordine dell'enum: una per ruolo (vedi {@link #getRuolo()}), identificata dalla classe al maschile. Mago e Maga sono
	 * la stessa voce, e sbloccarne una sblocca anche l'altra.
	 */
	public static List<TipoPersonaggio> getVociDelBestiario() {
		Set<TipoPersonaggio> voci = new LinkedHashSet<>();
		for (TipoPersonaggio classe : values()) {
			if (classe.hasVoceNelBestiario()) {
				voci.add(classe.getRuolo());
			}
		}
		return Collections.unmodifiableList(new ArrayList<>(voci));
	}

	/**
	 * La classe al femminile che condivide il ruolo con questa (la Maga per il Mago, la Cantastorie per il Bardo), o null se la classe non ne ha
	 */
	public TipoPersonaggio getFormaFemminile() {
		for (Map.Entry<TipoPersonaggio, TipoPersonaggio> ruolo : RUOLI.entrySet()) {
			if (ruolo.getValue() == this) {
				return ruolo.getKey();
			}
		}
		return null;
	}

	/**
	 * Il nome della voce del bestiario: quello della classe, e se il ruolo ha due forme anche quello della femminile ("Mago e Maga")
	 */
	public String getNomeDellaVoce() {
		TipoPersonaggio ruolo = getRuolo();
		TipoPersonaggio femminile = ruolo.getFormaFemminile();
		return femminile == null ? ruolo.getNome() : ruolo.getNome() + " e " + femminile.getNome();
	}

	/**
	 * La descrizione della voce del bestiario: chi è e le sue forze e debolezze, a parole (è la stessa per il maschile e per il femminile)
	 */
	public String getDescrizione() {
		return Testi.testoOCostante(getRuolo(), "DESCRIZIONE");
	}

	/**
	 * Il nome della classe, per i testi (l'iniziale è maiuscola, senza articolo)
	 */
	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

}
