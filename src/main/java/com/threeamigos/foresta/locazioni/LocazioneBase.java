package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.PortataIncantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione.TipoLocazione;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.EffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.offerte.Offerta;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

import java.util.List;

/**
 * La locazione è un automa a stati finiti. Un gruppo mentre si sposta per
 * la foresta si trova all'interno di una locazione. Appena entra la locazione
 * è in stato NUOVA_LOCAZIONE (vengono creati i mostri e gli oggetti). Quindi
 * il gruppo continua a trovarsi in stato IN_LOCAZIONE. A seconda delle azioni
 * che intraprende può spostarsi momentaneamente da tale stato (ad esempio
 * per richiedere il personaggio attivo o il bersaglio di un incantesimo) ma finisce
 * sempre per tornarvi.
 * Il gruppo può decidere di combattere e va in stato CHI_COMBATTE,
 * può decidere di formulare un incantesimo e va in stato CHI_FORMULA e
 * quindi in stato QUALE_FORMULA; eventualmente se l'incantesimo ha
 * bisogno di un personaggio bersaglio va in stato SU_CHI_FORMULA.
 * Può anche tentare una corruzione e va in stato CHI_CORROMPE, o
 * può tentare di fare amicizia e va in stato CHI_FA_AMICIZIA.
 * Gli stati sono riferiti al gruppo ma vengono tenuti all'interno della
 * locazione, questo perché esistono altre locazioni che fanno invece altre
 * cose - la locanda, ad esempio, permette di pernottare o prendere gente con se),
 * la città anche (andare da un alchimista, eccetera).
 */

public abstract class LocazioneBase implements Locazione {

	private static final ClassePersonaggio[] NESSUN_INCONTRO = {};
	private static final ClassiOggetto[] NESSUN_OGGETTO = {};

	private final GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
	private final GruppoAvversario gruppoAvversario = GruppoAvversario.getIstanza();

	/**
	 * Lo stato durevole della casella: nome, visitata, conosciuta, completa.
	 * Sopravvive alla visita e al salvataggio.
	 */
	private LocazioneMD md = new LocazioneMD(getClasseLocazione());

	private Oggetto oggettoCorrente;
	// Se il gruppo può (ancora) tentare di corrompere gli avversari
	private boolean opzioneCorruzioneDisponibile;
	// Se il gruppo può (ancora) cercare di fare amicizia
	private boolean opzioneAmiciziaDisponibile;
	// Se il gruppo stringe amicizia non può prendere gli oggetti
	private boolean haStrettoAmicizia;
	// Se si riesce a fare amicizia potrebbe essere formulata un'offerta
	private Offerta offerta;

	private StatoLocazione statoLocazione;

	// Chi sta combattendo
	private Personaggio combattente;

    private Incantesimo incantesimo;
	private Gruppo gruppoBersaglio;

	private enum StatoLocazione {
		NUOVA_LOCAZIONE,
		IN_LOCAZIONE,
		CHI_COMBATTE,
		IN_COMBATTIMENTO,
		CHI_BEVE_POZIONE_SALUTE,
		CHI_BEVE_POZIONE_SALUTE_GRANDE,
		CHI_BEVE_POZIONE_MAGIA,
		CHI_BEVE_POZIONE_MAGIA_GRANDE,
		CHI_FORMULA,
		QUALE_FORMULA,
		SU_CHI_FORMULA,
		CHI_CORROMPE,
		CHI_FA_AMICIZIA,
		ACCETTA_OFFERTA,
		CONFERMA_FUGA
	}

	/**
	 * L'elenco dei mostri e degli oggetti che è possibile trovare all'interno di
	 * questa locazione. Ogni locazione che ne ha sovrascrive questi due getter.
	 */
	protected ClassePersonaggio[] getPossibiliIncontri() {
		return NESSUN_INCONTRO;
	}

	protected ClassiOggetto[] getPossibiliOggetti() {
		return NESSUN_OGGETTO;
	}

	@Override
	public LocazioneMD getModelloDati() {
		return md;
	}

	@Override
	public void setModelloDati(LocazioneMD modelloDati) {
		this.md = modelloDati;
	}

	@Override
	public String getNome() {
		return md.getNome();
	}

