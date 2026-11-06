#!/usr/bin/env python3
"""Costruisce l'atlante di una cartella di img/ dalle immagini originali, secondo il suo descrittore.

Uso: python3 costruisci_atlante.py <cartella>        (per esempio: icone, mappa, oggetti)

Le immagini ORIGINALI stanno in 12-foresta-ui-comune/sorgenti-immagini/<cartella>/, fuori dalle risorse, quindi non finiscono nei jar:
non si buttano via, servono per rifare l'atlante quando si aggiunge o si cambia un'immagine. Nei jar vanno solo l'atlante e il suo
descrittore, che stanno in src/main/resources/com/threeamigos/foresta/img/<cartella>/ (atlante_<cartella>.png e .txt).

Il descrittore (atlante_<cartella>.txt) ha due formati.
A GRIGLIA: CELLA n (lato della cella quadrata) o CELLA LxA (cella rettangolare), 32 se manca, COLONNE n, FUORI nome... (file che restano a parte, nelle risorse), poi una riga per
ogni riga di celle: nome, nome@LxA se l'immagine è più piccola della cella, - per una cella vuota. Come i font Doomdark: prima riga e
prima colonna rosse e, a destra e in basso di ogni cella, la sua linea rossa.
LIBERO (immagini di misure diverse, la propone impagina_atlante.py): LIBERO, LARGHEZZA n, ALTEZZA n, FUORI..., e una riga
"nome x y larghezza altezza" per immagine, con attorno il suo riquadro rosso di 1 pixel.
ALIAS nome altro (in tutti e due i formati): l'immagine "nome" è identica ad "altro" e usa la sua stessa cella (lo script controlla che i
pixel siano davvero gli stessi); finché sono uguali si risparmia lo spazio, e quando si disegna quella nuova si toglie la riga ALIAS e si
aggiunge l'immagine all'atlante.
Le righe con # sono commenti. Lo script controlla che ogni immagine originale compaia nell'atlante o fra quelle fuori, che nessuna sia
ripetuta, che le misure scritte siano quelle dei file e che nel formato libero nulla esca o si sovrapponga.
"""
import os
import sys
from PIL import Image

if len(sys.argv) < 2:
    sys.exit(__doc__)
NOME = sys.argv[1]
MODULO = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '12-foresta-ui-comune')
SORGENTI = os.path.join(MODULO, 'sorgenti-immagini', NOME)
RISORSE = os.path.join(MODULO, 'src', 'main', 'resources', 'com', 'threeamigos', 'foresta', 'img', NOME)
ROSSO = (255, 0, 0, 255)

cella_l = cella_a = 32
colonne = 62
fuori = set()
righe = []
libero = False
larghezza_libera = altezza_libera = 0
posti = {}
alias = {}
with open(os.path.join(RISORSE, 'atlante_%s.txt' % NOME), encoding='utf-8') as f:
    for linea in f:
        linea = linea.strip()
        if not linea or linea.startswith('#'):
            continue
        parole = linea.split()
        if parole[0] == 'CELLA':
            cella_l, _, cella_a = parole[1].partition('x')
            cella_l = int(cella_l)
            cella_a = int(cella_a) if cella_a else cella_l
        elif parole[0] == 'COLONNE':
            colonne = int(parole[1])
        elif parole[0] == 'FUORI':
            fuori.update(parole[1:])
        elif parole[0] == 'ALIAS':
            alias[parole[1]] = parole[2]
        elif parole[0] == 'LIBERO':
            libero = True
        elif parole[0] == 'LARGHEZZA':
            larghezza_libera = int(parole[1])
        elif parole[0] == 'ALTEZZA':
            altezza_libera = int(parole[1])
        elif libero:
            posti[parole[0]] = tuple(int(p) for p in parole[1:5])
        else:
            righe.append(parole)


def originale(nome):
    return Image.open(os.path.join(SORGENTI, nome + '.png')).convert('RGBA')


