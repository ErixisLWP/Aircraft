package edu.hitsz.application;

import edu.hitsz.Factories.*;
import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.dao.Player;
import edu.hitsz.dao.PlayerDao;
import edu.hitsz.dao.PlayerDaoImpl;
import edu.hitsz.frame.Ranking;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.strategy.NormalShootStrategy;
import edu.hitsz.strategy.ShootStrategy;
import edu.hitsz.thread.MusicThread;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    public enum GameMode {SIMPLE, NORMAL, HARD};

    public static GameMode gameMode;

    private int backGroundTop = 0;

    /**
     * Scheduled 线程池，用于任务调度
     */
    private final ScheduledExecutorService executorService;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 40;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<BaseProp> props;

    /**
     * 屏幕中出现的敌机最大数量
     */
    private int enemyMaxNumber = 5;

    /**
     * 精英敌机刷新概率
     */
    private float eliteEnemyProbability = 0.2f;

    /**
     * 精英敌机升级版刷新概率
     */
    private float elitePlusEnemyProbability = 0.05f;

    /**
     *  boss敌机刷新阈值，每bossThreshold分出现一只boss
     *  若不需要boss敌机，可设置一个很大的值
     */
    private int bossThreshold = 500;

    /**
     * score / bossThreshold 大于bossAppearCount时，产生一只boss，bossAppearCount加1
     * 用于控制boss敌机的产生
     */
    private int bossAppearCount = 0;

    /**
     * 当前得分
     */
    private int score = 0;
    /**
     * 当前时刻
     */
    private int time = 0;

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    private int cycleDuration = 600;
    private int cycleTime = 0;

    /**
     * 游戏结束标志
     */
    private boolean gameOverFlag = false;

    // 各个工厂
//    EnemyCreator mobEnemyCreator = new MobEnemyCreator();
//    EnemyCreator eliteEnemyCreator = new EliteEnemyCreator();
    public EnemyCreator enemyCreator;
