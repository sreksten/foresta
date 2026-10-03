package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

/**
 * In città una donna chiede di liberare suo marito Armando, rapito da una banda di goblin che lo tiene in una grotta:
 * la missione rivendica la grotta, la segna sulla mappa e ci mette la banda (vedi {@link MissioneAPassi#combatti}).
 * Sconfitta la banda, Armando si unisce al gruppo come ospite vulnerabile (vedi
 * {@link MissioneAPassi#prendiInScorta(MomentoControllo, java.util.function.BooleanSupplier, String, boolean)}): gli
 * avversari lo possono attaccare, e va riportato vivo in città. Se muore per strada, la missione fallisce.
 */
public class IlRapimentoDiArmando extends IncaricoInCitta {

	public static final String ARMANDO = "Armando";
	public static final IncontroDiMissione RAPITORI = IncontroDiMissione.di(ClassePersonaggio.GOBLIN, 4).conCapo("Ghignazzo");
	private static final int RICOMPENSA = 35;
	private static final String COVO = "COVO";
	private static final String LIBERAZIONE = "LIBERAZIONE";
	private static final String LIBERATO = "LIBERATO";
	private static final String VIAGGIO = "VIAGGIO";

	public IlRapimentoDiArmando() {
		super(ClasseMissione.IL_RAPIMENTO_DI_ARMANDO);
	}

	@Override
	public String getNome() {
		return "Il rapimento di Armando";
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (VIAGGIO.equals(passo)) {
			return "Armando è libero ma debole: riportalo vivo a " + getNomeCitta() + ", e proteggilo per strada.";
		}
		if (RITORNO.equals(passo)) {
			return "Armando è a casa: sua moglie ti aspetta a " + getNomeCitta() + ".";
		}
		return "Una banda di goblin tiene prigioniero Armando in una grotta segnata sulla mappa. Liberalo e riportalo a "
				+ getNomeCitta() + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "In questa grotta i goblin di Ghignazzo tenevano prigioniero Armando.";
	}

	/**
	 * La grotta dei rapitori, o null finché la missione non l'ha trovata.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case COVO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, ClassiLocazione.GROTTA)
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getCovo());
							BusEventi.pubblica(new NotificaTestoParagrafo("La grotta dove i goblin tengono prigioniero Armando è segnata sulla mappa."));
						})
						.poi(LIBERAZIONE);
			case LIBERAZIONE:
				return combatti(this::getCovo, RAPITORI).poi(LIBERATO);
			case LIBERATO:
				return prendiInScorta(MomentoControllo.POST_LOCAZIONE,
								() -> getCovo().equals(GruppoGiocatore.getIstanza().getCoordinate()), ARMANDO, true)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Armando è libero, ma è debole e non sa difendersi: "
								+ "riportatelo vivo a " + getNomeCitta() + ".")))
						.poi(VIAGGIO);
			case VIAGGIO:
				return scorta(MomentoControllo.PRE_LOCAZIONE, () -> Foresta.getCoordinateLocazioneUnica(getCitta()),
								() -> "Armando non ce l'ha fatta: i suoi rapitori avevano degli amici. La sua famiglia non vi pagherà.")
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Vi prego, aiutatemi! Una banda di goblin ha rapito mio marito Armando.")
				.parlaIlMandante("Lo tengono in una grotta qui vicino, e il loro capo, Ghignazzo, chiede un riscatto che non posso pagare.")
				.parlaIlCapo("Il riscatto glielo portiamo noi, a modo nostro.")
				.parlaIlMandante("Riportatemelo vivo e avrete " + RICOMPENSA + " monete.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Armando! Sei tornato!")
				.parlaIlMandante("Non so come ringraziarvi. Ecco le " + RICOMPENSA + " monete.")
				.parlaIlCapo("Tenetelo d'occhio, la prossima volta.");
	}

	@Override
	protected String testoAccettazione() {
		return "La moglie di Armando pagherà " + RICOMPENSA + " monete per riaverlo a casa vivo.";
	}

	@Override
	protected String testoRicompensa() {
		return "La moglie di Armando paga le " + RICOMPENSA + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
