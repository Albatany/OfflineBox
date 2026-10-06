package offlinebox;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.security.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;

import static java.nio.charset.StandardCharsets.UTF_8;

/** Daftar modul. Untuk menambah alat baru: buat fungsi panel, lalu daftarkan di all(). */
final class Modules {
    private Modules() { }

    record Op(String name, UnaryOperator<String> fn) { }

    static List<Main.Mod> all() {
        return List.of(
            new Main.Mod("", "TUTORIAL", "Baca panduan singkat ini dulu, lalu pilih alat di daftar kiri. (2026 Albatany)", Main::tutorial),
            new Main.Mod("SYSTEM", "System Monitor", "Diperbarui tiap detik. Hijau = aman, kuning = tinggi, merah = hampir penuh.", Modules::monitor),
            new Main.Mod("FILES", "File Hasher", "Pilih algoritma, klik PILIH FILE. Checksum dipakai untuk memastikan file tidak berubah/rusak. File hanya dibaca.", Modules::hasher),
            new Main.Mod("TEXT", "JSON Formatter", "Tempel JSON di INPUT, klik FORMAT atau MINIFY. Alat ini merapikan dan mengecek kurung/kutip, bukan validator penuh.",
                () -> io(new Op("FORMAT", s -> json(s, true)), new Op("MINIFY", s -> json(s, false)))),
            new Main.Mod("TEXT", "Base64", "Ketik teks lalu ENCODE, atau tempel Base64 lalu DECODE.",
                () -> io(new Op("ENCODE", s -> Base64.getEncoder().encodeToString(s.getBytes(UTF_8))),
                         new Op("DECODE", s -> new String(Base64.getDecoder().decode(s.replaceAll("\\s", "")), UTF_8)))),
            new Main.Mod("TEXT", "URL Encoder", "ENCODE mengubah karakter khusus menjadi %XX, DECODE mengembalikannya.",
                () -> io(new Op("ENCODE", s -> URLEncoder.encode(s, UTF_8)), new Op("DECODE", s -> URLDecoder.decode(s, UTF_8)))),
            new Main.Mod("NETWORK", "Subnet Calculator", "Ketik IPv4 + CIDR, contoh 192.168.1.10/24, lalu klik HITUNG.",
                () -> io(new Op("HITUNG", Modules::subnet))),
            new Main.Mod("UTILITY", "Password Generator", "Atur panjang & jenis karakter, klik GENERATE, lalu COPY. Password tidak disimpan.", Modules::password),
            new Main.Mod("UTILITY", "Timestamp Converter", "Isi angka epoch lalu pilih DETIK atau MS. Untuk TANGGAL -> EPOCH pakai format 2026-10-06T10:30:00 (boleh diakhiri Z atau +07:00).",
                () -> io(new Op("DETIK -> TANGGAL", s -> ts(s, false)), new Op("MS -> TANGGAL", s -> ts(s, true)),
                         new Op("TANGGAL -> EPOCH", Modules::iso),
                         new Op("SEKARANG", s -> ts(String.valueOf(Instant.now().getEpochSecond()), false)))));
    }

