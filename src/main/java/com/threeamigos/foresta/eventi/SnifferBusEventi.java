package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import java.util.Date;
import java.util.stream.Collectors;

/**
 *
 * @author Stefano Reksten
 */
public class SnifferBusEventi {

    public SnifferBusEventi() {
        BusEventi.iscriviti(EventoAggiuntaModificatore.class, this::onEventoAggiuntaModificatore);
        BusEventi.iscriviti(EventoApprovazioneAcquistoArtefatto.class, this::onEventoApprovazioneAcquistoArtefatto);
        BusEventi.iscriviti(EventoApprovazioneAcquistoConsumabile.class, this::onEventoApprovazioneAcquistoConsumabile);
        BusEventi.iscriviti(EventoApprovazionePrelievoArtefatto.class, this::onEventoApprovazionePrelievoArtefatto);
        // EventoApprovazioneSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(EventoApprovazioneStoccaggioArtefatto.class, this::onEventoApprovazioneStoccaggioArtefatto);
        BusEventi.iscriviti(EventoApprovazioneVenditaArtefatto.class, this::onEventoApprovazioneVenditaArtefatto);
        BusEventi.iscriviti(EventoAumentoLivelloMondo.class, this::onEventoAumentoLivelloMondo);
        BusEventi.iscriviti(EventoAumentoLivelloPersonaggio.class, this::onEventoAumentoLivelloPersonaggio);
        // EventoBase è classe astratta
        BusEventi.iscriviti(EventoComandiDisponibili.class, this::onEventoComandiDisponibili);
        BusEventi.iscriviti(EventoComandoDiGioco.class, this::onEventoComandoDiGioco);
        BusEventi.iscriviti(EventoCombattimento.class, this::onEventoCombattimento);
        BusEventi.iscriviti(EventoConsumoPuntoAbilita.class, this::onEventoConsumoPuntoAbilita);
        BusEventi.iscriviti(EventoCreazionePersonaggio.class, this::onEventoCreazionePersonaggio);
        BusEventi.iscriviti(EventoCreazioneSpriteAnnuncioGlobale.class, this::onEventoCreazioneSpriteAnnuncioGlobale);
        BusEventi.iscriviti(EventoCreazioneSpriteATempo.class, this::onEventoCreazioneSpriteATempo);
        BusEventi.iscriviti(EventoCreazioneSpriteEffetto.class, this::onEventoCreazioneSpriteEffetto);
        BusEventi.iscriviti(EventoCreazioneSpriteFumetto.class, this::onEventoCreazioneSpriteFumetto);
        BusEventi.iscriviti(EventoCreazioneSpriteInDissolvenza.class, this::onEventoCreazioneSpriteInDissolvenza);
        BusEventi.iscriviti(EventoErroreCaricamento.class, this::onEventoErroreCaricamento);
        BusEventi.iscriviti(EventoErroreInterno.class, this::onEventoErroreInterno);
        BusEventi.iscriviti(EventoException.class, this::onEventoException);
        BusEventi.iscriviti(EventoFineGioco.class, this::onEventoFineGioco);
        BusEventi.iscriviti(EventoFumetto.class, this::onEventoFumetto);
        BusEventi.iscriviti(EventoInterazioneElementale.class, this::onEventoInterazioneElementale);
        BusEventi.iscriviti(EventoInterfacciaUtentePronta.class, this::onEventoInterfacciaUtentePronta);
        BusEventi.iscriviti(EventoMessaggio.class, this::onEventoMessaggio);
        BusEventi.iscriviti(EventoMessaggioInterno.class, this::onEventoMessaggioInterno);
        BusEventi.iscriviti(EventoMostraFinestra.class, this::onEventoMostraFinestra);
        BusEventi.iscriviti(EventoMostraPunteggi.class, this::onEventoMostraPunteggi);
        BusEventi.iscriviti(EventoMostraSchermataGioco.class, this::onEventoMostraSchermataGioco);
        BusEventi.iscriviti(EventoMostraStatistiche.class, this::onEventoMostraStatistiche);
        BusEventi.iscriviti(EventoNotificaGlobale.class, this::onEventoNotificaGlobale);
        BusEventi.iscriviti(EventoParagrafo.class, this::onEventoParagrafo);
        // EventoPersonaggio è classe astratta
        BusEventi.iscriviti(EventoPreparazioneLocazione.class, this::onEventoPreparazioneLocazione);
        BusEventi.iscriviti(EventoPuliziaCacheDinamicaImmagini.class, this::onEventoPuliziaCacheDinamicaImmagini);
        BusEventi.iscriviti(EventoRaccoltaOggetti.class, this::onEventoRaccoltaOggetti);
        BusEventi.iscriviti(EventoRichiestaAcquistoArtefatto.class, this::onEventoRichiestaAcquistoArtefatto);
        BusEventi.iscriviti(EventoRichiestaAcquistoConsumabile.class, this::onEventoRichiestaAcquistoConsumabile);
        BusEventi.iscriviti(EventoRichiestaAperturaFinestraCombattimento.class, this::onEventoRichiestaAperturaFinestraCombattimento);
        BusEventi.iscriviti(EventoRichiestaAperturaInventarioCommerciante.class, this::onEventoRichiestaAperturaInventarioCommerciante);
        BusEventi.iscriviti(EventoRichiestaAperturaInventarioFornitore.class, this::onEventoRichiestaAperturaInventarioFornitore);
        BusEventi.iscriviti(EventoRichiestaAperturaInventarioGruppo.class, this::onEventoRichiestaAperturaInventarioGruppo);
        BusEventi.iscriviti(EventoRichiestaChiusuraFinestraCombattimento.class, this::onEventoRichiestaChiusuraFinestraCombattimento);
        BusEventi.iscriviti(EventoRichiestaConfermaUscita.class, this::onEventoRichiestaConfermaUscita);
        BusEventi.iscriviti(EventoRichiestaPrelievoArtefatto.class, this::onEventoRichiestaPrelievoArtefatto);
        BusEventi.iscriviti(EventoRichiestaRefreshUI.class, this::onEventoRichiestaRefreshUI);
        BusEventi.iscriviti(EventoRichiestaReinizializzazioneUI.class, this::onEventoRichiestaReinizializzazioneUI);
        // EventoRichiestaSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(EventoRichiestaSelezioneSlotPerRilettura.class, this::onEventoRichiestaSelezioneSlotPerRilettura);
        BusEventi.iscriviti(EventoRichiestaSelezioneSlotPerSalvataggio.class, this::onEventoRichiestaSelezioneSlotPerSalvataggio);
        BusEventi.iscriviti(EventoRichiestaStoccaggioArtefatto.class, this::onEventoRichiestaStoccaggioArtefatto);
        BusEventi.iscriviti(EventoRichiestaTesto.class, this::onEventoRichiestaTesto);
        BusEventi.iscriviti(EventoRichiestaVenditaArtefatto.class, this::onEventoRichiestaVenditaArtefatto);
        BusEventi.iscriviti(EventoRichiestaVenditaArtefatto.class, this::onEventoRichiestaVenditaArtefatto);
        BusEventi.iscriviti(EventoRichiestaVisualizzazioneMappa.class, this::onEventoRichiestaVisualizzazioneMappa);
        BusEventi.iscriviti(EventoRifiutoAcquistoArtefatto.class, this::onEventoRifiutoAcquistoArtefatto);
        BusEventi.iscriviti(EventoRifiutoAcquistoConsumabile.class, this::onEventoRifiutoAcquistoConsumabile);
        BusEventi.iscriviti(EventoRifiutoPrelievoArtefatto.class, this::onEventoRifiutoPrelievoArtefatto);
        // EventoRifiutoSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(EventoRifiutoStoccaggioArtefatto.class, this::onEventoRifiutoStoccaggioArtefatto);
        BusEventi.iscriviti(EventoRifiutoVenditaArtefatto.class, this::onEventoRifiutoVenditaArtefatto);
        BusEventi.iscriviti(EventoStatoDiGioco.class, this::onEventoStatoDiGioco);
        BusEventi.iscriviti(EventoTestoDisponibile.class, this::onEventoTestoDisponibile);
        BusEventi.iscriviti(EventoValutazioneAttaccante.class, this::onEventoValutazioneAttaccante);
        BusEventi.iscriviti(EventoVariazioneEffettoDiStato.class, this::onEventoVariazioneEffettoDiStato);
        BusEventi.iscriviti(EventoVariazioneGemme.class, this::onEventoVariazioneGemme);
        BusEventi.iscriviti(EventoVariazioneIncantesimi.class, this::onEventoVariazioneIncantesimi);
        BusEventi.iscriviti(EventoVariazioneMappa.class, this::onEventoVariazioneMappa);
        BusEventi.iscriviti(EventoVariazioneMonete.class, this::onEventoVariazioneMonete);
        BusEventi.iscriviti(EventoVariazionePozioniMagia.class, this::onEventoVariazionePozioniMagia);
        BusEventi.iscriviti(EventoVariazionePozioniMagiaGrandi.class, this::onEventoVariazionePozioniMagiaGrandi);
        BusEventi.iscriviti(EventoVariazionePozioniSalute.class, this::onEventoVariazionePozioniSalute);
        BusEventi.iscriviti(EventoVariazionePozioniSaluteGrandi.class, this::onEventoVariazionePozioniSaluteGrandi);
        BusEventi.iscriviti(EventoVariazionePunti.class, this::onEventoVariazionePunti);
        BusEventi.iscriviti(EventoVariazionePuntiEsperienza.class, this::onEventoVariazionePuntiEsperienza);
        BusEventi.iscriviti(EventoVariazioneStatistichePersonaggio.class, this::onEventoVariazioneStatistichePersonaggio);
        BusEventi.iscriviti(EventoVariazioneStatoVitalePersonaggio.class, this::onEventoVariazioneStatoVitalePersonaggio);
    }

