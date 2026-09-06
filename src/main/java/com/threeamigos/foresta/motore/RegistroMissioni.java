package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegistroMissioni {

	private RegistroMissioni() {
	}

	public enum TipoMissione {
		SCONFIGGI_IL_DRAGO(SconfiggiIlDrago::new),
		SCONFIGGI_IL_MINOTAURO_GIGANTE(SconfiggiIlMinotauroGigante::new),
		SCONFIGGI_L_IDRA(SconfiggiLIdra::new),
		SCONFIGGI_IL_LICH(SconfiggiIlLich::new),
		SCONFIGGI_LA_STREGA(SconfiggiLaStrega::new),
		RECUPERA_IL_MEDAGLIONE(RecuperaIlMedaglione::new),
		RECUPERA_LE_DERRATE_ALIMENTARI(RecuperaLeDerrateAlimentari::new);
		
		TipoMissione(Supplier<Missione> supplier) {
			this.supplier = supplier;
		}

		private final Supplier<Missione> supplier;
		
		public Missione getIstanza() {
			return supplier.get();
		}
	}

	private static final Map<TipoMissione, Missione> elencoMissioni = new EnumMap<>(TipoMissione.class);
	
	public static void reimposta() {
		ModelloDati.getIstanza().getRegistroMissioniMD().reimposta();		
		elencoMissioni.clear();
		for (TipoMissione tipoMissione : TipoMissione.values()) {
			Missione missione = tipoMissione.getIstanza();
			elencoMissioni.put(tipoMissione, missione);
			ModelloDati.getIstanza().getRegistroMissioniMD().aggiungiMissione(missione.getModelloDati());
		}
	}
	
	public static void aggiornaDopoRilettura() {
		elencoMissioni.clear();
		for (TipoMissione tipoMissione : TipoMissione.values()) {
			Missione missione = tipoMissione.getIstanza();
			elencoMissioni.put(tipoMissione, missione);
			missione.setModelloDati(ModelloDati.getIstanza().getRegistroMissioniMD().getMissione(tipoMissione.name()));
		}
	}

	public static Missione getMissione(TipoMissione tipoMissione) {
		return elencoMissioni.get(tipoMissione);
	}
	
	public static List<Missione> getMissioni() {
		return elencoMissioni.values().stream().filter(m-> !m.isCompleta()).collect(Collectors.toList());
	}

	public static List<Missione> getMissioniAttive() {
		return elencoMissioni.values().stream().filter(m -> m.isAttiva() && !m.isCompleta()).collect(Collectors.toList());
	}

	public static List<Missione> getMissioniCompletate() {
		return elencoMissioni.values().stream().filter(Missione::isCompleta).collect(Collectors.toList());
	}

	public static SconfiggiIlDrago getMissionePrincipale() {
		return (SconfiggiIlDrago)elencoMissioni.get(TipoMissione.SCONFIGGI_IL_DRAGO);
	}
}
