package offlinebox;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** OfflineBox - made by albatany 2026 */
public final class Main {
    record Mod(String cat, String name, String help, Supplier<JPanel> ui) {}

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "off"); 
        System.setProperty("swing.aatext", "false");
        SwingUtilities.invokeLater(Main::start);
    }

    static void start() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
        JFrame f = new JFrame("OFFLINEBOX");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1000, 680);
        f.setMinimumSize(new Dimension(820, 560));
        f.setLocationRelativeTo(null);
        f.getContentPane().setBackground(Px.BG);

        CardLayout cl = new CardLayout();
        JPanel body = new JPanel(cl);
        body.setBackground(Px.BG);
        JTextArea help = Px.ta(false);
        help.setForeground(Px.GOLD);
        help.setRows(2);

        DefaultListModel<Object> model = new DefaultListModel<>();
        String cat = "";
        for (Mod x : Modules.all()) {
            if (!x.cat().equals(cat)) { cat = x.cat(); model.addElement(cat); }
            model.addElement(x);
        }
        JList<Object> list = new JList<>(model);
        list.setBackground(Px.BG);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, false, false);
                setFont(Px.F);
                setOpaque(true);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                if (v instanceof Mod x) {
                    setText((sel ? "> " : "  ") + x.name());
                    setBackground(sel ? Px.ORANGE : Px.BG);
                    setForeground(sel ? Px.DARK : Px.INK);
                } else {
                    setText("[" + v + "]");
                    setBackground(Px.BG);
                    setForeground(Px.CYAN);
                }
                return this;
            }
        });

        Set<String> made = new HashSet<>();
        int[] last = {0};
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            if (!(list.getSelectedValue() instanceof Mod x)) { list.setSelectedIndex(last[0]); return; }
            last[0] = list.getSelectedIndex();
            if (made.add(x.name())) body.add(x.ui().get(), x.name()); // modul dibuat saat pertama dibuka
            cl.show(body, x.name());
            help.setText("CARA PAKAI: " + x.help());
        });

        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Px.BG);
        side.setBorder(new MatteBorder(0, 0, 0, 4, Px.INK));
        side.setPreferredSize(new Dimension(230, 0));
        side.add(logo(), BorderLayout.NORTH);
        side.add(Px.scroll(list), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 0));
        right.setBackground(Px.BG);
        right.add(body, BorderLayout.CENTER);
        right.add(Px.scroll(help), BorderLayout.SOUTH);

        f.add(side, BorderLayout.WEST);
        f.add(right, BorderLayout.CENTER);
        list.setSelectedIndex(0);
        f.setVisible(true);
    }

    static JComponent logo() {
        String[] s = {"..WWWWWW..", ".WYYYYYYW.", "WYYYYYYYYW", "WWWWYYWWWW",
                      "WOOOWWOOOW", "WOOOOOOOOW", "WOOOOOOOOW", ".WWWWWWWW."};
        return new JComponent() {
            { setPreferredSize(new Dimension(230, 72)); }
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Px.BG);
                g.fillRect(0, 0, getWidth(), getHeight());
                for (int y = 0; y < s.length; y++) {
                    for (int x = 0; x < 10; x++) {
                        char c = s[y].charAt(x);
                        if (c == '.') continue;
                        g.setColor(c == 'W' ? Px.INK : c == 'Y' ? Px.GOLD : Px.ORANGE);
                        g.fillRect(12 + x * 5, 12 + y * 6, 5, 6);
                    }
                }
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
                g.setColor(Px.GOLD);
                g.drawString("OFFLINE", 76, 34);
                g.setColor(Px.CYAN);
                g.drawString("BOX", 76, 56);
            }
        };
    }

    static JPanel tutorial() {
        JTextArea t = Px.ta(false);
        t.setForeground(Px.INK);
        t.setWrapStyleWord(true);
        t.setText("""
                SELAMAT DATANG DI OFFLINEBOX!
                Satu aplikasi, banyak alat. Semuanya jalan 100% offline.

                CARA DASAR
                1. Pilih alat di daftar sebelah kiri.
                2. Isi kotak INPUT, lalu klik tombol aksinya (ENCODE, FORMAT, HITUNG, dst).
                3. Hasil muncul di OUTPUT. Klik COPY untuk menyalin, CLEAR untuk mengosongkan.
                4. Kotak kuning di bawah selalu menjelaskan cara pakai alat yang sedang aktif.
                5. Pesan merah berarti input tidak valid - baca pesannya, perbaiki, coba lagi.

                ALAT YANG TERSEDIA
                SYSTEM  : System Monitor (CPU, RAM, Disk)
                FILES   : File Hasher (MD5 / SHA-1 / SHA-256 / SHA-512)
                TEXT    : JSON Formatter, Base64, URL Encoder
                NETWORK : Subnet Calculator (IPv4 + CIDR)
                UTILITY : Password Generator, Timestamp Converter

                KEAMANAN
                - Tidak ada koneksi internet, tidak ada telemetri.
                - Tidak menjalankan program atau perintah sistem lain.
                - File hanya DIBACA, tidak pernah diubah atau dihapus.
                - Password dibuat dengan SecureRandom (acak kuat), tidak disimpan di mana pun.
                """);
        t.setCaretPosition(0);
        JPanel p = Px.page();
        p.add(Px.scroll(t));
        return p;
    }
}

/** Helper tampilan pixel art: palet warna + komponen bergaya kotak tegas. */
final class Px {
    static final Color BG = new Color(0x1a1c2c), DARK = new Color(0x12121f), PANEL = new Color(0x333c57),
            INK = new Color(0xf4f4f4), GOLD = new Color(0xffcd75), ORANGE = new Color(0xef7d57),
            GREEN = new Color(0xa7f070), CYAN = new Color(0x73eff7), RED = new Color(0xff5a6e);
    static final Font F = new Font(Font.MONOSPACED, Font.BOLD, 14);
    static final int MAX_CHARS = 2_000_000; // batas input agar aplikasi tidak hang

    private Px() { }

    static JLabel label(String t, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(F);
        l.setForeground(c);
        return l;
    }

    static JButton btn(String t, Runnable r) {
        JButton b = new JButton(t);
        b.setFont(F);
        b.setForeground(DARK);
        b.setBackground(GREEN);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(new LineBorder(INK, 2), new EmptyBorder(6, 12, 6, 12)));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(GOLD); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(GREEN); }
        });
        b.addActionListener(e -> r.run());
        return b;
    }

    static JTextArea ta(boolean editable) {
        JTextArea t = new JTextArea();
        t.setFont(F);
        t.setEditable(editable);
        t.setBackground(DARK);
        t.setForeground(editable ? INK : CYAN);
        t.setCaretColor(GOLD);
        t.setLineWrap(true);
        t.setBorder(new EmptyBorder(6, 6, 6, 6));
        return t;
    }

    static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(new LineBorder(INK, 3));
        s.getViewport().setBackground(DARK);
        return s;
    }

    static JPanel page() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        return p;
    }

    static JPanel row() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        return p;
    }

    static JPanel titled(String t, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(label(t, GOLD), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    /** Ambil teks input dengan batas ukuran. */
    static String in(JTextArea t) {
        String s = t.getText();
        if (s.length() > MAX_CHARS) throw new IllegalArgumentException("Input terlalu besar (maks 2.000.000 karakter)");
        return s;
    }

    static void copy(String s) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
        } catch (HeadlessException | IllegalStateException ignored) { }
    }
}
