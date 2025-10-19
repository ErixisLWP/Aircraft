package edu.hitsz.frame;

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
    private JTable rankTable;
    private JButton deleteButton;
    private JScrollPane tableScrollPanel;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;

    // 表格信息
    private String[] columnName = {"Rank", "Player", "Score", "RecordTime"};
    private String[][] playerInfo;

    //表格模型
    private DefaultTableModel model;

    public Ranking() {
        // 更新表格
        updateTable();

        // 删除选中行成绩
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = rankTable.getSelectedRow();
                System.out.println(row);
                int result = JOptionPane.showConfirmDialog(deleteButton, "是否确定删除选中成绩？");
                if(result == JOptionPane.YES_OPTION && row != -1) {
                    System.out.println(rankTable.getValueAt(row, 1));
                    PlayerDao playerDao = new PlayerDaoImpl();
                    playerDao.deletePlayerByIndex(row);
                    model.removeRow(row);
                }
            }
        });
    }

    // 更新表格
    public void updateTable() {
        PlayerDao playerDao = new PlayerDaoImpl();
        playerInfo = convertPlayersToTableData(playerDao.getAllPlayers());
        model = new DefaultTableModel(playerInfo, columnName){
            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        };
        rankTable.setModel(model);
        tableScrollPanel.setViewportView(rankTable);
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
        int width = (int) (screenSize.width * 0.6);
        int height = (int) (screenSize.height * 0.9);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(new Ranking().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
        frame.setVisible(true);
    }
}
