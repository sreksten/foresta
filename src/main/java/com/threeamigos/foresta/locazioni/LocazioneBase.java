package com.threeamigos.foresta.locazioni;

import java.util.List;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.PortataIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.motore.Azioni;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.RegistroArtefatti;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

/**
 * La locazione e' un automa a stati finiti. Un gruppo mentre si sposta per
 * la foresta si trova all'interno di una locazione. Appena entra la locazione
 * e' in stato NUOVA_LOCAZIONE (vengono creati i mostri e gli oggetti). Quindi
 * il gruppo continua a trovarsi in stato IN_LOCAZIONE. A seconda delle azioni
 * che intraprende puo' spostarsi momentaneamente da tale stato (ad esempio
 * per richiedere il personaggio attivo o il bersaglio di un incantesimo) ma finisce
 * sempre per tornarvi.
 * Il gruppo puo' decidere di combattere e va in stato CHI_COMBATTE,
 * puo' decidere di formulare un incantesimo e va in stato CHI_FORMULA e
 * quindi in stato QUALE_FORMULA; eventualmente se l'incantesimo ha
 * bisogno di un personaggio bersaglio va in stato SU_CHI_FORMULA.
 * Puo' anche tentare una corruzione e va in stato CHI_CORROMPE, o
 * puo' tentare di fare amicizia e va in stato CHI_FA_AMICIZIA.
 * Gli stati sono riferiti al gruppo ma vengono tenuti all'interno della
 * locazione, questo perche' esistono altre locazioni che fanno invece altre
 * cose - la locanda, ad esempio, permette di pernottare o prendere gente con se),
 * la citta' anche (andare da un alchimista, eccetera).
 */

public abstract class LocazioneBase implements Locazione {

	/**
	 * L'elenco dei mostri e degli oggetti che e' possibile trovare
	 * all'interno di questa locazione
	 */
	protected static ClassiPersonaggio[] possibiliIncontri = {};
	protected static ClassiOggetto[] possibiliOggetti = {};

	private GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
	private GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();
	
	private Oggetto oggettoCorrente;

	// Una locazione e' completa se non vi sono più mostri ed il gruppo non
	// e' fuggito; questo serve per sapere se si possono
	// prendere gli oggetti o se i mostri dei castelli sono
	// stati sconfitti.
	protected boolean completa;
	// Se il gruppo puo' (ancora) tentare di corrompere gli avversari
	private boolean opzioneCorruzioneDisponibile;
	// Se il gruppo puo' (ancora) cercare di fare amicizia
	private boolean opzioneAmiciziaDisponibile;
	// Se il gruppo stringe amicizia non può prendere gli oggetti
	private boolean haStrettoAmicizia;
	// Se si riesce a fare amicizia potrebbe essere formulata un'offerta
	private Offerta offerta;

	private StatoLocazione statoLocazione;

	// chi sta combattendo
	private Personaggio combattente;
	// Per formulare un incantesimo
	private Personaggio formulante;
	// Per fare una azione generica
	private Personaggio chiAgisce;
	
	private Incantesimo incantesimo;
	private Gruppo gruppoBersaglio;

	private enum StatoLocazione {
		NUOVA_LOCAZIONE,
		IN_LOCAZIONE,
		CHI_COMBATTE,
		IN_COMBATTIMENTO,
		CHI_BEVE_POZIONE_FORZA,
		CHI_BEVE_POZIONE_GRANDE_FORZA,
		CHI_BEVE_POZIONE_MAGIA,
		CHI_FORMULA,
		QUALE_FORMULA,
		SU_CHI_FORMULA,
		CHI_CORROMPE,
		CHI_FA_AMICIZIA,
		ACCETTA_OFFERTA,
		CONFERMA_FUGA
	}

	protected ClassiPersonaggio[] getPossibiliIncontri() {
		return possibiliIncontri;
	}

	protected ClassiOggetto[] getPossibiliOggetti() {
		return possibiliOggetti;
	}

	/**
	 * Per risparmiare un po' di lavoro alla VM le locazioni vengono
	 * costruite solo all'inizio (una per tipo), e quindi occorre reimpostare
	 * l'automa allo stato iniziale ogni volta.
	 */
	public void reimposta() {
		statoLocazione = StatoLocazione.NUOVA_LOCAZIONE;
		offerta = null;
		completa = false;
		opzioneCorruzioneDisponibile = false;
		opzioneAmiciziaDisponibile = false;
		formulante = null;
		combattente = null;
		setOggetto(null);
	}

