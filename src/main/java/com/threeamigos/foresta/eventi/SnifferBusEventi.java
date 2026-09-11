package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Date;

/**
 *
 * @author Stefano Reksten
 */
public class SnifferBusEventi {

    public SnifferBusEventi() {
        BusEventi.iscriviti(EventoAggiuntaModificatore.class, this::onEventoAggiuntaModificatore);
        BusEventi.iscriviti(EventoCombattimento.class, this::onEventoCombattimento);
        BusEventi.iscriviti(EventoConsumoPuntoAbilita.class, this::onEventoConsumoPuntoAbilita);
        BusEventi.iscriviti(EventoCreazionePersonaggio.class, this::onEventoCreazionePersonaggio);
        BusEventi.iscriviti(EventoInterazioneElementale.class, this::onEventoInterazioneElementale);
        BusEventi.iscriviti(EventoMessaggio.class, this::onEventoMessaggio);
        BusEventi.iscriviti(EventoValutazioneAttaccante.class, this::onEventoValutazione);
        BusEventi.iscriviti(EventoVariazioneEffettoDiStato.class, this::onEventoVariazioneEffettoDiStato);
        BusEventi.iscriviti(EventoVariazioneStatistichePersonaggio.class, this::onEventoVariazioneStatistichePersonaggio);
        BusEventi.iscriviti(EventoVariazioneStatoVitalePersonaggio.class, this::onEventoVariazioneStatoVitalePersonaggio);
    }

    private void onEventoAggiuntaModificatore(EventoAggiuntaModificatore evento) {
        ModificatoreAttributo modificatore = evento.getModificatore();
        Logger.log(headerEvento(evento) + formattaModificatoreAttributo(modificatore) +
                formattaStatistichePersonaggio(evento.getPersonaggio()));
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

    private void onEventoInterazioneElementale(EventoInterazioneElementale evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoInterazioneElementale());
    }

    private void onEventoMessaggio(EventoMessaggio evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoValutazione(EventoValutazioneAttaccante evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getRisultatoValutazione());
    }

    private void onEventoVariazioneStatoVitalePersonaggio(EventoVariazioneStatoVitalePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Stato: -> %s, Causa trapasso: -> %s - ",
                p.isVivo() ? "Vivo" : "Morto", p.getCausaTrapasso()) + formattaStatistichePersonaggio(p));
    }

    private void onEventoVariazioneStatistichePersonaggio(EventoVariazioneStatistichePersonaggio evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Attributo: %s, Variazione: %7.2f -> %7.2f - ",
                evento.getTipoAttributo(), evento.getValorePrecedente(), evento.getNuovoValore()) +
                formattaStatistichePersonaggio(p));
    }

    private void onEventoVariazioneEffettoDiStato(EventoVariazioneEffettoDiStato evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + String.format("Tipo: %s, EffettoDiStato: %s - ",
                evento.getTipo(), evento.getEffetto()) + formattaStatistichePersonaggio(p));
    }

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

}
