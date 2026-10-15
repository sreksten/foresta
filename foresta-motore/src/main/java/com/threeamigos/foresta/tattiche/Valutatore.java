package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.SelettoreMD;
import com.threeamigos.foresta.modellodati.tattiche.AzioneMD;
import com.threeamigos.foresta.modellodati.tattiche.VoceMD;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.PortataAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.List;
import com.threeamigos.foresta.tattiche.rose.Rose;
import com.threeamigos.foresta.tattiche.valutazione.Predicati;
import com.threeamigos.foresta.tattiche.valutazione.Requisiti;
import com.threeamigos.foresta.tattiche.valutazione.SceltaIncantesimo;
import com.threeamigos.foresta.tattiche.valutazione.Selettori;

/**
 * Scorre la tattica di un personaggio e decide che cosa fa (tattiche_di_combattimento_automatiche.md §1, §11): la prima voce eseguibile vince. Una voce è
 * eseguibile se è attiva, le sue condizioni sono tutte vere e il suo selettore trova un bersaglio su cui almeno un'azione si può fare; si esegue
 * la prima azione applicabile a lui. Se nessuna voce lo è, ripiega sull'attacco fisico al primo avversario in campo.
 * <p>
 * Non cambia niente nel gioco: restituisce una {@link Decisione} che l'automa esegue (e per la quale registra l'uso della voce e l'esito).
 * Tira il dado solo se la tattica lo chiede (PROBABILITA, A_CASO).
 */
public final class Valutatore {

	private Valutatore() {
	}

	/**
	 * @return la decisione, o null se non c'è nessun avversario in campo da attaccare e nessuna voce è eseguibile
	 */
	public static Decisione decidi(TatticaMD tattica, ContestoTattico contesto) {
		// La spiegazione si riempie passo per passo e finisce nella Decisione (e di lì nel log e in InternoDecisioneTattica): ogni ramo che scarta
		// una voce o un'azione ne lascia il motivo, così si può ricostruire perché un personaggio ha fatto proprio quella mossa
		List<String> spiegazione = new ArrayList<>();
		List<VoceMD> voci = tattica.getVoci();
		// Le voci si provano nell'ordine in cui sono scritte: la prima che produce una decisione vince e le successive non si guardano nemmeno
		for (int i = 0; i < voci.size(); i++) {
			VoceMD voce = voci.get(i);
			String numero = "voce " + (i + 1);
			if (!voce.isAttiva()) {
				spiegazione.add(numero + ": disattivata");
				continue;
			}
			// L'identificativo di una voce è la sua posizione: i predicati VOCE_USATA e ROUND_DALL_ULTIMO_USO lo usano per leggere la scheda dello stato
			// del combattimento. Va scritto nel contesto prima delle condizioni, non solo prima dell'esecuzione
			ContestoTattico perLaVoce = contesto.conVoce(String.valueOf(i));
			if (!Predicati.valgonoTutti(voce.getCondizioni(), perLaVoce)) {
				spiegazione.add(numero + ": le condizioni non sono tutte vere " + voce.getCondizioni());
				continue;
			}
			Decisione decisione = valutaLaVoce(voce, i, perLaVoce, spiegazione);
			if (decisione != null) {
				return decisione;
			}
		}
		// Nessuna voce ha prodotto una decisione (o la tattica è vuota)
		return ripiego(contesto, spiegazione);
	}