	/**
	 * All'interno di una specifica locazione possono essere creati determinati
	 * tipi di mostri e di oggetti. Ogni locazione semplicemente modifica la
	 * getMostri() e la getOggetti() per riportare quale mostro e quale oggetto
	 * possono essere trovati in ogni locazione. La locazione base non ha mostri
	 * ed oggetti associati.
	 */
	public void crea(GruppoGiocatore g, GruppoAvversario avversario) {
		ClassiPersonaggio[] m = getPossibiliIncontri();
		if (m.length > 0) {
			int ordinale = Random.getInt(m.length) + 1;
			// Non sempre si trovano mostri
			if (ordinale < m.length) {
				ClassiPersonaggio classePersonaggio = m[ordinale];
				int numero = 1 + Random.getInt(classePersonaggio.getQuantitaMassima() - 1);
				Logger.log("Scelta da " + m.length + " personaggi la classe " + classePersonaggio + ", numero " + numero);
				Personaggio p = null;
				for (int i = 0; i < numero; i++) {
					p = classePersonaggio.getIstanza();
					p.setOrdinale(i + 1);
					avversario.aggiungiPersonaggio(p);
				}
			} else {
				Logger.log("Per stavolta niente mostri");
			}
		}
		// Vediamo quali artefatti sono presenti in questa locazione.
		Artefatto a = getArtefatto(g);
		if (a != null) {
			setOggetto(a);
		} else if (!isLocazioneVisitata()) {
			// Non ci sono artefatti, creiamo un oggetto.
			ClassiOggetto[] o = getPossibiliOggetti();
			Logger.log("Scelta da " + o.length + " oggetti");
			if (o.length > 0) {
				ClassiOggetto classeOggetto = o[Random.getInt(o.length)];
				Logger.log("Classe oggetto " + classeOggetto);
				Oggetto probabileOggetto = classeOggetto.getIstanza();
				if (probabileOggetto.getQuantita() > 0) {
					setOggetto(probabileOggetto);
				}
			}
		}
	}
	
	protected boolean isLocazioneVisitata() {
		return Foresta.isLocazioneVisitata(gruppo.getCoordinate());
	}

	/**
	 * Restituisce il primo artefatto presente in questa locazione
	 */
	//TODO potremmo generare gli artefatti a caso
	protected Artefatto getArtefatto(GruppoGiocatore g) {
		return RegistroArtefatti.getArtefattoInLocazione(g.getCoordinate());
	}

