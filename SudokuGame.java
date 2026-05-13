import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class SudokuGame extends JFrame {

    // ── costanti colori ──────────────────────────────────────────────────────
    private static final Color SFONDO_SCURO          = new Color(18,  18,  30);
    private static final Color SFONDO_PANNELLO       = new Color(28,  28,  46);
    private static final Color SFONDO_CELLA          = new Color(38,  38,  60);
    private static final Color SFONDO_CELLA_FISSA    = new Color(22,  22,  40);
    private static final Color SFONDO_CELLA_SELEZION = new Color(60,  80, 160);
    private static final Color SFONDO_CELLA_EVIDENZ  = new Color(45,  55, 100);
    private static final Color SFONDO_CELLA_ERRORE   = new Color(120, 30,  40);
    private static final Color COLORE_ACCENTO        = new Color(99, 179, 237);
    private static final Color TESTO_BIANCO          = new Color(230, 230, 255);
    private static final Color TESTO_GRIGIO          = new Color(140, 140, 170);
    private static final Color TESTO_FISSO           = new Color(180, 180, 220);
    private static final Color TESTO_UTENTE          = new Color(99,  179, 237);
    private static final Color TESTO_ERRORE          = new Color(255, 100, 100);
    private static final Color BORDO_SEPARATORE      = new Color(80,  80, 130);
    private static final Color BORDO_BLOCCO          = new Color(99, 179, 237);
    private static final Color BOTTONE_VERDE         = new Color(56, 161, 105);
    private static final Color BOTTONE_BLU           = new Color(66, 120, 200);
    private static final Color BOTTONE_ROSSO         = new Color(180, 50,  60);
    private static final Color BOTTONE_ARANCIO       = new Color(200, 120, 40);

    // ── stato partita ────────────────────────────────────────────────────────
    private int dimensioneQuadrante = 3;
    private int dimensioneGriglia;
    private int[][] soluzione;
    private int[][] schema;
    private int[][] grigliaUtente;
    private boolean[][] celleFisse;

    // ── componenti interfaccia ───────────────────────────────────────────────
    private JPanel pannelloGriglia;
    private JTextField[][] celle;
    private JLabel etichettaTimer, etichettaStato;
    private JButton bottoneNuovaPartita, bottoneSalva, bottoneCarica,
                    bottoneVerifica, bottoneSoluzione;
    private CardLayout layoutSchede;
    private JPanel contenitoreSchede;

    // ── timer ────────────────────────────────────────────────────────────────
    private javax.swing.Timer timerGioco;
    private int secondiTrascorsi = 0;

    // ── selezione cella ──────────────────────────────────────────────────────
    private int rigaSelezionata = -1, colonnaSelezionata = -1;

    public SudokuGame() {
        setTitle("Sudoku");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setBackground(SFONDO_SCURO);
        getContentPane().setBackground(SFONDO_SCURO);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent evento) {
                if (grigliaUtente != null) {
                    int risposta = JOptionPane.showConfirmDialog(SudokuGame.this,
                        "Vuoi salvare la partita prima di uscire?",
                        "Salva e Esci", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (risposta == JOptionPane.YES_OPTION) { salvaPartita(null); dispose(); }
                    else if (risposta == JOptionPane.NO_OPTION) dispose();
                } else {
                    dispose();
                }
            }
        });

        layoutSchede = new CardLayout();
        contenitoreSchede = new JPanel(layoutSchede);
        contenitoreSchede.setBackground(SFONDO_SCURO);
        contenitoreSchede.add(costruisciPannelloMenu(), "menu");
        contenitoreSchede.add(costruisciPannelloGioco(), "gioco");
        add(contenitoreSchede);

        setSize(680, 780);
        setMinimumSize(new Dimension(500, 600));
        setLocationRelativeTo(null);
        setVisible(true);
        layoutSchede.show(contenitoreSchede, "menu");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANNELLO MENU
    // ════════════════════════════════════════════════════════════════════════
    private JPanel costruisciPannelloMenu() {
        JPanel pannello = new JPanel(new GridBagLayout());
        pannello.setBackground(SFONDO_SCURO);
        GridBagConstraints vincoli = new GridBagConstraints();
        vincoli.insets = new Insets(8, 8, 8, 8);
        vincoli.fill = GridBagConstraints.HORIZONTAL;
        vincoli.gridx = 0; vincoli.gridy = 0;

        JLabel titolo = new JLabel("SUDOKU", SwingConstants.CENTER);
        titolo.setFont(new Font("SansSerif", Font.BOLD, 48));
        titolo.setForeground(COLORE_ACCENTO);
        vincoli.insets = new Insets(40, 8, 4, 8);
        pannello.add(titolo, vincoli);

        JLabel sottotitolo = new JLabel("Scegli come vuoi giocare", SwingConstants.CENTER);
        sottotitolo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sottotitolo.setForeground(TESTO_GRIGIO);
        vincoli.gridy++; vincoli.insets = new Insets(0, 8, 30, 8);
        pannello.add(sottotitolo, vincoli);

        JLabel etichettaQuadranti = new JLabel("Dimensione quadranti:", SwingConstants.CENTER);
        etichettaQuadranti.setFont(new Font("SansSerif", Font.BOLD, 15));
        etichettaQuadranti.setForeground(TESTO_BIANCO);
        vincoli.gridy++; vincoli.insets = new Insets(8, 60, 4, 60);
        pannello.add(etichettaQuadranti, vincoli);

        JPanel pannelloRadio = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 4));
        pannelloRadio.setBackground(SFONDO_SCURO);
        ButtonGroup gruppoBottoni = new ButtonGroup();
        int[] dimensioni = {2, 3, 4};
        String[] etichette = {"2x2  (4x4)", "3x3  (9x9)", "4x4  (16x16)"};
        JRadioButton[] opzioni = new JRadioButton[3];
        for (int i = 0; i < 3; i++) {
            final int dim = dimensioni[i];
            opzioni[i] = new JRadioButton(etichette[i]);
            opzioni[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            opzioni[i].setForeground(TESTO_BIANCO);
            opzioni[i].setBackground(SFONDO_SCURO);
            opzioni[i].setSelected(dim == 3);
            opzioni[i].addActionListener(e -> dimensioneQuadrante = dim);
            gruppoBottoni.add(opzioni[i]);
            pannelloRadio.add(opzioni[i]);
        }
        vincoli.gridy++; vincoli.insets = new Insets(0, 20, 16, 20);
        pannello.add(pannelloRadio, vincoli);

        vincoli.insets = new Insets(6, 60, 6, 60);
        JButton bottoneNuova = creaBottone("Nuova Partita", BOTTONE_VERDE);
        bottoneNuova.addActionListener(e -> {
            dimensioneQuadrante = opzioni[0].isSelected() ? 2 : opzioni[2].isSelected() ? 4 : 3;
            avviaNuovaPartita();
        });
        vincoli.gridy++; pannello.add(bottoneNuova, vincoli);

        JButton bottoneCaricaMenu = creaBottone("Carica Salvataggio", BOTTONE_BLU);
        bottoneCaricaMenu.addActionListener(e -> caricaPartita());
        vincoli.gridy++; pannello.add(bottoneCaricaMenu, vincoli);

        JLabel piedipagina = new JLabel("v1.0  -  Sudoku Java", SwingConstants.CENTER);
        piedipagina.setFont(new Font("SansSerif", Font.PLAIN, 11));
        piedipagina.setForeground(TESTO_GRIGIO);
        vincoli.gridy++; vincoli.insets = new Insets(40, 8, 8, 8);
        pannello.add(piedipagina, vincoli);

        return pannello;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PANNELLO GIOCO
    // ════════════════════════════════════════════════════════════════════════
    private JPanel costruisciPannelloGioco() {
        JPanel esterno = new JPanel(new BorderLayout(0, 0));
        esterno.setBackground(SFONDO_SCURO);

        JPanel barraSuperiore = new JPanel(new BorderLayout());
        barraSuperiore.setBackground(SFONDO_PANNELLO);
        barraSuperiore.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JButton bottoneMenu = creaBottone("<- Menu", new Color(70, 70, 100));
        bottoneMenu.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bottoneMenu.addActionListener(e -> tornaAlMenu());
        barraSuperiore.add(bottoneMenu, BorderLayout.WEST);

        etichettaTimer = new JLabel("00:00", SwingConstants.CENTER);
        etichettaTimer.setFont(new Font("SansSerif", Font.BOLD, 18));
        etichettaTimer.setForeground(COLORE_ACCENTO);
        barraSuperiore.add(etichettaTimer, BorderLayout.CENTER);

        etichettaStato = new JLabel("", SwingConstants.RIGHT);
        etichettaStato.setFont(new Font("SansSerif", Font.PLAIN, 13));
        etichettaStato.setForeground(TESTO_GRIGIO);
        barraSuperiore.add(etichettaStato, BorderLayout.EAST);

        esterno.add(barraSuperiore, BorderLayout.NORTH);

        pannelloGriglia = new JPanel();
        pannelloGriglia.setBackground(SFONDO_SCURO);
        JScrollPane scorrimento = new JScrollPane(pannelloGriglia);
        scorrimento.setBackground(SFONDO_SCURO);
        scorrimento.getViewport().setBackground(SFONDO_SCURO);
        scorrimento.setBorder(null);
        esterno.add(scorrimento, BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        barraInferiore.setBackground(SFONDO_PANNELLO);

        bottoneNuovaPartita = creaBottone("Nuova", BOTTONE_ARANCIO);
        bottoneNuovaPartita.addActionListener(e -> avviaNuovaPartita());

        bottoneSalva = creaBottone("Salva", BOTTONE_BLU);
        bottoneSalva.addActionListener(e -> salvaPartita(null));

        bottoneCarica = creaBottone("Carica", BOTTONE_BLU);
        bottoneCarica.addActionListener(e -> caricaPartita());

        bottoneVerifica = creaBottone("Verifica", BOTTONE_VERDE);
        bottoneVerifica.addActionListener(e -> verificaSoluzione());

        bottoneSoluzione = creaBottone("Soluzione", BOTTONE_ROSSO);
        bottoneSoluzione.addActionListener(e -> mostraSoluzione());

        barraInferiore.add(bottoneNuovaPartita);
        barraInferiore.add(bottoneSalva);
        barraInferiore.add(bottoneCarica);
        barraInferiore.add(bottoneVerifica);
        barraInferiore.add(bottoneSoluzione);
        esterno.add(barraInferiore, BorderLayout.SOUTH);

        return esterno;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  COSTRUZIONE GRIGLIA
    // ════════════════════════════════════════════════════════════════════════
    private void costruisciGriglia() {
        pannelloGriglia.removeAll();
        pannelloGriglia.setLayout(new GridBagLayout());
        pannelloGriglia.setBackground(SFONDO_SCURO);

        celle = new JTextField[dimensioneGriglia][dimensioneGriglia];

        int dimensioneCella = dimensioneGriglia <= 4 ? 80 : dimensioneGriglia <= 9 ? 58 : 40;
        Font fontCella = new Font("SansSerif", Font.BOLD,
                dimensioneGriglia <= 4 ? 28 : dimensioneGriglia <= 9 ? 20 : 14);

        JPanel tavola = new JPanel(new GridLayout(dimensioneGriglia, dimensioneGriglia, 2, 2));
        tavola.setBackground(BORDO_BLOCCO);
        int margine = dimensioneGriglia <= 4 ? 20 : 14;
        tavola.setBorder(BorderFactory.createEmptyBorder(margine, margine, margine, margine));

        for (int riga = 0; riga < dimensioneGriglia; riga++) {
            for (int colonna = 0; colonna < dimensioneGriglia; colonna++) {
                final int r = riga, c = colonna;
                JTextField campoCella = new JTextField();
                campoCella.setHorizontalAlignment(JTextField.CENTER);
                campoCella.setFont(fontCella);
                campoCella.setPreferredSize(new Dimension(dimensioneCella, dimensioneCella));
                campoCella.setOpaque(true);
                campoCella.setBorder(creaBordoCella(r, c));
                campoCella.setCaretColor(COLORE_ACCENTO);

                campoCella.addFocusListener(new FocusAdapter() {
                    @Override public void focusGained(FocusEvent evento) {
                        rigaSelezionata = r;
                        colonnaSelezionata = c;
                        aggiornaColoriCelle();
                    }
                });
                campoCella.addKeyListener(new KeyAdapter() {
                    @Override public void keyReleased(KeyEvent evento) {
                        if (!celleFisse[r][c]) gestisciInput(r, c, campoCella);
                    }
                });

                celle[riga][colonna] = campoCella;
                tavola.add(campoCella);
            }
        }

        GridBagConstraints vincoliTavola = new GridBagConstraints();
        vincoliTavola.weightx = 1; vincoliTavola.weighty = 1;
        vincoliTavola.anchor = GridBagConstraints.CENTER;
        pannelloGriglia.add(tavola, vincoliTavola);
        pannelloGriglia.revalidate();
        pannelloGriglia.repaint();
    }

    private Border creaBordoCella(int riga, int colonna) {
        int alto     = (riga    % dimensioneQuadrante == 0) ? 3 : 1;
        int sinistra = (colonna % dimensioneQuadrante == 0) ? 3 : 1;
        int basso    = (riga    == dimensioneGriglia - 1) ? 3 : 0;
        int destra   = (colonna == dimensioneGriglia - 1) ? 3 : 0;
        return BorderFactory.createMatteBorder(alto, sinistra, basso, destra, BORDO_SEPARATORE);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOGICA PARTITA
    // ════════════════════════════════════════════════════════════════════════
    private void avviaNuovaPartita() {
        dimensioneGriglia = dimensioneQuadrante * dimensioneQuadrante;
        secondiTrascorsi  = 0;
        rigaSelezionata   = -1;
        colonnaSelezionata = -1;

        soluzione     = new int[dimensioneGriglia][dimensioneGriglia];
        schema        = new int[dimensioneGriglia][dimensioneGriglia];
        grigliaUtente = new int[dimensioneGriglia][dimensioneGriglia];
        celleFisse    = new boolean[dimensioneGriglia][dimensioneGriglia];

        generaSoluzione();
        generaSchema();

        for (int riga = 0; riga < dimensioneGriglia; riga++)
            for (int colonna = 0; colonna < dimensioneGriglia; colonna++) {
                grigliaUtente[riga][colonna] = schema[riga][colonna];
                celleFisse[riga][colonna]    = schema[riga][colonna] != 0;
            }

        costruisciGriglia();
        aggiornaTutteLeCelle();
        avviaTimer();
        layoutSchede.show(contenitoreSchede, "gioco");
        setTitle("Sudoku  " + dimensioneGriglia + "x" + dimensioneGriglia);
        etichettaStato.setText("Quadranti " + dimensioneQuadrante + "x" + dimensioneQuadrante);
    }

    private void gestisciInput(int riga, int colonna, JTextField campoCella) {
        String testo = campoCella.getText().trim();
        if (testo.isEmpty()) {
            grigliaUtente[riga][colonna] = 0;
        } else {
            try {
                int valore = Integer.parseInt(testo);
                if (valore >= 1 && valore <= dimensioneGriglia) {
                    grigliaUtente[riga][colonna] = valore;
                    campoCella.setText(String.valueOf(valore));
                } else {
                    campoCella.setText("");
                    grigliaUtente[riga][colonna] = 0;
                }
            } catch (NumberFormatException eccezione) {
                campoCella.setText("");
                grigliaUtente[riga][colonna] = 0;
            }
        }
        aggiornaColoriCelle();
        if (isPuzzleCompleto()) alCompletamento();
    }

    private boolean isPuzzleCompleto() {
        for (int riga = 0; riga < dimensioneGriglia; riga++)
            for (int colonna = 0; colonna < dimensioneGriglia; colonna++)
                if (grigliaUtente[riga][colonna] != soluzione[riga][colonna]) return false;
        return true;
    }

    private void alCompletamento() {
        fermaTimer();
        String tempo = etichettaTimer.getText();
        JOptionPane.showMessageDialog(this,
            "Complimenti! Hai risolto il Sudoku!\n\nTempo: " + tempo,
            "Vittoria!", JOptionPane.INFORMATION_MESSAGE);
        grigliaUtente = null;
    }

    private void verificaSoluzione() {
        boolean ciSonoErrori = false;
        for (int riga = 0; riga < dimensioneGriglia; riga++)
            for (int colonna = 0; colonna < dimensioneGriglia; colonna++)
                if (!celleFisse[riga][colonna] && grigliaUtente[riga][colonna] != 0
                        && grigliaUtente[riga][colonna] != soluzione[riga][colonna])
                    ciSonoErrori = true;
        if (ciSonoErrori) {
            etichettaStato.setText("Ci sono errori");
            etichettaStato.setForeground(TESTO_ERRORE);
            aggiornaColoriCelle();
        } else {
            etichettaStato.setText("Nessun errore!");
            etichettaStato.setForeground(new Color(100, 220, 120));
        }
    }

    private void mostraSoluzione() {
        int risposta = JOptionPane.showConfirmDialog(this,
            "Vuoi vedere la soluzione? La partita terminera'.",
            "Mostra Soluzione", JOptionPane.YES_NO_OPTION);
        if (risposta != JOptionPane.YES_OPTION) return;
        fermaTimer();
        for (int riga = 0; riga < dimensioneGriglia; riga++)
            for (int colonna = 0; colonna < dimensioneGriglia; colonna++)
                grigliaUtente[riga][colonna] = soluzione[riga][colonna];
        aggiornaTutteLeCelle();
        etichettaStato.setText("Soluzione mostrata");
        etichettaStato.setForeground(TESTO_GRIGIO);
        grigliaUtente = null;
    }

    private void tornaAlMenu() {
        if (grigliaUtente != null) {
            int risposta = JOptionPane.showConfirmDialog(this,
                "Vuoi salvare prima di tornare al menu?",
                "Torna al Menu", JOptionPane.YES_NO_CANCEL_OPTION);
            if (risposta == JOptionPane.YES_OPTION) salvaPartita(null);
            else if (risposta == JOptionPane.CANCEL_OPTION) return;
        }
        fermaTimer();
        grigliaUtente = null;
        layoutSchede.show(contenitoreSchede, "menu");
        setTitle("Sudoku");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  AGGIORNAMENTO CELLE
    // ════════════════════════════════════════════════════════════════════════
    private void aggiornaTutteLeCelle() {
        if (celle == null) return;
        for (int riga = 0; riga < dimensioneGriglia; riga++)
            for (int colonna = 0; colonna < dimensioneGriglia; colonna++)
                aggiornaCella(riga, colonna, false, false, false);
    }

    private void aggiornaColoriCelle() {
        if (celle == null) return;
        for (int riga = 0; riga < dimensioneGriglia; riga++)
            for (int colonna = 0; colonna < dimensioneGriglia; colonna++) {
                boolean selezionata  = (riga == rigaSelezionata && colonna == colonnaSelezionata);
                boolean stessoNumero = rigaSelezionata >= 0 && colonnaSelezionata >= 0
                        && grigliaUtente[riga][colonna] != 0
                        && grigliaUtente[riga][colonna] == grigliaUtente[rigaSelezionata][colonnaSelezionata];
                boolean evidenziata  = rigaSelezionata >= 0 && colonnaSelezionata >= 0
                        && (riga == rigaSelezionata || colonna == colonnaSelezionata
                        || (riga / dimensioneQuadrante == rigaSelezionata / dimensioneQuadrante
                            && colonna / dimensioneQuadrante == colonnaSelezionata / dimensioneQuadrante));
                boolean errore = !celleFisse[riga][colonna] && grigliaUtente[riga][colonna] != 0
                        && grigliaUtente[riga][colonna] != soluzione[riga][colonna];
                aggiornaCella(riga, colonna, selezionata, evidenziata || stessoNumero, errore);
            }
    }

    private void aggiornaCella(int riga, int colonna, boolean selezionata,
                                boolean evidenziata, boolean errore) {
        JTextField campoCella = celle[riga][colonna];
        int valore = grigliaUtente[riga][colonna];
        campoCella.setText(valore == 0 ? "" : String.valueOf(valore));

        Color sfondo, coloreTesto;
        if (celleFisse[riga][colonna]) {
            sfondo = selezionata ? SFONDO_CELLA_SELEZION
                   : evidenziata ? SFONDO_CELLA_EVIDENZ : SFONDO_CELLA_FISSA;
            coloreTesto = TESTO_FISSO;
        } else {
            if (errore)           sfondo = SFONDO_CELLA_ERRORE;
            else if (selezionata) sfondo = SFONDO_CELLA_SELEZION;
            else if (evidenziata) sfondo = SFONDO_CELLA_EVIDENZ;
            else                  sfondo = SFONDO_CELLA;
            coloreTesto = errore ? TESTO_ERRORE : TESTO_UTENTE;
        }
        campoCella.setBackground(sfondo);
        campoCella.setForeground(coloreTesto);
        campoCella.setEditable(!celleFisse[riga][colonna]);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TIMER
    // ════════════════════════════════════════════════════════════════════════
    private void avviaTimer() {
        fermaTimer();
        timerGioco = new javax.swing.Timer(1000, evento -> {
            secondiTrascorsi++;
            int minuti  = secondiTrascorsi / 60;
            int secondi = secondiTrascorsi % 60;
            etichettaTimer.setText(String.format("%02d:%02d", minuti, secondi));
        });
        timerGioco.start();
    }

    private void fermaTimer() {
        if (timerGioco != null) { timerGioco.stop(); timerGioco = null; }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SALVATAGGIO / CARICAMENTO
    // ════════════════════════════════════════════════════════════════════════
    private void salvaPartita(File fileSalvataggio) {
        if (grigliaUtente == null) {
            JOptionPane.showMessageDialog(this, "Nessuna partita attiva.");
            return;
        }
        if (fileSalvataggio == null) {
            JFileChooser selettoreFile = new JFileChooser();
            selettoreFile.setDialogTitle("Salva Partita");
            selettoreFile.setSelectedFile(new File("sudoku_salvataggio.sud"));
            if (selettoreFile.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            fileSalvataggio = selettoreFile.getSelectedFile();
            if (!fileSalvataggio.getName().endsWith(".sud"))
                fileSalvataggio = new File(fileSalvataggio.getAbsolutePath() + ".sud");
        }
        try (PrintWriter scrittore = new PrintWriter(new FileWriter(fileSalvataggio))) {
            scrittore.println("SUDOKU_SAVE_V1");
            scrittore.println(dimensioneQuadrante);
            scrittore.println(secondiTrascorsi);
            scriviMatrice(scrittore, soluzione);
            scriviMatrice(scrittore, schema);
            scriviMatrice(scrittore, grigliaUtente);
            etichettaStato.setText("Salvato!");
            etichettaStato.setForeground(new Color(100, 220, 120));
        } catch (IOException eccezione) {
            JOptionPane.showMessageDialog(this, "Errore salvataggio:\n" + eccezione.getMessage());
        }
    }

    private void caricaPartita() {
        JFileChooser selettoreFile = new JFileChooser();
        selettoreFile.setDialogTitle("Carica Salvataggio");
        selettoreFile.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Salvataggi Sudoku (*.sud)", "sud"));
        if (selettoreFile.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File fileCaricato = selettoreFile.getSelectedFile();
        try (Scanner lettore = new Scanner(fileCaricato)) {
            String intestazione = lettore.nextLine();
            if (!intestazione.equals("SUDOKU_SAVE_V1")) throw new IOException("File non valido.");
            dimensioneQuadrante = Integer.parseInt(lettore.nextLine().trim());
            dimensioneGriglia   = dimensioneQuadrante * dimensioneQuadrante;
            secondiTrascorsi    = Integer.parseInt(lettore.nextLine().trim());
            soluzione     = leggiMatrice(lettore, dimensioneGriglia);
            schema        = leggiMatrice(lettore, dimensioneGriglia);
            grigliaUtente = leggiMatrice(lettore, dimensioneGriglia);
            celleFisse = new boolean[dimensioneGriglia][dimensioneGriglia];
            for (int riga = 0; riga < dimensioneGriglia; riga++)
                for (int colonna = 0; colonna < dimensioneGriglia; colonna++)
                    celleFisse[riga][colonna] = schema[riga][colonna] != 0;
            costruisciGriglia();
            aggiornaTutteLeCelle();
            avviaTimer();
            layoutSchede.show(contenitoreSchede, "gioco");
            setTitle("Sudoku  " + dimensioneGriglia + "x" + dimensioneGriglia);
            etichettaStato.setText("Caricato! Quadranti " + dimensioneQuadrante + "x" + dimensioneQuadrante);
            etichettaStato.setForeground(COLORE_ACCENTO);
        } catch (Exception eccezione) {
            JOptionPane.showMessageDialog(this, "Errore caricamento:\n" + eccezione.getMessage());
        }
    }

    private void scriviMatrice(PrintWriter scrittore, int[][] matrice) {
        for (int[] riga : matrice) {
            StringBuilder costruttore = new StringBuilder();
            for (int i = 0; i < riga.length; i++) {
                if (i > 0) costruttore.append(',');
                costruttore.append(riga[i]);
            }
            scrittore.println(costruttore);
        }
    }

    private int[][] leggiMatrice(Scanner lettore, int dimensione) {
        int[][] matrice = new int[dimensione][dimensione];
        for (int riga = 0; riga < dimensione; riga++) {
            String[] parti = lettore.nextLine().trim().split(",");
            for (int colonna = 0; colonna < dimensione; colonna++)
                matrice[riga][colonna] = Integer.parseInt(parti[colonna]);
        }
        return matrice;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  GENERAZIONE PUZZLE
    // ════════════════════════════════════════════════════════════════════════
    private void generaSoluzione() {
        risolvi(soluzione, 0);
    }

    private boolean risolvi(int[][] griglia, int posizione) {
        if (posizione == dimensioneGriglia * dimensioneGriglia) return true;
        int riga    = posizione / dimensioneGriglia;
        int colonna = posizione % dimensioneGriglia;
        if (griglia[riga][colonna] != 0) return risolvi(griglia, posizione + 1);
        java.util.List<Integer> numeri = new ArrayList<>();
        for (int i = 1; i <= dimensioneGriglia; i++) numeri.add(i);
        Collections.shuffle(numeri);
        for (int numero : numeri) {
            if (eValido(griglia, riga, colonna, numero)) {
                griglia[riga][colonna] = numero;
                if (risolvi(griglia, posizione + 1)) return true;
                griglia[riga][colonna] = 0;
            }
        }
        return false;
    }

    private boolean eValido(int[][] griglia, int riga, int colonna, int numero) {
        for (int i = 0; i < dimensioneGriglia; i++)
            if (griglia[riga][i] == numero || griglia[i][colonna] == numero) return false;
        int rigaInizio    = (riga    / dimensioneQuadrante) * dimensioneQuadrante;
        int colonnaInizio = (colonna / dimensioneQuadrante) * dimensioneQuadrante;
        for (int dr = 0; dr < dimensioneQuadrante; dr++)
            for (int dc = 0; dc < dimensioneQuadrante; dc++)
                if (griglia[rigaInizio + dr][colonnaInizio + dc] == numero) return false;
        return true;
    }

    private void generaSchema() {
        for (int riga = 0; riga < dimensioneGriglia; riga++)
            System.arraycopy(soluzione[riga], 0, schema[riga], 0, dimensioneGriglia);
        int celleRimuovere = (dimensioneGriglia == 4) ? 8 : (dimensioneGriglia == 9) ? 45 : 90;
        Random casuale = new Random();
        int rimossi = 0, tentativi = 0;
        while (rimossi < celleRimuovere && tentativi < celleRimuovere * 10) {
            int riga    = casuale.nextInt(dimensioneGriglia);
            int colonna = casuale.nextInt(dimensioneGriglia);
            if (schema[riga][colonna] != 0) {
                schema[riga][colonna] = 0;
                rimossi++;
            }
            tentativi++;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITY INTERFACCIA
    // ════════════════════════════════════════════════════════════════════════
    private JButton creaBottone(String testo, Color coloreBase) {
        JButton bottone = new JButton(testo) {
            @Override protected void paintComponent(Graphics grafico) {
                Graphics2D grafico2D = (Graphics2D) grafico.create();
                grafico2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color sfondoBottone = getModel().isPressed() ? coloreBase.darker().darker()
                        : getModel().isRollover() ? coloreBase.brighter() : coloreBase;
                grafico2D.setColor(sfondoBottone);
                grafico2D.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                grafico2D.dispose();
                super.paintComponent(grafico);
            }
        };
        bottone.setForeground(Color.WHITE);
        bottone.setFont(new Font("SansSerif", Font.BOLD, 13));
        bottone.setContentAreaFilled(false);
        bottone.setBorderPainted(false);
        bottone.setFocusPainted(false);
        bottone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bottone.setOpaque(false);
        bottone.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        return bottone;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] argomenti) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignorata) {}
        SwingUtilities.invokeLater(SudokuGame::new);
    }
}
