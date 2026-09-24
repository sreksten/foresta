package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoEffettoDiStato;
import com.threeamigos.foresta.motore.modellodati.TipoInterazioneElementale;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GradoIncantamento;
import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Stefano Reksten
 */
public class CalcolatoreCombattimento {


    public static int calcolaProbabilitaDiColpire(Personaggio attaccante, Personaggio difensore, SupertipoDanno tipoDanno) {

        // 0. CONTROLLO EFFETTI DI STATO CHE DETERMINANO AUTOMATICAMENTE LA RIUSCITA
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            Logger.log("L'attaccante è STORDITO e non può colpire.");
            return 0;
        }
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO)) {
            Logger.log("Il difensore è ATTERRATO/CONGELATO/STORDITO e viene colpito automaticamente.");
            return 100;
        }

        double attaccoTotale = 0.0d;
        double difesaTotale = 0.0d;

        // BIVIO LOGICO DI BILANCIAMENTO IN BASE AL TIPO DI DANNO
        if (tipoDanno == SupertipoDanno.FISICO) {
            // --- CALCOLO ASSE FISICO ---
            // Attaccante usa la coordinazione occhio-mano (Precisione + Destrezza)
            attaccoTotale = attaccante.getPrecisione() + attaccante.getDestrezza();
            Logger.log(String.format("[FISICO] Attacco totale (PRECISIONE %d + DESTREZZA %d): %f",
                    attaccante.getPrecisione(), attaccante.getDestrezza(), attaccoTotale));

            // Il difensore contrasta fisicamente (Velocità + Destrezza) modificata dalla PARATA
            difesaTotale = difensore.getVelocita() + difensore.getDestrezza() + (difensore.getParata() * 0.5d);
            Logger.log(String.format("[FISICO] Difesa totale (VELOCITA %d + DESTREZZA %d + 50%% PARATA %d): %f",
                    difensore.getVelocita(), difensore.getDestrezza(), difensore.getParata(), difesaTotale));

        } else {
            // --- CALCOLO ASSE MAGICO/ELEMENTALE ---
            // Chi lancia magie si affida al controllo mentale e alla potenza magica (Intelligenza + Precisione)
            attaccoTotale = attaccante.getPrecisione() + attaccante.getIntelligenza();
            Logger.log(String.format("[MAGICO] Attacco totale (PRECISIONE %d + INTELLIGENZA %d): %f",
                    attaccante.getPrecisione(), attaccante.getIntelligenza(), attaccoTotale));

            // Chi subisce magie si affida al filtro difensivo mistico e alla stabilità mentale (Resistenza Magica + Saggezza)
            difesaTotale = difensore.getResistenzaMagica() + difensore.getSaggezza();
            Logger.log(String.format("[MAGICO] Difesa totale (RESISTENZA_MAGICA %d + SAGGEZZA %d): %f",
                    difensore.getResistenzaMagica(), difensore.getSaggezza(), difesaTotale));
        }

        // 3. CONTROLLO EFFETTI DI STATO SULL'ATTACCANTE (CONFUSO / ACCECATO / STANCHEZZA)
        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.CONFUSO)) {
            attaccoTotale = attaccoTotale * (8.0d + Math.min(2, attaccante.getSaggezza() / 20.0d)) / 10.0d;
            Logger.log(String.format("Attacco dopo effetto CONFUSO (mitigato da SAGGEZZA %d): %f", attaccante.getSaggezza(), attaccoTotale));
        }

        if (attaccante.hasEffettoDiStato(TipoEffettoDiStato.ACCECATO)) {
            if (tipoDanno == SupertipoDanno.FISICO) {
                // La cecità devasta la mira fisica
                double penalitaAccecato = (attaccoTotale / 2.0d) * (1.0d - Math.min(1.0d, attaccante.getPercezione() / 100.0d));
                attaccoTotale -= penalitaAccecato;
            } else {
                // Per le magie, la cecità influisce meno perché il mago si guida con la Percezione dei flussi magici
                double penalitaAccecatoMagico = (attaccoTotale / 4.0d) * (1.0d - Math.min(1.0d, attaccante.getPercezione() / 100.0d));
                attaccoTotale -= penalitaAccecatoMagico;
            }
            Logger.log(String.format("Attacco dopo effetto ACCECATO (mitigato da PERCEZIONE %d): %f", attaccante.getPercezione(), attaccoTotale));
        }

        // Penalità di STANCHEZZA sull'attaccante
        attaccoTotale -= attaccante.getStanchezza() * 2.0d;
        Logger.log(String.format("Attacco totale dopo stanchezza (STANCHEZZA %d): %f", attaccante.getStanchezza(), attaccoTotale));


        // 4. APPLICAZIONE DEI MODIFICATORI DI STATO AL DIFENSORE
        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.RALLENTATO)) {
            if (tipoDanno == SupertipoDanno.FISICO) {
                difesaTotale = difesaTotale / 2.0d; // Rallentato distrugge la schivata fisica
            } else {
                // Rallentato influisce pochissimo sulla barriera mistica passiva (Resistenza Magica)
                difesaTotale = difesaTotale * 0.9d;
            }
            Logger.log("Difesa totale dopo effetto RALLENTATO: " + difesaTotale);
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO)) {
            difesaTotale = (difesaTotale * (9.0d + Math.min(1, difensore.getSaggezza() / 20.0d) + Math.min(1.0d, difensore.getCoraggio() / 100.0d)) / 10.0d);
            Logger.log(String.format("Difesa totale dopo effetto SPAVENTATO (mitigato da SAGGEZZA %d e CORAGGIO %d): %f", difensore.getSaggezza(), difensore.getCoraggio(), difesaTotale));
        }

        // Penalità di STANCHEZZA sul difensore (La fatica logora sia i riflessi fisici che la concentrazione magica)
        difesaTotale -= difensore.getStanchezza() * 2.0d;
        Logger.log(String.format("Difesa totale dopo stanchezza (STANCHEZZA %d): %f", difensore.getStanchezza(), difesaTotale));

        // 5. CALCOLO DELLA PROBABILITÀ FINALE DI COLPIRE
        int probabilitaFinale = 75 + (int)((attaccoTotale - difesaTotale) * 2.0d);
        Logger.log("Probabilità finale di colpire: " + probabilitaFinale);

        // Limiti minimi e massimi (Cap) per il bilanciamento
        if (probabilitaFinale < 5) {
            probabilitaFinale = 5;
        } else if (probabilitaFinale > 95) {
            probabilitaFinale = 95;
        }
        Logger.log("Probabilità finale di colpire dopo applicazione dei limiti: " + probabilitaFinale);
        return probabilitaFinale;
    }

    public static boolean colpisce(Personaggio attaccante, Personaggio difensore, SupertipoDanno tipoDanno) {

        int probabilitaFinale = calcolaProbabilitaDiColpire(attaccante, difensore, tipoDanno);

        // 6. TIRO DEL DADO (Generazione numero casuale da 1 a 100)
        int tiroDado = Dado.tira(100);
        Logger.log("Risultato sul COLPIRE (tiro del dado = " + tiroDado + "): " + (tiroDado <= probabilitaFinale));

        return tiroDado <= probabilitaFinale;
    }

    public static DannoRisultante calcolaDannoRisultante(Personaggio attaccante, Personaggio difensore, Arma arma) {
        return calcolaDannoRisultante(attaccante, difensore, arma, 1.0d);
    }

    /**
     * Le fasi di attacco di un turno: l'arma principale e, per chi combatte con due armi, quella nella
     * mano secondaria, al 60% (Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA).
     */
    public static List<FaseDiAttacco> fasiDiAttacco(Personaggio attaccante) {
        List<FaseDiAttacco> fasi = new ArrayList<>();
        fasi.add(new FaseDiAttacco(attaccante.getArmaEquipaggiata(), 1.0d));
        attaccante.getArmaSecondaria()
                .ifPresent(arma -> fasi.add(new FaseDiAttacco(arma, Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA)));
        return fasi;
    }

    /**
     * @param fattore la quota del danno (base e incantamenti) che l'arma fa in questa fase: 1 per l'arma
     *                principale, meno per la seconda arma
     */
    public static DannoRisultante calcolaDannoRisultante(Personaggio attaccante, Personaggio difensore, Arma arma, double fattore) {

        DannoRisultante dannoRisultante = new DannoRisultante(attaccante, difensore);
        TipoDanno tipoDanno = arma.getTipoDanno();
        dannoRisultante.setTipoDanno(tipoDanno);

        // 1. CALCOLO STATISTICHE EFFETTIVE
        // Determina se l'attacco scala su FORZA (Fisico) o INTELLIGENZA (Magico/Elementale)
        int statOffensiva;
        boolean dannoNonFisico = tipoDanno.getSuperTipo() == SupertipoDanno.ELEMENTALE || tipoDanno.getSuperTipo() == SupertipoDanno.MAGICO;
        Logger.log("tipoDanno = " + tipoDanno + ", dannoNonFisico = " + dannoNonFisico);
        if (dannoNonFisico) {
            statOffensiva = attaccante.getIntelligenza();
            Logger.log("statOffensiva (INTELLIGENZA) = " + statOffensiva);
            // La SAGGEZZA potenzia i danni SACRO
            if (tipoDanno == TipoDanno.SACRO) {
                statOffensiva += attaccante.getSaggezza() / 2;
                Logger.log(String.format("statOffensiva modificata da SACRO (%d) = %d", attaccante.getSaggezza(), statOffensiva));
            }
        } else {
            statOffensiva = attaccante.getForza();
            Logger.log("statOffensiva (FORZA) = " + statOffensiva);
        }

        // Determina la difesa del bersaglio (COSTITUZIONE + PARATA per Fisico, RESISTENZA_MAGICA per Magico/Elementale),
        // con le resistenze di elmo, scudo e armatura contro questo tipo di danno
        double statDifensiva = difesaContro(difensore, tipoDanno);

        // 2. MATEMATICA DI BASE DEL DANNO (Con fattore di scala livello arma)
        int dannoBaseArma = arma.getDanni() * arma.getLivello();
        if (isIncantesimo(arma)) {
            // Il danno proprio dell'incantesimo: il resto lo fanno l'INTELLIGENZA e il moltiplicatore magico di chi la lancia
            dannoBaseArma = (int) Math.round(dannoBaseArma * Costanti.INCANTESIMO_FATTORE_DANNI);
        }

        Logger.log(String.format("dannoBaseArma = danniBase %d + livello arma %d = %d", arma.getDanni(), arma.getLivello(), dannoBaseArma));

        // Rapporto di Efficacia dell'Arma per evitare exploit di armi liv. 1 su campioni liv. 20
        double rapportoEfficacia = (double) arma.getLivello() / (double) attaccante.getLivello();
        if (rapportoEfficacia > 1.0d) {
            rapportoEfficacia = 1.0d;
        }
        Logger.log(String.format("rapportoEfficacia (livello arma %d / livello attaccante %d) = %f", arma.getLivello(), attaccante.getLivello(), rapportoEfficacia));

        double contributoEroe = (double)(statOffensiva * attaccante.getLivello()) / 5.0d;
        Logger.log(String.format("contributoEroe (statOffensiva %d * livello attaccante %d / 5 = %f", statOffensiva, attaccante.getLivello(), contributoEroe));
        // Il carattere della classe: il guerriero picchia, il mago incanta (Costanti.*_MOLTIPLICATORE_DANNI_*)
        double moltiplicatoreClasse = dannoNonFisico ? attaccante.getMoltiplicatoreDanniMagici() : attaccante.getMoltiplicatoreDanniFisici();
        double dannoOffensivoGrezzo = (dannoBaseArma + Math.floor(contributoEroe * rapportoEfficacia)) * fattore * moltiplicatoreClasse;
        Logger.log("dannoOffensivoGrezzo = " + dannoOffensivoGrezzo + " (fattore " + fattore + ", moltiplicatore di classe " + moltiplicatoreClasse + ")");

        // 2.5 APPLICAZIONE DEL BONUS BERSERK (Esclusivo ai Guerrieri con FURIA)
        // Solo sul danno fisico (vedi TipoEffettoDiStato.BERSERK): la condizione era negata al
        // contrario, e il bonus valeva solo per gli attacchi elementali e magici.
        if (!dannoNonFisico && attaccante.hasEffettoDiStato(TipoEffettoDiStato.BERSERK)) {
            // Il danno fisico scala con la salute persa: più è ferito, più forte colpisce
            double percentualeSalutePerduta = 1.0d - ((double)attaccante.getSalute() / (double)attaccante.getSaluteMassima());
            dannoOffensivoGrezzo = dannoOffensivoGrezzo * (1.0d + percentualeSalutePerduta * 0.5d);
            Logger.log("dannoOffensivoGrezzo dopo BERSERK = " + dannoOffensivoGrezzo);
        }

        // 3. APPLICAZIONE INTERAZIONI ELEMENTALI E STATI DEL DIFENSORE
        double moltiplicatoreDannoStato = 1.0d;
        boolean criticoAutomatico = false;

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
            Logger.log("difensore ha stato BAGNATO");
            if (tipoDanno == TipoDanno.FULMINE) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.ELETTROCUZIONE);
            } else if (tipoDanno == TipoDanno.GELO) {
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.CONGELATO,
                        calcolaDurataStato(difensore, TipoEffettoDiStato.CONGELATO),
                        calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.CONGELATO));
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.CONGELAMENTO);
            } else if (tipoDanno == TipoDanno.FUOCO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 0.5d;
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.VAPORIZZAZIONE);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.BRUCIATO)) {
            Logger.log("difensore ha stato BRUCIATO");
            if (tipoDanno == TipoDanno.ARIA) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.ALIMENTAZIONE_FIAMMA);
            } else if (tipoDanno == TipoDanno.ACQUA) {
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.ESTINZIONE);
            } else if (tipoDanno == TipoDanno.GELO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.BAGNATO,
                        calcolaDurataStato(difensore, TipoEffettoDiStato.BAGNATO),
                        calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.BAGNATO));
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.SCIOGLIMENTO_TERMICO);
            } else if (tipoDanno == TipoDanno.VELENO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.3d; // Esplosione di gas
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.BRUCIATO);
                dannoRisultante.addEffettoDiStato(TipoEffettoDiStato.AVVELENATO,
                        calcolaDurataStato(difensore, TipoEffettoDiStato.AVVELENATO),
                        calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.AVVELENATO));
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.ESPLOSIONE_DI_GAS);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.CONGELATO)) {
            Logger.log("difensore ha stato CONGELATO");
            if (tipoDanno == TipoDanno.CONTUNDENTE) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 2.0d;
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.CONGELATO);
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.FRANTUMAZIONE_DEL_GHIACCO);
            } else if (tipoDanno == TipoDanno.FUOCO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.CONGELATO);
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.DISGELO_VIOLENTO);
            } else if (tipoDanno.getSuperTipo() == SupertipoDanno.FISICO) {
                statDifensiva = statDifensiva * 1.5d; // Il guscio di ghiaccio fa da scudo ai colpi di lama/punta
            } else if (tipoDanno == TipoDanno.FULMINE) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.3d;
                statDifensiva = statDifensiva * 0.7d;
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.SUPERCONDUZIONE);
            }

        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.MALEDETTO)) {
            Logger.log("difensore ha stato MALEDETTO");
            if (tipoDanno == TipoDanno.NECROTICO) {
                // La SAGGEZZA del difensore riduce l'efficacia dei danni NECROTICO su un bersaglio MALEDETTO
                double moltiplicatoreMaledetto = Math.max(1.0d, 2.0d - (difensore.getSaggezza() / 100.0));
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * moltiplicatoreMaledetto;
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.MIETITURA);
            } else if (tipoDanno == TipoDanno.SACRO) {
                dannoRisultante.rimuoviEffettoDiStato(TipoEffettoDiStato.MALEDETTO);
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.RIGETTO);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.INFETTATO)) {
            Logger.log("difensore ha stato INFETTATO");
            if (tipoDanno == TipoDanno.SACRO) {
                moltiplicatoreDannoStato = moltiplicatoreDannoStato * 1.5d;
                dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.PURIFICAZIONE);
            }
        }

        if (difensore.hasEffettoDiStato(TipoEffettoDiStato.STORDITO) ||
                difensore.hasEffettoDiStato(TipoEffettoDiStato.ATTERRATO)) {
            Logger.log("difensore ha stato STORDITO o ATTERRATO");
            if (tipoDanno == TipoDanno.TAGLIENTE || tipoDanno == TipoDanno.PERFORANTE) {
                Logger.log("tipo danno TAGLIENTE o PERFORANTE, critico automatico");
                criticoAutomatico = true;
                dannoRisultante.setColpoDiGrazia(true);
            }
        }

        // 4. MITIGAZIONE DELLA DIFESA DEL DIFENSORE (Formula Diminishing Returns)
        double fattoreMitigazione = 100.0d / (100.0d + statDifensiva);
        Logger.log("fattoreMitigazione: " + fattoreMitigazione + ", moltiplicatoreDannoStato = " + moltiplicatoreDannoStato);
        double dannoMitigato = Math.floor(dannoOffensivoGrezzo * fattoreMitigazione * moltiplicatoreDannoStato);
        Logger.log("dannoMitigato: " + dannoMitigato);

        // 4.2 --- COMPONENTE ELEMENTALE INCANTATA CUMULATIVA ---
        double dannoElementaleFinale = 0.0d;

        if (arma.isIncantata()) {
            for (Incantamento inc : arma.getIncantamenti()) {
                TipoDanno elementoMagico = inc.getTipoDannoElementale();

                double dannoQuestoIncantamento = dannoIncantamento(attaccante, difensore, inc, arma.getLivello(), fattore, 1.0d);

                // Se il bersaglio era BAGNATO e la spada è di FUOCO, si attiva l'interazione Vaporizzazione
                if (elementoMagico == TipoDanno.FUOCO && difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
                    dannoRisultante.addInterazioneElementale(TipoInterazioneElementale.VAPORIZZAZIONE);
                }

                // SOMMA i danni invece di sovrascriverli
                dannoElementaleFinale += dannoQuestoIncantamento;
                Logger.log(String.format("[INCANTAMENTO] Danno bonus accumulato da %s: %.2f", elementoMagico, dannoQuestoIncantamento));
            }
        }

        // 4.3 --- LIBRO MAGICO: bonus al danno degli incantesimi di chi lo porta ---
        if (isIncantesimo(arma)) {
            Optional<Artefatto> libro = libroMagico(attaccante);
            if (libro.isPresent()) {
                // Il bonus del libro è danno dell'incantesimo: prende tutto il moltiplicatore magico della classe
                double dannoLibro = dannoIncantamento(attaccante, difensore, bonusLibroMagico(libro.get(), tipoDanno),
                        libro.get().getLivello(), 1.0d, attaccante.getMoltiplicatoreDanniMagici());
                dannoElementaleFinale += dannoLibro;
                Logger.log(String.format("[LIBRO MAGICO] Danno bonus: %.2f", dannoLibro));
            }
        }

        // Il danno totale combinato dell'attacco
        double dannoTotaleCombinato = dannoMitigato + dannoElementaleFinale;

        // 5. DETERMINAZIONE DEL COLPO CRITICO (Come capire se il colpo raddoppia)
        // Formula di base: 5% fisso + 1% per ogni punto statistica CRITICO dell'attaccante,
        // contrastata dalla FORTUNA del difensore.

        int intuitoCritico = attaccante.getCritico();
        int contromisuraCritico = difensore.getFortuna();

        double probabilitaCritico = Math.max(0.0d, 5.0d + intuitoCritico - contromisuraCritico);
        Logger.log(String.format("probabilitaCritico: %f = MAX(0, 5 + CRITICO attaccante %d - FORTUNA difensore %d)", probabilitaCritico, intuitoCritico, contromisuraCritico));

        double tiroDadoCritico = Dado.tira(100);
        Logger.log("tiroDadoCritico: " + tiroDadoCritico);
        if (tiroDadoCritico <= probabilitaCritico || criticoAutomatico) {
            // Il colpo critico raddoppia il danno finale calcolato
            dannoTotaleCombinato = dannoTotaleCombinato * 2.0d;
            Logger.log("dannoTotaleCombinato raddoppiato per CRITICO: " + dannoTotaleCombinato);
        }

        // 6. APPLICAZIONE DEI NUOVI STATI NATIVI (Proc Rate)
        if (dannoTotaleCombinato > 0.0d) {
            Logger.log("Applicazione stati nativi per arma ibrida");

            // --- TRAGUARDO 1: Stato dell'arma Principale (es. TAGLIENTE ➔ SANGUINAMENTO) ---
            if (tipoDanno.hasEffettiDiStato()) {
                double probStatoFisico = ((dannoMitigato * 100.0d) / difensore.getForza()) + (attaccante.getFuria() * 2.0d);
                if (Dado.tira(100) <= probStatoFisico) {
                    TipoEffettoDiStato effettoFisico = tipoDanno.getTipoEffettoDiStatoCasuale();
                    if (effettoFisico != null) {
                        dannoRisultante.addEffettoDiStato(effettoFisico, calcolaDurataStato(difensore, effettoFisico),
                                calcolaDannoPeriodico(attaccante, difensore, effettoFisico));
                        Logger.log("Arma applica stato fisico: " + effettoFisico);
                    }
                }
            }

            // --- TRAGUARDO 2: Stati degli Incantamenti (Multipli e Indipendenti) ---
            if (arma.isIncantata()) {
                for (Incantamento incantamento : arma.getIncantamenti()) {
                    if (incantamento.getTipoDannoElementale().hasEffettiDiStato()) {
                        TipoDanno elemento = incantamento.getTipoDannoElementale();

                        // Ricalcola il danno specifico di QUESTO incantamento per un Proc Rate preciso
                        double dannoQuestoIncantamentoProc = dannoIncantamento(attaccante, difensore, incantamento,
                                arma.getLivello(), fattore, 1.0d);

                        // La probabilità del proc magico si basa sul danno reale di QUESTO elemento e sulla statistica MAGIA
                        double probStatoMagico = ((dannoQuestoIncantamentoProc * 100.0d) / difensore.getForza()) + (attaccante.getMagia() * 2.0d);
                        if (Dado.tira(100) <= probStatoMagico) {
                            TipoEffettoDiStato effettoMagico = elemento.getTipoEffettoDiStatoCasuale();
                            if (effettoMagico != null) {
                                dannoRisultante.addEffettoDiStato(effettoMagico, calcolaDurataStato(difensore, effettoMagico),
                                        calcolaDannoPeriodico(attaccante, difensore, effettoMagico));
                                Logger.log("L'incantamento applica stato magico: " + effettoMagico.name());
                            }
                        }
                    }
                }
            }

            int dannoFinale = Math.max(1, (int)dannoTotaleCombinato);
            dannoRisultante.setDanno(dannoFinale);
        }

        return dannoRisultante;
    }

    /**
     * Il danno di un incantamento, mitigato dalla difesa del bersaglio contro il suo tipo di danno.
     * La parte fissa scala con il livello dell'oggetto (e per moltiplicatoreParteFissa), quella percentuale
     * con l'INTELLIGENZA e il moltiplicatore di danno magico di chi colpisce: una spada di fuoco rende di più
     * in mano a un elfo che a un guerriero. Contro un bersaglio BAGNATO il fuoco fa la metà.
     */
    private static double dannoIncantamento(Personaggio attaccante, Personaggio difensore, Incantamento incantamento,
                                            int livelloOggetto, double fattore, double moltiplicatoreParteFissa) {
        double dannoGrezzo = ((incantamento.getDannoBonusFisso() * livelloOggetto * moltiplicatoreParteFissa) +
                (attaccante.getIntelligenza() * incantamento.getCoefficienteScala() * attaccante.getMoltiplicatoreDanniMagici())) * fattore;
        double mitigazione = 100.0d / (100.0d + difesaContro(difensore, incantamento.getTipoDannoElementale()));
        double danno = Math.floor(dannoGrezzo * mitigazione);
        if (incantamento.getTipoDannoElementale() == TipoDanno.FUOCO && difensore.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO)) {
            danno = danno * 0.5d;
        }
        return danno;
    }

    /**
     * La difesa del bersaglio contro un tipo di danno T:
     * (difesa base + Σ fisso_T × livello del pezzo) × (1 + Σ percentuale_T / 2), sugli incantamenti di tipo T
     * di elmo, scudo e armatura. La difesa base è COSTITUZIONE + PARATA per il danno fisico,
     * RESISTENZA_MAGICA per quello elementale o magico.
     */
    public static double difesaContro(Personaggio difensore, TipoDanno tipoDanno) {
        boolean fisico = tipoDanno.getSuperTipo() == SupertipoDanno.FISICO;
        double difesaBase = fisico ? difensore.getCostituzione() + difensore.getParata() : difensore.getResistenzaMagica();
        double fisso = 0.0d;
        double percentuale = 0.0d;
        for (Artefatto pezzo : difensore.getInventario()) {
            if (!isPezzoDifensivo(pezzo)) {
                continue;
            }
            for (Incantamento incantamento : pezzo.getIncantamenti()) {
                if (incantamento.getTipoDannoElementale() == tipoDanno) {
                    fisso += incantamento.getDannoBonusFisso() * pezzo.getLivello();
                    percentuale += incantamento.getCoefficienteScala() * Costanti.RESISTENZA_FATTORE_PERCENTUALE;
                }
            }
        }
        double difesa = (difesaBase + fisso) * (1.0d + percentuale);
        Logger.log(String.format("difesa contro %s = (base %.0f + fisso %.0f) x (1 + %.3f) = %.2f",
                tipoDanno, difesaBase, fisso, percentuale, difesa));
        return difesa;
    }

    private static boolean isPezzoDifensivo(Artefatto artefatto) {
        SupertipoArtefatto supertipo = artefatto.getTipo().getSupertipo();
        return supertipo == SupertipoArtefatto.SCUDO || supertipo == SupertipoArtefatto.ELMO
                || supertipo == SupertipoArtefatto.ARMATURA;
    }

    /**
     * Le pergamene di incantesimi e il dardo arcano: il loro danno segue le regole degli incantesimi
     * (INCANTESIMO_FATTORE_DANNI, bonus del libro magico)
     */
    private static boolean isIncantesimo(Arma arma) {
        return arma instanceof IncantesimoMalefico || arma instanceof DardoArcano;
    }

    private static Optional<Artefatto> libroMagico(Personaggio personaggio) {
        return personaggio.getInventario().stream()
                .filter(a -> a.getTipo() == TipoArtefatto.LIBRO_MAGICO)
                .findFirst();
    }

    /**
     * Il bonus del libro magico come un incantamento del tipo di danno dell'incantesimo: parte fissa e
     * percentuale del grado adatto al livello del libro, più il 25%.
     */
    static Incantamento bonusLibroMagico(Artefatto libro, TipoDanno tipoDanno) {
        GradoIncantamento grado = GradoIncantamento.perLivello(libro.getLivello());
        double maggiorazione = 1.0d + Costanti.LIBRO_MAGICO_MAGGIORAZIONE;
        return new Incantamento("Libro magico", tipoDanno,
                (int) Math.round(grado.getBonusFisso() * maggiorazione), grado.getCoefficiente() * maggiorazione);
    }

    private static int calcolaDurataStato(Personaggio difensore, TipoEffettoDiStato stato) {
        // Gestione immediata dell'unico stato a 1 turno fisso (Azione per rialzarsi)
        if (stato == TipoEffettoDiStato.ATTERRATO) {
            return 1;
        }

        // Lo stato speciale BERSERK non ha turni, termina al cambio mappa
        if (stato == TipoEffettoDiStato.BERSERK) {
            return 1;
        }

        int durataBase;
        int riduzione = 0;

        // BIVIO LOGICO DI BILANCIAMENTO GENERALE
        switch (stato) {

            case STORDITO:
                // --- CATEGORIA: TRAUMA FISICO (Base 2 turni, contrasta COSTITUZIONE) ---
                durataBase = 2;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getCostituzione()) / 3.0d);
                break;

            case CONGELATO:
                // --- CATEGORIA: BLOCCO MISTICO (Base 3 turni, contrasta SAGGEZZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getSaggezza()) / 3.0d);
                break;

            case RALLENTATO:
                // --- CATEGORIA: INTRALCIO MOTORIO (Base 3 turni, contrasta DESTREZZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getDestrezza()) / 3.0d);
                break;

            case IMMOBILIZZATO:
                // --- CATEGORIA: MORSA FISICA/TERRESTRE (Base 3 turni, contrasta FORZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getForza()) / 3.0d);
                break;

            case SPAVENTATO:
                // --- CATEGORIA: ATTACCO AL MORALE (Base 3 turni, contrasta CORAGGIO secondario) ---
                durataBase = 3;
                // Moltiplichiamo il moltiplicatore di Coraggio * 10 per portarlo sulla stessa scala degli attributi primari
                riduzione = (int) Math.floor(Math.sqrt(difensore.getCoraggio() * 10.0d) / 3.0d);
                break;

            case SANGUINAMENTO:
            case AVVELENATO:
            case INFETTATO:
                // --- CATEGORIA: TOSSINE E DEGENERAZIONI ORGANICHE (Base 4 turni, contrasta COSTITUZIONE) ---
                durataBase = 4;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getCostituzione()) / 3.0d);
                break;

            case BRUCIATO:
                // --- CATEGORIA: INCENDIO ELEMENTALE (Base 4 turni, contrasta SAGGEZZA/Controllo Energetico) ---
                durataBase = 4;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getSaggezza()) / 3.0d);
                break;

            case CONFUSO:
            case ACCECATO:
            case ASSORDATO:
            case SILENZIATO:
            case MALEDETTO:
            case BAGNATO:
            default:
                // --- CATEGORIA: DEBILITAZIONI MENTALI E SENSORIALI BASE (Base 3 turni, contrasta SAGGEZZA) ---
                durataBase = 3;
                riduzione = (int) Math.floor(Math.sqrt(difensore.getSaggezza()) / 3.0d);
                break;
        }

        // CALCOLO FINALE E COMPUTAZIONE DEI LIMITI (CAP)
        int durataFinale = durataBase - riduzione;

        // BINDAGGIO ANTIPANICO: Qualsiasi stato applicato con successo deve durare
        // almeno 1 turno (il turno corrente in cui viene consumato/subito)
        // e non può mai superare la sua durata base naturale per evitare il perma-block.
        return Math.max(1, Math.min(durataBase, durataFinale));
    }

    /**
     * Calcola il danno ad ogni tick
     */
    public static int calcolaDannoPeriodico(Personaggio attaccante, Personaggio difensore,
                                            TipoEffettoDiStato stato) {

        double dannoGrezzo = 0.0d;
        double difesaFiltro = 0.0d;
        int livelloAttaccante = attaccante.getLivello();

        switch (stato) {
            case SANGUINAMENTO:
                // Danno fisico: scala su FORZA dell'attaccante e ignora metà COSTITUZIONE
                dannoGrezzo = (attaccante.getForza() * livelloAttaccante) / 10.0d;
                difesaFiltro = difensore.getCostituzione() / 4.0d;
                Logger.log(String.format("[DoT] Elaborazione SANGUINAMENTO su %s. Attacco: %.2f, Difesa Filtro: %.2f",
                        difensore.getNome(), dannoGrezzo, difesaFiltro));
                break;

            case BRUCIATO:
                // Danno elementale: scala su INTELLIGENZA dell'attaccante, contrasta RESISTENZA MAGICA
                dannoGrezzo = (attaccante.getIntelligenza() * livelloAttaccante) / 10.0d;
                difesaFiltro = difensore.getResistenzaMagica() / 2.0d;
                Logger.log(String.format("[DoT] Elaborazione BRUCIATO su %s. Attacco: %.2f, Difesa Filtro: %.2f",
                        difensore.getNome(), dannoGrezzo, difesaFiltro));
                break;

            case AVVELENATO:
                // Danno elementale: scala su INTELLIGENZA dell'attaccante, contrasta COSTITUZIONE
                dannoGrezzo = (attaccante.getIntelligenza() * livelloAttaccante) / 10.0d;
                difesaFiltro = difensore.getCostituzione() / 2.0d;
                Logger.log(String.format("[DoT] Elaborazione AVVELENATO su %s.", difensore.getNome()));
                break;

            case INFETTATO:
                // Danno necrotico: scala su INTELLIGENZA, contrasta RESISTENZA MAGICA
                dannoGrezzo = (attaccante.getIntelligenza() * livelloAttaccante) / 12.0d; // Leggermente più lento ma blocca le cure
                difesaFiltro = difensore.getResistenzaMagica() / 2.0d;
                Logger.log(String.format("[DoT] Elaborazione INFETTATO su %s.", difensore.getNome()));
                break;

            default:
                return 0;
        }

        // Calcolo finale del danno del singolo tick del timer
        return (int) Math.max(1, Math.floor(dannoGrezzo - difesaFiltro));
    }
}
