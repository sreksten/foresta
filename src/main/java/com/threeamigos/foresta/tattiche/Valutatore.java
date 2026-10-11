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
		List<String> spiegazione = new ArrayList<>();
		List<VoceMD> voci = tattica.getVoci();
		for (int i = 0; i < voci.size(); i++) {
			VoceMD voce = voci.get(i);
			String numero = "voce " + (i + 1);
			if (!voce.isAttiva()) {
				spiegazione.add(numero + ": disattivata");
				continue;
			}
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
		return ripiego(contesto, spiegazione);
	}

	private static Decisione valutaLaVoce(VoceMD voce, int indice, ContestoTattico contesto, List<String> spiegazione) {
		String numero = "voce " + (indice + 1);
		Personaggio attore = contesto.getAttore();
		SelettoreMD selettore = voce.selettoreEffettivo();
		PortataAzione portata = voce.getPortata();
		if (portata == PortataAzione.GRUPPO_ALLEATI || portata == PortataAzione.GRUPPO_AVVERSARI) {
			// Il selettore è solo un test di esistenza; l'azione agisce su tutto il gruppo
			RisultatoSelezione esistenza = Selettori.seleziona(selettore, contesto.conAzione(voce.getAzioni().get(0).getTipo()));
			if (esistenza.isVuoto()) {
				spiegazione.add(numero + ": nessun candidato " + esistenza.getPassi());
				return null;
			}
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
		RisultatoSelezione risultato = Selettori.seleziona(selettore, contesto, candidato -> primaAzioneApplicabile(voce, attore, candidato, contesto));
		if (risultato.isVuoto()) {
			spiegazione.add(numero + ": nessun bersaglio " + risultato.getPassi());
			return null;
		}
		Personaggio bersaglio = risultato.getScelto();
		spiegazione.add(numero + ": bersaglio " + bersaglio.getNome() + " " + risultato.getPassi());
		for (AzioneMD azione : voce.getAzioni()) {
			String motivo = Requisiti.motivoDelNo(azione, attore, bersaglio, contesto);
			if (motivo != null) {
				spiegazione.add(numero + ": " + azione.getTipo() + " scartata: " + motivo);
				continue;
			}
			return decisioneSu(indice, azione, attore, bersaglio, contesto, spiegazione);
		}
		return null;
	}

	/**
	 * L'azione di tipo INCANTESIMO_DI_DANNO diventa l'incantesimo scelto; le altre restano come sono. Per il cambio d'arma non c'è un bersaglio.
	 */
	private static Decisione decisioneSu(int indice, AzioneMD azione, Personaggio attore, Personaggio bersaglio, ContestoTattico contesto,
										 List<String> spiegazione) {
		TipoAzione tipo = azione.getTipo();
		ClasseIncantesimo classe = tipo.getClasseIncantesimo();
		if (tipo == TipoAzione.INCANTESIMO_DI_DANNO) {
			classe = SceltaIncantesimo.scegli(azione, attore, bersaglio, contesto);
			tipo = TipoAzione.di(classe);
			spiegazione.add("voce " + (indice + 1) + ": " + azione + " sceglie " + classe);
		} else {
			spiegazione.add("voce " + (indice + 1) + ": " + azione + " su " + (bersaglio == null ? "nessuno" : bersaglio.getNome()));
		}
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
