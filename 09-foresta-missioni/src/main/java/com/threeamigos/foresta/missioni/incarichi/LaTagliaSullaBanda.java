package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.testi.Testi;

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
		super(MissionePredefinita.LA_TAGLIA_SULLA_BANDA);
	}

	/**
	 * Il nome del capobanda, da missioni.txt.
	 */
	public String getCapobanda() {
		return parametro(CAPOBANDA, ProduttoreDiMissioniCasuale::nomeCapobanda);
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
		return Testi.formatta("missioni", "LaTagliaSullaBanda.t1", getCapobanda());
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return Testi.formatta("missioni", "LaTagliaSullaBanda.t2", getCapobanda(), getNomeCitta());
		}
		return Testi.formatta("missioni", "LaTagliaSullaBanda.t3", getNomeCitta(), getCapobanda());
	}

	@Override
	public String getRicordoDellaLocazione() {
		return Testi.formatta("missioni", "LaTagliaSullaBanda.t4", getCapobanda());
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
							BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "LaTagliaSullaBanda.s1", getCapobanda())));
						})
						.poi(CACCIA);
			case CACCIA:
				return combatti(this::getCovo, getBanda())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "LaTagliaSullaBanda.s2", getCapobanda(), getNomeCitta()))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(Testi.formatta("missioni", "LaTagliaSullaBanda.1", getCapobanda()))
				.parlaIlMandante(Testi.formatta("missioni", "LaTagliaSullaBanda.2", getRicompensa()))
				.parlaIlCapo(Testi.formatta("missioni", "LaTagliaSullaBanda.3"));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(Testi.formatta("missioni", "LaTagliaSullaBanda.4", getCapobanda()))
				.parlaIlMandante(Testi.formatta("missioni", "LaTagliaSullaBanda.5"))
				.parlaIlCapo(Testi.formatta("missioni", "LaTagliaSullaBanda.6"));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "LaTagliaSullaBanda.t5", getCapobanda(), getRicompensa());
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "LaTagliaSullaBanda.t6", getRicompensa());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
