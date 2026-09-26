package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

/**
 * Partite vere (senza la modalità di prova) giocate da un giocatore automatico, per avere dei numeri sul
 * bilanciamento: quanto dura un personaggio, a che livello arriva, quanti mostri uccide, quanti danni subisce e da
 * chi, quando finisce pozioni e pergamene e come finisce la partita.
 * <p>
 * La strategia del giocatore è descritta in {@link GiocatoreAutomatico}: in breve stima ogni scontro prima di
 * affrontarlo (combatte, corrompe, fa amicizia o fugge), fa combattere prima i compagni, beve pozioni, si
 * accampa di notte, va in locanda e in città quando è ferito o ha monete da spendere, recluta, compra pozioni,
 * pergamene e la mappa della Foresta, e altrimenti esplora scegliendo gli spostamenti meno rischiosi.
 * <p>
 * I danni subiti si misurano sul gruppo del giocatore (compagni compresi) come somma delle perdite di salute di
 * ogni personaggio fra un comando e l'altro: le cure non li compensano. Uno scontro è una visita a una locazione
 * con avversari (non locande né città), dal primo sguardo alla scelta della direzione; i round sono gli impulsi
 * della mischia più le azioni a cui gli avversari rispondono (incantesimi, dardi). I danni della fuga si contano
 * a parte.
 */
class SimulazionePartiteTest {

	private static final int PARTITE_PER_CLASSE = 30;
	private static final Comando[] CLASSI = {Comando.GUERRIERO, Comando.LADRO, Comando.ELFO, Comando.BARDO, Comando.MAGO};
	/**
	 * Oltre questi passi (comandi e impulsi) la partita si considera bloccata o infinita e si interrompe
	 */
	private static final int PASSI_MASSIMI = 60_000;

	@Disabled("Da eseguire manualmente per avere i numeri del bilanciamento")
	@Test
	void simulaPartite() {
		stampa(System.out, PARTITE_PER_CLASSE);
	}

	/**
	 * Come finisce una partita
	 */
	enum Esito {
		MORTO, VINTO, TEMPO_SCADUTO, BLOCCATO, ERRORE_DEL_GIOCO
	}

	/**
	 * I numeri di uno scontro, per la tabella degli avversari
	 */
	static final class Scontro {
		final ClassePersonaggio avversario;
		final boolean inCastello;
		int danni;
		int round;
		boolean mortoUnPersonaggio;
		boolean fuga;
		boolean vinto;

		Scontro(ClassePersonaggio avversario, boolean inCastello) {
			this.avversario = avversario;
			this.inCastello = inCastello;
		}
	}

	/**
	 * I numeri di una partita
	 */
	static final class Risultato {
		Esito esito;
		int spostamenti;
		int giorni;
		int livello;
		int mostriUccisi;
		int combattimenti;
		int pozioniBevute;
		int incantesimiLanciati;
		/**
		 * A quale spostamento sono finite le pergamene (-1 se non sono finite)
		 */
		int spostamentoFinePergamene = -1;
		String ultimoStato;
		int reclutati;
		int mercenari;
		int visiteLocande;
		int visiteCitta;
		int resurrezioni;
		int fughe;
		int corruzioni;
		int amicizie;
		int moneteSpese;
		int moneteNeiNegozi;
		int moneteDaVendite;
		int pozioniComprate;
		int pergameneComprate;
		int mappeComprate;
		int danniFuga;
		int compagniMorti;
		/**
		 * Chi ha ucciso il capo: la classe dell'avversario, "fuga", "tempo"...
		 */
		String causaMorte;
		final List<Scontro> scontri = new ArrayList<>();

		int danniNegliScontri() {
			return scontri.stream().mapToInt(s -> s.danni).sum();
		}

		int roundNegliScontri() {
			return scontri.stream().mapToInt(s -> s.round).sum();
		}
	}

