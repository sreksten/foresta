package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.LocazioneCorrente;

import com.threeamigos.foresta.motore.EsperienzaDelGruppo;

import com.threeamigos.foresta.mappa.CategoriaLocazione;

import com.threeamigos.foresta.mappa.TipoLocazione;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoArtefattoTrovato;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStatistiche;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.motore.GestoreProgressione;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegoleEquipaggiamento;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.tipi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Cosa succede quando il gruppo prende il bottino che ha trovato in una locazione: le monete e le pietre vanno nella cassa del
 * gruppo, un cofano si apre, l'oggetto di una missione la avvisa, un artefatto va a chi può usarlo o all'inventario del gruppo.
 * Poi, per ogni cosa presa, si assegnano i punti del suo valore e si riaprono le finestre di stato e di statistiche.
 * <p>
 * Gli oggetti del bottino sono solo dati (cosa sono, quanti ne sono, quale artefatto portano): tutto ciò che toccano nel mondo
 * lo fa questa classe, e non loro.
 *
 * @author Stefano Reksten
 */
public final class RaccoltaBottino {

	private RaccoltaBottino() {
	}

	/**
	 * Il gruppo prende il bottino.
	 *
	 * @param azione il personaggio che prende l'oggetto: {@code null} (o {@code Comando.TIMER}) lascia scegliere al gioco,
	 *               {@code Comando.GRUPPO} lo mette nell'inventario del gruppo, {@code PERSONAGGIO_n} lo dà a quel personaggio
	 * @return true se è stato preso; false se occorre dire chi lo prende (più personaggi possono usarlo e il comando non
	 *         lo indica)
	 */
	public static boolean raccogli(Bottino oggetto, GruppoGiocatore gruppo, Comando azione) {
		if (oggetto instanceof BottinoArtefatto) {
			if (!raccogliArtefatto(gruppo, azione, oggetto.getArtefatto().orElseThrow(IllegalStateException::new))) {
				return false;
			}
		} else if (oggetto instanceof Anello) {
			if (!raccogliAnello((Anello) oggetto, gruppo, azione)) {
				return false;
			}
		} else if (oggetto instanceof Cofano) {
			apri((Cofano) oggetto, gruppo);
		} else if (oggetto instanceof Moneta) {
			gruppo.addMonete(oggetto.getQuantita());
		} else if (oggetto instanceof Corona || oggetto instanceof PietraPreziosa) {
			gruppo.addPreziosi(oggetto.getQuantita());
		} else if (oggetto instanceof OggettoMissione) {
			OggettoMissione dellaMissione = (OggettoMissione) oggetto;
			RegistroMissioni.getMissione(dellaMissione.getIdMissione())
					.filter(MissioneAPassi.class::isInstance)
					.ifPresent(m -> ((MissioneAPassi) m).oggettoDiMissioneRaccolto(dellaMissione.getChiave(), dellaMissione.getQuantita()));
		}
		Statistiche.addPunti(oggetto.getClasse().getValore() * oggetto.getQuantita());
		BusEventi.pubblica(new InternoMostraFinestraStato());
		BusEventi.pubblica(new InternoMostraFinestraStatistiche());
		return true;
	}

	// Un anello magico porta un artefatto, che si annuncia la prima volta (un anello rifiutato e ripreso non si annuncia due volte)
	// e dà qualche punto in più; uno non magico si conta come ogni altro oggetto.
	private static boolean raccogliAnello(Anello anello, GruppoGiocatore gruppo, Comando azione) {
		if (anello.isMagico()) {
			if (!anello.isMagiaAnnunciata()) {
				StringBuilder sb = new StringBuilder("Questo è un anello magico, che fa aumentare ");
				if (anello.getTipoDiMagia() == 1)
					sb.append("il valore in combattimento");
				else if (anello.getTipoDiMagia() == 2)
					sb.append("il coraggio");
				else
					sb.append("il carisma");
				sb.append("!");
				BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
				anello.segnaMagiaAnnunciata();
			}
			if (!raccogliArtefatto(gruppo, azione, anello.getArtefatto().orElseThrow(IllegalStateException::new))) {
				return false;
			}
			Statistiche.addPunti(Costanti.ANELLO_MAGICO_PUNTEGGIO);
		}
		return true;
	}

