package edu.hitsz.network;

class PlayerState {
    float x;
    float y;
    int hp;
}

class BulletState {
    float x;
    float y;
    float vx;
    float vy;
    int owner;
}

class EnemyState {
    float x;
    float y;
    float vx;
    float vy;
    int hp;
    int maxHp;
    boolean boss;
    int kind;
    int score;
}
