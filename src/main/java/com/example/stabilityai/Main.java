package com.example.stabilityai;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.Collections;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    private static final int POPULATION_SIZE = 8;
    private static final int MAX_GENERATIONS = 10;
    private static final double CROSSOVER_RATE = 0.9;
    private static final double MUTATION_RATE = 0.1;

    private static GenePool genePool;
    private static int generationCount = 1;

    private static String selectedGender;
    private static String selectedHeight;
    private static String selectedBuild;
    private static String selectedColor;

    private static final Random rand = new Random(System.currentTimeMillis());

    private static long programStartTime; // プログラム全体の実行開始時間

    // ログファイル名 (日時フォルダ内のファイル名)
    private static String logFilename;
    // 画像とログをまとめる出力ディレクトリ (日時フォルダ)
    private static Path generationOutputDir;

    public static void main(String[] args) {
        programStartTime = System.currentTimeMillis();

        // 実行日時を取得
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);

        // フォルダ名を生成し、Pathを設定
        String folderName = "run_" + timestamp;
        generationOutputDir = Paths.get(folderName);

        // CSVファイル名を新しいフォルダ内に設定
        logFilename = generationOutputDir.resolve("evaluation_log.csv").toString();

        // フォルダが存在しなければ作成
        try {
            Files.createDirectories(generationOutputDir);
            System.out.println("Output directory created: " + generationOutputDir);
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + generationOutputDir);
            e.printStackTrace();
            return; // フォルダ作成失敗時は処理を中断
        }

        SwingUtilities.invokeLater(() -> {
            showGenderSelection();
        });
    }

    private static void showGenderSelection() {
        JFrame genderFrame = new JFrame("見たいジャンルを選んでください");
        genderFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        genderFrame.setSize(300, 150);
        genderFrame.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        genderFrame.setLocationRelativeTo(null);

        JLabel instructionLabel = new JLabel("見たいジャンルを選んでください:");
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        genderFrame.add(instructionLabel);

        JRadioButton maleButton = new JRadioButton("mens");
        JRadioButton femaleButton = new JRadioButton("womens");
        maleButton.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(maleButton);
        group.add(femaleButton);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        radioPanel.add(maleButton);
        radioPanel.add(femaleButton);
        genderFrame.add(radioPanel);

        JButton nextButton = new JButton("次へ");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        nextButton.addActionListener(_ -> {
            selectedGender = maleButton.isSelected() ? "mens" : "womens";
            genderFrame.dispose();
            showBodyTypeSelection();
        });
        genderFrame.add(nextButton);

        genderFrame.setVisible(true);
    }

    private static void showBodyTypeSelection() {
        String[] heightOptions = { "低め", "平均的", "高め" };
        String[] buildOptions = { "細身", "標準", "がっしり" };

        JFrame bodyTypeFrame = new JFrame("身長と体格を選んでください");
        bodyTypeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        bodyTypeFrame.setSize(400, 200);
        bodyTypeFrame.setLayout(new GridLayout(3, 1, 10, 10));
        bodyTypeFrame.setLocationRelativeTo(null);

        JPanel heightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        heightPanel.add(new JLabel("身長:"));
        JComboBox<String> heightComboBox = new JComboBox<>(heightOptions);
        heightPanel.add(heightComboBox);
        bodyTypeFrame.add(heightPanel);

        JPanel buildPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buildPanel.add(new JLabel("体格:"));
        JComboBox<String> buildComboBox = new JComboBox<>(buildOptions);
        buildPanel.add(buildComboBox);
        bodyTypeFrame.add(buildPanel);

        JButton nextButton = new JButton("次へ");
        nextButton.addActionListener(_ -> {
            selectedHeight = (String) heightComboBox.getSelectedItem();
            selectedBuild = (String) buildComboBox.getSelectedItem();
            bodyTypeFrame.dispose();
            showColorSelection();
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(nextButton);
        bodyTypeFrame.add(buttonPanel);

        bodyTypeFrame.setVisible(true);
    }

    private static void showColorSelection() {
        String[] colorOptions = {
                "red", "green", "blue", "yellow",
                "magenta", "cyan", "black", "white",
                "gray", "brown", "purple", "orange"
        };

        JFrame colorFrame = new JFrame("色を選んでください");
        colorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        colorFrame.setSize(500, 300);
        colorFrame.setLayout(new BorderLayout(10, 10));
        colorFrame.setLocationRelativeTo(null);

        JLabel instructionLabel = new JLabel("基本色を選んでください:", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        colorFrame.add(instructionLabel, BorderLayout.NORTH);

        JPanel colorButtonPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        colorButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (String colorName : colorOptions) {
            JButton colorButton = new JButton(colorName);
            colorButton.setBackground(getColorFromString(colorName));
            colorButton.setOpaque(true);
            colorButton.setBorderPainted(false);
            colorButton.setPreferredSize(new Dimension(100, 50));
            colorButton.addActionListener(_ -> {
                selectedColor = colorName;
                colorFrame.dispose();
                genePool = new GenePool(POPULATION_SIZE);
                genePool.generateInitialPopulation(selectedColor, selectedGender, selectedHeight, selectedBuild);
                showGeneration();
            });
            colorButtonPanel.add(colorButton);
        }
        colorFrame.add(colorButtonPanel, BorderLayout.CENTER);

        JButton randomColorButton = new JButton("ランダムな色");
        randomColorButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        randomColorButton.addActionListener(_ -> {
            selectedColor = colorOptions[rand.nextInt(colorOptions.length)];
            colorFrame.dispose();
            genePool = new GenePool(POPULATION_SIZE);
            genePool.generateInitialPopulation(selectedColor, selectedGender, selectedHeight, selectedBuild);
            showGeneration();
        });
        JPanel randomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        randomButtonPanel.add(randomColorButton);
        colorFrame.add(randomButtonPanel, BorderLayout.SOUTH);

        colorFrame.setVisible(true);
    }

    private static Color getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red":
                return Color.RED;
            case "green":
                return Color.GREEN;
            case "blue":
                return Color.BLUE;
            case "yellow":
                return Color.YELLOW;
            case "magenta":
                return Color.MAGENTA;
            case "cyan":
                return Color.CYAN;
            case "black":
                return Color.BLACK;
            case "white":
                return Color.WHITE;
            case "gray":
                return Color.GRAY;
            case "brown":
                return new Color(139, 69, 19);
            case "purple":
                return new Color(128, 0, 128);
            case "orange":
                return Color.ORANGE;
            default:
                return Color.LIGHT_GRAY;
        }
    }

    public static int getGenerationCount() {
        return generationCount;
    }

    private static void showGeneration() {
        new FashionEvaluationGUI(genePool.getGenes(), (evaluatedGenes) -> {

            long generationProcessStartTime = System.currentTimeMillis();

            genePool.setGenes(evaluatedGenes);

            // 統計情報の計算と出力、およびファイルへの書き出し
            calculateAndPrintStats(evaluatedGenes, generationCount, generationProcessStartTime);

            // 最良個体のみの保存メソッドを使用
            saveBestGeneImage(evaluatedGenes, generationCount);

            if (generationCount >= MAX_GENERATIONS) {
                long totalTime = System.currentTimeMillis() - programStartTime;
                System.out.println("==============================================");
                System.out.printf(" Total execution time: %.2f seconds\n", totalTime / 1000.0);
                System.out.println(" Maximum generations reached. Program finished.");
                System.out.println(" Output saved in: " + generationOutputDir);
                System.out.println("==============================================");
                System.exit(0);
            } else {
                generationCount++;
                genePool.evolve(evaluatedGenes, CROSSOVER_RATE, MUTATION_RATE, selectedGender, selectedColor,
                        selectedHeight, selectedBuild);
                showGeneration();
            }
        });
    }

    // ----------------------------------------------
    // 世代の統計情報を計算・出力するメソッド
    // ----------------------------------------------
    private static void calculateAndPrintStats(List<FashionGene> genes, int generationCount,
            long generationProcessStartTime) {

        long generationProcessEndTime = System.currentTimeMillis();

        if (genes == null || genes.isEmpty() || genes.stream().allMatch(g -> g.getScore() == 0)) {
            System.out.println("Generation " + generationCount + " Stats: No evaluated data available.");
            return;
        }

        List<Integer> scores = genes.stream()
                .map(FashionGene::getScore)
                .collect(Collectors.toList());

        int minScore = Collections.min(scores);
        int maxScore = Collections.max(scores);

        double averageScore = scores.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // 共通メソッドを使用してランダムな最良個体を選択
        FashionGene bestGene = selectRandomBestGene(genes);

        double processTimeSeconds = (generationProcessEndTime - generationProcessStartTime) / 1000.0;
        double totalTimeSeconds = (generationProcessEndTime - programStartTime) / 1000.0;

        writeStatsToFile(generationCount, averageScore, maxScore, processTimeSeconds, totalTimeSeconds, bestGene);

        // コンソールに出力
        System.out.println("==============================================");
        System.out.println(" Generation " + generationCount + " Evaluation Statistics");
        System.out.println("----------------------------------------------");
        System.out.printf("   Average Score: %.2f\n", averageScore);
        System.out.println("   Minimum Score: " + minScore);
        System.out.println("   Maximum Score: " + maxScore);
        System.out.println("----------------------------------------------");
        System.out.printf("   Process Time: %.3f seconds\n", processTimeSeconds);
        System.out.printf("   Cumulative Time: %.3f seconds\n", totalTimeSeconds);
        System.out.println("==============================================");
    }

    // ----------------------------------------------
    // ファイル書き出しメソッド
    // ----------------------------------------------
    private static void writeStatsToFile(int generationCount, double averageScore, int maxScore,
            double processTimeSeconds, double totalTimeSeconds,
            FashionGene bestGene) {

        Path filePath = Paths.get(logFilename);
        boolean fileExists = Files.exists(filePath);

        try (FileWriter fw = new FileWriter(filePath.toFile(), true);
                PrintWriter pw = new PrintWriter(fw)) {

            // ファイルが新規作成の場合のみ、ヘッダーを書き込む
            if (!fileExists) {
                pw.println("Generation,Average Score,Max Score,Process Time (s),Cumulative Time (s),Best Prompt");
            }

            // データを書き込む
            String prompt = (bestGene != null) ? bestGene.toPrompt() : "N/A";

            // プロンプト内の改行をスペースに、ダブルクォートをエスケープしてCSV形式で出力
            String safePrompt = prompt.replace("\n", " ").replace("\"", "\"\"");

            pw.printf("%d,%.2f,%d,%.3f,%.3f,\"%s\"\n",
                    generationCount,
                    averageScore,
                    maxScore,
                    processTimeSeconds,
                    totalTimeSeconds,
                    safePrompt);

        } catch (IOException e) {
            System.err.println("Failed to write to " + logFilename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------------------------------------
    // 同点の場合はランダムに最良個体を選ぶメソッド
    // ----------------------------------------------
    // Main.java の selectRandomBestGene メソッド内の修正

    private static FashionGene selectRandomBestGene(List<FashionGene> genes) {
        if (genes == null || genes.isEmpty()) {
            return null;
        }

        // 1. 最大スコアを特定
        int maxScore = genes.stream()
                .mapToInt(FashionGene::getScore)
                .max()
                .orElse(0);

        // 2. 最大スコアを持つ個体をすべて抽出 (エリート群)
        List<FashionGene> eliteGenes = genes.stream()
                .filter(g -> g.getScore() == maxScore)
                .collect(Collectors.toList());

        if (!eliteGenes.isEmpty()) {

            // デバッグ出力 (Max Score: 5, Elite Candidate Count: 6 が出ています)

            // System.out.println("DEBUG: Before Shuffle (1st gene prompt hash): " +
            // eliteGenes.get(0).toPrompt().hashCode());

            Collections.shuffle(eliteGenes, rand);

            // ★確認用デバッグ: シャッフル後の先頭要素のハッシュコードを出力
            // System.out.println("DEBUG: After Shuffle (1st gene prompt hash): " +
            // eliteGenes.get(0).toPrompt().hashCode());

            // シャッフル後のリストの最初の要素を返す
            return eliteGenes.get(0);
        }

        return null;
    }

    // ----------------------------------------------
    // 最良個体を保存するメソッド（ランダム選択を適用）
    // ----------------------------------------------
    public static void saveBestGeneImage(List<FashionGene> genes, int generationCount) {
        if (genes == null || genes.isEmpty()) {
            return;
        }

        // 共通メソッドを使用してランダムな最良個体を選択
        FashionGene bestGene = selectRandomBestGene(genes);

        if (bestGene == null || bestGene.getScore() == 0) {
            System.out.println("No evaluated genes found for saving.");
            return;
        }

        String filename = bestGene.getImageFilename();
        if (filename == null || filename.isEmpty()) {
            System.out.println("Best gene has no associated image file.");
            return;
        }

        // サブフォルダに保存するロジックを維持
        Path destDir = generationOutputDir;

        String newFilename = String.format("gen_%d_score_%d_%s",
                generationCount,
                bestGene.getScore(),
                filename);
        Path sourcePath = Paths.get("images", filename);
        Path destPath = destDir.resolve(newFilename);

        try {
            Files.copy(sourcePath, destPath);
            System.out.println("Successfully saved best gene image to: " + destPath);
        } catch (IOException e) {
            System.err.println("Failed to copy image file.");
            e.printStackTrace();
        }
    }
}