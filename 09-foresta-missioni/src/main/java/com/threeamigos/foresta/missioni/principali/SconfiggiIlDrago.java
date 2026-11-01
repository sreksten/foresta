package com.threeamigos.foresta.missioni.principali;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaGlobale;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneBase;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Testi;

/**
 * La missione principale: sconfiggere il Drago. Il suo castello è nascosto da un incantesimo finché non sono stati sconfitti tutti i suoi alleati: le missioni
 * figlie ({@link SconfiggiLaStrega}, {@link SconfiggiIlLich}, {@link SconfiggiIlMinotauroGigante}, {@link SconfiggiLIdra}) vanno sconfitte prima.
 */
public class SconfiggiIlDrago extends MissioneBase implements Missione {

	public SconfiggiIlDrago() {
		super(MissionePredefinita.SCONFIGGI_IL_DRAGO);
		aggiungiMissione(new SconfiggiLaStrega());
		aggiungiMissione(new SconfiggiIlLich());
		aggiungiMissione(new SconfiggiIlMinotauroGigante());
		aggiungiMissione(new SconfiggiLIdra());
	}

	private static final String DRAGO_APPARSO = "DRAGO_APPARSO";

	@Override
	public String getNome() {
		return Testi.formatta("missioni", "SconfiggiIlDrago.t1");
	}

	@Override
	public String getDescrizione() {
		StringBuilder sb = new StringBuilder();
		sb.append(Testi.formatta("missioni", "SconfiggiIlDrago.s1"));
		if (isDragoNonApparso()) {
			sb.append(Testi.formatta("missioni", "SconfiggiIlDrago.s2"));
			sb.append(GruppoGiocatore.getIstanza().getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			sb.append(Testi.formatta("missioni", "SconfiggiIlDrago.s3"));			
		} else {
			sb.append(Testi.formatta("missioni", "SconfiggiIlDrago.s4"));
		}
		return sb.toString();
	}

	@Override
	public void controllaPreLocazione() {
		if (!isAttiva()) {
			BusEventi.pubblica(new NotificaTestoParagrafo(getDescrizione()));
			attivaMissione();
		}
	}
	
	@Override
	public void controllaInLocazione() {
		// Non succede niente
	}

	@Override
	public void controllaPostLocazione() {
		if (isDragoNonApparso() && castelliDistrutti()) {
			// Come gli altri castelli, anche quello del Drago sorge su un bosco rivendicato dalla missione; se non ce
			// n'è nessuno libero si riprova alla fine della prossima locazione
			if (RegistroMissioni.rivendicaPerLocazioneUnica(TipoLocazione.CASTELLO_DRAGO, TipoLocazione.BOSCO, this) != null) {
				BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "SconfiggiIlDrago.s5")));
				setDragoApparso();
			}
		} else {
			GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
			if (Foresta.getLocazioneDelGruppo() == TipoLocazione.CASTELLO_DRAGO && RegistroMissioni.isLocazioneCompleta()) {
				completaMissione();
				BusEventi.pubblica(new NotificaGlobale("Vittoria", Testi.formatta("missioni", "SconfiggiIlDrago.s6")));
				LineaTemporale.setGiocoFinito(true);
			}
		}
	}

	/**
	 * I quattro alleati del Drago sono sconfitti quando le rispettive missioni sono complete.
	 * Restano sempre nell'albero (vedi RegistroMissioni.getMissioniCompletate), quindi la
	 * verifica regge anche dopo un caricamento e non dipende da quando/se una missione
	 * ha già costruito la propria locazione.
	 */
	private boolean castelliDistrutti() {
		return getMissioniSecondarie().stream().allMatch(Missione::isCompleta);
	}

	@Override
	public boolean isPrimaria() {
		return true;
	}
	
	public boolean isDragoNonApparso() {
		return md.ottieniProprieta(DRAGO_APPARSO) == null;
	}
	
	public void setDragoApparso() {
		md.aggiungiProprieta(DRAGO_APPARSO, AFFERMATIVO);
	}

	@Override
	public String getRicordoDellaLocazione() {
		return Testi.formatta("missioni", "SconfiggiIlDrago.t2");
	}
}
