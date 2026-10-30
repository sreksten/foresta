package com.threeamigos.foresta;

import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.OggettoConCosto;
import com.threeamigos.foresta.artefatti.OggettoConPeso;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.comandigiocatore.*;
import com.threeamigos.foresta.eventi.interni.*;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.eventi.richieste.*;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Ascolta tutti i tipi di evento del bus e ne scrive una riga nel log, con il tipo in testa per poterle cercare con grep:
 * serve a seguire dall'esterno cosa accade in una partita, senza influire sul gioco.
 *
 * @author Stefano Reksten
 */
public class SnifferBusEventi {

    /**
     * Si iscrive a tutte le notifiche e richieste; da creare una volta sola, all'avvio.
     */
    public SnifferBusEventi() {
        BusEventi.iscriviti(NotificaAggiornamentoStatoMissione.class, this::onEventoAggiornamentoStatoMissione);
        BusEventi.iscriviti(NotificaVariazioneModificatorePersonaggio.class, this::onEventoVariazioneModificatore);
        BusEventi.iscriviti(NotificaApprovazioneAcquistoArtefatto.class, this::onEventoApprovazioneAcquistoArtefatto);
        BusEventi.iscriviti(NotificaApprovazioneAcquistoConsumabile.class, this::onEventoApprovazioneAcquistoConsumabile);
        BusEventi.iscriviti(NotificaApprovazionePrelievoArtefatto.class, this::onEventoApprovazionePrelievoArtefatto);
        // EventoApprovazioneSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(NotificaApprovazioneStoccaggioArtefatto.class, this::onEventoApprovazioneStoccaggioArtefatto);
        BusEventi.iscriviti(NotificaApprovazioneVenditaArtefatto.class, this::onEventoApprovazioneVenditaArtefatto);
        BusEventi.iscriviti(NotificaAumentoLivelloMondo.class, this::onEventoAumentoLivelloMondo);
        BusEventi.iscriviti(NotificaArtefattoTrovato.class, this::onEventoArtefattoTrovato);
        BusEventi.iscriviti(NotificaAumentoLivelloPersonaggio.class, this::onEventoAumentoLivelloPersonaggio);
        // EventoBase è classe astratta
        BusEventi.iscriviti(InternoAggiornamentoComandiDisponibili.class, this::onEventoComandiDisponibili);
        BusEventi.iscriviti(ComandoDiGioco.class, this::onEventoComandoDiGioco);
        BusEventi.iscriviti(NotificaConsumoPuntoAbilitaPersonaggio.class, this::onEventoConsumoPuntoAbilita);
        BusEventi.iscriviti(InternoCreazioneFumetto.class, this::onEventoCreazioneFumetto);
        BusEventi.iscriviti(InternoCreazionePersonaggio.class, this::onEventoCreazionePersonaggio);
        BusEventi.iscriviti(InternoCreazioneSpriteAnnuncioGlobale.class, this::onEventoCreazioneSpriteAnnuncioGlobale);
        BusEventi.iscriviti(InternoCreazioneSpriteATempo.class, this::onEventoCreazioneSpriteATempo);
        BusEventi.iscriviti(InternoCreazioneSpriteEffettoDiStato.class, this::onEventoCreazioneSpriteEffetto);
        BusEventi.iscriviti(InternoCreazioneSpriteFumettoATempo.class, this::onEventoCreazioneSpriteFumetto);
        BusEventi.iscriviti(InternoCreazioneSpriteInDissolvenza.class, this::onEventoCreazioneSpriteInDissolvenza);
        BusEventi.iscriviti(NotificaErroreCaricamento.class, this::onEventoErroreCaricamento);
        BusEventi.iscriviti(InternoErrore.class, this::onEventoErroreInterno);
        BusEventi.iscriviti(InternoException.class, this::onEventoException);
        BusEventi.iscriviti(NotificaFineGioco.class, this::onEventoFineGioco);
        BusEventi.iscriviti(InternoNotificaViaFumettoATempo.class, this::onEventoFumetto);
        BusEventi.iscriviti(InternoInizioRound.class, this::onEventoInizioRound);
        BusEventi.iscriviti(InternoAzioneInCombattimento.class, this::onEventoAzioneInCombattimento);
        BusEventi.iscriviti(InternoDecisioneTattica.class, this::onEventoDecisioneTattica);
        BusEventi.iscriviti(NotificaInterazionePersonaggio.class, this::onEventoInterazione);
        BusEventi.iscriviti(NotificaReazioneDiClassePersonaggio.class, this::onEventoReazioneDiClasse);
        BusEventi.iscriviti(NotificaColpoMancato.class, this::onEventoColpoMancato);
        BusEventi.iscriviti(InternoInterfacciaUtentePronta.class, this::onEventoInterfacciaUtentePronta);
        BusEventi.iscriviti(ComandoInvioTesto.class, this::onEventoInvioTesto);
        BusEventi.iscriviti(NotificaTestoFrase.class, this::onEventoMessaggio);
        BusEventi.iscriviti(InternoMessaggio.class, this::onEventoMessaggioInterno);
        BusEventi.iscriviti(InternoMostraFinestraStato.class, this::onEventoMostraFinestra);
        BusEventi.iscriviti(InternoMostraFinestraStatistiche.class, this::onEventoMostraFinestra);
        BusEventi.iscriviti(InternoMostraFinestraIncantesimiEPozioni.class, this::onEventoMostraFinestra);
        BusEventi.iscriviti(NotificaMostraPunteggiMigliori.class, this::onEventoMostraPunteggi);
        BusEventi.iscriviti(InternoMostraSchermataGioco.class, this::onEventoMostraSchermataGioco);
        BusEventi.iscriviti(NotificaMostraStatisticheFineGioco.class, this::onEventoMostraStatistiche);
        BusEventi.iscriviti(NotificaGlobale.class, this::onEventoNotificaGlobale);
        BusEventi.iscriviti(NotificaTestoParagrafo.class, this::onEventoParagrafo);
        // EventoPersonaggio è classe astratta
        BusEventi.iscriviti(InternoPreparazioneLocazione.class, this::onEventoPreparazioneLocazione);
        BusEventi.iscriviti(InternoPuliziaCacheDinamicaImmagini.class, this::onEventoPuliziaCacheDinamicaImmagini);
        BusEventi.iscriviti(NotificaRaccoltaOggetti.class, this::onEventoRaccoltaOggetti);
        BusEventi.iscriviti(InternoTrofeoAcquisito.class, this::onEventoTrofeoAcquisito);
        BusEventi.iscriviti(InternoAvversarioSconfitto.class, this::onEventoAvversarioSconfitto);
        BusEventi.iscriviti(InternoVoceDelBestiarioSbloccata.class, this::onEventoVoceDelBestiarioSbloccata);
        BusEventi.iscriviti(InternoCaratteristicaOsservata.class, this::onEventoCaratteristicaOsservata);
        BusEventi.iscriviti(InternoPersonaggioEntratoNelGruppo.class, this::onEventoPersonaggioEntratoNelGruppo);
        BusEventi.iscriviti(InternoSfidaVinta.class, this::onEventoSfidaVinta);
        BusEventi.iscriviti(InternoFineLocazione.class, this::onEventoFineLocazione);
        BusEventi.iscriviti(InternoPastoConsumatoInLocanda.class, this::onEventoPastoConsumatoInLocanda);
        BusEventi.iscriviti(InternoLocazioneVisitata.class, this::onEventoLocazioneVisitata);
        BusEventi.iscriviti(InternoPuntiEsperienzaAssegnati.class, this::onEventoPuntiEsperienzaAssegnati);
        BusEventi.iscriviti(InternoEremitiIncontrati.class, this::onEventoEremitiIncontrati);
        BusEventi.iscriviti(InternoSpadaNascostaImpugnata.class, this::onEventoSpadaNascostaImpugnata);
        BusEventi.iscriviti(InternoAmiciziaStretta.class, this::onEventoAmiciziaStretta);
        BusEventi.iscriviti(InternoCorruzioneRiuscita.class, this::onEventoCorruzioneRiuscita);
        BusEventi.iscriviti(InternoScheletroRisvegliato.class, this::onEventoScheletroRisvegliato);
        BusEventi.iscriviti(InternoOggettoRaccolto.class, this::onEventoOggettoRaccolto);
        BusEventi.iscriviti(InternoArtefattoTrovato.class, this::onEventoArtefattoTrovato);
        BusEventi.iscriviti(InternoMissioneCompletata.class, this::onEventoMissioneCompletata);
        BusEventi.iscriviti(InternoAcquistoArtefatto.class, this::onEventoRichiestaAcquistoArtefatto);
        BusEventi.iscriviti(ComandoAcquistoConsumabile.class, this::onEventoRichiestaAcquistoConsumabile);
        BusEventi.iscriviti(InternoRichiestaAperturaFinestraCombattimento.class, this::onEventoRichiestaAperturaFinestraCombattimento);
        BusEventi.iscriviti(RichiestaAperturaInventarioCommerciante.class, this::onEventoRichiestaAperturaInventarioCommerciante);
        BusEventi.iscriviti(RichiestaAperturaInventarioFornitore.class, this::onEventoRichiestaAperturaInventarioFornitore);
        BusEventi.iscriviti(RichiestaAperturaInventarioGruppo.class, this::onEventoRichiestaAperturaInventarioGruppo);
        BusEventi.iscriviti(InternoRichiestaChiusuraFinestraCombattimento.class, this::onEventoRichiestaChiusuraFinestraCombattimento);
        BusEventi.iscriviti(InternoPrelievoArtefatto.class, this::onEventoRichiestaPrelievoArtefatto);
        BusEventi.iscriviti(InternoRichiestaRefreshUI.class, this::onEventoRichiestaRefreshUI);
        BusEventi.iscriviti(InternoRichiestaReinizializzazioneUI.class, this::onEventoRichiestaReinizializzazioneUI);
        // EventoRichiestaSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(RichiestaSelezioneSlotPerRilettura.class, this::onEventoRichiestaSelezioneSlotPerRilettura);
        BusEventi.iscriviti(RichiestaSelezioneSlotPerSalvataggio.class, this::onEventoRichiestaSelezioneSlotPerSalvataggio);
        BusEventi.iscriviti(InternoStoccaggioArtefatto.class, this::onEventoRichiestaStoccaggioArtefatto);
        BusEventi.iscriviti(RichiestaTesto.class, this::onEventoRichiestaTesto);
        BusEventi.iscriviti(InternoVenditaArtefatto.class, this::onEventoRichiestaVenditaArtefatto);
        BusEventi.iscriviti(RichiestaVisualizzazioneMappa.class, this::onEventoRichiestaVisualizzazioneMappa);
        BusEventi.iscriviti(NotificaRifiutoAcquistoArtefatto.class, this::onEventoRifiutoAcquistoArtefatto);
        BusEventi.iscriviti(NotificaRifiutoAcquistoConsumabile.class, this::onEventoRifiutoAcquistoConsumabile);
        BusEventi.iscriviti(NotificaRifiutoPrelievoArtefatto.class, this::onEventoRifiutoPrelievoArtefatto);
        // EventoRifiutoSpostamentoArtefatto è classe astratta
        BusEventi.iscriviti(NotificaRifiutoStoccaggioArtefatto.class, this::onEventoRifiutoStoccaggioArtefatto);
        BusEventi.iscriviti(NotificaRifiutoVenditaArtefatto.class, this::onEventoRifiutoVenditaArtefatto);
        BusEventi.iscriviti(RichiestaUscitaDalGioco.class, this::onEventoSelezioneConfermaUscita);
        BusEventi.iscriviti(NotificaUscitaDalGioco.class, this::onEventoUscitaDalGioco);
        BusEventi.iscriviti(RichiestaSelezioneDirezione.class, this::onEventoSelezioneDirezione);
        BusEventi.iscriviti(RichiestaSelezioneIncantesimoDaLanciare.class, this::onEventoSelezioneIncantesimoDaLanciare);
        BusEventi.iscriviti(RichiestaSelezioneSiNo.class, this::onEventoSelezioneSiNo);
        BusEventi.iscriviti(InternoFaseDiGioco.class, this::onEventoFaseDiGioco);
        BusEventi.iscriviti(NotificaVariazioneEffettoDiStatoPersonaggio.class, this::onEventoVariazioneEffettoDiStato);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPreziosi.class, this::onEventoVariazionePreziosi);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaIncantesimi.class, this::onEventoVariazioneIncantesimi);
        BusEventi.iscriviti(NotificaVariazioneConoscenzaMappa.class, this::onEventoVariazioneMappa);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaMonete.class, this::onEventoVariazioneMonete);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagia.class, this::onEventoVariazionePozioniMagia);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniMagiaGrandi.class, this::onEventoVariazionePozioniMagiaGrandi);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniAumentoLivello.class, this::onEventoVariazionePozioniAumentoLivello);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSalute.class, this::onEventoVariazionePozioniSalute);
        BusEventi.iscriviti(NotificaVariazioneDisponibilitaPozioniSaluteGrandi.class, this::onEventoVariazionePozioniSaluteGrandi);
        BusEventi.iscriviti(NotificaVariazionePunteggio.class, this::onEventoVariazionePunti);
        BusEventi.iscriviti(NotificaVariazionePuntiEsperienzaPersonaggio.class, this::onEventoVariazionePuntiEsperienza);
        BusEventi.iscriviti(NotificaVariazioneStatistichePersonaggio.class, this::onEventoVariazioneStatistichePersonaggio);
        BusEventi.iscriviti(NotificaVariazioneStatoVitalePersonaggio.class, this::onEventoVariazioneStatoVitalePersonaggio);
        // Lo sniffer ascolta qualsiasi evento: chi aggiunge un evento nuovo lo iscrive qui (lo controlla SnifferBusEventiTest)
        BusEventi.iscriviti(ComandoCommutazioneElenco.class, this::onEventoCommutazioneElenco);
        BusEventi.iscriviti(ComandoImpostaTattica.class, this::onEventoImpostaTattica);
        BusEventi.iscriviti(ComandoImpostazioneAiuto.class, this::onEventoImpostazioneAiuto);
        BusEventi.iscriviti(ComandoScambioArtefatto.class, this::onEventoScambioArtefatto);
        BusEventi.iscriviti(ComandoSpesaPuntoAbilita.class, this::onEventoSpesaPuntoAbilita);
        BusEventi.iscriviti(ComandoSpostaPersonaggio.class, this::onEventoSpostaPersonaggio);
        BusEventi.iscriviti(InternoAssegnaCoordinateAPersonaggi.class, e -> Logger.log(() -> headerEvento(e)));
        BusEventi.iscriviti(InternoCaricamentoCompletato.class, this::onEventoCaricamentoCompletato);
        BusEventi.iscriviti(InternoFineLogoIniziale.class, e -> Logger.log(() -> headerEvento(e)));
        BusEventi.iscriviti(InternoFumettoSuccessivo.class, this::onEventoFumettoSuccessivo);
        BusEventi.iscriviti(InternoIncantatura.class, this::onEventoIncantatura);
        BusEventi.iscriviti(InternoPassaggioInosservato.class, e -> Logger.log(() -> headerEvento(e)));
        BusEventi.iscriviti(InternoPersonaggioArreso.class, this::onEventoPersonaggioArreso);
        BusEventi.iscriviti(InternoPrecaricamentoMotoreCompletato.class, e -> Logger.log(() -> headerEvento(e)));
        BusEventi.iscriviti(InternoSfidaCartaForbiciSasso.class, this::onEventoSfidaCartaForbiciSasso);
        BusEventi.iscriviti(InternoUiInattiva.class, e -> Logger.log(() -> headerEvento(e)));
        BusEventi.iscriviti(InternoUiOccupata.class, e -> Logger.log(() -> headerEvento(e)));
        BusEventi.iscriviti(NotificaApprovazioneIncantatura.class, this::onEventoApprovazioneIncantatura);
        BusEventi.iscriviti(NotificaAvvisoIncantatura.class, this::onEventoAvvisoIncantatura);
        BusEventi.iscriviti(NotificaPaginaIntermezzo.class, this::onEventoPaginaIntermezzo);
        BusEventi.iscriviti(NotificaRifiutoIncantatura.class, this::onEventoRifiutoIncantatura);
        BusEventi.iscriviti(RichiestaAperturaIncantatore.class, this::onEventoRichiestaAperturaIncantatore);
        BusEventi.iscriviti(RichiestaAperturaTattiche.class, this::onEventoRichiestaAperturaTattiche);
        BusEventi.iscriviti(RichiestaAperturaTrofei.class, this::onEventoRichiestaAperturaTrofei);
        BusEventi.iscriviti(RichiestaAperturaBestiario.class, this::onEventoRichiestaAperturaBestiario);
        BusEventi.iscriviti(RichiestaSelezioneMissione.class, this::onEventoRichiestaSelezioneMissione);
    }

    private void onEventoCommutazioneElenco(ComandoCommutazioneElenco evento) {
        Logger.log(() -> headerEvento(evento) + "Scambio " + evento.getIdScambio() + ", artefatto " + evento.getUuidArtefatto()
                + ", missione " + evento.getIdMissione());
    }

    private void onEventoImpostaTattica(ComandoImpostaTattica evento) {
        Logger.log(() -> headerEvento(evento) + "Personaggio " + evento.getUuidPersonaggio() + ", voci: "
                + (evento.getTattica() == null ? "nessuna tattica" : evento.getTattica().getVoci().size()));
    }

    private void onEventoImpostazioneAiuto(ComandoImpostazioneAiuto evento) {
        Logger.log(() -> headerEvento(evento) + (evento.isAbilitato() ? "Aiuto abilitato" : "Aiuto disabilitato"));
    }

    private void onEventoScambioArtefatto(ComandoScambioArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Scambio " + evento.getIdScambio() + ", artefatto " + evento.getUuidArtefatto()
                + ", destinazione " + evento.getDestinazione());
    }

    private void onEventoSpesaPuntoAbilita(ComandoSpesaPuntoAbilita evento) {
        Logger.log(() -> headerEvento(evento) + "Personaggio " + evento.getUuidPersonaggio() + ", attributo " + evento.getAttributo());
    }

    private void onEventoSpostaPersonaggio(ComandoSpostaPersonaggio evento) {
        Logger.log(() -> headerEvento(evento) + "Personaggio " + evento.getUuidPersonaggio() + " verso "
                + (evento.isVersoDestra() ? "destra" : "sinistra"));
    }

    private void onEventoCaricamentoCompletato(InternoCaricamentoCompletato evento) {
        Logger.log(() -> headerEvento(evento) + "Ultimi messaggi: " + evento.getUltimiMessaggi().size());
    }

    private void onEventoFumettoSuccessivo(InternoFumettoSuccessivo evento) {
        Logger.log(() -> headerEvento(evento) + "Fra " + evento.getSecondi() + " secondi");
    }

    private void onEventoIncantatura(InternoIncantatura evento) {
        Logger.log(() -> headerEvento(evento) + "Nome proprio: " + evento.getNomeProprio());
    }

    private void onEventoPersonaggioArreso(InternoPersonaggioArreso evento) {
        Logger.log(() -> headerEvento(evento) + evento.getPersonaggio().getNome(Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA));
    }

    private void onEventoSfidaCartaForbiciSasso(InternoSfidaCartaForbiciSasso evento) {
        Logger.log(() -> headerEvento(evento) + (evento.isInizio() ? "Inizio" : evento.isFinale() ? "Fine" : "Mano")
                + " - Giocatore: " + evento.getMossaDelGiocatore() + " (" + evento.getPunteggioDelGiocatore() + "), Avversario: "
                + evento.getMossaDellAvversario() + " (" + evento.getPunteggioDellAvversario() + ")");
    }

    private void onEventoApprovazioneIncantatura(NotificaApprovazioneIncantatura evento) {
        Logger.log(() -> headerEvento(evento) + evento.getArtefatto().getNomeBreve() + ", costo " + evento.getCosto());
    }

    private void onEventoAvvisoIncantatura(NotificaAvvisoIncantatura evento) {
        Logger.log(() -> headerEvento(evento) + evento.getFrase());
    }

    private void onEventoPaginaIntermezzo(NotificaPaginaIntermezzo evento) {
        Logger.log(() -> headerEvento(evento) + "Pagina " + evento.getNumeroPagina() + " di " + evento.getTotalePagine());
    }

    private void onEventoRifiutoIncantatura(NotificaRifiutoIncantatura evento) {
        Logger.log(() -> headerEvento(evento) + evento.getMotivo());
    }

    private void onEventoRichiestaAperturaIncantatore(RichiestaAperturaIncantatore evento) {
        Logger.log(() -> headerEvento(evento) + "Comandi: " + evento.getPossibilita() + ", messaggio: " + evento.getMessaggio());
    }

    private void onEventoRichiestaAperturaTattiche(RichiestaAperturaTattiche evento) {
        Logger.log(() -> headerEvento(evento) + "Personaggio " + evento.getPersonaggio().getUuid() + ", comandi: " + evento.getPossibilita());
    }

    private void onEventoRichiestaAperturaTrofei(RichiestaAperturaTrofei evento) {
        Logger.log(() -> headerEvento(evento) + "Comandi: " + evento.getPossibilita());
    }

    private void onEventoRichiestaAperturaBestiario(RichiestaAperturaBestiario evento) {
        Logger.log(() -> headerEvento(evento) + "Comandi: " + evento.getPossibilita());
    }

    private void onEventoRichiestaSelezioneMissione(RichiestaSelezioneMissione evento) {
        Logger.log(() -> headerEvento(evento) + "Comandi: " + evento.getPossibilita());
    }

    private void onEventoAggiornamentoStatoMissione(NotificaAggiornamentoStatoMissione evento) {
        Logger.log(() -> headerEvento(evento) + "Missione " + evento.getMissione().getNome() + " - " +
                evento.getEtichetta() + " - " + evento.getDescrizione());
    }

    private void onEventoVariazioneModificatore(NotificaVariazioneModificatorePersonaggio evento) {
        ModificatoreAttributoMD modificatore = evento.getModificatore();
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Tipo: %s, Durata: %d -> %d, ", evento.getTipo(),
                evento.getDurataPrecedente(), evento.getNuovaDurata()) + formattaModificatoreAttributo(modificatore) +
                formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoApprovazioneAcquistoArtefatto(NotificaApprovazioneAcquistoArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoApprovazioneAcquistoConsumabile(NotificaApprovazioneAcquistoConsumabile evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaAcquistoConsumabile().getUuid()
                + ", Pagato: " + evento.getPrezzoPagato());
    }

    private void onEventoApprovazionePrelievoArtefatto(NotificaApprovazionePrelievoArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoApprovazioneStoccaggioArtefatto(NotificaApprovazioneStoccaggioArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoApprovazioneVenditaArtefatto(NotificaApprovazioneVenditaArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoArtefattoTrovato(NotificaArtefattoTrovato evento) {
        Logger.log(() -> headerEvento(evento) + "Trovato " + evento.getArtefatto().getNomeCompleto());
    }

    private void onEventoAumentoLivelloMondo(NotificaAumentoLivelloMondo evento) {
        Logger.log(() -> headerEvento(evento) + "Mondo aumenta di livello a " + evento.getLivello());
    }

    private void onEventoAumentoLivelloPersonaggio(NotificaAumentoLivelloPersonaggio evento) {
        Logger.log(() -> headerEvento(evento) + "Aumento livello " + evento.getLivelloPrecedente() + " -> " +
                evento.getLivelloAttuale() + " - " + formattaStatistichePersonaggio(evento.getPersonaggio()));
    }

    private void onEventoComandiDisponibili(InternoAggiornamentoComandiDisponibili evento) {
        Logger.log(() -> headerEvento(evento) + "Comandi disponibili: " + evento.getPossibilita());
    }

    private void onEventoComandoDiGioco(ComandoDiGioco evento) {
        Comando comando = evento.getComando();
        Logger.log(() -> headerEvento(evento) + "Comando: " + comando);
    }

    private void onEventoConsumoPuntoAbilita(NotificaConsumoPuntoAbilitaPersonaggio evento) {
        VistaPersonaggio p = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoAttributo());
    }

    private void onEventoCreazioneFumetto(InternoCreazioneFumetto evento) {
        Logger.log(() -> headerEvento(evento) + "Frase: " + evento.getFrase());
    }

    private void onEventoCreazionePersonaggio(InternoCreazionePersonaggio evento) {
        VistaPersonaggio p  = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + formattaStatistichePersonaggio(p));
    }

    private void onEventoCreazioneSpriteAnnuncioGlobale(InternoCreazioneSpriteAnnuncioGlobale evento) {
        Logger.log(() -> headerEvento(evento) + "Titolo: " + evento.getSprite().getTitolo() +
                ", Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteATempo(InternoCreazioneSpriteATempo evento) {
        Logger.log(() -> headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoCreazioneSpriteEffetto(InternoCreazioneSpriteEffettoDiStato evento) {
        Logger.log(() -> headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteFumetto(InternoCreazioneSpriteFumettoATempo evento) {
        Logger.log(() -> headerEvento(evento) + "Descrizione: " + evento.getSprite().getTesto());
    }

    private void onEventoCreazioneSpriteInDissolvenza(InternoCreazioneSpriteInDissolvenza evento) {
        Logger.log(() -> headerEvento(evento) + "Descrizione: " + evento.getSprite().getDescrizione());
    }

    private void onEventoErroreCaricamento(NotificaErroreCaricamento evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
    }

    private void onEventoErroreInterno(InternoErrore evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoException(InternoException evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getException().getMessage()));
        Logger.log(evento.getException());
    }

    private void onEventoFineGioco(NotificaFineGioco evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - Completato con successo: %s", new Date(), evento.getTipoEvento(), evento.isCompletatoConSuccesso()));
    }

    private void onEventoFumetto(InternoNotificaViaFumettoATempo evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getTesto()));
    }

    private void onEventoAzioneInCombattimento(InternoAzioneInCombattimento evento) {
        Logger.log(() -> headerEvento(evento) + "Agisce " + formattaStatistichePersonaggio(evento.getPersonaggio()) +
                (evento.getBersaglio() == null ? " (senza bersaglio)" : " contro " + formattaStatistichePersonaggio(evento.getBersaglio())));
    }

    private void onEventoDecisioneTattica(InternoDecisioneTattica evento) {
        Logger.log(() -> headerEvento(evento) + formattaStatistichePersonaggio(evento.getPersonaggio()) + "decide " + evento.getAzione()
                + (evento.getBersaglio() == null ? "" : " contro " + formattaStatistichePersonaggio(evento.getBersaglio()))
                + (evento.isRipiego() ? " (ripiego)" : " (voce " + (evento.getIndiceVoce() + 1) + ")") + " - " + String.join(" | ", evento.getSpiegazione()));
    }

    private void onEventoInizioRound(InternoInizioRound evento) {
        Logger.log(() -> headerEvento(evento) + "Round " + evento.getNumeroRound());
    }

    private void onEventoColpoMancato(NotificaColpoMancato evento) {
        VistaPersonaggio p = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + formattaStatistichePersonaggio(p));
    }

    private void onEventoReazioneDiClasse(NotificaReazioneDiClassePersonaggio evento) {
        VistaPersonaggio p = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoDanno() + " x" + evento.getMoltiplicatore());
    }

    private void onEventoInterazione(NotificaInterazionePersonaggio evento) {
        VistaPersonaggio p = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + formattaStatistichePersonaggio(p) + evento.getTipoInterazione());
    }

    private void onEventoInterfacciaUtentePronta(InternoInterfacciaUtentePronta evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoInvioTesto(ComandoInvioTesto evento) {
        Logger.log(() -> headerEvento(evento) + "Ricevuto testo: " + evento.getTesto());
    }

    private void onEventoMessaggio(NotificaTestoFrase evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoMessaggioInterno(InternoMessaggio evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggioInterno()));
    }

    private void onEventoMostraFinestra(EventoBase evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s ", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMostraPunteggi(NotificaMostraPunteggiMigliori evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMostraSchermataGioco(InternoMostraSchermataGioco evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoMostraStatistiche(NotificaMostraStatisticheFineGioco evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoNotificaGlobale(NotificaGlobale evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s - %s", new Date(), evento.getTipoEvento(), evento.getEtichetta(), evento.getMessaggio()));
    }

    private void onEventoParagrafo(NotificaTestoParagrafo evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s - %s ", new Date(), evento.getTipoEvento(), evento.getMessaggio()));
    }

    private void onEventoPreparazioneLocazione(InternoPreparazioneLocazione evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoPuliziaCacheDinamicaImmagini(InternoPuliziaCacheDinamicaImmagini evento) {
        int prima = evento.getElementiPrima();
        int dopo = evento.getElementiDopo();
        Logger.log(() -> headerEvento(evento) + "Eliminate " + (prima - dopo) + " immagini dalla cache. Rimanenti: " + dopo);
    }

    private void onEventoRaccoltaOggetti(NotificaRaccoltaOggetti evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoTrofeoAcquisito(InternoTrofeoAcquisito evento) {
        Logger.log(() -> headerEvento(evento) + "Trofeo vinto: " + evento.getTrofeo().getNome());
    }

    private void onEventoAvversarioSconfitto(InternoAvversarioSconfitto evento) {
        Logger.log(() -> headerEvento(evento) + "Sconfitto: " + evento.getClasse());
    }

    private void onEventoVoceDelBestiarioSbloccata(InternoVoceDelBestiarioSbloccata evento) {
        Logger.log(() -> headerEvento(evento) + "Voce sbloccata: " + evento.getVoce());
    }

    private void onEventoCaratteristicaOsservata(InternoCaratteristicaOsservata evento) {
        Logger.log(() -> headerEvento(evento) + evento.getClasse() + ": " + evento.getGenere() + " " + evento.getValore());
    }

    private void onEventoPersonaggioEntratoNelGruppo(InternoPersonaggioEntratoNelGruppo evento) {
        Logger.log(() -> headerEvento(evento) + "Nel gruppo: " + evento.getClasse());
    }

    private void onEventoSfidaVinta(InternoSfidaVinta evento) {
        Logger.log(() -> headerEvento(evento) + "Sfida vinta contro: " + evento.getAvversario());
    }

    private void onEventoFineLocazione(InternoFineLocazione evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoPastoConsumatoInLocanda(InternoPastoConsumatoInLocanda evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoLocazioneVisitata(InternoLocazioneVisitata evento) {
        Logger.log(() -> headerEvento(evento) + "Prima visita: " + evento.getTipo());
    }

    private void onEventoPuntiEsperienzaAssegnati(InternoPuntiEsperienzaAssegnati evento) {
        Logger.log(() -> headerEvento(evento) + "Punti esperienza: " + evento.getPunti());
    }

    private void onEventoEremitiIncontrati(InternoEremitiIncontrati evento) {
        Logger.log(() -> headerEvento(evento) + "Eremiti: " + evento.getQuantita());
    }

    private void onEventoSpadaNascostaImpugnata(InternoSpadaNascostaImpugnata evento) {
        Logger.log(() -> headerEvento(evento) + "La spada normalmente nascosta è stata impugnata");
    }

    private void onEventoAmiciziaStretta(InternoAmiciziaStretta evento) {
        Logger.log(() -> headerEvento(evento) + "Amicizia con: " + evento.getAvversari());
    }

    private void onEventoCorruzioneRiuscita(InternoCorruzioneRiuscita evento) {
        Logger.log(() -> headerEvento(evento) + "Corrotti: " + evento.getAvversari());
    }

    private void onEventoScheletroRisvegliato(InternoScheletroRisvegliato evento) {
        Logger.log(() -> headerEvento(evento) + "Uno scheletro si è risvegliato dalla parte del gruppo");
    }

    private void onEventoMissioneCompletata(InternoMissioneCompletata evento) {
        Logger.log(() -> headerEvento(evento) + "Missione completata: " + evento.getMissione().getNome());
    }

    private void onEventoArtefattoTrovato(InternoArtefattoTrovato evento) {
        Logger.log(() -> headerEvento(evento) + "Artefatto trovato: " + evento.getArtefatto().getNomeCompleto()
                + (evento.isCustodito() ? ", custodito" : ", incustodito"));
    }

    private void onEventoOggettoRaccolto(InternoOggettoRaccolto evento) {
        Logger.log(() -> headerEvento(evento) + evento.getQuantita() + " " + evento.getClasse()
                + (evento.isCustodito() ? ", custodito" : ", incustodito"));
    }

    private void onEventoRichiestaAcquistoArtefatto(InternoAcquistoArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(() -> headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di acquistare "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaAcquistoConsumabile(ComandoAcquistoConsumabile evento) {
        TipoIncantesimo incantesimo = evento.getClasseIncantesimo();
        Logger.log(() -> headerEvento(evento) + "Gruppo richiede di acquistare " + evento.getTipoConsumabile() +
                (incantesimo != null ? (" " + incantesimo) : ""));
    }

    private void onEventoRichiestaAperturaFinestraCombattimento(InternoRichiestaAperturaFinestraCombattimento evento) {
        Logger.log(() -> headerEvento(evento) + "Attiva - " + formattaStatistichePersonaggio(evento.getPersonaggio()) + " vs " +
                formattaStatistichePersonaggio(evento.getAvversario()));
    }

    private void onEventoRichiestaAperturaInventarioCommerciante(RichiestaAperturaInventarioCommerciante evento) {
        Logger.log(() -> headerEvento(evento) + evento.getNegozio());
    }

    private void onEventoRichiestaAperturaInventarioFornitore(RichiestaAperturaInventarioFornitore evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoRichiestaAperturaInventarioGruppo(RichiestaAperturaInventarioGruppo evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoRichiestaChiusuraFinestraCombattimento(InternoRichiestaChiusuraFinestraCombattimento evento) {
        Logger.log(() -> headerEvento(evento) + "Chiusa");
    }

    private void onEventoRichiestaPrelievoArtefatto(InternoPrelievoArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(() -> headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di prelevare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") da "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaRefreshUI(InternoRichiestaRefreshUI evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoRichiestaReinizializzazioneUI(InternoRichiestaReinizializzazioneUI evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoRichiestaSelezioneSlotPerRilettura(RichiestaSelezioneSlotPerRilettura evento) {
        Logger.log(() -> headerEvento(evento) + "Salvataggi disponibili: " + evento.getSalvataggiDisponibili()
                .stream().map(TestataSalvataggio::getId).map(Comando::name).collect(Collectors.joining(", ")));
    }

    private void onEventoRichiestaSelezioneSlotPerSalvataggio(RichiestaSelezioneSlotPerSalvataggio evento) {
        Logger.log(() -> headerEvento(evento) + "Salvataggi disponibili: " + evento.getSalvataggiDisponibili()
                .stream().map(TestataSalvataggio::getId).map(Comando::name).collect(Collectors.joining(", ")));
    }

    private void onEventoRichiestaVisualizzazioneMappa(RichiestaVisualizzazioneMappa evento) {
        Logger.log(() -> headerEvento(evento));
    }

    private void onEventoRichiestaStoccaggioArtefatto(InternoStoccaggioArtefatto evento) {
        OggettoConPeso oggetto = evento.getOggettoDaSpostare();
        Logger.log(() -> headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di stoccare "
                + nomeOggetto(oggetto) + " (peso: " + oggetto.getPeso() + ") su "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRichiestaTesto(RichiestaTesto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta: " + evento.getRichiesta());
    }

    private void onEventoRichiestaVenditaArtefatto(InternoVenditaArtefatto evento) {
        OggettoConCosto oggetto = evento.getOggettoDaSpostare();
        Logger.log(() -> headerEvento(evento) + formattaParte(evento.getParteAttiva()) + " richiede di vendere "
                + nomeOggetto(oggetto) + " (costo: " + oggetto.getCostoAcquisto() + ") a "
                + formattaParte(evento.getParteRemota()));
    }

    private void onEventoRifiutoAcquistoArtefatto(NotificaRifiutoAcquistoArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoRifiutoAcquistoConsumabile(NotificaRifiutoAcquistoConsumabile evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getEventoRichiestaAcquistoConsumabile().getUuid());
    }

    private void onEventoRifiutoPrelievoArtefatto(NotificaRifiutoPrelievoArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoRifiutoStoccaggioArtefatto(NotificaRifiutoStoccaggioArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoRifiutoVenditaArtefatto(NotificaRifiutoVenditaArtefatto evento) {
        Logger.log(() -> headerEvento(evento) + "Richiesta UUID " + evento.getUuidRichiesta());
    }

    private void onEventoUscitaDalGioco(NotificaUscitaDalGioco evento) {
        Logger.log(() -> headerEvento(evento) + "Codice " + evento.getCodice() + (evento.getMotivo() == null ? "" : ": " + evento.getMotivo()));
    }

    private void onEventoSelezioneConfermaUscita(RichiestaUscitaDalGioco evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoSelezioneDirezione(RichiestaSelezioneDirezione evento) {
        Logger.log(() -> headerEvento(evento) + evento.getPossibilita());
    }

    private void onEventoSelezioneIncantesimoDaLanciare(RichiestaSelezioneIncantesimoDaLanciare evento) {
        Logger.log(() -> headerEvento(evento) + evento.getPossibilita());
    }

    private void onEventoSelezioneSiNo(RichiestaSelezioneSiNo evento) {
        Logger.log(() -> String.format(Locale.ROOT, "%s - %s", new Date(), evento.getTipoEvento()));
    }

    private void onEventoFaseDiGioco(InternoFaseDiGioco evento) {
        Logger.log(() -> headerEvento(evento) + evento.getFase().toString());
    }

    private void onEventoVariazioneEffettoDiStato(NotificaVariazioneEffettoDiStatoPersonaggio evento) {
        VistaPersonaggio p = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Tipo: %s, EffettoDiStatoMD: %s - ",
                evento.getTipo(), evento.getEffetto()) + formattaStatistichePersonaggio(p));
    }

    public void onEventoVariazionePreziosi(NotificaVariazioneDisponibilitaPreziosi evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneIncantesimi(NotificaVariazioneDisponibilitaIncantesimi evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Tipo: %s, Variazione: %d -> %d",
                evento.getClasseIncantesimo(), evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazioneMappa(NotificaVariazioneConoscenzaMappa evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: (%d, %d) -> (%d, %d)",
                evento.getDaX(), evento.getDaY(), evento.getAX(), evento.getAY()));
    }

    public void onEventoVariazioneMonete(NotificaVariazioneDisponibilitaMonete evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagia(NotificaVariazioneDisponibilitaPozioniMagia evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniMagiaGrandi(NotificaVariazioneDisponibilitaPozioniMagiaGrandi evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniAumentoLivello(NotificaVariazioneDisponibilitaPozioniAumentoLivello evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSalute(NotificaVariazioneDisponibilitaPozioniSalute evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePozioniSaluteGrandi(NotificaVariazioneDisponibilitaPozioniSaluteGrandi evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePunti(NotificaVariazionePunteggio evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    public void onEventoVariazionePuntiEsperienza(NotificaVariazionePuntiEsperienzaPersonaggio evento) {
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Variazione: %d -> %d",
                evento.getValorePrecedente(), evento.getNuovoValore()));
    }

    private void onEventoVariazioneStatistichePersonaggio(NotificaVariazioneStatistichePersonaggio evento) {
        VistaPersonaggio p = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Attributo: %s, Variazione: %7.2f -> %7.2f - ",
                evento.getTipoAttributo(), evento.getValorePrecedente(), evento.getNuovoValore()) +
                formattaStatistichePersonaggio(p));
    }

    private void onEventoVariazioneStatoVitalePersonaggio(NotificaVariazioneStatoVitalePersonaggio evento) {
        VistaPersonaggio p = evento.getPersonaggio();
        Logger.log(() -> headerEvento(evento) + String.format(Locale.ROOT, "Stato: -> %s, Causa trapasso: -> %s - ",
                p.isVivo() ? "Vivo" : "Morto", p.getCausaTrapasso()) + formattaStatistichePersonaggio(p));
    }

    //--- Metodi generali di utilità

    private String formattaStatistichePersonaggio(VistaPersonaggio p) {
        return String.format(Locale.ROOT, "UUID: %s,Nome: %17s, Livello: %2d, Salute: %3d/%3d; Magia: %3d/%3d; Forza: %3d; Destrezza: %3d; Costituzione: %3d; Intelligenza: %3d; Saggezza: %3d; Carisma: %3d; Fortuna: %3d",
                p.getUuid(), p.getNomeSingolare(), p.getLivello(), p.getSalute(), p.getSaluteMassima(), p.getMagia(), p.getMagiaMassima(),
                p.get(TipoAttributo.FORZA), p.get(TipoAttributo.DESTREZZA), p.get(TipoAttributo.COSTITUZIONE), p.get(TipoAttributo.INTELLIGENZA), p.get(TipoAttributo.SAGGEZZA), p.get(TipoAttributo.CARISMA), p.get(TipoAttributo.FORTUNA));
    }

    private String formattaModificatoreAttributo(ModificatoreAttributoMD modificatore) {
        return String.format(Locale.ROOT, "TipoAttributo: %s, TipoModificatore: %s, Quantità: %5f, Durata: %s %d, Bersaglio: %s, Note: %s - ",
                modificatore.getTipoAttributo(), modificatore.getTipoModificatoreAttributo(), modificatore.getQuantita(),
                modificatore.getUnitaDurata(), modificatore.getDurata(), modificatore.getBersaglio(), modificatore.getNote());
    }

    private String headerEvento(EventoBase evento) {
        return String.format(Locale.ROOT, "%s - %s - ", new Date(), evento.getTipoEvento());
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
