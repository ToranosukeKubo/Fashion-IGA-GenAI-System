package com.example.stabilityai;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 選択されたファッション画像を拡大表示するためのダイアログ。
 * 画像のスクロールと拡大・縮小機能を提供。
 */
public class ImageViewerDialog extends JDialog {

    private JLabel imageLabel;
    private JScrollPane scrollPane;
    private Image originalImage;

    private double scale = 1.0;

    public ImageViewerDialog(JFrame parent, String imageFilename) {
        super(parent, "画像ビューア", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 800);
        setLocationRelativeTo(parent);

        try {
            File imageFile = new File("images/" + imageFilename);
            if (imageFile.exists()) {
                originalImage = ImageIO.read(imageFile);
            }
        } catch (IOException e) {
            System.err.println("画像ビューアでの画像読み込みエラー: " + "images/" + imageFilename + " - " + e.getMessage());
            originalImage = null;
        }

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        // scrollPaneが初期化された後にupdateImage()を呼び出すように修正
        if (originalImage != null) {
            updateImage();
        } else {
            imageLabel.setText("画像が見つかりません: " + imageFilename);
        }

        JPanel controlPanel = new JPanel();
        JButton zoomInButton = new JButton("+");
        JButton zoomOutButton = new JButton("-");
        JButton resetButton = new JButton("Reset");
        JButton closeButton = new JButton("閉じる");

        zoomInButton.addActionListener(_ -> {
            scale *= 1.2;
            updateImage();
        });

        zoomOutButton.addActionListener(_ -> {
            scale /= 1.2;
            updateImage();
        });

        resetButton.addActionListener(_ -> {
            scale = 1.0;
            updateImage();
        });

        closeButton.addActionListener(_ -> dispose());

        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(resetButton);
        controlPanel.add(closeButton);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void updateImage() {
        if (originalImage == null)
            return;

        int scaledWidth = (int) (originalImage.getWidth(null) * scale);
        int scaledHeight = (int) (originalImage.getHeight(null) * scale);

        Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));

        imageLabel.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        scrollPane.revalidate();
        scrollPane.repaint();
    }
}
