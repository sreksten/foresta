package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaRefreshUI;
import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Il torneo di carta, forbici e sasso (vedi carta_forbici_sasso.md): alla terza sfida vinta dal gruppo (vedi
 * LocazioneBase) chi lo ha sfidato lo manda a battere i quattro campioni locali, che stanno in quattro posti della
 * Foresta, segnati tutti e quattro sulla mappa. Ogni campione è una missione affidata ({@link IlCampione}): un
 * avversario scelto a caso fra quelli che si possono avere come amici, con un nome di missioni.txt, in un tipo di
 * posto diverso dagli altri. Battuto il quarto, sul posto, il gruppo riceve {@value #MONETE_PER_LIVELLO} monete per ogni
 * livello del mondo di quando la missione è partita e un oggetto leggendario: uno del catalogo che nessuna leggenda e
 * nessun torneo ha già messo in palio (vedi {@link PescaLeggendaria}), se ne restano, altrimenti uno costruito al volo,
 * di due livelli più del mondo.
 * <p>
 * Ce n'è una sola per partita: quando è partita, chi stringe amicizia non viene più sfidato (vedi
 * {@link #isPartita()}).
 * <ol>
 * <li>CAMPIONI, a fine locazione, subito: i quattro campioni sono affidati;</li>
 * <li>ATTESA, a fine locazione: i quattro campioni sono battuti;</li>
 * <li>RICOMPENSA, a fine locazione, subito: le monete e il leggendario.</li>
 * </ol>
 */
public class LaSfidaDeiCampioni extends MissioneAPassi implements ConLeggendario {

	public static final int CAMPIONI = 4;
	public static final int MONETE_PER_LIVELLO = 100;
	public static final int LIVELLI_IN_PIU_DEL_LEGGENDARIO_COSTRUITO = 2;
	private static final String LIVELLO_DEL_MONDO = "LIVELLO_DEL_MONDO";
	private static final String LEGGENDARIO = "LEGGENDARIO";
	private static final String AFFIDATI = "CAMPIONI";
	private static final String ATTESA = "ATTESA";
	private static final String RICOMPENSA = "RICOMPENSA";
	private static final int LIVELLO_MASSIMO_ARTEFATTO = 10;

	public LaSfidaDeiCampioni() {
		super(ClasseMissione.LA_SFIDA_DEI_CAMPIONI);
	}

	/**
	 * Se la missione è già partita in questa partita, in corso, finita o fallita: ce n'è una sola.
	 */
	public static boolean isPartita() {
		return RegistroMissioni.getTutteLeMissioni().stream().anyMatch(missione -> missione instanceof LaSfidaDeiCampioni);
	}

	/**
	 * Fa partire la missione: fissa il livello del mondo di adesso e il leggendario in palio, la attiva (con la notifica di
	 * nuova missione) e affida subito i quattro campioni, ognuno già con il suo posto segnato sulla mappa.
	 */
	public static LaSfidaDeiCampioni avvia() {
		LaSfidaDeiCampioni missione = new LaSfidaDeiCampioni();
		missione.aggiungiProprieta(LIVELLO_DEL_MONDO, String.valueOf(Statistiche.getLivello()));
		missione.aggiungiProprieta(LEGGENDARIO, missione.scegliIlLeggendario().getRiga());
		RegistroMissioni.aggiungiMissioneSecondaria(missione);
		missione.attivaMissione();
		BusEventi.pubblica(new NotificaAggiornamentoStatoMissione(missione, "NUOVA MISSIONE", missione.getNome()));
		// Non si aspetta il prossimo controllo: i campioni si affidano subito e ognuno trova e segna subito il suo posto,
		// così la notifica c'è e i quattro segnalini sono già sulla mappa
		missione.controllaPostLocazione();
		missione.getCampioni().forEach(Missione::controllaInLocazione);
		BusEventi.pubblica(new InternoRichiestaRefreshUI());
		return missione;
	}

	private OggettoLeggendario scegliIlLeggendario() {
		if (PescaLeggendaria.restaUnLeggendario(PescaLeggendaria.giaPescati(this))) {
			return OggettoLeggendario.da(PescaLeggendaria.pesca(PescaLeggendaria.giaPescati(this), 0));
		}
		return costruisciUnLeggendario(Math.min(LIVELLO_MASSIMO_ARTEFATTO, Statistiche.getLivello() + LIVELLI_IN_PIU_DEL_LEGGENDARIO_COSTRUITO));
	}

	/**
	 * Un leggendario costruito al volo, quando il catalogo non ne ha più: un oggetto raro di quel livello, con un nome
	 * di sfide.txt, e i modificatori forti come quelli del catalogo.
	 */
	public static OggettoLeggendario costruisciUnLeggendario(int livello) {
		String nome = ProduttoreDiTestiCasuale.nomeLeggendarioDeiCampioni();
		String breve = nome;
		String chiave = "CAMPIONI_" + Math.abs(nome.hashCode()) + "_" + livello;
		String riga = "TIPO=ANELLO;NOME=" + nome + ";CHIAVE=" + chiave + ";BREVE=" + breve + ";"
				+ "DESCRIZIONE=che appartiene a chi ha battuto tutti i campioni;LIVELLO=" + livello + ";COSTO=" + (livello * 100)
				+ ";PESO=1;MOD=FORZA +" + (livello * 10) + "%;MOD=CORAGGIO +" + (livello * 5) + "%;MOD=VALORE +" + livello + ";"
				+ "LEGGENDA=Avete mai sentito parlare di " + nome + "?;"
				+ "LEGGENDA=Lo vince solo chi batte, uno dopo l'altro, i quattro campioni di carta, forbici e sasso.";
		return OggettoLeggendario.da(riga);
	}

	@Override
	public OggettoLeggendario getLeggendario() {
		String riga = ottieniProprieta(LEGGENDARIO);
		return riga == null ? null : OggettoLeggendario.da(riga);
	}

	/**
	 * Il livello del mondo di quando la missione è partita.
	 */
	public int getLivelloDelMondo() {
		String livello = ottieniProprieta(LIVELLO_DEL_MONDO);
		return livello != null ? Integer.parseInt(livello) : Statistiche.getLivello();
	}

	/**
	 * Le monete della ricompensa: {@value #MONETE_PER_LIVELLO} per ogni livello del mondo di quando la missione è partita.
	 */
	public int getMonete() {
		return MONETE_PER_LIVELLO * getLivelloDelMondo();
	}

	/**
	 * I quattro campioni, nell'ordine in cui sono stati affidati; vuota finché non sono affidati.
	 */
	public List<Missione> getCampioni() {
		return getMissioniAffidate(AFFIDATI);
	}

	@Override
	public String getNome() {
		return "La sfida dei campioni";
	}

	@Override
	public String getDescrizione() {
		List<Missione> campioni = getCampioni();
		long battuti = campioni.stream().filter(Missione::isCompleta).count();
		return "Batti a carta, forbici e sasso, a tre mani vinte, i quattro campioni della Foresta, nei posti segnati sulla mappa: "
				+ battuti + " battuti su " + CAMPIONI + ". In palio " + getMonete() + " monete e "
				+ (getLeggendario() != null ? getLeggendario().getNomeBreve() : "un oggetto leggendario") + ".";
	}

	@Override
	protected String passoIniziale() {
		return AFFIDATI;
	}

	@Override
	protected Passo costruisciPasso(String id) {
		switch (id) {
			case AFFIDATI:
				return affida(AFFIDATI, MomentoControllo.POST_LOCAZIONE, () -> true, this::creaICampioni)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Ora che ti sei fatto un nome a carta, forbici e sasso, i "
								+ "quattro campioni della Foresta ti aspettano: i loro posti sono segnati sulla mappa. In palio "
								+ getMonete() + " monete e " + getLeggendario().getNomeBreve() + ".")))
						.poi(ATTESA);
			case ATTESA:
				return attendiLeAffidate(AFFIDATI, MomentoControllo.POST_LOCAZIONE).poi(RICOMPENSA);
			case RICOMPENSA:
				return ricompensa(MomentoControllo.POST_LOCAZIONE,
								Ricompensa.inMonete(getMonete()).conArtefatto(() -> getLeggendario().costruisci()),
								() -> "Il quarto campione si arrende: sei il re di carta, forbici e sasso. Ricevi " + getMonete()
										+ " monete e " + getLeggendario().getNomeBreve() + ".")
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	/**
	 * Quattro campioni: ognuno un avversario che si può avere come amico, scelto a caso, con un nome di missioni.txt
	 * (di donna se è una femmina), in un tipo di posto diverso dagli altri.
	 */
	private List<Missione> creaICampioni() {
		List<TipoPersonaggio> classi = classiAmichevoli();
		List<TipoLocazione> luoghi = new ArrayList<>(CombattimentoRichiesto.LUOGHI);
		luoghi.sort(null);
		List<Missione> campioni = new ArrayList<>();
		for (int i = 0; i < CAMPIONI; i++) {
			TipoPersonaggio classe = classi.get(Dado.tiraAncheAUnaFaccia(classi.size()) - 1);
			TipoLocazione luogo = luoghi.remove(Dado.tiraAncheAUnaFaccia(luoghi.size()) - 1);
			boolean donna = FabbricaPersonaggi.modello(classe).getSesso() == Personaggio.Sesso.FEMMINA;
			String nome = ProduttoreDiTestiCasuale.rigaDiMissioni(donna ? "NOME_CAMPIONESSA" : "NOME_CAMPIONE");
			campioni.add(IlCampione.di(classe, nome, luogo));
		}
		return campioni;
	}

	/**
	 * Le classi con cui si può stringere amicizia (vedi Personaggio.isAmichevole) e che possono giocare a carta, forbici
	 * e sasso (vedi MossaCartaForbiciSasso.puoGiocare).
	 */
	public static List<TipoPersonaggio> classiAmichevoli() {
		List<TipoPersonaggio> classi = new ArrayList<>();
		for (TipoPersonaggio classe : TipoPersonaggio.values()) {
			if (FabbricaPersonaggi.modello(classe).isAmichevole() && MossaCartaForbiciSasso.puoGiocare(classe)) {
				classi.add(classe);
			}
		}
		return Collections.unmodifiableList(classi);
	}
}
