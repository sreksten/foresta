package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.RegistroMissioniMD;

import java.util.*;

public class RegistroMissioni {

	private static final RegistroMissioniMD md;

	static {
		md = ModelloDati.getIstanza().getRegistroMissioniMD();
	}

	private RegistroMissioni() {
	}

	public enum TipoMissionePredefinita {
		SCONFIGGI_IL_DRAGO(ClasseMissione.SCONFIGGI_IL_DRAGO),
		SCONFIGGI_IL_MINOTAURO_GIGANTE(ClasseMissione.SCONFIGGI_IL_MINOTAURO_GIGANTE),
		SCONFIGGI_L_IDRA(ClasseMissione.SCONFIGGI_L_IDRA),
		SCONFIGGI_IL_LICH(ClasseMissione.SCONFIGGI_IL_LICH),
		SCONFIGGI_LA_STREGA(ClasseMissione.SCONFIGGI_LA_STREGA),
		MISSIONE_DI_PROVA(ClasseMissione.MISSIONE_DI_PROVA),
		RECUPERA_IL_MEDAGLIONE(ClasseMissione.RECUPERA_IL_MEDAGLIONE),
		RECUPERA_LE_DERRATE_ALIMENTARI(ClasseMissione.RECUPERA_LE_DERRATE_ALIMENTARI),
		CRONACHE_DI_UN_FEGATO_EROICO(ClasseMissione.CRONACHE_DI_UN_FEGATO_EROICO),
		NESSUN_BOCCALE_LASCIATO_INDIETRO(ClasseMissione.NESSUN_BOCCALE_LASCIATO_INDIETRO),
		DISTURBATORE_DELLA_QUIETE_PUBBLICA(ClasseMissione.DISTURBATORE_DELLA_QUIETE_PUBBLICA);

		TipoMissionePredefinita(ClasseMissione classeMissione) {
			this.classeMissione = classeMissione;
		}

		private final ClasseMissione classeMissione;

		public Missione getIstanza() {
			return classeMissione.getIstanza();
		}

		public static boolean contieneMissione(String id) {
			return Arrays.stream(TipoMissionePredefinita.values()).anyMatch(m -> m.name().equals(id));
		}
	}

	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefinite = new EnumMap<>(TipoMissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarie = new ArrayList<>();

	public static void reimposta() {
		md.reimposta();
		elencoMissioniPredefinite.clear();
		elencoMissioniSecondarie.clear();
		for (TipoMissionePredefinita tipoMissionePredefinita : TipoMissionePredefinita.values()) {
			Missione missione = tipoMissionePredefinita.getIstanza();
			missione.getModelloDati().setId(tipoMissionePredefinita.name());
			elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
			md.aggiungiMissione(tipoMissionePredefinita.name(), missione.getModelloDati());
		}
	}
	
	public static void aggiornaDopoRilettura() {
		elencoMissioniPredefinite.clear();
		elencoMissioniSecondarie.clear();
		for (MissioneMD missioneMD : md.getMissioni()) {
			if (TipoMissionePredefinita.contieneMissione(missioneMD.getId())) {
				TipoMissionePredefinita tipoMissionePredefinita = TipoMissionePredefinita.valueOf(missioneMD.getId());
				// Il tipo delle radici predefinite lo decide il codice, non il salvataggio:
				// altrimenti un salvataggio manomesso farebbe esplodere getMissionePrincipale().
				elencoMissioniPredefinite.put(tipoMissionePredefinita, ricostruisci(tipoMissionePredefinita.getIstanza(), missioneMD));
			} else {
				elencoMissioniSecondarie.add(ricostruisci(missioneMD));
			}
		}
	}

	/**
	 * Ricrea l'oggetto di dominio del nodo, della classe dichiarata nel modello dati,
	 * e ricorre sui figli.
	 */
	private static Missione ricostruisci(MissioneMD missioneMD) {
		return ricostruisci(missioneMD.getClasse().getIstanza(), missioneMD);
	}

	private static Missione ricostruisci(Missione missione, MissioneMD missioneMD) {
		missione.setModelloDati(missioneMD);
		List<Missione> missioniSecondarie = new ArrayList<>();
		for (MissioneMD missioneSecondariaMD : missioneMD.getMissioniMD()) {
			missioniSecondarie.add(ricostruisci(missioneSecondariaMD));
		}
		missione.sostituisciMissioniSecondarie(missioniSecondarie);
		return missione;
	}

	public static Missione getMissione(TipoMissionePredefinita tipoMissionePredefinita) {
		return elencoMissioniPredefinite.get(tipoMissionePredefinita);
	}

	/**
	 * Riporta le missioni di primo livello non completate, attive e non. La discesa
	 * nell'albero è a carico del chiamante.
	 */
	public static List<Missione> getMissioni() {
		List<Missione> missioni = new ArrayList<>();
		elencoMissioniPredefinite.values().stream().filter(m-> !m.isCompleta()).forEach(missioni::add);
		elencoMissioniSecondarie.stream().filter(m-> !m.isCompleta()).forEach(missioni::add);
		return missioni;
	}

	/**
	 * Riporta tutte le missioni attive.
	 */
	public static List<Missione> getMissioniAttive() {
		List<Missione> missioni = new ArrayList<>();
		elencoMissioniPredefinite.values().stream().filter(m -> m.isAttiva() && !m.isCompleta()).forEach(missioni::add);
		elencoMissioniSecondarie.stream().filter(m -> m.isAttiva() && !m.isCompleta()).forEach(missioni::add);
		return missioni;
	}

	/**
	 * Riporta le missioni da mostrare nel riquadro: quelle che sono state attivate,
	 * completate o no. Le missioni non ancora attivate non vanno mostrate.
	 */
	public static List<Missione> getMissioniDaMostrare() {
		List<Missione> missioni = new ArrayList<>();
		elencoMissioniPredefinite.values().stream().filter(RegistroMissioni::isDaMostrare).forEach(missioni::add);
		elencoMissioniSecondarie.stream().filter(RegistroMissioni::isDaMostrare).forEach(missioni::add);
		return missioni;
	}

	/**
	 * Completare una missione non ne azzera l'attivazione, ma non tutte le missioni
	 * passano per attivaMissione(): entrambe le condizioni vanno controllate.
	 */
	public static boolean isDaMostrare(Missione missione) {
		return missione.isAttiva() || missione.isCompleta();
	}

	public static List<Missione> getMissioniCompletate() {
		List<Missione> missioni = new ArrayList<>();
		elencoMissioniPredefinite.values().stream().filter(Missione::isCompleta).forEach(missioni::add);
		elencoMissioniSecondarie.stream().filter(Missione::isCompleta).forEach(missioni::add);
		return missioni;
	}

	public static SconfiggiIlDrago getMissionePrincipale() {
		return (SconfiggiIlDrago) elencoMissioniPredefinite.get(TipoMissionePredefinita.SCONFIGGI_IL_DRAGO);
	}
}
