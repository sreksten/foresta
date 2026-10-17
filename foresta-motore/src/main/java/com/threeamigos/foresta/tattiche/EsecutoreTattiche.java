package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.canti.FabbricaCanti;
import com.threeamigos.foresta.combattimento.*;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.eventi.interni.InternoDecisioneTattica;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.incantesimi.*;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.tattiche.valutazione.SceltaIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoCanto;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.EsitoAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.Collections;
import java.util.List;

/**
 * Esegue le decisioni delle tattiche (tattiche_di_combattimento_automatiche.md §11): il valutatore sceglie (<code>Valutatore.decidi</code>), qui si fa quello che ha
 * scelto, sul modello dati di sempre: un attacco con le armi, un incantesimo, un canto, una pozione, il cambio d'arma. Lo usano il combattimento di una
 * locazione (<code>LocazioneBase</code>) e le simulazioni, così i mostri e i miei personaggi fanno le stesse cose in tutti e due.
 * <p>
 * Non gestisce il round (chi agisce e quando lo decide chi lo chiama) né la fine dello scontro: dopo ogni azione chi chiama guarda se gli avversari
 * sono tutti fuori combattimento o se è caduto il capo. Gli avversari messi fuori combattimento dai miei personaggi li comunica a un {@link RegistroUccisioni} (nel gioco danno esperienza e punti); quelli dei mostri no.
 */
public final class EsecutoreTattiche {

	/**
	 * Chi viene a sapere che un personaggio ha messo fuori combattimento un altro (per le statistiche e l'esperienza)
	 */
	public interface RegistroUccisioni {
		void registra(Personaggio uccisore, Personaggio vittima);
	}

	private final Gruppo miei;
	private final Gruppo avversari;
	private final StatoDelCombattimento stato;
	private final RegistroUccisioni uccisioni;

	/**
	 * @param miei      il gruppo del giocatore (i suoi personaggi sono "i miei"; per il cambio d'arma serve un GruppoGiocatore)
	 * @param avversari il gruppo avversario
	 * @param uccisioni chi registra gli uccisi
	 */
	public EsecutoreTattiche(Gruppo miei, Gruppo avversari, StatoDelCombattimento stato, RegistroUccisioni uccisioni) {
		this.miei = miei;
		this.avversari = avversari;
		this.stato = stato;
		this.uccisioni = uccisioni;
	}

	/**
	 * Il personaggio agisce con la tattica: decide, annuncia la decisione (<code>InternoDecisioneTattica</code>), la esegue e ricorda l'uso della voce.
	 *
	 * @return la decisione, o null se non c'era niente da fare (nessun bersaglio)
	 */
	public Decisione agisci(Personaggio attore, TatticaMD tattica) {
		// Tre passi: decidere (il Valutatore non cambia niente), annunciare la decisione e poi eseguirla. Il contesto è nuovo a ogni azione e si butta
		// dopo: è una fotografia dei gruppi di quel momento (chi è caduto, chi è silenziato...) e non va tenuto da un'azione all'altra
		Decisione decisione = Valutatore.decidi(tattica, ContestoTattico.per(attore, miei, avversari));
		if (decisione == null) {
			return null;
		}
		// Pubblicato prima dell'esecuzione: nel log la spiegazione precede le righe dell'azione, e la UI scrive l'azione sotto il personaggio prima che
		// questa accada (ForestaUI.gestisciEventoDecisioneTattica)
		BusEventi.pubblica(new InternoDecisioneTattica(attore, decisione.getBersaglio(), decisione.getTipoEseguito(), decisione.getIndiceVoce(),
				decisione.getSpiegazione()));
		esegui(attore, decisione);
		// Il ripiego non è una voce della tattica: non ha un identificativo e non conta per VOCE_USATA e ROUND_DALL_ULTIMO_USO. L'uso si registra dopo
		// l'esecuzione, così durante l'azione la voce risulta ancora non usata in questo round
		if (!decisione.isRipiego()) {
			stato.registraUsoVoce(attore, decisione.getIdentificativoVoce());
		}
		return decisione;
	}

