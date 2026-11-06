package com.threeamigos.foresta.libgdx.sviluppo;

import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.PredicatoMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.tipi.CriterioIncantesimo;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoConfronto;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoFiltro;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoRosa;

import java.util.Arrays;
import java.util.Collections;

/**
 * La tattica di prova della schermata delle tattiche: due voci, la prima con un filtro negato, una condizione "Quanti" e due azioni, la seconda con una
 * probabilità e due pozioni.
 */
public final class TatticheDiProva {

	private TatticheDiProva() {
	}

	public static TatticaMD tattica() {
		return TatticaMD.di(
				new VoceMD(new SelettoreMD(TipoRosa.AVVERSARI,
						Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO).negato()),
						OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)),
						Collections.singletonList(PredicatoMD.quanti(TipoRosa.AVVERSARI,
								Collections.singletonList(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 30)),
								TipoConfronto.MAGGIORE, 0)),
						Arrays.asList(AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_ADATTO, true), AzioneMD.di(TipoAzione.DARDO_ARCANO))),
				new VoceMD(new SelettoreMD(TipoRosa.ALLEATI,
						Collections.singletonList(FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.MINORE, 50)),
						OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)),
						Collections.singletonList(PredicatoMD.probabilita(70)),
						Arrays.asList(AzioneMD.di(TipoAzione.POZIONE_SALUTE_GRANDE), AzioneMD.di(TipoAzione.POZIONE_MAGIA_GRANDE))));
	}
}