	/**
	 * Prova a ricavare una decisione da una voce le cui condizioni sono già vere: trova il bersaglio con il selettore e poi sceglie l'azione.
	 * Per le voci su un gruppo intero (portata GRUPPO_ALLEATI o GRUPPO_AVVERSARI) il selettore dice solo se c'è qualcuno e il bersaglio resta null.
	 *
	 * @return la decisione, o null se la voce non è eseguibile (il motivo è già in <code>spiegazione</code>) e si deve passare alla successiva
	 */
	private static Decisione valutaLaVoce(VoceMD voce, int indice, ContestoTattico contesto, List<String> spiegazione) {
		String numero = "voce " + (indice + 1);
		Personaggio attore = contesto.getAttore();
		SelettoreMD selettore = voce.selettoreEffettivo();
		PortataAzione portata = voce.getPortata();
		if (portata == PortataAzione.GRUPPO_ALLEATI || portata == PortataAzione.GRUPPO_AVVERSARI) {
			// Il selettore è solo un test di esistenza; l'azione agisce su tutto il gruppo. Si passa l'azione al contesto (la prima: sono tutte della
			// stessa portata) perché un filtro come DEBOLE_ALL_AZIONE, che ne dipende, abbia qualcosa da leggere
			RisultatoSelezione esistenza = Selettori.seleziona(selettore, contesto.conAzione(voce.getAzioni().get(0).getTipo()));
			// Si guardano i rimasti e non lo scelto: l'ordinamento potrebbe non scegliere nessuno (ULTIMO_ATTACCANTE) pur restando dei candidati
			if (esistenza.getRimasti().isEmpty()) {
				spiegazione.add(numero + ": nessun candidato " + esistenza.getPassi());
				return null;
			}
			// Le azioni si provano in ordine e vince la prima che i requisiti permettono; senza bersaglio, i requisiti guardano il gruppo
			for (AzioneMD azione : voce.getAzioni()) {
				String motivo = Requisiti.motivoDelNo(azione, attore, null, contesto);
				if (motivo == null) {
					spiegazione.add(numero + ": " + azione + " su tutto il gruppo");
					return new Decisione(indice, azione, azione.getTipo(), azione.getTipo().getClasseIncantesimo(), null, spiegazione);
				}
				spiegazione.add(numero + ": " + azione.getTipo() + " scartata: " + motivo);
			}
			return null;
		}
		// Il bersaglio si sceglie fra chi ammette davvero almeno un'azione della voce (Ammissione): altrimenti un ordinamento come MENO_SALUTE potrebbe
		// cadere su un candidato su cui nessuna azione si può fare, e la voce verrebbe scartata anche se ce n'era un altro adatto
		RisultatoSelezione risultato = Selettori.seleziona(selettore, contesto, candidato -> primaAzioneApplicabile(voce, attore, candidato, contesto));
		if (risultato.isVuoto()) {
			spiegazione.add(numero + ": nessun bersaglio " + risultato.getPassi());
			return null;
		}
		Personaggio bersaglio = risultato.getScelto();
		spiegazione.add(numero + ": bersaglio " + bersaglio.getNome() + " " + risultato.getPassi());
		// Le azioni si riprovano sul bersaglio scelto, nello stesso ordine: la prima che passa i requisiti è quella che si esegue (con l'incantesimo
		// di danno sceglierla significa anche scegliere quale incantesimo, vedi decisioneSu)
		for (AzioneMD azione : voce.getAzioni()) {
			String motivo = Requisiti.motivoDelNo(azione, attore, bersaglio, contesto);
			if (motivo != null) {
				spiegazione.add(numero + ": " + azione.getTipo() + " scartata: " + motivo);
				continue;
			}
			Decisione decisione = decisioneSu(indice, azione, attore, bersaglio, contesto, spiegazione);
			if (decisione != null) {
				return decisione;
			}
			// Succede solo con un incantesimo a criterio A_CASO e soloSePiuForteDellAttacco: la scelta ritira il dado, e l'incantesimo che bastava nel
			// controllo dei requisiti può non bastare nella scelta vera. L'azione non è applicabile e si passa alla successiva della voce
			spiegazione.add(numero + ": " + azione.getTipo() + " scartata: nessun incantesimo adatto alla scelta");
		}
		// Dopo l'Ammissione di solito non si arriva qui: solo se tutte le azioni sono state scartate come sopra
		return null;
	}

	/**
	 * L'azione di tipo INCANTESIMO_DI_DANNO diventa l'incantesimo scelto; le altre restano come sono. Per il cambio d'arma non c'è un bersaglio.
	 *
	 * @return la decisione, o null se l'incantesimo a criterio non ne trova nessuno da lanciare (vedi il chiamante)
	 */
	private static Decisione decisioneSu(int indice, AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
										 List<String> spiegazione) {
		TipoAzione tipo = azione.getTipo();
		ClasseIncantesimo classe = tipo.getClasseIncantesimo();
		if (tipo == TipoAzione.INCANTESIMO_DI_DANNO || tipo == TipoAzione.INCANTESIMO_DEL_REPERTORIO) {
			// Qui si sceglie davvero: i requisiti avevano chiamato la stessa scelta solo per sapere se c'era qualcosa da lanciare. Con il criterio A_CASO
			// ogni chiamata ritira il dado, quindi l'incantesimo di allora e questo possono essere diversi (entrambi fra i lanciabili)
			classe = SceltaIncantesimo.scegli(azione, attore, bersaglio, contesto);
			if (classe == null) {
				// TipoAzione.di(null) darebbe ATTACCO_FISICO, la prima azione senza classe: un attacco che la tattica non ha chiesto
				return null;
			}
			tipo = TipoAzione.di(classe);
			spiegazione.add("voce " + (indice + 1) + ": " + azione + " sceglie " + classe + " (" + SceltaIncantesimo.descrivi(azione, attore, bersaglio, contesto) + ")");
		} else {
			spiegazione.add("voce " + (indice + 1) + ": " + azione + " su " + (bersaglio == null ? "nessuno" : bersaglio.getNome()));
		}
		// Il cambio d'arma ha come "bersaglio" l'attore stesso (rosa SE_STESSO), ma la decisione non lo porta: non c'è nessuno da colpire
		Personaggio colpito = tipo == TipoAzione.CAMBIA_ARMA ? null : bersaglio;
		return new Decisione(indice, azione, tipo, classe, colpito, spiegazione);
	}

	/**
	 * La prima azione della voce che si può fare su quel candidato (per il selettore), o null
	 */
	private static TipoAzione primaAzioneApplicabile(VoceMD voce, Personaggio attore, Personaggio candidato, ContestoTattico contesto) {
		for (AzioneMD azione : voce.getAzioni()) {
			if (Requisiti.motivoDelNo(azione, attore, candidato, contesto) == null) {
				return azione.getTipo();
			}
		}
		return null;
	}

	/**
	 * Nessuna voce eseguibile: un attacco fisico al primo avversario in campo (un turno non resta mai senza un'azione)
	 */
	private static Decisione ripiego(ContestoTattico contesto, List<String> spiegazione) {
		List<Personaggio> avversari = Rose.di(TipoRosa.AVVERSARI).candidati(contesto);
		if (avversari.isEmpty()) {
			spiegazione.add("nessuna voce eseguibile e nessun avversario in campo");
			return null;
		}
		spiegazione.add("nessuna voce eseguibile: attacco fisico al primo avversario");
		return new Decisione(-1, AzioneMD.di(TipoAzione.ATTACCO_FISICO), TipoAzione.ATTACCO_FISICO, null, avversari.get(0), spiegazione);
	}
}