//    PropCreator bloodPropCreator = new BloodPropCreator();
//    PropCreator bombPropCreator = new BombPropCreator();
//    PropCreator bulletPropCreator = new BulletPropCreator();
//    public PropCreator propCreator;


    public Game() {
        heroAircraft = HeroAircraft.getInstance();
        heroAircraft.setShootStrategy(new NormalShootStrategy());

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        /**
         * Scheduled 线程池，用于定时任务调度
         * 关于alibaba code guide：可命名的 ThreadFactory 一般需要第三方包
         * apache 第三方库： org.apache.commons.lang3.concurrent.BasicThreadFactory
         */
        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {

        // bgm线程
        AudioManager.playBgm();

        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {

            time += timeInterval;


            // 周期性执行（控制频率）
            if (timeCountAndNewCycleJudge()) {
                System.out.println(time);
                // 新敌机产生
                CreateNewEnemy();
                // 飞机射出子弹
                shootAction();
                // 检测道具剩余时间
                checkPropDuration();
            }

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            // 道具移动
            propsMoveAction();

            // 撞击检测
            crashCheckAction();

            // 后处理
            postProcessAction();

            //每个时刻重绘界面
            repaint();

            // 游戏结束检查英雄机是否存活
            if (heroAircraft.getHp() <= 0) {
                // 游戏结束
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");

                // 游戏结束音效
                AudioManager.playGameOverSound();

                // 游戏结束窗口
                GameOverDialog();
            }

        };

        /**
         * 以固定延迟时间进行执行
         * 本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
         */
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }

    /**
     * 游戏结束窗口，可保存成绩、删除成绩、查看排行榜
     */
    private void GameOverDialog() {
        try {
            // 创建PlayerDao实例
            PlayerDao playerDao = new PlayerDaoImpl();

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

            // 创建保存成绩窗口
            String name = JOptionPane.showInputDialog("游戏结束！你的得分为" + score + "。是否输入昵称保存？");

            if (name != null) {
                // 创建Player对象（ID设为0，rank暂时设为0，后续计算）
                Player currentPlayer = new Player(name, score, LocalDateTime.now());
                // 添加玩家记录
                playerDao.addPlayer(currentPlayer);
                // 更新当前窗口
                rankingFrame.updateTable();
            }

            // 打印排行榜（按分数从高到低）
            printScoreRanking(playerDao);

        } catch (Exception e) {
            System.err.println("记录分数时出错: " + e.getMessage());
        }
    }

    /**
     * 打印得分排行榜
     */
    private void printScoreRanking(PlayerDao playerDao) {
        System.out.println("\n======================= 得分排行榜 =======================");

        // 获取所有玩家记录
        List<Player> allPlayers = playerDao.getAllPlayers();

        // 按分数从高到低排序（已排序）
        // allPlayers.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        // 更新排名并打印
        System.out.printf("%-6s%-8s%-40s%-24s%n", "排名", "分数", "玩家名", "时间");
        System.out.println("--------------------------------------------------------");

        for (int i = 0; i < allPlayers.size(); i++) {
            Player player = allPlayers.get(i);
//            player.setRank(i + 1); // 设置实际排名

            // 格式化时间输出
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = player.getRecordTime().format(formatter);

            System.out.printf("%-6d%-8d%-40s%-24s%n",
                    player.getRank(),
                    player.getScore(),
                    player.getName(),
                    formattedTime);
        }
        System.out.println("========================================================\n");
    }

    //***********************
    //      Action 各部分
    //***********************

    private void CreateNewEnemy() {
        if (enemyAircrafts.size() < enemyMaxNumber) {
            if (Math.random() < elitePlusEnemyProbability) {
                enemyCreator = new ElitePlusEnemyCreator();
                enemyAircrafts.add(enemyCreator.createEnemy());
            }
            else if (Math.random() < eliteEnemyProbability + elitePlusEnemyProbability) {
                enemyCreator = new EliteEnemyCreator();
                enemyAircrafts.add(enemyCreator.createEnemy());
            }
            else {
                enemyCreator = new MobEnemyCreator();
                enemyAircrafts.add(enemyCreator.createEnemy());
            }
        }
        // boss敌机产生
        if (score / bossThreshold > bossAppearCount) {
            // 播放boss背景音乐
            AudioManager.playBossBgm();
            enemyCreator = new BossEnemyCreator();
            enemyAircrafts.add(enemyCreator.createEnemy());
            bossAppearCount++;
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            // 跨越到新的周期
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    private void shootAction() {
        // TODO 敌机射击
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (!(enemy instanceof MobEnemy)) {
                enemyBullets.addAll(enemy.shoot());
            }
        }
        // 英雄射击
        heroBullets.addAll(heroAircraft.shoot());
        AudioManager.playShootSound();
    }

    private void checkPropDuration() {
        if (heroAircraft.getPropEffectiveEndTime() < System.currentTimeMillis()) {
            heroAircraft.setShootStrategy(new NormalShootStrategy());
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    private void propsMoveAction() {
        for (BaseProp prop : props) {
            prop.forward();
        }
    }


    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // TODO 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                // 音效
                AudioManager.playBulletHitSound();
                // 根据攻击力扣血
                heroAircraft.decreaseHp(bullet.getPower());
                // 伤害判定仅生效一次
                bullet.vanish();
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 音效
                    AudioManager.playBulletHitSound();
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // TODO 获得分数，产生道具补给
                        score += ((Enemy) enemyAircraft).getScore();
                        ((Enemy) enemyAircraft).dropProp(props, enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
                    }
                }
                // 英雄机 与 敌机 相撞，均损毁
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // Todo: 我方获得道具，道具生效
        for (BaseProp prop : props) {
            if (prop.crash(heroAircraft) || heroAircraft.crash(prop)) {
                // 道具生效并销毁
                prop.takeEffect(heroAircraft);
            }
        }
    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * <p>
     * 无效的原因可能是撞击或者飞出边界
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }


    //***********************
    //      Paint 各部分
    //***********************

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param  g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        BufferedImage image = gameMode == GameMode.SIMPLE ? ImageManager.BACKGROUND_IMAGE_SIMPLE :
                                gameMode == GameMode.NORMAL ? ImageManager.BACKGROUND_IMAGE_NORMAL :
                                    ImageManager.BACKGROUND_IMAGE_HARD;
        g.drawImage(image, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(image, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        // 道具要先绘制，否则会遮挡子弹
        paintImageWithPositionRevised(g, props);
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);

        paintImageWithPositionRevised(g, enemyAircrafts);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        // 为英雄机绘制血条
        drawHealthBarsForHeroAircraft(g);

        // 为敌机绘制血条
        drawHealthBarsForEnemies(g);

        // 绘制得分和生命值
        paintScoreAndLife(g);

        // 绘制 buff 剩余时间条
        paintBuffTimerBar(g);

    }
    /**
     * 为英雄机绘制血条
     */
    private void drawHealthBarsForHeroAircraft(Graphics g) {
        drawHealthBar(g, heroAircraft, Color.GREEN);
    }

    /**
     * 为所有敌机绘制血条
     */
    private void drawHealthBarsForEnemies(Graphics g) {
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (!enemy.notValid()) {
                // 只为存活的敌机绘制
                drawHealthBar(g, enemy, Color.RED);
            }
        }
    }

    /**
     * 绘制 buff 剩余时间条
     */
    private void paintBuffTimerBar(Graphics g) {
        long endTime = heroAircraft.getPropEffectiveEndTime();
        long currentTime = System.currentTimeMillis();

        if (endTime > currentTime) {
            long totalDuration = heroAircraft.getPropDuration();
            long remainingTime = endTime - currentTime;
            double percentage = (double) remainingTime / totalDuration;

            int barWidth = 200;
            int barHeight = 15;
            int x = (Main.WINDOW_WIDTH - barWidth) / 2;
            int y = Main.WINDOW_HEIGHT - 50; // 绘制在窗口底部

            // 绘制背景
            g.setColor(Color.GRAY);
            g.fillRect(x, y, barWidth, barHeight);

            // 绘制前景
            g.setColor(Color.CYAN);
            g.fillRect(x, y, (int) (barWidth * percentage), barHeight);

            // 绘制边框
            g.setColor(Color.WHITE);
            g.drawRect(x, y, barWidth, barHeight);

            // 绘制文字
            g.setColor(Color.BLACK);
            g.drawString("buff剩余时间", x + 35, y + barHeight - 2);
        }
    }


    /**
     * 可复用的绘制条方法，用于绘制血条
     * @param g 画笔
     * @param aircraft 目标飞行器
     * @param color 条的颜色
     */
    private void drawHealthBar(Graphics g, AbstractAircraft aircraft, Color color) {
        int hp = aircraft.getHp();
        int maxHp = aircraft.getMaxHp();
        if (hp <= 0 || hp == maxHp) {
            // 血量为0或满血时，不绘制血条
            return;
        }

        int barWidth = aircraft.getWidth(); // 血条宽度与飞机宽度一致
        int barHeight = 5;
        // 血条绘制在飞机下方5像素处
        int barX = aircraft.getLocationX() - barWidth / 2;
        int barY = aircraft.getLocationY() + aircraft.getHeight() / 2 + barHeight + 5;

        // 计算血量百分比
        double percentage = (double) hp / maxHp;

        // 绘制血条背景（代表已损失的血量）
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);

        // 绘制血条前景（代表剩余血量）
        g.setColor(color);
        g.fillRect(barX, barY, (int) (barWidth * percentage), barHeight);
    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
    }


}
