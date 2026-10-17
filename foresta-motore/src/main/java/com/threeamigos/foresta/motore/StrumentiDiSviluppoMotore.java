package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaRefreshUI;
import com.threeamigos.foresta.interfacce.StrumentiDiSviluppo;
import com.threeamigos.foresta.missioni.FabbricaMissioni;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.IncaricoInCitta;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.oggetti.leggendari.CatalogoLeggendari;
import com.threeamigos.foresta.oggetti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gli strumenti della finestra di prova (vedi StrumentiDiSviluppo) sul gruppo e sulle missioni della partita in corso.
 * Non tiene stato: a ogni chiamata guarda il gruppo e il registro delle missioni di adesso.
 *
 * @author Stefano Reksten
 */
public class StrumentiDiSviluppoMotore implements StrumentiDiSviluppo {

    // Le missioni che si offrono in locanda; le altre (incarichi e leggende) si offrono in città
    private static final Set<MissionePredefinita> IN_LOCANDA = EnumSet.of(
            MissionePredefinita.LA_LEGGENDA_DEL_LOCANDIERE,
            MissionePredefinita.NESSUN_BOCCALE_LASCIATO_INDIETRO,
            MissionePredefinita.NON_SPARATE_SUL_PIANISTA,
            MissionePredefinita.IL_VIAGGIO_DELLA_MOGLIE,
            MissionePredefinita.LA_BENEDIZIONE,
            MissionePredefinita.RICHIESTA_LOCANDIERE);

    private static final Set<MissionePredefinita> IN_CITTA_SENZA_INCARICO = EnumSet.of(
            MissionePredefinita.LA_LEGGENDA_DELL_ARMAIOLO,
            MissionePredefinita.LA_LEALTA);

    // Dove parte ogni missione: non cambia mai, e per saperlo di un incarico serve costruirlo
    private static final Map<MissionePredefinita, Optional<DovePartono>> DOVE_PARTONO = new ConcurrentHashMap<>();

    private static final List<TipoAttributo> MODIFICABILI = Collections.unmodifiableList(Arrays.asList(
            TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE, TipoAttributo.INTELLIGENZA,
            TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA,
            TipoAttributo.SALUTE, TipoAttributo.MAGIA, TipoAttributo.STANCHEZZA));

    @Override
    public int getValoreBase(String uuidPersonaggio, TipoAttributo attributo) {
        return (int) personaggio(uuidPersonaggio).getModelloDati().get(attributo);
    }

    @Override
    public List<TipoAttributo> getAttributiModificabili() {
        return MODIFICABILI;
    }

    @Override
    public void impostaAttributo(String uuidPersonaggio, TipoAttributo attributo, int valore) {
        if (!MODIFICABILI.contains(attributo)) {
            throw new IllegalArgumentException("L'attributo " + attributo + " non si può impostare");
        }
        Personaggio personaggio = personaggio(uuidPersonaggio);
        int nuovo = Math.max(0, valore);
        if (attributo == TipoAttributo.SALUTE) {
            nuovo = Math.min(nuovo, personaggio.getSaluteMassima());
        } else if (attributo == TipoAttributo.MAGIA) {
            nuovo = Math.min(nuovo, personaggio.getMagiaMassima());
        }
        personaggio.getModelloDati().set(attributo, nuovo);
        aggiornaLaUI();
    }

    @Override
    public void impostaVivo(String uuidPersonaggio, boolean vivo) {
        Personaggio personaggio = personaggio(uuidPersonaggio);
        if (personaggio.isVivo() == vivo) {
            return;
        }
        if (vivo) {
            personaggio.resuscita();
        } else {
            personaggio.muore("Fatto morire dalla finestra di prova");
        }
        aggiornaLaUI();
    }

    @Override
    public void impostaLivello(String uuidPersonaggio, int livello) {
        PersonaggioMD md = personaggio(uuidPersonaggio).getModelloDati();
        int nuovo = Math.max(1, Math.min(livello, Costanti.MASSIMO_LIVELLO));
        md.setLivello(nuovo);
        md.setEsperienza(GestoreProgressione.getXpNecessariPerLivello(nuovo));
        aggiornaLaUI();
    }

