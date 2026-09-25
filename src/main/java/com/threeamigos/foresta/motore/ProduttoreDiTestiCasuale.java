package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.GrammarBean.InvalidGrammarException;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.Notizia;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProduttoreDiTestiCasuale {

	private static final int MASSIMO_TENTATIVI_NOTIZIA_LOCANDA = 20;

	private static GrammarBean fiabe;
	private static GrammarBean oroscopi;
	private static GrammarBean locande;

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
		} catch (InvalidGrammarException | IOException e) {
			Logger.log(e);
			System.exit(0);
		}
	}

	public static List<String> fiaba() {
		List<String> fiaba = fiabe.produce();
		fiabe.reset();
		return fiaba;
	}

	public static List<String> oroscopo() {
		List<String> oroscopo = oroscopi.produce();
		oroscopi.reset();
		return oroscopo;
	}

	public static Notizia getNotiziaLocanda(String identificativoLocanda, String nomeLocanda, String nomeLocandiere) {
		return getNotiziaLocanda(identificativoLocanda, nomeLocanda, nomeLocandiere, 0);
	}

	private static Notizia getNotiziaLocanda(String identificativoLocanda, String nomeLocanda, String nomeLocandiere, int tentativo) {
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
		return new Notizia(identificativo, contenuto);
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
		Collection<ClassePersonaggio> classiPresenti = personaggiVivi.stream().map(Personaggio::getClasse).collect(Collectors.toSet());
		boolean appareBardo = contenuto.contains(ClassePersonaggio.BARDO.name()) || contenuto.contains(ClassePersonaggio.CANTASTORIE.name());
		if (appareBardo) {
			applicabile = classiPresenti.contains(ClassePersonaggio.BARDO) || classiPresenti.contains(ClassePersonaggio.CANTASTORIE);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, ClassePersonaggio.CANTASTORIE, ClassePersonaggio.BARDO);
			contenuto = sostituisci(contenuto, ClassePersonaggio.BARDO, ClassePersonaggio.CANTASTORIE);
		}
		boolean appareLadro = contenuto.contains(ClassePersonaggio.LADRO.name()) || contenuto.contains(ClassePersonaggio.LADRA.name());
		if (appareLadro) {
			applicabile = classiPresenti.contains(ClassePersonaggio.LADRO) || classiPresenti.contains(ClassePersonaggio.LADRA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, ClassePersonaggio.LADRA, ClassePersonaggio.LADRO);
			contenuto = sostituisci(contenuto, ClassePersonaggio.LADRO, ClassePersonaggio.LADRA);

		}
		boolean appareGuerriero = contenuto.contains(ClassePersonaggio.GUERRIERO.name()) || contenuto.contains(ClassePersonaggio.GUERRIERA.name());
		if (appareGuerriero) {
			applicabile = classiPresenti.contains(ClassePersonaggio.GUERRIERO) || classiPresenti.contains(ClassePersonaggio.GUERRIERA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, ClassePersonaggio.GUERRIERA, ClassePersonaggio.GUERRIERO);
			contenuto = sostituisci(contenuto, ClassePersonaggio.GUERRIERO, ClassePersonaggio.GUERRIERA);
		}
		boolean appareMago = contenuto.contains(ClassePersonaggio.MAGO.name()) || contenuto.contains(ClassePersonaggio.MAGA.name());
		if (appareMago) {
			applicabile = classiPresenti.contains(ClassePersonaggio.MAGO) || classiPresenti.contains(ClassePersonaggio.MAGA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, ClassePersonaggio.MAGA, ClassePersonaggio.MAGO);
			contenuto = sostituisci(contenuto, ClassePersonaggio.MAGO, ClassePersonaggio.MAGA);
		}
		boolean appareElfo = contenuto.contains(ClassePersonaggio.ELFO.name()) || contenuto.contains(ClassePersonaggio.ELFA.name());
		if (appareElfo) {
			applicabile = classiPresenti.contains(ClassePersonaggio.ELFO) || classiPresenti.contains(ClassePersonaggio.ELFA);
			if (!applicabile) {
				return null;
			}
			contenuto = sostituisci(contenuto, ClassePersonaggio.ELFA, ClassePersonaggio.ELFO);
			contenuto = sostituisci(contenuto, ClassePersonaggio.ELFO, ClassePersonaggio.ELFA);
		}
		Personaggio eroe = GruppoGiocatore.getIstanza().capo;
		contenuto = contenuto.replace("EROE", eroe.getNomeProprio().orElseThrow(() -> new IllegalStateException("Personaggio senza nome proprio")));
		contenuto = contenuto.replace("LETTERA_FINALE", eroe.getLetteraFinaleAttributo());
		return contenuto;
	}

	private static String sostituisci(String contenuto, ClassePersonaggio classe1, ClassePersonaggio classe2) {
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

	private static Personaggio trova(ClassePersonaggio classe, ClassePersonaggio classeDiRipiego) {
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
