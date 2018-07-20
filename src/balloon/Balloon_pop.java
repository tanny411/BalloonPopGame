package balloon;

import static balloon.Assets.font20;
import static balloon.Assets.font30;
import static balloon.Assets.font40;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Balloon_pop {

    public static void main(String[] args) {
        Game game = new Game("Balloon-Pop", 350, 600);
        game.start();
    }

}

class Game implements Runnable {

    String name;
    int width, height;
    JFrame frame;
    Canvas canvas;
    MouseManager mouseManager;
    KeyManager keyManager;
    BufferStrategy bs;
    Graphics g;
    boolean running = false;
    Thread thread;
    State state;
    MenuState menuState;
    GameState gameState;
    Assets assets;

    Game(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
        keyManager = new KeyManager();
        mouseManager = new MouseManager(this);
    }

    void init() {
        frame = new JFrame(name);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.setMaximumSize(new Dimension(width, height));
        canvas.setMinimumSize(new Dimension(width, height));
        canvas.setFocusable(false);

        frame.add(canvas);
        frame.pack();

        frame.addKeyListener(keyManager);
        frame.addMouseListener(mouseManager);
        canvas.addMouseMotionListener(mouseManager);
        canvas.addMouseListener(mouseManager);

        assets = new Assets(this);
        menuState = new MenuState(this);
        gameState=null;
        state = menuState;
    }

    void newGame()
    {
        gameState=new GameState(this);
        state=gameState;
    }
    @Override
    public void run() {
        init();

        int fps = 60;
        double timePerTick = 1000000000 / fps;
        double delta = 0;
        long now;
        long lastTime = System.nanoTime();
        long timer = 0;
        int ticks = 0;

        while (running) {
            now = System.nanoTime();
            delta += (now - lastTime) / timePerTick;
            lastTime = now;

            if (delta >= 1) {
                
                tick();
                render();
                delta--;
            }
        }
    }

    void tick() {
        if (state != null) {
            state.tick();
        }
    }

    void render() {
        bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(3);
            return;
        }
        g = bs.getDrawGraphics();
        //Clear Screen
        g.clearRect(0, 0, width, height);
        //Draw Here!
        if (state != null) {
            state.render(g);
        }
        //End Drawing!
        bs.show();
        g.dispose();
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MouseManager implements MouseListener, MouseMotionListener {

    boolean leftButton, rightButton;
    Game game;

    MouseManager(Game game) {
        this.game = game;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftButton = true;
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            rightButton = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftButton = false;
        } else if (e.getButton() == MouseEvent.BUTTON2) {
            rightButton = false;
        }
        game.state.onMouseRelease(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        game.state.onMouseMove(e);
    }

}

abstract class UIButton {

    BufferedImage image;
    int x, y, width, height;
    Rectangle bounds;
    boolean hovering;

    UIButton(int x, int y, int width, int height, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        bounds = new Rectangle(x, y, width, height);
    }

    void render(Graphics g) {
        if (hovering) {
            g.drawImage(image, x, y, width + (int) (.1 * width), height + (int) (.1 * height), null);
        } else {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    abstract void onClick();

    void onMouseRelease(MouseEvent e) {
        if (hovering) {
            onClick();
        }
    }

    void onMouseMove(MouseEvent e) {
        hovering = bounds.contains(e.getX(), e.getY());
    }

}

class KeyManager implements KeyListener {

    public boolean[] keys;

    public KeyManager() {
        keys = new boolean[256];
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() < 0 || e.getKeyCode() >= keys.length) {
            return;
        }
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() < 0 || e.getKeyCode() >= keys.length) {
            return;
        }
        keys[e.getKeyCode()] = false;
    }
}

class State {

    ArrayList<UIButton> buttons;
    Game game;

    State(Game game) {
        this.game = game;
        buttons = new ArrayList<>();
    }

    void tick() {
    }

    void render(Graphics g) {

    }

    void onMouseMove(MouseEvent e) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).onMouseMove(e);
        }
    }

    void onMouseRelease(MouseEvent e) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).onMouseRelease(e);
        }
    }
}

class MenuState extends State {

