package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.SconfiggiIlDrago;
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
		//FIXME va levata dopo le prove
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
	private static final Map<TipoMissionePredefinita, Missione> elencoMissioniPredefiniteCompletate = new EnumMap<>(TipoMissionePredefinita.class);
	private static final List<Missione> elencoMissioniSecondarie = new ArrayList<>();
	private static final List<Missione> elencoMissioniSecondarieCompletate = new ArrayList<>();

	private static void pulisciElenchi() {
		elencoMissioniPredefinite.clear();
		elencoMissioniPredefiniteCompletate.clear();
		elencoMissioniSecondarie.clear();
		elencoMissioniSecondarieCompletate.clear();
	}

	public static void reimposta() {
		md.reimposta();
		pulisciElenchi();

		for (TipoMissionePredefinita tipoMissionePredefinita : TipoMissionePredefinita.values()) {
			Missione missione = tipoMissionePredefinita.getIstanza();
			missione.getModelloDati().setId(tipoMissionePredefinita.name());
			elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
			md.aggiungiMissione(tipoMissionePredefinita.name(), missione.getModelloDati());
		}
	}
	
	public static void aggiornaDopoRilettura() {
		pulisciElenchi();
		aggiornaDopoRiletturaImpl(md.getMissioniAttive());
		aggiornaDopoRiletturaImpl(md.getMissioniCompletate());
	}

	private static void aggiornaDopoRiletturaImpl(Collection<MissioneMD> missioni) {
		for (MissioneMD missioneMD : missioni) {
			if (TipoMissionePredefinita.contieneMissione(missioneMD.getId())) {
				TipoMissionePredefinita tipoMissionePredefinita = TipoMissionePredefinita.valueOf(missioneMD.getId());
				Missione missione = ricostruisci(tipoMissionePredefinita.getIstanza(), missioneMD);
				if (missione.isCompleta()) {
					elencoMissioniPredefiniteCompletate.put(tipoMissionePredefinita, missione);
				} else {
					elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
				}
			} else {
				Missione missione = ricostruisci(missioneMD);
				if (missione.isCompleta()) {
					elencoMissioniSecondarie.add(missione);
				} else {
					elencoMissioniSecondarieCompletate.add(missione);
				}
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

	/**
	 * Riporta le missioni di primo livello non completate, attive e da attivare. La discesa
	 * nell'albero è a carico del chiamante.
	 */
	public static List<Missione> getMissioniNonCompletate() {
        List<Missione> missioni = new ArrayList<>(elencoMissioniPredefinite.values());
		missioni.addAll(elencoMissioniSecondarie);
		return missioni;
	}

	/**
	 * Riporta solo le missioni attive.
	 */
	public static List<Missione> getMissioniAttive() {
		List<Missione> missioni = new ArrayList<>();
		elencoMissioniPredefinite.values().stream().filter(Missione::isAttiva).forEach(missioni::add);
		elencoMissioniSecondarie.stream().filter(Missione::isAttiva).forEach(missioni::add);
		return missioni;
	}

	public static List<Missione> getMissioniCompletate() {
		List<Missione> missioni = new ArrayList<>(elencoMissioniPredefiniteCompletate.values());
		missioni.addAll(elencoMissioniSecondarieCompletate);
		return missioni;
	}

	public static SconfiggiIlDrago getMissionePrincipale() {
		return (SconfiggiIlDrago) elencoMissioniPredefinite.get(TipoMissionePredefinita.SCONFIGGI_IL_DRAGO);
	}

	public static void completaMissione(Missione missione) {
		if (TipoMissionePredefinita.contieneMissione(missione.getId())) {
			TipoMissionePredefinita tipoMissione = TipoMissionePredefinita.valueOf(missione.getId());
			elencoMissioniPredefinite.remove(tipoMissione);
			elencoMissioniPredefiniteCompletate.put(tipoMissione, missione);
		} else {
			elencoMissioniSecondarie.removeIf(missione::equals);
			elencoMissioniSecondarieCompletate.add(missione);
		}
	}
}
