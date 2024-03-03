package objects.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import ee.taltech.americandream.AmericanDream;
import helper.Direction;
import helper.packet.PlayerPositionMessage;
import objects.bullet.Bullet;

import java.util.List;
import java.util.Objects;

import static helper.Constants.*;

public class Player extends GameEntity {
    private final float speed;
    private Direction direction;
    private int jumpCounter;
    private float keyDownTime = 0;
    private float timeTillRespawn = 0;
    private Integer livesCount = LIVES_COUNT;

    public Player(float width, float height, Body body) {
        super(width, height, body);
        this.speed = PLAYER_SPEED;
        this.jumpCounter = 0;
        this.direction = Direction.RIGHT;
        body.setTransform(new Vector2(body.getPosition().x, body.getPosition().y + 30), 0);
    }

    @Override
    public void update(float delta, Vector2 center) {
        x = body.getPosition().x * PPM;
        y = body.getPosition().y * PPM;
        handleInput(delta);
        handlePlatform();
        handleOutOfBounds(delta, center);
        direction = velX > 0 ? Direction.RIGHT : Direction.LEFT;

        // construct player position message to be sent to the server
        PlayerPositionMessage positionMessage = new PlayerPositionMessage();
        positionMessage.x = x;
        positionMessage.y = y;
        positionMessage.direction = Direction.LEFT;
        positionMessage.livesCount = livesCount;
        // send player position message to the server
        AmericanDream.client.sendUDP(positionMessage);
    }

    private void handleInput(float delta) {
        Controller controller = Controllers.getCurrent();
        velX = 0;
        // Moving right
        if (Gdx.input.isKeyPressed(Input.Keys.D) || (controller != null &&
                (controller.getButton(controller.getMapping().buttonDpadRight) ||
                        Objects.requireNonNull(controller).getAxis(controller.getMapping().axisLeftX) > 0.5f
                ))) {
            velX = 1;
        }
        // Moving left
        if (Gdx.input.isKeyPressed(Input.Keys.A) || (controller != null &&
                (controller.getButton(controller.getMapping().buttonDpadLeft) ||
                        Objects.requireNonNull(controller).getAxis(controller.getMapping().axisLeftX) < -0.5f))) {
            velX = -1;
        }

        // Jumping
        if (jumpCounter < JUMP_COUNT && Gdx.input.isKeyJustPressed(Input.Keys.W) || (controller != null &&
                controller.getButton(controller.getMapping().buttonA))) {
            float force = body.getMass() * JUMP_FORCE;
            body.setLinearVelocity(body.getLinearVelocity().x, 0);
            body.applyLinearImpulse(new Vector2(0, force), body.getWorldCenter(), true);
            jumpCounter++;
        }

        // key down on platform
        if (Gdx.input.isKeyPressed(Input.Keys.S) || (controller != null &&
                (controller.getButton(controller.getMapping().buttonDpadDown) ||
                        Objects.requireNonNull(controller).getAxis(controller.getMapping().axisLeftY) > 0.5f))) {
            keyDownTime += delta;
        } else {
            keyDownTime = 0;
        }

        // reset jump counter if landed (sometimes stopping in midair works as well)
        if (body.getLinearVelocity().y == 0) {
            jumpCounter = 0;
        }


        body.setLinearVelocity(velX * speed, body.getLinearVelocity().y);


    }

    public void handleBulletInput(List<Bullet> bullets) {
        // shooting code

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) ||
                (Controllers.getCurrent() != null &&
                        Controllers.getCurrent().getAxis(Controllers.getCurrent().getMapping().axisRightX) > 0.5f)) {
            bullets.add(new Bullet(this.getPosition().x - 20, this.getPosition().y, BULLET_SPEED));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) ||
                (Controllers.getCurrent() != null &&
                        Controllers.getCurrent().getAxis(Controllers.getCurrent().getMapping().axisRightX) < -0.5f)) {
            bullets.add(new Bullet(this.getPosition().x - 20, this.getPosition().y, BULLET_SPEED));
        }

        Gdx.app.log("Bullets", "Bullets: " + bullets.toString());
    }

    /*
     * Handle the platform.
     * If player is below the platform, move it some random distance to the right
     * If player is above the platform, move it back to the original position
     * TODO: Make the logic less hacky
     */
    private void handlePlatform() {
        Array<Body> bodies = new Array<Body>();
        body.getWorld().getBodies(bodies);

        for (Body b : bodies) {
            if (b.getUserData() != null && b.getUserData().toString().contains("platform")) {
                float height = Float.parseFloat(b.getUserData().toString().split(":")[1]);
                height = height / PPM;
                if (body.getPosition().y - this.height / PPM >= height && b.getPosition().x >= 2000 && (keyDownTime == 0 || keyDownTime > PLATFORM_DESCENT * 1.1)) {
                    // bring back platform
                    b.setTransform(b.getPosition().x - 2000, b.getPosition().y, 0);
                } else if ((body.getPosition().y - this.height / PPM < height || (keyDownTime >= PLATFORM_DESCENT && keyDownTime <= PLATFORM_DESCENT * 1.1)) && b.getPosition().x <= 2000) {
                    // remove platform
                    b.setTransform(b.getPosition().x + 2000, b.getPosition().y, 0);
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // here we can draw the player sprite eventually
    }

    public Vector2 getPosition() {
        return body.getPosition().scl(PPM);
    }

    /*
     * Get the dimensions of the player
     * (width, height)
     */
    public Vector2 getDimensions() {
        return new Vector2(width, height);
    }

    /*
     * Checks if player is out of bounds and handles it (respawn)
     */
    private void handleOutOfBounds(float delta, Vector2 center) {
        if (y < -BOUNDS) {
            if (timeTillRespawn <= RESPAWN_TIME) {
                // wait for respawn time
                timeTillRespawn += delta;
            } else {
                // respawn player
                body.setTransform(center.x / PPM, center.y / PPM + 30, 0);
                body.setLinearVelocity(0, 0);
                // set time till respawn to 0
                timeTillRespawn = 0;
                // decrement lives
                livesCount--;
            }
        }
    }


    public Direction getDirection() {
        return direction;
    }

    public Integer getLives() {
        return this.livesCount;
    }
}