	// Il gruppo apre il cofano: per ogni cofano trovato un esito a caso (vedi Cofano)
	private static void apri(Cofano cofano, GruppoGiocatore gruppo) {
		TipoLocazione tipoLocazione = gruppo.getTipoLocazioneCorrente();
		int min;
		int max;
		int tipo;
		if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CASTELLO) {
			min = 1;
			max = 6;
		} else {
			min = 0;
			max = 7;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cofano.getQuantita(); i++) {
			tipo = Dado.tira(ContestoLancio.COFANO_ESITO, min, max);
			// Di rado, al posto dell'esito solito, una pergamena o un artefatto casuale, che vanno nel gruppo
			Artefatto trovato = Cofano.artefattoRaro();
			sb.append("Aprendo il");
			if (cofano.getQuantita() > 1) {
				sb.append(' ').append(Numeri.getOrdinaleM(i + 1, false));
			}
			if (Dado.tira(2) == 1) {
				sb.append(" cofano, ");
			} else {
				sb.append(" forziere, ");
			}
			if (i == 0) {
				sb.append(gruppo.chi()).append(' ');
			}
			if (trovato != null) {
				gruppo.addArtefatto(trovato);
				registraArtefattoOttenuto(gruppo, trovato);
				BusEventi.pubblica(new NotificaArtefattoTrovato(trovato, Statistiche.getLivello()));
				LibroTecnico.di(trovato).ifPresent(IntermezzoLibroTecnico::trovato);
				sb.append("trova ").append(trovato.getNomeCompleto()).append(": finisce nell'inventario del gruppo.");
			} else if (tipo == 0) {
				sb.append("non trova nulla.");
			} else if (tipo == 1) {
				sb.append("trova una pergamena con un ");
				TipoIncantesimo tipoIncantesimo = FabbricaIncantesimi.casuale();
				sb.append(tipoIncantesimo.getNomeSingolare()).append('.');
				gruppo.addIncantesimi(tipoIncantesimo, 1);
			} else if (tipo == 2) {
				sb.append("trova una pergamena con tre ");
				TipoIncantesimo tipoIncantesimo = FabbricaIncantesimi.casuale();
				sb.append(tipoIncantesimo.getNomePlurale()).append('.');
				gruppo.addIncantesimi(tipoIncantesimo, 3);
			} else if (tipo == 3) {
				sb.append("trova una pozione della salute.");
				gruppo.addPozioniSalute(1);
			} else if (tipo == 4) {
				sb.append("trova una pozione della salute grande.");
				gruppo.addPozioniSaluteGrande(1);
			} else if (tipo == 5) {
				sb.append("trova una pozione della magia.");
				gruppo.addPozioniMagia(1);
			} else if (tipo == 6) {
				sb.append("trova alcune monete d'oro.");
				gruppo.addMonete(Dado.tira(5, 10));
			} else if (tipo == 7) {
				sb.append("trova alcune pietre preziose.");
				gruppo.addPreziosi(Dado.tira(5, 10));
			}
			if (i < cofano.getQuantita() - 1) {
				sb.append(' ');
			}
		}
		BusEventi.pubblica(new NotificaTestoFrase(sb.toString()));
	}

	/**
	 * Chi raccoglie un artefatto (vedi artefatti_e_incantamenti.md, §2 "Loot"). Senza comando si guardano
	 * i candidati, cioè i personaggi vivi che possono equipaggiarlo ({@link #candidati}):
	 * <ul>
	 * <li>nessuno (anche perché chi c'è ha già occupato quello slot, o è una pergamena): va nel gruppo;</li>
	 * <li>uno solo: va direttamente a lui;</li>
	 * <li>più di uno: sceglie il giocatore (Stato.SCELTA_DESTINATARIO_OGGETTO) e si restituisce false.</li>
	 * </ul>
	 * Con GRUPPO va nell'inventario del gruppo, con un personaggio a quel personaggio.
	 *
	 * @return true se l'artefatto è stato raccolto
	 */
	public static boolean raccogliArtefatto(GruppoGiocatore gruppo, Comando comando, Artefatto artefatto) {
		if (!consegnaArtefatto(gruppo, comando, artefatto)) {
			return false;
		}
		registraArtefattoOttenuto(gruppo, artefatto);
		return true;
	}

	// Un artefatto ottenuto (da un cofano, dal bottino, da un tempio, da un anello magico) fa salire un poco di livello e conta per i
	// trofei dei tesori degli avversari; gli ingredienti magici no, come nei trofei degli acquisti.
	private static void registraArtefattoOttenuto(GruppoGiocatore gruppo, Artefatto artefatto) {
		if (artefatto.getTipo().isIngrediente()) {
			return;
		}
		Locazione locazione = LocazioneCorrente.get();
		BusEventi.pubblica(new InternoArtefattoTrovato(artefatto, locazione != null && locazione.isCustodita()));
		EsperienzaDelGruppo.acquisisciArtefattoMinore();
	}

	// Chi prende l'artefatto: vedi raccogliArtefatto
	private static boolean consegnaArtefatto(GruppoGiocatore gruppo, Comando comando, Artefatto artefatto) {
		if (comando == null || comando == Comando.TIMER) {
			List<Comando> candidati = candidati(gruppo, artefatto);
			if (candidati.isEmpty()) {
				gruppo.addArtefatto(artefatto);
				if (troppoPesante(gruppo, artefatto)) {
					BusEventi.pubblica(new NotificaTestoParagrafo(comeSoggetto(artefatto)
							+ " è troppo pesante da portare, quindi viene messo nell'inventario del gruppo."));
				} else {
					BusEventi.pubblica(new NotificaTestoFrase(comeSoggetto(artefatto)
							+ " viene messo nell'inventario del gruppo: nessuno può utilizzarlo in questo momento."));
				}
				return true;
			}
			if (candidati.size() > 1) {
				BusEventi.pubblica(new NotificaTestoFrase("Chi prende " + artefatto.getNomeCompleto() + "?"));
				return false;
			}
			comando = candidati.get(0);
		}
		if (comando == Comando.GRUPPO) {
			riponiNelGruppo(gruppo, artefatto);
			return true;
		}
		Personaggio p = gruppo.getPersonaggio(comando.getIndicePersonaggio());
		if (consegna(gruppo, p, artefatto)) {
			BusEventi.pubblica(new NotificaTestoFrase(p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA)
					+ ' ' + artefatto.getTipo().getUtilizzo() + ' ' + artefatto.getNomeCompleto() + '.'));
		}
		return true;
	}

	/**
	 * I comandi dei personaggi vivi che possono equipaggiare l'artefatto: slot libero, livello
	 * sufficiente, mani libere, carico. Le pergamene non hanno mai candidati. Chi è a tempo non è
	 * un candidato: se ne andrebbe subito portandosi via l'artefatto.
	 */
	public static List<Comando> candidati(GruppoGiocatore gruppo, Artefatto artefatto) {
		List<Comando> candidati = new ArrayList<>();
		List<Personaggio> personaggi = gruppo.getPersonaggi();
		for (int i = 0; i < personaggi.size(); i++) {
			Personaggio personaggio = personaggi.get(i);
			if (personaggio.isVivo() && !personaggio.isATempo() && !personaggio.puoEquipaggiare(artefatto).isPresent()) {
				candidati.add(Comando.ofPersonaggio(i));
			}
		}
		return candidati;
	}

	/**
	 * true se almeno un personaggio vivo potrebbe equipaggiare l'artefatto e ne è impedito solo dal peso
	 * (TROPPO_CARICO è l'ultimo controllo di {@link Personaggio#puoEquipaggiare}).
	 */
	private static boolean troppoPesante(GruppoGiocatore gruppo, Artefatto artefatto) {
		for (Personaggio personaggio : gruppo.getPersonaggi()) {
			if (personaggio.isVivo() && !personaggio.isATempo() && personaggio.puoEquipaggiare(artefatto)
					.filter(esito -> esito.getMotivo() == TipoMotivoRifiutoEquipaggiamento.TROPPO_CARICO).isPresent()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Mette l'artefatto raccolto nell'inventario del gruppo, per scelta del giocatore.
	 */
	private static void riponiNelGruppo(GruppoGiocatore gruppo, Artefatto artefatto) {
		gruppo.addArtefatto(artefatto);
		BusEventi.pubblica(new NotificaTestoFrase(comeSoggetto(artefatto)
				+ " viene messo nell'inventario del gruppo."));
	}

	/**
	 * Dà l'artefatto al personaggio se lo può equipaggiare (peso, slot, livello...), altrimenti lo mette
	 * nell'inventario del gruppo spiegando il perché. Restituisce true se l'ha preso il personaggio.
	 */
	private static boolean consegna(GruppoGiocatore gruppo, Personaggio personaggio, Artefatto artefatto) {
		Optional<RegoleEquipaggiamento.EsitoControlloRichiestaEquipaggiamento> motivoRifiuto = personaggio.puoEquipaggiare(artefatto);
		if (!motivoRifiuto.isPresent()) {
			personaggio.addArtefatto(artefatto);
			return true;
		}
		gruppo.addArtefatto(artefatto);
		// TODO oggi i personaggi del gruppo non si vedono a video: quando ci saranno, il
		// rifiuto andrà mostrato anche lì (es. un fumetto sul personaggio).
		BusEventi.pubblica(new NotificaTestoParagrafo(comeSoggetto(artefatto)
				+ " resta nell'inventario del gruppo: "
				+ personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
				+ ' ' + motivoRifiuto.get() + '.'));
		return false;
	}

	/**
	 * Forma completa con l'iniziale maiuscola, da usare come soggetto a inizio frase: se contiene
	 * un inciso ("Diavolina, la spada di fuoco, che brucia i nemici") lo chiude con una virgola.
	 */
	private static String comeSoggetto(Artefatto artefatto) {
		String nome = artefatto.getNomeCompleto();
		nome = nome.substring(0, 1).toUpperCase(Locale.ROOT) + nome.substring(1);
		return nome.contains(", ") ? nome + ',' : nome;
	}

}
