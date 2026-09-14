package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.*;
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
        BusEventi.iscriviti(EventoApprovazioneAcquistoArtefatto.class, this::onEventoApprovazioneAcquisto);
        BusEventi.iscriviti(EventoApprovazionePrelievoArtefatto.class, this::onEventoApprovazionePrelievo);
        // EventoApprovazioneSpostamento è classe astratta
        BusEventi.iscriviti(EventoApprovazioneStoccaggioArtefatto.class, this::onEventoApprovazioneStoccaggio);
        BusEventi.iscriviti(EventoApprovazioneVenditaArtefatto.class, this::onEventoApprovazioneVendita);
        // EventoBase è classe astratta
        BusEventi.iscriviti(EventoCombattimento.class, this::onEventoCombattimento);
        BusEventi.iscriviti(EventoConsumoPuntoAbilita.class, this::onEventoConsumoPuntoAbilita);
        BusEventi.iscriviti(EventoCreazionePersonaggio.class, this::onEventoCreazionePersonaggio);
        BusEventi.iscriviti(EventoErroreInterno.class, this::onEventoErroreInterno);
        BusEventi.iscriviti(EventoException.class, this::onEventoException);
        BusEventi.iscriviti(EventoFumetto.class, this::onEventoFumetto);
        BusEventi.iscriviti(EventoInterazioneElementale.class, this::onEventoInterazioneElementale);
        BusEventi.iscriviti(EventoMessaggio.class, this::onEventoMessaggio);
        BusEventi.iscriviti(EventoMessaggioInterno.class, this::onEventoMessaggioInterno);
        BusEventi.iscriviti(EventoNotificaGlobale.class, this::onEventoNotificaGlobale);
        BusEventi.iscriviti(EventoParagrafo.class, this::onEventoParagrafo);
        // EventoPersonaggio è classe astratta
        BusEventi.iscriviti(EventoPuliziaCacheDinamicaImmagini.class, this::onEventoPuliziaCacheDinamicaImmagini);
        BusEventi.iscriviti(EventoRichiestaAcquistoArtefatto.class, this::onEventoRichiestaAcquisto);
        BusEventi.iscriviti(EventoRichiestaPrelievoArtefatto.class, this::onEventoRichiestaPrelievo);
        // EventoRichiestaSpostamento è classe astratta
        BusEventi.iscriviti(EventoRichiestaStoccaggioArtefatto.class, this::onEventoRichiestaStoccaggio);
        BusEventi.iscriviti(EventoRichiestaTesto.class, this::onEventoRichiestaTesto);
        BusEventi.iscriviti(EventoRichiestaVenditaArtefatto.class, this::onEventoRichiestaVendita);
        BusEventi.iscriviti(EventoRifiutoAcquistoArtefatto.class, this::onEventoRifiutoAcquisto);
        BusEventi.iscriviti(EventoRifiutoPrelievoArtefatto.class, this::onEventoRifiutoPrelievo);
        // EventoRifiutoSpostamento è classe astratta
        BusEventi.iscriviti(EventoRifiutoStoccaggioArtefatto.class, this::onEventoRifiutoStoccaggio);
        BusEventi.iscriviti(EventoRifiutoVenditaArtefatto.class, this::onEventoRifiutoVendita);
        BusEventi.iscriviti(EventoStatoDiGioco.class, this::onEventoStatoDiGioco);
        BusEventi.iscriviti(EventoTestoDisponibile.class, this::onEventoTestoDisponibile);
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

    private void onEventoApprovazioneAcquisto(EventoApprovazioneAcquistoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " acquista "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazionePrelievo(EventoApprovazionePrelievoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " preleva "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazioneStoccaggio(EventoApprovazioneStoccaggioArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " stocca "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoApprovazioneVendita(EventoApprovazioneVenditaArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamentoArtefatto();
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

    private void onEventoErroreInterno(EventoErroreInterno evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoException(EventoException evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
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

    private void onEventoMessaggioInterno(EventoMessaggioInterno evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggioInterno()));
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

    private void onEventoRichiestaAcquisto(EventoRichiestaAcquistoArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaPrelievo(EventoRichiestaPrelievoArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaStoccaggio(EventoRichiestaStoccaggioArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaTesto(EventoRichiestaTesto evento) {
        Logger.log(headerEvento(evento) + "Richiesta: " + evento.getRichiesta());
    }

    private void onEventoRichiestaVendita(EventoRichiestaVenditaArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRifiutoAcquisto(EventoRifiutoAcquistoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(richiesta.getParteRemota()) + ": fondi insufficienti");
    }

    private void onEventoRifiutoPrelievo(EventoRifiutoPrelievoArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(richiesta.getParteRemota()) + ": carico eccessivo");
    }

    private void onEventoRifiutoStoccaggio(EventoRifiutoStoccaggioArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConPeso> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConPeso oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoRifiutoVendita(EventoRifiutoVenditaArtefatto evento) {
        EventoRichiestaSpostamentoArtefatto<OggettoConCosto> richiesta = evento.getEventoRichiestaSpostamento();
        OggettoConCosto oggetto = richiesta.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(richiesta.getParteAttiva()) + " non può vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(richiesta.getParteRemota()));
    }

    private void onEventoStatoDiGioco(EventoStatoDiGioco evento) {
        Logger.log(headerEvento(evento) + evento.getStato().toString());
    }

    private void onEventoTestoDisponibile(EventoTestoDisponibile evento) {
        Logger.log(headerEvento(evento) + "Ricevuto testo: " + evento.getTesto());
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
