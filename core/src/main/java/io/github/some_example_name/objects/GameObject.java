package io.github.some_example_name.objects;

import static io.github.some_example_name.GameSettings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class GameObject {

    public short cBits;

    public int width;
    public int height;

    public Body body;
    protected Texture texture;

    GameObject(String texturePath, int x, int y, int width, int height, short cBits, World world) {
        this.width = width;
        this.height = height;
        this.cBits = cBits;

        texture = new Texture(texturePath);
        body = createBody(x, y, world);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture,
            getX() - (width / 2f),
            getY() - (height / 2f),
            width,
            height
        );
    }

    public void hit() {
    }

    public int getX() {
        return (int) (body.getPosition().x / SCALE);
    }

    public int getY() {
        return (int) (body.getPosition().y / SCALE);
    }

    public void setX(int x) {
        body.setTransform(x * SCALE, body.getPosition().y, 0);
    }

    public void setY(int y) {
        body.setTransform(body.getPosition().x, y * SCALE, 0);
    }

    protected boolean isSensor() {
        return false;
    }

    private Body createBody(float x, float y, World world) {

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.fixedRotation = true;

        Body body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(Math.max(width, height) * SCALE / 2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.filter.categoryBits = cBits;

        fixtureDef.isSensor = isSensor();

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        shape.dispose();

        body.setTransform(x * SCALE, y * SCALE, 0);
        return body;
    }
}
