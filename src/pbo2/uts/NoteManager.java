/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pbo2.uts;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Base64;

/**
 *
 * @author 07rrk
 */
public class NoteManager {
    private final List<Note> notes;
    private static final String NOTE_DELIMITER = "===NOTE===";
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NoteManager() {
        notes = new ArrayList<>();
    }

    // CRUD
    public void addNote(Note note) { notes.add(note); }
    public void deleteNote(Note note) { notes.remove(note); }
    public List<Note> getAllNotes() { return new ArrayList<>(notes); }

    public void updateNote(Note note, String newTitle, String newContent) {
        note.setTitle(newTitle);
        note.setContent(newContent);
    }

    public Note findById(String id) {
        for (Note n : notes) if (n.getId().equals(id)) return n;
        return null;
    }

    // Export ke file .txt dalam format aman (Base64 untuk konten)
    public void exportToTxt(File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (Note n : notes) {
                bw.write(NOTE_DELIMITER);
                bw.newLine();
                bw.write("id:" + n.getId()); bw.newLine();
                bw.write("title:" + escapeLine(n.getTitle())); bw.newLine();
                // encode content to base64 to preserve newlines and special chars
                String b64 = Base64.getEncoder().encodeToString(n.getContent().getBytes(StandardCharsets.UTF_8));
                bw.write("content_base64:" + b64); bw.newLine();
                bw.write("createdAt:" + n.getCreatedAt().format(fmt)); bw.newLine();
                bw.write("modifiedAt:" + n.getModifiedAt().format(fmt)); bw.newLine();
            }
        }
    }

    // Import dari file .txt yang dihasilkan oleh exportToTxt
    public void importFromTxt(File file) throws IOException {
        List<Note> imported = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            Map<String, String> current = null;
            while ((line = br.readLine()) != null) {
                if (line.equals(NOTE_DELIMITER)) {
                    if (current != null) {
                        Note n = mapToNote(current);
                        if (n != null) imported.add(n);
                    }
                    current = new HashMap<>();
                } else if (current != null && line.contains(":")) {
                    int idx = line.indexOf(':');
                    String key = line.substring(0, idx);
                    String val = line.substring(idx + 1);
                    current.put(key, val);
                }
            }
            if (current != null && !current.isEmpty()) {
                Note n = mapToNote(current);
                if (n != null) imported.add(n);
            }
        }

        // Tambahkan imported notes (hindari duplikat id)
        for (Note n : imported) {
            if (findById(n.getId()) == null) notes.add(n);
        }
    }

    private Note mapToNote(Map<String, String> m) {
        try {
            String id = m.getOrDefault("id", UUID.randomUUID().toString());
            String title = unescapeLine(m.getOrDefault("title", "Untitled"));
            String contentB64 = m.getOrDefault("content_base64", "");
            String content = "";
            if (!contentB64.isEmpty()) {
                byte[] decoded = Base64.getDecoder().decode(contentB64);
                content = new String(decoded, StandardCharsets.UTF_8);
            }
            LocalDateTime created = LocalDateTime.parse(m.getOrDefault("createdAt", LocalDateTime.now().format(fmt)), fmt);
            LocalDateTime modified = LocalDateTime.parse(m.getOrDefault("modifiedAt", created.format(fmt)), fmt);
            return new Note(id, title, content, created, modified);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String escapeLine(String s) {
        if (s == null) return "";
        return s.replace("\n", "\\n").replace("\r", "");
    }

    private String unescapeLine(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n");
    }

    // Pencarian sederhana berdasarkan kata di judul atau konten
    public List<Note> search(String keyword) {
        List<Note> res = new ArrayList<>();
        String k = keyword.toLowerCase();
        for (Note n : notes) {
            if ((n.getTitle() != null && n.getTitle().toLowerCase().contains(k)) ||
                (n.getContent() != null && n.getContent().toLowerCase().contains(k))) {
                res.add(n);
            }
        }
        return res;
    }

    // Clear semua note (opsional, digunakan untuk testing atau reset)
    public void clearAll() {
        notes.clear();
    }
}