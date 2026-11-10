package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.ColpoMagico;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.CategoriaIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoAzione;

import java.util.ArrayList;
import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/*
 * Incantesimi originali su ZX Spectrum:
 * Sonno
 * Lievitazione
 * Invisibilità
 */

/**
 * Il lancio di un incantesimo malefico, per chiunque lo formuli (gruppo del giocatore o mostri). Su ogni bersaglio
 * tiro per colpire e danno si calcolano con CalcolatoreCombattimento, come per le armi.
 */
public abstract class IncantesimoMaleficoImpl implements IncantesimoMalefico {

	protected final int livello;

	protected IncantesimoMaleficoImpl(int livello) {
		this.livello = livello;
	}

	public int getLivello() {
		return livello;
	}

	public CategoriaIncantesimo getTipo() {
		return CategoriaIncantesimo.MALEFICO;
	}

	/**
	 * Un incantesimo GLOBALE agisce su tutta la locazione (vedi {@link #formulaGlobale}); altrimenti con un bersaglio colpisce solo lui (è il caso
	 * dei mostri, che lanciano sempre su un personaggio), e con un gruppo colpisce tutti i vivi (GRUPPO) o fino al
	 * numero di bersagli di chi lo formula (MULTIPLO). Con più bersagli il danno si ripartisce per posizione: il primo lo subisce
	 * intero, gli altri una quota minore (vedi CalcolatoreCombattimento.quotaDelDannoPerPosizione).
	 * <p>
	 * Chi formula paga il costo per ogni bersaglio colpito; se la sua Magia non basta per tutti, colpisce solo i primi che può pagare.
	 */
	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		int pagati;
		if (getClasse().getPortata() == PortataIncantesimo.GLOBALE) {
			List<Personaggio> bersagli = soloIPagabili(formulante, bersagliGlobali(formulante, gruppoBersaglio));
			formulaGlobale(formulante, bersagli);
			pagati = bersagli.size();
		} else if (personaggioBersaglio != null) {
			annuncia(formulante, personaggioBersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			colpisci(formulante, personaggioBersaglio);
			pagati = 1;
		} else if (gruppoBersaglio != null) {
			List<Personaggio> bersagli = gruppoBersaglio.getPersonaggiVivi();
			if (getClasse().getPortata() == PortataIncantesimo.MULTIPLO && bersagli.size() > formulante.getBersagli()) {
				bersagli = bersagli.subList(0, formulante.getBersagli());
			}
			bersagli = soloIPagabili(formulante, bersagli);
			if (bersagli.isEmpty()) {
				// Non dovrebbe succedere: chi sceglie l'incantesimo controlla che si possa pagare almeno un bersaglio
				Logger.log("Incantesimo senza bersagli che si possano pagare: non succede niente.");
				return;
			}
			annuncia(formulante, bersagli.size() > 1 ? Testi.get("messaggi_poteri", "IncantesimoMaleficoImpl.ilGruppo").orElse("") :
					bersagli.get(0).getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			boolean ripartito = getClasse().getPortata() == PortataIncantesimo.MULTIPLO;
			for (int posizione = 0; posizione < bersagli.size(); posizione++) {
				if (ripartito) {
					colpisci(formulante, bersagli.get(posizione), CalcolatoreCombattimento.quotaDelDannoPerPosizione(posizione));
				} else {
					colpisci(formulante, bersagli.get(posizione));
				}
			}
			pagati = bersagli.size();
		} else {
			throw new IllegalArgumentException("Non so come formulare questo incantesimo!");
		}
		paga(formulante, pagati);
		StatoDelCombattimento.getIstanza().registraIncantesimoLanciato(formulante);
	}

	/**
	 * Chi tocca un incantesimo GLOBALE, prima di guardare quanti se ne possono pagare: gli avversari vivi (la Tempesta ci aggiunge i compagni
	 * di chi la evoca). Un elenco nuovo, fatto all'inizio: chi cade durante l'incantesimo non conta più.
	 *
	 * @param gruppoAvversario il gruppo avversario di chi formula (chi lo lancia non lo sceglie: è la locazione); può essere null
	 */
	protected List<Personaggio> bersagliGlobali(Personaggio formulante, Gruppo gruppoAvversario) {
		return gruppoAvversario == null ? new ArrayList<>() : new ArrayList<>(gruppoAvversario.getPersonaggiVivi());
	}

	/**
	 * L'effetto di un incantesimo GLOBALE: nessun bersaglio scelto, agisce su tutta la locazione. Chi lo è ridefinisce questo metodo; il costo in
	 * Magia lo toglie {@link #formula}, per i bersagli che riceve qui.
	 *
	 * @param bersagli chi tocca (vedi {@link #bersagliGlobali}), già ridotto a quanti chi formula può pagare
	 */
	protected void formulaGlobale(Personaggio formulante, List<Personaggio> bersagli) {
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "IncantesimoMaleficoImpl.1", formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), getClasse().getNomeSingolare())));
	}

	private void annuncia(Personaggio formulante, String nomeBersaglio) {
		BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "IncantesimoMaleficoImpl.2", formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA), getClasse().getNomeSingolare(), nomeBersaglio)));
	}

	/**
	 * Lancia l'incantesimo su un bersaglio: se è immune non succede niente, altrimenti si tira per colpire come per le armi e, se va a segno, il
	 * danno risultante si applica al bersaglio. L'esito viene registrato nello stato del combattimento.
	 */
	protected void colpisci(Personaggio formulante, Personaggio personaggioBersaglio) {
		colpisci(formulante, personaggioBersaglio, 1.0d);
	}

	/**
	 * Come {@link #colpisci(Personaggio, Personaggio)}, ma il bersaglio subisce solo quella quota del danno (vedi
	 * CalcolatoreCombattimento.quotaDelDannoPerPosizione): gli stati alterati dipendono dal colpo, non dalla quota.
	 */
	protected void colpisci(Personaggio formulante, Personaggio personaggioBersaglio, double quotaDelDanno) {
		ColpoMagico.colpisci(formulante, personaggioBersaglio, this, TipoAzione.di(getClasse()), getClasse(), quotaDelDanno);
	}
}
