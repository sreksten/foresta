package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Uno dei quattro campioni locali di carta, forbici e sasso (vedi {@link LaSfidaDeiCampioni}, che li affida): un
 * avversario che sta in un posto della Foresta, segnato sulla mappa, e sfida il capo del gruppo appena ci si arriva.
 * Le sole azioni possibili sono carta, forbici e sasso (vedi LocazioneBase, stato SFIDA_CARTA_FORBICI_SASSO); vinta la
 * sfida si va via normalmente e il campione è battuto, persa si può tornare a riprovare. Il campione, il suo nome e il
 * suo posto li sceglie la madre.
 * <ol>
 * <li>POSTO, in locazione, subito: il posto compare sulla mappa;</li>
 * <li>SFIDA, a fine locazione, nel posto: la sfida è stata vinta, e la locazione è completa.</li>
 * </ol>
 */
public class IlCampione extends MissioneAPassi {

	private static final String CLASSE = "CLASSE";
	private static final String NOME = "NOME";
	private static final String LUOGO = "LUOGO";
	private static final String POSTO = "POSTO";
	private static final String SFIDA = "SFIDA";

	public IlCampione() {
		super(ClasseMissione.IL_CAMPIONE);
	}

	/**
	 * Il campione di quella classe, con quel nome, che aspetta in un posto di quel tipo.
	 */
	static IlCampione di(TipoPersonaggio classe, String nome, TipoLocazione luogo) {
		IlCampione campione = new IlCampione();
		campione.aggiungiProprieta(PARAMETRO + CLASSE, classe.name());
		campione.aggiungiProprieta(PARAMETRO + NOME, nome);
		campione.aggiungiProprieta(PARAMETRO + LUOGO, luogo.name());
		return campione;
	}

	public TipoPersonaggio getClasseDelCampione() {
		return TipoPersonaggio.valueOf(getParametro(CLASSE));
	}

	public String getCampione() {
		return getParametro(NOME);
	}

	public TipoLocazione getLuogo() {
		return TipoLocazione.valueOf(getParametro(LUOGO));
	}

	/**
	 * Il posto del campione, o null finché la missione non l'ha trovato.
	 */
	public CoordinateMD getPosto() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	/**
	 * Il campione per i testi: "Bradamante la Fulva, una Guerriera".
	 */
	public String ilCampione() {
		Personaggio modello = FabbricaPersonaggi.modello(getClasseDelCampione());
		return getCampione() + ", " + modello.getAIS() + modello.getNomeSingolare();
	}

	private IncontroDiMissione getIncontro() {
		return IncontroDiMissione.di(getClasseDelCampione(), 1).conCapo(getCampione()).aCartaForbiciSasso();
	}

	@Override
	public String getNome() {
		return "Il campione " + getCampione();
	}

	@Override
	public String getDescrizione() {
		return "Sfida " + ilCampione() + ", a carta, forbici e sasso, a tre mani vinte: lo trovi " + TestiDeiLuoghi.dentro(getLuogo())
				+ ", nel posto segnato sulla mappa. Se perdi puoi tornare a riprovare.";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return isFallita() ? null : "Qui " + getCampione() + " ha sfidato il gruppo a carta, forbici e sasso.";
	}

	@Override
	protected String passoIniziale() {
		return POSTO;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case POSTO:
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, getLuogo())
						.esegui(() -> {
							Foresta.setLocazioneConosciuta(getPosto());
							BusEventi.pubblica(new NotificaTestoParagrafo(ilCampione() + ", aspetta " + TestiDeiLuoghi.dentro(getLuogo())
									+ ": il posto è segnato sulla mappa."));
						})
						.poi(SFIDA);
			case SFIDA:
				return Passo.quando(MomentoControllo.POST_LOCAZIONE, this::sfidaVinta)
						.affronta(this::getPosto, getIncontro())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(getCampione() + " è battuto.")))
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	/**
	 * Il gruppo è nel posto e la locazione è completa: con un campione lo è solo se il giocatore ha vinto la sfida.
	 */
	private boolean sfidaVinta() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return getPosto() != null && getPosto().equals(gruppo.getCoordinate()) && gruppo.getLocazioneCorrente().isCompleta();
	}
}