    static JPanel io(Op... ops) {
        JTextArea in = Px.ta(true), out = Px.ta(false);
        JPanel bar = Px.row();
        for (Op o : ops) {
            bar.add(Px.btn(o.name(), () -> {
                try {
                    String r = o.fn().apply(Px.in(in));
                    out.setForeground(Px.CYAN);
                    out.setText(r);
                } catch (RuntimeException e) {
                    out.setForeground(Px.RED);
                    out.setText("ERROR: " + Objects.toString(e.getMessage(), e.toString()));
                }
            }));
        }
        bar.add(Px.btn("COPY", () -> Px.copy(out.getText())));
        bar.add(Px.btn("CLEAR", () -> { in.setText(""); out.setText(""); }));
        JPanel mid = new JPanel(new GridLayout(1, 2, 10, 0));
        mid.setOpaque(false);
        mid.add(Px.titled("INPUT", Px.scroll(in)));
        mid.add(Px.titled("OUTPUT", Px.scroll(out)));
        JPanel p = Px.page();
        p.add(bar, BorderLayout.NORTH);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    static String json(String src, boolean pretty) {
        String t = src.strip();
        if (t.isEmpty()) throw new IllegalArgumentException("Input kosong");
        StringBuilder o = new StringBuilder();
        Deque<Character> st = new ArrayDeque<>();
        boolean str = false, esc = false;
        int ind = 0;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (str) {
                o.append(c);
                if (esc) esc = false;
                else if (c == '\\') esc = true;
                else if (c == '"') str = false;
                continue;
            }
            switch (c) {
                case '"' -> { str = true; o.append(c); }
                case '{', '[' -> {
                    char close = c == '{' ? '}' : ']';
                    int j = i + 1;
                    while (j < t.length() && Character.isWhitespace(t.charAt(j))) j++;
                    if (j < t.length() && t.charAt(j) == close) { o.append(c).append(close); i = j; }
                    else { st.push(close); o.append(c); if (pretty) nl(o, ++ind); }
                }
                case '}', ']' -> {
                    if (st.isEmpty() || st.pop() != c) throw new IllegalArgumentException("Kurung tidak seimbang di posisi " + i);
                    if (pretty) nl(o, --ind);
                    o.append(c);
                }
                case ',' -> { o.append(c); if (pretty) nl(o, ind); }
                case ':' -> o.append(pretty ? ": " : ":");
                default -> { if (!Character.isWhitespace(c)) o.append(c); }
            }
        }
        if (str) throw new IllegalArgumentException("Tanda kutip belum ditutup");
        if (!st.isEmpty()) throw new IllegalArgumentException("Ada kurung yang belum ditutup");
        return o.toString();
    }

    private static void nl(StringBuilder o, int n) {
        o.append('\n').append("  ".repeat(Math.max(0, n)));
    }

    static String subnet(String s) {
        String[] p = s.trim().split("/", -1);
        if (p.length != 2) throw new IllegalArgumentException("Format: 192.168.1.10/24");
        String[] o = p[0].split("\\.", -1);
        if (o.length != 4) throw new IllegalArgumentException("IPv4 harus punya 4 oktet");
        long ip = 0;
        for (String x : o) {
            if (!x.matches("0|[1-9]\\d{0,2}")) throw new IllegalArgumentException("Oktet tidak valid: '" + x + "'");
            int v = Integer.parseInt(x);
            if (v > 255) throw new IllegalArgumentException("Oktet harus 0-255: " + v);
            ip = (ip << 8) | v;
        }
        if (!p[1].matches("0|[1-9]\\d?")) throw new IllegalArgumentException("CIDR tidak valid: '" + p[1] + "'");
        int n = Integer.parseInt(p[1]);
        if (n > 32) throw new IllegalArgumentException("CIDR harus 0-32");
        long mask = n == 0 ? 0 : (0xFFFFFFFFL << (32 - n)) & 0xFFFFFFFFL;
        long net = ip & mask, bc = net | (~mask & 0xFFFFFFFFL);
        long total = 1L << (32 - n);
        boolean small = n >= 31; // /31 dan /32 tidak mengurangi alamat network/broadcast
        StringBuilder r = new StringBuilder();
        r.append(String.format("%-14s: %s%n", "Alamat", dot(ip)));
        r.append(String.format("%-14s: %s (/%d)%n", "Netmask", dot(mask), n));
        r.append(String.format("%-14s: %s%n", "Wildcard", dot(~mask & 0xFFFFFFFFL)));
        r.append(String.format("%-14s: %s%n", "Network", dot(net)));
        r.append(String.format("%-14s: %s%n", "Broadcast", dot(bc)));
        r.append(String.format("%-14s: %s%n", "Host pertama", dot(small ? net : net + 1)));
        r.append(String.format("%-14s: %s%n", "Host terakhir", dot(small ? bc : bc - 1)));
        r.append(String.format("%-14s: %d", "Jumlah host", small ? total : total - 2));
        return r.toString();
    }

