package com.threeamigos.foresta.missioni.leggende;

import com.threeamigos.foresta.missioni.LocandaDelRacconto;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInLocanda;
import com.threeamigos.foresta.tipi.MissionePredefinita;

import java.util.List;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;

/**
 * Il locandiere di una locanda racconta la leggenda di un oggetto leggendario (vedi LaLeggenda), in una locanda del racconto (vedi
 * LocandaDelRacconto: dalla terza locanda conosciuta, alla seconda visita a una locanda nel bosco o dopo), al posto della recensione
 * della locanda. Nel sorteggio delle missioni concorre con Non sparate sul pianista e le missioni del sacerdote.
 */
public class LaLeggendaDelLocandiere extends LaLeggenda {

	public LaLeggendaDelLocandiere() {
		super(MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE);
	}

	@Override
	protected boolean isPostoDelRacconto() {
		return LocandaDelRacconto.isQui();
	}

	@Override
	protected String getNarratore() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_LEGGENDA_DEL_LOCANDIERE_NARRATORE");
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
