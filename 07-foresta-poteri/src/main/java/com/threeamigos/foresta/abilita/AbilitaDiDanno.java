package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.combattimento.AnnuncioDellAzione;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.ColpoMagico;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoAzione;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/**
 * Un'abilità innata che fa danno (le radici e lo sciame del Druido, il dardo arcano di Mago ed Elfo): è essa stessa l'attacco magico, un
 * {@link Arma} che non si impugna. Il tiro per colpire e il danno sono quelli di ogni attacco (vedi ColpoMagico), con danni e costo propri
 * e senza pergamene: i bersagli sono i primi avversari vivi, fino al NUMERO_BERSAGLI di chi la usa (uno solo per il dardo), e il danno si
 * ripartisce per posizione. Se chi la usa ha già scelto un bersaglio (una tattica) si comincia da lui. L'abilità dice alla UI chi agisce e su chi
 * prima di colpire.
 */
public abstract class AbilitaDiDanno implements Abilita, Arma {

	private final int livello;

	/**
	 * @param utilizzatore chi la usa: ne dipende il livello dell'attacco
	 */
	protected AbilitaDiDanno(Personaggio utilizzatore) {
		this.livello = utilizzatore.getLivello();
	}

	/**
	 * L'incantesimo che ha lo stesso tipo di danno: dà le immunità e, se non si dice altro, l'azione con cui si ricorda l'esito contro i
	 * bersagli. Null se l'abilità non ne ha uno.
	 */
	protected TipoIncantesimo getTipoIncantesimo() {
		return null;
	}

	/**
	 * L'azione con cui si ricorda l'esito contro i bersagli (per le tattiche): quella dell'incantesimo di riferimento, se c'è, altrimenti
	 * quella di questa abilità.
	 */
	protected TipoAzione getTipoAzione() {
		return getTipoIncantesimo() != null ? TipoAzione.di(getTipoIncantesimo()) : getTipo().getTipoAzione();
	}

	/**
	 * Quanti avversari colpisce chi la usa.
	 */
	protected int getNumeroBersagli(Personaggio utilizzatore) {
		return utilizzatore.getBersagli();
	}

	/**
	 * La frase che annuncia l'abilità (da {@code messaggi_poteri_<lingua>.properties}), col nome di chi la usa all'inizio.
	 *
	 * @param bersagli gli avversari che colpirà, almeno uno
	 */
	protected abstract String getAnnuncio(Personaggio utilizzatore, List<Personaggio> bersagli);

	/**
	 * Il nome con l'articolo e l'iniziale maiuscola, per aprire una frase.
	 */
	protected static String chi(Personaggio personaggio) {
		return personaggio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
	}

	@Override
	public int getLivello() {
		return livello;
	}

	@Override
	public boolean isIncantata() {
		return false;
	}

	@Override
	public Collection<IncantamentoMD> getIncantamenti() {
		return Collections.emptyList();
	}

	@Override
	public boolean isMagica() {
		return true;
	}

	/**
	 * Come per gli incantesimi, il costo per bersaglio segue il danno base, quello di chi la usa al suo livello (il dardo arcano dell'Elfo
	 * cresce con il livello, e con lui il costo)
	 */
	@Override
	public int getCostoPerBersaglio() {
		return CostantiIncantesimi.costoPerBersaglio(getDanni());
	}

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		return usaSu(utilizzatore, null, gruppo, gruppoAvversario);
	}

	/**
	 * Con un solo bersaglio possibile (il dardo) colpisce quello scelto, se è ancora in piedi; altrimenti i primi avversari vivi.
	 */
	@Override
	public boolean usaSu(Personaggio utilizzatore, Personaggio scelto, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<Personaggio> bersagli = gruppoAvversario.getPersonaggiVivi();
		if (bersagli.isEmpty()) {
			return false;
		}
		int massimo = getNumeroBersagli(utilizzatore);
		if (scelto != null && scelto.isVivo() && bersagli.contains(scelto)) {
			// Il bersaglio scelto (da una tattica) per primo, poi gli altri avversari vivi nel loro ordine
			List<Personaggio> inOrdine = new ArrayList<>(bersagli);
			inOrdine.remove(scelto);
			inOrdine.add(0, scelto);
			bersagli = inOrdine;
		}
		if (bersagli.size() > massimo) {
			bersagli = bersagli.subList(0, massimo);
		}
		// Si colpiscono solo i bersagli che la Magia di chi la usa può pagare
		bersagli = soloIPagabili(utilizzatore, bersagli);
		if (bersagli.isEmpty()) {
			return false;
		}
		AnnuncioDellAzione.azione(utilizzatore, bersagli.size() == 1 ? bersagli.get(0) : null, getTipo().getTipoAzione());
		BusEventi.pubblica(new NotificaTestoFrase(getAnnuncio(utilizzatore, bersagli)));
		// Il danno si ripartisce per posizione, come per gli incantesimi a più bersagli
		for (int posizione = 0; posizione < bersagli.size(); posizione++) {
			colpisci(utilizzatore, bersagli.get(posizione), CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione));
		}
		dopoIColpi(utilizzatore);
		paga(utilizzatore, bersagli.size());
		return true;
	}

	/**
	 * Un colpo su un bersaglio: il bersaglio subisce quella quota del danno (vedi CalcolatoreCombattimento.quotaDelDannoPerPosizione).
	 */
	protected void colpisci(Personaggio utilizzatore, Personaggio bersaglio, double quotaDelDanno) {
		ColpoMagico.colpisci(utilizzatore, bersaglio, this, getTipoAzione(), getTipoIncantesimo(), quotaDelDanno);
	}

	/**
	 * Quel che l'abilità fa in più dopo aver colpito i bersagli, prima di pagare la Magia. Per default niente.
	 */
	protected void dopoIColpi(Personaggio utilizzatore) {
	}
}
