package com.example.stabilityai;

import javax.swing.*;

import java.awt.*;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.io.File;

import java.io.IOException;

import javax.imageio.ImageIO;

import java.util.List;

import java.util.function.Consumer;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

public class FashionEvaluationGUI extends JFrame {

    private List<FashionGene> genes;

    private Consumer<List<FashionGene>> onEvaluationComplete;

    private JButton nextButton;

    // 評価が完了した個体の数を追跡

    private int evaluatedCount = 0;

    // バックグラウンドでの画像生成用スレッドプール

    private final ExecutorService imageGenerationExecutor = Executors.newFixedThreadPool(8);

    public FashionEvaluationGUI(List<FashionGene> genes, Consumer<List<FashionGene>> onEvaluationComplete) {

        this.genes = genes;

        this.onEvaluationComplete = onEvaluationComplete;

        setTitle("ファッション評価: 第" + Main.getGenerationCount() + "世代");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // フレームサイズを調整

        setSize(1800, 1200);

        setLayout(new BorderLayout(10, 10));

        setLocationRelativeTo(null);

        // タイトルラベルを小さく

        JLabel titleLabel = new JLabel("すべての画像に1から7で評価を付けてください", SwingConstants.CENTER);

        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        add(titleLabel, BorderLayout.NORTH);

        // 画像と評価パネル

        JPanel evaluationPanel = new JPanel(new GridLayout(2, 4, 15, 15));

        evaluationPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (FashionGene gene : genes) {

            gene.setScore(0);

            evaluationPanel.add(createEvaluationItem(gene));

        }

        add(evaluationPanel, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nextButton = new JButton("次へ");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        nextButton.setEnabled(false);
        nextButton.addActionListener(_ -> {
            setVisible(false);
            dispose();
            imageGenerationExecutor.shutdownNow();

            this.onEvaluationComplete.accept(this.genes);
        });
        buttonPanel.add(nextButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override

            public void windowClosing(java.awt.event.WindowEvent windowEvent) {

                imageGenerationExecutor.shutdownNow();

            }

        });

        setVisible(true);

    }

