package edu.hitsz.frame;

import edu.hitsz.application.AudioManager;
import edu.hitsz.application.Game;
import edu.hitsz.application.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Console;
import java.util.Objects;


public class ModeSelect extends  JFrame {
    private JButton simpleModeButton;
    private JButton hardModeButton;
    private JButton normalModeButton;
    private JComboBox audioEffect;
    private JPanel mainPanel;
    private JTextArea audioEffectTextArea;

    public ModeSelect() {
        audioEffect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) audioEffect.getSelectedItem();
                AudioManager.audioEffect = Objects.equals(selectedItem, "开");
            }
        });

        simpleModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Game.gameMode = Game.GameMode.SIMPLE;
                StartGame();
            }
        });

        normalModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Game.gameMode = Game.GameMode.NORMAL;
                StartGame();
            }
        });

        hardModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Game.gameMode = Game.GameMode.HARD;
                StartGame();
            }
        });
    }

    private void StartGame() {
        Main.frame.remove(mainPanel);
        Game game = new Game();
        Main.frame.add(game);
        // 刷新并重绘窗口，使新面板可见
        Main.frame.revalidate();
        Main.frame.repaint();
        game.action();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ModeSelect");
        // 设置窗口大小为屏幕的x%
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.3);
        int height = (int) (screenSize.height * 0.6);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(new ModeSelect().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
        frame.setVisible(true);
    }
}
