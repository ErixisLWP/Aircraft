package edu.hitsz.dao;

import edu.hitsz.application.Game;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDaoImpl implements PlayerDao {

    private List<Player> players;
    private Game.GameMode gameMode;
    private static final String DATA_FILE_SIMPLE = "data/player_records_simple.dat"; // 数据文件
    private static final String DATA_FILE_NORMAL = "data/player_records_normal.dat"; // 数据文件
    private static final String DATA_FILE_HARD = "data/player_records_hard.dat"; // 数据文件

    private String DATA_FILE;

    public PlayerDaoImpl(Game.GameMode gameMode) {
        if (gameMode == null) {
            gameMode = Game.GameMode.SIMPLE;
        }
        if (gameMode == Game.GameMode.SIMPLE) {
            DATA_FILE = DATA_FILE_SIMPLE;
        } else if (gameMode == Game.GameMode.NORMAL) {
            DATA_FILE = DATA_FILE_NORMAL;
        } else {
            DATA_FILE = DATA_FILE_HARD;
        }
        players = new ArrayList<Player>();
        loadPlayersFromFile(DATA_FILE); // 启动时加载已有记录
    }

//    public void setMode(Game.GameMode mode) {
//        this.gameMode = mode;
//    }

    @Override
    public void addPlayer(Player player) {
        players.add(player);
            savePlayersToFile(DATA_FILE); // 保存到文件
    }

    @Override
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players); // 返回副本
    }

    @Override
    public Player getPlayerByName(String name) {
        return players.stream()                                         // 将List转为Stream
                .filter(player -> player.getName().equals(name))  // 过滤出符合条件的元素
                .findFirst()    // 返回第一个符合条件的元素
                .orElse(null);  // 如果没有符合条件的元素则返回null
    }

    // 其他方法实现...
    private void loadPlayersFromFile(String file) {
        // 实现从文件加载玩家记录的逻辑
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            players = (List<Player>) ois.readObject();
            // 根据分数由高到低排序并赋值排名属性
            this.sortPlayerByScore(players);
        } catch (FileNotFoundException e) {
            // 文件不存在，第一次运行，使用空列表
            players = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("加载玩家记录失败: " + e.getMessage());
            players = new ArrayList<>();
        }
    }

    private void savePlayersToFile(String file) {
        // 实现保存玩家记录到文件的逻辑
        // 存数据前先根据分数由高到低排序并赋值排名属性
        this.sortPlayerByScore(players);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(players);
        } catch (Exception e) {
            System.err.println("保存玩家记录失败: " + e.getMessage());
        }
    }

    private void sortPlayerByScore(List<Player> players) {
        players.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.setRank(i + 1);
        }
    }

    @Override
    public void updatePlayer(Player player) {
        // 实现更新逻辑
        deletePlayerByName(player.getName());
        players.add(player);
        savePlayersToFile(DATA_FILE);
    }

    @Override
    public void deletePlayerByName(String name) {
        players.removeIf(player -> player.getName().equals(name));
        savePlayersToFile(DATA_FILE);
    }

    @Override
    public void deletePlayerByIndex(int index) {
        players.removeIf(player -> player.getRank() == index + 1);
        savePlayersToFile(DATA_FILE);
    }
}
