package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.IncantamentoMD;
import com.threeamigos.foresta.combattimento.AnnuncioDellAzione;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.ColpoMagico;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Un'abilità innata che fa danno (le radici e lo sciame del Druido, il dardo arcano di Mago ed Elfo): è essa stessa l'attacco magico, un
 * {@link Arma} che non si impugna. Il tiro per colpire e il danno sono quelli di ogni attacco (vedi {@link ColpoMagico}), con danni e costo propri
 * e senza pergamene: i bersagli sono i primi avversari vivi, fino al NUMERO_BERSAGLI di chi la usa (uno solo per il dardo), e il danno si
 * ripartisce per posizione. Chi la usa dice alla UI chi agisce e su chi prima di colpire.
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
		return getTipoIncantesimo() != null ? TipoAzione.di(getTipoIncantesimo()) : TipoAzione.di(getTipo());
	}

	/**
	 * Quanti avversari colpisce chi la usa.
	 */
	protected int getNumeroBersagli(Personaggio utilizzatore) {
		return utilizzatore.getBersagli();
	}

	/**
	 * La frase che annuncia l'abilità, dopo il nome di chi la usa.
	 *
	 * @param bersagli gli avversari che colpirà, almeno uno
	 */
	protected abstract String getAnnuncio(List<Personaggio> bersagli);

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
		if (massimo == 1 && scelto != null && scelto.isVivo()) {
			bersagli = Collections.singletonList(scelto);
		} else if (bersagli.size() > massimo) {
			bersagli = bersagli.subList(0, massimo);
		}
		AnnuncioDellAzione.azione(utilizzatore, bersagli.size() == 1 ? bersagli.get(0) : null, TipoAzione.di(getTipo()));
		BusEventi.pubblica(new NotificaTestoFrase(utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " " + getAnnuncio(bersagli)));
		// Il danno si ripartisce per posizione, come per gli incantesimi a più bersagli
		for (int posizione = 0; posizione < bersagli.size(); posizione++) {
			ColpoMagico.colpisci(utilizzatore, bersagli.get(posizione), this, getTipoAzione(), getTipoIncantesimo(),
					CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione));
		}
		utilizzatore.subMagia(getCostoLancio());
		return true;
	}
}
