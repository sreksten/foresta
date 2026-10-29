package com.threeamigos.foresta.tattiche.valutazione;

import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.canti.CantoDelBardo;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoFiltro;

import java.util.EnumMap;
import java.util.Map;

/**
 * La risposta di ogni {@link TipoFiltro} per un candidato (tattiche_di_combattimento_automatiche.md §11.5). Senza stato: quello che serve
 * ricordare lo leggono dalle schede dello StatoDelCombattimento, attraverso il {@link ContestoTattico}. Non cambiano niente e non tirano i dadi.
 */
public final class Filtri {

	/**
	 * La risposta di un tipo di filtro: vero se il candidato passa, senza tenere conto della negazione (la applica {@link #ammette})
	 */
	private interface Verifica {
		boolean ammette(FiltroMD filtro, Personaggio candidato, ContestoTattico contesto);
	}

	// Una risposta per ogni tipo di filtro: aggiungere un valore a TipoFiltro senza aggiungerla qui fa fallire un test (haLaSuaRisposta)
	private static final Map<TipoFiltro, Verifica> VERIFICHE = new EnumMap<>(TipoFiltro.class);

	static {
		VERIFICHE.put(TipoFiltro.SALUTE, (f, c, ctx) -> f.getConfronto().vale(c.getSalute(), f.getNumero()));
		// Con una salute massima nulla si evita la divisione per zero e la percentuale vale 0
		VERIFICHE.put(TipoFiltro.SALUTE_PERCENTUALE, (f, c, ctx) -> f.getConfronto().vale(
				c.getSaluteMassima() <= 0 ? 0 : 100.0d * c.getSalute() / c.getSaluteMassima(), f.getNumero()));
		VERIFICHE.put(TipoFiltro.MAGIA, (f, c, ctx) -> f.getConfronto().vale(c.getMagia(), f.getNumero()));
		VERIFICHE.put(TipoFiltro.LIVELLO, (f, c, ctx) -> f.getConfronto().vale(c.getLivello(), f.getNumero()));
		VERIFICHE.put(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, (f, c, ctx) -> f.getConfronto().vale(c.getLivello(), ctx.getAttore().getLivello()));
		// I filtri su classe, categoria e magia non comprendono mai gli ospiti (tattiche_di_combattimento_automatiche.md §7.5, F1)
		VERIFICHE.put(TipoFiltro.CLASSE, (f, c, ctx) -> !ctx.isOspite(c) && c.getClasse().stessoRuoloDi(f.getTipoPersonaggio()));
		VERIFICHE.put(TipoFiltro.CATEGORIA, (f, c, ctx) -> !ctx.isOspite(c) && c.getClasse().isDellaCategoria(f.getCategoriaPersonaggio()));
		// stessoRuoloDi: il Mago e la Maga sono lo stesso ruolo, quindi un filtro CLASSE MAGO prende entrambi
		VERIFICHE.put(TipoFiltro.STATO, (f, c, ctx) -> c.hasEffettoDiStato(f.getTipoEffettoDiStato()));
		VERIFICHE.put(TipoFiltro.STATO_NEGATIVO, (f, c, ctx) -> haUnoStatoNegativo(c));
		VERIFICHE.put(TipoFiltro.MAGICO, (f, c, ctx) -> !ctx.isOspite(c) && isMagico(c, ctx));
		// DEBOLE_A ha il tipo di danno come parametro, DEBOLE_ALL_AZIONE lo ricava dall'azione che il contesto sta valutando (conAzione): fuori da una
		// voce, o per un'azione senza tipo di danno fisso (l'attacco dipende dall'arma), non è debole a niente
		VERIFICHE.put(TipoFiltro.DEBOLE_A, (f, c, ctx) -> isDebole(c, f.getTipoDanno()));
		VERIFICHE.put(TipoFiltro.DEBOLE_ALL_AZIONE, (f, c, ctx) -> {
			TipoDanno tipoDanno = ctx.getAzioneInValutazione() == null ? null : ctx.getAzioneInValutazione().getTipoDanno();
			return tipoDanno != null && isDebole(c, tipoDanno);
		});
		VERIFICHE.put(TipoFiltro.IMMUNE_A, (f, c, ctx) -> c.isImmuneATipoDanno(f.getTipoDanno()));
		VERIFICHE.put(TipoFiltro.IL_CAPO, (f, c, ctx) -> ctx.isCapo(c));
		VERIFICHE.put(TipoFiltro.ME_STESSO, (f, c, ctx) -> c == ctx.getAttore());
		// "Scelto l'ultima volta" vale sia per l'avversario che per l'alleato ricordati nella scheda dello stato: la rosa del selettore decide quale dei due
		// può comparire fra i candidati. Chi ha attaccato per ultimo, invece, è sempre un avversario
		VERIFICHE.put(TipoFiltro.SCELTO_L_ULTIMA_VOLTA, (f, c, ctx) ->
				c == ctx.getStato().getUltimoAvversario(ctx.getAttore()) || c == ctx.getStato().getUltimoAlleato(ctx.getAttore()));
		VERIFICHE.put(TipoFiltro.MI_HA_ATTACCATO_PER_ULTIMO, (f, c, ctx) -> c == ctx.getStato().getUltimoAttaccante(ctx.getAttore()));
		VERIFICHE.put(TipoFiltro.ARMA_CHE_DA, (f, c, ctx) -> daTipoDanno(c.getArmaEquipaggiata(), f.getTipoDanno())
				|| c.getArmaSecondaria().map(a -> daTipoDanno(a, f.getTipoDanno())).orElse(false));
	}

