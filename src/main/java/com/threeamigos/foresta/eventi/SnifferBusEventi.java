package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Date;

/**
 *
 * @author Stefano Reksten
 */
public class SnifferBusEventi {

    public SnifferBusEventi() {
        BusEventi.iscriviti(EventoAggiuntaModificatore.class, this::onEventoAggiuntaModificatore);
        BusEventi.iscriviti(EventoApprovazioneAcquisto.class, this::onEventoApprovazioneAcquisto);
        BusEventi.iscriviti(EventoApprovazionePrelievo.class, this::onEventoApprovazionePrelievo);
        // EventoApprovazioneSpostamento è classe astratta
        BusEventi.iscriviti(EventoApprovazioneStoccaggio.class, this::onEventoApprovazioneStoccaggio);
        BusEventi.iscriviti(EventoApprovazioneVendita.class, this::onEventoApprovazioneVendita);
        // EventoBase è classe astratta
        BusEventi.iscriviti(EventoCombattimento.class, this::onEventoCombattimento);
        BusEventi.iscriviti(EventoConsumoPuntoAbilita.class, this::onEventoConsumoPuntoAbilita);
        BusEventi.iscriviti(EventoCreazionePersonaggio.class, this::onEventoCreazionePersonaggio);
        BusEventi.iscriviti(EventoFumetto.class, this::onEventoFumetto);
        BusEventi.iscriviti(EventoInterazioneElementale.class, this::onEventoInterazioneElementale);
        BusEventi.iscriviti(EventoMessaggio.class, this::onEventoMessaggio);
        BusEventi.iscriviti(EventoNotificaGlobale.class, this::onEventoNotificaGlobale);
        BusEventi.iscriviti(EventoParagrafo.class, this::onEventoParagrafo);
        // EventoPersonaggio è classe astratta
        BusEventi.iscriviti(EventoPuliziaCacheDinamicaImmagini.class, this::onEventoPuliziaCacheDinamicaImmagini);
        BusEventi.iscriviti(EventoRichiestaAcquisto.class, this::onEventoRichiestaAcquisto);
        BusEventi.iscriviti(EventoRichiestaPrelievo.class, this::onEventoRichiestaPrelievo);
        // EventoRichiestaSpostamento è classe astratta
        BusEventi.iscriviti(EventoRichiestaStoccaggio.class, this::onEventoRichiestaStoccaggio);
        BusEventi.iscriviti(EventoRichiestaVendita.class, this::onEventoRichiestaVendita);
        BusEventi.iscriviti(EventoRifiutoAcquisto.class, this::onEventoRifiutoAcquisto);
        BusEventi.iscriviti(EventoRifiutoPrelievo.class, this::onEventoRifiutoPrelievo);
        // EventoRifiutoSpostamento è classe astratta
        BusEventi.iscriviti(EventoRifiutoStoccaggio.class, this::onEventoRifiutoStoccaggio);
        BusEventi.iscriviti(EventoRifiutoVendita.class, this::onEventoRifiutoVendita);
        BusEventi.iscriviti(EventoValutazioneAttaccante.class, this::onEventoValutazioneAttaccante);
        BusEventi.iscriviti(EventoVariazioneEffettoDiStato.class, this::onEventoVariazioneEffettoDiStato);
        BusEventi.iscriviti(EventoVariazioneStatistichePersonaggio.class, this::onEventoVariazioneStatistichePersonaggio);
        BusEventi.iscriviti(EventoVariazioneStatoVitalePersonaggio.class, this::onEventoVariazioneStatoVitalePersonaggio);
    }