    private JPanel createEvaluationItem(FashionGene gene) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel.setBackground(new Color(250, 250, 250));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);

        // スクロール可能なプロンプト表示エリア

        JTextArea promptArea = new JTextArea();

        promptArea.setText(gene.toPrompt());

        promptArea.setEditable(false);

        promptArea.setLineWrap(true);

        promptArea.setWrapStyleWord(true);

        promptArea.setFont(new Font("SansSerif", Font.PLAIN, 10));

        promptArea.setBackground(new Color(240, 240, 240));

        promptArea.setForeground(Color.DARK_GRAY);

        promptArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(promptArea);

        scrollPane.setPreferredSize(new Dimension(350, 40));

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridx = 0;

        gbc.gridy = 0;

        gbc.weightx = 1.0;

        gbc.weighty = 0.0;

        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(scrollPane, gbc);

        // 画像を表示するラベル

        JLabel imgLabel = new JLabel("Loading...", SwingConstants.CENTER);

        gbc.gridy = 1;

        gbc.weighty = 1.0; // 画像に垂直方向のスペースを最大限に割り当てる

        gbc.fill = GridBagConstraints.BOTH;

        panel.add(imgLabel, gbc);

        // 画像生成と表示

        SwingUtilities.invokeLater(() -> {

            generateAndDisplayImage(gene, imgLabel);

        });

        imgLabel.addMouseListener(new MouseAdapter() {

            @Override

            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2 && e.getComponent() instanceof JLabel) {

                    JLabel sourceLabel = (JLabel) e.getComponent();

                    ImageIcon icon = (ImageIcon) sourceLabel.getIcon();

                    if (icon != null) {

                        showEnlargedImage(icon.getImage());

                    }

                }

            }

        });

        // 7段階評価ボタンを追加

        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        ratingPanel.setOpaque(false);

        for (int i = 1; i <= 7; i++) {

            JButton button = new JButton(String.valueOf(i));

            button.setMargin(new Insets(2, 5, 2, 5));

            button.setFont(new Font("SansSerif", Font.BOLD, 14));

            final int score = i;

            button.addActionListener(_ -> {

                if (gene.getScore() == 0) {

                    evaluatedCount++;

                }

                gene.setScore(score);

                updateButtonStyles(ratingPanel, button);

                checkEvaluationComplete();

            });

            ratingPanel.add(button);

        }

        gbc.gridy = 2;

        gbc.weighty = 0.0;

        gbc.fill = GridBagConstraints.NONE;

        panel.add(ratingPanel, gbc);

        return panel;

    }

    private void checkEvaluationComplete() {

        if (evaluatedCount >= genes.size()) {

            nextButton.setEnabled(true);

        }

    }

    private void updateButtonStyles(JPanel ratingPanel, JButton selectedButton) {

        for (Component comp : ratingPanel.getComponents()) {

            if (comp instanceof JButton) {

                JButton button = (JButton) comp;

                button.setBackground(UIManager.getColor("Button.background"));

                button.setForeground(UIManager.getColor("Button.foreground"));

                button.setFont(new Font("SansSerif", Font.BOLD, 14));

            }

        }

        selectedButton.setBackground(new Color(50, 150, 250));

        selectedButton.setForeground(Color.WHITE);

        selectedButton.setFont(new Font("SansSerif", Font.BOLD, 16));

    }

    // FashionEvaluationGUI.java の generateAndDisplayImage メソッド内の修正

    private void generateAndDisplayImage(FashionGene gene, JLabel imgLabel) {

        // ★追加: プロンプトからシード値を固定（ハッシュ化）

        String promptText = gene.toPrompt();

        int seedValue = promptText.hashCode();

        // シード値が負にならないように調整 (Stability AIのシードは通常、正の整数)

        long positiveSeed = Math.abs((long) seedValue);

        String uniqueIdentifier = promptText + "_" + positiveSeed;

        String filename = "fashion_gen_" + Math.abs(uniqueIdentifier.hashCode()) + ".png";

        // 既存画像の読み込みロジック (変更なし)

        if (gene.getImageFilename() != null && !gene.getImageFilename().isEmpty()) {

            try {

                File imageFile = new File("images/" + gene.getImageFilename());

                if (imageFile.exists()) {

                    Image loadedImage = ImageIO.read(imageFile);

                    if (loadedImage != null) {

                        Image scaledImage = getScaledImage(loadedImage, imgLabel.getWidth(),

                                imgLabel.getHeight());

                        SwingUtilities.invokeLater(() -> {

                            imgLabel.setIcon(new ImageIcon(scaledImage));

                            imgLabel.setText("");

                        });

                        return;

                    }

                }

            } catch (IOException e) {

                System.err.println("既存画像の読み込みエラー: " + "images/" + gene.getImageFilename() + " - " + e.getMessage());

            }

        }

        imgLabel.setText("Generating...");

        imageGenerationExecutor.submit(() -> {

            String outputPath = "images/" + filename;

            File outputFile = new File(outputPath);

            boolean success = StabilityAIImageGenerator.generateImage(promptText, (int) positiveSeed, outputPath);

            SwingUtilities.invokeLater(() -> {

                if (success) {

                    gene.setImageFilename(filename); // 新しい一意なファイル名を設定

                    try {

                        Image loadedImage = ImageIO.read(outputFile);

                        Image scaledImage = getScaledImage(loadedImage, imgLabel.getWidth(),

                                imgLabel.getHeight());

                        imgLabel.setIcon(new ImageIcon(scaledImage));

                        imgLabel.setText("");

                    } catch (IOException e) {

                        imgLabel.setText("Gen Success, Load Fail");

                        System.err.println("生成後画像の読み込みエラー: " + e.getMessage());

                    }

                } else {

                    imgLabel.setText("Gen Failed");

                }

            });

        });

    }

    private Image getScaledImage(Image srcImg, int maxWidth, int maxHeight) {

        int originalWidth = srcImg.getWidth(null);

        int originalHeight = srcImg.getHeight(null);

        if (originalWidth <= 0 || originalHeight <= 0) {

            return srcImg;

        }

        double scaleX = (double) maxWidth / originalWidth;

        double scaleY = (double) maxHeight / originalHeight;

        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) (originalWidth * scale);

        int scaledHeight = (int) (originalHeight * scale);

        return srcImg.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

    }

    private void showEnlargedImage(Image originalImage) {

        JFrame enlargedFrame = new JFrame("画像拡大");

        enlargedFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        int originalWidth = originalImage.getWidth(null);

        int originalHeight = originalImage.getHeight(null);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int maxWidth = (int) (screenSize.width * 0.9);

        int maxHeight = (int) (screenSize.height * 0.9);

        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);

        int scaledWidth = (int) (originalWidth * scale);

        int scaledHeight = (int) (originalHeight * scale);

        Image resizedImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

        JLabel enlargedLabel = new JLabel(new ImageIcon(resizedImage));

        JScrollPane scrollPane = new JScrollPane(enlargedLabel);

        scrollPane.setPreferredSize(new Dimension(scaledWidth, scaledHeight));

        enlargedFrame.add(scrollPane, BorderLayout.CENTER);

        enlargedFrame.pack();

        enlargedFrame.setLocationRelativeTo(null);

        enlargedFrame.setVisible(true);

    }

}