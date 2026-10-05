package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * A Ruuna il Borgomastro chiede di recuperare un carico di derrate alimentari che una banda di Troll ha nascosto in
 * alcune rovine.
 */
public class RecuperaLeDerrateAlimentari extends MissioneRecuperaBersaglio implements Missione {

	public RecuperaLeDerrateAlimentari() {
		super(ClasseMissione.RECUPERA_LE_DERRATE_ALIMENTARI);
	}

	@Override
	public String getNome() {
		return "Recupera le derrate alimentari";
	}

	@Override
	public String getDescrizione() {
		if (!isBersaglioRecuperato()) {
			return "Il Borgomastro chiede aiuto: una banda di Troll ha fatto sparire un carico di derrate alimentari. " +
					"Offre " + AMMONTARE_RICOMPENSA + " monete come ricompensa.";
		} else {
			return "Torna in città per riconsegnare le derrate alimentari recuperate e ottenere la ricompensa di "
					+ AMMONTARE_RICOMPENSA + " monete.";
		}
	}

	@Override
	protected TipoLocazione getCittaFissa() {
		return TipoLocazione.CITTA_RUUNA;
	}

	@Override
	protected TipoLocazione getCovo() {
		return TipoLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI;
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Forestieri! Sono il Borgomastro di Ruuna, e la città ha bisogno di voi.")
				.parlaIlMandante("Una banda di Troll ha fatto sparire il carico di derrate per l'inverno.")
				.parlaIlCapo("Dove si sono cacciati?")
				.parlaIlMandante("Si nascondono in certe rovine qui intorno. Riportate le derrate e avrete " + AMMONTARE_RICOMPENSA + " monete.")
				.parlaIlCapo("Ai Troll ci pensiamo noi.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Le derrate! Quest'inverno Ruuna non patirà la fame.")
				.parlaIlMandante("Ecco le " + AMMONTARE_RICOMPENSA + " monete, come promesso.")
				.parlaIlCapo("Tenete d'occhio i Troll, la prossima volta.");
	}

	@Override
	protected String testoAccettazione() {
		return GruppoGiocatore.getIstanza().getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
				+ " promette al Borgomastro di recuperare le derrate rubate dai Troll, nascosti in alcune rovine. Ricompensa: "
				+ AMMONTARE_RICOMPENSA + " monete.";
	}

	@Override
	protected String testoRecupero() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return "Le derrate alimentari sono state recuperate. " + gruppo.chiMaiuscolo() + " può tornare in città per reclamare la ricompensa.";
	}

	@Override
	protected String testoRicompensa() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return "Il Borgomastro accoglie " + gruppo.chi() + ", che ha recuperato le derrate alimentari. La ricompensa promessa viene saldata: "
				+ AMMONTARE_RICOMPENSA + " monete.";
	}

	@Override
	protected String testoCittaDistrutta() {
		return "Ruuna è stata distrutta: le derrate alimentari non potranno più essere consegnate al Borgomastro.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "Fra queste rovine i Troll nascondevano le derrate di Ruuna.";
	}
}
