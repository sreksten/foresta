package com.threeamigos.foresta.testi;

import java.util.ArrayList;
import java.util.List;

/**
 * I testi a caso delle locande ({@code locande.txt}): i dati delle locande del mondo (nome, locandiere, dialogo, recensione), le
 * reazioni del locandiere e le righe grezze delle notizie. Quale notizia si racconta al gruppo, e come si adatta ai suoi
 * personaggi, lo decide il motore (vedi nomi.md).
 */
public final class ProduttoreDiLocandeCasuale {

	private static final GrammaticaPigra LOCANDE = GrammaticaPigra.deiTesti("locande.txt", true);

	private ProduttoreDiLocandeCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		LOCANDE.get();
	}

	/**
	 * Una reazione a caso del locandiere, per esempio quando il gruppo gli dice cosa vuole.
	 */
	public static String reazioneLocandiere() {
		// Niente reset: REAZIONE_LOCANDIERE non usa produzioni monouso, non si esaurisce
		return LOCANDE.get().produce("REAZIONE_LOCANDIERE").get(0);
	}

	/**
	 * Una notizia della locanda, come la scrive la grammatica: {@code ID-corpo}, dove il corpo può nominare le classi dei personaggi
	 * (da adattare al gruppo). Le notizie non sono monouso: la grammatica non si esaurisce.
	 *
	 * @param identificativoLocanda l'identificativo della locanda: si pesca da {@code NOTIZIE_<identificativo>}
	 */
	public static String notiziaGrezza(String identificativoLocanda) {
		return LOCANDE.get().produce("NOTIZIE_" + identificativoLocanda).get(0);
	}

	/**
	 * Estrae i dati di tante locande (nome, locandiere, dialogo, recensione), una volta per tutte, alla creazione del mondo
	 * (vedi Foresta.reimposta): i nomi dei locandieri si pescano dalle liste one-shot di nomi.txt, che si azzerano con
	 * {@link ProduttoriDiTesti#resetProduzioni()}.
	 * <p>
	 * <b>Non va più chiamato a partita iniziata.</b> A partita avviata le sezioni statiche di nomi.txt si azzerano ogni volta che
	 * i nomi delle missioni finiscono (vedi {@code produciDaMissioni}), e con loro si ricostruiscono le locande one-shot di
	 * locande.txt: nuove locande potrebbero ripetere il nome di una già estratta o il nome di un residente (vedi
	 * RegistroPersonaggi).
	 */
	public static List<DatiLocanda> getDatiLocanda(int quantita) {
		return getDatiLocanda(quantita, LOCANDE.get());
	}

	// I dati da una grammatica data: serve a provare le righe malformate, che la grammatica del gioco non ha
	static List<DatiLocanda> getDatiLocanda(int quantita, GrammarBean locande) {
		List<DatiLocanda> datiLocanda = new ArrayList<>();
		for (int i = 0; i < quantita; i++) {
			List<String> produzioni = locande.produce();
			if (produzioni.size() > 1) {
				throw new IllegalArgumentException("La grammatica produce più di una riga di produzione per " + locande.getRootNode());
			}
			// Con -1 i campi finali vuoti restano (una locanda senza dialogo o senza recensione)
			String[] token = produzioni.get(0).split("/", -1);
			if (token.length < 5) {
				throw new IllegalArgumentException("La produzione di una locanda ha " + token.length
						+ " campi invece di 5 (nome/identificativo/locandiere/recensione/dialogo): " + produzioni.get(0));
			}
			String nomeLocanda = token[0];
			String identificativoLocanda = token[1];
			String nomeLocandiere = token[2];
			String recensioneLocanda = token[3];
			String dialogoLocanda = token[4];
			final String chiaveRecensione = "RECENSIONE=";
			if (recensioneLocanda.startsWith(chiaveRecensione)) {
				recensioneLocanda = recensioneLocanda.substring(chiaveRecensione.length());
			} else {
				recensioneLocanda = "";
			}
			final String chiaveDialogo = "DIALOGO=";
			if (dialogoLocanda.startsWith(chiaveDialogo)) {
				dialogoLocanda = dialogoLocanda.substring(chiaveDialogo.length());
			} else {
				dialogoLocanda = "";
			}
			datiLocanda.add(new DatiLocanda(nomeLocanda, identificativoLocanda, nomeLocandiere, dialogoLocanda, recensioneLocanda));
		}
		return datiLocanda;
	}

	/**
	 * Azzera le liste esauribili della grammatica (vedi {@link ProduttoriDiTesti#resetProduzioni()}).
	 */
	static void reset() {
		LOCANDE.get().reset();
	}

	/**
	 * I dati di una locanda del mondo, estratti da {@code locande.txt}: come si chiama, chi la gestisce e cosa dice a chi entra.
	 */
	public static class DatiLocanda {
		private final String nome;
		private final String identificativo;
		private final String nomeLocandiere;
		private final String dialogo;
		private final String recensione;
		// Li costruisce solo getDatiLocanda
		DatiLocanda(String nome, String identificativo, String nomeLocandiere, String dialogo, String recensione) {
			this.nome = nome;
			this.identificativo = identificativo;
			this.nomeLocandiere = nomeLocandiere;
			this.dialogo = dialogo;
			this.recensione = recensione;
		}

		/**
		 * Il nome della locanda, con l'articolo.
		 */
		public String getNome() {
			return nome;
		}

		/**
		 * L'identificativo della locanda, da cui dipendono le sue notizie ({@code NOTIZIE_<identificativo>}).
		 */
		public String getIdentificativo() {
			return identificativo;
		}

		/**
		 * Il nome del locandiere.
		 */
		public String getNomeLocandiere() {
			return nomeLocandiere;
		}

		/**
		 * Quello che dice il locandiere alla prima visita; vuoto se la locanda non ha un dialogo.
		 */
		public String getDialogo() {
			return dialogo;
		}

		/**
		 * La recensione della locanda; vuota se non ne ha una.
		 */
		public String getRecensione() {
			return recensione;
		}
	}
}