	/**
	 * Fa quello che la decisione dice
	 */
	public void esegui(Personaggio attore, Decisione decisione) {
		// Lo stesso esecutore serve i due lati: "miei" è sempre il gruppo del giocatore, e da lì si ricava di chi è l'attore e chi è il suo nemico
		boolean deiMiei = miei.contiene(attore);
		Gruppo gruppoDellAttore = deiMiei ? miei : avversari;
		Gruppo gruppoNemico = deiMiei ? avversari : miei;
		Personaggio bersaglio = decisione.getBersaglio();
		switch (decisione.getTipoEseguito()) {
			case ATTACCO_FISICO:
				// I miei passano dal metodo di questa classe, che registra gli uccisi (esperienza e punti); i mostri attaccano con il proprio metodo, in
				// PersonaggioBase, che ferma il colpo successivo se il bersaglio è già fuori combattimento ma non registra nessuno
				if (deiMiei) {
					attaccaConLeArmi(attore, bersaglio);
				} else {
					attore.attaccaConLeArmi(bersaglio);
				}
				break;
			case DARDO_ARCANO:
				scagliaIlDardo(attore, bersaglio);
				break;
			// La Resurrezione consuma una pergamena se il gruppo ne ha una (consumaLaPergamena non fa niente se non ce ne sono)
			case RESURREZIONE:
				annuncia(attore, bersaglio);
				FabbricaIncantesimi.crea(TipoIncantesimo.RESURREZIONE, attore.getLivello()).formula(attore, bersaglio, null);
				consumaLaPergamena(gruppoDellAttore, TipoIncantesimo.RESURREZIONE);
				break;
			case ALBA_SACRA:
				annuncia(attore, null);
				FabbricaIncantesimi.crea(TipoIncantesimo.ALBA_SACRA, attore.getLivello()).formula(attore, null, gruppoDellAttore);
				// Chi la conosce per natura (Sacerdote, Sacerdotessa) non consuma pergamene
				if (!AlbaSacra.conosciutaDa(attore.getClasse())) {
					consumaLaPergamena(gruppoDellAttore, TipoIncantesimo.ALBA_SACRA);
				}
				break;
			// I canti sono tutti la stessa azione con un tipo diverso: il canto curativo ha un bersaglio, gli altri agiscono sul gruppo
			case CANTO_CURATIVO:
				canta(attore, TipoCanto.CURATIVO, bersaglio, gruppoDellAttore, gruppoNemico);
				break;
			case CANTO_IPNOTICO:
				canta(attore, TipoCanto.IPNOTICO, null, gruppoDellAttore, gruppoNemico);
				break;
			case URLO_DELLA_BANSHEE:
				canta(attore, TipoCanto.URLO_DELLA_BANSHEE, null, gruppoDellAttore, gruppoNemico);
				break;
			case CANTO_DI_VALORE:
				canta(attore, TipoCanto.DI_VALORE, null, gruppoDellAttore, gruppoNemico);
				break;
			// Le pozioni si prendono dalle scorte del gruppo dell'attore e si ricorda l'alleato scelto, per ULTIMO_SCELTO e SCELTO_L_ULTIMA_VOLTA. Il
			// bersaglio è annunciato come null: l'attore beve o dà da bere, non attacca nessuno
			case POZIONE_SALUTE:
				annuncia(attore, null);
				gruppoDellAttore.consumaPozioneSalute(bersaglio);
				stato.registraAlleato(attore, bersaglio);
				break;
			case POZIONE_SALUTE_GRANDE:
				annuncia(attore, null);
				gruppoDellAttore.consumaPozioneSaluteGrande(bersaglio);
				stato.registraAlleato(attore, bersaglio);
				break;
			case POZIONE_MAGIA:
				annuncia(attore, null);
				gruppoDellAttore.consumaPozioneMagia(bersaglio);
				stato.registraAlleato(attore, bersaglio);
				break;
			case POZIONE_MAGIA_GRANDE:
				annuncia(attore, null);
				gruppoDellAttore.consumaPozioneMagiaGrande(bersaglio);
				stato.registraAlleato(attore, bersaglio);
				break;
			case CAMBIA_ARMA:
				cambiaArma(attore, decisione);
				break;
			// Tutto il resto sono incantesimi di una classe precisa: ARIA, ACQUA, ..., MORTE, SILENZIO, LUCE_DIVINA. INCANTESIMO_DI_DANNO e
			// INCANTESIMO_DEL_REPERTORIO non arrivano qui: il Valutatore li ha già trasformati in quello scelto (Decisione.getTipoEseguito)
			default:
				lanciaUnIncantesimoDiDanno(attore, decisione, gruppoDellAttore);
				break;
		}
	}

