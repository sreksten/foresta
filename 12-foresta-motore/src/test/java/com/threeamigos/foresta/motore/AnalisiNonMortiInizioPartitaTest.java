package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Bardo;
import com.threeamigos.foresta.personaggi.Chierico;
import com.threeamigos.foresta.personaggi.Druido;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.GestoreProgressione;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tattiche.EsecutoreTattiche;
import com.threeamigos.foresta.tattiche.RegistroTattiche;
import com.threeamigos.foresta.tipi.CostantiMotore;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Un capo e un compagno, a livello 1 o 2, con l'equipaggiamento iniziale e la dotazione dell'eremita (tre pergamene di aria, acqua e terra, due
 * pozioni di salute), contro due non morti delle rovine (fantasma, spettro, spirito) del livello del mondo. Lo scontro è la mischia automatica
 * del gioco ({@code LocazioneBase.eseguiPassoDellaMischia}): il piano del round di StatoDelCombattimento, la tattica predefinita per i miei e
 * quella della classe per i mostri (RegistroTattiche), chi è atterrato, congelato o stordito salta il turno, a fine round passa un turno di
 * effetti di stato; se cade il capo la partita è persa. Non ci sono il raffreddore (gli starnuti), la fuga, la corruzione e l'amicizia.
 * Per confronto, gli stessi gruppi contro due goblin e due scheletri.
 */
class AnalisiNonMortiInizioPartitaTest {