    private void onEventoAggiuntaModificatore(EventoAggiuntaModificatore evento) {
        ModificatoreAttributo modificatore = evento.getModificatore();
        Logger.log(headerEvento(evento) + formattaModificatoreAttributo(modificatore) +
                formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoApprovazioneAcquistoArtefatto(EventoApprovazioneAcquistoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoApprovazioneAcquistoConsumabile(EventoApprovazioneAcquistoConsumabile evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaAcquistoConsumabile().getUuid());
    }

    private void onEventoApprovazionePrelievoArtefatto(EventoApprovazionePrelievoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoApprovazioneStoccaggioArtefatto(EventoApprovazioneStoccaggioArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoApprovazioneVenditaArtefatto(EventoApprovazioneVenditaArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamentoArtefatto().getUuid());
    }

    private void onEventoAumentoLivelloMondo(EventoAumentoLivelloMondo evento) {
        Logger.log(headerEvento(evento) + "Mondo aumenta di livello a " + evento.getLivello());
    }

    private void onEventoAumentoLivelloPersonaggio(EventoAumentoLivelloPersonaggio evento) {
        Logger.log(headerEvento(evento) + "Aumento livello " + evento.getLivelloPrecedente() + " -> " +
                evento.getLivelloAttuale() + " - " + formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoComandiDisponibili(EventoComandiDisponibili evento) {
        Logger.log(headerEvento(evento) + "Comandi disponibili: " + evento.getComandiDisponibili());
    }

    private void onEventoComandoDiGioco(EventoComandoDiGioco evento) {
        Comando comando = evento.getComando();
        Logger.log(headerEvento(evento) + "Comando: " + comando);
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

    private void onEventoCreazioneSpriteAnnuncioGlobale(EventoCreazioneSpriteAnnuncioGlobale evento) {
        Logger.log(headerEvento(evento) + "Titolo: " + evento.getSprite().getTitolo() +
                ", Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteATempo(EventoCreazioneSpriteATempo evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteEffetto(EventoCreazioneSpriteEffetto evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteFumetto(EventoCreazioneSpriteFumetto evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteInDissolvenza(EventoCreazioneSpriteInDissolvenza evento) {
        Logger.log(headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoErroreCaricamento(EventoErroreCaricamento evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
    }

    private void onEventoErroreInterno(EventoErroreInterno evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoException(EventoException evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
    }

    private void onEventoFineGioco(EventoFineGioco evento) {
        Logger.log(String.format("%s - %s - Completato con successo: %s", new Date(), evento.getTipoEvento(), evento.isCompletatoConSuccesso()));
    }

    private void onEventoFumetto(EventoFumetto evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getTesto()));
    }

    private void onEventoInterazioneElementale(EventoInterazioneElementale evento) {
        Personaggio p = evento.getPersonaggio();
        Logger.log(headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoInterazioneElementale());
    }

    private void onEventoInterfacciaUtentePronta(EventoInterfacciaUtentePronta evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMessaggio(EventoMessaggio evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoMessaggioInterno(EventoMessaggioInterno evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggioInterno()));
    }

    private void onEventoMostraFinestra(EventoMostraFinestra evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getFinestre()));
    }

    private void onEventoMostraPunteggi(EventoMostraPunteggi evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMostraSchermataGioco(EventoMostraSchermataGioco evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMostraStatistiche(EventoMostraStatistiche evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoNotificaGlobale(EventoNotificaGlobale evento) {
        Logger.log(String.format("%s - %s - %s - %s - %s", new Date(), evento.getTipoEvento(), evento.getEtichetta(), evento.getEtichetta(), evento.getMessaggio()));
    }

    private void onEventoParagrafo(EventoParagrafo evento) {
        Logger.log(String.format("%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoPreparazioneLocazione(EventoPreparazioneLocazione evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoPuliziaCacheDinamicaImmagini(EventoPuliziaCacheDinamicaImmagini evento) {
        int prima = evento.getElementiPrima();
        int dopo = evento.getElementiDopo();
        Logger.log(headerEvento(evento) + "Eliminate " + (prima - dopo) + " immagini dalla cache. Rimanenti: " + dopo);
    }

    private void onEventoRaccoltaOggetti(EventoRaccoltaOggetti evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaAcquistoArtefatto(EventoRichiestaAcquistoArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaAcquistoConsumabile(EventoRichiestaAcquistoConsumabile evento) {
        ClasseIncantesimo incantesimo = evento.getClasseIncantesimo();
        Logger.log(headerEvento(evento) + "Gruppo richiede di acquistare " + evento.getTipoConsumabile() +
                (incantesimo != null ? (" " + incantesimo) : "") + ", Costo: " + evento.getPrezzo());
    }

    private void onEventoRichiestaAperturaFinestraCombattimento(EventoRichiestaAperturaFinestraCombattimento evento) {
        String notifica = "Attiva - " + formattaStatistichePersonaggio(evento.getPersonaggio()) + " vs " +
                formattaStatistichePersonaggio(evento.getAvversario());
        Logger.log(headerEvento(evento) + notifica);
    }

    private void onEventoRichiestaAperturaInventarioCommerciante(EventoRichiestaAperturaInventarioCommerciante evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaAperturaInventarioFornitore(EventoRichiestaAperturaInventarioFornitore evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaAperturaInventarioGruppo(EventoRichiestaAperturaInventarioGruppo evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaChiusuraFinestraCombattimento(EventoRichiestaChiusuraFinestraCombattimento evento) {
        Logger.log(headerEvento(evento) + "Chiusa");
    }

    private void onEventoRichiestaConfermaUscita(EventoRichiestaConfermaUscita evento) {
        Logger.log(String.format("%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoRichiestaPrelievoArtefatto(EventoRichiestaPrelievoArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaRefreshUI(EventoRichiestaRefreshUI evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaReinizializzazioneUI(EventoRichiestaReinizializzazioneUI evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaSelezioneSlotPerRilettura(EventoRichiestaSelezioneSlotPerRilettura evento) {
        Logger.log(headerEvento(evento) + "Salvataggi disponibili: " + evento.getSalvataggiDisponibili()
                .stream().map(TestataSalvataggio::getId).map(Comando::name).collect(Collectors.joining(", ")));
    }

    private void onEventoRichiestaSelezioneSlotPerSalvataggio(EventoRichiestaSelezioneSlotPerSalvataggio evento) {
        Logger.log(headerEvento(evento) + "Salvataggi disponibili: " + evento.getSalvataggiDisponibili()
                .stream().map(TestataSalvataggio::getId).map(Comando::name).collect(Collectors.joining(", ")));
    }

    private void onEventoRichiestaVisualizzazioneMappa(EventoRichiestaVisualizzazioneMappa evento) {
        Logger.log(headerEvento(evento));
    }

    private void onEventoRichiestaStoccaggioArtefatto(EventoRichiestaStoccaggioArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaTesto(EventoRichiestaTesto evento) {
        Logger.log(headerEvento(evento) + "Richiesta: " + evento.getRichiesta());
    }

    private void onEventoRichiestaVenditaArtefatto(EventoRichiestaVenditaArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRifiutoAcquistoArtefatto(EventoRifiutoAcquistoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
    }

    private void onEventoRifiutoAcquistoConsumabile(EventoRifiutoAcquistoConsumabile evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaAcquistoConsumabile().getUuid());
    }

    private void onEventoRifiutoPrelievoArtefatto(EventoRifiutoPrelievoArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
    }

    private void onEventoRifiutoStoccaggioArtefatto(EventoRifiutoStoccaggioArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
    }

    private void onEventoRifiutoVenditaArtefatto(EventoRifiutoVenditaArtefatto evento) {
        Logger.log(headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaSpostamento().getUuid());
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

    public void onEventoVariazioneGemme(EventoVariazioneGemme evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneIncantesimi(EventoVariazioneIncantesimi evento) {
        Logger.log(headerEvento(evento) + String.format("Tipo: %s, Variazione: %d -> %d",
                evento.getClasseIncantesimo(), evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneMappa(EventoVariazioneMappa evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: (%d, %d) -> (%d, %d)",
                evento.getDaX(), evento.getDaY(), evento.getaX(), evento.getaY()));
    }

    public void onEventoVariazioneMonete(EventoVariazioneMonete evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagia(EventoVariazionePozioniMagia evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagiaGrandi(EventoVariazionePozioniMagiaGrandi evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSalute(EventoVariazionePozioniSalute evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSaluteGrandi(EventoVariazionePozioniSaluteGrandi evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePunti(EventoVariazionePunti evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePuntiEsperienza(EventoVariazionePuntiEsperienza evento) {
        Logger.log(headerEvento(evento) + String.format("Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
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
