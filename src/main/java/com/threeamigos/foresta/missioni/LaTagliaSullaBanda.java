package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * In città c'è una taglia sul capo di una banda di hobgoblin che si nasconde in un bosco: la missione rivendica il
 * bosco, lo segna sulla mappa e ci mette la banda (vedi {@link MissioneAPassi#combatti}). Sconfitta tutta la banda,
 * si torna a riscuotere (vedi IncaricoInCitta). Il nome del capo viene da missioni.txt: la missione si ripete con
 * altri capi.
 */
public class LaTagliaSullaBanda extends IncaricoInCitta {

	private static final String CAPOBANDA = "CAPOBANDA";
	public static final int HOBGOBLIN = 3;
	private static final int RICOMPENSA = 30;
	private static final String COVO = "COVO";
	private static final String CACCIA = "CACCIA";

	public LaTagliaSullaBanda() {
		super(ClasseMissione.LA_TAGLIA_SULLA_BANDA);
	}

	/**
	 * Il nome del capobanda, da missioni.txt.
	 */
	public String getCapobanda() {
		return parametro(CAPOBANDA, ProduttoreDiTestiCasuale::nomeCapobanda);
	}

	/**
	 * La banda di hobgoblin, con il suo capo.
	 */
	public IncontroDiMissione getBanda() {
		return IncontroDiMissione.di(TipoPersonaggio.HOBGOBLIN, HOBGOBLIN).conCapo(getCapobanda());
	}

	@Override
	protected void allIncarico() {
		getCapobanda();
	}

	@Override
	public String getNome() {
		return "La taglia su " + getCapobanda();
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return getCapobanda() + " non darà più fastidio: torna a " + getNomeCitta() + " a riscuotere la taglia.";
		}
		return "A " + getNomeCitta() + " c'è una taglia su " + getCapobanda() + ", il capo di una banda di hobgoblin che si nasconde in un bosco segnato sulla mappa.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "In questo bosco si nascondeva la banda di " + getCapobanda() + ".";
	}

	/**
	 * Il bosco della banda, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case COVO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, TipoLocazione.BOSCO)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo("Il bosco dove si nasconde la banda di " + getCapobanda() + " è segnato sulla mappa."));
						})
						.poi(CACCIA);
			case CACCIA:
				return combatti(this::getCovo, getBanda())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(getCapobanda() + " e la sua banda non daranno più fastidio a nessuno: la taglia aspetta a "
								+ getNomeCitta() + ".")))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante("Avete visto l'avviso? C'è una taglia su " + getCapobanda() + ", il capo degli hobgoblin.")
				.parlaIlMandante("Lui e la sua banda si nascondono in un bosco qui vicino. " + RICOMPENSA + " monete a chi li sistema.")
				.parlaIlCapo("Dicci dov'è il bosco.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(getCapobanda() + " è andato? Finalmente si torna a dormire tranquilli.")
				.parlaIlMandante("Ecco la taglia, ve la siete guadagnata.")
				.parlaIlCapo("Era un tipo simpatico, in fondo. No, non è vero.");
	}

	@Override
	protected String testoAccettazione() {
		return "La taglia su " + getCapobanda() + " vale " + RICOMPENSA + " monete.";
	}

	@Override
	protected String testoRicompensa() {
		return "La taglia di " + RICOMPENSA + " monete è vostra.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
