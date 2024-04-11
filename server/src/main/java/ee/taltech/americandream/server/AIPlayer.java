package ee.taltech.americandream.server;

import helper.BulletData;

import java.util.ArrayList;
import java.util.List;

import static helper.Constants.*;

public class AIPlayer {
    private final List<BulletData> bullets;
    private float x;
    private float y;
    private float velocity = 100;
    private float shootCountdown = 0;
    private float knockback = 0;

    public AIPlayer(float x, float y) {
        this.x = x;
        this.y = y;
        this.bullets = new ArrayList<>();
    }

    public List<BulletData> getBullets() {
        return bullets;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void update(float delta, Player[] players) {
        // select player closest to
        Player closestPlayer = null;
        float closestDistance = Float.MAX_VALUE;
        for (Player player : players) {
            float distance = (float) Math.sqrt(Math.pow(player.getState().x - x, 2) + Math.pow(player.getState().y - y, 2));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        if (closestPlayer == null) {
            return;
        }

        // set velocity according to:
        // when distance is higher -> velocity higher
        // when distance is lower -> velocity lower
        velocity = closestDistance;

        // move towards player
        float angle = (float) Math.atan2(closestPlayer.getState().y - y, closestPlayer.getState().x - x);

        x += (float) (Math.cos(angle) * velocity * delta);
        y += (float) (Math.sin(angle) * velocity * delta);

        // shoot a bullet if countdown is over
        if (shootCountdown >= AI_PLAYER_SHOOTING_INTERVAL) {
            BulletData bullet = new BulletData();
            bullet.x = x;
            bullet.y = y;
            bullet.speedBullet = PISTOL_BULLET_SPEED * (closestPlayer.getState().x < x ? -1 : 1);
            bullet.id = -1;
            bullets.add(bullet);
            shootCountdown = 0;
        }

        // update bullets
        bullets.forEach(bullet -> bullet.x += bullet.speedBullet);

        // removing bullets
        bullets.removeIf(bullet -> bullet.x < x - BOUNDS || bullet.x > x + BOUNDS);

        if (Math.abs(knockback) < 1) {
            // if knockback is small enough, set it to 0
            knockback = 0;
        } else {
            // apply knockback
            x += knockback * delta;
            // reduce knockback
            knockback *= 0.9;
        }

        shootCountdown += delta;
    }

    public void bulletHit(BulletData bullet) {
        knockback = (bullet.speedBullet < 0 ? -1 : 1) * PISTOL_BULLET_FORCE;
    }
}
