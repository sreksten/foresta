package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.CostantiIncantesimi;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;

import java.util.List;

/**
 * Un grido di battaglia (incantesimo GLOBALE benefico): a ogni personaggio vivo del gruppo di chi lo lancia dà due modificatori temporanei,
 * CORAGGIO e VALORE, di un numero fisso di punti (INCANTESIMO_GRIDO_DI_BATTAGLIA_BASE_* per la radice quadrata del livello di chi lo lancia) per
 * INCANTESIMO_GRIDO_DI_BATTAGLIA_DURATA turni. Lanciarlo di nuovo non raddoppia l'effetto: rinfresca la durata (vedi Personaggio.addModificatore).
 * Come {@link Resurrezione} non è un incantesimo malefico: non ha danni né tipo di danno.
 */
public class GridoDiBattaglia implements IncantesimoGlobale {

	/**
	 * La nota dei due modificatori: li distingue dagli altri
	 */
	public static final String NOTA = "Grido di battaglia";

	private final int livello;

	public GridoDiBattaglia(int livello) {
		this.livello = livello;
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.GRIDO_DI_BATTAGLIA;
	}

	public int getLivello() {
		return livello;
	}

	/**
	 * Quanti punti di coraggio dà a ognuno del gruppo
	 */
	public int getPuntiCoraggio() {
		return (int) Math.round(CostantiIncantesimi.INCANTESIMO_GRIDO_DI_BATTAGLIA_BASE_CORAGGIO * Math.sqrt(livello));
	}

	/**
	 * Quanti punti di valore dà a ognuno del gruppo
	 */
	public int getPuntiValore() {
		return (int) Math.round(CostantiIncantesimi.INCANTESIMO_GRIDO_DI_BATTAGLIA_BASE_VALORE * Math.sqrt(livello));
	}

	/**
	 * @param gruppoBersaglio il gruppo di chi la lancia, non quello avversario
	 */
	@Override
	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " lancia un grido di battaglia: il gruppo si sente più coraggioso e valoroso."));
		for (Personaggio personaggio : gruppoBersaglio.getPersonaggiVivi()) {
			personaggio.addModificatore(modificatore(TipoAttributo.CORAGGIO, getPuntiCoraggio()));
			personaggio.addModificatore(modificatore(TipoAttributo.VALORE, getPuntiValore()));
		}
		formulante.subMagia(getCostoLancio());
		StatoDelCombattimento.getIstanza().registraIncantesimoLanciato(formulante);
	}

	private static ModificatoreAttributoMD modificatore(TipoAttributo attributo, int punti) {
		return new ModificatoreAttributoMD(attributo, TipoModificatore.VARIAZIONE_FISSA, punti, NOTA, UnitaDurataModificatore.TURNI,
				CostantiIncantesimi.INCANTESIMO_GRIDO_DI_BATTAGLIA_DURATA, BersaglioModificatore.PORTATORE);
	}

	/**
	 * Serve se c'è fra gli alleati qualcuno in piedi che non è già sotto il grido di battaglia
	 */
	@Override
	public boolean serve(List<Personaggio> alleati, List<Personaggio> avversari) {
		for (Personaggio alleato : alleati) {
			if (!alleato.isFuoriCombattimento() && alleato.getModificatoriTemporanei().stream().noneMatch(m -> NOTA.equals(m.getNote()))) {
				return true;
			}
		}
		return false;
	}
}
