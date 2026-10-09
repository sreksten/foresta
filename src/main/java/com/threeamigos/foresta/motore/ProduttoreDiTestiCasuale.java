package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProduttoreDiTestiCasuale {

	private static final int MASSIMO_TENTATIVI_NOTIZIA_LOCANDA = 20;

	private static GrammarBean fiabe;
	private static GrammarBean oroscopi;
	private static GrammarBean locande;
	private static GrammarBean templi;
	private static GrammarBean rovine;
	private static GrammarBean missioni;
	private static GrammarBean leggendari;
	private static GrammarBean sfide;
	private static final String OGGETTO_LEGGENDARIO = "OGGETTO_LEGGENDARIO";
	private static final String SET_LEGGENDARIO = "SET_LEGGENDARIO";

	private ProduttoreDiTestiCasuale() {
	}

	/**
	 * Non fa nulla: chiamarlo basta a caricare subito le grammatiche, che il blocco statico legge al primo uso della
	 * classe (vedi Automa, stato LOGO_INIZIALE).
	 */
	static void precarica() {
		// il lavoro lo fa il blocco statico
	}

	static {
		try {
			fiabe = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/fiabe.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
			oroscopi = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/oroscopo.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
			locande = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/locande.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
			templi = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/templi.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
			rovine = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/rovine.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
			missioni = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/missioni.txt"),
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/preposizioni_articolate_pp.txt"));
			leggendari = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/leggendari.txt"), null);
			sfide = new GrammarBean(
					ProduttoreDiTestiCasuale.class.getResourceAsStream("/com/threeamigos/foresta/motore/sfide.txt"), null);
		} catch (InvalidGrammarException | IOException e) {
			// Senza grammatiche il gioco non puo' andare avanti: si segnala l'errore e si esce. L'uscita va in coda
			// sull'EDT dopo la notifica, cosi' chi ascolta le InternoException la riceve prima.
			Logger.log(e);
			BusEventi.pubblica(new InternoException("Grammatiche dei testi non valide o mancanti", e));
			SwingUtilities.invokeLater(() -> System.exit(1));
		}
	}

	public static List<String> fiaba() {
		List<String> fiaba = fiabe.produce();
		fiabe.reset();
		return fiaba;
	}

	public static String reazioneLocandiere() {
		List<String> reazioneLocandiere = locande.produce("REAZIONE_LOCANDIERE");
		locande.reset();
		return reazioneLocandiere.get(0);
	}

	/**
	 * Un nome a caso per un tempio, con l'articolo: "il Santuario della Luna" (vedi templi.txt e Tempio.getNome).
	 */
	public static String nomeTempio() {
		return templi.produce("NOME_TEMPIO").get(0).trim();
	}

	/**
	 * Un nome a caso per delle rovine, con l'articolo: "le Vestigia dell'Antico Impero" (vedi rovine.txt e
	 * Rovine.getNome).
	 */
	public static String nomeRovine() {
		return rovine.produce("NOME_ROVINE").get(0).trim();
	}

	/**
	 * La frase con cui chi ha stretto amicizia sfida il capo del gruppo a carta, forbici e sasso (vedi sfide.txt).
	 */
	public static String fraseDiSfida() {
		return sfide.produce("SFIDA").get(0).trim();
	}

	/**
	 * Quello che dice l'avversario quando il giocatore vince la sfida (vedi sfide.txt).
	 */
	public static String fraseSeIlGiocatoreVince() {
		return sfide.produce("GIOCATORE_VINCE").get(0).trim();
	}

	/**
	 * Quello che dice l'avversario quando il giocatore perde la sfida (vedi sfide.txt).
	 */
	public static String fraseSeIlGiocatorePerde() {
		return sfide.produce("GIOCATORE_PERDE").get(0).trim();
	}

	/**
	 * Quello che dice l'avversario alla terza sfida vinta dal gruppo, per mandarlo al torneo (vedi sfide.txt).
	 */
	public static String fraseDelTorneo() {
		return sfide.produce("TORNEO").get(0).trim();
	}

	/**
	 * Il nome, con l'articolo, di un leggendario costruito al volo per la ricompensa de La sfida dei campioni (vedi
	 * sfide.txt).
	 */
	public static String nomeLeggendarioDeiCampioni() {
		return sfide.produce("NOME_LEGGENDARIO_DEI_CAMPIONI").get(0).trim();
	}

	/**
	 * Il nome di un ostaggio da liberare (vedi missioni.txt).
	 */
	public static String nomeOstaggio() {
		return missioni.produce("NOME_OSTAGGIO").get(0).trim();
	}

	/**
	 * Il nome di un bardo (vedi missioni.txt).
	 */
	public static String nomeBardo() {
		return missioni.produce("NOME_BARDO").get(0).trim();
	}

	/**
	 * Il nome di un pellegrino (vedi missioni.txt).
	 */
	public static String nomePellegrino() {
		return missioni.produce("NOME_PELLEGRINO").get(0).trim();
	}

	/**
	 * Una riga di una produzione di missioni.txt (gli incarichi di combattimento, le sorveglianze...).
	 */
	public static String rigaDiMissioni(String produzione) {
		return missioni.produce(produzione).get(0).trim();
	}

	/**
	 * Un materiale che un mandante chiede, da una produzione di missioni.txt (RICHIESTA_ALCHIMISTA...), come riga di
	 * otto campi separati da ";" (vedi MaterialeRichiesto).
	 */
	public static String materialeRichiesto(String produzione) {
		return missioni.produce(produzione).get(0).trim();
	}

	/**
	 * Una cosa da portare da una città all'altra, come riga di dieci campi separati da ";" (vedi missioni.txt e
	 * Spedizione).
	 */
	public static String spedizione() {
		return missioni.produce("TRASPORTO").get(0).trim();
	}

	/**
	 * Un oggetto smarrito nella foresta, come riga di nove campi separati da ";" (vedi missioni.txt e
	 * OggettoSmarrito).
	 */
	public static String oggettoSmarrito() {
		return missioni.produce("OGGETTO_SMARRITO").get(0).trim();
	}

	/**
	 * Un oggetto leggendario di leggendari.txt (vedi OggettoLeggendario) che non è già stato pescato, o vuoto se sono
	 * stati pescati tutti. Le righe sono one-shot: si riparte ogni volta da tutte e si scartano quelle già pescate
	 * nella partita, che lo sa dalle sue missioni (la grammatica non si salva con la partita).
	 */
	public static synchronized Optional<String> oggettoLeggendario(Predicate<String> giaPescato) {
		leggendari.reset();
		while (leggendari.canProduce(OGGETTO_LEGGENDARIO)) {
			String riga = String.join(" ", leggendari.produce(OGGETTO_LEGGENDARIO)).trim();
			if (!giaPescato.test(riga)) {
				return Optional.of(riga);
			}
		}
		return Optional.empty();
	}

	/**
	 * Tutti gli oggetti leggendari di leggendari.txt, in ordine casuale.
	 */
	public static List<String> tuttiGliOggettiLeggendari() {
		return tutteLeRigheLeggendarie(OGGETTO_LEGGENDARIO);
	}

	/**
	 * Tutti i set leggendari di leggendari.txt (vedi SetLeggendario), in ordine casuale.
	 */
	public static List<String> tuttiISetLeggendari() {
		return tutteLeRigheLeggendarie(SET_LEGGENDARIO);
	}

	/**
	 * Tutte le righe di una produzione one-shot di leggendari.txt: si pescano finché ce ne sono.
	 */
	private static synchronized List<String> tutteLeRigheLeggendarie(String produzione) {
		List<String> righe = new ArrayList<>();
		leggendari.reset();
		while (leggendari.canProduce(produzione)) {
			righe.add(String.join(" ", leggendari.produce(produzione)).trim());
		}
		return righe;
	}

	/**
	 * Il nome di un capobanda di goblin o hobgoblin, a volte con un soprannome: "Grumolo il Guercio" (vedi
	 * missioni.txt).
	 */
	public static String nomeCapobanda() {
		return missioni.produce("NOME_CAPOBANDA").get(0).trim();
	}

	public static List<String> oroscopo() {
		List<String> oroscopo = oroscopi.produce();
		oroscopi.reset();
		return oroscopo;
	}

	public static NotiziaMD getNotiziaLocanda(String identificativoLocanda, String nomeLocanda, String nomeLocandiere) {
		return getNotiziaLocanda(identificativoLocanda, nomeLocanda, nomeLocandiere, 0);
	}

	private static NotiziaMD getNotiziaLocanda(String identificativoLocanda, String nomeLocanda, String nomeLocandiere, int tentativo) {
		if (tentativo >= MASSIMO_TENTATIVI_NOTIZIA_LOCANDA) {
			throw new IllegalStateException("Non è stato possibile trovare una notizia applicabile e non recente per la locanda " + identificativoLocanda);
		}
		List<String> notizie = locande.produce("NOTIZIE_" + identificativoLocanda);
		String notizia = notizie.get(0);
		int posizioneSeparatore = notizia.indexOf("-");
		String identificativo = notizia.substring(0, posizioneSeparatore);
		if (ModelloDati.getIstanza().getNotizieMD().getUltimeNotizie().stream().anyMatch(n -> n.getId().equals(identificativo))) {
			// La notizia è già stata pubblicata tra le ultime, ne scegliamo un'altra
			return getNotiziaLocanda(identificativoLocanda, nomeLocanda, nomeLocandiere, tentativo + 1);
		}
		String contenuto = validaNotizia(notizia.substring(posizioneSeparatore + 1));
		if (contenuto == null) {
			return getNotiziaLocanda(identificativoLocanda, nomeLocanda, nomeLocandiere, tentativo + 1);
		}
		contenuto = contenuto.replace("NOME_LOCANDA", nomeLocanda);
		contenuto = contenuto.replace("NOME_LOCANDIERE", nomeLocandiere);
		return new NotiziaMD(identificativo, contenuto);
	}

	/**
	 * Per validare una notizia occorre controllare se nel corpo appaiono una o più classi personaggio. Per quelle che
	 * appaiono, occorre che ci sia un personaggio della classe corrispondente vivo nel gruppo. In mancanza, la notizia
	 * non è valida.
	 *
	 * @param contenuto il contenuto da controllare
	 * @return null se il contenuto non è applicabile al gruppo dei personaggi, una stringa con le debite sostituzioni fatte altrimenti
	 */
	private static String validaNotizia(String contenuto) {
		// Occorre vedere se tante volte appare uno dei personaggi del gruppo o più di uno.
		boolean applicabile = true;
		Collection<Personaggio> personaggiVivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();
		Collection<TipoPersonaggio> classiPresenti = personaggiVivi.stream().map(Personaggio::getClasse).collect(Collectors.toSet());
		boolean appareBardo = contenuto.contains(TipoPersonaggio.BARDO.name()) || contenuto.contains(TipoPersonaggio.CANTASTORIE.name());
		if (appareBardo) {
			applicabile = classiPresenti.contains(TipoPersonaggio.BARDO) || classiPresenti.contains(TipoPersonaggio.CANTASTORIE);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, TipoPersonaggio.CANTASTORIE, TipoPersonaggio.BARDO);
			contenuto = sostituisci(contenuto, TipoPersonaggio.BARDO, TipoPersonaggio.CANTASTORIE);
		}
		boolean appareLadro = contenuto.contains(TipoPersonaggio.LADRO.name()) || contenuto.contains(TipoPersonaggio.LADRA.name());
		if (appareLadro) {
			applicabile = classiPresenti.contains(TipoPersonaggio.LADRO) || classiPresenti.contains(TipoPersonaggio.LADRA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, TipoPersonaggio.LADRA, TipoPersonaggio.LADRO);
			contenuto = sostituisci(contenuto, TipoPersonaggio.LADRO, TipoPersonaggio.LADRA);

		}
		boolean appareGuerriero = contenuto.contains(TipoPersonaggio.GUERRIERO.name()) || contenuto.contains(TipoPersonaggio.GUERRIERA.name());
		if (appareGuerriero) {
			applicabile = classiPresenti.contains(TipoPersonaggio.GUERRIERO) || classiPresenti.contains(TipoPersonaggio.GUERRIERA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, TipoPersonaggio.GUERRIERA, TipoPersonaggio.GUERRIERO);
			contenuto = sostituisci(contenuto, TipoPersonaggio.GUERRIERO, TipoPersonaggio.GUERRIERA);
		}
		boolean appareMago = contenuto.contains(TipoPersonaggio.MAGO.name()) || contenuto.contains(TipoPersonaggio.MAGA.name());
		if (appareMago) {
			applicabile = classiPresenti.contains(TipoPersonaggio.MAGO) || classiPresenti.contains(TipoPersonaggio.MAGA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, TipoPersonaggio.MAGA, TipoPersonaggio.MAGO);
			contenuto = sostituisci(contenuto, TipoPersonaggio.MAGO, TipoPersonaggio.MAGA);
		}
		boolean appareElfo = contenuto.contains(TipoPersonaggio.ELFO.name()) || contenuto.contains(TipoPersonaggio.ELFA.name());
		if (appareElfo) {
			applicabile = classiPresenti.contains(TipoPersonaggio.ELFO) || classiPresenti.contains(TipoPersonaggio.ELFA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, TipoPersonaggio.ELFA, TipoPersonaggio.ELFO);
			contenuto = sostituisci(contenuto, TipoPersonaggio.ELFO, TipoPersonaggio.ELFA);
		}
		Personaggio eroe = GruppoGiocatore.getIstanza().capo;
		contenuto = contenuto.replace("EROE", eroe.getNomeProprio().orElseThrow(() -> new IllegalStateException("Personaggio senza nome proprio")));
		contenuto = contenuto.replace("LETTERA_FINALE", eroe.getLetteraFinaleAttributo());
		return contenuto;
	}

	private static String sostituisci(String contenuto, TipoPersonaggio classe1, TipoPersonaggio classe2) {
		if (contenuto.contains(classe1.name())) {
			Personaggio p = trova(classe1, classe2);
			contenuto = contenuto.replace(classe1.name(), p.getNomeProprio().orElseThrow(() -> new IllegalStateException("Personaggio senza nome proprio")));
		}
		if (contenuto.contains(classe2.name())) {
			Personaggio p = trova(classe2, classe1);
			contenuto = contenuto.replace(classe2.name(), p.getNomeProprio().orElseThrow(() -> new IllegalStateException("Personaggio senza nome proprio")));
		}
		return contenuto;
	}

	private static Personaggio trova(TipoPersonaggio classe, TipoPersonaggio classeDiRipiego) {
		Collection<Personaggio> personaggiVivi = GruppoGiocatore.getIstanza().getPersonaggiVivi();
		Optional<Personaggio> opt = personaggiVivi.stream().filter(p -> p.getClasse() == classe).findFirst();
		if (!opt.isPresent()) {
			opt = personaggiVivi.stream().filter(p -> p.getClasse() == classeDiRipiego).findFirst();
		}
		return opt.orElseThrow(() -> new IllegalStateException("Non è stato trovato nessun " + classe + " o " + classeDiRipiego + " tra i personaggi vivi"));
	}

	public static void resetProduzioni() {
		fiabe.reset();
		oroscopi.reset();
		locande.reset();
		GrammarBean.resetStaticProductions();
	}

	public static List<DatiLocanda> getDatiLocanda(int quantita) {
		List<DatiLocanda> datiLocanda = new ArrayList<>();
		for (int i = 0; i < quantita; i++) {
			List<String> produzioni = locande.produce();
			if (produzioni.size() > 1) {
				throw new IllegalArgumentException("La grammatica produce più di una riga di produzione per " + locande.getRootNode());
			}
			String[] token = produzioni.get(0).split("/");
			String nomeLocanda = token[0];
			String identificativoLocanda = token[1];
			String nomeLocandiere = token[2];
			String recensioneLocanda = token[3];
			String dialogoLocanda = token[4];
			final String chiaveRecensione = "RECENSIONE=";
			if (recensioneLocanda != null && recensioneLocanda.startsWith(chiaveRecensione)) {
				recensioneLocanda = recensioneLocanda.substring(chiaveRecensione.length());
			} else {
				recensioneLocanda = "";
			}
			final String chiaveDialogo = "DIALOGO=";
			if (dialogoLocanda != null && dialogoLocanda.startsWith(chiaveDialogo)) {
				dialogoLocanda = dialogoLocanda.substring(chiaveDialogo.length());
			} else {
				dialogoLocanda = "";
			}
			datiLocanda.add(new DatiLocanda(nomeLocanda, identificativoLocanda, nomeLocandiere, dialogoLocanda, recensioneLocanda));
		}
		return datiLocanda;
	}

	public static class DatiLocanda {
		private final String nome;
		private final String identificativo;
		private final String nomeLocandiere;
		private final String dialogo;
		private final String recensione;
		DatiLocanda(String nome, String identificativo, String nomeLocandiere, String dialogo, String recensione) {
			this.nome = nome;
			this.identificativo = identificativo;
			this.nomeLocandiere = nomeLocandiere;
			this.dialogo = dialogo;
			this.recensione = recensione;
		}

		public String getNome() {
			return nome;
		}

		public String getIdentificativo() {
			return identificativo;
		}

		public String getNomeLocandiere() {
			return nomeLocandiere;
		}

		public String getDialogo() {
			return dialogo;
		}

		public String getRecensione() {
			return recensione;
		}
	}
}