	private static final int ITERAZIONI = 400;
	private static final int ROUND_MASSIMI = 100;
	private static final List<TipoPersonaggio> CAPI = Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.ELFO,
			TipoPersonaggio.BARDO, TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE, TipoPersonaggio.CHIERICO, TipoPersonaggio.DRUIDO);
	private static final List<TipoPersonaggio> COMPAGNI = CAPI;
	private static final List<TipoPersonaggio> AVVERSARI = Arrays.asList(TipoPersonaggio.FANTASMA, TipoPersonaggio.SPETTRO, TipoPersonaggio.SPIRITO,
			TipoPersonaggio.GOBLIN, TipoPersonaggio.SCHELETRO);
	private static final int QUANTI_AVVERSARI = 2;

	private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) {
		}
	});

	static class Esito {
		boolean vittoria;
		boolean capoCaduto;
		boolean compagnoCaduto;
		int round;
		double danniInflitti;
		double danniSubiti;
	}

	@Disabled("Da eseguire manualmente: due personaggi contro due non morti a inizio partita")
	@Test
	void analisi() {
		PrintStream out = System.out;
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		System.setOut(NULL_STREAM);
		try {
			schede(out);
			riepilogo(out);
			for (int livello : new int[] {1, 2}) {
				for (TipoPersonaggio avversario : AVVERSARI.subList(0, 3)) {
					tabella(out, livello, avversario);
				}
			}
		} finally {
			System.setOut(out);
			BusEventi.azzera();
		}
	}

	/**
	 * Che cosa fanno gli avversari, azione per azione, contro un Guerriero con un Ladro a livello 1, e quanti turni i miei passano senza poter
	 * agire (atterrati, congelati, storditi).
	 */
	@Disabled("Da eseguire manualmente: le azioni dei non morti")
	@Test
	void azioniDegliAvversari() {
		PrintStream out = System.out;
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		System.setOut(NULL_STREAM);
		try {
			for (TipoPersonaggio avversario : AVVERSARI) {
				java.util.Map<String, Integer> azioni = new java.util.TreeMap<>();
				int[] fermi = {0, 0};
				for (int i = 0; i < 200; i++) {
					conteggio = (attore, azione) -> {
						if (attore.getClasse() == avversario) {
							azioni.merge(String.valueOf(azione), 1, Integer::sum);
						}
					};
					contaFermi = fermi;
					scontro(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, avversario, 1);
				}
				out.printf(Locale.ROOT, "%-10s azioni %s; turni dei miei senza agire %d su %d %s%n", avversario, azioni, fermi[0], fermi[1], FERMI_PER_EFFETTO);
				FERMI_PER_EFFETTO.clear();
			}
		} finally {
			conteggio = null;
			contaFermi = null;
			System.setOut(out);
			BusEventi.azzera();
		}
	}

	private static java.util.function.BiConsumer<com.threeamigos.foresta.interfacce.VistaPersonaggio, com.threeamigos.foresta.tipi.TipoAzione> conteggio;
	private static int[] contaFermi;
	private static final java.util.Map<TipoEffettoDiStato, Integer> FERMI_PER_EFFETTO = new java.util.EnumMap<>(TipoEffettoDiStato.class);

	private static void schede(PrintStream out) {
		out.println("== Gli avversari ==");
		for (int livello : new int[] {1, 2}) {
			for (TipoPersonaggio classe : AVVERSARI) {
				nuovaPartita();
				Personaggio m = FabbricaPersonaggi.crea(classe, livello);
				out.printf(Locale.ROOT, "liv %d %-10s salute %3d magia %3d forza %3d int %3d parata %3d resM %3d precisione %3d arma %s%n", livello, classe,
						m.getSaluteMassima(), m.getMagiaMassima(), m.get(TipoAttributo.FORZA), m.get(TipoAttributo.INTELLIGENZA), m.get(TipoAttributo.PARATA),
						m.get(TipoAttributo.RESISTENZA_MAGICA), m.get(TipoAttributo.PRECISIONE),
						m.getArmaEquipaggiata() == null ? "-" : m.getArmaEquipaggiata().getTipoDanno() + " " + m.getArmaEquipaggiata().getDanni());
			}
		}
		out.println();
	}

	/**
	 * Per ogni avversario e livello, su tutte le coppie: partite perse, round medi, salute tolta agli avversari e persa dal gruppo in un round.
	 */
	private static void riepilogo(PrintStream out) {
		riepilogo(out, new int[] {1, 2}, AVVERSARI);
	}

	private static void riepilogo(PrintStream out, int[] livelli, List<TipoPersonaggio> classiAvversarie) {
		out.println("== Riepilogo su tutte le coppie: perse% / round / danno inflitto per round / danno subito per round ==");
		for (int livello : livelli) {
			for (TipoPersonaggio avversario : classiAvversarie) {
				int perse = 0;
				double round = 0;
				double inflitti = 0;
				double subiti = 0;
				int n = 0;
				for (TipoPersonaggio capo : CAPI) {
					for (TipoPersonaggio compagno : COMPAGNI) {
						for (int i = 0; i < ITERAZIONI / 4; i++) {
							Esito esito = scontro(capo, compagno, avversario, livello);
							n++;
							perse += esito.capoCaduto ? 1 : 0;
							round += esito.round;
							inflitti += esito.danniInflitti / esito.round;
							subiti += esito.danniSubiti / esito.round;
						}
					}
				}
				out.printf(Locale.ROOT, "liv %d %-10s perse %5.1f%%  round %5.1f  inflitto/round %5.1f  subito/round %5.1f%n", livello, avversario,
						100.0d * perse / n, round / n, inflitti / n, subiti / n);
			}
		}
		out.println();
	}

	/**
	 * Righe: il capo; colonne: il compagno. In ogni casella: la percentuale di partite perse (il capo cade), e fra parentesi quella degli scontri
	 * in cui cade il compagno.
	 */
	private static void tabella(PrintStream out, int livello, TipoPersonaggio avversario) {
		out.printf(Locale.ROOT, "== livello %d, %d x %s: partita persa%% (compagno caduto%%) ==%n", livello, QUANTI_AVVERSARI, avversario);
		StringBuilder testata = new StringBuilder(String.format(Locale.ROOT, "%-10s", "capo\\comp"));
		for (TipoPersonaggio compagno : COMPAGNI) {
			testata.append(String.format(Locale.ROOT, " | %-11s", compagno.name().substring(0, Math.min(9, compagno.name().length()))));
		}
		testata.append(" | media");
		out.println(testata);
		double totale = 0;
		for (TipoPersonaggio capo : CAPI) {
			StringBuilder riga = new StringBuilder(String.format(Locale.ROOT, "%-10s", capo));
			double rigaPerse = 0;
			for (TipoPersonaggio compagno : COMPAGNI) {
				int perse = 0;
				int compagniCaduti = 0;
				for (int i = 0; i < ITERAZIONI; i++) {
					Esito esito = scontro(capo, compagno, avversario, livello);
					if (esito.capoCaduto) {
						perse++;
					}
					if (esito.compagnoCaduto) {
						compagniCaduti++;
					}
				}
				double p = 100.0d * perse / ITERAZIONI;
				rigaPerse += p;
				riga.append(String.format(Locale.ROOT, " | %4.1f (%4.1f)", p, 100.0d * compagniCaduti / ITERAZIONI));
			}
			riga.append(String.format(Locale.ROOT, " | %4.1f", rigaPerse / COMPAGNI.size()));
			totale += rigaPerse / COMPAGNI.size();
			out.println(riga);
		}
		out.printf(Locale.ROOT, "media generale: %4.1f%% di partite perse%n%n", totale / CAPI.size());
	}

	private static void nuovaPartita() {
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		GruppoGiocatore.azzeraIstanza();
		GruppoAvversario.azzeraIstanza();
		StatoDelCombattimento.azzeraIstanza();
	}

	static Esito scontro(TipoPersonaggio classeCapo, TipoPersonaggio classeCompagno, TipoPersonaggio classeAvversario, int livello) {
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		nuovaPartita();
		if (conteggio != null) {
			BusEventi.iscriviti(com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento.class,
					e -> conteggio.accept(e.getPersonaggio(), e.getAzione()));
		}
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		Personaggio capo = protagonista(classeCapo, "Eroe", livello);
		Personaggio compagno = protagonista(classeCompagno, "Amico", livello);
		gruppo.aggiungiPersonaggio(capo);
		gruppo.aggiungiPersonaggio(compagno);
		for (TipoIncantesimo tipo : new TipoIncantesimo[] {TipoIncantesimo.ARIA, TipoIncantesimo.ACQUA, TipoIncantesimo.TERRA}) {
			gruppo.addIncantesimi(tipo, CostantiMotore.DOTAZIONE_EREMITA_INCANTESIMI_PER_ELEMENTO);
		}
		gruppo.addPozioniSalute(CostantiMotore.DOTAZIONE_EREMITA_POZIONI_SALUTE);
		GruppoAvversario avversari = GruppoAvversario.getIstanza();
		for (int i = 0; i < QUANTI_AVVERSARI; i++) {
			Personaggio mostro = FabbricaPersonaggi.crea(classeAvversario, livello);
			mostro.addMagia(mostro.getMagiaMassima());
			if (variante.vuoto && conIlVuoto(mostro)) {
				// Il Gelo lo sostituisce il Vuoto, che lancia la simulazione: la tattica non deve trovare magia per il Gelo
				mostro.subMagia(mostro.getMagia());
			}
			avversari.aggiungiPersonaggio(mostro);
		}
		StatoDelCombattimento stato = StatoDelCombattimento.getIstanza();
		stato.reimposta();
		java.util.Map<Personaggio, Integer> lanciFatti = new java.util.IdentityHashMap<>();
		double saluteMostri = 0;
		for (Personaggio mostro : avversari.getPersonaggi()) {
			saluteMostri += mostro.getSalute();
		}
		double saluteGruppo = capo.getSalute() + compagno.getSalute();
		EsecutoreTattiche esecutore = new EsecutoreTattiche(gruppo, avversari, stato, (uccisore, vittima) -> {
		});
		Esito esito = new Esito();
		int passi = 0;
		while (stato.getRound() <= ROUND_MASSIMI && passi++ < ROUND_MASSIMI * 10) {
			if (avversari.getPersonaggiVivi().isEmpty()) {
				esito.vittoria = true;
				break;
			}
			if (!capo.isVivo()) {
				break;
			}
			StatoDelCombattimento.Lato lato = stato.getProssimoLato();
			boolean fineRound;
			if (lato == StatoDelCombattimento.Lato.AVVERSARI) {
				Personaggio avversario = stato.getProssimoAttaccante(avversari);
				if (avversario == null) {
					fineRound = stato.registraAzioneSaltata(StatoDelCombattimento.Lato.AVVERSARI);
				} else {
					if (!isIncapaceDiAgire(avversario) && !gruppo.getPersonaggiVivi().isEmpty()) {
						int saluteMiaPrima = saluteDi(gruppo);
						int magiaPrima = avversario.getMagia();
						int lanciFinora = lanciFatti.getOrDefault(avversario, 0);
						if (variante.vuoto && conIlVuoto(avversario) && lanciFinora < variante.limite(avversario.getLivello())
								&& com.threeamigos.foresta.strumenti.dado.Dado.tira(100) <= PROBABILITA_VUOTO) {
							lanciaIlVuoto(avversario, gruppo);
							lanciFatti.put(avversario, lanciFinora + 1);
							magiaPrima = Integer.MAX_VALUE;
						} else {
							esecutore.agisci(avversario, RegistroTattiche.perClasse(avversario.getClasse()));
							if (avversario.getMagia() < magiaPrima) {
								int lanci = lanciFatti.merge(avversario, 1, Integer::sum);
								if (lanci >= variante.limite(avversario.getLivello())) {
									// Ha finito i suoi incantesimi: da qui in poi combatte con le armi
									avversario.subMagia(avversario.getMagia());
								}
							}
						}
						if (fonti != null) {
							fonti.registra(avversario.getMagia() < magiaPrima ? Fonti.INCANTESIMO : Fonti.ARMI, saluteMiaPrima - saluteDi(gruppo));
						}
					} else if (fonti != null) {
						fonti.registra(Fonti.FERMO, 0);
					}
					fineRound = stato.registraAzione(avversario);
				}
			} else if (lato == StatoDelCombattimento.Lato.GIOCATORE) {
				Personaggio attore = stato.getProssimoAttaccante(gruppo);
				if (attore == null) {
					fineRound = stato.registraAzioneSaltata(StatoDelCombattimento.Lato.GIOCATORE);
				} else {
					if (contaFermi != null) {
						contaFermi[1]++;
						contaFermi[0] += isIncapaceDiAgire(attore) ? 1 : 0;
						for (TipoEffettoDiStato effetto : new TipoEffettoDiStato[] {TipoEffettoDiStato.ATTERRATO, TipoEffettoDiStato.CONGELATO,
								TipoEffettoDiStato.STORDITO}) {
							if (attore.hasEffettoDiStato(effetto)) {
								FERMI_PER_EFFETTO.merge(effetto, 1, Integer::sum);
							}
						}
					}
					if (!isIncapaceDiAgire(attore)) {
						esecutore.agisci(attore, RegistroTattiche.predefinitaDelGiocabile(attore.getClasse()));
					}
					fineRound = stato.registraAzione(attore);
				}
			} else {
				// Nessuno può agire: passa comunque un turno di effetti di stato
				fineRound = true;
			}
			if (fineRound && capo.isVivo() && !avversari.getPersonaggiVivi().isEmpty()) {
				int saluteMiaPrima = saluteDi(gruppo);
				gruppo.trascorriUnTurno();
				if (fonti != null) {
					fonti.registra(Fonti.EFFETTI, saluteMiaPrima - saluteDi(gruppo));
				}
				for (Personaggio avversario : avversari.getPersonaggiVivi()) {
					avversario.trascorriUnTurno();
				}
			}
		}
		esito.round = Math.max(1, stato.getRound());
		double saluteMostriAllaFine = 0;
		for (Personaggio mostro : avversari.getPersonaggi()) {
			saluteMostriAllaFine += Math.max(0, mostro.getSalute());
		}
		esito.danniInflitti = saluteMostri - saluteMostriAllaFine;
		esito.danniSubiti = saluteGruppo - Math.max(0, capo.getSalute()) - Math.max(0, compagno.getSalute());
		esito.capoCaduto = !capo.isVivo();
		esito.compagnoCaduto = !compagno.isVivo();
		return esito;
	}

	private static int saluteDi(GruppoGiocatore gruppo) {
		int salute = 0;
		for (Personaggio p : gruppo.getPersonaggi()) {
			salute += Math.max(0, p.getSalute());
		}
		return salute;
	}

	/** Il danno che il gruppo subisce, diviso per fonte: azioni con le armi, incantesimi, effetti di stato a fine round; e i turni persi dei mostri */
	static final class Fonti {
		static final int ARMI = 0;
		static final int INCANTESIMO = 1;
		static final int EFFETTI = 2;
		static final int FERMO = 3;
		final long[] volte = new long[4];
		final long[] danno = new long[4];
		final long[] aSegno = new long[4];

		void registra(int fonte, int quanto) {
			volte[fonte]++;
			// Le pozioni bevute nel frattempo non ci sono: chi beve è dei miei, non agisce durante l'azione del mostro
			if (quanto > 0) {
				danno[fonte] += quanto;
				aSegno[fonte]++;
			}
		}
	}

	private static Fonti fonti;
	/** Le varianti in prova: quanti incantesimi può lanciare un mostro in uno scontro, e se i non morti lanciano il Vuoto invece del Gelo */
	enum Variante {
		SENZA_LIMITE(false), PARI_AL_LIVELLO(false), UNO_PIU_UN_TERZO(false), UNO_PIU_UN_TERZO_COL_VUOTO(true);

		final boolean vuoto;

		Variante(boolean vuoto) {
			this.vuoto = vuoto;
		}

		int limite(int livello) {
			switch (this) {
				case SENZA_LIMITE:
					return Integer.MAX_VALUE;
				case PARI_AL_LIVELLO:
					return livello;
				default:
					return 1 + livello / 3;
			}
		}
	}

	private static Variante variante = Variante.SENZA_LIMITE;
	/** Quante volte su cento un non morto che ha ancora lanci a disposizione lancia il Vuoto invece di attaccare con l'arma */
	private static final int PROBABILITA_VUOTO = 50;

	/**
	 * Un Vuoto di prova: il Gelo, con lo stesso danno base, ma di danno VUOTO (che non mette stati) e su un bersaglio solo.
	 */
	static final class VuotoDiProva extends com.threeamigos.foresta.incantesimi.Gelo {
		VuotoDiProva(int livello) {
			super(livello);
		}

		@Override
		public com.threeamigos.foresta.tipi.TipoDanno getTipoDanno() {
			return com.threeamigos.foresta.tipi.TipoDanno.VUOTO;
		}
	}

	/** Solo i non morti incorporei: altri (il goblin) hanno il Gelo nel repertorio ma non la magia per lanciarlo */
	private static boolean conIlVuoto(Personaggio mostro) {
		return mostro instanceof com.threeamigos.foresta.personaggi.NonMortoVolante;
	}

	/** Il Vuoto su uno dei miei, a caso */
	private static void lanciaIlVuoto(Personaggio mostro, GruppoGiocatore gruppo) {
		List<Personaggio> vivi = new java.util.ArrayList<>(gruppo.getPersonaggiVivi());
		Personaggio bersaglio = vivi.get(com.threeamigos.foresta.strumenti.dado.Dado.tiraAncheAUnaFaccia(vivi.size()) - 1);
		VuotoDiProva vuoto = new VuotoDiProva(mostro.getLivello());
		if (CalcolatoreCombattimento.colpisce(mostro, bersaglio, vuoto.getTipoDanno().getCategoriaDanno())) {
			com.threeamigos.foresta.combattimento.ApplicazioneDelRisultato.applica(bersaglio,
					CalcolatoreCombattimento.calcolaDannoRisultante(mostro, bersaglio, vuoto));
		}
	}

	/**
	 * Il riepilogo per ogni variante: senza limite, lanci pari al livello, 1 + livello/3, e 1 + livello/3 col Vuoto a bersaglio singolo al posto del Gelo.
	 */
	@Disabled("Da eseguire manualmente: un limite ai lanci dei mostri, e il Vuoto al posto del Gelo")
	@Test
	void limiteAiLanci() {
		PrintStream out = System.out;
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		System.setOut(NULL_STREAM);
		try {
			List<TipoPersonaggio> classi = Arrays.asList(TipoPersonaggio.FANTASMA, TipoPersonaggio.SPETTRO, TipoPersonaggio.SPIRITO, TipoPersonaggio.GOBLIN);
			for (Variante v : Variante.values()) {
				variante = v;
				out.println("######## " + v);
				riepilogo(out, new int[] {1, 2, 3, 4}, classi);
			}
		} finally {
			variante = Variante.SENZA_LIMITE;
			System.setOut(out);
			BusEventi.azzera();
		}
	}

	/**
	 * Da dove viene il danno: per ogni avversario, su tutte le coppie a livello 1 e 2, quante azioni con le armi e quanti incantesimi, quanti
	 * vanno a segno e quanto fanno, e quanto fanno gli effetti di stato a fine round. Poi, a tavolino, ogni classe dei miei (livello 1, equipaggiamento
	 * iniziale) contro ogni avversario: difese, probabilità di essere colpita e danno medio di un colpo d'arma e di un incantesimo.
	 */
	@Disabled("Da eseguire manualmente: da dove viene il danno dei non morti")
	@Test
	void daDoveVieneIlDanno() {
		PrintStream out = System.out;
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
		System.setOut(NULL_STREAM);
		try {
			out.println("== Il danno subito dal gruppo, per fonte (su tutte le coppie) ==");
			out.println("avversario      | armi: azioni a segno% danno/azione | incantesimi: azioni% a segno% danno/azione | effetti/round | turni persi%");
			for (int livello : new int[] {1, 2}) {
				for (TipoPersonaggio avversario : AVVERSARI) {
					fonti = new Fonti();
					for (TipoPersonaggio capo : CAPI) {
						for (TipoPersonaggio compagno : COMPAGNI) {
							for (int i = 0; i < ITERAZIONI / 8; i++) {
								scontro(capo, compagno, avversario, livello);
							}
						}
					}
					long azioni = fonti.volte[Fonti.ARMI] + fonti.volte[Fonti.INCANTESIMO];
					out.printf(Locale.ROOT, "liv %d %-10s | armi %6d %5.1f%% %5.1f | inc %5.1f%% %5.1f%% %5.1f | effetti %5.1f | fermi %5.1f%%%n", livello, avversario,
							fonti.volte[Fonti.ARMI], percentuale(fonti.aSegno[Fonti.ARMI], fonti.volte[Fonti.ARMI]), media(fonti.danno[Fonti.ARMI], fonti.volte[Fonti.ARMI]),
							percentuale(fonti.volte[Fonti.INCANTESIMO], azioni), percentuale(fonti.aSegno[Fonti.INCANTESIMO], fonti.volte[Fonti.INCANTESIMO]),
							media(fonti.danno[Fonti.INCANTESIMO], fonti.volte[Fonti.INCANTESIMO]), media(fonti.danno[Fonti.EFFETTI], fonti.volte[Fonti.EFFETTI]),
							percentuale(fonti.volte[Fonti.FERMO], azioni + fonti.volte[Fonti.FERMO]));
				}
			}
			fonti = null;
			out.println();
			aTavolino(out);
		} finally {
			fonti = null;
			System.setOut(out);
			BusEventi.azzera();
		}
	}

	private static double percentuale(long parte, long tutto) {
		return tutto == 0 ? 0 : 100.0d * parte / tutto;
	}

	private static double media(long somma, long volte) {
		return volte == 0 ? 0 : (double) somma / volte;
	}

	private static void aTavolino(PrintStream out) {
		out.println("== A tavolino, livello 1: difese dei miei, e per ogni avversario colpito% x danno medio di un colpo d'arma (e dell'incantesimo di Gelo) ==");
		StringBuilder testata = new StringBuilder(String.format(Locale.ROOT, "%-10s %-28s", "classe", "salute parata costit resM"));
		for (TipoPersonaggio avversario : AVVERSARI) {
			testata.append(String.format(Locale.ROOT, " | %-24s", avversario));
		}
		out.println(testata);
		for (TipoPersonaggio classe : CAPI) {
			nuovaPartita();
			Personaggio pg = protagonista(classe, "Eroe", 1);
			StringBuilder riga = new StringBuilder(String.format(Locale.ROOT, "%-10s %6d %6d %8d %4d       ", classe, pg.getSaluteMassima(),
					pg.get(TipoAttributo.PARATA), pg.get(TipoAttributo.COSTITUZIONE), pg.get(TipoAttributo.RESISTENZA_MAGICA)));
			for (TipoPersonaggio classeAvversario : AVVERSARI) {
				Personaggio mostro = FabbricaPersonaggi.crea(classeAvversario, 1);
				com.threeamigos.foresta.artefatti.Arma arma = mostro.getArmaEquipaggiata();
				String colpo = arma == null ? "-" : String.format(Locale.ROOT, "%3d%% x %4.1f", CalcolatoreCombattimento.calcolaProbabilitaDiColpire(mostro, pg,
						arma.getTipoDanno().getCategoriaDanno()), dannoMedio(mostro, pg, arma));
				String incantesimo = "";
				if (mostro.getRepertorioIncantesimi().contains(TipoIncantesimo.GELO)) {
					com.threeamigos.foresta.incantesimi.IncantesimoMalefico gelo = (com.threeamigos.foresta.incantesimi.IncantesimoMalefico)
							com.threeamigos.foresta.incantesimi.FabbricaIncantesimi.crea(TipoIncantesimo.GELO, 1);
					incantesimo = String.format(Locale.ROOT, " (%3d%% x %4.1f)", CalcolatoreCombattimento.calcolaProbabilitaDiColpire(mostro, pg,
							gelo.getTipoDanno().getCategoriaDanno()), dannoMedio(mostro, pg, gelo));
				}
				riga.append(String.format(Locale.ROOT, " | %-24s", colpo + incantesimo));
			}
			out.println(riga);
		}
	}

	private static double dannoMedio(Personaggio attaccante, Personaggio difensore, com.threeamigos.foresta.artefatti.Arma arma) {
		double totale = 0;
		int volte = 2_000;
		for (int i = 0; i < volte; i++) {
			totale += CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma).getDanno();
		}
		return totale / volte;
	}

	private static boolean isIncapaceDiAgire(Personaggio personaggio) {
		return personaggio.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) || personaggio.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO)
				|| personaggio.hasEffettoDiStato(TipoEffettoDiStato.STORDITO);
	}

	/**
	 * Un personaggio giocante con un nome (senza nome sarebbe un PNG), l'equipaggiamento iniziale e, a livello 2, l'esperienza che serve; a salute e
	 * magia piene.
	 */
	private static Personaggio protagonista(TipoPersonaggio classe, String nome, int livello) {
		Personaggio pg;
		switch (classe) {
			case GUERRIERO:
				pg = new Guerriero(nome, 1);
				break;
			case LADRO:
				pg = new Ladro(nome, 1);
				break;
			case ELFO:
				pg = new Elfo(nome, 1);
				break;
			case BARDO:
				pg = new Bardo(nome, 1);
				break;
			case MAGO:
				pg = new Mago(nome, 1);
				break;
			case SACERDOTE:
				pg = new Sacerdote(nome, 1);
				break;
			case CHIERICO:
				pg = new Chierico(nome, 1);
				break;
			case DRUIDO:
				pg = new Druido(nome, 1);
				break;
			default:
				throw new IllegalArgumentException(classe.toString());
		}
		pg.getModelloDati().setPng(false);
		EquipaggiamentoIniziale.equipaggia(pg, livello);
		if (livello > 1) {
			pg.addPuntiEsperienza(GestoreProgressione.getXpNecessariPerLivello(livello));
		}
		pg.addSalute(pg.getSaluteMassima());
		pg.addMagia(pg.getMagiaMassima());
		return pg;
	}
}
