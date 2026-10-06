package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoConsumabile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
	static Optional<OffertaConsumabile> trova(GruppoGiocatore gruppo, TipoConsumabile tipo, ClasseIncantesimo classe,
											  String uuidPersonaggio) {
		for (OffertaConsumabile offerta : costruisci(gruppo)) {
			if (offerta.getTipo() != tipo) {
				continue;
			}
			if (tipo == TipoConsumabile.INCANTESIMO && offerta.getClasseIncantesimo() != classe) {
				continue;
			}
			if (tipo == TipoConsumabile.AUMENTO_MAGIA_SINGOLO
					&& (offerta.getPersonaggio() == null || !offerta.getPersonaggio().getUuid().equals(uuidPersonaggio))) {
				continue;
			}
			return Optional.of(offerta);
		}
		return Optional.empty();
	}

	private static List<OffertaConsumabile> costruisci(GruppoGiocatore gruppo) {
		List<OffertaConsumabile> offerte = new ArrayList<>();
		for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
			String nome = classe.getNomeSingolare();
			offerte.add(new OffertaConsumabile(TipoConsumabile.INCANTESIMO, classe, null,
					nome.substring(0, 1).toUpperCase() + nome.substring(1), classe.getEffetto(),
					FabbricaIncantesimi.costoAcquisto(classe)));
		}
		offerte.add(semplice(TipoConsumabile.POZIONE_SALUTE, "Pozione della Salute",
				"Fa riacquistare punti di Salute", Costanti.COSTO_POZIONE_SALUTE));
		offerte.add(semplice(TipoConsumabile.POZIONE_SALUTE_GRANDE, "Pozione della Salute (grande)",
				"Fa riacquistare punti di Salute e ne aumenta il livello massimo", Costanti.COSTO_POZIONE_SALUTE_GRANDE));
		offerte.add(semplice(TipoConsumabile.POZIONE_MAGIA, "Pozione della Magia",
				"Fa riacquistare punti di Magia", Costanti.COSTO_POZIONE_MAGIA));
		offerte.add(semplice(TipoConsumabile.POZIONE_MAGIA_GRANDE, "Pozione della Magia (grande)",
				"Fa riacquistare punti di Magia e ne aumenta il livello massimo", Costanti.COSTO_POZIONE_MAGIA_GRANDE));
		int giocanti = 0;
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			if (!personaggio.isPNG()) {
				giocanti++;
				offerte.add(new OffertaConsumabile(TipoConsumabile.AUMENTO_MAGIA_SINGOLO, null, personaggio,
						"Aumento Magia massima", "Aumenta la magia massima di " + personaggio.getNome(),
						Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO));
			}
		}
		if (giocanti > 1) {
			// Il secondo e i successivi costano il 75% del primo
			long costo = Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO
					+ (giocanti - 1) * Costanti.COSTO_AUMENTO_MAGIA_GIOCATORE_SINGOLO * 75 / 100;
			offerte.add(semplice(TipoConsumabile.AUMENTO_MAGIA_GRUPPO, "Aumento Magia massima",
					"Aumenta la magia massima di tutto il gruppo", (int) costo));
		}
		offerte.add(semplice(TipoConsumabile.MAPPA_PARZIALE_FORESTA, "Mappa della zona",
				"Una mappa della zona circostante", Costanti.COSTO_MAPPA_DELLA_ZONA));
		offerte.add(semplice(TipoConsumabile.MAPPA_COMPLETA_FORESTA, "Mappa della Foresta",
				"Una mappa completa della Foresta", Costanti.COSTO_MAPPA_DELLA_FORESTA));
		return Collections.unmodifiableList(offerte);
	}

	private static OffertaConsumabile semplice(TipoConsumabile tipo, String nome, String descrizione, int costo) {
		return new OffertaConsumabile(tipo, null, null, nome, descrizione, costo);
	}
}
