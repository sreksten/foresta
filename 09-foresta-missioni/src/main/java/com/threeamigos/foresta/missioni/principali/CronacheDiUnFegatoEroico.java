package com.threeamigos.foresta.missioni.principali;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneBase;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;

/**
 * Un boccale in ogni locanda cittadina. Una tappa per città, e la missione si chiude
 * da sé quando le tappe sono tutte concluse.
 */
public class CronacheDiUnFegatoEroico extends MissioneBase {

	public CronacheDiUnFegatoEroico() {
		super(MissionePredefinita.CRONACHE_DI_UN_FEGATO_EROICO);
		// Una tappa per città: se il mondo ne guadagnasse una, la missione la seguirebbe
		for (TipoLocazione tipoLocazione : TipoLocazione.values()) {
			if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CITTA) {
				VisitaLocanda visitaLocanda = new VisitaLocanda();
				visitaLocanda.setCitta(tipoLocazione);
				aggiungiMissione(visitaLocanda);
			}
		}
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CRONACHE_DI_UN_FEGATO_EROICO_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CRONACHE_DI_UN_FEGATO_EROICO_DESCRIZIONE");
	}

	@Override
	public void controllaPreLocazione() {
		if (!isAttiva() && Foresta.getLocazioneDelGruppo().getCategoriaLocazione() == CategoriaLocazione.CITTA) {
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("CRONACHE_DI_UN_FEGATO_EROICO_INIZIO", "PARAM_DESCRIZIONE", getDescrizione(), "PARAM_NOME", getNome())));
			attivaMissione();
		}
	}

	@Override
	public void controllaInLocazione() {
		// Le tappe falliscono nel controllo di inizio locazione, che viene prima di questo nello stesso turno
		fallisceSeUnaTappaEFallita();
	}

	/**
	 * Se una tappa e' fallita (la sua citta' e' stata distrutta) le Cronache non si possono piu' completare.
	 *
	 * @return vero se la missione e' fallita adesso
	 */
	private boolean fallisceSeUnaTappaEFallita() {
		if (!isAttiva() || isCompleta() || isFallita() || getMissioniSecondarie().stream().noneMatch(Missione::isFallita)) {
			return false;
		}
		BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("CRONACHE_DI_UN_FEGATO_EROICO_INCOMPIUTE")));
		fallisciMissione();
		return true;
	}

	@Override
	public void controllaPostLocazione() {
		if (isCompleta() || isFallita() || getMissioniSecondarie().isEmpty()) {
			return;
		}
		if (fallisceSeUnaTappaEFallita()) {
			return;
		}
		// Le tappe vengono controllate prima di qui (OrdineVisita.FIGLI_PRIMA), quindi
		// l'ultima bevuta chiude anche la missione, nello stesso giro
		if (getMissioniSecondarie().stream().allMatch(Missione::isCompleta)) {
			completaMissione();
			BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("CRONACHE_DI_UN_FEGATO_EROICO_COMPLETE")));
		}
	}
}
