package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.poteri.Meccanica;
import com.threeamigos.foresta.poteri.MeccanicaDeiPoteri;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.CategoriaIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoAzione;

import java.util.List;

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
	 * Un incantesimo GLOBALE agisce su tutta la locazione; altrimenti con un bersaglio colpisce solo lui (è il caso
	 * dei mostri, che lanciano sempre su un personaggio), e con un gruppo colpisce tutti i vivi (GRUPPO) o fino al
	 * numero di bersagli di chi lo formula (MULTIPLO). Con più bersagli il danno si ripartisce per posizione: il primo lo subisce
	 * intero, gli altri una quota minore (vedi CalcolatoreCombattimento.quotaDelDannoPerPosizione).
	 */
	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		if (getClasse().getPortata() == PortataIncantesimo.GLOBALE) {
			formulaGlobale(formulante);
		} else if (personaggioBersaglio != null) {
			annuncia(formulante, personaggioBersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			colpisci(formulante, personaggioBersaglio);
		} else if (gruppoBersaglio != null) {
			List<Personaggio> bersagli = gruppoBersaglio.getPersonaggiVivi();
			if (getClasse().getPortata() == PortataIncantesimo.MULTIPLO && bersagli.size() > formulante.getBersagli()) {
				bersagli = bersagli.subList(0, formulante.getBersagli());
			}
			annuncia(formulante, bersagli.size() > 1 ? "il gruppo" :
					bersagli.get(0).getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE));
			boolean ripartito = getClasse().getPortata() == PortataIncantesimo.MULTIPLO;
			for (int posizione = 0; posizione < bersagli.size(); posizione++) {
				if (ripartito) {
					colpisci(formulante, bersagli.get(posizione), Meccanica.get().quotaDelDannoPerPosizione(posizione));
				} else {
					colpisci(formulante, bersagli.get(posizione));
				}
			}
		} else {
			throw new IllegalArgumentException("Non so come formulare questo incantesimo!");
		}
		formulante.subMagia(getCostoLancio());
		Meccanica.get().registraIncantesimoLanciato(formulante);
	}

	/**
	 * L'effetto di un incantesimo GLOBALE. Per ora nessuno lo è: chi lo sarà ridefinirà questo metodo.
	 */
	protected void formulaGlobale(Personaggio formulante) {
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " formula un " + getClasse().getNomeSingolare() + "."));
	}

	private void annuncia(Personaggio formulante, String nomeBersaglio) {
		BusEventi.pubblica(new NotificaTestoFrase(formulante.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " formula un " + getClasse().getNomeSingolare() + " contro " +
				nomeBersaglio + "."));
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
		Meccanica.get().colpisciConMagia(formulante, personaggioBersaglio, this, TipoAzione.di(getClasse()), getClasse(), quotaDelDanno);
	}
}
