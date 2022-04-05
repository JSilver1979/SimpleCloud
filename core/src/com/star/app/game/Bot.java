package com.star.app.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.star.app.game.helpers.Poolable;
import com.star.app.screen.utils.Assets;

public class Bot extends Ship implements Poolable {
    private boolean active;
    private Vector2 tempVec;

    @Override
    public boolean isActive() {
        return active;
    }

    public Bot(GameController gc) {
        super(gc, 300, 30 + gc.getLevel() * 20);
        this.texture = Assets.getInstance().getAtlas().findRegion("ship");
        this.position = new Vector2(100, 100);
        this.hitArea = new Circle(position, 28);
        this.active = false;
        this.tempVec = new Vector2();
        this.ownerType = OwnerType.BOT;
        this.weaponNum = MathUtils.random(gc.getLevel() + 1);
        this.currentWeapon = weapons[weaponNum];
    }

    public void activate(float x, float y) {
        position.set(x, y);
        hpMax = 30 + gc.getLevel() * 20;
        hp = hpMax;
        active = true;
    }


    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x - 32, position.y - 32, 32, 32,
                64, 64, 1, 1, angle);
    }

    public void update(float dt) {
        super.update(dt);

        if (!isAlive()) {
            active = false;
        }

        tempVec.set(gc.getHero().getPosition()).sub(position).nor();
        angle = tempVec.angleDeg();

        if (gc.getHero().getPosition().dst(position) > 200) {
            accelerate(dt);

            float bx = position.x + MathUtils.cosDeg(angle + 180) * 25;
            float by = position.y + MathUtils.sinDeg(angle + 180) * 25;

            for (int i = 0; i < 3; i++) {
                gc.getParticleController().setup(bx + MathUtils.random(-4, 4), by + MathUtils.random(-4, 4),
                        velocity.x * -0.1f + MathUtils.random(-20, 20), velocity.y * -0.1f + MathUtils.random(-20, 20),
                        0.4f,
                        1.2f, 0.2f,
                        0.0f, 0.5f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f, 0.0f);
            }
        }


        if (gc.getHero().getPosition().dst(position) < 300) {
            tryToFire();
        }
    }
}
