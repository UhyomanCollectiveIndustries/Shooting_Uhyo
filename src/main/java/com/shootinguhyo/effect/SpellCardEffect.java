package com.shootinguhyo.effect;

import java.awt.*;

public class SpellCardEffect {
    private Color overlayColor;
    private String spellName;
    private boolean active;
    private int displayTimer;

    public SpellCardEffect() {
        this.active = false;
    }

    public void activate(Color color, String name) {
        this.overlayColor = color;
        this.spellName = name;
        this.active = true;
        this.displayTimer = 120;
    }

    public void deactivate() {
        this.active = false;
    }

    public void update() {
        if (displayTimer > 0) displayTimer--;
    }

    public boolean isActive() { return active; }

    public void draw(Graphics2D g, int fieldWidth, int fieldHeight) {
        if (!active) return;

        g.setColor(overlayColor);
        g.fillRect(0, 0, fieldWidth, fieldHeight);

        float alpha = displayTimer > 20 ? 1.0f : displayTimer / 20.0f;
        g.setColor(new Color(1.0f, 1.0f, 1.0f, alpha));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(spellName);
        g.drawString(spellName, (fieldWidth - textW) / 2, 35);
    }
}
