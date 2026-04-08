package com.example.stabilityai;

import java.util.ArrayList;

import java.util.Collections;

import java.util.List;

import java.util.Random;

import java.util.stream.Collectors;

public class GenePool {

    private List<FashionGene> genes;

    private int populationSize;

    private static final Random rand = new Random(System.currentTimeMillis());

    private static final int ELITE_COUNT = 1; // 各世代で最もスコアの高い個体を1体引き継ぐ

    public GenePool(int populationSize) {

        this.populationSize = populationSize;

        this.genes = new ArrayList<>();

    }

    /**
     * 
     * 初期集団を生成。
     * 
     * * @param initialColor 初期集団の基本色
     * 
     * * @param initialGender 初期集団の性別
     *
     * 
     * 
     * @param initialHeight 初期集団の身長
     * 
     * @param initialBuild  初期集団の体格
     * 
     */

    public void generateInitialPopulation(String initialColor, String initialGender, String initialHeight,

            String initialBuild) {

        for (int i = 0; i < populationSize; i++) {

            FashionGene newGene = new FashionGene(initialColor, initialGender, initialHeight, initialBuild);

            genes.add(newGene);

        }

    }

    public List<FashionGene> getGenes() {

        return genes;

    }

    public void setGenes(List<FashionGene> genes) {

        this.genes = genes;

    }

    /**
     * 
     * 遺伝的アルゴリズムの進化ステップを実行。
     * 
     * 評価された個体から次世代の個体を生成（エリート選択、交叉、突然変異を含む）。
     *
     * 
     * 
     * @param evaluatedGenes ユーザーによって評価された現在の世代の個体リスト
     * 
     * @param crossoverRate  交叉の確率 (0.0 - 1.0)
     * 
     * @param mutationRate   突然変異の確率 (0.0 - 1.0)
     * 
     * @param selectedGender ユーザーが選択した性別（個体生成時の考慮用）
     * 
     * @param selectedColor  ユーザーが選択した基本色（個体生成時の考慮用）
     * 
     * @param selectedHeight ユーザーが選択した身長
     * 
     * @param selectedBuild  ユーザーが選択した体格
     * 
     */

    public void evolve(List<FashionGene> evaluatedGenes, double crossoverRate, double mutationRate,

            String selectedGender, String selectedColor, String selectedHeight, String selectedBuild) {

        // evaluatedGenes.sort(Comparator.comparingInt(FashionGene::getScore).reversed());

        List<FashionGene> newPopulation = new ArrayList<>();

        for (int i = 0; i < ELITE_COUNT; i++) {

            FashionGene elite = selectRandomElite(evaluatedGenes);

            if (elite != null) {

                // エリートを複製して次世代に追加

                newPopulation.add(new FashionGene(elite));

            }

        }

        // 残りの個体を交叉と突然変異で生成

        while (newPopulation.size() < populationSize) {

            // 親の選択 (スコアに基づく重み付きランダム選択)

            FashionGene parent1 = selectParent(evaluatedGenes);

            FashionGene parent2 = selectParent(evaluatedGenes);

            FashionGene child;

            // 交叉確率に基づいた子個体生成ロジック

            if (rand.nextDouble() < crossoverRate) {

                // 交叉を実行

                child = FashionGene.crossover(parent1, parent2);

            } else {

                // 交叉を行わず、親個体のどちらかをそのままコピー

                child = rand.nextBoolean() ? new FashionGene(parent1) : new FashionGene(parent2);

            }

            // 突然変異

            child.mutate(mutationRate);

            // ユーザーの選択した性別、色、身長、体格を強制的に適用

            child.setGender(selectedGender);

            child.setBaseColorName(selectedColor);

            child.setHeight(selectedHeight);

            child.setBuild(selectedBuild);

            newPopulation.add(child);

        }

        this.genes = newPopulation; // 新しい世代に置き換える

    }

    private FashionGene selectRandomElite(List<FashionGene> genes) {

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

        // 3. エリート群の中からランダムに1つを選ぶ

        if (!eliteGenes.isEmpty()) {

            // リストをシャッフルしてから最初の要素を選ぶ

            Collections.shuffle(eliteGenes, rand);

            return eliteGenes.get(0);

        }

        return null;

    }

    /**
     * 
     * スコアに基づいて親個体を重み付きランダム選択。
     * 
     * スコアが高いほど選択される確率が高くなる。
     * 
     * * @param population 選択対象の個体リスト
     * 
     * * @return 選択された親個体
     * 
     */

    private FashionGene selectParent(List<FashionGene> population) {

        // 全体のスコア合計を計算

        int totalScore = population.stream().mapToInt(FashionGene::getScore).sum();

        if (totalScore == 0) { // 全てのスコアが0の場合（初期状態など）はランダムに選択

            return population.get(rand.nextInt(population.size()));

        }

        // ランダムな閾値を生成

        int randomThreshold = rand.nextInt(totalScore);

        // ルーレット選択のように親を選ぶ

        int currentScoreSum = 0;

        for (FashionGene gene : population) {

            currentScoreSum += gene.getScore();

            if (currentScoreSum >= randomThreshold) {

                return gene;

            }

        }

        // ここに到達することは稀だが、念のため最後の個体を返す

        return population.get(population.size() - 1);

    }

}