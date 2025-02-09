package Components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ShadowButton extends JButton {
    ImageIcon redFLag = new ImageIcon("src/assets/redflag.png");
    private ImageIcon icon;

    public ShadowButton(ImageIcon icon, String text) {
        super(text);
        setContentAreaFilled(false); // Torna o fundo do botão transparente
        setFocusPainted(false); // Remove o contorno de foco

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    repaintIcon(redFLag);
                }
            }
        });
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        int arcWidth = 5;
        int arcHeight = 5;

        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, arcWidth, arcHeight);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, arcWidth, arcHeight);
        g2.setColor(getForeground());
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        if (icon != null) {
            int iconX = (getWidth() - icon.getIconWidth()) / 2;
            int iconY = (getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, iconX, iconY);
        }

        g2.drawString(getText(), x, y);

        g2.dispose();
    }

    public void repaintIcon(ImageIcon newIcon) {
        this.icon = newIcon;
        repaint();
    }



    public static Color brighten(Color color, float factor) {
        factor = Math.min(1, Math.max(0, factor));

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        red = (int) (red + (255 - red) * factor);
        green = (int) (green + (255 - green) * factor);
        blue = (int) (blue + (255 - blue) * factor);

        return new Color(red, green, blue);
    }
}
