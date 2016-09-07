package game;

import flounder.engine.*;
import flounder.engine.implementation.*;
import flounder.exceptions.*;
import flounder.helpers.*;
import flounder.inputs.*;
import flounder.lights.*;
import flounder.maths.*;
import flounder.maths.vectors.*;
import flounder.parsing.*;
import flounder.particles.*;
import flounder.particles.loading.*;
import flounder.particles.spawns.*;
import flounder.resources.*;
import game.cameras.*;
import game.celestial.*;
import game.entities.loading.*;
import game.options.*;
import game.players.*;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class MainGame extends IGame {
	public static final Config CONFIG = new Config(new MyFile("configs", "settings.conf"));
	public static final Config POST_CONFIG = new Config(new MyFile("configs", "post.conf"));
	public static final Config CONTROLS_CONFIG = new Config(new MyFile("configs", "controls_joystick.conf"));

	private KeyButton screenshot;
	private KeyButton fullscreen;
	private KeyButton polygons;
	private CompoundButton toggleMusic;
	private CompoundButton skipMusic;

	private IPlayer player;

	private boolean stillLoading;

	@Override
	public void init() {
		this.screenshot = new KeyButton(GLFW_KEY_F2);
		this.fullscreen = new KeyButton(GLFW_KEY_F11);
		this.polygons = new KeyButton(GLFW_KEY_P);
		this.toggleMusic = new CompoundButton(new KeyButton(GLFW_KEY_DOWN), new JoystickButton(OptionsControls.JOYSTICK_PORT, OptionsControls.JOYSTICK_MUSIC_PAUSE));
		this.skipMusic = new CompoundButton(new KeyButton(GLFW_KEY_LEFT, GLFW_KEY_RIGHT), new JoystickButton(OptionsControls.JOYSTICK_PORT, OptionsControls.JOYSTICK_MUSIC_SKIP));
		this.stillLoading = true;

		System.out.println("");

		for (int i = 0; i < 1; i++) {
			Star star = StarGenerator.generateStar(new Vector3f());
		}
	}

	public void generateWorlds() {
		Environment.init(new Fog(new Colour(1.0f, 1.0f, 1.0f), 0.003f, 2.0f, 0.0f, 50.0f), new Light(new Colour(0.85f, 0.85f, 0.85f), new Vector3f(0.0f, 2000.0f, 2000.0f)));

		// EntityLoader.load("dragon").createEntity(Environment.getEntitys(), new Vector3f(30, 0, 0), new Vector3f());
		EntityLoader.load("pane").createEntity(Environment.getEntities(), new Vector3f(), new Vector3f());
		EntityLoader.load("sphere").createEntity(Environment.getEntities(), Environment.getLights().get(0).position, new Vector3f());

		for (int n = 0; n < 32; n++) {
			for (int p = 0; p < 32; p++) {
				for (int q = 0; q < 32; q++) {
					if (Maths.RANDOM.nextInt(10) == 1) {
						EntityLoader.load("crate").createEntity(Environment.getEntities(), new Vector3f((n * 5) + 10, (p * 5) + 10, (q * 5) + 10), new Vector3f(0, Maths.RANDOM.nextInt(360), 0));
					}
				}
			}
		}

		ParticleSystem particleSystem = new ParticleSystem(new ArrayList<>(), null, 1750.0f, 1.9f, -0.03f);
		particleSystem.addParticleType(ParticleLoader.load("cosmic"));
		particleSystem.addParticleType(ParticleLoader.load("cosmicHot"));
		particleSystem.randomizeRotation();
		particleSystem.setSpawn(new SpawnCircle(20, new Vector3f(0.0f, 1.0f, 0.0f)));
		particleSystem.setSystemCentre(new Vector3f(0, 20, -10));
	}

	public void generatePlayer() {
		if (FlounderEngine.getCamera() instanceof CameraFocus) {
			this.player = new PlayerFocus();
		} else if (FlounderEngine.getCamera() instanceof CameraFPS) {
			this.player = new PlayerFPS();
		} else {
			throw new FlounderRuntimeException("Could not find IPlayer implementation for ICamera!");
		}

		this.player.init();
	}

	public void destroyWorld() {
		player = null;
		FlounderEngine.getParticles().clearAllParticles();
		Environment.destroy();
		System.gc();
	}

	@Override
	public void update() {
		if (screenshot.wasDown()) {
			FlounderEngine.getDevices().getDisplay().screenshot();
		}

		if (fullscreen.wasDown()) {
			FlounderEngine.getDevices().getDisplay().setFullscreen(!FlounderEngine.getDevices().getDisplay().isFullscreen());
		}

		if (polygons.wasDown()) {
			OpenGlUtils.goWireframe(!OpenGlUtils.isInWireframe());
		}

		if (toggleMusic.wasDown()) {
			if (FlounderEngine.getDevices().getSound().getMusicPlayer().isPaused()) {
				FlounderEngine.getDevices().getSound().getMusicPlayer().unpauseTrack();
			} else {
				FlounderEngine.getDevices().getSound().getMusicPlayer().pauseTrack();
			}
		}

		if (skipMusic.wasDown()) {
			MainSeed.randomize();
			FlounderEngine.getDevices().getSound().getMusicPlayer().skipTrack();
		}

		if (FlounderEngine.getManagerGUI().isMenuIsOpen()) {
			// Pause the music for the start screen.
			FlounderEngine.getDevices().getSound().getMusicPlayer().pauseTrack();
		} else if (!FlounderEngine.getManagerGUI().isMenuIsOpen() && stillLoading) {
			// Unpause the music for the main menu.
			stillLoading = false;
			//	FlounderEngine.getLogger().log("Starting main menu music.");
			//	FlounderEngine.getDevices().getSound().getMusicPlayer().unpauseTrack();
		}

		if (player != null) {
			player.update(FlounderEngine.getManagerGUI().isMenuIsOpen());
			update(player.getPosition(), player.getRotation());
		}

		Environment.update();
	}

	@Override
	public void dispose() {
		CONTROLS_CONFIG.dispose();
		POST_CONFIG.dispose();
		CONFIG.dispose();
	}
}

