package edu.hitsz.dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDaoImpl implements PlayerDao {

    private List<Player> players;
    private static final String DATA_FILE = "data/player_records.dat"; // 数据文件

    public PlayerDaoImpl() {
        players = new ArrayList<Player>();
        loadPlayersFromFile(); // 启动时加载已有记录
    }

    @Override
    public void addPlayer(Player player) {
        players.add(player);
        savePlayersToFile(); // 保存到文件
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
    private void loadPlayersFromFile() {
        // 实现从文件加载玩家记录的逻辑
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            players = (List<Player>) ois.readObject();
        } catch (FileNotFoundException e) {
            // 文件不存在，第一次运行，使用空列表
            players = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("加载玩家记录失败: " + e.getMessage());
            players = new ArrayList<>();
        }
    }

    private void savePlayersToFile() {
        // 实现保存玩家记录到文件的逻辑
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(players);
        } catch (Exception e) {
            System.err.println("保存玩家记录失败: " + e.getMessage());
        }
    }

    @Override
    public void updatePlayer(Player player) {
        // 实现更新逻辑
        deletePlayerByName(player.getName());
        players.add(player);
        savePlayersToFile();
    }

    @Override
    public void deletePlayerByName(String name) {
        players.removeIf(player -> player.getName().equals(name));
        savePlayersToFile();
    }
}