	private Filtri() {
	}

	/**
	 * Se ha almeno un effetto di stato che è un male da togliere: qualunque tranne quelli benefici (ISPIRATO, BERSERK)
	 */
	static boolean haUnoStatoNegativo(Personaggio personaggio) {
		return AbilitaInnate.haUnoStatoNegativo(personaggio);
	}

	/**
	 * Se il candidato passa il filtro (tenendo conto della negazione)
	 */
	public static boolean ammette(FiltroMD filtro, Personaggio candidato, ContestoTattico contesto) {
		return VERIFICHE.get(filtro.getTipoFiltro()).ammette(filtro, candidato, contesto) != filtro.isNegato();
	}

	/**
	 * Se per ogni tipo di filtro c'è la sua risposta (lo controlla un test)
	 */
	public static boolean haLaSuaRisposta(TipoFiltro tipo) {
		return VERIFICHE.containsKey(tipo);
	}

	/**
	 * Se l'arma dà quel tipo di danno (per le armi che ne danno più d'uno basta uno)
	 */
	private static boolean daTipoDanno(Arma arma, TipoDanno tipoDanno) {
		return arma.daTipoDanno(tipoDanno);
	}

	/**
	 * Debole: il moltiplicatore di classe e di stati maggiore di 1, senza contare la difesa (§6.3)
	 */
	private static boolean isDebole(Personaggio candidato, TipoDanno tipoDanno) {
		return CalcolatoreCombattimento.moltiplicatoreDelleInterazioni(candidato, tipoDanno,
				CalcolatoreCombattimento.interazioniAttive(candidato, tipoDanno)) > 1.0d;
	}

	/**
	 * Chi sa fare magie adesso (§7.5, F3): per una classe con un'abilità innata (Dardo arcano, Alba sacra, canti, abilità di chierico e druido) conta quella, se non è
	 * silenziato e ha la MAGIA per almeno una; per un mostro di una classe magica, se non è silenziato e ha MAGIA; per gli altri, se non è
	 * silenziato, ha MAGIA e ha già lanciato un incantesimo in questo combattimento.
	 */
	private static boolean isMagico(Personaggio candidato, ContestoTattico contesto) {
		if (candidato.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO)) {
			return false;
		}
		TipoPersonaggio classe = candidato.getClasse();
		if (CantoDelBardo.conosciutoDa(classe)
				|| AbilitaInnate.conosciutaDa(classe)) {
			return !CantoDelBardo.cantiPossibili(candidato).isEmpty() || !AbilitaInnate.abilitaPossibili(candidato).isEmpty();
		}
		if (contesto.isMostro(candidato)) {
			return candidato.isMagico() && candidato.getMagia() > 0;
		}
		return candidato.getMagia() > 0 && contesto.getStato().haLanciatoUnIncantesimo(candidato);
	}
}
