package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * A Fleena un uomo chiede di recuperare il medaglione che una banda di ladri gli ha rubato e nascosto in una grotta.
 */
public class RecuperaIlMedaglione extends MissioneRecuperaBersaglio implements Missione {

	public RecuperaIlMedaglione() {
		super(ClasseMissione.RECUPERA_IL_MEDAGLIONE);
	}

	@Override
	public String getNome() {
		return "Recupera il medaglione";
	}

	@Override
	public String getDescrizione() {
		if (isBersaglioRecuperato()) {
			return "Torna in città per riconsegnare il medaglione rubato in cambio della ricompensa.";
		} else {
			return "Un uomo ti ha chiesto di recuperare il suo prezioso medaglione rubato da una banda di ladri.";
		}
	}

	@Override
	protected TipoLocazione getCittaFissa() {
		return TipoLocazione.CITTA_FLEENA;
	}

	@Override
	protected TipoLocazione getCovo() {
		return TipoLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE;
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Viandanti, vi prego, aiutatemi!")
				.parlaIlMandante("Una banda di ladri mi ha rubato il medaglione di famiglia, l'unico ricordo di mia madre.")
				.parlaIlCapo("Sai dove si nascondono?")
				.parlaIlMandante("Hanno il covo in una grotta poco lontano da qui. Riportatemelo e vi darò " + AMMONTARE_RICOMPENSA + " monete.")
				.parlaIlCapo("Consideralo già al tuo collo.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Il mio medaglione! Siete tornati davvero!")
				.parlaIlMandante("Ecco le " + AMMONTARE_RICOMPENSA + " monete promesse, e tutta la mia gratitudine.")
				.parlaIlCapo("È stato un piacere. Più o meno.");
	}

	@Override
	protected String testoAccettazione() {
		return GruppoGiocatore.getIstanza().getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
				+ " promette di recuperare il medaglione rubato dai ladri, che hanno il loro covo in una grotta. Ricompensa: "
				+ AMMONTARE_RICOMPENSA + " monete.";
	}

	@Override
	protected String testoRecupero() {
		return "Il medaglione è stato recuperato. Puoi tornare in città per reclamare la ricompensa.";
	}

	@Override
	protected String testoRicompensa() {
		return "L'uomo è felicissimo di riavere il suo medaglione in cambio delle " + AMMONTARE_RICOMPENSA + " monete promesse.";
	}

	@Override
	protected String testoCittaDistrutta() {
		return "Fleena è stata distrutta: il medaglione non potrà più essere restituito al suo proprietario.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "In questa grotta i ladri nascondevano il medaglione rubato.";
	}
}