    private static String dot(long v) {
        return ((v >> 24) & 255) + "." + ((v >> 16) & 255) + "." + ((v >> 8) & 255) + "." + (v & 255);
    }

    // ---------- UTILITY ----------
    static String ts(String s, boolean ms) {
        long v = Long.parseLong(s.trim());
        Instant i = ms ? Instant.ofEpochMilli(v) : Instant.ofEpochSecond(v);
        return "UTC   : " + i + "\nLokal : " + i.atZone(ZoneId.systemDefault());
    }

    static String iso(String s) {
        String t = s.trim();
        Instant i;
        try {
            i = OffsetDateTime.parse(t).toInstant();
        } catch (DateTimeParseException e) {
            i = LocalDateTime.parse(t).atZone(ZoneId.systemDefault()).toInstant();
        }
        return "Epoch (detik): " + i.getEpochSecond() + "\nEpoch (ms)   : " + i.toEpochMilli();
    }

    static JPanel password() {
        String[] sets = {"abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "0123456789", "!@#$%^&*()-_=+[]{};:,.?"};
        String[] names = {"a-z", "A-Z", "0-9", "!@#"};
        JSpinner len = new JSpinner(new SpinnerNumberModel(16, 8, 128, 1));
        JCheckBox[] cb = new JCheckBox[4];
        JTextArea out = Px.ta(false);
        JPanel bar = Px.row();
        bar.add(Px.label("PANJANG:", Px.GOLD));
        bar.add(len);
        for (int i = 0; i < 4; i++) {
            cb[i] = new JCheckBox(names[i], true);
            cb[i].setFont(Px.F);
            cb[i].setForeground(Px.INK);
            cb[i].setOpaque(false);
            bar.add(cb[i]);
        }
        bar.add(Px.btn("GENERATE", () -> {
            List<String> use = new ArrayList<>();
            for (int i = 0; i < 4; i++) if (cb[i].isSelected()) use.add(sets[i]);
            if (use.isEmpty()) {
                out.setForeground(Px.RED);
                out.setText("ERROR: pilih minimal satu jenis karakter");
                return;
            }
            int n = (Integer) len.getValue();
            SecureRandom r = new SecureRandom();
            StringBuilder all = new StringBuilder();
            List<Character> pw = new ArrayList<>();
            for (String u : use) { all.append(u); pw.add(u.charAt(r.nextInt(u.length()))); }
            while (pw.size() < n) pw.add(all.charAt(r.nextInt(all.length())));
            Collections.shuffle(pw, r);
            StringBuilder sb = new StringBuilder();
            for (char c : pw) sb.append(c);
            out.setForeground(Px.GREEN);
            out.setText(sb.toString());
        }));
        bar.add(Px.btn("COPY", () -> Px.copy(out.getText())));
        JPanel p = Px.page();
        p.add(bar, BorderLayout.NORTH);
        p.add(Px.titled("PASSWORD", Px.scroll(out)), BorderLayout.CENTER);
        return p;
    }

    // ---------- FILES ----------
    static JPanel hasher() {
        JTextArea out = Px.ta(false);
        JComboBox<String> alg = new JComboBox<>(new String[]{"SHA-256", "SHA-512", "SHA-1", "MD5"});
        JPanel bar = Px.row();
        bar.add(Px.label("ALGORITMA:", Px.GOLD));
        bar.add(alg);
        bar.add(Px.btn("PILIH FILE...", () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
            Path p = fc.getSelectedFile().toPath();
            String a = (String) alg.getSelectedItem();
            out.setForeground(Px.GOLD);
            out.setText("Menghitung...");
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    if (!Files.isRegularFile(p)) throw new IOException("Bukan file biasa");
                    MessageDigest md = MessageDigest.getInstance(a);
                    try (InputStream in = Files.newInputStream(p)) {
                        byte[] buf = new byte[65536];
                        int n;
                        while ((n = in.read(buf)) != -1) md.update(buf, 0, n);
                    }
                    return HexFormat.of().formatHex(md.digest());
                }
                @Override protected void done() {
                    try {
                        String h = get();
                        out.setForeground(Px.CYAN);
                        out.setText(a + "\n" + h + "\n\nFile: " + p.getFileName());
                    } catch (Exception e) {
                        out.setForeground(Px.RED);
                        out.setText("ERROR: " + Objects.toString(e.getMessage(), e.toString()));
                    }
                }
            }.execute();
        }));
        JPanel pg = Px.page();
        pg.add(bar, BorderLayout.NORTH);
        pg.add(Px.titled("HASIL", Px.scroll(out)), BorderLayout.CENTER);
        return pg;
    }

    // ---------- SYSTEM ----------
    static final class Bar extends JComponent {
        private double v;
        private String t = "";

        Bar() { setPreferredSize(new Dimension(500, 34)); }

        void set(double v, String t) {
            this.v = Double.isNaN(v) ? 0 : Math.max(0, Math.min(1, v));
            this.t = t;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            g.setColor(Px.DARK);
            g.fillRect(0, 0, getWidth(), getHeight());
            int seg = 25, w = Math.max(3, (getWidth() - 8) / seg), on = (int) Math.round(v * seg);
            for (int i = 0; i < seg; i++) {
                g.setColor(i < on ? (v > .85 ? Px.RED : v > .6 ? Px.GOLD : Px.GREEN) : Px.PANEL);
                g.fillRect(4 + i * w, 4, w - 2, getHeight() - 8);
            }
            g.setFont(Px.F);
            g.setColor(Color.BLACK);
            g.drawString(t, 11, getHeight() / 2 + 6);
            g.setColor(Px.INK);
            g.drawString(t, 10, getHeight() / 2 + 5);
        }
    }

    private static String sz(long b) {
        String[] u = {"B", "KB", "MB", "GB", "TB"};
        double d = b;
        int i = 0;
        while (d >= 1024 && i < 4) { d /= 1024; i++; }
        return String.format("%.1f %s", d, u[i]);
    }

    static JPanel monitor() {
        var raw = ManagementFactory.getOperatingSystemMXBean();
        var os = raw instanceof com.sun.management.OperatingSystemMXBean x ? x : null;
        Bar cpu = new Bar(), ram = new Bar();
        File[] roots = Arrays.stream(File.listRoots()).filter(f -> f.getTotalSpace() > 0).limit(8).toArray(File[]::new);
        Bar[] dk = new Bar[roots.length];
        JPanel box = new JPanel(new GridLayout(0, 1, 0, 10));
        box.setOpaque(false);
        box.add(cpu);
        box.add(ram);
        for (int i = 0; i < dk.length; i++) { dk[i] = new Bar(); box.add(dk[i]); }
        Runnable tick = () -> {
            if (os != null) {
                double c = os.getCpuLoad();
                if (c >= 0) cpu.set(c, String.format("CPU   %.0f%%", c * 100));
                else cpu.set(0, "CPU   (mengukur...)");
                long tot = os.getTotalMemorySize(), used = tot - os.getFreeMemorySize();
                if (tot > 0) ram.set((double) used / tot, "RAM   " + sz(used) + " / " + sz(tot));
            } else {
                cpu.set(0, "CPU   tidak didukung di sistem ini");
                ram.set(0, "RAM   tidak didukung di sistem ini");
            }
            for (int i = 0; i < roots.length; i++) {
                long tot = roots[i].getTotalSpace(), used = tot - roots[i].getUsableSpace();
                if (tot > 0) dk[i].set((double) used / tot, "DISK  " + roots[i] + "  " + sz(used) + " / " + sz(tot));
            }
        };
        new javax.swing.Timer(1000, e -> tick.run()).start();
        tick.run();
        JPanel p = Px.page();
        p.add(box, BorderLayout.NORTH);
        return p;
    }
}