    MenuState(Game game) {
        super(game);
        UIButton play;
        play = new UIButton(game.width / 2 - Assets.menubtn.getWidth() / 4, game.height / 2, Assets.menubtn.getWidth() / 2, Assets.menubtn.getHeight() / 2, Assets.menubtn) {
            @Override
            void onClick() {
                game.newGame();
            }
        };
        buttons.add(play);
    }

    @Override
    void render(Graphics g) {

        g.drawImage(Assets.menuBack, 0, 0, null);

        Text.drawString(g, "Balloon-Pop", 90, 200, false, Color.DARK_GRAY, Assets.font30);

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).render(g);
        }
    }
}

class GameState extends State {

    boolean lose = false;
    int score = 0;
    int fail = 0;
    Random rand;
    long currentTime, lastTime, timer = 0, timer2 = 0;
    ArrayList<Balloon> balloons;
    int lim = 1;

    GameState(Game game) {
        super(game);
        rand = new Random();
        balloons = new ArrayList<>();
        lastTime = System.currentTimeMillis();
    }

    @Override
    void tick() {
        //System.out.println(score);
        currentTime = System.currentTimeMillis();
        timer2 += currentTime - lastTime;
        timer += currentTime - lastTime;
        lastTime = currentTime;
        if (timer > 10000) {
            timer = 0;
            lim++;
        }
        if (timer2 > 1000) {
            balloons.add(new Balloon(game, abs(rand.nextInt()) % lim + 1));
            timer2 = 0;
        }
        //System.out.println(balloons.size());
        for (int i = 0; i < balloons.size(); i++) {
            balloons.get(i).tick();
            if (balloons.get(i).popDead) {
                score++;
                balloons.remove(i);
            } else if (balloons.get(i).goDead) {
                fail++;
                balloons.remove(i);
            }
        }
        if (lose == false && fail > 10) {
            lose = true;
            UIButton replay;
            replay = new UIButton(game.width / 2 - Assets.rebtn.getWidth() / 4, game.height -game.height/ 3, Assets.rebtn.getWidth() / 2, Assets.rebtn.getHeight() / 2, Assets.rebtn) {
                @Override
                void onClick() {
                    game.newGame();
                }
            };
            buttons.add(replay);
        }
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(Assets.gameBack, 0, 0, null);
        
        for (int i = 0; i < balloons.size(); i++) {
            balloons.get(i).render(g);
        }
        
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).render(g);
        }
        
        if (lose) {
            Text.drawString(g, "Score : " + score, game.width / 2, game.height / 2 - 100, true, Color.BLACK, font40);
            Text.drawString(g, "YOU LOST!", game.width / 2, game.height / 2, true, Color.RED, font40);
            Text.drawString(g, "Can't you Type? Slow Hands -_-", game.width / 2, game.height / 2 + 40, true, Color.BLACK, font20);
            return;
        }

        Text.drawString(g, "Fail: " + Integer.toString(fail), game.width / 2, 50, true, Color.BLACK, font30);
        Text.drawString(g, "Score: " + Integer.toString(score), game.width / 2, 100, true, Color.BLACK, font30);
    }
}

class ImageLoader {

    public static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(ImageLoader.class.getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

}

class Assets {

    public static Font font30, font40, font20;
    public static BufferedImage menuBack, gameBack, menubtn,rebtn;
    public static BufferedImage[] balloon, animPop;
    Game game;

    Assets(Game game) {
        this.game = game;
        init();
    }