    @Override
    public void aggiungiPuntiAbilita(String uuidPersonaggio, int punti) {
        PersonaggioMD md = personaggio(uuidPersonaggio).getModelloDati();
        md.setPuntiAbilitaDisponibili(Math.max(0, md.getPuntiAbilitaDisponibili() + punti));
        aggiornaLaUI();
    }

    @Override
    public List<VoceMissione> getMissioni(DovePartono dove) {
        List<VoceMissione> voci = new ArrayList<>();
        for (MissionePredefinita classe : MissionePredefinita.values()) {
            if (classe.isPredefinita() && doveParte(classe) == dove) {
                Missione missione = trova(classe);
                voci.add(new VoceMissione(classe, nome(classe, missione), descrizione(missione), stato(missione), isForzabile(missione)));
            }
        }
        return voci;
    }

    @Override
    public void forzaPartenza(MissionePredefinita classe) {
        Missione missione = trova(classe);
        if (!isForzabile(missione)) {
            throw new IllegalStateException("La missione " + classe + " non si può forzare adesso");
        }
        DovePartono dove = doveParte(classe);
        ((MissioneAPassi) missione).forzaPartenza(() -> siTrovaIn(dove));
        aggiornaLaUI();
    }

    @Override
    public List<OggettoSpeciale> getOggettiSpeciali() {
        List<OggettoSpeciale> oggetti = new ArrayList<>();
        for (String codice : Cofano.opzioniOggettoSpeciale()) {
            oggetti.add(new OggettoSpeciale(codice, descrizione(codice)));
        }
        return oggetti;
    }

    @Override
    public int getScorta(Scorta scorta) {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        switch (scorta) {
            case MONETE:
                return gruppo.getMonete();
            case PREZIOSI:
                return gruppo.getPreziosi();
            case POZIONI_SALUTE:
                return gruppo.getPozioniSalute();
            case POZIONI_SALUTE_GRANDI:
                return gruppo.getPozioniSaluteGrande();
            case POZIONI_MAGIA:
                return gruppo.getPozioniMagia();
            case POZIONI_MAGIA_GRANDI:
                return gruppo.getPozioniMagiaGrande();
            case POZIONI_AUMENTO_LIVELLO:
                return gruppo.getPozioniAumentoLivello();
            default:
                throw new IllegalArgumentException(String.valueOf(scorta));
        }
    }

