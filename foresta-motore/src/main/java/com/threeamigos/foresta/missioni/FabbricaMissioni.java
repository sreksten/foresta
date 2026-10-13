package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.ClasseMissione;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import com.threeamigos.foresta.missioni.incarichi.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.incarichi.CacciatoreDiTaglie;
import com.threeamigos.foresta.missioni.incarichi.IlCartografo;
import com.threeamigos.foresta.missioni.incarichi.IlColpo;
import com.threeamigos.foresta.missioni.incarichi.IlContrabbandiere;
import com.threeamigos.foresta.missioni.incarichi.IlCorriere;
import com.threeamigos.foresta.missioni.incarichi.IlPellegrino;
import com.threeamigos.foresta.missioni.incarichi.IlRapimento;
import com.threeamigos.foresta.missioni.incarichi.IlRituale;
import com.threeamigos.foresta.missioni.incarichi.IlSoccorso;
import com.threeamigos.foresta.missioni.incarichi.IlTorneo;
import com.threeamigos.foresta.missioni.incarichi.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.incarichi.LIndagine;
import com.threeamigos.foresta.missioni.incarichi.LOggettoSmarrito;
import com.threeamigos.foresta.missioni.incarichi.LaDocumentazione;
import com.threeamigos.foresta.missioni.incarichi.LaSorveglianza;
import com.threeamigos.foresta.missioni.incarichi.LaTagliaSullaBanda;
import com.threeamigos.foresta.missioni.incarichi.RecuperaIlMedaglione;
import com.threeamigos.foresta.missioni.incarichi.RecuperaLeDerrateAlimentari;
import com.threeamigos.foresta.missioni.incarichi.RichiestaDiMateriali;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDelLocandiere;
import com.threeamigos.foresta.missioni.leggende.LaLeggendaDellArmaiolo;
import com.threeamigos.foresta.missioni.principali.CronacheDiUnFegatoEroico;
import com.threeamigos.foresta.missioni.principali.DisturbatoreDellaQuietePubblica;
import com.threeamigos.foresta.missioni.principali.MuoviALocazione;
import com.threeamigos.foresta.missioni.principali.NessunBoccaleLasciatoIndietro;
import com.threeamigos.foresta.missioni.principali.SconfiggiIlDrago;
import com.threeamigos.foresta.missioni.principali.SconfiggiIlLich;
import com.threeamigos.foresta.missioni.principali.SconfiggiIlMinotauroGigante;
import com.threeamigos.foresta.missioni.principali.SconfiggiLIdra;
import com.threeamigos.foresta.missioni.principali.SconfiggiLaStrega;
import com.threeamigos.foresta.missioni.principali.VisitaLocanda;
import com.threeamigos.foresta.missioni.storie.IlCampione;
import com.threeamigos.foresta.missioni.storie.IlFavore;
import com.threeamigos.foresta.missioni.storie.IlViaggioDellaMoglie;
import com.threeamigos.foresta.missioni.storie.LaBenedizione;
import com.threeamigos.foresta.missioni.storie.LaLealta;
import com.threeamigos.foresta.missioni.storie.LaSfidaDeiCampioni;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;

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
		COSTRUTTORI.put(ClasseMissione.IL_VIAGGIO_DELLA_MOGLIE, IlViaggioDellaMoglie::new);
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
		COSTRUTTORI.put(ClasseMissione.LA_SFIDA_DEI_CAMPIONI, LaSfidaDeiCampioni::new);
		COSTRUTTORI.put(ClasseMissione.IL_CAMPIONE, IlCampione::new);
		COSTRUTTORI.put(ClasseMissione.VISITA_LOCANDA, VisitaLocanda::new);
		COSTRUTTORI.put(ClasseMissione.MUOVI_A_LOCAZIONE, MuoviALocazione::new);
		COSTRUTTORI.put(ClasseMissione.MISSIONE_SECONDARIA, MissioneSecondaria::new);
	}

	private FabbricaMissioni() {
	}

	/**
	 * Fa conoscere alla fabbrica una missione che non sta nel gioco, come quelle che i test inventano, così si può ricostruire
	 * dopo un caricamento.
	 */
	public static void registra(ClasseMissione classe, Supplier<Missione> costruttore) {
		COSTRUTTORI.put(classe, costruttore);
	}

	/**
	 * Una nuova missione di quella classe
	 */
	public static Missione crea(ClasseMissione classe) {
		return COSTRUTTORI.get(classe).get();
	}
}
