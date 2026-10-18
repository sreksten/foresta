package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.Collection;
import java.util.Set;

/**
 * Il giocatore chiede di vedere la schermata delle tattiche, dall'inventario; il motore la ripubblica quando cambia l'ordine del
 * gruppo o il personaggio scelto.
 */
public class RichiestaAperturaTattiche extends RichiestaConComandi {

    private final VistaPersonaggio personaggio;
    private final TatticaMD tattica;
    private final TatticaMD tatticaPredefinita;
    private final Set<TipoAzione> azioniPossibili;

    /**
     * @param personaggio il personaggio scelto
     * @param tattica     la sua tattica attuale (mai nulla: per chi non ne ha una è la vuota)
     * @param tatticaPredefinita quella a cui il giocatore può tornare con il pulsante "predefinita"
     * @param azioniPossibili    le azioni che la classe del personaggio può avere nella tattica (un elfo non canta)
     */
    public RichiestaAperturaTattiche(Collection<Comando> possibilita, VistaPersonaggio personaggio, TatticaMD tattica,
                                     TatticaMD tatticaPredefinita, Set<TipoAzione> azioniPossibili) {
        super(TipoEvento.RICHIESTA_APERTURA_TATTICHE, possibilita);
        this.personaggio = personaggio;
        this.tattica = tattica;
        this.tatticaPredefinita = tatticaPredefinita;
        this.azioniPossibili = azioniPossibili;
    }

    public VistaPersonaggio getPersonaggio() {
        return personaggio;
    }

    public TatticaMD getTattica() {
        return tattica;
    }

    public TatticaMD getTatticaPredefinita() {
        return tatticaPredefinita;
    }

    public Set<TipoAzione> getAzioniPossibili() {
        return azioniPossibili;
    }

}