	/**
	 * All'interno di una specifica locazione possono essere eseguiti determinati tipi di azioni.
	 * La funzione torna lo stato (dell'Automa) in cui si trova il gruppo.
	 *
	 * @param azioni un int[] destinato a tenere tutte le azioni che il gruppo puo'
	 *        fare all'interno dei questa locazione (l'automa di stato della locazione
	 *        puo' tornare valori differenti a seconda del suo stato). L'ultima azione
	 *        DEVE sempre essere Azione.INVALIDA, che indica che non ci sono
	 *        azioni ulteriori.
	 * @param azione l'azione che il gruppo ha deciso di intraprendere; la prima
	 *        volta la funzione viene chiamata con Azione.INVALIDA; dalla successiva
	 *        viene chiamata passandole l'azione scelta dal giocatore.
	 */
	@Override
	public Stato impostaAzioni(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario, Comando azione) {
		String nome;
		Stato possibileStato = null;
		switch (statoLocazione) {
		case NUOVA_LOCAZIONE:
			possibileStato = gestisciNuovaLocazione(azione);
			if (possibileStato != null) {
				return possibileStato;
			}
			break;

		case IN_COMBATTIMENTO:
			possibileStato = gestisciCombattimento(azione);
			if (possibileStato != null) {
				return possibileStato;
			}
			possibileStato = gestisciInLocazione(azione);
			if (possibileStato != null) {
				return possibileStato;
			}
			break;

		case IN_LOCAZIONE:
			possibileStato = gestisciInLocazione(azione);
			if (possibileStato != null) {
				return possibileStato;
			}
			break;

		case CHI_COMBATTE:
			gestisciChiCombatte(azione);
			break;

		case CHI_BEVE_POZIONE_FORZA:
			Logger.log("LocazioneBase.CHI_BEVE_POZIONE_FORZA");
			if (azione != Comando.ANNULLA) {
				chiAgisce = gruppo.getPersonaggio(azione);
				chiAgisce.addForza(100);
				gruppo.subPozioniForza(1);
				UI.notifica(chiAgisce.getNome() + " ha bevuto una pozione che fa riacquistare forza.");
			}
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			impostaAzioni(gruppo, gruppoAvversario, null);
			return Stato.IN_LOCAZIONE;

		case CHI_BEVE_POZIONE_GRANDE_FORZA:
			Logger.log("LocazioneBase.CHI_BEVE_POZIONE_GRANDE_FORZA");
			if (azione != Comando.ANNULLA) {
				chiAgisce = gruppo.getPersonaggio(azione);
				chiAgisce.addForza(chiAgisce.getForzaMassima());
				chiAgisce.addForzaMassima(10);
				gruppo.subPozioniGrandeForza(1);
				UI.notifica(chiAgisce.getNome() + " ha bevuto una pozione che fa aumentare la forza!");
			}
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			impostaAzioni(gruppo, gruppoAvversario, null);
			return Stato.IN_LOCAZIONE;

		case CHI_BEVE_POZIONE_MAGIA:
			Logger.log("LocazioneBase.CHI_BEVE_POZIONE_MAGIA");
			if (azione != Comando.ANNULLA) {
				chiAgisce = gruppo.getPersonaggio(azione);
				chiAgisce.addMagia(10);
				gruppo.subPozioniMagia(1);
				UI.notifica(chiAgisce.getNome() + " ha bevuto una pozione che fa acquistare magia.");
			}
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			impostaAzioni(gruppo, gruppoAvversario, null);
			return Stato.IN_LOCAZIONE;
			
		case CHI_FORMULA:
			Logger.log("LocazioneBase.CHI_FORMULA");
			if (azione == Comando.ANNULLA) {
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;
			}
			formulante = gruppo.getPersonaggio(azione);
			statoLocazione = StatoLocazione.QUALE_FORMULA;
			return Stato.SCELTA_INCANTESIMO_DA_LANCIARE;

		case QUALE_FORMULA:
			Logger.log("LocazioneBase.QUALE_FORMULA: " + azione);
			if (azione == Comando.NO_INCANTESIMO) {
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;
			} else {
				opzioneCorruzioneDisponibile = false;
				opzioneAmiciziaDisponibile = false;
				incantesimo = ClassiIncantesimo.ofComando(azione);
				if (formulante.getMagia() < incantesimo.getCostoLancio()) {
					nome = formulante.getNome();
					StringBuilder sb = new StringBuilder("Il livello di magia ");
					if (nome == null) {
						sb.append(formulante.getDeS()).append(formulante.getNomeSingolare());
					} else {
						sb.append(" di ").append(formulante.getNome());
					}
					sb.append(" non permette di formulare questo incantesimo.");
					UI.notifica(sb.toString());
					rispostaAvversaria(null, gruppo, gruppoAvversario);
					if (!gruppo.getCapo().isVivo()) {
						return Stato.GIOCO_PERSO;
					}
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				}
				PortataIncantesimo tipo = incantesimo.getPortata();
				if (tipo == PortataIncantesimo.GLOBALE || tipo == PortataIncantesimo.GRUPPO) {
					if (tipo == PortataIncantesimo.GLOBALE) {
						incantesimo.formula(formulante, null, null);
					} else {
						incantesimo.formula(formulante, null, gruppoAvversario);
					}
					if (!gruppo.getCapo().isVivo()) {
						return Stato.GIOCO_PERSO;
					}
					gruppo.subIncantesimi(incantesimo.getClasse(), 1);
					if (gruppoAvversario.getNumeroPersonaggiVivi() == 0) {
						completa = true;
						return Stato.FINE_LOCAZIONE;
					}
					rispostaAvversaria(formulante, gruppo, gruppoAvversario);
					if (!gruppo.getCapo().isVivo()) {
						return Stato.GIOCO_PERSO;
					}
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				} else if (tipo == PortataIncantesimo.SINGOLO_SOLO_VIVI || tipo == PortataIncantesimo.SINGOLO_QUALSIASI) {
					Logger.log("Incantesimo di tipo " + (tipo == PortataIncantesimo.SINGOLO_SOLO_VIVI ? "SINGOLO_SOLO_VIVI" : "SINGOLO_QUALSIASI"));
					int l = gruppo.getNumeroPersonaggi();
					Personaggio personaggio;
					Azioni.clear();
					for (int i = 0; i < l; i++) {
						personaggio = gruppo.getPersonaggio(i);
						Logger.log("tipo == Incantesimo.SINGOLO_QUALSIASI || p.isVivo() ? " + (tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo() ? "true" : "false"));
						if (tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo()) {
							Azioni.add(Comando.ofPersonaggio(i));
						}
					}
					statoLocazione = StatoLocazione.SU_CHI_FORMULA;
					gruppoBersaglio = gruppo;
					return Stato.SCELTA_PERSONAGGIO_QUALSIASI;
				}
			}

		case SU_CHI_FORMULA:
			Logger.log("LocazioneBase.SU_CHI_FORMULA");
			if (azione != Comando.ANNULLA) {
				Personaggio personaggioBersaglio = gruppoBersaglio.getPersonaggio(azione);
				incantesimo.formula(formulante, personaggioBersaglio, null);
				gruppo.subIncantesimi(incantesimo.getClasse(), 1);
				rispostaAvversaria(formulante, gruppo, gruppoAvversario);
			}
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			break;

		case CHI_CORROMPE:
			Logger.log("LocazioneBase.CHI_CORROMPE");
			if (azione == Comando.ANNULLA) {
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;
			}
			if (gruppo.getMonete() >= gruppo.getNumeroPersonaggi() * 2 && Random.getInt(10) > 3) {
				gruppo.subMonete(gruppo.getNumeroPersonaggi() * 2);
				UI.notifica(gruppo.chiMaiuscolo() + " ha ottenuto un passaggio sicuro.");
				setOggetto(null);

				offerta = gruppoAvversario.getCapo().getOfferta(Comando.CORRUZIONE);
				if (offerta != null && offerta.isFattibile(gruppo, gruppoAvversario)) {
					UI.notifica(offerta.getDescrizione(gruppo, gruppoAvversario));
					if (offerta.isGratuita(gruppo, gruppoAvversario)) {
						offerta.accetta(gruppo, gruppoAvversario);
					} else {
						UI.notifica("Accetta?");
						Azioni.set(Comando.SI, Comando.NO);
						statoLocazione = StatoLocazione.ACCETTA_OFFERTA;
						return Stato.IN_LOCAZIONE;
					}
				} else {
					Logger.log("Mancano i prerequisiti per l'offerta");
				}
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				completa = true;
				return Stato.FINE_LOCAZIONE;
			} else {
				Personaggio p = gruppo.getPersonaggio(azione);
				nome = p.getNome();
				StringBuilder sb = new StringBuilder("Il tentativo di corruzione ");
				if (nome == null) {
					sb.append(p.getDeS()).append(p.getNomeSingolare());
				} else {
					sb.append(" di ").append(p.getNome());
				}
				sb.append(" non ha avuto successo.");
				UI.notifica(sb.toString());
				opzioneCorruzioneDisponibile = false;
				opzioneAmiciziaDisponibile = false;
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;
			}

		case CHI_FA_AMICIZIA:
			Logger.log("LocazioneBase.CHI_FA_AMICIZIA (azione " + azione + ")");
			if (azione == Comando.ANNULLA) {
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;
			}
			Personaggio personaggio = gruppo.getPersonaggio(azione);
			if (personaggio.getCarisma() > Random.getInt(12)) {
				personaggio.addCarisma(1);
				nome = personaggio.getNome();
				StringBuilder sb = new StringBuilder();
				if (nome == null) {
					sb.append(Character.toUpperCase(personaggio.getADS().charAt(0))).append(personaggio.getADS().substring(1)).append(personaggio.getNomeSingolare());
				} else {
					sb.append(personaggio.getNome());
				}
				sb.append(" riesce a stringere amicizia.");
				haStrettoAmicizia = true;
				UI.notifica(sb.toString());

				offerta = gruppoAvversario.getCapo().getOfferta(Comando.AMICIZIA);
				if (offerta != null && offerta.isFattibile(gruppo, gruppoAvversario)) {
					UI.notifica(offerta.getDescrizione(gruppo, gruppoAvversario));
					if (offerta.isGratuita(gruppo, gruppoAvversario)) {
						offerta.accetta(gruppo, gruppoAvversario);
					} else {
						UI.notifica("Accetta?");
						Azioni.set(Comando.SI, Comando.NO);
						statoLocazione = StatoLocazione.ACCETTA_OFFERTA;
						return Stato.IN_LOCAZIONE;
					}
				} else {
					Logger.log("Mancano i prerequisiti per l'offerta");
				}
				completa = true;
				return Stato.FINE_LOCAZIONE;
			} else {
				int spregio = Random.getInt(5);
				String descrizione = null;
				switch (spregio) {
				case 0:
					ClassiIncantesimo quale = ClassiIncantesimo.casuale();
					Incantesimo qualeIncantesimo = quale.getIstanza();
					if (gruppo.getIncantesimi(quale) > 0) {
						descrizione = new StringBuffer("perde un ").append(qualeIncantesimo.getNomeSingolare()).append('.').toString();
						gruppo.subIncantesimi(quale, 1);
					}
					break;
				case 1:
					Personaggio avversario = gruppoAvversario.getCapo();
					int ferite = Random.getInt(avversario.getForza());
					if (ferite < 20) {
						descrizione = "riceve alcune lievi ferite.";
					} else if (ferite > 40) {
						descrizione = "riceve gravi ferite.";
					} else {
						descrizione = "riceve alcune ferite.";
					}
					personaggio.subForza(ferite, avversario, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
					break;
				case 2:
					if (gruppo.getMonete() > 0) {
						descrizione = "perde alcune monete.";
						int quanteMonetePerde = Random.getInt(5) + 1;
						if (quanteMonetePerde > gruppo.getMonete()) {
							quanteMonetePerde = gruppo.getMonete();
						}
						gruppo.subMonete(quanteMonetePerde);
					}
					break;
				case 3:
					if (gruppo.getPreziosi() > 0) {
						descrizione = "perde alcuni preziosi.";
						int quantiPreziosiPerde = Random.getInt(5) + 1;
						if (quantiPreziosiPerde > gruppo.getPreziosi()) {
							quantiPreziosiPerde = gruppo.getPreziosi();
						}
						gruppo.subPreziosi(quantiPreziosiPerde);
					}
					break;
				default:
					break;
				}
				if (descrizione != null) {
					UI.notifica(new StringBuffer("Non solo ").append(personaggio.getNome()).append(" non riesce a stringere amicizia, ma in una breve collutazione ").append(descrizione).toString());
					UI.rinfresca();
				} else {
					UI.notifica(new StringBuffer(personaggio.getNome()).append(" non riesce a stringere amicizia.").toString());
				}
				opzioneAmiciziaDisponibile = false;
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;
			}

		case ACCETTA_OFFERTA:
			if (azione == Comando.SI) {
				offerta.accetta(gruppo, gruppoAvversario);
			}
			completa = true;
			return Stato.FINE_LOCAZIONE;

		case CONFERMA_FUGA:
			if (azione == Comando.SI) {
				completa = false;
				setOggetto(null);
				gruppo.fugge();
				if (!gruppo.getCapo().isVivo()) {
					return Stato.GIOCO_PERSO;
				} else {
					return Stato.FINE_LOCAZIONE;
				}
			} else if (azione == Comando.NO) {
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
			} else if (azione == Comando.TIMER) {
				return Stato.IN_LOCAZIONE;
			}
		}

		Logger.log("LocazioneBase.impostaAzioni() continua...");
		if (completa) {
			return Stato.FINE_LOCAZIONE;
		}

		Azioni.clear();
		// Possiamo combattere?
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO) {
			Azioni.add(Comando.COMBATTIMENTO);
		}
		// Possiamo formulare incantesimi?
		for (ClassiIncantesimo classeIncantesimo : ClassiIncantesimo.values()) {
			if (gruppo.getIncantesimi(classeIncantesimo) > 0) {
				Azioni.add(Comando.INCANTESIMO);
				break;
			}
		}
		// Possiamo corrompere gli avversari?
		if (opzioneCorruzioneDisponibile) {
			Azioni.add(Comando.CORRUZIONE);
		}
		// Possiamo fare amicizia?
		if (opzioneAmiciziaDisponibile) {
			Azioni.add(Comando.AMICIZIA);
		}
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO) {
			Azioni.add(Comando.MAPPA);
		}
		if (gruppo.getPozioniForza() > 0) {
			Azioni.add(Comando.FORZA);
		}
		if (gruppo.getPozioniGrandeForza() > 0) {
			Azioni.add(Comando.GRANDE_FORZA);
		}
		if (gruppo.getPozioniMagia() > 0) {
			Azioni.add(Comando.MAGIA);
		}
		// Si puo' sempre ricorrere ad una bella...
		Azioni.add(Comando.FUGA);
		// E possiamo sempre richiedere di descrivere di nuovo la locazione
		Azioni.add(Comando.AIUTO);

