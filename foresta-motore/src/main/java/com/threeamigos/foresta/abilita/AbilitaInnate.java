package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.canti.CantoCurativo;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Chi ha le abilità innate e quali può usare adesso. Le capacità dipendono dal {@link TipoPersonaggio}, non dalla classe Java.
 */
public final class AbilitaInnate {

	private AbilitaInnate() {
	}

	/**
	 * @return true se la classe ha almeno un'abilità innata (Chierico, Chierica, Druido, Druida)
	 */
	public static boolean conosciutaDa(TipoPersonaggio classe) {
		return isDelChierico(classe) || isDelDruido(classe);
	}

	private static boolean isDelChierico(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.CHIERICO || classe == TipoPersonaggio.CHIERICA;
	}

	private static boolean isDelDruido(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.DRUIDO || classe == TipoPersonaggio.DRUIDA;
	}

	/**
	 * Se la classe conosce quell'abilità: Scacciare i non morti e Imposizione delle mani sono del chierico, le altre del druido.
	 */
	public static boolean conosciutaDa(TipoPersonaggio classe, TipoAbilita tipo) {
		switch (tipo) {
			case SCACCIARE_I_NON_MORTI:
			case IMPOSIZIONE_DELLE_MANI:
				return isDelChierico(classe);
			default:
				return isDelDruido(classe);
		}
	}

	/**
	 * @return true se il personaggio è vivo, conosce quell'abilità, non è {@code SILENZIATO} e ha la MAGIA per usarla
	 */
	public static boolean puoUsare(Personaggio personaggio, TipoAbilita tipo) {
		return personaggio.isVivo() && conosciutaDa(personaggio.getClasse(), tipo)
				&& !personaggio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO)
				&& personaggio.getMagia() >= FabbricaAbilita.crea(tipo, personaggio).getCostoLancio();
	}

	/**
	 * Se un avversario è un non morto vivo, non già spaventato e non immune allo spavento: uno su cui scacciare i non morti serve
	 */
	public static boolean isDaScacciare(Personaggio avversario) {
		return !avversario.isFuoriCombattimento() && avversario.getClasse().isDellaCategoria(CategoriaPersonaggio.NON_MORTO)
				&& !avversario.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)
				&& !avversario.isImmuneAEffetto(TipoEffettoDiStato.SPAVENTATO);
	}

	/**
	 * Il personaggio vivo più ferito del gruppo che non ha già la rigenerazione, se qualcuno è ferito (vedi {@link CantoCurativo#piuFerito})
	 */
	public static Optional<Personaggio> daRigenerare(Gruppo gruppo) {
		Personaggio scelto = null;
		double quotaMinima = 1.0d;
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (!personaggio.isCurabile() || personaggio.hasEffettoDiStato(TipoEffettoDiStato.RIGENERAZIONE)) {
				continue;
			}
			double quota = (double) personaggio.getSalute() / personaggio.getSaluteMassima();
			if (quota < quotaMinima) {
				quotaMinima = quota;
				scelto = personaggio;
			}
		}
		return Optional.ofNullable(scelto);
	}

	/**
	 * Se l'abilità serve adesso, in uno scontro: contro avversari vivi, e (a seconda dell'abilità) se c'è un non morto da scacciare, un ferito da
	 * curare o da far rigenerare
	 */
	public static boolean serve(TipoAbilita tipo, Gruppo gruppo, Gruppo gruppoAvversario) {
		if (gruppoAvversario.getPersonaggiVivi().isEmpty()) {
			return false;
		}
		switch (tipo) {
			case SCACCIARE_I_NON_MORTI:
				return gruppoAvversario.getPersonaggiVivi().stream().anyMatch(AbilitaInnate::isDaScacciare);
			case IMPOSIZIONE_DELLE_MANI:
				return CantoCurativo.piuFerito(gruppo).isPresent();
			case RIGENERAZIONE:
				return daRigenerare(gruppo).isPresent();
			default:
				return true;
		}
	}

	/**
	 * Le abilità che il personaggio può usare adesso <b>nello scontro</b>: quelle che può usare (vedi {@link #abilitaPossibili}) e che servono
	 */
	public static List<TipoAbilita> abilitaUtili(Personaggio personaggio, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<TipoAbilita> abilita = new ArrayList<>();
		for (TipoAbilita tipo : abilitaPossibili(personaggio)) {
			if (serve(tipo, gruppo, gruppoAvversario)) {
				abilita.add(tipo);
			}
		}
		return abilita;
	}

	/**
	 * Le abilità che il personaggio può usare adesso, nell'ordine di {@link TipoAbilita}
	 */
	public static List<TipoAbilita> abilitaPossibili(Personaggio personaggio) {
		List<TipoAbilita> abilita = new ArrayList<>();
		for (TipoAbilita tipo : TipoAbilita.values()) {
			if (puoUsare(personaggio, tipo)) {
				abilita.add(tipo);
			}
		}
		return abilita;
	}
}
