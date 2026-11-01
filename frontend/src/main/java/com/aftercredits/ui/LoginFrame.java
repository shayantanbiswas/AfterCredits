package com.aftercredits.ui;

import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.client.auth.AuthApiClient;
import com.aftercredits.ui.components.CinemaLogo;
import com.aftercredits.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Cinematic entry screen for AfterCredits.
 * Authentication is intentionally local for the desktop demo:
 * the entered display name is passed into the application shell.
 */
public class LoginFrame extends JFrame {

    private final CinemaRepository repository;
    private final Runnable onLogin;

    private JTextField nameField;
    private JPasswordField passwordField;
    private JPanel card;
    private float animationProgress = 0f;

    public LoginFrame(CinemaRepository repository, Runnable onLogin) {
        super("AfterCredits");
        this.repository = repository;
        this.onLogin = onLogin;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1200, 780);
        setLocationRelativeTo(null);
        setUndecorated(false);

        buildUI();
        startEntranceAnimation();
    }

    private void buildUI() {
        CinematicBackground background = new CinematicBackground();
        background.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        CinemaLogo logo = new CinemaLogo(58);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("AFTERCREDITS");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));

        JLabel subtitle = new JLabel("YOUR CINEMA. YOUR TASTE. YOUR STORY.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));

        content.add(logo);
        content.add(Box.createVerticalStrut(4));
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(28));

        card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new Color(16, 19, 25, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR),
                new EmptyBorder(30, 34, 28, 34)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setPreferredSize(new Dimension(390, 360));
        card.setMaximumSize(new Dimension(390, 360));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 23));

        JLabel hint = new JLabel("Enter your details to enter AfterCredits");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(ThemeManager.TEXT_MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));

        card.add(welcome);
        card.add(Box.createVerticalStrut(6));
        card.add(hint);
        card.add(Box.createVerticalStrut(25));

        JLabel nameLabel = fieldLabel("EMAIL");
        nameField = new JTextField();
        styleField(nameField);
        nameField.setToolTipText("Your name on AfterCredits");

        JLabel passwordLabel = fieldLabel("PASSWORD");
        passwordField = new JPasswordField();
        styleField(passwordField);

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(nameField);
        card.add(Box.createVerticalStrut(15));
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(7));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(22));

        JButton signIn = createPrimaryButton("SIGN IN");
        signIn.addActionListener(e -> login());

        JButton guest = createGhostButton("CONTINUE AS GUEST");
        guest.addActionListener(e -> guestLogin());

        card.add(signIn);
        card.add(Box.createVerticalStrut(10));
        card.add(guest);

        content.add(card);
        content.add(Box.createVerticalStrut(18));

        JLabel footer = new JLabel("A social cinema experience by AfterCredits");
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.setForeground(ThemeManager.TEXT_FAINT);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));

        content.add(footer);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        background.add(content, gbc);
        setContentPane(background);

        getRootPane().setDefaultButton(signIn);
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(ThemeManager.TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        return label;
    }

    private void styleField(JTextField field) {
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setPreferredSize(new Dimension(320, 42));
        field.setMaximumSize(new Dimension(320, 42));
        field.setBackground(ThemeManager.BG_INPUT);
        field.setForeground(Color.WHITE);
        field.setCaretColor(ThemeManager.ACCENT_GOLD);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR),
                new EmptyBorder(0, 12, 0, 12)
        ));
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setForeground(Color.BLACK);
        button.setBackground(ThemeManager.ACCENT_GOLD);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ThemeManager.ACCENT_GOLD.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ThemeManager.ACCENT_GOLD);
            }
        });

        return button;
    }

    private JButton createGhostButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(320, 38));
        button.setMaximumSize(new Dimension(320, 38));
        button.setForeground(ThemeManager.TEXT_MUTED);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setFont(new Font("SansSerif", Font.BOLD, 10));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(ThemeManager.TEXT_MUTED);
            }
        });

        return button;
    }

    private void login() {
        String email = nameField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your email and password.",
                    "Missing details",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<AuthApiClient.AuthResult, Void>() {
            @Override
            protected AuthApiClient.AuthResult doInBackground() throws Exception {
                return new AuthApiClient().login(email, password);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());

                try {
                    AuthApiClient.AuthResult result = get();

                    System.setProperty("aftercredits.user.name", result.displayName());
                    System.setProperty("aftercredits.user.email", result.email());
                    System.setProperty("aftercredits.auth.token", result.token());

                    if (onLogin != null) {
                        onLogin.run();
                    }
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String message = cause.getMessage();

                    if (message == null || message.isBlank()) {
                        message = "Could not connect to the AfterCredits server.";
                    }

                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            message,
                            "Sign in failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void guestLogin() {
        System.setProperty("aftercredits.user.name", "Guest");
        System.clearProperty("aftercredits.user.email");
        System.clearProperty("aftercredits.auth.token");

        if (onLogin != null) {
            onLogin.run();
        }
        dispose();
    }

    private void startEntranceAnimation() {
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 0)),
                new EmptyBorder(30, 34, 28, 34)
        ));

        Timer timer = new Timer(16, null);

        timer.addActionListener(e -> {
            animationProgress += 0.045f;

            float p = Math.min(1f, animationProgress);
            float eased = 1f - (float) Math.pow(1f - p, 3);

            int alpha = Math.min(245, (int) (245 * eased));

            card.setOpaque(true);
            card.setBackground(new Color(16, 19, 25, alpha));

            int yOffset = (int) ((1f - eased) * 24);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            new Color(
                                    ThemeManager.BORDER_COLOR.getRed(),
                                    ThemeManager.BORDER_COLOR.getGreen(),
                                    ThemeManager.BORDER_COLOR.getBlue(),
                                    Math.max(1, alpha)
                            )
                    ),
                    new EmptyBorder(30 + yOffset, 34, 28, 34)
            ));

            card.revalidate();
            card.repaint();

            if (p >= 1f) {
                timer.stop();
            }
        });

        timer.start();
    }

    private static class CinematicBackground extends JPanel {

        private float pulse = 0f;

        CinematicBackground() {
            setOpaque(true);

            Timer timer = new Timer(40, e -> {
                pulse += 0.018f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            GradientPaint base = new GradientPaint(
                    0, 0,
                    new Color(5, 7, 10),
                    w, h,
                    new Color(14, 17, 23)
            );

            g2.setPaint(base);
            g2.fillRect(0, 0, w, h);

            float x = (float) ((Math.sin(pulse) + 1) / 2.0);
            float y = (float) ((Math.cos(pulse * 0.7) + 1) / 2.0);

            RadialGradientPaint glow = new RadialGradientPaint(
                    new Point(
                            (int) (w * (0.25 + x * 0.5)),
                            (int) (h * (0.25 + y * 0.35))
                    ),
                    Math.max(w, h) * 0.65f,
                    new float[]{0f, 0.45f, 1f},
                    new Color[]{
                            new Color(212, 175, 55, 28),
                            new Color(70, 75, 90, 10),
                            new Color(0, 0, 0, 0)
                    }
            );

            g2.setPaint(glow);
            g2.fillRect(0, 0, w, h);

            // Subtle cinematic light streaks.
            g2.setStroke(new BasicStroke(1f));

            for (int i = 0; i < 7; i++) {
                int yy = (int) ((h * (0.15 + i * 0.13))
                        + Math.sin(pulse + i) * 18);

                g2.setColor(new Color(255, 255, 255, 8));
                g2.drawLine(0, yy, w, yy - 70);
            }

            g2.dispose();
        }
    }
}