	/**
	 * Una locazione è completa se non vi sono più mostri e il gruppo non
	 * è fuggito; questo serve per sapere se si possono prendere gli oggetti
	 * o se i mostri dei castelli sono stati sconfitti.
	 */
	protected void setCompleta(boolean completa) {
		if (completa) {
			md.aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
		} else {
			md.rimuoviProprieta(LocazioneMD.COMPLETA);
		}
	}

	protected LocazioneBase() {
		statoLocazione = StatoLocazione.NUOVA_LOCAZIONE;
		gruppo.setFormulante(null);
	}

	/**
	 * All'interno di una specifica locazione possono essere creati determinati
	 * tipi di mostri e di oggetti. Ogni locazione semplicemente modifica la
	 * getMostri() e la getOggetti() per riportare quale mostro e quale oggetto
	 * possono essere trovati in ogni locazione. La locazione base non ha mostri
	 * e oggetti associati.
	 */
	public void crea(GruppoGiocatore g, GruppoAvversario avversario) {
		ClassePersonaggio[] m = getPossibiliIncontri();
		if (m.length > 0) {
			int possibilitaIncontro = Dado.tira(100);
			// Non sempre si trovano mostri
			if (possibilitaIncontro <= 90) {
				int ordinale = Dado.tiraAncheAUnaFaccia(m.length) - 1;
				ClassePersonaggio classePersonaggio = m[ordinale];

				// FASE 1: Calcolo del CAP base in base al LIVELLO DEL GIOCATORE (Regola Principale)
				int livelloGiocatore = g.getCapo().getLivello();
				int capLivello;
				if (livelloGiocatore <= 5) {
					capLivello = 2;  // Massimo 2 mostri a inizio gioco per evitare il collasso immediato
				} else if (livelloGiocatore <= 15) {
					capLivello = 3;  // Massimo 3 mostri a metà gioco
				} else {
					capLivello = 5;  // Massimo 5 mostri per livelli alti / party completi
				}

				int limiteMassimoIncontro = Math.min(capLivello, classePersonaggio.getQuantitaMassima());

				int numero = Dado.tiraAncheAUnaFaccia(limiteMassimoIncontro);
				Logger.log("Scelta da " + m.length + " personaggi la classe " + classePersonaggio + ", numero " + numero + " con cap " + capLivello);
				Personaggio p;
				for (int i = 0; i < numero; i++) {
					p = classePersonaggio.getIstanza(Statistiche.getLivello());
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
				int indice = Dado.tiraAncheAUnaFaccia(o.length) - 1;
				ClassiOggetto classeOggetto = o[indice];
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
	protected Artefatto getArtefatto(GruppoGiocatore g) {
		return RegistroArtefatti.getArtefattoInLocazione(g.getCoordinate());
	}

	/**
	 * All'interno di una specifica locazione possono essere eseguiti determinati tipi di azioni.
	 * La funzione torna lo stato (dell'Automa) in cui si trova il gruppo.
	 *
	 * @param azione l'azione che il gruppo ha deciso di intraprendere; la prima
	 *        volta la funzione viene chiamata con Azione.INVALIDA; dalla successiva
	 *        viene chiamata passandole l'azione scelta dal giocatore.
	 */
	@Override
	public Stato impostaAzioni(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario, Comando azione) {
        Stato possibileStato;
        switch (statoLocazione) {
		case NUOVA_LOCAZIONE:
			possibileStato = gestisciNuovaLocazione();
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

		case CHI_BEVE_POZIONE_SALUTE:
			Logger.log("LocazioneBase.CHI_BEVE_POZIONE_SALUTE");
			if (azione != Comando.ANNULLA) {
				gruppo.consumaPozioneSalute(azione);
			}
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			impostaAzioni(gruppo, gruppoAvversario, null);
			return Stato.IN_LOCAZIONE;

		case CHI_BEVE_POZIONE_SALUTE_GRANDE:
			Logger.log("LocazioneBase.CHI_BEVE_POZIONE_SALUTE_GRANDE");
			if (azione != Comando.ANNULLA) {
				gruppo.consumaPozioneSaluteGrande(azione);
			}
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			impostaAzioni(gruppo, gruppoAvversario, null);
			return Stato.IN_LOCAZIONE;

		case CHI_BEVE_POZIONE_MAGIA:
			Logger.log("LocazioneBase.CHI_BEVE_POZIONE_MAGIA");
			if (azione != Comando.ANNULLA) {
				gruppo.consumaPozioneMagia(azione);
			}
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			impostaAzioni(gruppo, gruppoAvversario, null);
			return Stato.IN_LOCAZIONE;

		case CHI_BEVE_POZIONE_MAGIA_GRANDE:
			Logger.log("LocazioneBase.CHI_BEVE_POZIONE_MAGIA_GRANDE");
			if (azione != Comando.ANNULLA) {
				gruppo.consumaPozioneMagiaGrande(azione);
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
			gruppo.setFormulante(gruppo.getPersonaggio(azione));
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
				Personaggio formulante = gruppo.getFormulante();
				ClasseIncantesimo classeIncantesimo = ClasseIncantesimo.ofComando(azione);
				incantesimo = classeIncantesimo.getIstanza(formulante.getLivello());
				if (formulante.getMagia() < incantesimo.getCostoLancio()) {
					// Non si dovrebbe più riuscire a entrare in questo ramo perché la scelta degli incantesimi è già stata filtrata
                    String sb = "Il livello di magia " + formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) +
                            " non permette di formulare questo incantesimo.";
					UI.notifica(sb);
					rispostaAvversaria(null, gruppo, gruppoAvversario);
					if (!gruppo.getCapo().isVivo()) {
						return Stato.GIOCO_PERSO;
					}
					statoLocazione = StatoLocazione.IN_LOCAZIONE;
					break;
				}
				PortataIncantesimo tipo = classeIncantesimo.getPortata();
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
						setCompleta(true);
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
					ComandiPossibili.reimposta();
					for (int i = 0; i < l; i++) {
						personaggio = gruppo.getPersonaggio(i);
						Logger.log("tipo == Incantesimo.SINGOLO_QUALSIASI || p.isVivo() ? " + ((tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo())));
						if (tipo == PortataIncantesimo.SINGOLO_QUALSIASI || personaggio.isVivo()) {
							ComandiPossibili.add(Comando.ofPersonaggio(i));
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
				Personaggio formulante = gruppo.getFormulante();
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
			if (gruppo.getMonete() >= gruppo.getNumeroPersonaggi() * 2 && Dado.tira(10) > 3) {
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
						ComandiPossibili.set(Comando.SI, Comando.NO);
						statoLocazione = StatoLocazione.ACCETTA_OFFERTA;
						return Stato.IN_LOCAZIONE;
					}
				} else {
					Logger.log("Mancano i prerequisiti per l'offerta");
				}
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				setCompleta(true);
				return Stato.FINE_LOCAZIONE;
			} else {
				Personaggio p = gruppo.getPersonaggio(azione);
                String sb = "Il tentativo di corruzione " + p.getNome(Personaggio.OpzioniGetNome.INCLUDI_PREPOSIZIONE_ARTICOLATA) +
                        " non ha avuto successo.";
				UI.notifica(sb);
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
			int tiroDelDado = Dado.tira(12);
			Logger.log("Carisma personaggio: " + personaggio.getCarisma() + "; tiro del dado: " + tiroDelDado);
			if (personaggio.getCarisma() > tiroDelDado) {
				personaggio.addCarisma(1);
                String sb = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
                        " riesce a stringere amicizia.";
				haStrettoAmicizia = true;
				UI.notifica(sb);

				offerta = gruppoAvversario.getCapo().getOfferta(Comando.AMICIZIA);
				if (offerta != null && offerta.isFattibile(gruppo, gruppoAvversario)) {
					UI.notifica(offerta.getDescrizione(gruppo, gruppoAvversario));
					if (offerta.isGratuita(gruppo, gruppoAvversario)) {
						offerta.accetta(gruppo, gruppoAvversario);
					} else {
						UI.notifica("Accetta?");
						ComandiPossibili.set(Comando.SI, Comando.NO);
						statoLocazione = StatoLocazione.ACCETTA_OFFERTA;
						return Stato.IN_LOCAZIONE;
					}
				} else {
					Logger.log("Mancano i prerequisiti per l'offerta");
				}
				setCompleta(true);
				return Stato.FINE_LOCAZIONE;
			} else {
				int spregio = Dado.tira(5);
				String descrizione = null;
				switch (spregio) {
					case 1:
						ClasseIncantesimo quale = ClasseIncantesimo.casuale();
						if (gruppo.getIncantesimi(quale) > 0) {
							descrizione = "perde un " + quale.getNomeSingolare() + '.';
							gruppo.subIncantesimi(quale, 1);
						}
						break;
					case 2:
						Personaggio avversario = gruppoAvversario.getCapo();
						int ferite = Dado.tiraAncheAUnaFaccia(avversario.getSalute());
						if (ferite < 20) {
							descrizione = "riceve alcune lievi ferite.";
						} else if (ferite > 40) {
							descrizione = "riceve gravi ferite.";
						} else {
							descrizione = "riceve alcune ferite.";
						}
						personaggio.subSalute(ferite, avversario, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
						break;
					case 3:
						if (gruppo.getMonete() > 0) {
							descrizione = "perde alcune monete.";
							int quanteMonetePerde = Dado.tira(5);
							if (quanteMonetePerde > gruppo.getMonete()) {
								quanteMonetePerde = gruppo.getMonete();
							}
							gruppo.subMonete(quanteMonetePerde);
						}
						break;
					case 4:
						if (gruppo.getPreziosi() > 0) {
							descrizione = "perde alcuni preziosi.";
							int quantiPreziosiPerde = Dado.tira(5);
							if (quantiPreziosiPerde > gruppo.getPreziosi()) {
								quantiPreziosiPerde = gruppo.getPreziosi();
							}
							gruppo.subPreziosi(quantiPreziosiPerde);
						}
						break;
					default:
						break;
				}

				String s = personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
				if (descrizione != null) {
					UI.notifica("Non solo " + s + " non riesce a stringere amicizia, ma in una breve colluttazione " + descrizione);
					UI.rinfresca();
				} else {
					UI.notifica(s + " non riesce a stringere amicizia.");
				}
				opzioneAmiciziaDisponibile = false;
				statoLocazione = StatoLocazione.IN_LOCAZIONE;
				break;
			}

		case ACCETTA_OFFERTA:
			if (azione == Comando.SI) {
				offerta.accetta(gruppo, gruppoAvversario);
			}
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;

		case CONFERMA_FUGA:
			if (azione == Comando.SI) {
				setCompleta(false);
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
		if (isCompleta()) {
			return Stato.FINE_LOCAZIONE;
		}

		impostaComandiPossibili();

		return statoLocazione == StatoLocazione.IN_COMBATTIMENTO ? Stato.IN_COMBATTIMENTO : Stato.IN_LOCAZIONE;
	}

	private void impostaComandiPossibili() {
		ComandiPossibili.reimposta();
		// Possiamo combattere? Oppure, vogliamo cambiare chi combatte?
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO || gruppo.getNumeroPersonaggiVivi() > 1) {
			ComandiPossibili.add(Comando.COMBATTIMENTO);
		}
		// Se stiamo combattendo possiamo interrompere la schermaglia
		if (statoLocazione == StatoLocazione.IN_COMBATTIMENTO) {
			ComandiPossibili.add(Comando.INTERRUZIONE_COMBATTIMENTO);
		}
		// Possiamo formulare incantesimi? Si se ne abbiamo almeno uno e se uno dei personaggi vivi può lanciarlo
		for (ClasseIncantesimo classeIncantesimo : ClasseIncantesimo.values()) {
			if (gruppo.getIncantesimi(classeIncantesimo) > 0 &&
					gruppo.getPersonaggiVivi().stream().anyMatch(p -> p.getMagia() >= classeIncantesimo.getCostoLancio())) {
				ComandiPossibili.add(Comando.INCANTESIMO);
				break;
			}
		}
		// Possiamo corrompere gli avversari?
		if (opzioneCorruzioneDisponibile) {
			ComandiPossibili.add(Comando.CORRUZIONE);
		}
		// Possiamo fare amicizia?
		if (opzioneAmiciziaDisponibile) {
			ComandiPossibili.add(Comando.AMICIZIA);
		}
		if (statoLocazione != StatoLocazione.IN_COMBATTIMENTO) {
			ComandiPossibili.add(Comando.MAPPA);
		}
		if (gruppo.getPozioniSalute() > 0) {
			ComandiPossibili.add(Comando.POZIONE_SALUTE);
		}
		if (gruppo.getPozioniSaluteGrande() > 0) {
			ComandiPossibili.add(Comando.POZIONE_SALUTE_GRANDE);
		}
		if (gruppo.getPozioniMagia() > 0) {
			ComandiPossibili.add(Comando.POZIONE_MAGIA);
		}
		if (gruppo.getPozioniMagiaGrande() > 0) {
			ComandiPossibili.add(Comando.POZIONE_MAGIA_GRANDE);
		}
		// Si puo' sempre ricorrere a una bella...
		ComandiPossibili.add(Comando.FUGA);
		// E possiamo sempre richiedere di descrivere di nuovo la locazione
		ComandiPossibili.add(Comando.AIUTO);
	}

	/**
	 * Il giocatore ha portato in fondo la locazione o è fuggito?
	 */
	public boolean isCompleta() {
		return md.ottieniProprieta(LocazioneMD.COMPLETA) != null;
	}

	/**
	 * Alla fine di un turno un gruppo rade al suolo una locazione e si può verificare
	 * qualcosa; per adesso al momento in cui il giocatore rade al suolo quattro castelli
	 * appare quello del drago
	 */
	public void azzeraLocazione(GruppoGiocatore g) {
		if (isCompleta()) {
			g.setLocazioneCorrenteVisitata();
		}
		for (Personaggio p : g.getPersonaggi()) {
			if (p.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
				p.rimuoviTuttiEffettiDiStato(TipoEffettoDiStato.BERSERK);
			}
		}
	}

	/**
	 * La descrizione dei presenti
	 */
	protected String descrizioneMostriEOggetti(GruppoGiocatore gruppoGiocatore, GruppoAvversario gruppoAvversario) {
		int numeroAvversari = gruppoAvversario.getNumeroPersonaggi();
		Personaggio p = gruppoAvversario.getCapo();
		Oggetto o = getOggetto();
		int numeroOggetti = (o == null ? 0 : o.getQuantita());
		StringBuilder sb = new StringBuilder(gruppoGiocatore.getCapo().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
		if (numeroAvversari == 0 && numeroOggetti == 0) {
			sb.append(" non trova nulla");
		} else {
			sb.append(" vede ");
			if (numeroAvversari > 0) {
				if (numeroAvversari == 1) {
					sb.append(p.getAIS()).append(p.getNomeSingolare());
					if (numeroOggetti > 0) {
						switch (Dado.tira(3)) {
							case 1:
								sb.append(" che protegge ");
								break;
							case 2:
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
						switch (Dado.tira(3)) {
							case 1:
								sb.append(" che proteggono ");
								break;
							case 2:
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
		}
		sb.append('.');
		return sb.toString();
	}

	private void rispostaAvversaria(Personaggio personaggioBersaglio, GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio avversarioAttaccante = null;
		List<Personaggio> avversariVivi = gruppoAvversario.getPersonaggiVivi();
		// Un avversario che può formulare incantesimi ha la precedenza
		for (Personaggio avversarioCorrente : avversariVivi) {
			if (avversarioCorrente.isMagico() && avversarioCorrente.getMagia() > 0) {
				avversarioAttaccante = avversarioCorrente;
				break;
			}
		}
		// Altrimenti, l'avversario con più danni in combattimento
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
	
	private Stato gestisciNuovaLocazione() {
		Logger.log("LocazioneBase.NUOVA_LOCAZIONE");
		TipoLocazione tipoLocazione = gruppo.getClasseLocazioneCorrente().getTipoLocazione();
		int numeroAvversari = gruppoAvversario.getNumeroPersonaggi();
		if (numeroAvversari == 0) {
			if (oggettoCorrente != null) {
				UI.notifica("Essendo il tesoro incustodito, " + gruppo.chi() + " se ne impossessa.");
			}
			if (tipoLocazione != TipoLocazione.MISSIONE_SECONDARIA) {
				gruppo.riposa(getTipoRiposo());
			}
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		} else {
			setCompleta(false);
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
		if (azione == Comando.INTERRUZIONE_COMBATTIMENTO) {
			UI.infoCombattimento(false, null, null);
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			combattente = null;
			return Stato.IN_LOCAZIONE;
		}
		if (azione == Comando.PERSONAGGIO_1 || azione == Comando.PERSONAGGIO_2 || azione == Comando.PERSONAGGIO_3 ||
			azione == Comando.PERSONAGGIO_4 || azione == Comando.PERSONAGGIO_5) {
			combattente = gruppo.getPersonaggio(azione);
			UI.infoCombattimento(true, combattente, gruppoAvversario.getPersonaggioVivo());
			impostaComandiPossibili();
			return Stato.IN_COMBATTIMENTO;
		}
		if (azione == Comando.COMBATTIMENTO && combattente != null) {
			// Il combattente corrente non va azzerato: se la scelta viene annullata
			// deve poter continuare a combattere lui.
			return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
		}
		if (azione == Comando.ANNULLA) {
			// Scelta del nuovo combattente annullata: si continua con quello corrente
			impostaComandiPossibili();
			return Stato.IN_COMBATTIMENTO;
		}
		if (azione == Comando.COMBATTIMENTO || azione == Comando.TIMER) {
			Personaggio bersaglio = gruppoAvversario.getPersonaggioVivo();
			if (bersaglio == null) {
				setCompleta(true);
				return Stato.FINE_LOCAZIONE;
			}
			int danniBersaglio = bersaglio.getDanniInCombattimento();
			int danniCombattente = combattente.getDanniInCombattimento();
			Logger.log("Valutazione danno originale: danniBersaglio (" + bersaglio.getNome() + ") = " + danniBersaglio + ", danniCombattente (" + combattente.getNome() + ") = " + danniCombattente);


			// Test per nuovo motore combattimento
			Logger.log("---------- NUOVO MOTORE ----------");
			Logger.log(combattente.getNome() + " attacca " + bersaglio.getNome());

			Logger.log("Valutazione combattente -> bersaglio");
			Arma arma = combattente.getArmaEquipaggiata();
			boolean colpisce = CalcolatoreCombattimento.colpisce(combattente, bersaglio, arma.getTipoDanno().getSuperTipo());
			if (colpisce) {
				RisultatoCombattimento risultato = CalcolatoreCombattimento.calcolaDannoFinale(combattente, bersaglio, arma);
				bersaglio.subSalute(risultato.getDanno(), combattente, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
				if (!bersaglio.isVivo()) {
					if (GruppoGiocatore.getIstanza().contiene(combattente)) {
						Statistiche.addMostroUcciso(bersaglio.getClasse());
						Statistiche.addPunti(bersaglio.getSaluteMassima());
						GruppoGiocatore.getIstanza().addPuntiEsperienza(bersaglio.getPuntiEsperienza());
					}
					Personaggio nuovoBersaglio = gruppoAvversario.getPersonaggioVivo();
					if (nuovoBersaglio != null) {
						bersaglio = nuovoBersaglio;
					} else {
						setCompleta(true);
						return Stato.FINE_LOCAZIONE;
					}
				} else {
					for (EffettoDiStato effetto : risultato.getEffettiDiStatoDaAggiungere()) {
						bersaglio.addEffettoDiStato(effetto.getTipoEffettoDiStato(), effetto.getValore());
					}
					for (TipoEffettoDiStato tipoEffettoDiStato: risultato.getEffettiDiStatoDaRimuovere()) {
						bersaglio.rimuoviTuttiEffettiDiStato(tipoEffettoDiStato);
					}
				}
			}
			Logger.log("Valutazione bersaglio -> combattente");

			arma = bersaglio.getArmaEquipaggiata();
			colpisce = CalcolatoreCombattimento.colpisce(bersaglio, combattente, arma.getTipoDanno().getSuperTipo());
			if (colpisce) {
				RisultatoCombattimento risultato = CalcolatoreCombattimento.calcolaDannoFinale(bersaglio, combattente, arma);
				combattente.subSalute(risultato.getDanno(), bersaglio, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
				if (!combattente.isVivo()) {
					if (gruppo.getCapo().isVivo()) {
						statoLocazione = StatoLocazione.IN_LOCAZIONE;
						return Stato.IN_LOCAZIONE;
					} else {
						return Stato.GIOCO_PERSO;
					}
				} else {
					for (EffettoDiStato effetto : risultato.getEffettiDiStatoDaAggiungere()) {
						combattente.addEffettoDiStato(effetto.getTipoEffettoDiStato(), effetto.getValore());
					}
					for (TipoEffettoDiStato tipoEffettoDiStato: risultato.getEffettiDiStatoDaRimuovere()) {
						combattente.rimuoviTuttiEffettiDiStato(tipoEffettoDiStato);
					}
				}
			}

			UI.infoCombattimento(true, combattente, bersaglio);

			if (gruppoAvversario.getNumeroPersonaggiVivi() > gruppo.getNumeroPersonaggiVivi()) {
				bersaglio = gruppoAvversario.getPersonaggioVivo();
                String sb = bersaglio.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) +
                        " si disimpegna e attacca!";
				UI.notifica(sb);
				bersaglio.attacca(gruppo);
			}
				if (!gruppo.getCapo().isVivo()) {
					return Stato.GIOCO_PERSO;
			}
		}
		if (azione == Comando.MAPPA) {
			return Stato.MAPPA;
		}
		if (azione == Comando.POZIONE_SALUTE) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneSalute(gruppo.getCapo());
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.POZIONE_SALUTE_GRANDE) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneSaluteGrande(gruppo.getCapo());
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.POZIONE_MAGIA) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneMagia(gruppo.getCapo());
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.POZIONE_MAGIA_GRANDE) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
			} else {
				gruppo.consumaPozioneMagiaGrande(gruppo.getCapo());
				return Stato.IN_COMBATTIMENTO;
			}
		}
		if (azione == Comando.INCANTESIMO) {
			UI.infoCombattimento(false, null, null);
			statoLocazione = StatoLocazione.CHI_FORMULA;
			return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
		}
		if (azione == Comando.FUGA) {
			UI.infoCombattimento(false, null, null);
			chiediConfermaPerLaFuga();
			statoLocazione = StatoLocazione.CONFERMA_FUGA;
			ComandiPossibili.set(Comando.SI, Comando.NO);
			return Stato.ATTESA_SI_NO;
		}
		return Stato.IN_COMBATTIMENTO;
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
				
			case POZIONE_SALUTE:
				Logger.log("Azione.POZIONE_SALUTE");
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;
				
			case POZIONE_SALUTE_GRANDE:
				Logger.log("Azione.POZIONE_SALUTE_GRANDE");
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_SALUTE_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

			case POZIONE_MAGIA:
				Logger.log("Azione.POZIONE_MAGIA");
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

			case POZIONE_MAGIA_GRANDE:
				Logger.log("Azione.POZIONE_MAGIA_GRANDE");
				statoLocazione = StatoLocazione.CHI_BEVE_POZIONE_MAGIA_GRANDE;
				return Stato.SCELTA_AUTOMATICA_PERSONAGGIO;

			case FUGA:
			Logger.log("Azione.FUGA");
				chiediConfermaPerLaFuga();
				statoLocazione = StatoLocazione.CONFERMA_FUGA;
			ComandiPossibili.set(Comando.SI, Comando.NO);
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

	private void chiediConfermaPerLaFuga() {
		if (gruppo.getNumeroPersonaggiVivi() > 1) {
			UI.notifica("Il gruppo è sicuro di voler fuggire?");
		} else {
			Personaggio capo = gruppo.getCapo();
			String sb = capo.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + " è sicur" +
					capo.getLetteraFinaleAttributo() +
					" di voler fuggire?";
			UI.notifica(sb);
		}
	}

	private void gestisciChiCombatte(Comando azione) {
		Logger.log("LocazioneBase.CHI_COMBATTE");
		if (azione == Comando.ANNULLA) {
			statoLocazione = StatoLocazione.IN_LOCAZIONE;
			return;
		}
		combattente = gruppo.getPersonaggio(azione);
		String nome = combattente.getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA, Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		UI.notifica(nome + " si appresta al combattimento.");
		UI.infoCombattimento(true, combattente, gruppoAvversario.getPersonaggioVivo());
		opzioneAmiciziaDisponibile = false;
		opzioneCorruzioneDisponibile = false;
		statoLocazione = StatoLocazione.IN_COMBATTIMENTO;
	}
}