	static void stampa(PrintStream out, int partitePerClasse) {
		PrintStream originale = System.out;
		// Il Logger scrive su System.out: durante le partite si zittisce
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
			}
		}));
		try {
			Map<Comando, List<Risultato>> perClasse = new EnumMap<>(Comando.class);
			for (Comando classe : CLASSI) {
				List<Risultato> risultati = new ArrayList<>();
				for (int i = 0; i < partitePerClasse; i++) {
					risultati.add(gioca(1000L * classe.ordinal() + i, classe));
				}
				perClasse.put(classe, risultati);
			}
			out.printf("Partite senza la modalità di prova, %d per classe (medie per partita)%n", partitePerClasse);
			out.printf("%-9s %5s %5s %5s %6s %4s %6s %6s %7s %6s %7s %7s %7s %12s%n", "Classe", "Morti", "Vinte", "Tempo",
					"Blocc.", "Err.", "Spost.", "Giorni", "Livello", "Uccisi", "Combatt", "Pozioni", "Incant.", "FinePerg.");
			perClasse.forEach((classe, risultati) -> stampaEsiti(out, classe, risultati));
			out.println();
			out.printf("%-9s %6s %6s %6s %6s %6s %6s %6s %6s %7s %7s %7s %7s %8s %8s %8s%n", "Classe", "Recl.",
					"Merc.", "Locan.", "Citta", "Resur.", "Fughe", "Corruz", "Amic.", "CompMor", "Monete",
					"Negozi", "PozCom", "Dan/comb", "Dan/rnd", "Dan/fuga");
			perClasse.forEach((classe, risultati) -> stampaRisorse(out, classe, risultati));
			out.println();
			out.println("Cause della morte del capo (partite)");
			perClasse.forEach((classe, risultati) -> stampaCauseMorte(out, classe, risultati));
			out.println();
			List<Risultato> tutti = new ArrayList<>();
			perClasse.values().forEach(tutti::addAll);
			stampaAvversari(out, tutti);
			perClasse.forEach((classe, risultati) -> risultati.stream()
					.filter(r -> r.esito == Esito.BLOCCATO || r.esito == Esito.ERRORE_DEL_GIOCO).limit(3)
					.forEach(r -> out.println(classe + " " + r.esito + ": " + r.ultimoStato)));
		} finally {
			System.setOut(originale);
		}
	}

	private static void stampaEsiti(PrintStream out, Comando classe, List<Risultato> risultati) {
		out.printf("%-9s %5d %5d %5d %6d %4d %6.1f %6.1f %7.1f %6.1f %7.1f %7.1f %7.1f %12s%n", classe,
				conta(risultati, Esito.MORTO), conta(risultati, Esito.VINTO), conta(risultati, Esito.TEMPO_SCADUTO),
				conta(risultati, Esito.BLOCCATO), conta(risultati, Esito.ERRORE_DEL_GIOCO),
				media(risultati, r -> r.spostamenti), media(risultati, r -> r.giorni), media(risultati, r -> r.livello),
				media(risultati, r -> r.mostriUccisi), media(risultati, r -> r.combattimenti),
				media(risultati, r -> r.pozioniBevute), media(risultati, r -> r.incantesimiLanciati),
				mediaSeFinite(risultati, r -> r.spostamentoFinePergamene));
	}

	private static void stampaRisorse(PrintStream out, Comando classe, List<Risultato> risultati) {
		double scontri = risultati.stream().mapToInt(r -> r.scontri.size()).sum();
		double round = risultati.stream().mapToInt(Risultato::roundNegliScontri).sum();
		double danni = risultati.stream().mapToInt(Risultato::danniNegliScontri).sum();
		double fughe = risultati.stream().mapToInt(r -> r.fughe).sum();
		double danniFuga = risultati.stream().mapToInt(r -> r.danniFuga).sum();
		out.printf("%-9s %6.1f %6.1f %6.1f %6.1f %6.1f %6.1f %6.1f %6.1f %7.1f %7.1f %7.1f %7.1f %8.1f %8.1f %8.1f%n", classe,
				media(risultati, r -> r.reclutati), media(risultati, r -> r.mercenari),
				media(risultati, r -> r.visiteLocande), media(risultati, r -> r.visiteCitta),
				media(risultati, r -> r.resurrezioni), media(risultati, r -> r.fughe),
				media(risultati, r -> r.corruzioni), media(risultati, r -> r.amicizie),
				media(risultati, r -> r.compagniMorti), media(risultati, r -> r.moneteSpese),
				media(risultati, r -> r.moneteNeiNegozi), media(risultati, r -> r.pozioniComprate),
				scontri == 0 ? 0 : danni / scontri, round == 0 ? 0 : danni / round, fughe == 0 ? 0 : danniFuga / fughe);
	}

	private static void stampaCauseMorte(PrintStream out, Comando classe, List<Risultato> risultati) {
		Map<String, Integer> cause = new TreeMap<>();
		risultati.stream().filter(r -> r.esito == Esito.MORTO)
				.forEach(r -> cause.merge(r.causaMorte == null ? "?" : r.causaMorte, 1, Integer::sum));
		StringBuilder sb = new StringBuilder(String.format("%-9s", classe));
		cause.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue())
				.forEach(e -> sb.append(' ').append(e.getKey()).append(' ').append(e.getValue()).append(';'));
		out.println(sb);
	}

	/**
	 * La tabella per classe di avversario, su tutte le partite
	 */
	private static void stampaAvversari(PrintStream out, List<Risultato> risultati) {
		Map<String, List<Scontro>> perAvversario = new TreeMap<>();
		for (Risultato r : risultati) {
			for (Scontro s : r.scontri) {
				String nome = s.avversario + (s.inCastello ? " (castello)" : "");
				perAvversario.computeIfAbsent(nome, k -> new ArrayList<>()).add(s);
			}
		}
		out.println("Scontri per classe di avversario (tutte le partite): danni subiti dal gruppo");
		out.printf("%-26s %7s %9s %8s %8s %8s %7s %7s%n", "Avversario", "Scontri", "Dan/comb", "Dan/rnd", "Rnd/comb",
				"%morto", "%fuga", "%vinti");
		perAvversario.forEach((nome, scontri) -> {
			double danni = scontri.stream().mapToInt(s -> s.danni).sum();
			double round = scontri.stream().mapToInt(s -> s.round).sum();
			out.printf("%-26s %7d %9.1f %8.1f %8.1f %7.0f%% %6.0f%% %6.0f%%%n", nome, scontri.size(),
					danni / scontri.size(), round == 0 ? 0 : danni / round, round / scontri.size(),
					100.0 * scontri.stream().filter(s -> s.mortoUnPersonaggio).count() / scontri.size(),
					100.0 * scontri.stream().filter(s -> s.fuga).count() / scontri.size(),
					100.0 * scontri.stream().filter(s -> s.vinto).count() / scontri.size());
		});
	}

	private static long conta(List<Risultato> risultati, Esito esito) {
		return risultati.stream().filter(r -> r.esito == esito).count();
	}

	private static double media(List<Risultato> risultati, ToDoubleFunction<Risultato> misura) {
		return risultati.stream().mapToDouble(misura).average().orElse(0);
	}

	/**
	 * La media fra le partite in cui la cosa è finita, con quante sono su quante (es. "12.5 (8/30)")
	 */
	private static String mediaSeFinite(List<Risultato> risultati, ToDoubleFunction<Risultato> misura) {
		double[] valori = risultati.stream().mapToDouble(misura).filter(v -> v >= 0).toArray();
		if (valori.length == 0) {
			return "- (0/" + risultati.size() + ")";
		}
		return String.format("%.1f (%d/%d)", Arrays.stream(valori).average().orElse(0), valori.length, risultati.size());
	}

	/**
	 * Una partita dall'inizio alla fine (o fino a PASSI_MASSIMI)
	 */
	static Risultato gioca(long seme, Comando classe) {
		Risultato risultato = new Risultato();
		try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(seme)) {
			partita.iniziaCon("Prova", Comando.MASCHIO, classe, () -> {
			});
			GiocatoreAutomatico giocatore = new GiocatoreAutomatico(partita, new Random(seme));
			Osservatore osservatore = new Osservatore(partita, risultato);
			boolean eraInCombattimento = false;
			for (int passo = 0; passo < PASSI_MASSIMI; passo++) {
				Stato stato = partita.stato();
				if (stato == Stato.GIOCO_PERSO || stato == Stato.GIOCO_VINTO) {
					osservatore.fine(stato);
					break;
				}
				if (stato == Stato.IN_COMBATTIMENTO && !eraInCombattimento) {
					risultato.combattimenti++;
				}
				eraInCombattimento = stato == Stato.IN_COMBATTIMENTO;
				if (stato == Stato.SCELTA_DIREZIONE) {
					risultato.spostamenti++;
					registraRisorse(partita.gruppo(), risultato);
				}
				osservatore.primaDelPasso(stato);
				Comando comando = giocatore.scegli();
				if (comando == Comando.POZIONE_SALUTE || comando == Comando.POZIONE_SALUTE_GRANDE) {
					risultato.pozioniBevute++;
				} else if (stato == Stato.INCANTESIMO_SCELTO && comando != null && comando != Comando.NO_INCANTESIMO) {
					risultato.incantesimiLanciati++;
				}
				boolean fuga = stato == Stato.ATTESA_SI_NO && comando == Comando.SI;
				osservatore.comando(stato, comando);
				try {
					if (comando == null) {
						partita.scatta();
					} else {
						partita.comando(comando);
					}
				} catch (AssertionError | RuntimeException e) {
					// Un errore del gioco: la partita si ferma qui e si annota
					risultato.esito = Esito.ERRORE_DEL_GIOCO;
					risultato.ultimoStato = "seme " + seme + ", " + stato + " + " + comando + ": " + e.getMessage();
					break;
				}
				osservatore.dopoIlPasso(fuga);
			}
			if (risultato.esito == null) {
				risultato.esito = Esito.BLOCCATO;
				risultato.ultimoStato = partita.stato() + " " + partita.comandiDisponibili() + " - " + partita.ultimoTesto();
			}
			risultato.giorni = LineaTemporale.getGiorno();
			risultato.livello = Statistiche.getLivello();
			risultato.mostriUccisi = Arrays.stream(ClassePersonaggio.values()).mapToInt(Statistiche::getMostriUccisi).sum();
			risultato.fughe = giocatore.fughe;
			risultato.corruzioni = giocatore.corruzioni;
			risultato.amicizie = giocatore.amicizie;
			risultato.resurrezioni = giocatore.resurrezioni;
			risultato.moneteNeiNegozi = giocatore.moneteNeiNegozi;
			risultato.moneteDaVendite = giocatore.moneteDaVendite;
			risultato.pozioniComprate = giocatore.pozioniComprate;
			risultato.pergameneComprate = giocatore.pergameneComprate;
			risultato.mappeComprate = giocatore.mappeComprate;
		}
		return risultato;
	}

	private static void registraRisorse(GruppoGiocatore gruppo, Risultato risultato) {
		int pergamene = Arrays.stream(ClasseIncantesimo.values())
				.filter(c -> c != ClasseIncantesimo.RESURREZIONE)
				.mapToInt(gruppo::getIncantesimi).sum();
		if (pergamene == 0 && risultato.spostamentoFinePergamene < 0) {
			risultato.spostamentoFinePergamene = risultato.spostamenti;
		}
	}

	/**
	 * Guarda la partita passo per passo e ne ricava i numeri che non dipendono dalla strategia: danni subiti,
	 * scontri, visite, reclutamenti, monete spese, cause di morte.
	 */
	private static final class Osservatore {

		private final PartitaDiTest partita;
		private final Risultato risultato;
		private final Map<Personaggio, Integer> salutePrima = new IdentityHashMap<>();
		private Locazione locazione;
		private Scontro scontro;
		private int monetePrima;
		private int personaggiPrima;
		private int viviPrima;

		Osservatore(PartitaDiTest partita, Risultato risultato) {
			this.partita = partita;
			this.risultato = risultato;
		}

		void primaDelPasso(Stato stato) {
			GruppoGiocatore gruppo = partita.gruppo();
			Locazione corrente = gruppo.getLocazioneCorrente();
			if (corrente != locazione) {
				chiudiScontro();
				locazione = corrente;
				if (corrente != null) {
					ClassiLocazione classe = corrente.getClasseLocazione();
					if (classe == ClassiLocazione.LOCANDA) {
						risultato.visiteLocande++;
					} else if (classe.getTipoLocazione() == TipoLocazione.CITTA) {
						risultato.visiteCitta++;
					} else {
						List<Personaggio> avversari = GruppoAvversario.getIstanza().getPersonaggiVivi();
						if (!avversari.isEmpty()) {
							scontro = new Scontro(avversari.get(0).getClasse(),
									classe.getTipoLocazione() == TipoLocazione.CASTELLO);
							risultato.scontri.add(scontro);
						}
					}
				}
			}
			if (stato == Stato.SCELTA_DIREZIONE) {
				chiudiScontro();
			}
			salutePrima.clear();
			for (Personaggio p : gruppo.getPersonaggi()) {
				salutePrima.put(p, p.getSalute());
			}
			monetePrima = gruppo.getMonete();
			personaggiPrima = gruppo.getNumeroPersonaggi();
			viviPrima = gruppo.getNumeroPersonaggiVivi();
		}

		void comando(Stato stato, Comando comando) {
			if (scontro == null) {
				return;
			}
			if (stato == Stato.IN_COMBATTIMENTO && comando == null) {
				scontro.round++;
			} else if (stato == Stato.INCANTESIMO_SCELTO && comando != null && comando != Comando.NO_INCANTESIMO) {
				scontro.round++;
			}
		}

		void dopoIlPasso(boolean fuga) {
			GruppoGiocatore gruppo = partita.gruppo();
			int danni = 0;
			for (Map.Entry<Personaggio, Integer> prima : salutePrima.entrySet()) {
				if (gruppo.getPersonaggi().contains(prima.getKey())) {
					danni += Math.max(0, prima.getValue() - prima.getKey().getSalute());
				}
			}
			if (fuga) {
				risultato.danniFuga += danni;
				if (scontro != null) {
					scontro.fuga = true;
				}
			} else if (scontro != null) {
				scontro.danni += danni;
			}
			int viviDopo = gruppo.getNumeroPersonaggiVivi();
			if (viviDopo < viviPrima) {
				int morti = viviPrima - viviDopo;
				if (gruppo.getCapo().isVivo()) {
					risultato.compagniMorti += morti;
				}
				if (scontro != null) {
					scontro.mortoUnPersonaggio = true;
				}
			}
			if (!gruppo.getCapo().isVivo() && risultato.causaMorte == null) {
				risultato.causaMorte = fuga ? "fuga" : scontro != null ? scontro.avversario.name() : "fuori dagli scontri";
			}
			int monete = gruppo.getMonete();
			if (monete < monetePrima && !fuga) {
				risultato.moneteSpese += monetePrima - monete;
			}
			if (gruppo.getNumeroPersonaggi() > personaggiPrima && locazione != null) {
				ClassiLocazione classe = locazione.getClasseLocazione();
				if (classe == ClassiLocazione.LOCANDA || classe.getTipoLocazione() == TipoLocazione.CITTA) {
					risultato.reclutati += gruppo.getNumeroPersonaggi() - personaggiPrima;
				} else {
					risultato.mercenari += gruppo.getNumeroPersonaggi() - personaggiPrima;
				}
			}
		}

		void fine(Stato stato) {
			chiudiScontro();
			if (stato == Stato.GIOCO_VINTO) {
				risultato.esito = Esito.VINTO;
			} else if (partita.gruppo().getCapo().isVivo()) {
				risultato.esito = Esito.TEMPO_SCADUTO;
			} else {
				risultato.esito = Esito.MORTO;
			}
		}

		private void chiudiScontro() {
			if (scontro != null) {
				scontro.vinto = GruppoAvversario.getIstanza().getPersonaggiVivi().isEmpty() && !scontro.fuga
						&& partita.gruppo().getCapo().isVivo();
				scontro = null;
			}
		}
	}
}