    @Override
    public void impostaScorta(Scorta scorta, int valore) {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        int differenza = Math.max(0, valore) - getScorta(scorta);
        switch (scorta) {
            case MONETE:
                gruppo.addMonete(differenza);
                break;
            case PREZIOSI:
                gruppo.addPreziosi(differenza);
                break;
            case POZIONI_SALUTE:
                gruppo.addPozioniSalute(differenza);
                break;
            case POZIONI_SALUTE_GRANDI:
                gruppo.addPozioniSaluteGrande(differenza);
                break;
            case POZIONI_MAGIA:
                gruppo.addPozioniMagia(differenza);
                break;
            case POZIONI_MAGIA_GRANDI:
                gruppo.addPozioniMagiaGrande(differenza);
                break;
            case POZIONI_AUMENTO_LIVELLO:
                gruppo.addPozioniAumentoLivello(differenza);
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(scorta));
        }
        aggiornaLaUI();
    }

    @Override
    public int getIncantesimi(TipoIncantesimo classe) {
        return GruppoGiocatore.getIstanza().getIncantesimi(classe);
    }

    @Override
    public void impostaIncantesimi(TipoIncantesimo classe, int valore) {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        gruppo.addIncantesimi(classe, Math.max(0, valore) - gruppo.getIncantesimi(classe));
        aggiornaLaUI();
    }

    @Override
    public void aggiungiEsperienzaAlGruppo(int punti) {
        GruppoGiocatore.getIstanza().addPuntiEsperienza(punti);
        aggiornaLaUI();
    }

    @Override
    public int getLivelloDelMondo() {
        return Statistiche.getLivello();
    }

    @Override
    public int getEsperienzaDelMondo() {
        return Statistiche.getPuntiEsperienza();
    }

    @Override
    public void impostaLivelloDelMondo(int livello) {
        Statistiche.impostaLivelloDelMondo(Math.max(1, Math.min(livello, Costanti.MASSIMO_LIVELLO)));
        aggiornaLaUI();
    }

    private static String descrizione(String codice) {
        switch (codice) {
            case Cofano.OGGETTO_SPECIALE_CASUALE:
                return "Come sempre (a caso)";
            case Cofano.OGGETTO_SPECIALE_NESSUNO:
                return "Nessun oggetto speciale";
            case Cofano.OGGETTO_SPECIALE_INGREDIENTE:
                return "Un ingrediente magico";
            case Cofano.OGGETTO_SPECIALE_ARTEFATTO:
                return "Un artefatto normale";
            case Cofano.OGGETTO_SPECIALE_AGGIORNAMENTO:
                return "Un aggiornamento";
            default:
                String chiave = codice.substring(Cofano.PREFISSO_LEGGENDARIO.length());
                String nome = CatalogoLeggendari.getLeggendario(chiave).map(OggettoLeggendario::getNomeBreve).orElse(chiave);
                return "Leggendario: " + nome;
        }
    }

    /**
     * Dove si offre la missione: le leggende e le storie elencate in locanda, gli incarichi e il resto in città.
     */
    static DovePartono doveParte(MissionePredefinita classe) {
        return DOVE_PARTONO.computeIfAbsent(classe, StrumentiDiSviluppoMotore::calcolaDoveParte).orElse(null);
    }

    private static Optional<DovePartono> calcolaDoveParte(MissionePredefinita classe) {
        if (IN_LOCANDA.contains(classe)) {
            return Optional.of(DovePartono.LOCANDA);
        }
        if (IN_CITTA_SENZA_INCARICO.contains(classe) || FabbricaMissioni.crea(classe) instanceof IncaricoInCitta) {
            return Optional.of(DovePartono.CITTA);
        }
        return Optional.empty();
    }

    private static boolean siTrovaIn(DovePartono dove) {
        TipoLocazione tipo = GruppoGiocatore.getIstanza().getTipoLocazioneCorrente();
        if (tipo == null) {
            return false;
        }
        return dove == DovePartono.LOCANDA
                ? tipo == TipoLocazione.LOCANDA
                : tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA;
    }

    private static Missione trova(MissionePredefinita classe) {
        for (Missione missione : RegistroMissioni.getMissioniNonCompletate()) {
            if (missione.getClasse() == classe) {
                return missione;
            }
        }
        return null;
    }

    private static String nome(MissionePredefinita classe, Missione missione) {
        return missione != null ? missione.getNome() : classe.name();
    }

    /**
     * La descrizione della missione, o null se non ne ha. Una missione che non è ancora partita può non saperla scrivere (la
     * descrizione di IlTorneo cita il leggendario in palio, che sceglie solo partendo): vale come nessuna descrizione.
     */
    private static String descrizione(Missione missione) {
        if (missione == null) {
            return null;
        }
        try {
            String descrizione = missione.getDescrizione();
            return descrizione == null || descrizione.trim().isEmpty() ? null : descrizione.trim();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String stato(Missione missione) {
        if (missione == null) {
            // Non è più fra quelle da fare: completata o fallita
            return "finita";
        }
        if (missione.isFallita()) {
            return "fallita";
        }
        if (missione.isCompleta()) {
            return "completata";
        }
        if (missione.isAttiva()) {
            return "in corso";
        }
        if (missione instanceof MissioneAPassi && ((MissioneAPassi) missione).isPartenzaForzata()) {
            return "partenza forzata";
        }
        return "da cominciare";
    }

    private static boolean isForzabile(Missione missione) {
        return missione instanceof MissioneAPassi && !missione.isAttiva() && !missione.isCompleta() && !missione.isFallita()
                && !((MissioneAPassi) missione).isPartenzaForzata();
    }

    private static Personaggio personaggio(String uuid) {
        for (Personaggio personaggio : GruppoGiocatore.getIstanza().getPersonaggi()) {
            if (personaggio.getModelloDati().getUuid().equals(uuid)) {
                return personaggio;
            }
        }
        throw new IllegalArgumentException("Nessun personaggio del gruppo ha l'identificativo " + uuid);
    }

    private static void aggiornaLaUI() {
        BusEventi.pubblica(new InternoRichiestaRefreshUI());
    }
}
