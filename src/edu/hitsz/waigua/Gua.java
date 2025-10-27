package edu.hitsz.waigua;

import edu.hitsz.application.Game;
import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;
import edu.hitsz.frame.Ranking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

public class Gua {
    private JButton addSimpleScoreButton;
    private JTextArea enterYourNameTextArea;
    private JTextArea enterYouScoreTextArea;
    private JFormattedTextField nameTextField;
    private JFormattedTextField scoreTextField;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton addNormalScoreButton;
    private JButton addHardScoreButton;
    private String mode;

    private String name;
    private int score;

    public Gua() {

        // 创建Ranking窗口
        JFrame frame = new JFrame("Ranking");
        // 设置窗口大小为屏幕的x%
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.6);
        int height = (int) (screenSize.height * 0.9);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        Ranking rankingFrame = new Ranking();
        frame.setContentPane(rankingFrame.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.pack();
        frame.setVisible(true);

        addSimpleScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    name = nameTextField.getText();
                    score = Integer.parseInt(scoreTextField.getText());
                    if (name != null) {
                        // 创建Player对象（ID设为0，rank暂时设为0，后续计算）
                        Player currentPlayer = new Player(name, score, LocalDateTime.now());
                        // 创建PlayerDao实例
                        PlayerDao simplePlayerDao = new PlayerDaoImpl(Game.GameMode.SIMPLE);
//                        ((PlayerDaoImpl) simplePlayerDao).setMode(Game.GameMode.SIMPLE);
                        // 添加玩家记录
                        simplePlayerDao.addPlayer(currentPlayer);
                        // 更新当前窗口
                        rankingFrame.updateSimpleModeTable();
                    }
                }
                catch (Exception error) {
                    System.err.println("记录分数时出错: " + error.getMessage());
                }
            }
        });

        addNormalScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    name = nameTextField.getText();
                    score = Integer.parseInt(scoreTextField.getText());
                    if (name != null) {
                        // 创建Player对象（ID设为0，rank暂时设为0，后续计算）
                        Player currentPlayer = new Player(name, score, LocalDateTime.now());
                        // 创建PlayerDao实例
                        PlayerDao normalPlayerDao = new PlayerDaoImpl(Game.GameMode.NORMAL);
//                        ((PlayerDaoImpl) normalPlayerDao).setMode(Game.GameMode.NORMAL);
                        // 添加玩家记录
                        normalPlayerDao.addPlayer(currentPlayer);
                        // 更新当前窗口
                        rankingFrame.updateNormalModeTable();
                    }
                }
                catch (Exception error) {
                    System.err.println("记录分数时出错: " + error.getMessage());
                }
            }
        });

        addHardScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    name = nameTextField.getText();
                    score = Integer.parseInt(scoreTextField.getText());
                    if (name != null) {
                        // 创建Player对象（ID设为0，rank暂时设为0，后续计算）
                        Player currentPlayer = new Player(name, score, LocalDateTime.now());
                        // 创建PlayerDao实例
                        PlayerDao hardPlayerDao = new PlayerDaoImpl(Game.GameMode.HARD);
//                        ((PlayerDaoImpl) hardPlayerDao).setMode(Game.GameMode.HARD);
                        // 添加玩家记录
                        hardPlayerDao.addPlayer(currentPlayer);
                        // 更新当前窗口
                        rankingFrame.updateHardModeTable();
                    }
                }
                catch (Exception error) {
                    System.err.println("记录分数时出错: " + error.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("外挂");
        frame.setContentPane(new Gua().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
