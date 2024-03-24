package com.threeamigos.foresta.motore;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RecuperaIlMedaglione;
import com.threeamigos.foresta.missioni.RecuperaLeDerrateAlimentari;
import com.threeamigos.foresta.missioni.SconfiggiIlDrago;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;

public class RegistroMissioni {

	private RegistroMissioni() {
	}

	public enum TipoMissione {
		SCONFIGGI_IL_DRAGO(SconfiggiIlDrago::new),
		RECUPERA_IL_MEDAGLIONE(RecuperaIlMedaglione::new),
		RECUPERA_LE_DERRATE_ALIMENTARI(RecuperaLeDerrateAlimentari::new);
		
		private TipoMissione(Supplier<Missione> supplier) {
			this.supplier = supplier;
		}

		private Supplier<Missione> supplier;
		
		public Missione getIstanza() {
			return supplier.get();
		}
	}

	private static Map<TipoMissione, Missione> elencoMissioni = new EnumMap<>(TipoMissione.class);
	
	public static final void reimposta() {
		ModelloDati.getIstanza().getRegistroMissioniMD().reimposta();		
		elencoMissioni.clear();
		for (TipoMissione tipoMissione : TipoMissione.values()) {
			Missione missione = tipoMissione.getIstanza();
			elencoMissioni.put(tipoMissione, missione);
			missione.getModelloDati().setId(tipoMissione.name());
			ModelloDati.getIstanza().getRegistroMissioniMD().aggiungiMissione(missione.getModelloDati());
		}
	}
	
	public static final void aggiornaDopoRilettura() {
		elencoMissioni.clear();
		for (TipoMissione tipoMissione : TipoMissione.values()) {
			Missione missione = tipoMissione.getIstanza();
			elencoMissioni.put(tipoMissione, missione);
			missione.setModelloDati(ModelloDati.getIstanza().getRegistroMissioniMD().getMissione(tipoMissione.name()));
		}
	}

	public static final Missione getMissione(TipoMissione tipoMissione) {
		return elencoMissioni.get(tipoMissione);
	}
	
	public static final List<Missione> getMissioni() {
		return elencoMissioni.values().stream().filter(m-> !m.isCompleta()).collect(Collectors.toList());
	}

	public static final List<Missione> getMissioniAttive() {
		return elencoMissioni.values().stream().filter(m -> m.isAttiva() && !m.isCompleta()).collect(Collectors.toList());
	}
	
	public static final SconfiggiIlDrago getMissionePrincipale() {
		return (SconfiggiIlDrago)elencoMissioni.get(TipoMissione.SCONFIGGI_IL_DRAGO);
	}
}
