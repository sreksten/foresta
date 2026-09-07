package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.motore.modellodati.MissioneMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.RegistroMissioniMD;

import java.util.*;
import java.util.function.Supplier;

public class RegistroMissioni {

	private static final RegistroMissioniMD md;

	static {
		md = ModelloDati.getIstanza().getRegistroMissioniMD();
	}

	private RegistroMissioni() {
	}

	public enum TipoMissionePredefinita {
		SCONFIGGI_IL_DRAGO(SconfiggiIlDrago::new),
		SCONFIGGI_IL_MINOTAURO_GIGANTE(SconfiggiIlMinotauroGigante::new),
		SCONFIGGI_L_IDRA(SconfiggiLIdra::new),
		SCONFIGGI_IL_LICH(SconfiggiIlLich::new),
		SCONFIGGI_LA_STREGA(SconfiggiLaStrega::new),
		MISSIONE_DI_PROVA(MissioneDIProva::new),
		RECUPERA_IL_MEDAGLIONE(RecuperaIlMedaglione::new),
		RECUPERA_LE_DERRATE_ALIMENTARI(RecuperaLeDerrateAlimentari::new);
		
		TipoMissionePredefinita(Supplier<Missione> supplier) {
			this.supplier = supplier;
		}

		private final Supplier<Missione> supplier;
		
		public Missione getIstanza() {
			return supplier.get();
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
				Missione missione = tipoMissionePredefinita.getIstanza();
				elencoMissioniPredefinite.put(tipoMissionePredefinita, missione);
				missione.setModelloDati(md.getMissione(tipoMissionePredefinita.name()));
			} else {
				elencoMissioniSecondarie.add(new MissioneSecondaria(missioneMD));
			}
		}
	}

	public static Missione getMissione(TipoMissionePredefinita tipoMissionePredefinita) {
		return elencoMissioniPredefinite.get(tipoMissionePredefinita);
	}

	/**
	 * Riporta tutte le missioni non completate, attive e non.
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