    private void onEventoAggiuntaModificatore(EventoAggiuntaModificatore evento) {
        ModificatoreAttributo modificatore = evento.getModificatore();
        Logger.log(headerEvento(evento) + formattaModificatoreAttributo(modificatore) +
                formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoApprovazioneAcquisto(EventoApprovazioneAcquisto evento) {
        EventoRichiestaSpostamento<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " acquista "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazionePrelievo(EventoApprovazionePrelievo evento) {
        EventoRichiestaSpostamento<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " preleva "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazioneStoccaggio(EventoApprovazioneStoccaggio evento) {
        EventoRichiestaSpostamento<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " stocca "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazioneVendita(EventoApprovazioneVendita evento) {
        EventoRichiestaSpostamento<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " vende "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoCombattimento(EventoCombattimento evento) {
        Personaggio p = evento.getPersonaggio();
        Personaggio bersaglio = evento.getBersaglio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) +
                formattaStatistichePersonaggio(bersaglio) + evento.formattaRisultatoCombattimento());
    }

    private void onEventoConsumoPuntoAbilita(EventoConsumoPuntoAbilita evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoAttributo());
    }

    private void onEventoCreazionePersonaggio(EventoCreazionePersonaggio evento) {
        Personaggio p  = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p));
    }

    private void onEventoFumetto(EventoFumetto evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getTesto()));
    }

    private void onEventoInterazioneElementale(EventoInterazioneElementale evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoInterazioneElementale());
    }

    private void onEventoMessaggio(EventoMessaggio evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoNotificaGlobale(EventoNotificaGlobale evento) {
        Logger.log(String.format("%s - %s - %s - %s - %s", new Date(), evento.getTipoEvento(), evento.getEtichetta(), evento.getEtichetta(), evento.getMessaggio()));
    }

    private void onEventoParagrafo(EventoParagrafo evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoPuliziaCacheDinamicaImmagini(EventoPuliziaCacheDinamicaImmagini evento) {
        int prima = evento.getElementiPrima();
        int dopo = evento.getElementiDopo();
        Logger.log(headerEvento(evento) + "Eliminate " + (prima - dopo) + " immagini dalla cache. Rimanenti: " + dopo);
    }

    private void onEventoRichiestaAcquisto(EventoRichiestaAcquisto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaPrelievo(EventoRichiestaPrelievo evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaStoccaggio(EventoRichiestaStoccaggio evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaVendita(EventoRichiestaVendita evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRifiutoAcquisto(EventoRifiutoAcquisto evento) {
        EventoRichiestaSpostamento<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(richiesta.getParteRemota()) + ": fondi insufficienti");
    }

    private void onEventoRifiutoPrelievo(EventoRifiutoPrelievo evento) {
        EventoRichiestaSpostamento<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(richiesta.getParteRemota()) + ": carico eccessivo");
    }

    private void onEventoRifiutoStoccaggio(EventoRifiutoStoccaggio evento) {
        EventoRichiestaSpostamento<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoRifiutoVendita(EventoRifiutoVendita evento) {
        EventoRichiestaSpostamento<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoValutazioneAttaccante(EventoValutazioneAttaccante evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getRisultatoValutazione());
    }

    private void onEventoVariazioneEffettoDiStato(EventoVariazioneEffettoDiStato evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Tipo: %s, EffettoDiStato: %s - ",
                evento.getTipo(), evento.getEffetto()) + formattaStatistichePersonaggio(p));
    }

    private void onEventoVariazioneStatistichePersonaggio(EventoVariazioneStatistichePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Attributo: %s, Variazione: %7.2f -> %7.2f - ",
                evento.getTipoAttributo(), evento.getValorePrecedente(), evento.getNuovoValore()) +
                formattaStatistichePersonaggio(p));
    }

    private void onEventoVariazioneStatoVitalePersonaggio(EventoVariazioneStatoVitalePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Stato: -> %s, Causa trapasso: -> %s - ",
                p.isVivo() ? "Vivo" : "Morto", p.getCausaTrapasso()) + formattaStatistichePersonaggio(p));
    }

    //--- Metodi generali di utilità

    private String formattaStatistichePersonaggio(Personaggio p) {
        return String.format("UUID: %s,Nome: %17s, Livello: %2d, Salute: %3d/%3d; Magia: %3d/%3d; Forza: %3d; Destrezza: %3d; Costituzione: %3d; Intelligenza: %3d; Saggezza: %3d; Carisma: %3d; Fortuna: %3d",
                p.getModelloDati().getUuid(), p.getNomeSingolare(), p.getLivello(), p.getSalute(), p.getSaluteMassima(), p.getMagia(), p.getMagiaMassima(),
                p.getForza(), p.getDestrezza(), p.getCostituzione(), p.getIntelligenza(), p.getSaggezza(), p.getCarisma(), p.getFortuna());
    }

    private String formattaModificatoreAttributo(ModificatoreAttributo modificatore) {
        return String.format("TipoAttributo: %s, TipoModificatore: %s, Quantità: %5f, Note: %s - ",
                modificatore.getTipoModificatoreAttributo(), modificatore.getTipoAttributo(), modificatore.getQuantita(), modificatore.getNote());
    }

    private String headerEvento(EventoBase evento) {
        return String.format("%s - %s - ", new Date(), evento.getTipoEvento());
    }

    private String formattaParte(ScambiatoreArtefatti parte) {
        if (parte instanceof Personaggio) {
            return ((Personaggio) parte).getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
        }
        if (parte instanceof GruppoGiocatore) {
            return "il gruppo";
        }
        return parte.getClass().getSimpleName();
    }

    private String nomeOggetto(Object oggetto) {
        return oggetto instanceof Artefatto ? ((Artefatto) oggetto).getNome() : String.valueOf(oggetto);
    }

}
