package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tools.Misc;

/**
 * In città una donna chiede di accompagnare suo fratello Anselmo, un pellegrino, fino a un tempio nella foresta: la
 * missione rivendica il tempio e lo segna sulla mappa, Anselmo viaggia con il gruppo come ospite (vedi
 * {@link MissioneAPassi#prendiInScorta}) e se ne separa arrivati al tempio; poi si torna dalla sorella a riscuotere
 * (vedi IncaricoInCitta).
 */
public class IlPellegrino extends IncaricoInCitta {

	public static final String ANSELMO = "Anselmo";
	private static final int RICOMPENSA = 25;
	private static final String META = "META";
	private static final String PARTENZA = "PARTENZA";
	private static final String VIAGGIO = "VIAGGIO";

	public IlPellegrino() {
		super(ClasseMissione.IL_PELLEGRINO);
	}

	/**
	 * Ha un personaggio con un nome proprio: finché il nome non viene da una grammatica, la stessa storia non si
	 * ripete.
	 */
	@Override
	protected boolean isRipetibile() {
		return false;
	}

	@Override
	public String getNome() {
		return "Il pellegrino";
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (RITORNO.equals(passo)) {
			return "Anselmo è arrivato " + Misc.conPreposizione("a", getNomeDelTempio()) + ": torna da sua sorella a " + getNomeCitta() + ".";
		}
		return "Accompagna il pellegrino Anselmo fino " + Misc.conPreposizione("a", getNomeDelTempio()) + ", segnato sulla mappa.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "In questo tempio venne a pregare il pellegrino Anselmo.";
	}

	/**
	 * Il tempio della meta, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getTempio() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il nome del tempio della meta, con l'articolo; "il tempio" finché la missione non l'ha trovato.
	 */
	private String getNomeDelTempio() {
		return getTempio() == null ? "il tempio" : Tempio.getNome(getTempio());
	}

	@Override
	protected String primoPassoDelCompito() {
		return META;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case META:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, ClassiLocazione.TEMPIO)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getTempio());
							BusEventi.pubblica(new NotificaTestoParagrafo(Misc.inizialeMaiuscola(getNomeDelTempio())
									+ ", dove vuole andare Anselmo, è segnato sulla mappa."));
						})
						.poi(PARTENZA);
			case PARTENZA:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, this::nellaCitta, ANSELMO).poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, this::getTempio)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Arrivato " + Misc.conPreposizione("a", getNomeDelTempio()) + ", Anselmo vi ringrazia e si mette a pregare. Sua sorella aspetta notizie a "
								+ getNomeCitta() + ".")))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Scusate, andate verso la foresta? Mio fratello Anselmo vuole andare in pellegrinaggio a un tempio.")
				.parlaIlMandante("Da solo non ci arriva vivo. Accompagnatelo, e al ritorno vi darò " + RICOMPENSA + " monete.")
				.parlaIlCapo("Sa almeno tenere in mano una spada?")
				.parlaIlMandante("Sa pregare. Molto.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Anselmo è arrivato " + Misc.conPreposizione("a", getNomeDelTempio()) + "? Che il cielo vi benedica!")
				.parlaIlMandante("Ecco le " + RICOMPENSA + " monete.")
				.parlaIlCapo("Ha pregato anche per noi, speriamo.");
	}

	@Override
	protected String testoAccettazione() {
		return "Anselmo viaggerà con il gruppo fino al tempio. Ricompensa al ritorno: " + RICOMPENSA + " monete.";
	}

	@Override
	protected String testoRicompensa() {
		return "La sorella di Anselmo paga le " + RICOMPENSA + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
