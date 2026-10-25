package com.threeamigos.foresta.poteri;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.EsitoAzione;
import com.threeamigos.foresta.tipi.TipoAzione;

/**
 * Quel che un potere chiede alla meccanica dello scontro per esercitarsi: se un colpo va a segno, quanto dura uno stato alterato, come si
 * ripartisce il danno fra più bersagli, come si applica un colpo magico, e come si racconta e si registra quel che è successo.
 * <p>
 * Sono i poteri a dire che cosa chiedono: chi fa i conti li implementa e dipende da loro, non il contrario. Chi fa partire il gioco
 * installa l'implementazione con {@link Meccanica#installa(MeccanicaDeiPoteri)}.
 *
 * @author Stefano Reksten
 */
public interface MeccanicaDeiPoteri {

    /**
     * Racconta che l'attore esercita un'azione sul bersaglio (null se ne ha più di uno o nessuno).
     */
    void annuncia(Personaggio attore, Personaggio bersaglio, TipoAzione azione);

    /**
     * Se il colpo dell'attaccante va a segno sul difensore.
     */
    boolean colpisce(Personaggio attaccante, Personaggio difensore, CategoriaDanno categoriaDanno);

    /**
     * La quota del danno che subisce il bersaglio in quella posizione (0 è il primo).
     */
    double quotaDelDannoPerPosizione(int posizione);

    /**
     * Quanti turni dura lo stato alterato inflitto a quel bersaglio.
     */
    int calcolaDurataStato(Personaggio difensore, TipoEffettoDiStato stato);

    /**
     * Colpisce il bersaglio con un attacco magico: se è immune non succede niente, altrimenti si tira per colpire e, se va a segno, il danno
     * risultante si applica. L'esito si registra per le tattiche.
     *
     * @param arma                l'attacco magico, per calcolare il danno
     * @param azione              l'azione con cui si registra l'esito
     * @param immunitaIncantesimo il tipo di incantesimo di cui il bersaglio può essere immune, o null se l'attacco non ha immunità
     * @param quotaDelDanno       la quota del danno che il bersaglio subisce
     */
    void colpisciConMagia(Personaggio formulante, Personaggio bersaglio, Arma arma, TipoAzione azione,
                          TipoIncantesimo immunitaIncantesimo, double quotaDelDanno);

    /**
     * Registra come è andata un'azione contro quel bersaglio.
     */
    void registraEsitoAzione(Personaggio attore, Personaggio bersaglio, TipoAzione azione, EsitoAzione esito);

    /**
     * Registra che quel personaggio ha lanciato un incantesimo nello scontro.
     */
    void registraIncantesimoLanciato(Personaggio personaggio);

    /**
     * Registra che il personaggio ha aiutato l'alleato.
     */
    void registraAlleato(Personaggio personaggio, Personaggio alleato);
}
