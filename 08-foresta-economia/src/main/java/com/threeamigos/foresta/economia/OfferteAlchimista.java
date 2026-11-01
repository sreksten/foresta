package com.threeamigos.foresta.economia;

import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.economia.OffertaConsumabile;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoConsumabile;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.*;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Testi;

/**
 * Il listino dell'alchimista: nomi, descrizioni e costi di ciò che vende al gruppo. Lo costruisce il motore, che
 * decide anche il prezzo di un acquisto (la UI mostra il listino e chiede un'offerta, senza dire quanto costa).
 */
public final class OfferteAlchimista {

	private OfferteAlchimista() {
	}

	/**
	 * Il listino per il gruppo: gli incantesimi, le pozioni, un aumento di magia per ogni personaggio vivo che non
	 * sia un PNG (e uno per tutto il gruppo, scontato, se sono più di uno) e le mappe.
	 */
	public static List<? extends VistaOffertaConsumabile> elenco(GruppoGiocatore gruppo) {
		return costruisci(gruppo);
	}

	/**
	 * L'offerta a cui corrisponde una richiesta di acquisto, se c'è: per gli incantesimi conta la classe, per
	 * l'aumento di magia di un personaggio il suo uuid.
	 */
	static Optional<OffertaConsumabile> trova(GruppoGiocatore gruppo, TipoConsumabile tipo, TipoIncantesimo classe,
											  String uuidPersonaggio) {
		for (OffertaConsumabile offerta : costruisci(gruppo)) {
			if (offerta.getTipo() != tipo) {
				continue;
			}
			if (tipo == TipoConsumabile.INCANTESIMO && offerta.getClasseIncantesimo() != classe) {
				continue;
			}
			if (tipo == TipoConsumabile.AUMENTO_MAGIA_SINGOLO && !offerta.getPersonaggio().getUuid().equals(uuidPersonaggio)) {
				continue;
			}
			return Optional.of(offerta);
		}
		return Optional.empty();
	}

	private static List<OffertaConsumabile> costruisci(GruppoGiocatore gruppo) {
		List<OffertaConsumabile> offerte = new ArrayList<>();
		for (TipoIncantesimo classe : TipoIncantesimo.values()) {
			offerte.add(new OffertaConsumabile(TipoConsumabile.INCANTESIMO, classe, null,
					Lingue.corrente().inizialeMaiuscola(classe.getNomeSingolare()), classe.getEffetto(),
					classe.getCostoAcquisto()));
		}
		offerte.add(semplice(TipoConsumabile.POZIONE_SALUTE, CostantiEconomia.COSTO_POZIONE_SALUTE));
		offerte.add(semplice(TipoConsumabile.POZIONE_SALUTE_GRANDE, CostantiEconomia.COSTO_POZIONE_SALUTE_GRANDE));
		offerte.add(semplice(TipoConsumabile.POZIONE_MAGIA, CostantiEconomia.COSTO_POZIONE_MAGIA));
		offerte.add(semplice(TipoConsumabile.POZIONE_MAGIA_GRANDE, CostantiEconomia.COSTO_POZIONE_MAGIA_GRANDE));
		offerte.add(semplice(TipoConsumabile.POZIONE_PANACEA, CostantiEconomia.COSTO_POZIONE_PANACEA));
		int giocanti = 0;
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (!personaggio.isPNG()) {
				giocanti++;
				offerte.add(new OffertaConsumabile(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, personaggio,
						Testi.testoOCostante(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, "NOME"),
						Testi.sostituisci(Testi.testoOCostante(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, "DESCRIZIONE"), personaggio.getNome()),
						CostantiEconomia.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO));
			}
		}
		if (giocanti > 1) {
			// Il secondo e i successivi costano il 75% del primo
			long costo = CostantiEconomia.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO
					+ (long) (giocanti - 1) * CostantiEconomia.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO * 75 / 100;
			offerte.add(semplice(TipoConsumabile.AUMENTO_MAGIA_GRUPPO, (int) costo));
		}
		offerte.add(semplice(TipoConsumabile.MAPPA_PARZIALE_FORESTA, CostantiEconomia.COSTO_MAPPA_DELLA_ZONA));
		offerte.add(semplice(TipoConsumabile.MAPPA_COMPLETA_FORESTA, CostantiEconomia.COSTO_MAPPA_DELLA_FORESTA));
		return Collections.unmodifiableList(offerte);
	}

	// Nome e descrizione vengono dai testi di TipoConsumabile (tipo_consumabile_<lingua>.properties)
	private static OffertaConsumabile semplice(TipoConsumabile tipo, int costo) {
		return new OffertaConsumabile(tipo, null, null, Testi.testoOCostante(tipo, "NOME"), Testi.testoOCostante(tipo, "DESCRIZIONE"), costo);
	}
}
