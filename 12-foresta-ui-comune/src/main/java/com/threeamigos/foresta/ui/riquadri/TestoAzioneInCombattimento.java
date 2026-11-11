package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

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
		String su = bersaglio == null ? null : nome(bersaglio);
		switch (azione) {
			case ATTACCO_FISICO:
				return conBersaglio("AZIONE_ATTACCO", su);
			case INCANTESIMO_DI_DANNO:
			case INCANTESIMO_DEL_REPERTORIO:
				return conBersaglio("AZIONE_INCANTESIMO", su);
			case DARDO_ARCANO:
				return conBersaglio("AZIONE_DARDO_ARCANO", su);
			case MARTELLO_SACRO:
				return conBersaglio("AZIONE_MARTELLO_SACRO", sopra(su));
			case ATTACCO_ALLE_SPALLE:
				return conBersaglio("AZIONE_ATTACCO_ALLE_SPALLE", su);
			case RESURREZIONE:
				return conBersaglio("AZIONE_RESURREZIONE", su);
			case ALBA_SACRA:
				return TestiDellaUI.get("AZIONE_ALBA_SACRA");
			case LUCE_DIVINA:
				return conBersaglio("AZIONE_LUCE_DIVINA", su);
			case CANTO_CURATIVO:
				return conBersaglio("AZIONE_CANTO_CURATIVO", su);
			case CANTO_IPNOTICO:
				return TestiDellaUI.get("AZIONE_CANTO_IPNOTICO");
			case CANTO_DI_VALORE:
				return TestiDellaUI.get("AZIONE_CANTO_DI_VALORE");
			case URLO_DELLA_BANSHEE:
				return TestiDellaUI.get("AZIONE_URLO_DELLA_BANSHEE");
			case SCACCIARE_I_NON_MORTI:
				return TestiDellaUI.get("AZIONE_SCACCIARE_I_NON_MORTI");
			case IMPOSIZIONE_DELLE_MANI:
				return conBersaglio("AZIONE_IMPOSIZIONE_DELLE_MANI", sopra(su));
			case RADICI:
				return TestiDellaUI.get("AZIONE_RADICI");
			case SCIAME:
				return TestiDellaUI.get("AZIONE_SCIAME");
			case PELLE_DI_CORTECCIA:
				return TestiDellaUI.get("AZIONE_PELLE_DI_CORTECCIA");
			case RIGENERAZIONE:
				return conBersaglio("AZIONE_RIGENERAZIONE", su);
			case POZIONE_SALUTE:
			case POZIONE_SALUTE_GRANDE:
			case POZIONE_MAGIA:
			case POZIONE_MAGIA_GRANDE:
			case POZIONE_PANACEA:
				return bersaglio == null || bersaglio == attore
						? TestiDellaUI.get("AZIONE_BEVE_POZIONE", azione.getDescrizione())
						: TestiDellaUI.get("AZIONE_DA_POZIONE", su, azione.getDescrizione());
			case CAMBIA_ARMA:
				return TestiDellaUI.get("AZIONE_CAMBIA_ARMA");
			default:
				// Gli incantesimi di una classe precisa: "Lancia Fulmine contro la viverna"
				return bersaglio == null ? TestiDellaUI.get("AZIONE_LANCIA", azione.getDescrizione())
						: TestiDellaUI.get("AZIONE_LANCIA_SU", azione.getDescrizione(), su);
		}
	}

	// La frase di quell'azione: con il bersaglio ({0}, già con l'articolo) se c'è, senza altrimenti (chiave con _SU)
	private static String conBersaglio(String chiave, String bersaglio) {
		return bersaglio == null ? TestiDellaUI.get(chiave) : TestiDellaUI.get(chiave + "_SU", bersaglio);
	}

	// Il bersaglio di un gesto che cade su di lui, con la preposizione: "sull'arpia", "on the harpy"
	private static String sopra(String bersaglio) {
		return bersaglio == null ? null : Lingue.corrente().conPreposizione(Preposizione.SU, bersaglio);
	}

	/**
	 * Quando non c'è una decisione della tattica (un'azione fuori dalla tattica): attacca il bersaglio, se c'è.
	 */
	static String predefinito(VistaPersonaggio bersaglio) {
		return bersaglio == null ? TestiDellaUI.get("AZIONE_AGISCE") : TestiDellaUI.get("AZIONE_ATTACCO_SU", nome(bersaglio));
	}

	private static String nome(VistaPersonaggio personaggio) {
		return personaggio.getNome(VistaPersonaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
	}
}
