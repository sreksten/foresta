package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

/**
 * Che cosa scrive il riquadro del combattimento sotto il personaggio che agisce: "Attacca la viverna 2", "Intona il Canto di Valore"...
 */
final class TestoAzioneInCombattimento {

	private TestoAzioneInCombattimento() {
	}

	/**
	 * L'azione scelta con la tattica. Il bersaglio può mancare (un'azione sul gruppo, o senza un bersaglio solo).
	 */
	static String di(TipoAzione azione, VistaPersonaggio attore, VistaPersonaggio bersaglio) {
		String su = bersaglio == null ? "" : " " + nome(bersaglio);
		switch (azione) {
			case ATTACCO_FISICO:
				return bersaglio == null ? "Attacca" : "Attacca" + su;
			case INCANTESIMO_DI_DANNO:
			case INCANTESIMO_DEL_REPERTORIO:
				return bersaglio == null ? "Lancia un incantesimo" : "Lancia un incantesimo contro" + su;
			case DARDO_ARCANO:
				return bersaglio == null ? "Lancia il Dardo Arcano" : "Lancia il Dardo Arcano contro" + su;
			case RESURREZIONE:
				return bersaglio == null ? "Lancia la Resurrezione" : "Resuscita" + su;
			case ALBA_SACRA:
				return "Invoca l'Alba Sacra";
			case CANTO_CURATIVO:
				return bersaglio == null ? "Intona il Canto Curativo" : "Intona il Canto Curativo per" + su;
			case CANTO_IPNOTICO:
				return "Intona il Canto Ipnotico";
			case CANTO_DI_VALORE:
				return "Intona il Canto di Valore";
			case URLO_DELLA_BANSHEE:
				return "Lancia l'Urlo della Banshee";
			case SCACCIARE_I_NON_MORTI:
				return "Scaccia i non morti";
			case IMPOSIZIONE_DELLE_MANI:
				return bersaglio == null ? "Impone le mani" : "Impone le mani su" + su;
			case RADICI:
				return "Fa spuntare le radici";
			case SCIAME:
				return "Evoca lo sciame";
			case PELLE_DI_CORTECCIA:
				return "Evoca la pelle di corteccia";
			case RIGENERAZIONE:
				return bersaglio == null ? "Invoca la rigenerazione" : "Fa rigenerare" + su;
			case POZIONE_SALUTE:
			case POZIONE_SALUTE_GRANDE:
			case POZIONE_MAGIA:
			case POZIONE_MAGIA_GRANDE:
				return (bersaglio == null || bersaglio == attore ? "Beve una " : "Dà a" + su + " una ") + azione.getDescrizione();
			case CAMBIA_ARMA:
				return "Cambia arma";
			default:
				// Gli incantesimi di una classe precisa: "Lancia Fulmine contro la viverna"
				return "Lancia " + azione.getDescrizione() + (bersaglio == null ? "" : " contro" + su);
		}
	}

	/**
	 * Quando non c'è una decisione della tattica (un'azione fuori dalla tattica): attacca il bersaglio, se c'è.
	 */
	static String predefinito(VistaPersonaggio bersaglio) {
		return bersaglio == null ? "Agisce" : "Attacca " + nome(bersaglio);
	}

	private static String nome(VistaPersonaggio personaggio) {
		return personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
	}
}
