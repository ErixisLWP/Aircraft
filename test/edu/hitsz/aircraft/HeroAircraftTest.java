package edu.hitsz.aircraft;

import edu.hitsz.prop.BloodProp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeroAircraftTest {

    private HeroAircraft heroAircraft;

    @BeforeEach
    void setUp() {
        System.out.println("**--- Executed before each test method in this class ---**");
        heroAircraft = HeroAircraft.getInstance();
    }

    @AfterEach
    void tearDown() {
        System.out.println("**--- Executed after each test method in this class ---**");
        heroAircraft = null;
    }

    @Test
    void decreaseHp() {
        System.out.println("**--- Test decreaseHp method executed ---**");
        heroAircraft.decreaseHp(15);
        assertEquals(85, heroAircraft.getHp());
        heroAircraft.decreaseHp(100);
        assertEquals(0, heroAircraft.getHp());
    }

    @Test
    void increaseHp() {
        System.out.println("**--- Test increaseHp method executed ---**");
        heroAircraft.decreaseHp(50);
        heroAircraft.increaseHp(30);
        assertEquals(80, heroAircraft.getHp());
        heroAircraft.increaseHp(50);
        assertEquals(100, heroAircraft.getHp());
    }

    @Test
    void crash() {
        System.out.println("**--- Test crash method executed ---**");
        heroAircraft.setLocation(200,200);
        BloodProp bloodProp = new BloodProp(200,200,0,0);
        assertTrue(heroAircraft.crash(bloodProp));
        bloodProp.setLocation(400,400);
        assertFalse(heroAircraft.crash(bloodProp));
    }
}