	/**
	 * L'attacco con le armi di un mio personaggio contro il bersaglio scelto (la seconda arma sullo stesso bersaglio, o sul prossimo avversario vivo se la
	 * prima l'ha ucciso); chi ucciderebbe l'ultimo avversario non ha altri colpi.
	 */
	public void attaccaConLeArmi(Personaggio attore, Personaggio bersaglio) {
		// Prima si apre il riquadro del combattimento sul bersaglio (ForestaUI), poi si annuncia chi agisce e si scrive la riga di testo dell'attacco
		BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(attore, bersaglio));
		annuncia(attore, bersaglio);

		if (Logger.isAttivo()) {
			Logger.log(attore.getNome() + " attacca " + bersaglio.getNome());
		}

		// Una fase per ogni arma impugnata (principale e secondaria); il fattore ne riduce il danno. Il tiro per colpire si fa fase per fase
		for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(attore, bersaglio)) {
			Arma arma = fase.getArma();
			boolean colpisce = CalcolatoreCombattimento.colpisce(attore, bersaglio, arma.getTipoDanno().getSuperTipo());
			if (colpisce) {
				DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attore, bersaglio, arma, fase.getFattore());
				bersaglio.applicaRisultatoCombattimento(risultato);
				if (bersaglio.isFuoriCombattimento()) {
					uccisioni.registra(attore, bersaglio);
					// Il colpo successivo passa al primo avversario ancora in piedi (non al "prossimo" della rosa): coerente con il ripiego del Valutatore
					List<Personaggio> vivi = avversari.getPersonaggiVivi();
					if (vivi.isEmpty()) {
						return;
					}
					bersaglio = vivi.get(0);
				}
			}
		}
	}

	/**
	 * Il dardo arcano contro un bersaglio (costa MAGIA, non consuma pergamene): l'esito si ricorda per le tattiche
	 */
	public void scagliaIlDardo(Personaggio formulante, Personaggio bersaglio) {
		// Un incantesimo "fatto in casa": non passa dalla fabbrica degli incantesimi, quindi l'esito (colpito, mancato) lo registra qui e non in
		// IncantesimoMaleficoImpl come per gli altri. Il costo in MAGIA si paga comunque, anche se il dardo manca
		DardoArcano dardo = new DardoArcano(formulante);
		annuncia(formulante, bersaglio);
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " scaglia un dardo arcano contro "
				+ bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + "."));
		if (CalcolatoreCombattimento.colpisce(formulante, bersaglio, dardo.getTipoDanno().getSuperTipo())) {
			stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.DARDO_ARCANO, EsitoAzione.RIUSCITA);
			bersaglio.applicaRisultatoCombattimento(CalcolatoreCombattimento.calcolaDannoRisultante(formulante, bersaglio, dardo));
			uccisioni.registra(formulante, bersaglio);
		} else {
			stato.registraEsitoAzione(formulante, bersaglio, TipoAzione.DARDO_ARCANO, EsitoAzione.MANCATA);
		}
		formulante.subMagia(dardo.getCostoLancio());
		stato.registraIncantesimoLanciato(formulante);
	}

	/**
	 * Un incantesimo di danno, la Morte o il Silenzio contro il bersaglio scelto; con più bersagli (MULTIPLO) il principale e i successivi vivi (per un mostro solo
	 * il principale). Il lancio, costo in MAGIA compreso, lo fa l'incantesimo; la pergamena si consuma se il gruppo ne ha una (altrimenti è il repertorio di un mostro).
	 */
	private void lanciaUnIncantesimoDiDanno(Personaggio attore, Decisione decisione, Gruppo gruppoDellAttore) {
		TipoIncantesimo classe = decisione.getIncantesimo();
		Personaggio bersaglio = decisione.getBersaglio();
		Incantesimo incantesimo = FabbricaIncantesimi.crea(classe, attore.getLivello());
		annuncia(attore, bersaglio);
		List<Personaggio> colpiti = Collections.singletonList(bersaglio);
		if (classe.getPortata() == PortataIncantesimo.MULTIPLO) {
			colpiti = SceltaIncantesimo.bersagli(attore, bersaglio, classe, ContestoTattico.per(attore, miei, avversari));
			incantesimo.formula(attore, null, new GruppoDiBersagli(colpiti));
		} else {
			incantesimo.formula(attore, bersaglio, null);
		}
		for (Personaggio colpito : colpiti) {
			uccisioni.registra(attore, colpito);
		}
		// Chi la conosce per natura (Sacerdote, Sacerdotessa) non consuma pergamene
		if (!(classe == TipoIncantesimo.LUCE_DIVINA && LuceDivina.conosciutaDa(attore.getClasse()))) {
			consumaLaPergamena(gruppoDellAttore, classe);
		}
	}

	/**
	 * Toglie una pergamena di quella classe dalle scorte del gruppo, se ce n'è; senza, niente (un mostro non ne ha, un personaggio con il potere innato
	 * non ne usa: chi chiama decide se chiamarlo)
	 */
	private static void consumaLaPergamena(Gruppo gruppoDellAttore, TipoIncantesimo classe) {
		if (gruppoDellAttore.getIncantesimi(classe) > 0) {
			gruppoDellAttore.subIncantesimi(classe, 1);
		}
	}

	/**
	 * <code>cantaSu</code> dice se il canto ha avuto luogo (e ha consumato MAGIA) o se non c'era nessuno su cui farlo: solo nel primo caso conta come
	 * incantesimo lanciato (per il filtro MAGICO) e si annuncia. Nel secondo non si annuncia niente e per la tattica il turno è comunque speso.
	 */
	private void canta(Personaggio cantore, TipoCanto tipo, Personaggio bersaglio, Gruppo gruppoDelCantore, Gruppo gruppoNemico) {
		if (FabbricaCanti.crea(tipo, cantore).cantaSu(cantore, bersaglio, gruppoDelCantore, gruppoNemico)) {
			stato.registraIncantesimoLanciato(cantore);
			annuncia(cantore, null);
		}
	}

	/**
	 * Il cambio d'arma è l'azione del turno (tattiche_di_combattimento_automatiche.md §5): prende la prima arma del gruppo che dia il tipo di danno voluto. Solo
	 * i personaggi del giocatore, che hanno un inventario.
	 */
	private void cambiaArma(Personaggio attore, Decisione decisione) {
		if (!(miei instanceof GruppoGiocatore)) {
			return;
		}
		GruppoGiocatore gruppo = (GruppoGiocatore) miei;
		List<Artefatto> armi = gruppo.armiPerCambio(attore, decisione.getAzione().getArmaDi());
		if (!armi.isEmpty()) {
			annuncia(attore, null);
			gruppo.cambiaArma(attore, armi.get(0));
		}
	}

	/**
	 * Dice alla UI che l'attore sta agendo (bersaglio null se non ne ha uno): il riquadro del combattimento mostra l'uno e l'altro
	 */
	private static void annuncia(Personaggio attore, Personaggio bersaglio) {
		BusEventi.pubblica(new InternoAzioneInCombattimento(attore, bersaglio));
	}
}
