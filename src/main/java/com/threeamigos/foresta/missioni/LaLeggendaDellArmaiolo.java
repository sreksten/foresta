package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.List;

/**
 * L'armaiolo di una città qualsiasi racconta la leggenda di un oggetto leggendario (vedi LaLeggenda).
 */
public class LaLeggendaDellArmaiolo extends LaLeggenda {

	public LaLeggendaDellArmaiolo() {
		super(ClasseMissione.LA_LEGGENDA_DELL_ARMAIOLO);
	}

	@Override
	protected boolean isPostoDelRacconto() {
		TipoLocazione tipo = GruppoGiocatore.getIstanza().getTipoLocazioneCorrente();
		return tipo != null && tipo.getCategoria() == CategoriaLocazione.CITTA
				&& !LineaTemporale.isCittaDistrutta(tipo);
	}

	@Override
	protected String getNarratore() {
		return "l'armaiolo";
	}

	@Override
	protected Racconto nuovoRacconto() {
		ScenaInCitta scena = ScenaInCitta.conArmaiolo();
		return new Racconto() {
			@Override
			public Racconto narra(String testo) {
				scena.parlaIlMandante(testo);
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
