package helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Textures {

    public final static Texture BULLET_TEXTURE = new Texture("bullet2-transformed.png");
    public final static Texture PLAYER_TEXTURE = new Texture("badlogic.jpg");
    public final static Texture TRUMP_TEXTURE = new Texture("trump.jpg");
    public final static Texture BIDEN_TEXTURE = new Texture("biden.jpg");
    public final static Texture OBAMA_TEXTURE = new Texture("obama.jpg");
    public final static Texture ALIEN_TEXTURE = new Texture("alien.png");
    // Heart png (8bit) is from this website: https://www.pngwing.com/en/free-png-nymsb
    public final static Texture HEALTH_TEXTURE = new Texture(Gdx.files.internal("heart.png"));
    public final static Texture BLACK_HEART_TEXTURE = new Texture(Gdx.files.internal("black_heart.png"));

}
