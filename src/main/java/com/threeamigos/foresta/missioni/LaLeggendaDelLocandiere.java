package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.List;

/**
 * Il locandiere di una locanda racconta la leggenda di un oggetto leggendario (vedi LaLeggenda), ma solo dalla
 * terza visita a quella locanda in poi: alla prima e alla seconda c'è già l'intermezzo della locanda.
 */
public class LaLeggendaDelLocandiere extends LaLeggenda {

	// Le visite precedenti alla locanda sono già contate quando si entra
	private static final int VISITE_PRECEDENTI = 2;

	public LaLeggendaDelLocandiere() {
		super(ClasseMissione.LA_LEGGENDA_DEL_LOCANDIERE);
	}

	@Override
	protected boolean isPostoDelRacconto() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (gruppo.getClasseLocazioneCorrente() != TipoLocazione.LOCANDA) {
			return false;
		}
		String visite = Foresta.getLocazioneMD(gruppo.getCoordinate()).ottieniProprieta(Locanda.LOCANDA_VISITE);
		return visite != null && Integer.parseInt(visite) >= VISITE_PRECEDENTI;
	}

	@Override
	protected String getNarratore() {
		return "il locandiere";
	}

	@Override
	protected Racconto nuovoRacconto() {
		ScenaInLocanda scena = ScenaInLocanda.conLocandiere();
		return new Racconto() {
			@Override
			public Racconto narra(String testo) {
				scena.parlaIlLocandiere(testo);
				return this;
			}

			@Override
			public Racconto parlaIlCapo(String testo) {
				scena.parlaIlCapo(testo);
				return this;
			}

			@Override
			public List<PaginaIntermezzo> getPagine() {
				return scena.getPagine();
			}
		};
	}
}
