package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoSuggerimento;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Il motore chiede alla UI di dire una cosa con la finestra del tutorial, perché non sfugga a chi non legge il testo che scorre: per esempio che «non è ancora
 * il momento di visitare un tempio». Non è un suggerimento che scatta da una situazione: il motore sceglie quale (un {@link TipoSuggerimento} il cui testo ha
 * dei segnaposto {0}, {1}...) e con quali valori, e la UI lo mostra subito, qualunque cosa sia già stata vista. Se il suggerimento ha un innesco suo
 * (non {@code MESSAGGIO_DEL_MOTORE}, per esempio il recupero fra le locazioni) il motore dice solo che è successo: la UI lo dà al suo turno, nell'ordine dei
 * suggerimenti.
 */
public class NotificaMessaggioDelTutorial extends EventoBase {

    private final TipoSuggerimento suggerimento;
    private final List<String> parametri;

    public NotificaMessaggioDelTutorial(TipoSuggerimento suggerimento, String... parametri) {
        super(TipoEvento.NOTIFICA_MESSAGGIO_DEL_TUTORIAL);
        this.suggerimento = suggerimento;
        this.parametri = Collections.unmodifiableList(Arrays.asList(parametri));
    }

    public TipoSuggerimento getSuggerimento() {
        return suggerimento;
    }

    public List<String> getParametri() {
        return parametri;
    }
}
