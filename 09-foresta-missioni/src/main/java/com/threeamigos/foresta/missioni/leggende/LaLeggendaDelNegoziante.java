package com.threeamigos.foresta.missioni.leggende;

import java.util.Arrays;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.mondo.RegistroPersonaggi;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.List;

/**
 * Un negoziante di una città qualsiasi (l'armaiolo, l'alchimista, il venditore di pergamene o l'incantatore, scelto a caso quando la
 * leggenda si offre) racconta la leggenda di un oggetto leggendario (vedi LaLeggenda).
 */
public class LaLeggendaDelNegoziante extends LaLeggenda {

	private static final String ASPETTO = "ASPETTO";
	private static final List<AspettoDelMandante> NEGOZIANTI = Arrays.asList(AspettoDelMandante.ARMAIOLO, AspettoDelMandante.ALCHIMISTA,
			AspettoDelMandante.VENDITORE_DI_PERGAMENE, AspettoDelMandante.INCANTATORE);

	public LaLeggendaDelNegoziante() {
		super(MissionePredefinita.LA_LEGGENDA_DEL_NEGOZIANTE);
	}

	/**
	 * Chi racconta, scelto a caso la prima volta che serve e poi sempre lo stesso.
	 */
	private AspettoDelMandante getAspetto() {
		return AspettoDelMandante.valueOf(parametro(ASPETTO, () -> NEGOZIANTI.get(Dado.tira(0, NEGOZIANTI.size() - 1)).name()));
	}

	@Override
	protected boolean isPostoDelRacconto() {
		TipoLocazione tipo = Foresta.getLocazioneDelGruppo();
		return tipo != null && tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA
				&& !Foresta.isCittaDistrutta(tipo);
	}

	@Override
	protected String getNarratore() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("RUOLO_" + getAspetto().name());
	}

	@Override
	protected String getNarratoreConNome() {
		TipoLocazione citta = Foresta.getLocazioneDelGruppo();
		if (citta == null || citta.getCategoriaLocazione() != CategoriaLocazione.CITTA) {
			return getNarratore();
		}
		return getNarratore() + ", " + RegistroPersonaggi.getResidente(PersonaggioNoto.valueOf(getAspetto().name()), citta).getNome() + ",";
	}

	@Override
	protected Racconto nuovoRacconto() {
		ScenaInCitta scena = getAspetto().nuovaScena();
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
