package edu.hitsz.dao;

import java.util.List;

public interface PlayerDao {
    public abstract List<Player> getAllPlayers();
    public abstract Player getPlayerByName(String name);
    public abstract void addPlayer(Player player);
    public abstract void updatePlayer(Player player);
    public abstract void deletePlayerByName(String name);
}
