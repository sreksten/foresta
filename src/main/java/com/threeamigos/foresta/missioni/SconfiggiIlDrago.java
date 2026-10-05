package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaGlobale;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;

public class SconfiggiIlDrago extends MissioneBase implements Missione {

	public SconfiggiIlDrago() {
		super(ClasseMissione.SCONFIGGI_IL_DRAGO);
		aggiungiMissione(new SconfiggiLaStrega());
		aggiungiMissione(new SconfiggiIlLich());
		aggiungiMissione(new SconfiggiIlMinotauroGigante());
		aggiungiMissione(new SconfiggiLIdra());
	}

	private static final String DRAGO_APPARSO = "DRAGO_APPARSO";

	@Override
	public String getNome() {
		return "Sconfiggi il Drago";
	}

	@Override
	public String getDescrizione() {
		StringBuilder sb = new StringBuilder();
		sb.append("La Foresta è minacciata da un temibile Drago. ");
		if (isDragoNonApparso()) {
			sb.append("Il Castello dove si trova è nascosto da un incantesimo. ");
			sb.append(GruppoGiocatore.getIstanza().getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			sb.append(" deve sconfiggere tutti i suoi alleati per poterlo affrontare!");			
		} else {
			sb.append("Occorre entrare nel suo Castello ed affrontarlo!");
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
				BusEventi.pubblica(new NotificaTestoParagrafo("L'incantesimo che nascondeva il castello del Drago " +
						"è svanito! La missione è quasi giunta al termine!"));
				setDragoApparso();
			}
		} else {
			GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
			if (gruppo.getClasseLocazioneCorrente() == TipoLocazione.CASTELLO_DRAGO && gruppo.getLocazioneCorrente().isCompleta()) {
				completaMissione();
				BusEventi.pubblica(new NotificaGlobale("Vittoria", "Il drago e' morto"));
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
		return "Qui sorgeva il castello del Drago.";
	}
}
