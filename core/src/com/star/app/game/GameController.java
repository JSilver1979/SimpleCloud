package com.star.app.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.star.app.screen.ScreenManager;
import com.star.app.screen.utils.Assets;

public class GameController {
    private Background background;
    private BulletController bulletController;
    private AsteroidController asteroidController;
    private ParticleController particleController;
    private PowerUpsController powerUpsController;
    private InfoController infoController;
    private BotController botController;
    private Hero hero;
    private Vector2 tempVec;
    private Stage stage;
    private boolean pause;
    private int level;
    private float timer;
    private Music music;


    public float getTimer() {
        return timer;
    }

    public int getLevel() {
        return level;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public Stage getStage() {
        return stage;
    }

    public BotController getBotController() {
        return botController;
    }

    public InfoController getInfoController() {
        return infoController;
    }

    public PowerUpsController getPowerUpsController() {
        return powerUpsController;
    }

    public ParticleController getParticleController() {
        return particleController;
    }

    public AsteroidController getAsteroidController() {
        return asteroidController;
    }

    public BulletController getBulletController() {
        return bulletController;
    }

    public Background getBackground() {
        return background;
    }

    public Hero getHero() {
        return hero;
    }

    public GameController(SpriteBatch batch) {
        this.background = new Background(this);
        this.bulletController = new BulletController(this);
        this.asteroidController = new AsteroidController(this);
        this.particleController = new ParticleController();
        this.powerUpsController = new PowerUpsController(this);
        this.infoController = new InfoController();
        this.botController = new BotController(this);
        this.hero = new Hero(this);
        this.tempVec = new Vector2();
        this.stage = new Stage(ScreenManager.getInstance().getViewport(), batch);
        this.stage.addActor(hero.getShop());
        Gdx.input.setInputProcessor(stage);
        this.level = 1;
        generateBigAsteroids(2);

        botController.setup(100, 100);

        this.music = Assets.getInstance().getAssetManager().get("audio/mortal.mp3");
        this.music.setLooping(true);
        this.music.play();

    }

    public void generateBigAsteroids(int count) {
        for (int i = 0; i < count; i++) {
            asteroidController.setup(MathUtils.random(0, ScreenManager.SCREEN_WIDTH),
                    MathUtils.random(0, ScreenManager.SCREEN_HEIGHT),
                    MathUtils.random(-150, 150), MathUtils.random(-150, 150), 1.0f);
        }
    }

    public void update(float dt) {
        if (pause) {
            return;
        }
        timer += dt;
        background.update(dt);
        bulletController.update(dt);
        asteroidController.update(dt);
        particleController.update(dt);
        powerUpsController.update(dt);
        infoController.update(dt);
        botController.update(dt);
        hero.update(dt);
        stage.act(dt);
        checkCollisions();
        if (!hero.isAlive()) {
            ScreenManager.getInstance().changeScreen(ScreenManager.ScreenType.GAMEOVER, hero);
        }
        if (asteroidController.getActiveList().size() == 0) {
            level++;
            generateBigAsteroids(level + 2);
            timer = 0;
        }
    }


    public void checkCollisions() {
        //столкновение астероидов и героя
        for (int i = 0; i < asteroidController.getActiveList().size(); i++) {
            Asteroid a = asteroidController.getActiveList().get(i);
            if (hero.getHitArea().overlaps(a.getHitArea())) {
                float dst = a.getPosition().dst(hero.getPosition());
                float halfOverLen = (a.getHitArea().radius + hero.getHitArea().radius - dst) / 2.0f;
                tempVec.set(hero.getPosition()).sub(a.getPosition()).nor();
                hero.getPosition().mulAdd(tempVec, halfOverLen);
                a.getPosition().mulAdd(tempVec, -halfOverLen);

                float sumScl = hero.getHitArea().radius * 2 + a.getHitArea().radius;
                hero.getVelocity().mulAdd(tempVec, 200.0f * a.getHitArea().radius / sumScl);
                a.getVelocity().mulAdd(tempVec, -200.0f * hero.getHitArea().radius / sumScl);

                if (a.takeDamage(2)) {
                    hero.addScore(a.getHpMax() * 50);
                }
                hero.takeDamage(2 * level);
            }
        }

        //столкновение астероидов и ботов
        for (int i = 0; i < asteroidController.getActiveList().size(); i++) {
            Asteroid a = asteroidController.getActiveList().get(i);
            for (int j = 0; j < botController.getActiveList().size(); j++) {
                Bot b = botController.getActiveList().get(j);

                if (b.getHitArea().overlaps(a.getHitArea())) {
                    float dst = a.getPosition().dst(b.getPosition());
                    float halfOverLen = (a.getHitArea().radius + b.getHitArea().radius - dst) / 2.0f;
                    tempVec.set(b.getPosition()).sub(a.getPosition()).nor();
                    b.getPosition().mulAdd(tempVec, halfOverLen);
                    a.getPosition().mulAdd(tempVec, -halfOverLen);

                    float sumScl = b.getHitArea().radius * 2 + a.getHitArea().radius;
                    b.getVelocity().mulAdd(tempVec, 200.0f * a.getHitArea().radius / sumScl);
                    a.getVelocity().mulAdd(tempVec, -200.0f * b.getHitArea().radius / sumScl);

                    a.takeDamage(1);
                    b.takeDamage(level);
                }
            }
        }

        //столкновение пуль и астероидов
        for (int i = 0; i < bulletController.getActiveList().size(); i++) {
            Bullet b = bulletController.getActiveList().get(i);
            for (int j = 0; j < asteroidController.getActiveList().size(); j++) {
                Asteroid a = asteroidController.getActiveList().get(j);
                if (a.getHitArea().contains(b.getPosition())) {
                    particleController.getEffectBuilder().bulletCollideWithAsteroid(b);

                    b.deactivate();
                    if (a.takeDamage(b.getOwner().getCurrentWeapon().getDamage())) {
                        if (b.getOwner().getOwnerType() == OwnerType.PLAYER) {
                            hero.addScore(a.getHpMax() * 100);
                            for (int k = 0; k < 3; k++) {
                                powerUpsController.setup(a.getPosition().x, a.getPosition().y, a.getScale() * 0.25f);
                            }
                            if (MathUtils.random(0, 100) < 10 * a.getScale()) {
                                botController.setup(a.getPosition().x, a.getPosition().y);
                            }
                        }
                    }
                    break;
                }
            }
        }

        // Столкновение поверапсов и героя
        for (int i = 0; i < powerUpsController.getActiveList().size(); i++) {
            PowerUp pu = powerUpsController.getActiveList().get(i);
            if (hero.getMagneticField().contains(pu.getPosition())) {
                tempVec.set(hero.getPosition()).sub(pu.getPosition()).nor();
                pu.getVelocity().mulAdd(tempVec, 100);
            }

            if (hero.getHitArea().contains(pu.getPosition())) {
                hero.consume(pu);
                particleController.getEffectBuilder().takePowerUpsEffect(pu);
                pu.deactivate();
            }
        }

        //столкновение пуль и кораблей
        for (int i = 0; i < bulletController.getActiveList().size(); i++) {
            Bullet b = bulletController.getActiveList().get(i);

            if (b.getOwner().getOwnerType() == OwnerType.BOT) {
                if (hero.getHitArea().contains(b.getPosition())) {
                    hero.takeDamage(b.getOwner().getCurrentWeapon().getDamage());
                    b.deactivate();
                }
            }

            if (b.getOwner().getOwnerType() == OwnerType.PLAYER) {
                for (int j = 0; j < botController.getActiveList().size(); j++) {
                    Bot bot = botController.getActiveList().get(j);
                    if (bot.getHitArea().contains(b.getPosition())) {
                        bot.takeDamage(b.getOwner().getCurrentWeapon().getDamage());
                        b.deactivate();
                    }
                }
            }
        }
    }

    public void dispose() {
        background.dispose();
    }
}
