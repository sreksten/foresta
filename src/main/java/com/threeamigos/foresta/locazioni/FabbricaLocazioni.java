package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Costruisce la locazione di ogni TipoLocazione. Chi la usa per una casella della Foresta deve poi passarle il modello
 * dati di quella casella.
 */
public final class FabbricaLocazioni {

	private static final Map<TipoLocazione, Supplier<Locazione>> COSTRUTTORI = new EnumMap<>(TipoLocazione.class);

	static {
		COSTRUTTORI.put(TipoLocazione.RADURA, Radura::new);
		COSTRUTTORI.put(TipoLocazione.BOSCO, Bosco::new);
		COSTRUTTORI.put(TipoLocazione.PALUDE, Palude::new);
		COSTRUTTORI.put(TipoLocazione.LOCANDA, Locanda::new);
		COSTRUTTORI.put(TipoLocazione.ROVINE, Rovine::new);
		COSTRUTTORI.put(TipoLocazione.TEMPIO, Tempio::new);
		COSTRUTTORI.put(TipoLocazione.GROTTA, Grotta::new);
		COSTRUTTORI.put(TipoLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE, GrottaRecuperaIlMedaglione::new);
		COSTRUTTORI.put(TipoLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI, RovineRecuperaLeDerrateAlimentari::new);
		COSTRUTTORI.put(TipoLocazione.CITTA_NYENA, CittaNyena::new);
		COSTRUTTORI.put(TipoLocazione.CITTA_MALGAARD, CittaMalgaard::new);
		COSTRUTTORI.put(TipoLocazione.CITTA_RUUNA, CittaRuuna::new);
		COSTRUTTORI.put(TipoLocazione.CITTA_FLEENA, CittaFleena::new);
		COSTRUTTORI.put(TipoLocazione.CASTELLO_IDRA, CastelloIdra::new);
		COSTRUTTORI.put(TipoLocazione.CASTELLO_MINOTAURO, CastelloMinotauro::new);
		COSTRUTTORI.put(TipoLocazione.CASTELLO_LICH, CastelloLich::new);
		COSTRUTTORI.put(TipoLocazione.CASTELLO_STREGA, CastelloStrega::new);
		COSTRUTTORI.put(TipoLocazione.CASTELLO_DRAGO, CastelloDrago::new);
	}

	private FabbricaLocazioni() {
	}

	/**
	 * Una nuova istanza della locazione di quel tipo
	 */
	public static Locazione crea(TipoLocazione tipo) {
		return COSTRUTTORI.get(tipo).get();
	}
}