    private void init() {

        try {
            font40 = Font.createFont(Font.TRUETYPE_FONT, new File("res/NotoSans-Bold.ttf")).deriveFont(Font.PLAIN, 40);
            font20 = Font.createFont(Font.TRUETYPE_FONT, new File("res/NotoSans-Bold.ttf")).deriveFont(Font.PLAIN, 20);
            font30 = Font.createFont(Font.TRUETYPE_FONT, new File("res/NotoSans-Bold.ttf")).deriveFont(Font.PLAIN, 30);
        } catch (FontFormatException | IOException ex) {
            ex.printStackTrace();
        }
        menuBack = ImageLoader.loadImage("/bg.png");
        menuBack = menuBack.getSubimage(menuBack.getWidth() / 2 - game.width, menuBack.getHeight() - game.height, game.width, game.height);
        gameBack = ImageLoader.loadImage("/images/game_bg.png");
        gameBack = gameBack.getSubimage(gameBack.getWidth() / 2 - game.width, gameBack.getHeight() - game.height, game.width, game.height);
        rebtn = ImageLoader.loadImage("/images/btn_retry.png");
        menubtn = ImageLoader.loadImage("/images/play.png");
        balloon = new BufferedImage[5];
        balloon[0] = ImageLoader.loadImage("/images/balloon1.png");
        balloon[1] = ImageLoader.loadImage("/images/balloon2.png");
        balloon[2] = ImageLoader.loadImage("/images/balloon3.png");
        balloon[3] = ImageLoader.loadImage("/images/balloon4.png");
        balloon[4] = ImageLoader.loadImage("/images/balloon5.png");
        BufferedImage popSprite = ImageLoader.loadImage("/images/explosion.png");
        animPop = new BufferedImage[4];
        animPop[0] = popSprite.getSubimage(2 * 302, 2 * 302, 302, 302);
        animPop[1] = popSprite.getSubimage(1 * 302, 3 * 302, 302, 302);
        animPop[2] = popSprite.getSubimage(2 * 302, 3 * 302, 302, 302);
        animPop[3] = popSprite.getSubimage(0 * 302, 3 * 302, 302, 302);
    }
}

class Balloon {

    Animation animPop;
    Game game;
    char letter;
    int x, y, speed, lim, width = Assets.balloon[0].getWidth() / 5, height = Assets.balloon[0].getHeight() / 5, in;
    int pwidth = Assets.animPop[0].getWidth() / 5, pheight = Assets.animPop[0].getHeight() / 5;
    boolean popDead = false, goDead = false, popped = false;
    Random rand;

    Balloon(Game game, int speed) {
        this.game = game;
        this.speed = speed;

        animPop = new Animation(50, Assets.animPop);

        rand = new Random();
        in = abs(rand.nextInt()) % 5;
        letter = (char) (abs(rand.nextInt()) % 26 + 65);
        //System.out.println(letter);
        x = abs(rand.nextInt()) % (game.width - width);
        y = game.height;
    }

    void tick() {
        if (game.gameState.lose == false && game.keyManager.keys[(int) letter]) {
            popped = true;
        }
        if (goDead || popDead) {
            return;
        }

        if (popped) {
            animPop.tick();
        }

        if (animPop.over) {
            popDead = true;
        }

        y -= speed;
        if (y <= -height) {
            goDead = true;
        }
    }

    void render(Graphics g) {

        if (popDead || goDead) {
            return;
        }
        if (popped) {
            g.drawImage(animPop.getCurrentFrame(), x, y, pwidth, pheight, null);
        } else {
            g.drawImage(Assets.balloon[in], x, y, width, height, null);

            Text.drawString(g, Character.toString(letter), x + 16, y + 40, false, Color.DARK_GRAY, font30);
        }
    }
}

class Animation {

    private int speed, index;
    private long lastTime, timer;
    private BufferedImage[] frames;
    public boolean over = false;

    public Animation(int speed, BufferedImage[] frames) {
        this.speed = speed;
        this.frames = frames;
        index = 0;
        timer = 0;
        lastTime = System.currentTimeMillis();
    }

    public void tick() {
        timer += System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        if (timer > speed) {
            index++;
            timer = 0;
            if (index >= frames.length) {
                over = true;
            }
        }
    }

    public BufferedImage getCurrentFrame() {
        if (over) {
            return null;
        }
        return frames[index];
    }

}

class Text {

    public static void drawString(Graphics g, String text, int xPos, int yPos, boolean center, Color c, Font font) {
        g.setColor(c);
        g.setFont(font);
        int x = xPos;
        int y = yPos;
        if (center) {
            FontMetrics fm = g.getFontMetrics(font);
            x = xPos - fm.stringWidth(text) / 2;
            y = (yPos - fm.getHeight() / 2) + fm.getAscent();
        }
        g.drawString(text, x, y);
    }

}
