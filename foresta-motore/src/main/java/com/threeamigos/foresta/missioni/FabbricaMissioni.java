package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.missioni.incarichi.*;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDelLocandiere;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo;
import com.threeamigos.foresta.missioni.principali.*;
import com.threeamigos.foresta.missioni.storie.*;
import com.threeamigos.foresta.tipi.MissionePredefinita;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Costruisce la missione di ogni ClasseMissione (per esempio nella ricostruzione dell'albero delle missioni dopo un
 * caricamento).
 */
public final class FabbricaMissioni {

	private static final Map<MissionePredefinita, Supplier<Missione>> COSTRUTTORI = new EnumMap<>(MissionePredefinita.class);

	static {
		COSTRUTTORI.put(MissionePredefinita.SCONFIGGI_IL_DRAGO, SconfiggiIlDrago::new);
		COSTRUTTORI.put(MissionePredefinita.SCONFIGGI_IL_MINOTAURO_GIGANTE, SconfiggiIlMinotauroGigante::new);
		COSTRUTTORI.put(MissionePredefinita.SCONFIGGI_L_IDRA, SconfiggiLIdra::new);
		COSTRUTTORI.put(MissionePredefinita.SCONFIGGI_IL_LICH, SconfiggiIlLich::new);
		COSTRUTTORI.put(MissionePredefinita.SCONFIGGI_LA_STREGA, SconfiggiLaStrega::new);
		COSTRUTTORI.put(MissionePredefinita.LA_LEGGENDA_DELL_ARMAIOLO, LaLeggendaDellArmaiolo::new);
		COSTRUTTORI.put(MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE, LaLeggendaDelLocandiere::new);
		COSTRUTTORI.put(MissionePredefinita.CRONACHE_DI_UN_FEGATO_EROICO, CronacheDiUnFegatoEroico::new);
		COSTRUTTORI.put(MissionePredefinita.NESSUN_BOCCALE_LASCIATO_INDIETRO, NessunBoccaleLasciatoIndietro::new);
		COSTRUTTORI.put(MissionePredefinita.DISTURBATORE_DELLA_QUIETE_PUBBLICA, DisturbatoreDellaQuietePubblica::new);
		COSTRUTTORI.put(MissionePredefinita.IL_GIRO_DELLE_SETTE_CHIESE, IlGiroDelleSetteChiese::new);
		COSTRUTTORI.put(MissionePredefinita.CACCIA_AI_GOBLIN, CacciaAiGoblin::new);
		COSTRUTTORI.put(MissionePredefinita.RICHIESTA_ALCHIMISTA, RichiestaDiMateriali::dellAlchimista);
		COSTRUTTORI.put(MissionePredefinita.RICHIESTA_ARMAIOLO, RichiestaDiMateriali::dellArmaiolo);
		COSTRUTTORI.put(MissionePredefinita.RICHIESTA_CAPITANO, RichiestaDiMateriali::delCapitano);
		COSTRUTTORI.put(MissionePredefinita.RICHIESTA_LOCANDIERE, RichiestaDiMateriali::delLocandiere);
		COSTRUTTORI.put(MissionePredefinita.LA_TAGLIA_SULLA_BANDA, LaTagliaSullaBanda::new);
		COSTRUTTORI.put(MissionePredefinita.IL_PELLEGRINO, IlPellegrino::new);
		COSTRUTTORI.put(MissionePredefinita.IL_RAPIMENTO, IlRapimento::new);
		COSTRUTTORI.put(MissionePredefinita.NON_SPARATE_SUL_PIANISTA, NonSparateSulPianista::new);
		COSTRUTTORI.put(MissionePredefinita.IL_VIAGGIO_DELLA_MOGLIE, IlViaggioDellaMoglie::new);
		COSTRUTTORI.put(MissionePredefinita.CACCIATORE_DI_TAGLIE, CacciatoreDiTaglie::new);
		COSTRUTTORI.put(MissionePredefinita.IL_CARTOGRAFO, IlCartografo::new);
		COSTRUTTORI.put(MissionePredefinita.IL_CORRIERE, IlCorriere::new);
		COSTRUTTORI.put(MissionePredefinita.L_OGGETTO_SMARRITO, LOggettoSmarrito::new);
		COSTRUTTORI.put(MissionePredefinita.INCARICO_DI_COMBATTIMENTO, IncaricoDiCombattimento::new);
		COSTRUTTORI.put(MissionePredefinita.LA_SORVEGLIANZA, LaSorveglianza::new);
		COSTRUTTORI.put(MissionePredefinita.IL_CONTRABBANDIERE, IlContrabbandiere::new);
		COSTRUTTORI.put(MissionePredefinita.IL_SOCCORSO, IlSoccorso::new);
		COSTRUTTORI.put(MissionePredefinita.L_INDAGINE, LIndagine::new);
		COSTRUTTORI.put(MissionePredefinita.LA_BENEDIZIONE, LaBenedizione::new);
		COSTRUTTORI.put(MissionePredefinita.IL_FAVORE, IlFavore::new);
		COSTRUTTORI.put(MissionePredefinita.LA_LEALTA, LaLealta::new);
		COSTRUTTORI.put(MissionePredefinita.IL_TORNEO, IlTorneo::new);
		COSTRUTTORI.put(MissionePredefinita.IL_NEGOZIO_DELL_ALCHIMISTA, IlNegozioDellAlchimista::new);
		COSTRUTTORI.put(MissionePredefinita.IL_NEGOZIO_DELL_ARMAIOLO, IlNegozioDellArmaiolo::new);
		COSTRUTTORI.put(MissionePredefinita.IL_NEGOZIO_DEL_VENDITORE_DI_PERGAMENE, IlNegozioDelVenditoreDiPergamene::new);
		COSTRUTTORI.put(MissionePredefinita.IL_NEGOZIO_DELL_INCANTATORE, IlNegozioDellIncantatore::new);
		COSTRUTTORI.put(MissionePredefinita.LA_DOCUMENTAZIONE, LaDocumentazione::new);
		COSTRUTTORI.put(MissionePredefinita.IL_COLPO, IlColpo::new);
		COSTRUTTORI.put(MissionePredefinita.IL_RITUALE, IlRituale::new);
		COSTRUTTORI.put(MissionePredefinita.LA_SFIDA_DEI_CAMPIONI, LaSfidaDeiCampioni::new);
		COSTRUTTORI.put(MissionePredefinita.IL_CAMPIONE, IlCampione::new);
		COSTRUTTORI.put(MissionePredefinita.VISITA_LOCANDA, VisitaLocanda::new);
		COSTRUTTORI.put(MissionePredefinita.MUOVI_A_LOCAZIONE, MuoviALocazione::new);
		COSTRUTTORI.put(MissionePredefinita.MISSIONE_SECONDARIA, MissioneSecondaria::new);
	}

	private FabbricaMissioni() {
	}

	/**
	 * Fa conoscere alla fabbrica una missione che non sta nel gioco, come quelle che i test inventano, così si può ricostruire
	 * dopo un caricamento.
	 */
	public static void registra(MissionePredefinita classe, Supplier<Missione> costruttore) {
		COSTRUTTORI.put(classe, costruttore);
	}

	/**
	 * Una nuova missione di quella classe
	 */
	public static Missione crea(MissionePredefinita classe) {
		return COSTRUTTORI.get(classe).get();
	}
}
