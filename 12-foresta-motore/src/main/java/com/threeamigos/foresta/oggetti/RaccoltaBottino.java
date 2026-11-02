package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoArtefattoTrovato;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStatistiche;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.eventi.notifiche.NotificaArtefattoTrovato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.intermezzi.IntermezzoLibroTecnico;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.motore.LocazioneCorrente;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.RegoleEquipaggiamento;
import com.threeamigos.foresta.statistiche.EsperienzaDelGruppo;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

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
			Cassa.addMonete(oggetto.getQuantita());
		} else if (oggetto instanceof Corona || oggetto instanceof PietraPreziosa) {
			Cassa.addPreziosi(oggetto.getQuantita());
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
	// e dà qualche punto in più; uno non magico non ha effetto, ma si vende come una gemma: vale un prezioso per anello.
	private static boolean raccogliAnello(Anello anello, GruppoGiocatore gruppo, Comando azione) {
		if (anello.isMagico()) {
			if (!anello.isMagiaAnnunciata()) {
				BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "RaccoltaBottino.anelloMagico." + anello.getTipoDiMagia())));
				anello.segnaMagiaAnnunciata();
			}
			if (!raccogliArtefatto(gruppo, azione, anello.getArtefatto().orElseThrow(IllegalStateException::new))) {
				return false;
			}
			Statistiche.addPunti(CostantiBottino.ANELLO_MAGICO_PUNTEGGIO);
		} else {
			BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "RaccoltaBottino.1")));
			Cassa.addPreziosi(anello.getQuantita());
		}
		return true;
	}

	// Il gruppo apre il cofano: per ogni cofano trovato un esito a caso (vedi Cofano)
	private static void apri(Cofano cofano, GruppoGiocatore gruppo) {
		TipoLocazione tipoLocazione = Foresta.getLocazioneDelGruppo();
		int min;
		int max;
		int tipo;
		if (tipoLocazione.getCategoriaLocazione() == CategoriaLocazione.CASTELLO) {
			min = 1;
			max = 7;
		} else {
			min = 0;
			max = 8;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cofano.getQuantita(); i++) {
			tipo = Dado.tira(ContestoLancio.COFANO_ESITO, min, max);
			// Di rado, al posto dell'esito solito, una pergamena o un artefatto casuale, che vanno nel gruppo
			Artefatto trovato = Cofano.artefattoRaro();
			// "Aprendo il cofano, " o "Aprendo il secondo forziere, ": con più cofani si dice quale
			boolean cofanoOForziere = Dado.tira(2) == 1;
			sb.append(Testi.formatta("messaggi_motore", (cofano.getQuantita() > 1 ? "RaccoltaBottino.apre.n." : "RaccoltaBottino.apre.") + (cofanoOForziere ? "cofano" : "forziere"),
					Lingue.corrente().ordinale(i + 1, Genere.MASCHILE, false)));
			// In italiano il soggetto si dice una volta ("Aprendo il cofano, il gruppo trova... Aprendo il secondo, trova..."); in inglese ogni volta
			if (i == 0 || Boolean.parseBoolean(Testi.formatta("messaggi_motore", "RaccoltaBottino.soggettoOgniVolta"))) {
				sb.append(gruppo.chi()).append(' ');
			}
			if (trovato != null) {
				gruppo.addArtefatto(trovato);
				registraArtefattoOttenuto(gruppo, trovato);
				BusEventi.pubblica(new NotificaArtefattoTrovato(trovato, Statistiche.getLivello()));
				LibroTecnico.di(trovato).ifPresent(IntermezzoLibroTecnico::trovato);
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.trovaArtefatto", trovato.getNomeCompleto()));
			} else if (tipo == 0) {
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.nulla"));
			} else if (tipo == 1) {
				TipoIncantesimo tipoIncantesimo = FabbricaIncantesimi.casuale();
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.pergamenaUno", tipoIncantesimo.getNomeSingolare()));
				gruppo.addIncantesimi(tipoIncantesimo, 1);
			} else if (tipo == 2) {
				TipoIncantesimo tipoIncantesimo = FabbricaIncantesimi.casuale();
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.pergamenaTre", tipoIncantesimo.getNomePlurale()));
				gruppo.addIncantesimi(tipoIncantesimo, 3);
			} else if (tipo == 3) {
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.pozioneSalute"));
				gruppo.addPozioniSalute(1);
			} else if (tipo == 4) {
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.pozioneSaluteGrande"));
				gruppo.addPozioniSaluteGrande(1);
			} else if (tipo == 5) {
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.pozioneMagia"));
				gruppo.addPozioniMagia(1);
			} else if (tipo == 6) {
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.panacea"));
				gruppo.addPozioniPanacea(1);
			} else if (tipo == 7) {
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.monete"));
				Cassa.addMonete(Dado.tira(5, 10));
			} else if (tipo == 8) {
				sb.append(Testi.formatta("messaggi_motore", "RaccoltaBottino.preziosi"));
				Cassa.addPreziosi(Dado.tira(5, 10));
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
					BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("messaggi_motore", "RaccoltaBottino.2", comeSoggetto(artefatto))));
				} else {
					BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "RaccoltaBottino.3", comeSoggetto(artefatto))));
				}
				return true;
			}
			if (candidati.size() > 1) {
				BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "RaccoltaBottino.chiPrende", artefatto.getNomeCompleto())));
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
		for (int posizione : gruppo.chiPuoEquipaggiare(artefatto)) {
			candidati.add(Comando.ofPersonaggio(posizione));
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
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_motore", "RaccoltaBottino.4", comeSoggetto(artefatto))));
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
		BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("messaggi_motore", "RaccoltaBottino.5", comeSoggetto(artefatto), personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE), motivoRifiuto.get())));
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
