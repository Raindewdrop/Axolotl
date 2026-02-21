package awa.hyw;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;
import javax.swing.UIManager;

import com.google.gson.JsonObject;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import awa.hyw.common.status.Status;
import awa.hyw.common.status.StatusServer;
import awa.hyw.ui.LogUI;
import awa.hyw.ui.TrayIconGenerator;

public class ServiceMain {
    private static int statusServerPort = 0;
    private static final File log = new File("Axolotl-loader.log");
    private static final File errorLog = new File("Axolotl-loader-error.log");

    private static File self;

    private static final AtomicBoolean running = new AtomicBoolean(true);
    // 存储已加载的JVM进程ID
    private static final Set<String> injectedPids = new HashSet<>();
    
    // 设置全局字体
    private static final Font CHINESE_FONT = new Font("Microsoft YaHei", Font.PLAIN, 12);
    private static TrayIcon trayIcon;
    private static boolean showUiOnDetection = true;

    static {
        try {
            self = new File(ServiceMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            System.err.println("Failed to get self path: " + e.getMessage());
            e.printStackTrace();
            self = new File("Axolotl Inject.jar");
        }

        try {
            if (!log.exists() && !log.createNewFile())
                throw new IOException("无法创建日志文件: " + log.getAbsolutePath());
            if (!errorLog.exists() && !errorLog.createNewFile())
                throw new IOException("无法创建错误日志文件: " + errorLog.getAbsolutePath());
            
            // Custom PrintStream to capture logs and redact tokens
            PrintStream fileOut = new PrintStream(new FileOutputStream(log, true));
            PrintStream fileErr = new PrintStream(new FileOutputStream(errorLog, true));
            
            System.setOut(new PrintStream(fileOut) {
                @Override
                public void print(String x) {
                    super.print(x);
                    LogUI.getInstance().log(x);
                }
            });
            
            System.setErr(new PrintStream(fileErr) {
                @Override
                public void print(String x) {
                    super.print(x);
                    LogUI.getInstance().log("ERROR: " + x);
                }
            });

            // 设置全局字体
            setGlobalFont();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置全局字体，使中文正常显示
     */
    private static void setGlobalFont() {
        // 设置字体时尝试获取系统上的中文字体
        Font chineseFont = getChineseFont();

        // 设置UIManager中的所有字体
        UIManager.put("Button.font", chineseFont);
        UIManager.put("Label.font", chineseFont);
        UIManager.put("Menu.font", chineseFont);
        UIManager.put("MenuItem.font", chineseFont);
        UIManager.put("PopupMenu.font", chineseFont);
        UIManager.put("CheckboxMenuItem.font", chineseFont);
        UIManager.put("ToolTip.font", chineseFont);
        UIManager.put("OptionPane.messageFont", chineseFont);
        UIManager.put("OptionPane.buttonFont", chineseFont);
        UIManager.put("OptionPane.font", chineseFont);
        UIManager.put("Dialog.font", chineseFont);
        UIManager.put("Panel.font", chineseFont);

        // 设置JDialog和JFrame的默认字体
        try {
            Font titleFont = chineseFont.deriveFont(Font.BOLD, 14);
            UIManager.put("InternalFrame.titleFont", titleFont);
            JDialog.setDefaultLookAndFeelDecorated(true);
        } catch (Exception e) {
            System.err.println("设置标题字体失败: " + e.getMessage());
        }
    }

    private static Font getChineseFont() {
        String[] fontFamilies = {"Microsoft YaHei", "SimSun", "NSimSun", "SimHei", "Dialog"};
        Font chineseFont = null;
        for (String family : fontFamilies) {
            chineseFont = new Font(family, Font.PLAIN, 12);
            if (chineseFont.canDisplay('中')) {
                break;
            }
        }
        return chineseFont;
    }
    
    public static void main(String[] args) {
        try {
            StatusServer statusServer = new StatusServer(status -> {
                System.out.println("Status update: " + status.getDescription());
                updateUiStatus(status);
            }, 0); // Use port 0 for automatic port selection
            statusServerPort = statusServer.getPort();
            System.out.println("Status Server started on port " + statusServerPort);
            new Thread(statusServer, "StatusServer").start();
        } catch (IOException e) {
            System.err.println("Failed to start status server: " + e.getMessage());
        }
        LogUI.showUI();
        createTrayIcon();

        System.out.println("Axolotl Service started.");
        
        while (running.get()) {
            try {
                findAndInject();
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                System.err.println("Error in main loop: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

    private static void findAndInject() {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor vmDesc : vms) {
            String pid = vmDesc.id();
            if (injectedPids.contains(pid)) continue;

            if (isMinecraftProcess(vmDesc)) {
                System.out.println("Found Minecraft process: " + pid + " " + vmDesc.displayName());
                if (showUiOnDetection) {
                    LogUI.showUI();
                    showUiOnDetection = false;
                }
                
                try {
                    LogUI.getInstance().setStatus("正在注入...", false, false);
                    System.out.println("Using Agent Jar: " + self.getAbsolutePath());
                    if (!self.exists()) {
                         throw new IOException("Agent Jar not found: " + self.getAbsolutePath());
                    }
                    inject(pid);
                    injectedPids.add(pid);
                    running.set(false);
                } catch (Exception e) {
                    LogUI.getInstance().setStatus("注入失败", true, false);
                    System.err.println("Failed to inject into process " + pid + ": " + e.getMessage());
                    e.printStackTrace(System.err);
                    running.set(false);
                }
            }
        }
    }

    private static boolean isMinecraftProcess(VirtualMachineDescriptor vmDesc) {
        String displayName = vmDesc.displayName();
        return isOfficialMinecraft(displayName) || isNeteaseMinecraft(displayName);
    }

    private static boolean isOfficialMinecraft(String displayName) {
        return displayName.contains("Minecraft") || displayName.contains("net.minecraft.client.main.Main");
    }

    private static boolean isNeteaseMinecraft(String displayName) {
        String lower = displayName.toLowerCase();
        return lower.contains("netease") 
            || lower.contains("com.netease.mc")
            || lower.contains("163")
            || displayName.contains("MCLauncher");
    }

    private static void inject(String pid) throws Exception {
        VirtualMachine vm = VirtualMachine.attach(pid);
        try {
            JsonObject config = new JsonObject();
            config.addProperty("file", self.getAbsolutePath());
            if (statusServerPort > 0) {
                config.addProperty("statusPort", statusServerPort);
            }
            
            String agentArgs = config.toString();
            vm.loadAgent(self.getAbsolutePath(), agentArgs);
            System.out.println("Agent loaded into process " + pid);
            LogUI.getInstance().setStatus("注入成功 (等待初始化)", false, true);
        } finally {
            vm.detach();
        }
    }

    private static void updateUiStatus(Status status) {
        int code = status.getCode();
        String desc = status.getDescription();
        
        if (status == Status.SUCCESS) {
            LogUI.getInstance().setStatus("注入成功", false, true);
            System.out.println("Injection Success: " + desc);
            showUiOnDetection = true;
        } else if (code >= 9000 && status != Status.SUCCESS) { // Error codes are 9xxx (except success)
             LogUI.getInstance().setStatus("错误: " + desc, true, false);
             System.err.println("Error status: " + desc);
        } else {
             // Normal progress codes (1xxx, 2xxx)
             LogUI.getInstance().setStatus(desc, false, false);
             System.out.println("Status: " + desc);
        }
    }

    private static void createTrayIcon() {
        if (!SystemTray.isSupported()) return;

        SystemTray tray = SystemTray.getSystemTray();
        Image image = TrayIconGenerator.generateIcon();
        
        PopupMenu menu = new PopupMenu();
        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> {
            running.set(false);
            System.exit(0);
        });
        menu.add(exitItem);

        trayIcon = new TrayIcon(image, "Axolotl Loader", menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        }
    }
}
