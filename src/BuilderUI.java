import Components.ShadowButton;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class BuilderUI implements ActionListener {
    ImageIcon image = new ImageIcon("src/assets/bomb4.png");
    File explosionAudio =new File("src/assets/songExplosion.wav");
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(explosionAudio);
    Clip explosionClip = AudioSystem.getClip();

    private static final int numberMInes = 30;
    private static final int size = 16;
    private int timerCount = 0;
    private int clicksCount = 0;
    private Timer timer;

    JFrame frame = new JFrame();
    JPanel panelButton = new JPanel();
    ShadowButton[][] buttonList = new ShadowButton[size][size];
    boolean[][] minesLocation = new boolean[size][size];
    int[][] distance = new int[size][size];
    JLabel clicksCountLabel = new JLabel();
    JLabel timerLabel = new JLabel();
    JLabel redFlagLabel = new JLabel();

    JPanel infoPanel = new JPanel();
    Random rand = new Random();

    public BuilderUI() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try {
            explosionClip.open(audioInputStream);
        } catch (LineUnavailableException | IOException ex) {
            throw new RuntimeException(ex);
        }

        frame.setTitle("CAMPO MINADO");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 1920);
        frame.getContentPane().setBackground(Color.black);
        frame.setVisible(true);
        frame.setLayout(new BorderLayout());

        panelButton.setLayout(new GridLayout(size, size));
        panelButton.setBounds(0, 0, 800, 600);

        createButtons();
        generateNumberMInes();
        calculateProximity();
        defineMenuInfo();
        frame.add(panelButton, BorderLayout.CENTER);

    }


    public void defineMenuInfo(){
        clicksCountLabel.setBackground(new Color(25, 25,25));
        clicksCountLabel.setForeground(new Color(25,255,0));
        clicksCountLabel.setFont(new Font("Ink Free", Font.BOLD, 40));
        clicksCountLabel.setText(String.format("%03d", clicksCount));
        clicksCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clicksCountLabel.setOpaque(true);

        timerLabel.setBackground(new Color(25, 25,25));
        timerLabel.setForeground(new Color(25,255,0));
        timerLabel.setFont(new Font("Ink Free", Font.BOLD, 40));
        timerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        timerLabel.setText("000");
        timerLabel.setOpaque(true);

        redFlagLabel.setBackground(new Color(25, 25,25));
        redFlagLabel.setForeground(new Color(25,255,0));
        redFlagLabel.setFont(new Font("Ink Free", Font.BOLD, 40));
        redFlagLabel.setHorizontalAlignment(SwingConstants.CENTER);
        redFlagLabel.setText(String.valueOf(ShadowButton.flags));
        redFlagLabel.setOpaque(true);
        redFlagLabel.setIcon(ShadowButton.redFLag);

        infoPanel.setLayout(new GridLayout(1, 2));
        infoPanel.add(timerLabel, BorderLayout.EAST);
        infoPanel.add(redFlagLabel, BorderLayout.CENTER);
        infoPanel.add(clicksCountLabel, BorderLayout.WEST);
        infoPanel.setBackground(Color.black);

        frame.add(infoPanel, BorderLayout.NORTH);
    }

    public void timer(boolean start){
        if(timer == null){
            timer = new Timer(1000, e -> {
                timerCount++;
                timerLabel.setText(String.format("%03d", timerCount));
                repaintRedFLag();
            });
        }

        if(start){
            if(!timer.isRunning())
                timer.start();
        }else{
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var buttton = (JButton) e.getSource();
        setClicksCount();

        if(timerCount == 0){
            timer(true);
        }

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                if(buttonList[i][j] == buttton) {
                    if(minesLocation[i][j]) {
                        buttonList[i][j].repaintIcon(image);
                        explosionClip.start();
                        timer(false);
                        JOptionPane.showMessageDialog(null, "Você Explodiu!!");
                        restartGame();
                    }else {
                        showFields(i, j);
                    }
                    return;
                }
            }
        }

    }

    private void restartGame() {
        repaintTimer();

        ShadowButton.flags = 40;
        repaintRedFLag();

        repaintClickCount();

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                buttonList[i][j].setText("");
                buttonList[i][j].repaintIcon(null);
                minesLocation[i][j] = false;
                buttonList[i][j].setBackground(new Color(127, 127, 134, 100));
                distance[i][j] = 0;
            }
        }

        explosionClip.setFramePosition(0);
        generateNumberMInes();
        calculateProximity();
    }

    private void repaintClickCount() {
        clicksCount = 0;
        clicksCountLabel.setText(String.format("%03d", clicksCount));
        clicksCountLabel.repaint();
    }

    private void repaintTimer() {
        timerCount = 0;
        timerLabel.setText(String.format("%03d", timerCount));
        timerLabel.repaint();
    }

    private void repaintRedFLag(){
        redFlagLabel.setText(String.valueOf(ShadowButton.flags));
        redFlagLabel.repaint();
    }

    private void setClicksCount(){
        clicksCount++;
        clicksCountLabel.setText(String.format("%03d", clicksCount));
        clicksCountLabel.repaint();
    }

    private void showFields(int row, int col) {
        if(!buttonList[row][col].getText().isEmpty()){
            return;
        }

        if(distance[row][col] == 0) {
            buttonList[row][col].setText("");
            showAdjacentFields(row, col);
        }else{
            buttonList[row][col].setText(String.valueOf(distance[row][col]));
            buttonList[row][col].setBackground(Color.gray);
        }
    }

    private void showAdjacentFields(int row, int col) {
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},          {0, 1},
                {1, -1},  {1, 0}, {1, 1}
        };

        boolean[][] visited = new boolean[size][size];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{row, col});
        visited[row][col] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currentRow = current[0];
            int currentCol = current[1];

            // Revela a casa atual
            if (distance[currentRow][currentCol] == 0) {
                buttonList[currentRow][currentCol].setText("");
                buttonList[currentRow][currentCol].setBackground(Color.gray);
            } else {
                buttonList[currentRow][currentCol].setText(String.valueOf(distance[currentRow][currentCol]));
            }

            if (distance[currentRow][currentCol] == 0) {
                for (var dir : directions) {
                    int newRow = currentRow + dir[0];
                    int newCol = currentCol + dir[1];

                    if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                        if (!minesLocation[newRow][newCol] && !visited[newRow][newCol]) {
                            visited[newRow][newCol] = true;
                            if (distance[newRow][newCol] == 0) {
                                buttonList[newRow][newCol].repaintIcon(null);
                                queue.add(new int[]{newRow, newCol});
                            } else {
                                buttonList[newRow][newCol].setText(String.valueOf(distance[newRow][newCol]));
                                buttonList[newRow][newCol].setBackground(Color.gray);
                            }
                        }
                    }
                }
            }
        }
    }

    public void createButtons() {
        for (var i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                var button = new ShadowButton("");
                button.setFocusable(false);
                button.setPreferredSize(new Dimension(80, 120));
                button.setFont(new Font("Arial", Font.BOLD, 20));
                button.addActionListener(this);
                button.setBackground(new Color(127, 127, 134, 100));
                buttonList[i][j] = button;
                panelButton.add(button);
            }
        }
    }

    private void calculateProximity() {
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},          {0, 1},
                {1, -1},  {1, 0}, {1, 1}
        };

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!minesLocation[i][j]) {
                    int count = 0;

                    for (var dir : directions) {
                        int newRow = i + dir[0];
                        int newCol = j + dir[1];

                        if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size
                                && minesLocation[newRow][newCol]) {
                            count++;
                        }
                    }

                    distance[i][j] = count;
                } else {
                    distance[i][j] = -1;
                }
            }
        }
    }

    public void generateNumberMInes() {
        int addedMines = 0;

        while (addedMines < numberMInes) {
            int row = rand.nextInt(size);
            int col = rand.nextInt(size);

            if(!minesLocation[row][col]) {
                minesLocation[row][col] = true;
                buttonList[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                addedMines += 1;
            }
        }
    }


}
