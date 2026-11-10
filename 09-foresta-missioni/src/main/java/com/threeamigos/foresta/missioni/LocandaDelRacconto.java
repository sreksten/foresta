package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * La locanda in cui qualcuno racconta una storia al gruppo: dalla terza locanda in cui il gruppo è entrato in tutta la partita (nel bosco
 * o in città, contate una volta ciascuna), alla seconda visita a una locanda nel bosco o a una delle successive. Alla prima visita c'è la
 * scena del benvenuto (IntermezzoLocandaPrimaVisita); alla seconda la storia prende il posto della recensione, che è un intermezzo di
 * ripiego (IntermezzoLocandaVisitaSuccessiva) e quindi aspetta da sé.
 * <p>
 * Qui si offrono, nel sorteggio delle missioni (SceltaDegliIncarichi), Non sparate sul pianista, la leggenda del locandiere e le
 * missioni di recupero del sacerdote: ne parte una per visita, a caso.
 */
public final class LocandaDelRacconto {

	/**
	 * Le locande in cui il gruppo deve essere già entrato prima di quella del racconto: si comincia dalla terza.
	 */
	public static final int LOCANDE_PRECEDENTI = 2;

	/**
	 * Le visite precedenti a questa locanda (sono già contate quando si entra): dalla seconda visita.
	 */
	public static final int VISITE_PRECEDENTI = 1;

	private LocandaDelRacconto() {
	}

	/**
	 * Se il gruppo è in una locanda del racconto: una locanda nel bosco già visitata, quando il gruppo ha già conosciuto almeno altre due
	 * locande.
	 */
	public static boolean isQui() {
		if (Foresta.getLocazioneDelGruppo() != TipoLocazione.LOCANDA) {
			return false;
		}
		String visite = Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo()).ottieniProprieta(Locande.LOCANDA_VISITE);
		return visite != null && Integer.parseInt(visite) >= VISITE_PRECEDENTI && locandeConosciute() > LOCANDE_PRECEDENTI;
	}

	/**
	 * In quante locande diverse, nel bosco o in città, il gruppo è entrato in tutta la partita.
	 */
	public static int locandeConosciute() {
		int locande = 0;
		for (int y = 0; y < Foresta.getDimensioneY(); y++) {
			for (int x = 0; x < Foresta.getDimensioneX(); x++) {
				CoordinateMD coordinate = new CoordinateMD(x, y);
				TipoLocazione tipo = Foresta.getLocazione(coordinate);
				if (tipo == TipoLocazione.LOCANDA || tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
					String visite = Foresta.getLocazioneMD(coordinate).ottieniProprieta(Locande.LOCANDA_VISITE);
					locande += visite != null && Integer.parseInt(visite) > 0 ? 1 : 0;
				}
			}
		}
		return locande;
	}
}
