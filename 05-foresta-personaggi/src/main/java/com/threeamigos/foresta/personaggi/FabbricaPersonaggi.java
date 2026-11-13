package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Costruisce i personaggi di ogni TipoPersonaggio. Quel che dipende solo dalla classe non chiede un personaggio: i nomi stanno in
 * NomeDellaClasse, i valori (moltiplicatori, quanti se ne incontrano...) nella tabella CostantiPersonaggi.valoriDi.
 */
public final class FabbricaPersonaggi {

	private static final Map<TipoPersonaggio, Function<Integer, Personaggio>> COSTRUTTORI = new EnumMap<>(TipoPersonaggio.class);

	static {
		COSTRUTTORI.put(TipoPersonaggio.ARPIA, Arpia::new);
		COSTRUTTORI.put(TipoPersonaggio.CENTAURO, Centauro::new);
		COSTRUTTORI.put(TipoPersonaggio.CHIMERA, Chimera::new);
		COSTRUTTORI.put(TipoPersonaggio.CHIMERA_DRAGO, ChimeraDrago::new);
		COSTRUTTORI.put(TipoPersonaggio.DRAGO, Drago::new);
		COSTRUTTORI.put(TipoPersonaggio.EREMITA, Eremita::new);
		COSTRUTTORI.put(TipoPersonaggio.FANTASMA, Fantasma::new);
		COSTRUTTORI.put(TipoPersonaggio.FOLLETTO, Folletto::new);
		COSTRUTTORI.put(TipoPersonaggio.GARGOYLE, Gargoyle::new);
		COSTRUTTORI.put(TipoPersonaggio.GIGANTE, Gigante::new);
		COSTRUTTORI.put(TipoPersonaggio.GOBLIN, Goblin::new);
		COSTRUTTORI.put(TipoPersonaggio.HOBGOBLIN, Hobgoblin::new);
		COSTRUTTORI.put(TipoPersonaggio.IDRA, Idra::new);
		COSTRUTTORI.put(TipoPersonaggio.LICH, Lich::new);
		COSTRUTTORI.put(TipoPersonaggio.MINOTAURO, Minotauro::new);
		COSTRUTTORI.put(TipoPersonaggio.MINOTAURO_GIGANTE, MinotauroGigante::new);
		COSTRUTTORI.put(TipoPersonaggio.NECROMANTE, Necromante::new);
		COSTRUTTORI.put(TipoPersonaggio.OMBRA_NERA, OmbraNera::new);
		COSTRUTTORI.put(TipoPersonaggio.SCHELETRO, Scheletro::new);
		COSTRUTTORI.put(TipoPersonaggio.SPETTRO, Spettro::new);
		COSTRUTTORI.put(TipoPersonaggio.SPIRITO, Spirito::new);
		COSTRUTTORI.put(TipoPersonaggio.STREGA, Strega::new);
		COSTRUTTORI.put(TipoPersonaggio.TITANO, Titano::new);
		COSTRUTTORI.put(TipoPersonaggio.TROLL, Troll::new);
		COSTRUTTORI.put(TipoPersonaggio.VIVERNA, Viverna::new);
		COSTRUTTORI.put(TipoPersonaggio.BARDO, Bardo::new);
		COSTRUTTORI.put(TipoPersonaggio.CANTASTORIE, Cantastorie::new);
		COSTRUTTORI.put(TipoPersonaggio.ELFA, Elfa::new);
		COSTRUTTORI.put(TipoPersonaggio.ELFO, Elfo::new);
		COSTRUTTORI.put(TipoPersonaggio.GUERRIERA, Guerriera::new);
		COSTRUTTORI.put(TipoPersonaggio.GUERRIERO, Guerriero::new);
		COSTRUTTORI.put(TipoPersonaggio.LADRA, Ladra::new);
		COSTRUTTORI.put(TipoPersonaggio.LADRO, Ladro::new);
		COSTRUTTORI.put(TipoPersonaggio.MAGA, Maga::new);
		COSTRUTTORI.put(TipoPersonaggio.MAGO, Mago::new);
		COSTRUTTORI.put(TipoPersonaggio.OMBRAFIAMMA, OmbraFiamma::new);
		COSTRUTTORI.put(TipoPersonaggio.SACERDOTE, Sacerdote::new);
		COSTRUTTORI.put(TipoPersonaggio.SACERDOTESSA, Sacerdotessa::new);
		COSTRUTTORI.put(TipoPersonaggio.CHIERICA, Chierica::new);
		COSTRUTTORI.put(TipoPersonaggio.CHIERICO, Chierico::new);
		COSTRUTTORI.put(TipoPersonaggio.DRUIDA, Druida::new);
		COSTRUTTORI.put(TipoPersonaggio.DRUIDO, Druido::new);
		COSTRUTTORI.put(TipoPersonaggio.LOCANDIERE, Locandiere::new);
		COSTRUTTORI.put(TipoPersonaggio.ARMAIOLO, Armaiolo::new);
		COSTRUTTORI.put(TipoPersonaggio.ALCHIMISTA, Alchimista::new);
		COSTRUTTORI.put(TipoPersonaggio.VENDITORE_DI_PERGAMENE, VenditoreDiPergamene::new);
		COSTRUTTORI.put(TipoPersonaggio.INCANTATORE, Incantatore::new);
		COSTRUTTORI.put(TipoPersonaggio.MOGLIE_DEL_BARDO, MoglieDelBardo::new);
		COSTRUTTORI.put(TipoPersonaggio.VIANDANTE, Viandante::new);
		COSTRUTTORI.put(TipoPersonaggio.BARDO_DELLE_LOCANDE, BardoLocanda::new);
		COSTRUTTORI.put(TipoPersonaggio.CAPITANO_DELLE_GUARDIE, CapitanoDelleGuardie::new);
		COSTRUTTORI.put(TipoPersonaggio.VICHINGO, Vichingo::new);
		COSTRUTTORI.put(TipoPersonaggio.UOMO, Uomo::new);
		COSTRUTTORI.put(TipoPersonaggio.BORGOMASTRO, Borgomastro::new);
		COSTRUTTORI.put(TipoPersonaggio.DONNA, Donna::new);
	}

	private FabbricaPersonaggi() {
	}

	/**
	 * Un nuovo personaggio di quella classe e di quel livello (ogni TipoPersonaggio ha il suo costruttore: lo controlla TuttiIPersonaggiTest)
	 */
	public static Personaggio crea(TipoPersonaggio tipo, int livello) {
		return COSTRUTTORI.get(tipo).apply(livello);
	}

	/**
	 * Il nome al singolare della classe, per esempio "Goblin".
	 */
	public static String nomeSingolare(TipoPersonaggio tipo) {
		return NomeDellaClasse.di(tipo).getNomeSingolare();
	}

	/**
	 * Il nome al plurale della classe, per esempio "Idre".
	 */
	public static String nomePlurale(TipoPersonaggio tipo) {
		return NomeDellaClasse.di(tipo).getNomePlurale();
	}

	/**
	 * Quanti personaggi di quella classe si incontrano al massimo in una locazione
	 */
	public static int quantitaMassima(TipoPersonaggio tipo) {
		return CostantiPersonaggi.valoriDi(tipo).getQuantitaMassima();
	}
}
