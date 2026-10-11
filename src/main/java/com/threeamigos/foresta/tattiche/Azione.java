package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.tattiche.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.Objects;

/**
 * Un'azione di una voce di tattica: il tipo e, per quelle che li hanno, i parametri: il criterio di scelta dell'incantesimo
 * (INCANTESIMO_DI_DANNO), se rinunciare quando l'attacco con le armi rende di più, il tipo di danno dell'arma voluta (CAMBIA_ARMA).
 * Immutabile. Vedi tattiche_di_combattimento_automatiche.md §11.9 e §11.10.
 */
public final class Azione {

	private final TipoAzione tipo;
	private final CriterioIncantesimo criterio;
	private final boolean soloSePiuForteDellAttacco;
	private final TipoDanno armaDi;

	private Azione(TipoAzione tipo, CriterioIncantesimo criterio, boolean soloSePiuForteDellAttacco, TipoDanno armaDi) {
		this.tipo = tipo;
		this.criterio = criterio;
		this.soloSePiuForteDellAttacco = soloSePiuForteDellAttacco;
		this.armaDi = armaDi;
	}

	/**
	 * Un'azione senza parametri (ogni tipo tranne INCANTESIMO_DI_DANNO e CAMBIA_ARMA)
	 */
	public static Azione di(TipoAzione tipo) {
		if (tipo == TipoAzione.INCANTESIMO_DI_DANNO || tipo == TipoAzione.CAMBIA_ARMA) {
			throw new IllegalArgumentException(tipo + " vuole dei parametri");
		}
		return new Azione(tipo, null, false, null);
	}

	/**
	 * L'incantesimo di danno con un criterio; con <code>soloSePiuForteDellAttacco</code> è applicabile solo se rende più dell'attacco con le armi
	 */
	public static Azione incantesimoDiDanno(CriterioIncantesimo criterio, boolean soloSePiuForteDellAttacco) {
		return new Azione(TipoAzione.INCANTESIMO_DI_DANNO, Objects.requireNonNull(criterio), soloSePiuForteDellAttacco, null);
	}

	public static Azione cambiaArma(TipoDanno armaDi) {
		return new Azione(TipoAzione.CAMBIA_ARMA, null, false, Objects.requireNonNull(armaDi));
	}

	/**
	 * La stessa azione che si applica solo se rende più dell'attacco con le armi: per gli incantesimi e il dardo che fanno danno
	 */
	public Azione soloSePiuForteDellAttacco() {
		if (tipo == TipoAzione.INCANTESIMO_DI_DANNO) {
			return incantesimoDiDanno(criterio, true);
		}
		if (!faDanno(tipo)) {
			throw new IllegalArgumentException(tipo + " non fa danno da confrontare con l'attacco");
		}
		return new Azione(tipo, null, true, null);
	}

	private static boolean faDanno(TipoAzione tipo) {
		return tipo == TipoAzione.DARDO_ARCANO
				|| (tipo.getClasseIncantesimo() != null && tipo.getTipoDanno() != null && tipo != TipoAzione.SILENZIO && tipo != TipoAzione.MORTE);
	}

	public TipoAzione getTipo() {
		return tipo;
	}

	/** Solo per INCANTESIMO_DI_DANNO */
	public CriterioIncantesimo getCriterio() {
		return criterio;
	}

	public boolean isSoloSePiuForteDellAttacco() {
		return soloSePiuForteDellAttacco;
	}

	/** Solo per CAMBIA_ARMA */
	public TipoDanno getArmaDi() {
		return armaDi;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(tipo.name());
		if (criterio != null) {
			sb.append(' ').append(criterio);
		}
		if (armaDi != null) {
			sb.append(' ').append(armaDi);
		}
		if (soloSePiuForteDellAttacco) {
			sb.append(" SOLO_SE_PIU_FORTE_DELL_ATTACCO");
		}
		return sb.toString();
	}
}
