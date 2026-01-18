package io.github.some_example_name.screens;

import static com.badlogic.gdx.math.MathUtils.random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.*;
import io.github.some_example_name.components.*;
import io.github.some_example_name.manager.ContactManager;
import io.github.some_example_name.manager.MemoryManager;
import io.github.some_example_name.objects.BulletObject;
import io.github.some_example_name.objects.ShipObject;
import io.github.some_example_name.objects.TrashObject;

import java.util.ArrayList;

public class GameScreen extends ScreenAdapter {

    Main main;
    GameSession gameSession;
    ShipObject shipObject;

    ArrayList<TrashObject> trashArray;
    ArrayList<BulletObject> bulletArray;

    float shakeTime = 0f;
    float slowMoTimer = 0f;
    float timeScale = 1f;
    float shakeDuration = 0.25f;
    float shakePower = 8f;
    float camBaseX;
    float camBaseY;
    int lastLives;
    private int lastSpeedupScore = 0;

    boolean doubleShotReady = false;

    ContactManager contactManager;

    MovingBackgroundView backgroundView;
    ImageView topBlackoutView;
    LiveView liveView;
    TextView scoreTextView;

    ButtonView pauseButton;
    Texture doubleShotIcon;
    ImageView fullBlackoutView;
    TextView pauseTextView;
    ButtonView homeButton;
    ButtonView continueButton;

    TextView recordsTextView;
    RecordsListView recordsListView;
    ButtonView homeButton2;

    public GameScreen(Main main) {
        this.main = main;
        gameSession = new GameSession();

        contactManager = new ContactManager(main.world);

        trashArray = new ArrayList<>();
        bulletArray = new ArrayList<>();

        shipObject = new ShipObject(
            GameSettings.SCREEN_WIDTH / 2, 150,
            GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
            GameResources.SHIP_IMG_PATH,
            main.world
        );
        doubleShotIcon = new Texture(GameResources.DOUBLE_SHOT_ICON_PATH);

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        topBlackoutView = new ImageView(0, 1180, GameResources.BLACKOUT_TOP_IMG_PATH);
        liveView = new LiveView(305, 1215);
        scoreTextView = new TextView(main.commonWhiteFont, 50, 1215);
        pauseButton = new ButtonView(
            605, 1200,
            46, 54,
            GameResources.PAUSE_IMG_PATH
        );

        fullBlackoutView = new ImageView(0, 0, GameResources.BLACKOUT_FULL_IMG_PATH);
        pauseTextView = new TextView(main.largeWhiteFont, 282, 842, "Pause");
        homeButton = new ButtonView(
            138, 695,
            200, 70,
            main.commonBlackFont,
            GameResources.BUTTON_SHORT_BG_IMG_PATH,
            "Home"
        );
        continueButton = new ButtonView(
            393, 695,
            200, 70,
            main.commonBlackFont,
            GameResources.BUTTON_SHORT_BG_IMG_PATH,
            "Continue"
        );

        recordsListView = new RecordsListView(main.commonWhiteFont, 690);
        recordsTextView = new TextView(main.largeWhiteFont, 206, 842, "Last records");
        homeButton2 = new ButtonView(
            280, 365,
            160, 70,
            main.commonBlackFont,
            GameResources.BUTTON_SHORT_BG_IMG_PATH,
            "Home"
        );

    }

    @Override
    public void show() {
        restartGame();
        doubleShotReady = false;
        camBaseX = main.camera.position.x;
        camBaseY = main.camera.position.y;
        lastLives = shipObject.getLiveLeft();
        shakeTime = 0f;
    }

    private void startShake()
    {
        shakeTime = shakeDuration;
    }
    private void startSlowMo()
    {
        slowMoTimer = 1.0f;
    }

