package com.threeamigos.foresta.missioni.leggende;

import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.motore.RegistroPersonaggi;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.List;

/**
 * L'armaiolo di una città qualsiasi racconta la leggenda di un oggetto leggendario (vedi LaLeggenda).
 */
public class LaLeggendaDellArmaiolo extends LaLeggenda {

	public LaLeggendaDellArmaiolo() {
		super(MissionePredefinita.LA_LEGGENDA_DELL_ARMAIOLO);
	}

	@Override
	protected boolean isPostoDelRacconto() {
		TipoLocazione tipo = Foresta.getLocazioneDelGruppo();
		return tipo != null && tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA
				&& !Foresta.isCittaDistrutta(tipo);
	}

	@Override
	protected String getNarratore() {
		return "l'armaiolo";
	}

	@Override
	protected String getNarratoreConNome() {
		TipoLocazione citta = Foresta.getLocazioneDelGruppo();
		if (citta == null || citta.getCategoriaLocazione() != CategoriaLocazione.CITTA) {
			return getNarratore();
		}
		return getNarratore() + ", " + RegistroPersonaggi.getResidente(PersonaggioNoto.ARMAIOLO, citta).getNome() + ",";
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
