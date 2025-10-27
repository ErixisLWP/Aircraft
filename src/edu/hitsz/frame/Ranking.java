package edu.hitsz.frame;

import edu.hitsz.application.Game;
import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Ranking extends JFrame {
    private JTable simpleRankTable;
    private JButton simpleModeDeleteButton;
    private JScrollPane simpleTableScrollPanel;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JTextArea simpleModeText;
    private JTextArea hardModeText;
    private JTextArea normalModeText;
    private JTable normalRankTable;
    private JTable hardRankTable;
    private JButton normalModeDeleteButton;
    private JButton hardModeDeleteButton;
    private JScrollPane normalTableScrollPanel;
    private JScrollPane hardTableScrollPanel;

    // 表格信息
    private String[] columnName = {"Rank", "Player", "Score", "RecordTime"};
    private String[][] simpleModePlayerInfo;
    private String[][] normalModePlayerInfo;
    private String[][] hardModePlayerInfo;

    //表格模型
    private DefaultTableModel simpleModeModel;
    private DefaultTableModel normalModeModel;
    private DefaultTableModel hardModeModel;

    public Ranking() {
        // 更新表格
        updateSimpleModeTable();
        updateNormalModeTable();
        updateHardModeTable();

        // 删除选中行成绩
        simpleModeDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = simpleRankTable.getSelectedRow();
                System.out.println(row);
                int result = JOptionPane.showConfirmDialog(simpleModeDeleteButton, "是否确定删除选中成绩？");
                if(result == JOptionPane.YES_OPTION && row != -1) {
                    System.out.println(simpleRankTable.getValueAt(row, 1));
                    PlayerDao simpleModePlayerDao = new PlayerDaoImpl(Game.GameMode.SIMPLE);
//                    ((PlayerDaoImpl) simpleModePlayerDao).setMode(Game.GameMode.SIMPLE);
                    simpleModePlayerDao.deletePlayerByIndex(row);
                    simpleModeModel.removeRow(row);
                }
            }
        });

        normalModeDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = normalRankTable.getSelectedRow();
                System.out.println(row);
                int result = JOptionPane.showConfirmDialog(normalModeDeleteButton, "是否确定删除选中成绩？");
                if(result == JOptionPane.YES_OPTION && row != -1) {
                    System.out.println(normalRankTable.getValueAt(row, 1));
                    PlayerDao normalModePlayerDao = new PlayerDaoImpl(Game.GameMode.NORMAL);
//                    ((PlayerDaoImpl) normalModePlayerDao).setMode(Game.GameMode.NORMAL);
                    normalModePlayerDao.deletePlayerByIndex(row);
                    normalModeModel.removeRow(row);
                }
            }
        });

        hardModeDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = hardRankTable.getSelectedRow();
                System.out.println(row);
                int result = JOptionPane.showConfirmDialog(hardModeDeleteButton, "是否确定删除选中成绩？");
                if(result == JOptionPane.YES_OPTION && row != -1) {
                    System.out.println(hardRankTable.getValueAt(row, 1));
                    PlayerDao hardModePlayerDao = new PlayerDaoImpl(Game.GameMode.HARD);
//                    ((PlayerDaoImpl) hardModePlayerDao).setMode(Game.GameMode.HARD);
                    hardModePlayerDao.deletePlayerByIndex(row);
                    hardModeModel.removeRow(row);
                }
            }
        });
    }

    // 更新表格
    public void updateSimpleModeTable() {
        PlayerDao simpleModePlayerDao = new PlayerDaoImpl(Game.GameMode.SIMPLE);
//        ((PlayerDaoImpl) simpleModePlayerDao).setMode(Game.GameMode.SIMPLE);
        simpleModePlayerInfo = convertPlayersToTableData(simpleModePlayerDao.getAllPlayers());
        simpleModeModel = new DefaultTableModel(simpleModePlayerInfo, columnName){
            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        };
        simpleRankTable.setModel(simpleModeModel);
        simpleTableScrollPanel.setViewportView(simpleRankTable);
    }

    public void updateNormalModeTable() {
        PlayerDao normalModePlayerDao = new PlayerDaoImpl(Game.GameMode.NORMAL);
//        ((PlayerDaoImpl) normalModePlayerDao).setMode(Game.GameMode.NORMAL);
        normalModePlayerInfo = convertPlayersToTableData(normalModePlayerDao.getAllPlayers());
        normalModeModel = new DefaultTableModel(normalModePlayerInfo, columnName){
            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        };
        normalRankTable.setModel(normalModeModel);
        normalTableScrollPanel.setViewportView(normalRankTable);
    }

    public void updateHardModeTable() {
        PlayerDao hardModePlayerDao = new PlayerDaoImpl(Game.GameMode.HARD);
//        ((PlayerDaoImpl) hardModePlayerDao).setMode(Game.GameMode.HARD);
        hardModePlayerInfo = convertPlayersToTableData(hardModePlayerDao.getAllPlayers());
        hardModeModel = new DefaultTableModel(hardModePlayerInfo, columnName){
            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        };
        hardRankTable.setModel(hardModeModel);
        hardTableScrollPanel.setViewportView(hardRankTable);
    }

    // 制作String二维数组
    public static String[][] convertPlayersToTableData(List<Player> players) {
        String[][] tableData = new String[players.size()][4]; // 4列: Rank, Player, Score, RecordTime
        for (int i = 0; i < players.size(); i++) {
            // 获取当前索引的Player
            Player player = players.get(i);
            tableData[i][0] = String.valueOf(player.getRank());       // 排名
            tableData[i][1] = player.getName();                        // 玩家名
            tableData[i][2] = String.valueOf(player.getScore());       // 分数
            // 格式化时间输出
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            tableData[i][3] = player.getRecordTime().format(formatter);       // 时间
        }
        return tableData;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ranking");
        // 设置窗口大小为屏幕的x%
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.9);
        int height = (int) (screenSize.height * 0.9);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(new Ranking().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
        frame.setVisible(true);
    }
}
