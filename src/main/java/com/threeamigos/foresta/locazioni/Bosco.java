package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;

public class Bosco extends LocazioneBase {

	/**
	 * Quante immagini alternative rappresentano il bosco sulla mappa (Foresta.gif,
	 * Foresta2.gif, ...): vedi ImageCache.getImmagineMappaBosco.
	 */
	public static final int NUMERO_VARIANTI_MAPPA = 4;

	/**
	 * Quale delle {@link #NUMERO_VARIANTI_MAPPA} immagini rappresenta questo bosco sulla
	 * mappa (1-based), scelta una volta per tutte alla costruzione della casella (vedi
	 * Foresta.reimposta) così la mappa resta coerente da una visualizzazione all'altra e
	 * dopo un salvataggio.
	 */
	public static final String VARIANTE_MAPPA = "VARIANTE_MAPPA";

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.BOSCO;
	}

	public static void impostaVarianteMappaACaso(LocazioneMD modelloDati) {
		modelloDati.aggiungiProprieta(VARIANTE_MAPPA, String.valueOf(Dado.tira(NUMERO_VARIANTI_MAPPA)));
	}

	/**
	 * L'indice (1-based) della variante scelta per questo bosco, o 1 se manca (bosco
	 * creato prima che questa proprietà esistesse, es. in un salvataggio precedente).
	 */
	public static int getVarianteMappa(LocazioneMD modelloDati) {
		String valore = modelloDati.ottieniProprieta(VARIANTE_MAPPA);
		return valore == null ? 1 : Integer.parseInt(valore);
	}

	private static final TipoPersonaggio[] mostri = {
			TipoPersonaggio.ARPIA,
			TipoPersonaggio.CENTAURO,
			TipoPersonaggio.CHIMERA,
			TipoPersonaggio.CHIMERA_DRAGO,
			TipoPersonaggio.EREMITA,
			TipoPersonaggio.FOLLETTO,
			TipoPersonaggio.GIGANTE,
			TipoPersonaggio.GOBLIN,
			TipoPersonaggio.HOBGOBLIN,
			TipoPersonaggio.MINOTAURO,
			TipoPersonaggio.SCHELETRO,
			TipoPersonaggio.TITANO,
			TipoPersonaggio.TROLL,
			TipoPersonaggio.VIVERNA
	};

	private static final TipoOggetto[] oggetti = {
			TipoOggetto.ANELLO,
			TipoOggetto.COFANO,
			TipoOggetto.CORONA,
			TipoOggetto.PIETRA_PREZIOSA,
			TipoOggetto.MONETA,
			TipoOggetto.SCUDO,
			TipoOggetto.SPADA,
			TipoOggetto.SPADONE,
			TipoOggetto.ARMATURA,
			TipoOggetto.ELMO,
			TipoOggetto.MASCHERA,
			TipoOggetto.SCHINIERI
	};

	@Override
	public TipoPersonaggio[] getPossibiliIncontri() {
		return mostri;
	}

	@Override
	public TipoOggetto[] getPossibiliOggetti() {
		return oggetti;
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoFrase("Qui, nella foresta, " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
