import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class SudokuGame extends JFrame {

    // Queste matrici tengono traccia dei numeri. Una è quella corretta, l'altra è quella che cambia
    private int[][] matriceSoluzione = new int[9][9];
    private int[][] matriceUtente = new int[9][9];
    
    // Questa serve per gestire la grafica (i quadratini dove scriviamo)
    private JTextField[][] caselleTesto = new JTextField[9][9];

    public SudokuGame() {
        // Impostazioni base della finestra
        setTitle("Progetto Sudoku - ITIS");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Creo un pannello con GridLayout per fare la griglia 9x9
        JPanel pannelloGriglia = new JPanel(new GridLayout(9, 9));
        pannelloGriglia.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Metodi per creare i numeri e "bucare" la griglia
        generaSoluzioneCasuale();
        preparaLivello();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                final int rigaCorrente = r;
                final int colonnaCorrente = c;
                
                caselleTesto[r][c] = new JTextField();
                caselleTesto[r][c].setHorizontalAlignment(JTextField.CENTER);
                caselleTesto[r][c].setFont(new Font("SansSerif", Font.BOLD, 22));

                // LOGICA BORDI: Se il resto della divisione per 3 è 0, vuol dire che siamo sul bordo di un quadrante
                int sopra = (r % 3 == 0) ? 3 : 1;
                int sinistra = (c % 3 == 0) ? 3 : 1;
                int sotto = (r == 8) ? 3 : 1;
                int destra = (c == 8) ? 3 : 1;
                caselleTesto[r][c].setBorder(new MatteBorder(sopra, sinistra, sotto, destra, Color.BLACK));

                // Se la cella ha già un numero (quelli generati dal PC), la blocco e la coloro di grigio
                if (matriceUtente[r][c] != 0) {
                    caselleTesto[r][c].setText(String.valueOf(matriceUtente[r][c]));
                    caselleTesto[r][c].setEditable(false);
                    caselleTesto[r][c].setBackground(new Color(220, 220, 220));
                }

                // Listener per controllare cosa scrive l'utente in tempo reale
                caselleTesto[r][c].addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        // Se l'utente scrive, resetto lo sfondo (magari era rosso per un errore precedente)
                        if (caselleTesto[rigaCorrente][colonnaCorrente].isEditable()) {
                            caselleTesto[rigaCorrente][colonnaCorrente].setBackground(Color.WHITE);
                        }
                        
                        String input = caselleTesto[rigaCorrente][colonnaCorrente].getText();
                        if (!input.isEmpty()) {
                            // Uso una Regex: deve essere un numero tra 1 e 9 e lungo solo 1 carattere
                            if (!input.matches("[1-9]") || input.length() > 1) {
                                caselleTesto[rigaCorrente][colonnaCorrente].setText("");
                                matriceUtente[rigaCorrente][colonnaCorrente] = 0;
                            } else {
                                matriceUtente[rigaCorrente][colonnaCorrente] = Integer.parseInt(input);
                            }
                        } else {
                            matriceUtente[rigaCorrente][colonnaCorrente] = 0;
                        }
                    }
                });
                
                pannelloGriglia.add(caselleTesto[r][c]);
            }
        }

        // Pannello per i tasti Salva, Carica e Verifica
        JPanel areaBottoni = new JPanel();
        JButton tastoSalva = new JButton("Salva");
        JButton tastoCarica = new JButton("Carica");
        JButton tastoVerifica = new JButton("Verifica");

        tastoSalva.addActionListener(e -> salvaSuFile());
        tastoCarica.addActionListener(e -> caricaDaFile());
        tastoVerifica.addActionListener(e -> controllaErrori());

        areaBottoni.add(tastoSalva);
        areaBottoni.add(tastoCarica);
        areaBottoni.add(tastoVerifica);

        add(pannelloGriglia, BorderLayout.CENTER);
        add(areaBottoni, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null); // Centra la finestra sullo schermo
    }

    private void generaSoluzioneCasuale() {
        // Genero una griglia base valida matematicamente
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                matriceSoluzione[i][j] = ((i * 3 + i / 3 + j) % 9) + 1;
            }
        }

        Random casuale = new Random();
        
        // Mischio le righe: posso scambiarle solo all'interno dello stesso blocco 3x3
        for (int i = 0; i < 25; i++) {
            int blocco = casuale.nextInt(3) * 3; // Sceglie riga 0, 3 o 6 (inizio dei blocchi)
            int r1 = blocco + casuale.nextInt(3);
            int r2 = blocco + casuale.nextInt(3);
            
            int[] temp = matriceSoluzione[r1];
            matriceSoluzione[r1] = matriceSoluzione[r2];
            matriceSoluzione[r2] = temp;
        }

        // Mischio le colonne con la stessa logica dei blocchi
        for (int i = 0; i < 25; i++) {
            int blocco = casuale.nextInt(3) * 3;
            int c1 = blocco + casuale.nextInt(3);
            int c2 = blocco + casuale.nextInt(3);
            
            for (int r = 0; r < 9; r++) {
                int temp = matriceSoluzione[r][c1];
                matriceSoluzione[r][c1] = matriceSoluzione[r][c2];
                matriceSoluzione[r][c2] = temp;
            }
        }
    }

    private void controllaErrori() {
        boolean tuttoOk = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // Controllo solo le caselle che l'utente può modificare (quelle bianche)
                if (caselleTesto[i][j].isEditable()) {
                    if (matriceUtente[i][j] != 0 && matriceUtente[i][j] != matriceSoluzione[i][j]) {
                        caselleTesto[i][j].setBackground(new Color(255, 150, 150)); // Rosso chiaro
                        tuttoOk = false;
                    } else if (matriceUtente[i][j] == 0) {
                        tuttoOk = false; // Casella vuota
                    }
                }
            }
        }
        if (tuttoOk) JOptionPane.showMessageDialog(this, "Ottimo! Sudoku risolto!");
        else JOptionPane.showMessageDialog(this, "Ci sono errori o mancano dei numeri!");
    }

    private void salvaSuFile() {
        // Scrivo la matrice utente su un file di testo separando i numeri con la virgola
        try (PrintWriter scrivi = new PrintWriter(new FileWriter("salvataggio_sudoku.txt"))) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    scrivi.print(matriceUtente[i][j] + (j == 8 ? "" : ","));
                }
                scrivi.println(); // A capo dopo ogni riga
            }
            JOptionPane.showMessageDialog(this, "Partita salvata correttamente!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Errore durante il salvataggio!");
        }
    }

    private void caricaDaFile() {
        File fileSalvataggio = new File("salvataggio_sudoku.txt");
        if (!fileSalvataggio.exists()) {
            JOptionPane.showMessageDialog(this, "Nessun salvataggio trovato!");
            return;
        }

        try (BufferedReader leggi = new BufferedReader(new FileReader(fileSalvataggio))) {
            for (int i = 0; i < 9; i++) {
                String rigaStr = leggi.readLine();
                String[] pezzi = rigaStr.split(",");
                for (int j = 0; j < 9; j++) {
                    int valore = Integer.parseInt(pezzi[j]);
                    matriceUtente[i][j] = valore;
                    // Riempio la grafica solo se la casella è quella modificabile
                    if (caselleTesto[i][j].isEditable()) {
                        caselleTesto[i][j].setText(valore == 0 ? "" : String.valueOf(valore));
                        caselleTesto[i][j].setBackground(Color.WHITE);
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Caricamento completato!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "File di salvataggio corrotto!");
        }
    }

    private void preparaLivello() {
        // Copio la soluzione completa nella griglia utente
        for (int i = 0; i < 9; i++) {
            System.arraycopy(matriceSoluzione[i], 0, matriceUtente[i], 0, 9);
        }
        
        // Tolgo 45 numeri a caso per creare il gioco
        Random casuale = new Random();
        int vuoti = 45; 
        while (vuoti > 0) {
            int r = casuale.nextInt(9);
            int c = casuale.nextInt(9);
            if (matriceUtente[r][c] != 0) {
                matriceUtente[r][c] = 0;
                vuoti--;
            }
        }
    }

    public static void main(String[] args) {
        // Imposto il tema di sistema (Windows, Mac o Linux) così non sembra un'app del 1990
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        // Avvio l'interfaccia
        SwingUtilities.invokeLater(() -> new SudokuGame().setVisible(true));
    }
}
