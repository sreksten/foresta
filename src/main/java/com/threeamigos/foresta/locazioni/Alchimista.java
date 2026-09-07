package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.InterfacciaUtente;
import com.threeamigos.foresta.ui.UI;

import java.util.Arrays;
import java.util.List;

public class Alchimista extends LocazioneBase implements Locazione {

	private enum StatoDaAlchimista {
		SULLA_PORTA,
		ENTRATO,
		INCANTESIMI
	}

	private static final String ARRIVEDERCI = "'Arrivederci, e buona fortuna!'";
	private static final String DICE = ", dice l'alchimista.";
	private static final String CHIEDE = ", chiede l'alchimista.";
	private final GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();

	private StatoDaAlchimista stato;
	private boolean benvenutoDato;
	private boolean ripristinareMagia;
	private boolean ripristinareMagiaGruppo;
	private int costoTotaleAumentoMagiaGruppo;
	boolean incantesimiAcquistabili;
	boolean pozioniAcquistabili;
	boolean nessunAcquistoEseguibile;

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.ALCHIMISTA;
	}

	public Alchimista() {
		reimpostaAcquistiPossibili();
		stato = StatoDaAlchimista.SULLA_PORTA;
	}

	private void reimpostaAcquistiPossibili() {
		int monete = gruppo.getMonete();
		pozioniAcquistabili = monete >= Costanti.COSTO_POZIONE_SALUTE;
		ripristinareMagia = monete >= Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO &&
				gruppo.getPersonaggiVivi().stream().anyMatch(p -> p.getMagia() < p.getMagiaMassima());
		if (gruppo.getPersonaggiVivi().stream().filter(p -> p.getMagia() < p.getMagiaMassima()).count() > 1) {
			// Dopo il primo personaggio sconta del 25%. Molto generoso.
			costoTotaleAumentoMagiaGruppo = Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO +
					(Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO * (gruppo.getNumeroPersonaggiVivi() - 1)) * 75 / 100;
			ripristinareMagiaGruppo = monete >= costoTotaleAumentoMagiaGruppo;
		} else {
			ripristinareMagiaGruppo = false;
		}
		incantesimiAcquistabili = Arrays.stream(ClassiIncantesimo.values()).anyMatch(c -> c.getIstanza().getCostoAcquisto() <= monete);
		nessunAcquistoEseguibile = !pozioniAcquistabili && !ripristinareMagia && !ripristinareMagiaGruppo && !incantesimiAcquistabili;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		// nulla da creare
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica("");
		UI.notifica(g.chiMaiuscolo() + " arriva alla bottega di un alchimista.");
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore ignorato, GruppoAvversario ignorato2, Comando azione) {
		switch (stato) {
		case SULLA_PORTA:
			if (nessunAcquistoEseguibile) {
				UI.notifica("'Buongiorno! Mi dispiace ma non posso fare credito.'" + DICE);
				return Stato.FINE_LOCAZIONE;
			}
			UI.notifica("L'alchimista è intento a produrre l'oroscopo della giornata:");
			List<String> oroscopo = ProduttoreDiTestiCasuale.oroscopo();
			for (String linea : oroscopo) {
				UI.notifica(linea);
			}
			UI.notifica("");
			UI.impostaAzioni(Comando.PERGAMENA);
			stato = StatoDaAlchimista.ENTRATO;
			return Stato.IN_LOCAZIONE;

		case ENTRATO:
			if (!benvenutoDato) {
				daiIlBenvenuto();
				benvenutoDato = true;
			}
			imposta();
			if (azione == Comando.PERSONAGGIO_1 ||
			azione == Comando.PERSONAGGIO_2 ||
			azione == Comando.PERSONAGGIO_3 ||
			azione == Comando.PERSONAGGIO_4 ||
			azione == Comando.PERSONAGGIO_5) {
				if (gruppo.getMonete() >= Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO) {
					Personaggio p = gruppo.getPersonaggio(azione);
					gruppo.subMonete(Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO);
					p.addMagia(Costanti.AUMENTO_MAGIA_PERSONAGGIO);
					reimpostaAcquistiPossibili();
					UI.primoPiano(InterfacciaUtente.Finestra.STATO);
					UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
					UI.rinfresca();
					return Stato.IN_LOCAZIONE;
				} else {
					UI.notifica("'Non hai abbastanza monete per pagare i miei servigi.'" + DICE);
					imposta();
					return Stato.FINE_LOCAZIONE;
				}

			} else if (azione == Comando.GRUPPO) {
				if (gruppo.getMonete() >= costoTotaleAumentoMagiaGruppo) {
					gruppo.subMonete(costoTotaleAumentoMagiaGruppo);
					for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
						personaggio.addMagia(Costanti.AUMENTO_MAGIA_PERSONAGGIO);
					}
					reimpostaAcquistiPossibili();
					UI.primoPiano(InterfacciaUtente.Finestra.STATO);
					UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
					UI.rinfresca();
                } else {
					UI.notifica("'Non avete abbastanza monete per pagare i miei servigi.'" + DICE);
					imposta();
                }
                return Stato.IN_LOCAZIONE;

            } else if (azione == Comando.INCANTESIMO) {
				stato = StatoDaAlchimista.INCANTESIMI;
				UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				impostaIncantesimi();
				return Stato.IN_LOCAZIONE;

			} else if (azione == Comando.POZIONE_SALUTE) {
				gruppo.subMonete(Costanti.COSTO_POZIONE_SALUTE);
				gruppo.addPozioniSalute(1);
				UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				return Stato.IN_LOCAZIONE;

			} else if (azione == Comando.POZIONE_SALUTE_GRANDE) {
				gruppo.subMonete(Costanti.COSTO_POZIONE_SALUTE_GRANDE);
				gruppo.addPozioniSaluteGrande(1);
				UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				return Stato.IN_LOCAZIONE;

			} else if (azione == Comando.POZIONE_MAGIA) {
				gruppo.subMonete(Costanti.COSTO_POZIONE_MAGIA);
				gruppo.addPozioniMagia(1);
				UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				return Stato.IN_LOCAZIONE;

			} else if (azione == Comando.POZIONE_MAGIA_GRANDE) {
				gruppo.subMonete(Costanti.COSTO_POZIONE_MAGIA_GRANDE);
				gruppo.addPozioniMagiaGrande(1);
				UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
				UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
				UI.rinfresca();
				return Stato.IN_LOCAZIONE;

			} else if (azione == Comando.NO_INCANTESIMO) {
				UI.notifica(ARRIVEDERCI + DICE);
				return Stato.FINE_LOCAZIONE;

			} else {
				imposta();
				return Stato.IN_LOCAZIONE;
			}

		case INCANTESIMI:
			if (azione == Comando.NO_INCANTESIMO) {
				UI.notifica(ARRIVEDERCI + DICE);
				return Stato.FINE_LOCAZIONE;
			} else {
				if (azione != null) {
					Incantesimo i = ClassiIncantesimo.ofComando(azione);
					int costo = i.getCostoAcquisto();
					if (gruppo.getMonete() < costo)
						UI.notifica("'Questo incantesimo costa troppo per le tue tasche.'" + DICE);
					else {
						gruppo.subMonete(costo);
						gruppo.addIncantesimi(i.getClasse(), 1);
						UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
						UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
						UI.rinfresca();
					}
				}
				impostaIncantesimi();
				return Stato.IN_LOCAZIONE;
			}
		}
		return Stato.FINE_LOCAZIONE;
	}

	private void daiIlBenvenuto() {
		StringBuilder sb = new StringBuilder();
		int numeroPersonaggiVivi = gruppo.getNumeroPersonaggiVivi();
		sb.append("'Benvenut");
		if (numeroPersonaggiVivi == 1) {
			sb.append(gruppo.getCapo().getLetteraFinaleAttributo());
		} else {
			if (gruppo.getPersonaggiVivi().stream().allMatch(p -> p.getSesso() == Personaggio.Sesso.FEMMINA)) {
				sb.append("e");
			} else {
				sb.append("i");
			}
		}
		sb.append("! Posso vendere pozioni");
		if (incantesimiAcquistabili) {
			sb.append(" o incantesimi");
		}
		if (ripristinareMagia) {
			sb.append(" o ripristinare il tuo potere magico per ")
					.append(Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO)
					.append(" monete");
			if (ripristinareMagiaGruppo) {
				sb.append(", o ripristinare quello di tutto il gruppo per ")
						.append(costoTotaleAumentoMagiaGruppo);
			}
		}
		sb.append(". Come posso aiutare?");
		sb.append("'").append(CHIEDE);
		UI.notifica(sb.toString());
	}

	private void imposta() {
		ComandiPossibili.reimposta();
		if (ripristinareMagia) {
			int l = gruppo.getNumeroPersonaggi();
			Personaggio personaggio;
			for (int i = 0; i < l; i++) {
				personaggio = gruppo.getPersonaggio(i);
				if (personaggio.isVivo() && personaggio.getMagia() < personaggio.getMagiaMassima()) {
					ComandiPossibili.add(Comando.ofPersonaggio(i));
				}
			}
		}
		if (ripristinareMagiaGruppo) {
			ComandiPossibili.add(Comando.GRUPPO);
		}
		if (incantesimiAcquistabili) {
			ComandiPossibili.add(Comando.INCANTESIMO);
		}
		if (pozioniAcquistabili) {
			if (gruppo.getMonete() >= Costanti.COSTO_POZIONE_SALUTE) {
				ComandiPossibili.add(Comando.POZIONE_SALUTE);
			}
			if (gruppo.getMonete() >= Costanti.COSTO_POZIONE_SALUTE_GRANDE) {
				ComandiPossibili.add(Comando.POZIONE_SALUTE_GRANDE);
			}
			if (gruppo.getMonete() >= Costanti.COSTO_POZIONE_MAGIA) {
				ComandiPossibili.add(Comando.POZIONE_MAGIA);
			}
			if (gruppo.getMonete() >= Costanti.COSTO_POZIONE_MAGIA_GRANDE) {
				ComandiPossibili.add(Comando.POZIONE_MAGIA_GRANDE);
			}
		}
		ComandiPossibili.add(Comando.NO_INCANTESIMO);
	}

	private void impostaIncantesimi() {
		ComandiPossibili.reimposta();
		for (ClassiIncantesimo classiIncantesimo : ClassiIncantesimo.values()) {
			if (classiIncantesimo.getIstanza().getCostoAcquisto() <= gruppo.getMonete()) {
				ComandiPossibili.add(classiIncantesimo.getComandoDiAttivazione());
			}
		}
		ComandiPossibili.add(Comando.NO_INCANTESIMO);
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare nel negozio di un alchimista");
	}
}
