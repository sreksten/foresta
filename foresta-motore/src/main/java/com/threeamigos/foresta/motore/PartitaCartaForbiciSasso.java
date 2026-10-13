package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;

/**
 * Una sfida a carta, forbici e sasso: vince chi arriva per primo a {@link Costanti#VITTORIE_PER_VINCERE_LA_SFIDA}
 * mani vinte. Ogni mano è la scelta del giocatore contro una dell'avversario, tirata con il dado della partita; un
 * pareggio non conta e si rigioca.
 */
public class PartitaCartaForbiciSasso {

	/**
	 * L'esito di una mano: quello che si è giocato e come è andata.
	 */
	public static final class Mano {
		private final MossaCartaForbiciSasso giocatore;
		private final MossaCartaForbiciSasso avversario;

		private Mano(MossaCartaForbiciSasso giocatore, MossaCartaForbiciSasso avversario) {
			this.giocatore = giocatore;
			this.avversario = avversario;
		}

		public MossaCartaForbiciSasso getGiocatore() {
			return giocatore;
		}

		public MossaCartaForbiciSasso getAvversario() {
			return avversario;
		}

		public boolean isPareggio() {
			return giocatore == avversario;
		}

		public boolean haVintoIlGiocatore() {
			return giocatore.batte(avversario);
		}
	}

	private int vittorieDelGiocatore;
	private int vittorieDellAvversario;

	/**
	 * Gioca una mano: l'avversario sceglie a caso la sua mossa. Dopo la fine della sfida non si gioca più.
	 */
	public Mano gioca(MossaCartaForbiciSasso mossaDelGiocatore) {
		if (isFinita()) {
			throw new IllegalStateException("La sfida è già finita");
		}
		MossaCartaForbiciSasso[] mosse = MossaCartaForbiciSasso.values();
		Mano mano = new Mano(mossaDelGiocatore, mosse[Dado.tira(mosse.length) - 1]);
		if (mano.haVintoIlGiocatore()) {
			vittorieDelGiocatore++;
		} else if (!mano.isPareggio()) {
			vittorieDellAvversario++;
		}
		return mano;
	}

	public int getVittorieDelGiocatore() {
		return vittorieDelGiocatore;
	}

	public int getVittorieDellAvversario() {
		return vittorieDellAvversario;
	}

	public boolean isFinita() {
		return vittorieDelGiocatore >= Costanti.VITTORIE_PER_VINCERE_LA_SFIDA
				|| vittorieDellAvversario >= Costanti.VITTORIE_PER_VINCERE_LA_SFIDA;
	}

	public boolean haVintoIlGiocatore() {
		return vittorieDelGiocatore >= Costanti.VITTORIE_PER_VINCERE_LA_SFIDA;
	}
}
