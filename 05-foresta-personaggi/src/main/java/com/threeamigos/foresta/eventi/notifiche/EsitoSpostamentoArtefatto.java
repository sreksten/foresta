package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Quel che hanno in comune l'approvazione e il rifiuto di una richiesta di spostamento di un artefatto (Personaggio &lt;-&gt;
 * GruppoGiocatore, GruppoGiocatore &lt;-&gt; commerciante): lo scambio da cui veniva, l'oggetto e l'identificativo della richiesta.
 * Le due famiglie restano separate ({@link NotificaApprovazioneSpostamentoArtefatto}, {@link NotificaRifiutoSpostamentoArtefatto})
 * perché chi ascolta il bus si iscrive a una classe precisa.
 *
 * @param <T> il tipo dell'oggetto spostato (un artefatto o un consumabile)
 * @author Stefano Reksten
 */
public abstract class EsitoSpostamentoArtefatto<T> extends EventoBase {

    private final VistaScambio scambio;
    private final T oggettoSpostato;
    private final String uuidRichiesta;

    /**
     * @param scambio lo scambio aperto da cui veniva la richiesta, o null
     * @param oggettoSpostato l'oggetto di cui si chiedeva lo spostamento
     * @param uuidRichiesta l'identificativo della richiesta a cui si risponde
     */
    protected EsitoSpostamentoArtefatto(TipoEvento tipoEvento, VistaScambio scambio, T oggettoSpostato, String uuidRichiesta) {
        super(tipoEvento);
        this.scambio = scambio;
        this.oggettoSpostato = oggettoSpostato;
        this.uuidRichiesta = uuidRichiesta;
    }

    /**
     * @return lo scambio aperto da cui veniva la richiesta, o null: le schermate rispondono solo alle notifiche del
     * proprio (la richiesta stessa è interna al motore)
     */
    public VistaScambio getScambio() {
        return scambio;
    }

    /**
     * @return l'oggetto di cui si chiedeva lo spostamento
     */
    public T getOggettoSpostato() {
        return oggettoSpostato;
    }

    /**
     * @return l'identificativo della richiesta a cui si risponde
     */
    public String getUuidRichiesta() {
        return uuidRichiesta;
    }
}
