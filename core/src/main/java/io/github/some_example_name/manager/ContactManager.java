package io.github.some_example_name.manager;

import com.badlogic.gdx.physics.box2d.*;
import io.github.some_example_name.GameSettings;
import io.github.some_example_name.objects.GameObject;

public class ContactManager {

    World world;

    public ContactManager(World world) {
        this.world = world;

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();

                int cDef = fixA.getFilterData().categoryBits;
                int cDef2 = fixB.getFilterData().categoryBits;

                if (cDef == GameSettings.TRASH_BIT && cDef2 == GameSettings.BULLET_BIT
                    || cDef2 == GameSettings.TRASH_BIT && cDef == GameSettings.BULLET_BIT
                    || cDef == GameSettings.TRASH_BIT && cDef2 == GameSettings.SHIP_BIT
                    || cDef2 == GameSettings.TRASH_BIT && cDef == GameSettings.SHIP_BIT) {

                    ((GameObject) fixA.getUserData()).hit();
                    ((GameObject) fixB.getUserData()).hit();

                }
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });

    }

}
