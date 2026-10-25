package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoScheletroRisvegliato;
import com.threeamigos.foresta.eventi.notifiche.NotificaInterazionePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaReazioneDiClassePersonaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Scheletro;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quel che succede a chi riceve il risultato di un colpo ({@link DannoRisultante}): gli si toglie la salute, gli si aggiungono gli stati
 * alterati (per lo stesso stato si tiene quello che dura di più) e i modificatori, gli si tolgono gli stati da rimuovere, si pubblicano le
 * notifiche delle interazioni e delle reazioni di classe, la cura ad area va agli alleati e i colpi ad area ai vicini. Sta fuori dal
 * personaggio perché conosce i gruppi e le regole del combattimento.
 */
public final class ApplicazioneDelRisultato {

	private ApplicazioneDelRisultato() {
	}

	/**
	 * I mostri che il gruppo del giocatore ha come servitori e che possono essere mietuti: non i boss, che partono con i valori pieni.
	 */
	private static boolean isServitoreAvversario(Personaggio personaggio) {
		return !personaggio.isParteConValoriMassimi() && GruppoAvversario.getIstanza().contiene(personaggio);
	}

	/**
	 * Applica a {@code bersaglio} il risultato di un colpo.
	 */
	public static void applica(Personaggio bersaglio, DannoRisultante risultato) {
		StatoDelCombattimento.getIstanza().registraColpo(risultato);
		bersaglio.subSalute(risultato.getDanno(), risultato.getAttaccante(), Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);

		// Il risultato del combattimento potrebbe cercare di applicare più di una volta lo stesso effetto di stato;
		// in questo caso, manteniamo solo il maggiore
		Map<TipoEffettoDiStato, EffettoDiStatoMD> mappaFiltrata = risultato.getEffettiDiStatoDaAggiungere().stream()
				.collect(Collectors.toMap(
						EffettoDiStatoMD::getTipoEffettoDiStato, // Chiave della mappa: il tipo di stato (es. CONGELATO)
						effetto -> effetto,       // Valore della mappa: l'oggetto effetto stesso
						// Funzione di risoluzione conflitti: se la chiave esiste già, confronta le durate e tiene il maggiore
						(effettoEsistente, nuovoEffetto) ->
								nuovoEffetto.getDurata() > effettoEsistente.getDurata() ? nuovoEffetto : effettoEsistente
				));

		for (EffettoDiStatoMD effetto : mappaFiltrata.values()) {
			bersaglio.addEffettoDiStato(effetto.getTipoEffettoDiStato(), effetto.getDurata(), effetto.getDanniNelTempo(), effetto.getIntensita());
		}
		for (ModificatoreAttributoMD modificatore : risultato.getModificatoriDaAggiungere()) {
			bersaglio.addModificatore(modificatore);
		}
		for (TipoEffettoDiStato tipoEffettoDiStato: risultato.getEffettiDiStatoDaRimuovere()) {
			bersaglio.rimuoviEffettoDiStato(tipoEffettoDiStato);
		}
		for (TipoInterazioneConEffettiDiStato interazione : risultato.getInterazioni()) {
			BusEventi.pubblica(new NotificaInterazionePersonaggio(bersaglio, interazione));
		}
		for (Map.Entry<TipoDanno, Double> reazione : risultato.getReazioniDiClasse().entrySet()) {
			BusEventi.pubblica(new NotificaReazioneDiClassePersonaggio(bersaglio, reazione.getKey(), reazione.getValue()));
		}

		if (risultato.getCuraAdArea() > 0) {
			Gruppo gruppoDifensore = bersaglio.appartieneAlGruppoDelGiocatore() ?
					GruppoGiocatore.getIstanza() : GruppoAvversario.getIstanza();
			for (Personaggio alleato : gruppoDifensore.getPersonaggiVivi()) {
				if (alleato != bersaglio) {
					alleato.addSalute(risultato.getCuraAdArea());
				}
			}
		}

		// Le reazioni ad area investono anche i vicini nella fila del gruppo (CalcolatoreCombattimento.viciniInvestiti);
		// il colpo che arriva ai vicini non ne porta altre (calcolaDannoAdArea)
		if (!risultato.getDanniAdArea().isEmpty()) {
			Gruppo gruppoDifensore = bersaglio.appartieneAlGruppoDelGiocatore() ?
					GruppoGiocatore.getIstanza() : GruppoAvversario.getIstanza();
			for (DannoRisultante.DannoAdArea area : risultato.getDanniAdArea()) {
				for (Personaggio vicino : CalcolatoreCombattimento.viciniInvestiti(gruppoDifensore.getPersonaggi(), bersaglio, area.getInterazione())) {
					BusEventi.pubblica(new NotificaTestoFrase(area.getInterazione().getTestoAdArea() + " "
							+ vicino.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + '.'));
					applica(vicino, CalcolatoreCombattimento.calcolaDannoAdArea(risultato.getAttaccante(), vicino, area));
				}
			}
		}

		if (risultato.getSifoneVitale() > 0) {
			Personaggio attaccante = risultato.getAttaccante();
			int sifone = risultato.getSifoneVitale();
			int capienzaMagia = attaccante.getMagiaMassima() - attaccante.getMagia();
			int perMagia = Math.min(sifone, capienzaMagia);
			if (perMagia > 0) {
				attaccante.addMagia(perMagia);
			}
			int eccedenza = sifone - perMagia;
			if (eccedenza > 0) {
				attaccante.addSalute(eccedenza);
			}
		}

		if (risultato.isDiluizioneEmaticaAttiva()) {
			bersaglio.getEffettiDiStato().stream()
					.filter(effetto -> effetto.getTipoEffettoDiStato() == TipoEffettoDiStato.SANGUINAMENTO)
					.findFirst()
					.ifPresent(effetto -> effetto.setDanniNelTempo(effetto.getDanniNelTempo() / 2));
		}

		if (risultato.isCollassoEntropicoAttivo()) {
			int mpBruciato = Math.min((int) Math.round(bersaglio.getMagiaMassima() * 0.25d), bersaglio.getMagia());
			if (mpBruciato > 0) {
				bersaglio.subMagia(mpBruciato);
				bersaglio.subSalute(mpBruciato, risultato.getAttaccante(), Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
			}
		}

		if (risultato.isMietituraAttiva() && isServitoreAvversario(bersaglio) && bersaglio.getSalute() * 5 < bersaglio.getSaluteMassima()) {
			// A gruppo pieno il minion muore lo stesso, ma lo scheletro non ha posto
			boolean cePosto = GruppoGiocatore.getIstanza().getNumeroPersonaggi() < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_TOTALE;
			if (bersaglio.isVivo()) {
				bersaglio.muore(cePosto ? "è stato mietuto e si è risvegliato come scheletro" : "è stato mietuto");
			}
			GruppoAvversario.getIstanza().rimuoviPersonaggio(bersaglio);
			if (cePosto) {
				Scheletro scheletro = new Scheletro(bersaglio.getLivello());
				scheletro.setMotivoNelGruppo(TipoMotivoNelGruppo.MIETITURA);
				scheletro.setTempo(CostantiPersonaggi.MIETITURA_DURATA_TURNI + 1);
				GruppoGiocatore.getIstanza().aggiungiPersonaggio(scheletro);
				BusEventi.pubblica(new InternoScheletroRisvegliato());
			}
		}
	}
}
