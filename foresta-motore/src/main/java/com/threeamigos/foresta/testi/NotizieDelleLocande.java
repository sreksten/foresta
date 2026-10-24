package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Le notizie che il locandiere racconta al gruppo: sceglie fra quelle del repertorio della locanda ({@link ProduttoreDiLocandeCasuale})
 * che si applicano ai personaggi vivi e che non sono fra le ultime già pubblicate, e le adatta al gruppo (i nomi e il genere dei
 * personaggi). Vedi nomi.md.
 */
public final class NotizieDelleLocande {

	private static final int MASSIMO_TENTATIVI_NOTIZIA_LOCANDA = 20;

	private NotizieDelleLocande() {
	}

	/**
	 * Una notizia della locanda, fra quelle del suo repertorio (NOTIZIE_ + identificativo, in locande.txt) che si applicano al gruppo
	 * (vedi {@link #validaNotizia}) e che non sono fra le ultime già pubblicate (MASSIMO_NOTIZIE_RICORDATE, in tutto il mondo).
	 * <p>
	 * Le notizie non sono monouso, la grammatica non si esaurisce: si pesca a caso fino a {@value #MASSIMO_TENTATIVI_NOTIZIA_LOCANDA}
	 * volte, scartando le recenti e quelle che non si applicano. Se tutti i tentativi vanno a vuoto (le notizie che si applicano al
	 * gruppo sono poche e già pubblicate) si riprova ammettendo anche le recenti, ripetere una notizia è meglio che non averne. Se
	 * nemmeno così ce n'è una applicabile il metodo restituisce null: la locanda non ha niente da raccontare. Non solleva eccezioni per
	 * questo.
	 *
	 * @return la notizia, o null se la locanda non ne ha una applicabile
	 */
	public static NotiziaMD getNotiziaLocanda(String identificativoLocanda, String nomeLocanda, String nomeLocandiere) {
		NotiziaMD notizia = cercaNotiziaLocanda(identificativoLocanda, nomeLocanda, nomeLocandiere, true);
		if (notizia == null) {
			notizia = cercaNotiziaLocanda(identificativoLocanda, nomeLocanda, nomeLocandiere, false);
		}
		return notizia;
	}

	private static NotiziaMD cercaNotiziaLocanda(String identificativoLocanda, String nomeLocanda, String nomeLocandiere,
												 boolean escludiLeRecenti) {
		for (int tentativo = 0; tentativo < MASSIMO_TENTATIVI_NOTIZIA_LOCANDA; tentativo++) {
			String notizia = ProduttoreDiLocandeCasuale.notiziaGrezza(identificativoLocanda);
			int posizioneSeparatore = notizia.indexOf('-');
			if (posizioneSeparatore < 0) {
				// Una notizia malformata (manca "ID-") si salta, come una che non si applica al gruppo
				Logger.log(() -> "Notizia della locanda senza identificativo, manca il '-': " + notizia);
				continue;
			}
			String identificativo = notizia.substring(0, posizioneSeparatore);
			if (escludiLeRecenti && ModelloDati.getIstanza().getNotizieMD().getUltimeNotizie().stream().anyMatch(n -> n.getId().equals(identificativo))) {
				// La notizia è già stata pubblicata tra le ultime, ne scegliamo un'altra
				continue;
			}
			String contenuto = validaNotizia(notizia.substring(posizioneSeparatore + 1));
			if (contenuto == null) {
				continue;
			}
			contenuto = contenuto.replace("NOME_LOCANDA", nomeLocanda);
			contenuto = contenuto.replace("NOME_LOCANDIERE", nomeLocandiere);
			if (contenuto.trim().isEmpty()) {
				// Una notizia vuota non si mostra: è un difetto della grammatica, e si prova con un'altra
				BusEventi.pubblica(new InternoErrore("Notizia vuota: " + notizia));
				continue;
			}
			return new NotiziaMD(identificativo, contenuto);
		}
		return null;
	}

	/**
	 * Per validare una notizia occorre controllare se nel corpo appaiono una o più classi personaggio. Per quelle che
	 * appaiono, occorre che ci sia un personaggio della classe corrispondente vivo nel gruppo. In mancanza, la notizia
	 * non è valida.
	 *
	 * @param contenuto il contenuto da controllare
	 * @return null se il contenuto non è applicabile al gruppo dei personaggi, una stringa con le debite sostituzioni fatte altrimenti
	 */
	static String validaNotizia(String contenuto) {
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
			contenuto = concordaConLaClasse(contenuto, "BARDO", TipoPersonaggio.BARDO, TipoPersonaggio.CANTASTORIE);
			contenuto = sostituisci(contenuto, TipoPersonaggio.CANTASTORIE, TipoPersonaggio.BARDO);
			contenuto = sostituisci(contenuto, TipoPersonaggio.BARDO, TipoPersonaggio.CANTASTORIE);
		}
		boolean appareLadro = contenuto.contains(TipoPersonaggio.LADRO.name()) || contenuto.contains(TipoPersonaggio.LADRA.name());
		if (appareLadro) {
			applicabile = classiPresenti.contains(TipoPersonaggio.LADRO) || classiPresenti.contains(TipoPersonaggio.LADRA);
			if (!applicabile) {
				return null;
			}
			contenuto = concordaConLaClasse(contenuto, "LADRO", TipoPersonaggio.LADRO, TipoPersonaggio.LADRA);
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
		Personaggio eroe = GruppoGiocatore.getIstanza().getCapo();
		contenuto = contenuto.replace("EROE", eroe.getNomeProprio().orElseThrow(() -> new IllegalStateException("Personaggio senza nome proprio")));
		contenuto = contenuto.replace("LETTERA_FINALE", eroe.getLetteraFinaleAttributo());
		return contenuto;
	}

	/**
	 * Le notizie il cui soggetto non è il capo ("BARDO viene cacciatLETTERA_FINALE_BARDO") accordano il participio con quel personaggio, non con
	 * il capo (che si accorda con LETTERA_FINALE, vedi validaNotizia). Va fatto prima di sostituire i nomi, che contengono la stessa parola.
	 *
	 * @param classe la classe che dà il nome al segnaposto, di cui si cerca il personaggio vivo (o la sua variante di genere)
	 */
	private static String concordaConLaClasse(String contenuto, String classe, TipoPersonaggio principale, TipoPersonaggio alternativa) {
		String segnaposto = "LETTERA_FINALE_" + classe;
		if (!contenuto.contains(segnaposto)) {
			return contenuto;
		}
		return contenuto.replace(segnaposto, trova(principale, alternativa).getLetteraFinaleAttributo());
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
}
