# Debolezze e resistenze dei personaggi

Quanto danno di un certo tipo subisce un personaggio dipende dalla sua **classe** (`TipoPersonaggio`) e dalle sue **categorie**
(`CategoriaPersonaggio`: umanoide, bestia, draconico, non morto, volante, gigantesco, ibrido, costrutto).
Le interazioni con gli *stati* del difensore (un bagnato e il fulmine) sono un'altra cosa: vedi
`interazioni_effetti_di_stato.md`. Il discorso sulle tattiche che le usano è in
`tattiche_di_combattimento_automatiche.md` §6.3.

## 1. Il modello

- Una **tabella `tipo di danno → moltiplicatore`** per ogni categoria e per ogni classe (`CategoriaPersonaggio` e
- TipoPersonaggio`). I tipi che mancano valgono 1.
- Il moltiplicatore di un personaggio (`Personaggio.getMoltiplicatoreDannoSubito(tipoDanno)`) è il **prodotto** del valore della sua
  classe e di quelli delle sue categorie. Per esempio un Fantasma è NON_MORTO (necrotico ×0,5) e anche, per la sua classe, necrotico ×0:
  il risultato è ×0.

  | Valore | Significato |
  | :--- | :--- |
  | 0 | **immune**: quel danno non lo sente proprio; si salta tutto (danno, stati nativi, reazioni con gli stati) |
  | tra 0 e 1 | **resistente**: il danno si moltiplica per il valore e gli effetti scattano con quella probabilità |
  | 1 | niente |
  | maggiore di 1 | **debole** (al massimo 2): più danno, ma non più effetti |

- Il danno del colpo si moltiplica per il valore dopo la difesa; gli *effetti* (stati nativi del tipo di danno e degli incantamenti,
  modificatori dell'arma, reazioni con gli stati) usano `CalcolatoreCombattimento.fattoreEffetti`, cioè il valore se è minore di 1.
  La durata degli stati non cambia. Gli incantamenti dell'arma seguono la stessa tabella, ognuno col suo elemento.
- La UI scrive sopra il bersaglio **"Debole a Sacro"**, **"Resiste a Fuoco"**, **"Immune a Aria"**, a ogni colpo in cui il valore non è 1
  (evento `NotificaReazioneDiClassePersonaggio`, testo e colore in `ui/testo/TestoReazioneDiClasse`). Il colore ha dieci sfumature per parte:
  dal grigio verso il **verde** man mano che la debolezza cresce (verde pieno a ×2), dal grigio verso il **rosso** man mano che
  la resistenza cresce, **rosso acceso** per l'immunità.
- Restano a parte, perché non sono tipi di danno, le immunità a un **incantesimo** (Morte non ha un tipo di danno) e a uno **stato**
  (Sanguinamento, Infettato...). Sono dati di classe come i moltiplicatori: stanno in `TipoPersonaggio` (con quelle delle categorie in `CategoriaPersonaggio`: i non
  morti sono immuni a Morte, Veleno, Sanguinamento e Infettato) e si leggono con `TipoPersonaggio.isImmuneAIncantesimo` e `isImmuneAEffetto`. `PersonaggioBase.isImmuneAIncantesimo`
  e `isImmuneAEffetto` guardano la classe **e** il singolo personaggio (`isImmuneAIncantesimoPerIstanza`, `isImmuneAEffettoPerIstanza`, di norma falsi: il
  Guerriero in berserk non si confonde né si spaventa). Il bestiario (`bestiario.md`) legge le immunità e i moltiplicatori di classe da `TipoPersonaggio.getCaratteristiche()`.

## 2. La tabella

I valori sono quelli di `CategoriaPersonaggio` e `TipoPersonaggio`; "×0" è immune. I tipi di danno sono quelli di `TipoDanno`
(fisici: contundente, perforante, tagliente; elementali: aria, acqua, terra, fuoco, fulmine, gelo, acido, sonico, veleno; magici:
necrotico, sacro, psichico, arcano, vuoto, maledizione).

### 2.1 Per categoria

| Categoria | Tipo di danno | Valore | Perché |
| :--- | :--- | :--- | :--- |
| Non morto | Sacro | ×2 | la luce sacra li brucia (il "danno sacro/luce" è il punto debole dei non morti) |
| Non morto | Veleno | ×0 | non hanno un corpo che vive: nessun veleno li tocca |
| Non morto | Necrotico | ×0,5 | l'energia della morte li rafforza più che ferirli |
| Volante | Terra | ×0,5 | sono fuori dalla portata di ciò che scuote il terreno |
| Costrutto | Veleno, Psichico | ×0 | non respirano, non hanno sangue né una mente |

Le altre categorie (umanoide, bestia, draconico, gigantesco, ibrido) non hanno regole comuni.

### 2.2 Per classe (in più di quanto dicano le categorie)

| Classe | Debolezze (>1) | Resistenze (tra 0 e 1) | Immunità (×0) | Perché |
| :--- | :--- | :--- | :--- | :--- |
| **Scheletro** | contundente ×1,5 | perforante ×0,5, tagliente ×0,75 | (veleno, dai non morti) | le ossa si spezzano a bastonate; lame e punte trovano poco da tagliare |
| **Fantasma, Ombra Nera, Spettro, Spirito** | (sacro ×2, dai non morti) | gelo ×0,5; contundente, perforante, tagliente ×0,5 | aria, terra, necrotico | incorporei: l'aria e la terra li attraversano e i colpi fisici fanno la metà; sono creature del gelo |
| **Lich** | (sacro ×2, dai non morti) | gelo ×0,5, fulmine ×0,5 | (veleno, dai non morti) | il Lich resiste a freddo e fulmine |
| **Drago** | gelo ×1,5 | | fuoco | il fuoco è casa sua; il freddo ferisce i draghi di fuoco |
| **Chimera Drago** | gelo ×1,5 | veleno ×0,5 | fuoco | idem; ha anche la testa velenosa |
| **Chimera** | | fuoco ×0,5, veleno ×0,5 | | respira fuoco e veleno (è nel suo repertorio) |
| **Viverna** | | | veleno | è velenosa (è nel suo repertorio) |
| **Idra** | fuoco ×1,5, acido ×1,5 | | terra, acqua, aria, veleno | creatura delle paludi e velenosa; il fuoco e l'acido impediscono alle teste di ricrescere |
| **Troll** | fuoco ×1,5, acido ×1,5 | | | si rigenera, ma non dal fuoco e dall'acido |
| **Gargoyle** | contundente ×1,5 | perforante ×0,75, tagliente ×0,75 | tutto ciò che non è fisico (e veleno e psichico, dal costrutto) | è di pietra: subisce solo i colpi fisici, e la pietra si spacca a martellate |
| **Strega** | fuoco ×1,5 | maledizione ×0,5 | | "al rogo"; le maledizioni sono il suo mestiere |
| **Folletto** | tagliente ×1,25, perforante ×1,25 | | | teme il ferro (le fate e i folletti soffrono il "ferro freddo") |

Senza voci particolari: Arpia, Centauro, Eremita, Gigante, Goblin, Hobgoblin, Minotauro, Minotauro Gigante, Titano
(per ora le sole regole sono quelle delle loro categorie), i personaggi giocanti, altri comprimari o comparse.

## Osservazioni

- La tabella è un punto di partenza: valori come ×0,5 o ×1,5 sono scelti a occhio, e un ×2 su un tipo comune cambia molto il
  gioco. Va guardata con le simulazioni (`SimulazionePartiteTest`, oggi saltato) e provata in partita.
- I boss (Lich, Strega, Minotauro Gigante, Drago) hanno delle resistenze: personaggi di livello 5 pesantemente armati non
  scalfiscono la Strega o il Lich. Non si migliora con il gelo e il fulmine ×0,5 del Lich; la Strega invece è debole al fuoco.
- Fantasmi con colpi fisici ×0,5 sono più duri per chi non ha magia o armi incantate: da verificare nelle Rovine.

## Da fare / idee aperte

- **Assegnare le voci mancanti** ad Arpia, Centauro, Minotauro, Gigante, Titano, Goblin e Hobgoblin: per ora non hanno regole di classe.
- **Resistenze e debolezze per famiglia di elementi** (una difesa diversa per il fuoco e per il gelo, per il tagliente e per il
  contundente), oggi solo il fisico e il non fisico hanno una difesa (`CalcolatoreCombattimento.difesaContro`).
- **Armi e incantamenti magici**: gli incorporei dovrebbero essere immuni ai colpi non magici. Qui basterebbe un moltiplicatore diverso per le
  armi incantate (`Arma.isIncantata`).
- **Le immunità a incantesimi e stati** come dato, per poterle scrivere nella stessa tabella.
