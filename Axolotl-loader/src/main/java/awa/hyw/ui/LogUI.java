package awa.hyw.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogUI extends JFrame {
    private static final LogUI INSTANCE = new LogUI();
    private final JTextArea logArea;
    private final JLabel statusLabel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final Pattern[] TOKEN_PATTERNS = {
            Pattern.compile("(--accessToken\\s+)(\\S+)"),
            Pattern.compile("(--uuid\\s+)(\\S+)"),
            Pattern.compile("(accessToken=)(\\S+)"),
            Pattern.compile("(uuid=)(\\S+)")
    };

    private LogUI() {
        super("Axolotl Inject");
        setSize(600, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        statusPanel.setBackground(new Color(30, 30, 30));

        statusLabel = new JLabel("等待 Minecraft...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        add(statusPanel, BorderLayout.NORTH);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(40, 40, 40));
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        
        // Auto-scroll
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        add(scrollPane, BorderLayout.CENTER);
    }

    public static LogUI getInstance() {
        return INSTANCE;
    }

    public static void showUI() {
        SwingUtilities.invokeLater(() -> {
            INSTANCE.setVisible(true);
            INSTANCE.toFront();
        });
    }

    public static void hideUI() {
        SwingUtilities.invokeLater(() -> INSTANCE.setVisible(false));
    }

    public void log(String message) {
        String redacted = redact(message);
        String timestamp = dateFormat.format(new Date());
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + timestamp + "] " + redacted + "\n");
        });
    }

    public void setStatus(String status, boolean isError, boolean isSuccess) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            if (isError) {
                statusLabel.setForeground(new Color(255, 80, 80));
            } else if (isSuccess) {
                statusLabel.setForeground(new Color(50, 205, 50));
            } else {
                statusLabel.setForeground(Color.WHITE);
            }
        });
    }

    private String redact(String input) {
        if (input == null) return "";
        String result = input;
        for (Pattern pattern : TOKEN_PATTERNS) {
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                result = matcher.replaceAll("$1******");
            }
        }
        return result;
    }
}
