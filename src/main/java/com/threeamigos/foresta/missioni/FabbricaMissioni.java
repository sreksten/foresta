package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.ClasseMissione;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Costruisce la missione di ogni ClasseMissione (per esempio nella ricostruzione dell'albero delle missioni dopo un
 * caricamento).
 */
public final class FabbricaMissioni {

	private static final Map<ClasseMissione, Supplier<Missione>> COSTRUTTORI = new EnumMap<>(ClasseMissione.class);

	static {
		COSTRUTTORI.put(ClasseMissione.SCONFIGGI_IL_DRAGO, SconfiggiIlDrago::new);
		COSTRUTTORI.put(ClasseMissione.SCONFIGGI_IL_MINOTAURO_GIGANTE, SconfiggiIlMinotauroGigante::new);
		COSTRUTTORI.put(ClasseMissione.SCONFIGGI_L_IDRA, SconfiggiLIdra::new);
		COSTRUTTORI.put(ClasseMissione.SCONFIGGI_IL_LICH, SconfiggiIlLich::new);
		COSTRUTTORI.put(ClasseMissione.SCONFIGGI_LA_STREGA, SconfiggiLaStrega::new);
		COSTRUTTORI.put(ClasseMissione.MISSIONE_DI_PROVA, MissioneDIProva::new);
		COSTRUTTORI.put(ClasseMissione.MISSIONE_CHE_FALLISCE, MissioneCheFallisce::new);
		COSTRUTTORI.put(ClasseMissione.RECUPERA_IL_MEDAGLIONE, RecuperaIlMedaglione::new);
		COSTRUTTORI.put(ClasseMissione.RECUPERA_LE_DERRATE_ALIMENTARI, RecuperaLeDerrateAlimentari::new);
		COSTRUTTORI.put(ClasseMissione.LA_LEGGENDA_DELL_ARMAIOLO, LaLeggendaDellArmaiolo::new);
		COSTRUTTORI.put(ClasseMissione.LA_LEGGENDA_DEL_LOCANDIERE, LaLeggendaDelLocandiere::new);
		COSTRUTTORI.put(ClasseMissione.CRONACHE_DI_UN_FEGATO_EROICO, CronacheDiUnFegatoEroico::new);
		COSTRUTTORI.put(ClasseMissione.NESSUN_BOCCALE_LASCIATO_INDIETRO, NessunBoccaleLasciatoIndietro::new);
		COSTRUTTORI.put(ClasseMissione.DISTURBATORE_DELLA_QUIETE_PUBBLICA, DisturbatoreDellaQuietePubblica::new);
		COSTRUTTORI.put(ClasseMissione.CACCIA_AI_GOBLIN, CacciaAiGoblin::new);
		COSTRUTTORI.put(ClasseMissione.RICHIESTA_ALCHIMISTA, RichiestaDiMateriali::dellAlchimista);
		COSTRUTTORI.put(ClasseMissione.RICHIESTA_ARMAIOLO, RichiestaDiMateriali::dellArmaiolo);
		COSTRUTTORI.put(ClasseMissione.RICHIESTA_CAPITANO, RichiestaDiMateriali::delCapitano);
		COSTRUTTORI.put(ClasseMissione.RICHIESTA_LOCANDIERE, RichiestaDiMateriali::delLocandiere);
		COSTRUTTORI.put(ClasseMissione.LA_TAGLIA_SULLA_BANDA, LaTagliaSullaBanda::new);
		COSTRUTTORI.put(ClasseMissione.IL_PELLEGRINO, IlPellegrino::new);
		COSTRUTTORI.put(ClasseMissione.IL_RAPIMENTO, IlRapimento::new);
		COSTRUTTORI.put(ClasseMissione.NON_SPARATE_SUL_PIANISTA, NonSparateSulPianista::new);
		COSTRUTTORI.put(ClasseMissione.CACCIATORE_DI_TAGLIE, CacciatoreDiTaglie::new);
		COSTRUTTORI.put(ClasseMissione.IL_CARTOGRAFO, IlCartografo::new);
		COSTRUTTORI.put(ClasseMissione.IL_CORRIERE, IlCorriere::new);
		COSTRUTTORI.put(ClasseMissione.L_OGGETTO_SMARRITO, LOggettoSmarrito::new);
		COSTRUTTORI.put(ClasseMissione.INCARICO_DI_COMBATTIMENTO, IncaricoDiCombattimento::new);
		COSTRUTTORI.put(ClasseMissione.LA_SORVEGLIANZA, LaSorveglianza::new);
		COSTRUTTORI.put(ClasseMissione.IL_CONTRABBANDIERE, IlContrabbandiere::new);
		COSTRUTTORI.put(ClasseMissione.IL_SOCCORSO, IlSoccorso::new);
		COSTRUTTORI.put(ClasseMissione.L_INDAGINE, LIndagine::new);
		COSTRUTTORI.put(ClasseMissione.LA_BENEDIZIONE, LaBenedizione::new);
		COSTRUTTORI.put(ClasseMissione.IL_FAVORE, IlFavore::new);
		COSTRUTTORI.put(ClasseMissione.LA_LEALTA, LaLealta::new);
		COSTRUTTORI.put(ClasseMissione.IL_TORNEO, IlTorneo::new);
		COSTRUTTORI.put(ClasseMissione.LA_DOCUMENTAZIONE, LaDocumentazione::new);
		COSTRUTTORI.put(ClasseMissione.IL_COLPO, IlColpo::new);
		COSTRUTTORI.put(ClasseMissione.IL_RITUALE, IlRituale::new);
		COSTRUTTORI.put(ClasseMissione.MISSIONE_DI_PROVA_SECONDARIA_UNO, MissioneDiProvaSecondariaUno::new);
		COSTRUTTORI.put(ClasseMissione.MISSIONE_DI_PROVA_SECONDARIA_DUE, MissioneDiProvaSecondariaDue::new);
		COSTRUTTORI.put(ClasseMissione.MISSIONE_DI_PROVA_TERZIARIA_UNO, MissioneDiProvaTerziariaUno::new);
		COSTRUTTORI.put(ClasseMissione.VISITA_LOCANDA, VisitaLocanda::new);
		COSTRUTTORI.put(ClasseMissione.MUOVI_A_LOCAZIONE, MuoviALocazione::new);
		COSTRUTTORI.put(ClasseMissione.MISSIONE_SECONDARIA, MissioneSecondaria::new);
	}

	private FabbricaMissioni() {
	}

	/**
	 * Una nuova missione di quella classe
	 */
	public static Missione crea(ClasseMissione classe) {
		return COSTRUTTORI.get(classe).get();
	}
}
