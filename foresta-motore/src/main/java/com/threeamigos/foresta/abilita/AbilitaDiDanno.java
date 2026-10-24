package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.incantesimi.IncantesimoMaleficoImpl;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.List;

/**
 * Un'abilità che fa danno a più avversari (le radici e lo sciame del Druido). Il tiro per colpire e il danno sono quelli di ogni incantesimo di danno
 * (vedi {@link IncantesimoMaleficoImpl}), con danni e costo propri e senza pergamene: i bersagli sono i primi avversari vivi, fino al NUMERO_BERSAGLI di
 * chi la usa. Le immunità sono quelle dell'incantesimo di quel tipo di danno ({@link #getTipoIncantesimo}).
 */
abstract class AbilitaDiDanno implements Abilita {

	/**
	 * L'incantesimo che ha lo stesso tipo di danno: serve per le immunità e per ricordare l'esito contro i bersagli
	 */
	protected abstract TipoIncantesimo getTipoIncantesimo();

	protected abstract TipoDanno getTipoDanno();

	protected abstract int getDanni();

	/**
	 * La frase che annuncia l'abilità, dopo il nome di chi la usa
	 */
	protected abstract String getAnnuncio();

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<Personaggio> bersagli = gruppoAvversario.getPersonaggiVivi();
		if (bersagli.isEmpty()) {
			return false;
		}
		if (bersagli.size() > utilizzatore.getBersagli()) {
			bersagli = bersagli.subList(0, utilizzatore.getBersagli());
		}
		BusEventi.pubblica(new NotificaTestoFrase(utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " " + getAnnuncio()));
		Colpo colpo = new Colpo(utilizzatore.getLivello());
		// Il danno si ripartisce per posizione, come per gli incantesimi a più bersagli
		for (int posizione = 0; posizione < bersagli.size(); posizione++) {
			colpo.colpisciUno(utilizzatore, bersagli.get(posizione), CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione));
		}
		utilizzatore.subMagia(getCostoLancio());
		return true;
	}

	/**
	 * Il colpo su un bersaglio, come un incantesimo malefico (il motore dei danni vuole un Incantesimo)
	 */
	private final class Colpo extends IncantesimoMaleficoImpl implements Incantesimo {

		private Colpo(int livello) {
			super(livello);
		}

		@Override
		public TipoIncantesimo getClasse() {
			return getTipoIncantesimo();
		}

		@Override
		public int getCostoLancio() {
			return AbilitaDiDanno.this.getCostoLancio();
		}

		@Override
		public TipoDanno getTipoDanno() {
			return AbilitaDiDanno.this.getTipoDanno();
		}

		@Override
		public int getDanni() {
			return AbilitaDiDanno.this.getDanni();
		}

		private void colpisciUno(Personaggio utilizzatore, Personaggio bersaglio, double quotaDelDanno) {
			colpisci(utilizzatore, bersaglio, quotaDelDanno);
		}
	}
}
