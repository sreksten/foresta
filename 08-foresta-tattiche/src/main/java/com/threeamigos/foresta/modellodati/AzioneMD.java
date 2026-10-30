package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.serializzazione.LettoreCampi;
import com.threeamigos.foresta.tipi.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoAzione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

import static com.threeamigos.foresta.serializzazione.Serializzabile.facoltativo;
import static com.threeamigos.foresta.serializzazione.Serializzabile.stampaRiga;

/**
 * Un'azione di una voce di tattica: il tipo e, per quelle che li hanno, i parametri: il criterio di scelta
 * dell'incantesimo (INCANTESIMO_DI_DANNO), se rinunciare quando l'attacco con le armi rende di più, il tipo di danno
 * dell'arma voluta (CAMBIA_ARMA). È immutabile. Vedi tattiche_di_combattimento_automatiche.md §11.9 e §11.10.
 *
 * @author Stefano Reksten
 */
public final class AzioneMD {

	/**
	 * Un'azione senza parametri (ogni tipo tranne INCANTESIMO_DI_DANNO e CAMBIA_ARMA)
	 */
	public static AzioneMD di(TipoAzione tipo) {
		if (tipo == TipoAzione.INCANTESIMO_DI_DANNO || tipo == TipoAzione.INCANTESIMO_DEL_REPERTORIO || tipo == TipoAzione.CAMBIA_ARMA) {
			throw new IllegalArgumentException(tipo + " vuole dei parametri");
		}
		return new AzioneMD(tipo, null, false, null);
	}

	/**
	 * L'incantesimo di danno con un criterio; con <code>soloSePiuForteDellAttacco</code> è applicabile solo se rende più dell'attacco con le armi
	 */
	public static AzioneMD incantesimoDiDanno(CriterioIncantesimo criterio, boolean soloSePiuForteDellAttacco) {
		return new AzioneMD(TipoAzione.INCANTESIMO_DI_DANNO, Objects.requireNonNull(criterio), soloSePiuForteDellAttacco, null);
	}

	/**
	 * L'incantesimo del repertorio (anche Morte e Silenzio) con un criterio: come scelgono i mostri oggi
	 */
	public static AzioneMD incantesimoDelRepertorio(CriterioIncantesimo criterio, boolean soloSePiuForteDellAttacco) {
		return new AzioneMD(TipoAzione.INCANTESIMO_DEL_REPERTORIO, Objects.requireNonNull(criterio), soloSePiuForteDellAttacco, null);
	}

	/**
	 * Un'azione che fa impugnare l'arma di quel tipo di danno; il tipo di danno è obbligatorio.
	 */
	public static AzioneMD cambiaArma(TipoDanno armaDi) {
		return new AzioneMD(TipoAzione.CAMBIA_ARMA, null, false, Objects.requireNonNull(armaDi));
	}

	private static boolean faDanno(TipoAzione tipo) {
		return tipo == TipoAzione.DARDO_ARCANO
				|| (tipo.getClasseIncantesimo() != null && tipo.getTipoDanno() != null && tipo != TipoAzione.SILENZIO && tipo != TipoAzione.MORTE
				&& tipo.getClasseIncantesimo().getPortata() != PortataIncantesimo.GLOBALE);
	}

	private final TipoAzione tipo;
	private final CriterioIncantesimo criterio;
	private final boolean soloSePiuForteDellAttacco;
	private final TipoDanno armaDi;

	private AzioneMD(TipoAzione tipo, CriterioIncantesimo criterio, boolean soloSePiuForteDellAttacco, TipoDanno armaDi) {
		this.tipo = tipo;
		this.criterio = criterio;
		this.soloSePiuForteDellAttacco = soloSePiuForteDellAttacco;
		this.armaDi = armaDi;
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

	/**
	 * La stessa azione che si applica solo se rende più dell'attacco con le armi: per gli incantesimi e il dardo che fanno danno
	 */
	public AzioneMD soloSePiuForteDellAttacco() {
		if (tipo == TipoAzione.INCANTESIMO_DI_DANNO) {
			return incantesimoDiDanno(criterio, true);
		}
		if (tipo == TipoAzione.INCANTESIMO_DEL_REPERTORIO) {
			return incantesimoDelRepertorio(criterio, true);
		}
		if (!faDanno(tipo)) {
			throw new IllegalArgumentException(tipo + " non fa danno da confrontare con l'attacco");
		}
		return new AzioneMD(tipo, null, true, null);
	}

	/**
	 * Scrive l'azione su una riga (vedi FormatoTattiche).
	 */
	public void salva(PrintWriter stream) {
		stampaRiga(stream,
				"AZIONE",
				tipo,
				facoltativo(criterio),
				soloSePiuForteDellAttacco,
				facoltativo(armaDi));
	}

	/**
	 * Rilegge un'azione scritta da {@link #salva(PrintWriter)}, passando dalle fabbriche che controllano i parametri.
	 */
	public static AzioneMD leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = FormatoTattiche.riga(stream, "AZIONE");
		TipoAzione tipo = campi.enumerato(TipoAzione.class);
		CriterioIncantesimo criterio = campi.enumeratoFacoltativo(CriterioIncantesimo.class);
		boolean solo = campi.booleano();
		TipoDanno armaDi = campi.enumeratoFacoltativo(TipoDanno.class);
		try {
			if (tipo == TipoAzione.INCANTESIMO_DI_DANNO) {
				return incantesimoDiDanno(criterio, solo);
			}
			if (tipo == TipoAzione.INCANTESIMO_DEL_REPERTORIO) {
				return incantesimoDelRepertorio(criterio, solo);
			}
			if (tipo == TipoAzione.CAMBIA_ARMA) {
				return cambiaArma(armaDi);
			}
			AzioneMD azione = di(tipo);
			return solo ? azione.soloSePiuForteDellAttacco() : azione;
		} catch (NullPointerException | IllegalArgumentException e) {
			throw new IOException("Azione " + tipo + " incoerente nel salvataggio", e);
		}
	}

	/**
	 * Una descrizione leggibile, per i messaggi di log e di prova.
	 */
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

	/**
	 * Due azioni sono uguali se hanno gli stessi dati.
	 */
	@Override
	public boolean equals(Object altro) {
		if (!(altro instanceof AzioneMD)) {
			return false;
		}
		AzioneMD a = (AzioneMD) altro;
		return tipo == a.tipo && criterio == a.criterio && soloSePiuForteDellAttacco == a.soloSePiuForteDellAttacco && armaDi == a.armaDi;
	}

	/**
	 * Coerente con {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(tipo, criterio, soloSePiuForteDellAttacco, armaDi);
	}

}