		return statoLocazione == StatoLocazione.IN_COMBATTIMENTO ? Stato.IN_COMBATTIMENTO : Stato.IN_LOCAZIONE;
	}

	/**
	 * Il giocatore ha portato in fondo la locazione o e' fuggito?
	 */
	public boolean isCompleta() {
		return completa;
	}

	/**
	 * Alla fine di un turno un gruppo rade al suolo una locazione e si può verificare
	 * qualcosa; per adesso al momento in cui il giocatore rade al suolo quattro castelli
	 * appare quello del drago
	 */
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneVisitata();
		}
	}

	/**
	 * La descrizione dei presenti
	 */
	protected String descrizioneMostriEOggetti(GruppoGiocatore g, GruppoAvversario gng) {
		int numeroAvversari = gng.getNumeroPersonaggi();
		Personaggio p = gng.getCapo();
		Oggetto o = getOggetto();
		int numeroOggetti = (o == null ? 0 : o.getQuantita());
		StringBuilder sb = new StringBuilder(g.getCapo().getNome());
		if (numeroAvversari == 0 && numeroOggetti == 0) {
			sb.append(" non trova nulla");
		} else {
			sb.append(" vede ");
		}
		if (numeroAvversari > 0) {
			if (numeroAvversari == 1) {
				sb.append(p.getAIS()).append(p.getNomeSingolare());
				if (numeroOggetti > 0) {
					switch (Random.getInt(3)) {
					case 0:
						sb.append(" che protegge ");
						break;
					case 1:
						sb.append(" che custodisce ");
						break;
					default:
						sb.append(" che sorveglia ");
						break;
					}
				}
			} else {
				sb.append(Misc.getCardinaleM(numeroAvversari)).append(' ').append(p.getNomePlurale());
				if (numeroOggetti > 0) {
					switch (Random.getInt(3)) {
					case 0:
						sb.append(" che proteggono ");
						break;
					case 1:
						sb.append(" che custodiscono ");
						break;
					default:
						sb.append(" che sorvegliano ");
						break;
					}
				}
			}
		}
		if (numeroOggetti > 0) {
			if (numeroOggetti == 1) {
				if (o.getClasse() == ClassiOggetto.ARTEFATTO) {
					Artefatto artefatto = (Artefatto)o;
					sb.append(artefatto.getNome()).append(", ").append(artefatto.getDescrizione());
				} else {
					sb.append(o.getAIS()).append(o.getNomeSingolare());
				}
			} else {
				sb.append(Misc.getCardinaleM(numeroOggetti)).append(' ').append(o.getNomePlurale());
			}
		}
		sb.append('.');
		return sb.toString();
	}

	private void rispostaAvversaria(Personaggio personaggioBersaglio, GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio avversarioAttaccante = null;
		List<Personaggio> avversariVivi = gruppoAvversario.getPersonaggiVivi();
		for (Personaggio avversarioCorrente : avversariVivi) {
			if (avversarioCorrente.isMagico() && avversarioCorrente.getMagia() > 0) {
				avversarioAttaccante = avversarioCorrente;
				break;
			}
		}
		if (avversarioAttaccante == null) {
			for (Personaggio avversarioCorrente : avversariVivi) {
				if (avversarioAttaccante == null || avversarioCorrente.getDanniInCombattimento() > avversarioAttaccante.getDanniInCombattimento()) {
					avversarioAttaccante = avversarioCorrente;
				}
			}
		}
		if (avversarioAttaccante != null) {
			if (personaggioBersaglio == null) {
				avversarioAttaccante.attacca(gruppo);
			} else {
				avversarioAttaccante.attacca(personaggioBersaglio);
			}
		}
	}

	protected void setOggetto(Oggetto oggetto) {
		if (oggetto != null && oggetto.getQuantita() > 0) {
			this.oggettoCorrente = oggetto;
		} else {
			this.oggettoCorrente = null;
		}
	}

	public Oggetto getOggetto() {
		return oggettoCorrente;
	}

	public void rimuoviOggetto() {
		oggettoCorrente = null;
	}

	public boolean isHaStrettoAmicizia() {
		return haStrettoAmicizia;
	}
	
	private Stato gestisciNuovaLocazione(Comando azione) {
		Logger.log("LocazioneBase.NUOVA_LOCAZIONE");
		TipoLocazione tipoLocazione = gruppo.getClasseLocazione().getTipoLocazione();
		int numeroAvversari = gruppoAvversario.getNumeroPersonaggi();
		if (numeroAvversari == 0) {
			if (oggettoCorrente != null) {
				UI.notifica("Essendo il tesoro incustodito, " + gruppo.chi() + " se ne impossessa.");
			}
			if (tipoLocazione != TipoLocazione.MISSIONE_SECONDARIA) {
				gruppo.riposa();
			}
			completa = true;
			return Stato.FINE_LOCAZIONE;
		} else {
			// Non possiamo fare amicizia o corrompere per completare le missioni secondarie!
			//TODO il meccanismo delle missioni andrebbe gestito meglio
			if (tipoLocazione != TipoLocazione.MISSIONE_SECONDARIA) {
				Personaggio p;
				for (int i = 0; i < numeroAvversari; i++) {
					p = gruppoAvversario.getPersonaggio(i);
					if (p.isCorrompibile() && gruppo.getMonete() > 0) {
						opzioneCorruzioneDisponibile = true;
						break;
					}
				}
				// Possiamo fare amicizia?
				for (int i = 0; i < numeroAvversari; i++) {
					p = gruppoAvversario.getPersonaggio(i);
					if (p.isAmichevole()) {
						opzioneAmiciziaDisponibile = true;
						break;
					}
				}
			}
		}
		statoLocazione = StatoLocazione.IN_LOCAZIONE;
		return null;
	}
	
	private Stato gestisciCombattimento(Comando azione) {
		Logger.log("LocazioneBase.IN_COMBATTIMENTO");
		if (azione == Comando.COMBATTIMENTO || azione == Comando.TIMER) {
			Personaggio bersaglio = gruppoAvversario.getPersonaggioVivo();
			if (bersaglio == null) {
				completa = true;
				return Stato.FINE_LOCAZIONE;
			}
			int danniBersaglio = bersaglio.getDanniInCombattimento();
			int danniCombattente = combattente.getDanniInCombattimento();

			combattente.subForza(danniBersaglio, bersaglio, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
			if (!combattente.isVivo()) {
				if (gruppo.getCapo().isVivo()) {
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					return Stato.IN_LOCAZIONE;
				} else {
					return Stato.GIOCO_PERSO;
				}
			}
			bersaglio.subForza(danniCombattente, combattente, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
			if (!bersaglio.isVivo()) {
				if (GruppoGiocatore.getIstanza().contiene(combattente)) {
					Statistiche.addMostroUcciso(bersaglio.getClasse());
					Statistiche.addPunti(bersaglio.getForzaMassima());
				}
				Personaggio nuovoBersaglio = gruppoAvversario.getPersonaggioVivo();
				if (nuovoBersaglio != null) {
					bersaglio = nuovoBersaglio;
				}
			}
			UI.infoCombattimento(true, combattente, bersaglio);

			if (gruppoAvversario.getNumeroPersonaggiVivi() > gruppo.getNumeroPersonaggiVivi()) {
				bersaglio = gruppoAvversario.getPersonaggioVivo();
				StringBuilder sb = new StringBuilder();
				String s = bersaglio.getNome();
				if (s == null) {
					s = bersaglio.getAIS();
					sb.append(Character.toUpperCase(s.charAt(0)));
					sb.append(s.substring(1));
					sb.append(bersaglio.getNomeSingolare());
				} else {
					sb.append(s);
				}
				sb.append(" si disimpegna e attacca!");
				UI.notifica(sb.toString());
				bersaglio.attacca(gruppo);
			}
			if (!gruppo.getCapo().isVivo()) {
				return Stato.GIOCO_PERSO;
			}
		}
		if (azione == Comando.MAPPA) {
			return Stato.MAPPA;
		}
		if (azione == Comando.FORZA) {
			return Stato.ATTESA_FORZA;
		}
		if (azione == Comando.GRANDE_FORZA) {
			return Stato.ATTESA_GRANDE_FORZA;
		}
		if (azione == Comando.MAGIA) {
			return Stato.ATTESA_MAGIA;
		}
		return null;
	}
	
	private Stato gestisciInLocazione(Comando azione) {
		Logger.log("LocazioneBase.IN_LOCAZIONE");
		if (azione != null) {
			switch (azione) {
			case COMBATTIMENTO:
				Logger.log("Azione.COMBATTIMENTO");
				statoLocazione = StatoLocazione.CHI_COMBATTE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				
			case INCANTESIMO:
				Logger.log("Azione.INCANTESIMO");
				statoLocazione = StatoLocazione.CHI_FORMULA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				
			case CORRUZIONE:
				Logger.log("Azione.CORRUZIONE");
				statoLocazione = StatoLocazione.CHI_CORROMPE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				
			case AMICIZIA:
				Logger.log("Azione.AMICIZIA");
				statoLocazione = StatoLocazione.CHI_FA_AMICIZIA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

			case MAPPA:
				Logger.log("Azione.MAPPA");
				return Stato.MAPPA;
				
			case FORZA:
				Logger.log("Azione.POZIONE_FORZA");
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_FORZA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				
			case GRANDE_FORZA:
				Logger.log("Azione.POZIONE_GRANDE_FORZA");
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_GRANDE_FORZA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

			case MAGIA:
				Logger.log("Azione.POZIONE_MAGIA");
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				
			case FUGA:
				Logger.log("Azione.FUGA");
				if (gruppo.getNumeroPersonaggiVivi() > 1) {
					UI.notifica("Il gruppo e' sicuro di voler fuggire?");
				} else {
					StringBuilder sb = new StringBuilder(gruppo.getCapo().getNome());
					sb.append(" e' sicur");
					if (gruppo.getCapo().getSesso() == Personaggio.Sesso.MASCHIO)
						sb.append('o');
					else
						sb.append('a');
					sb.append(" di voler fuggire?");
					UI.notifica(sb.toString());
				}
				statoLocazione = StatoLocazione.CONFERMA_FUGA;
				Azioni.set(Comando.SI, Comando.NO);
				return Stato.ATTESA_SI_NO;
				
			case AIUTO:
				Logger.log("Azione.AIUTO");
				for (Personaggio personaggio : gruppo.getPersonaggi()) {
					UI.notifica(personaggio.getDescrizione());
				}
				int numeroAvversari = gruppoAvversario.getNumeroPersonaggiVivi();
				Personaggio p = gruppoAvversario.getCapo();
				StringBuilder sb = new StringBuilder(gruppo.chiMaiuscolo()).append(" sta affrontando ");
				if (numeroAvversari == 1) {
					sb.append(p.getAIS()).append(p.getNomeSingolare());
				} else {
					sb.append(Misc.getCardinaleM(numeroAvversari)).append(' ').append(p.getNomePlurale());
				}
				sb.append('.');
				UI.notifica(sb.toString());
				UI.primoPiano(InterfacciaUtente.Finestra.STATO);
				break;

			default:
				break;
			}
		}
		return null;
	}

	private void gestisciChiCombatte(Comando azione) {
		Logger.log("LocazioneBase.CHI_COMBATTE");
		if (azione == Comando.ANNULLA) {
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			return;
		}
		combattente = gruppo.getPersonaggio(azione);
		String nome = combattente.getNome();
		if (nome == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(combattente.getADS());
			sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
			sb.append(combattente.getNomeSingolare());
			nome = sb.toString();
		}
		UI.notifica(nome + " si appresta al combattimento.");
		UI.infoCombattimento(true, combattente, gruppoAvversario.getPersonaggioVivo());
		opzioneAmiciziaDisponibile = false;
		statoLocazione = StatoLocazione.IN_COMBATTIMENTO;
	}
}