    @Override
    public void render(float delta) {
        if (slowMoTimer > 0) {
            slowMoTimer -= delta;
            timeScale = 0.4f;
        } else {
            timeScale = 1f;
        }
        handleInput();

        if (gameSession.state == GameState.PLAYING) {
            if (gameSession.shouldSpawnTrash()) {
                TrashObject trashObject = new TrashObject(
                    GameSettings.TRASH_WIDTH, GameSettings.TRASH_HEIGHT,
                    GameResources.TRASH_IMG_PATH,
                    main.world
                );
                trashArray.add(trashObject);
            }

            if (shipObject.needToShoot()) {

                int x = shipObject.getX();
                int y = shipObject.getY() + shipObject.height / 2;

                BulletObject bullet1 = new BulletObject(
                    x, y,
                    GameSettings.BULLET_WIDTH, GameSettings.BULLET_HEIGHT,
                    GameResources.BULLET_IMG_PATH,
                    main.world
                );
                bulletArray.add(bullet1);

                if (doubleShotReady) {

                    BulletObject bullet2 = new BulletObject(
                        x + 40, y,
                        GameSettings.BULLET_WIDTH, GameSettings.BULLET_HEIGHT,
                        GameResources.BULLET_IMG_PATH,
                        main.world
                    );
                    bulletArray.add(bullet2);

                    doubleShotReady = false;
                }

                if (main.audioManager.isSoundOn) main.audioManager.shootSound.play();
            }

            if (!shipObject.isAlive()) {
                gameSession.endGame();
                recordsListView.setRecords(MemoryManager.loadRecordsTable());
            }

            updateTrash();
            for (TrashObject trash : trashArray)
            {
                trash.body.setLinearVelocity(new Vector2(0, -GameSettings.TRASH_VELOCITY * timeScale));
            }
            updateBullets();
            for (BulletObject bullet : bulletArray)
            {
                bullet.body.setLinearVelocity(new Vector2(0, GameSettings.BULLET_VELOCITY * timeScale));
            }
            int score = gameSession.getScore();

            if (score >= lastSpeedupScore + 10) {
                GameSettings.TRASH_VELO = Math.min(
                    GameSettings.TRASH_VELO + GameSettings.TRASH_ACCELERATION,
                    GameSettings.TRASH_VELO_MAX
                );


                lastSpeedupScore = score;
                System.out.println("TRASH SPEED = " + GameSettings.TRASH_VELOCITY);
            }

            int curentLives = shipObject.getLiveLeft();
            if (curentLives < lastLives)
            {
                startShake();
                startSlowMo();
            }
            lastLives = curentLives;
            backgroundView.move();
            gameSession.updateScore();

            scoreTextView.setText("Score: " + gameSession.getScore());
            liveView.setLeftLives(shipObject.getLiveLeft());

            main.stepWorld();
        }

        draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            main.touch = main.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            switch (gameSession.state) {
                case PLAYING:
                    if (pauseButton.isHit(main.touch.x, main.touch.y)) {
                        gameSession.pauseGame();
                    }
                    shipObject.move(main.touch);
                    break;

                case PAUSED:
                    if (continueButton.isHit(main.touch.x, main.touch.y)) {
                        gameSession.resumeGame();
                    }
                    if (homeButton.isHit(main.touch.x, main.touch.y)) {
                        main.setScreen(main.menuScreen);
                    }
                    break;

                case ENDED:

                    if (homeButton2.isHit(main.touch.x, main.touch.y)) {
                        main.setScreen(main.menuScreen);
                    }
                    break;
            }

        }
    }

    private void draw() {
        if (shakeTime > 0) {
            shakeTime -= Gdx.graphics.getDeltaTime();

            float offsetX = (float)(Math.random() * 2 - 1) * shakePower;
            float offsetY = (float)(Math.random() * 2 - 1) * shakePower;

            main.camera.position.set(camBaseX + offsetX, camBaseY + offsetY, 0);
        } else {
            main.camera.position.set(camBaseX, camBaseY, 0);
        }

        main.camera.update();
        main.batch.setProjectionMatrix(main.camera.combined);
        ScreenUtils.clear(Color.CLEAR);

        main.batch.begin();
        backgroundView.draw(main.batch);
        for (TrashObject trash : trashArray) trash.draw(main.batch);
        shipObject.draw(main.batch);
        for (BulletObject bullet : bulletArray) bullet.draw(main.batch);
        topBlackoutView.draw(main.batch);
        scoreTextView.draw(main.batch);
        liveView.draw(main.batch);
        pauseButton.draw(main.batch);



        if (gameSession.state == GameState.PAUSED) {
            fullBlackoutView.draw(main.batch);
            pauseTextView.draw(main.batch);
            homeButton.draw(main.batch);
            continueButton.draw(main.batch);
        } else if (gameSession.state == GameState.ENDED) {
            fullBlackoutView.draw(main.batch);
            recordsTextView.draw(main.batch);
            recordsListView.draw(main.batch);
            homeButton2.draw(main.batch);

            if (doubleShotReady) {
                int size = 50;
                main.batch.draw(doubleShotIcon,
                    GameSettings.SCREEN_WIDTH - size - 20,
                    20,
                    size,
                    size
                );
            }
        }

        main.batch.end();

    }

    private void updateTrash() {
        for (int i = 0; i < trashArray.size(); i++) {

            boolean hasToBeDestroyed = !trashArray.get(i).isAlive() || !trashArray.get(i).isInFrame();

            if (!trashArray.get(i).isAlive()) {

                gameSession.destructionRegistration();
                if (main.audioManager.isSoundOn) main.audioManager.explosionSound.play(0.2f);

                if (!doubleShotReady && random.nextInt(100) < 15) {
                    doubleShotReady = true;
                    System.out.println("DOUBLE SHOT READY!");
                }
            }

            if (hasToBeDestroyed) {
                main.world.destroyBody(trashArray.get(i).body);
                trashArray.remove(i--);
            }
        }
    }

    private void updateBullets() {
        for (int i = 0; i < bulletArray.size(); i++) {
            if (bulletArray.get(i).hasToBeDestroyed()) {
                main.world.destroyBody(bulletArray.get(i).body);
                bulletArray.remove(i--);
            }
        }
    }

    private void restartGame() {

        for (int i = 0; i < trashArray.size(); i++) {
            main.world.destroyBody(trashArray.get(i).body);
            trashArray.remove(i--);
        }

        if (shipObject != null) {
            main.world.destroyBody(shipObject.body);
        }

        shipObject = new ShipObject(
            GameSettings.SCREEN_WIDTH / 2, 150,
            GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
            GameResources.SHIP_IMG_PATH,
            main.world
        );
        for (TrashObject trash : trashArray)
        {
            trash.body.setLinearVelocity(new Vector2(0, -GameSettings.TRASH_VELOCITY));
        }

        bulletArray.clear();
        gameSession.startGame();
    }

}
