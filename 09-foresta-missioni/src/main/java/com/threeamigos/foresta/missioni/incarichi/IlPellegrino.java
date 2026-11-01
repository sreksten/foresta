package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Testi;

/**
 * In città una donna chiede di accompagnare suo fratello, un pellegrino, fino a un tempio nella foresta: la
 * missione rivendica il tempio e lo segna sulla mappa, il pellegrino viaggia con il gruppo come ospite (vedi
 * {@link MissioneAPassi#prendiInScorta}) e se ne separa arrivati al tempio; poi si torna dalla sorella a riscuotere
 * (vedi IncaricoInCitta). Il tempio è sicuro: all'arrivo niente avversari né oggetti a caso (vedi
 * RegistroMissioni.sopprimiContenutoLocazione).
 */
public class IlPellegrino extends IncaricoInCitta {

	private static final String PELLEGRINO = "PELLEGRINO";
	private static final int RICOMPENSA = 25;
	private static final String META = "META";
	private static final String PARTENZA = "PARTENZA";
	private static final String VIAGGIO = "VIAGGIO";

	public IlPellegrino() {
		super(MissionePredefinita.IL_PELLEGRINO);
	}

	/**
	 * Il nome del pellegrino, da missioni.txt: la missione si ripete con altri pellegrini.
	 */
	public String getPellegrino() {
		return parametro(PELLEGRINO, ProduttoreDiMissioniCasuale::nomePellegrino);
	}

	@Override
	protected void allIncarico() {
		getPellegrino();
	}

	@Override
	public String getNome() {
		return Testi.formatta("missioni", "IlPellegrino.t1");
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (RITORNO.equals(passo)) {
			return Testi.formatta("missioni", "IlPellegrino.t2", getPellegrino(), Lingue.corrente().conPreposizione(Preposizione.A, getNomeDelTempio()), getNomeCitta());
		}
		return Testi.formatta("missioni", "IlPellegrino.t3", getPellegrino(), Lingue.corrente().conPreposizione(Preposizione.A, getNomeDelTempio()));
	}

	@Override
	public String getRicordoDellaLocazione() {
		return Testi.formatta("missioni", "IlPellegrino.t4", getPellegrino());
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
		return getTempio() == null ? "il tempio" : Foresta.nomeDelTempio(getTempio());
	}

	private boolean nelTempio() {
		return getTempio() != null && getTempio().equals(Foresta.getPosizioneDelGruppo());
	}

	@Override
	protected String primoPassoDelCompito() {
		return META;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case META:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, TipoLocazione.TEMPIO)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getTempio());
							BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "IlPellegrino.s1", Lingue.corrente().inizialeMaiuscola(getNomeDelTempio()), getPellegrino())));
						})
						.poi(PARTENZA);
			case PARTENZA:
				return prendiInScorta(MomentoControllo.IN_LOCAZIONE, this::nellaCitta, getPellegrino()).poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, this::getTempio)
						.aOgniControllo(() -> {
							if (nelTempio()) {
								RegistroMissioni.sopprimiContenutoLocazione(getTempio());
							}
						})
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "IlPellegrino.s2", Lingue.corrente().conPreposizione(Preposizione.A, getNomeDelTempio()), getPellegrino(), getNomeCitta()))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "IlPellegrino.1", getPellegrino()))
				.parlaIlMandante(Testi.formatta("missioni", "IlPellegrino.2", getRicompensa()))
				.parlaIlCapo(Testi.formatta("missioni", "IlPellegrino.3"))
				.parlaIlMandante(Testi.formatta("missioni", "IlPellegrino.4"));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "IlPellegrino.5", getPellegrino(), Lingue.corrente().conPreposizione(Preposizione.A, getNomeDelTempio())))
				.parlaIlMandante(Testi.formatta("missioni", "IlPellegrino.6", getRicompensa()))
				.parlaIlCapo(Testi.formatta("missioni", "IlPellegrino.7"));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "IlPellegrino.t5", getPellegrino(), getRicompensa());
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "IlPellegrino.t6", getPellegrino(), getRicompensa());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