def controlla_gli_alias(usati):
    for nome, altro in alias.items():
        if altro not in usati:
            sys.exit('ALIAS %s %s: %s non è nell\'atlante' % (nome, altro, altro))
        if nome in usati:
            sys.exit('ALIAS %s: è anche nell\'atlante' % nome)
        if originale(nome).tobytes() != originale(altro).tobytes() or originale(nome).size != originale(altro).size:
            sys.exit('ALIAS %s %s: le due immagini non sono identiche' % (nome, altro))


def controlla_che_non_manchi_nulla(usati):
    presenti = {f[:-4] for f in os.listdir(SORGENTI) if f.endswith('.png')}
    mancano = presenti - usati - set(alias)
    if mancano:
        sys.exit("Immagini originali né nell'atlante né fra quelle fuori: " + ', '.join(sorted(mancano)))
    for nome in fuori:
        if not os.path.exists(os.path.join(RISORSE, nome + '.png')):
            sys.exit('FUORI %s: manca il file nelle risorse' % nome)


if libero:
    atlante = Image.new('RGBA', (larghezza_libera, altezza_libera), (0, 0, 0, 0))
    px = atlante.load()
    occupati = {}
    for nome, (x, y, l, a) in sorted(posti.items()):
        immagine = originale(nome)
        if immagine.size != (l, a):
            sys.exit('%s è %dx%d, nel descrittore %dx%d' % (nome, immagine.size[0], immagine.size[1], l, a))
        if x < 1 or y < 1 or x + l + 1 > larghezza_libera or y + a + 1 > altezza_libera:
            sys.exit("%s (%dx%d in %d,%d) esce dall'atlante" % (nome, l, a, x, y))
        for yy in range(y - 1, y + a + 1):
            for xx in range(x - 1, x + l + 1):
                if x <= xx < x + l and y <= yy < y + a:
                    if (xx, yy) in occupati:
                        sys.exit('%s si sovrappone a %s in %d,%d' % (nome, occupati[(xx, yy)], xx, yy))
                    occupati[(xx, yy)] = nome
                else:
                    px[xx, yy] = ROSSO
        atlante.paste(immagine, (x, y))
    controlla_gli_alias(set(posti))
    controlla_che_non_manchi_nulla(set(posti) | fuori)
    atlante.save(os.path.join(RISORSE, 'atlante_%s.png' % NOME))
    print('atlante_%s.png: %dx%d, %d immagini (impaginazione libera)' % (NOME, larghezza_libera, altezza_libera, len(posti)))
    sys.exit(0)

passo_x = cella_l + 1
passo_y = cella_a + 1
usati = set()
larghezza = colonne * passo_x + 1
altezza = len(righe) * passo_y + 1
atlante = Image.new('RGBA', (larghezza, altezza), (0, 0, 0, 0))
px = atlante.load()
for x in range(larghezza):
    for y in range(altezza):
        if x % passo_x == 0 or y % passo_y == 0:
            px[x, y] = ROSSO
for r, nomi in enumerate(righe):
    if len(nomi) > colonne:
        sys.exit('La riga %d ha %d celle, le colonne sono %d' % (r + 1, len(nomi), colonne))
    for c, voce in enumerate(nomi):
        if voce == '-':
            continue
        nome, _, misura = voce.partition('@')
        if nome in usati:
            sys.exit('Immagine ripetuta: ' + nome)
        usati.add(nome)
        immagine = originale(nome)
        if misura and '%dx%d' % immagine.size != misura:
            sys.exit("%s è %dx%d, nell'elenco %s" % (nome, immagine.size[0], immagine.size[1], misura))
        if not misura and immagine.size != (cella_l, cella_a):
            sys.exit('%s è %dx%d: scrivere nome@LxA' % ((nome,) + immagine.size))
        atlante.paste(immagine, (1 + c * passo_x, 1 + r * passo_y))
controlla_gli_alias(usati)
controlla_che_non_manchi_nulla(usati | fuori)
atlante.save(os.path.join(RISORSE, 'atlante_%s.png' % NOME))
print('atlante_%s.png: %dx%d, %d righe, %d immagini' % (NOME, larghezza, altezza, len(righe), len(usati)))
