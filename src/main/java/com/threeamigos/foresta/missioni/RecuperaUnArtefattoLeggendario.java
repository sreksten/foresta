package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.ArtefattoLeggendario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * In una città l'armaiolo racconta la leggenda di un artefatto leggendario (vedi {@link ArtefattoLeggendario}),
 * e il gruppo decide di andarlo a prendere: potrebbe servire contro il Drago. La missione pesca un leggendario che
 * nessun'altra missione ha ancora, e lo fa custodire da un tempio nuovo, sorto su un bosco e segnato sulla mappa.
 * Sconfitte le viverne e raccolto l'artefatto, la missione è completa.
 * <ol>
 * <li>INCARICO, a inizio locazione, nella città: si pesca il leggendario e parte l'intermezzo con l'armaiolo;</li>
 * <li>ACCETTAZIONE, in locazione, nella città: sorge il tempio e la missione si attiva, così l'avviso arriva
 * dopo l'intermezzo;</li>
 * <li>RECUPERO, a fine locazione, nel tempio, quando l'artefatto non c'è più: la missione si completa.</li>
 * </ol>
 */
public abstract class RecuperaUnArtefattoLeggendario extends MissioneAPassi {

	private static final String INCARICO = "INCARICO";
	private static final String ACCETTAZIONE = "ACCETTAZIONE";
	private static final String RECUPERO = "RECUPERO";
	private static final String LEGGENDARIO = "LEGGENDARIO";

	protected RecuperaUnArtefattoLeggendario(ClasseMissione classe) {
		super(classe);
	}

	/**
	 * La città dell'armaiolo che racconta la leggenda.
	 */
	protected abstract ClassiLocazione getCitta();

	/**
	 * Il leggendario di questa missione, o null finché non l'ha pescato.
	 */
	public final ArtefattoLeggendario getLeggendario() {
		String nome = ottieniProprieta(LEGGENDARIO);
		return nome == null ? null : ArtefattoLeggendario.valueOf(nome);
	}

	@Override
	public String getNome() {
		ArtefattoLeggendario leggendario = getLeggendario();
		return leggendario == null ? "Una leggenda dell'armaiolo" : "Recupera " + leggendario.getNomeBreve();
	}

	@Override
	public String getDescrizione() {
		ArtefattoLeggendario leggendario = getLeggendario();
		if (leggendario == null) {
			return "";
		}
		return "Secondo la leggenda raccontata dall'armaiolo, un nido di viverne custodisce " + leggendario.getNomeBreve()
				+ " in un tempio segnato sulla mappa. Potrebbe servire contro il Drago.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		ArtefattoLeggendario leggendario = getLeggendario();
		return leggendario == null ? null : "Qui le viverne custodivano " + leggendario.getNomeBreve() + ".";
	}

	@Override
	protected String passoIniziale() {
		return INCARICO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case INCARICO:
				return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> nellaCitta() && (getLeggendario() != null || pesca() != null))
						.esegui(() -> {
							if (getLeggendario() == null) {
								aggiungiProprieta(LEGGENDARIO, pesca().name());
							}
						})
						.conIntermezzo(MomentoIntermezzo.INIZIO_LOCAZIONE, () -> scenaDellaLeggenda().getPagine())
						.poi(ACCETTAZIONE);
			case ACCETTAZIONE:
				// Il tempio sorge su un bosco che la missione rivendica; se non ce n'è nessuno libero si riprova
				return Passo.quando(MomentoControllo.IN_LOCAZIONE,
								() -> nellaCitta() && RegistroMissioni.cerca(ClassiLocazione.BOSCO, this).isPresent())
						.esegui(() -> {
							RegistroArtefatti.custodisciInUnTempioNuovo(getLeggendario().costruisci(), getTempio());
							BusEventi.pubblica(new NotificaTestoParagrafo("L'armaiolo segna sulla mappa il tempio dove le viverne custodiscono "
									+ getLeggendario().getNomeBreve() + "."));
							attivaMissione();
						})
						.poi(RECUPERO);
			case RECUPERO:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE,
								() -> getTempio().equals(GruppoGiocatore.getIstanza().getCoordinate())
										&& RegistroArtefatti.getArtefattoInLocazione(getTempio()) == null)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("La leggenda era vera: "
								+ getLeggendario().getNomeBreve() + " è nelle vostre mani.")))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private ScenaInCitta scenaDellaLeggenda() {
		ArtefattoLeggendario leggendario = getLeggendario();
		ScenaInCitta scena = ScenaInCitta.conArmaiolo();
		leggendario.getLeggenda().forEach(scena::parlaIlMandante);
		return scena
				.parlaIlCapo("Con " + leggendario.getNomeBreve() + " il Drago avrebbe di che preoccuparsi.")
				.parlaIlMandante("Allora andate. Vi segno il tempio sulla mappa, ma state attenti alle viverne.")
				.parlaIlCapo("Partiamo subito.");
	}

	private CoordinateMD getTempio() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	private boolean nellaCitta() {
		return GruppoGiocatore.getIstanza().isInLocazioneUnica(getCitta()) && !LineaTemporale.isCittaDistrutta(getCitta());
	}

	/**
	 * Un leggendario che nessun'altra missione ha ancora pescato, o null.
	 */
	private ArtefattoLeggendario pesca() {
		List<ArtefattoLeggendario> giaAssegnati = RegistroMissioni.getTutteLeMissioni().stream()
				.filter(m -> m != this && m instanceof RecuperaUnArtefattoLeggendario)
				.map(m -> ((RecuperaUnArtefattoLeggendario) m).getLeggendario())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return RegistroArtefatti.pescaLeggendario(giaAssegnati).orElse(null);
	}
}
