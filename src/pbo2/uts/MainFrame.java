/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pbo2.uts;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author 07rrk
 */
public class MainFrame extends JFrame {
    private NoteManager manager;
    private DefaultListModel<Note> listModel;
    private JList<Note> noteList;
    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField searchField;
    private JButton btnAdd, btnSave, btnDelete, btnImport, btnExport, btnSearch;

    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MainFrame() {
        super("Aplikasi Catatan Harian - PBO2 UTS");
        manager = new NoteManager();
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null); // center
    }

    private void initComponents() {
        // layout utama
        setLayout(new BorderLayout(8,8));
        // kiri - list
        listModel = new DefaultListModel<>();
        noteList = new JList<>(listModel);
        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane leftScroll = new JScrollPane(noteList);
        leftScroll.setPreferredSize(new Dimension(300, 0));
        add(leftScroll, BorderLayout.WEST);

        // kanan - detail
        JPanel right = new JPanel(new BorderLayout(6,6));
        JPanel topPanel = new JPanel(new BorderLayout(4,4));
        titleField = new JTextField();
        topPanel.add(new JLabel("Judul:"), BorderLayout.WEST);
        topPanel.add(titleField, BorderLayout.CENTER);
        right.add(topPanel, BorderLayout.NORTH);

        contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        right.add(contentScroll, BorderLayout.CENTER);

        // bawah tombol
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6,6));
        btnAdd = new JButton("Tambah Baru");
        btnSave = new JButton("Simpan/Update");
        btnDelete = new JButton("Hapus");
        btnImport = new JButton("Impor (.txt)");
        btnExport = new JButton("Ekspor (.txt)");

        btnPanel.add(btnAdd);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
        btnPanel.add(btnImport);
        btnPanel.add(btnExport);

        right.add(btnPanel, BorderLayout.SOUTH);

        add(right, BorderLayout.CENTER);

        // atas - search bar
        JPanel north = new JPanel(new BorderLayout(6,6));
        searchField = new JTextField();
        btnSearch = new JButton("Cari");
        north.add(new JLabel("Cari:"), BorderLayout.WEST);
        north.add(searchField, BorderLayout.CENTER);
        north.add(btnSearch, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        // event listeners
        noteList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Note selected = noteList.getSelectedValue();
                if (selected != null) {
                    titleField.setText(selected.getTitle());
                    contentArea.setText(selected.getContent());
                }
            }
        });

        btnAdd.addActionListener(e -> {
            titleField.setText("");
            contentArea.setText("");
            noteList.clearSelection();
            titleField.requestFocus();
        });

        btnSave.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Judul tidak boleh kosong", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Note selected = noteList.getSelectedValue();
            if (selected == null) {
                // create new
                Note n = new Note(title, content);
                manager.addNote(n);
                listModel.addElement(n);
                noteList.setSelectedValue(n, true);
            } else {
                manager.updateNote(selected, title, content);
                // refresh list model by forcing update (remove & add same object index)
                int idx = noteList.getSelectedIndex();
                listModel.set(idx, selected);
            }
        });

        btnDelete.addActionListener(e -> {
            Note selected = noteList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Pilih catatan yang ingin dihapus", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this, "Hapus catatan \"" + selected.getTitle() + "\"?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                manager.deleteNote(selected);
                listModel.removeElement(selected);
                titleField.setText("");
                contentArea.setText("");
            }
        });

        btnSearch.addActionListener(e -> doSearch());

        searchField.addActionListener(e -> doSearch());

        btnExport.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Simpan Ekspor (.txt)");
            int choice = fc.showSaveDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".txt")) {
                    f = new File(f.getAbsolutePath() + ".txt");
                }
                try {
                    manager.exportToTxt(f);
                    JOptionPane.showMessageDialog(this, "Berhasil diekspor ke " + f.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal ekspor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        btnImport.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Pilih file .txt untuk impor");
            int choice = fc.showOpenDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try {
                    manager.importFromTxt(f);
                    refreshList();
                    JOptionPane.showMessageDialog(this, "Berhasil impor dari " + f.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal impor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // double click untuk membuat salinan cepat atau edit
        noteList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Note sel = noteList.getSelectedValue();
                    if (sel != null) {
                        // buka dialog detail read-only kecil
                        JOptionPane.showMessageDialog(MainFrame.this, sel.getContent(), sel.getTitle() + " - " + sel.getCreatedAt().format(fmt), JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
    }

    private void doSearch() {
        String k = searchField.getText().trim();
        listModel.clear();
        if (k.isEmpty()) {
            for (Note n : manager.getAllNotes()) listModel.addElement(n);
        } else {
            List<Note> res = manager.search(k);
            for (Note n : res) listModel.addElement(n);
        }
    }

    private void refreshList() {
        listModel.clear();
        for (Note n : manager.getAllNotes()) listModel.addElement(n);
    }
}