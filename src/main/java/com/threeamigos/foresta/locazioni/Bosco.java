package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.tipi.TipoRiposo;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

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
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.BOSCO;
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

	private static final ClassePersonaggio[] mostri = {
			ClassePersonaggio.ARPIA,
			ClassePersonaggio.CENTAURO,
			ClassePersonaggio.CHIMERA,
			ClassePersonaggio.CHIMERA_DRAGO,
			ClassePersonaggio.EREMITA,
			ClassePersonaggio.FOLLETTO,
			ClassePersonaggio.GIGANTE,
			ClassePersonaggio.GOBLIN,
			ClassePersonaggio.HOBGOBLIN,
			ClassePersonaggio.MINOTAURO,
			ClassePersonaggio.SCHELETRO,
			ClassePersonaggio.TITANO,
			ClassePersonaggio.TROLL,
			ClassePersonaggio.VIVERNA
	};

	private static final ClassiOggetto[] oggetti = {
			ClassiOggetto.ANELLO,
			ClassiOggetto.COFANO,
			ClassiOggetto.CORONA,
			ClassiOggetto.PIETRA_PREZIOSA,
			ClassiOggetto.MONETA,
			ClassiOggetto.SCUDO,
			ClassiOggetto.SPADA,
			ClassiOggetto.SPADONE,
			ClassiOggetto.ARMATURA,
			ClassiOggetto.ELMO,
			ClassiOggetto.SCHINIERI
	};

	@Override
	public ClassePersonaggio[] getPossibiliIncontri() {
		return mostri;
	}

	@Override
	public ClassiOggetto[] getPossibiliOggetti() {
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
