package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.missioni.IlViaggioDellaMoglie;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.RegistroPersonaggi;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.List;
import java.util.Optional;

/**
 * Il bardo della locanda ringrazia il gruppo per aver accompagnato sua moglie (vedi IlViaggioDellaMoglie) e gli insegna
 * l'Urlo della Banshee. Scatta una volta sola, la prima volta che il gruppo entra in una locanda nel bosco dopo aver
 * <b>completato con successo</b> il viaggio della moglie; non è un intermezzo di ripiego. Da quel momento il Bardo e la
 * Cantastorie del gruppo conoscono il canto (vedi CantoDelBardo.isUrloDellaBansheeAppreso: lo dice l'essere scattato di
 * questo intermezzo).
 * <p>
 * Solo nel bosco: in città la missione si conclude nell'altra città, e passando subito dalla locanda il gruppo troverebbe
 * l'Urlo insieme al viaggio.
 */
public class IntermezzoUrloDellaBanshee implements Intermezzo {

	@Override
	public String getId() {
		return TipoIntermezzo.INTERMEZZO_URLO_DELLA_BANSHEE.name();
	}

	@Override
	public boolean deveScattare(MomentoIntermezzo momento) {
		return momento == MomentoIntermezzo.INIZIO_LOCAZIONE && Locande.momentoCoerenteConLocazioneCorrente(momento)
				&& getViaggioCompletato().isPresent();
	}

	/**
	 * Il viaggio della moglie, se il gruppo l'ha concluso con successo.
	 */
	private static Optional<IlViaggioDellaMoglie> getViaggioCompletato() {
		return RegistroMissioni.getMissioniCompletate().stream()
				.filter(IlViaggioDellaMoglie.class::isInstance)
				.map(IlViaggioDellaMoglie.class::cast)
				.findFirst();
	}

	@Override
	public List<PaginaIntermezzo> getPagine() {
		String moglie = RegistroPersonaggi.getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO).getNome();
		TipoLocazione arrivo = getViaggioCompletato().map(IlViaggioDellaMoglie::getCittaDiArrivo).orElse(null);
		String dove = arrivo == null ? "dall'altra parte della foresta" : Misc.conPreposizione("a", arrivo.getNomeProprio());
		return ScenaInLocanda.conBardo()
				.parlaIlBardo("Siete tornati! Mia moglie " + moglie + " mi ha scritto: è arrivata sana e salva " + dove + ", grazie a voi.")
				.parlaIlCapo("Era il minimo, dopo avervi riportato a casa.")
				.parlaIlBardo("Vi devo più di un grazie. Vi insegno il mio canto più segreto, che non ho mai insegnato a nessuno.")
				.parlaIlBardo("Si chiama l'Urlo della Banshee: un canto da far gelare il sangue. Chi lo ascolta si spaventa e perde la testa.")
				.parlaIlCapo("Ci sarà utile.")
				.parlaIlBardo("Chi di voi sa cantare lo imparerà in un attimo. Usatelo bene, e non fatelo sentire a mia moglie.")
				.getPagine();
	}
}
