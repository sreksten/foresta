package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAzioneInCombattimento;
import com.threeamigos.foresta.eventi.interni.InternoDecisioneTattica;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.incantesimi.AlbaSacra;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.FabbricaCanti;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.TipoCanto;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tattiche.Decisione;
import com.threeamigos.foresta.tattiche.SceltaIncantesimo;
import com.threeamigos.foresta.tattiche.Valutatore;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
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
		Decisione decisione = Valutatore.decidi(tattica, ContestoTattico.per(attore, miei, avversari));
		if (decisione == null) {
			return null;
		}
		BusEventi.pubblica(new InternoDecisioneTattica(attore, decisione.getBersaglio(), decisione.getTipoEseguito(), decisione.getIndiceVoce(),
				decisione.getSpiegazione()));
		esegui(attore, decisione);
		if (!decisione.isRipiego()) {
			stato.registraUsoVoce(attore, decisione.getIdentificativoVoce());
		}
		return decisione;
	}

	/**
	 * Fa quello che la decisione dice
	 */
	public void esegui(Personaggio attore, Decisione decisione) {
		boolean deiMiei = miei.contiene(attore);
		Gruppo gruppoDellAttore = deiMiei ? miei : avversari;
		Gruppo gruppoNemico = deiMiei ? avversari : miei;
		Personaggio bersaglio = decisione.getBersaglio();
		switch (decisione.getTipoEseguito()) {
			case ATTACCO_FISICO:
				if (deiMiei) {
					attaccaConLeArmi(attore, bersaglio);
				} else {
					attore.attaccaConLeArmi(bersaglio);
				}
				break;
			case DARDO_ARCANO:
				scagliaIlDardo(attore, bersaglio);
				break;
			case RESURREZIONE:
				annuncia(attore, bersaglio);
				FabbricaIncantesimi.crea(ClasseIncantesimo.RESURREZIONE, attore.getLivello()).formula(attore, bersaglio, null);
				consumaLaPergamena(gruppoDellAttore, ClasseIncantesimo.RESURREZIONE);
				break;
			case ALBA_SACRA:
				annuncia(attore, null);
				FabbricaIncantesimi.crea(ClasseIncantesimo.ALBA_SACRA, attore.getLivello()).formula(attore, null, gruppoDellAttore);
				// Chi la conosce per natura (Sacerdote, Sacerdotessa) non consuma pergamene
				if (!AlbaSacra.conosciutaDa(attore.getClasse())) {
					consumaLaPergamena(gruppoDellAttore, ClasseIncantesimo.ALBA_SACRA);
				}
				break;
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
			case POZIONE_SALUTE:
				beviLaPozioneDellaSalute(attore, bersaglio, gruppoDellAttore);
				break;
			case POZIONE_MAGIA:
				beviLaPozioneDellaMagia(attore, bersaglio, gruppoDellAttore);
				break;
			case CAMBIA_ARMA:
				cambiaArma(attore, decisione);
				break;
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
		BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(attore, bersaglio));
		annuncia(attore, bersaglio);

		Logger.log(attore.getNome() + " attacca " + bersaglio.getNome());

		for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(attore, bersaglio)) {
			Arma arma = fase.getArma();
			boolean colpisce = CalcolatoreCombattimento.colpisce(attore, bersaglio, arma.getTipoDanno().getSuperTipo());
			if (colpisce) {
				DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attore, bersaglio, arma, fase.getFattore());
				bersaglio.applicaRisultatoCombattimento(risultato);
				if (bersaglio.isFuoriCombattimento()) {
					uccisioni.registra(attore, bersaglio);
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
		ClasseIncantesimo classe = decisione.getIncantesimo();
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
		consumaLaPergamena(gruppoDellAttore, classe);
	}

	private static void consumaLaPergamena(Gruppo gruppoDellAttore, ClasseIncantesimo classe) {
		if (gruppoDellAttore.getIncantesimi(classe) > 0) {
			gruppoDellAttore.subIncantesimi(classe, 1);
		}
	}

	private void canta(Personaggio cantore, TipoCanto tipo, Personaggio bersaglio, Gruppo gruppoDelCantore, Gruppo gruppoNemico) {
		if (FabbricaCanti.crea(tipo, cantore).cantaSu(cantore, bersaglio, gruppoDelCantore, gruppoNemico)) {
			stato.registraIncantesimoLanciato(cantore);
			annuncia(cantore, null);
		}
	}

	/**
	 * Beve la pozione grande se manca almeno quanto ne ridà (o se non c'è altra), altrimenti la normale
	 */
	private void beviLaPozioneDellaSalute(Personaggio attore, Personaggio bevitore, Gruppo gruppoDellAttore) {
		annuncia(attore, null);
		boolean grande = gruppoDellAttore.getPozioniSaluteGrande() > 0;
		boolean normale = gruppoDellAttore.getPozioniSalute() > 0;
		int mancante = bevitore.getSaluteMassima() - bevitore.getSalute();
		if (grande && (mancante >= Costanti.RECUPERO_DA_POZIONE_SALUTE_GRANDE || !normale)) {
			gruppoDellAttore.consumaPozioneSaluteGrande(bevitore);
		} else if (normale) {
			gruppoDellAttore.consumaPozioneSalute(bevitore);
		}
		stato.registraAlleato(attore, bevitore);
	}

	private void beviLaPozioneDellaMagia(Personaggio attore, Personaggio bevitore, Gruppo gruppoDellAttore) {
		annuncia(attore, null);
		boolean grande = gruppoDellAttore.getPozioniMagiaGrande() > 0;
		boolean normale = gruppoDellAttore.getPozioniMagia() > 0;
		int mancante = bevitore.getMagiaMassima() - bevitore.getMagia();
		if (grande && (mancante >= Costanti.RECUPERO_DA_POZIONE_MAGIA_GRANDE || !normale)) {
			gruppoDellAttore.consumaPozioneMagiaGrande(bevitore);
		} else if (normale) {
			gruppoDellAttore.consumaPozioneMagia(bevitore);
		}
		stato.registraAlleato(attore, bevitore);
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

	private static void annuncia(Personaggio attore, Personaggio bersaglio) {
		BusEventi.pubblica(new InternoAzioneInCombattimento(attore, bersaglio));
	}
}
