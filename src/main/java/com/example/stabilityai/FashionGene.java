package com.example.stabilityai;

import java.util.Random;

public class FashionGene {

    private String baseColorName;

    private String imageFilename;

    private int score;

    private String gender;

    private String height;

    private String build;

    // トップス

    private String topPattern;

    private String topStyle;

    private String topMaterial;

    // ボトムス

    private String bottomPattern;

    private String bottomStyle;

    private String bottomMaterial;

    // 靴

    private String shoeStyle;

    private String shoeMaterial;

    // 定数定義

    private static final String[] TOP_PATTERNS = { "striped", "dotted", "plain", "checkered" };

    private static final String[] TOP_MATERIALS = { "cotton", "silk", "denim", "leather" };

    private static final String[] BOTTOM_PATTERNS = { "plain", "plaid", "floral", "striped" };

    private static final String[] BOTTOM_MATERIALS = { "denim", "cotton", "polyester", "leather" };

    private static final String[] SHOE_MATERIALS = { "leather", "canvas", "mesh", "suede" };

    // 男性用

    private static final String[] TOP_STYLES_MALE = { "shirt", "hoodie", "jacket" };

    private static final String[] BOTTOM_STYLES_MALE = { "jeans", "trousers", "shorts" };

    private static final String[] SHOE_STYLES_MALE = { "sneakers", "boots", "loafers" };

    // 女性用

    private static final String[] TOP_STYLES_FEMALE = { "blouse", "crop top" };

    private static final String[] BOTTOM_STYLES_FEMALE = { "skirt", "culottes", "leggings" };

    private static final String[] SHOE_STYLES_FEMALE = { "heels", "flats", "ankle boots" };

    // 身長と体格の定義

    private static final String[] HEIGHTS = { "tall", "average height", "short" };

    private static final String[] BUILDS = { "slim", "muscular", "curvy", "stocky" };

    private static final String[] COLOR_NAMES = {

            "red", "green", "blue", "yellow", "magenta", "cyan", "black", "white", "gray", "brown", "purple", "orange"

    };

    private static final Random rand = new Random();

    public FashionGene() {

        this.baseColorName = COLOR_NAMES[rand.nextInt(COLOR_NAMES.length)];

        this.gender = rand.nextBoolean() ? "mens" : "womens";

        this.height = HEIGHTS[rand.nextInt(HEIGHTS.length)];

        this.build = BUILDS[rand.nextInt(BUILDS.length)];

        randomize();

    }

    public FashionGene(String initialColorName, String initialGender, String initialHeight, String initialBuild) {

        this.baseColorName = initialColorName;

        this.gender = initialGender;

        this.height = initialHeight;

        this.build = initialBuild;

        randomize();

    }

    public FashionGene(String baseColorName, String gender, String topPattern, String topStyle, String topMaterial,

            String bottomPattern, String bottomStyle, String bottomMaterial,

            String shoeStyle, String shoeMaterial, String height, String build) {

        this.baseColorName = baseColorName;

        this.gender = gender;

        this.topPattern = topPattern;

        this.topStyle = topStyle;

        this.topMaterial = topMaterial;

        this.bottomPattern = bottomPattern;

        this.bottomStyle = bottomStyle;

        this.bottomMaterial = bottomMaterial;

        this.shoeStyle = shoeStyle;

        this.shoeMaterial = shoeMaterial;

        this.height = height;

        this.build = build;

    }

    public FashionGene(FashionGene source) {

        this.baseColorName = source.baseColorName;

        this.imageFilename = source.imageFilename;

        this.score = source.score;

        this.gender = source.gender;

        this.height = source.height;

        this.build = source.build;

        this.topPattern = source.topPattern;

        this.topStyle = source.topStyle;

        this.topMaterial = source.topMaterial;

        this.bottomPattern = source.bottomPattern;

        this.bottomStyle = source.bottomStyle;

        this.bottomMaterial = source.bottomMaterial;

        this.shoeStyle = source.shoeStyle;

        this.shoeMaterial = source.shoeMaterial;

    }

    public void randomize() {

        topPattern = TOP_PATTERNS[rand.nextInt(TOP_PATTERNS.length)];

        topMaterial = TOP_MATERIALS[rand.nextInt(TOP_MATERIALS.length)];

        bottomPattern = BOTTOM_PATTERNS[rand.nextInt(BOTTOM_PATTERNS.length)];

        bottomMaterial = BOTTOM_MATERIALS[rand.nextInt(BOTTOM_MATERIALS.length)];

        if ("womens".equals(gender)) {

            topStyle = TOP_STYLES_FEMALE[rand.nextInt(TOP_STYLES_FEMALE.length)];

            bottomStyle = BOTTOM_STYLES_FEMALE[rand.nextInt(BOTTOM_STYLES_FEMALE.length)];

            shoeStyle = SHOE_STYLES_FEMALE[rand.nextInt(SHOE_STYLES_FEMALE.length)];

        } else {

            topStyle = TOP_STYLES_MALE[rand.nextInt(TOP_STYLES_MALE.length)];

            bottomStyle = BOTTOM_STYLES_MALE[rand.nextInt(BOTTOM_STYLES_MALE.length)];

            shoeStyle = SHOE_STYLES_MALE[rand.nextInt(SHOE_STYLES_MALE.length)];

        }

        shoeMaterial = SHOE_MATERIALS[rand.nextInt(SHOE_MATERIALS.length)];

    }

    /**
     * 
     * 交叉（Crossover）: 2つの親から新しい子を生成。
     * 
     * 色、性別、身長、体格は親1からそのまま引き継ぐ。
     * 
     * 他のファッション要素は親からランダムに選択。
     * 
     */

    public static FashionGene crossover(FashionGene parent1, FashionGene parent2) {

        FashionGene child = new FashionGene();

        // ユーザーの選択を維持するため、親1から値を引き継ぐ

        child.setBaseColorName(parent1.baseColorName);

        child.setGender(parent1.gender);

        child.setHeight(parent1.height);

        child.setBuild(parent1.build);

        // 他のファッション要素は引き続き交叉

        child.setTopPattern(rand.nextBoolean() ? parent1.topPattern : parent2.topPattern);

        child.setTopStyle(rand.nextBoolean() ? parent1.topStyle : parent2.topStyle);

        child.setTopMaterial(rand.nextBoolean() ? parent1.topMaterial : parent2.topMaterial);

        child.setBottomPattern(rand.nextBoolean() ? parent1.bottomPattern : parent2.bottomPattern);

        child.setBottomStyle(rand.nextBoolean() ? parent1.bottomStyle : parent2.bottomStyle);

        child.setBottomMaterial(rand.nextBoolean() ? parent1.bottomMaterial : parent2.bottomMaterial);

        child.setShoeStyle(rand.nextBoolean() ? parent1.shoeStyle : parent2.shoeStyle);

        child.setShoeMaterial(rand.nextBoolean() ? parent1.shoeMaterial : parent2.shoeMaterial);

        return child;

    }

    /**
     * 
     * 突然変異（Mutation）: 遺伝子の一部をランダムに変化させる。
     * 
     * 色、性別、身長、体格は突然変異の対象外。
     * 
     */

    public void mutate(double mutationRate) {

        // 色、性別、身長、体格は固定するため、突然変異させない

        // if (rand.nextDouble() < mutationRate)

        // baseColorName = COLOR_NAMES[rand.nextInt(COLOR_NAMES.length)];

        // if (rand.nextDouble() < mutationRate)

        // gender = rand.nextBoolean() ? "mens" : "womens";

        // if (rand.nextDouble() < mutationRate)

        // height = HEIGHTS[rand.nextInt(HEIGHTS.length)];

        // if (rand.nextDouble() < mutationRate)

        // build = BUILDS[rand.nextInt(BUILDS.length)];

        if (rand.nextDouble() < mutationRate)

            topPattern = TOP_PATTERNS[rand.nextInt(TOP_PATTERNS.length)];

        if (rand.nextDouble() < mutationRate)

            topStyle = getTopStylesForGender(this.gender)[rand.nextInt(getTopStylesForGender(this.gender).length)];

        if (rand.nextDouble() < mutationRate)

            topMaterial = TOP_MATERIALS[rand.nextInt(TOP_MATERIALS.length)];

        if (rand.nextDouble() < mutationRate)

            bottomPattern = BOTTOM_PATTERNS[rand.nextInt(BOTTOM_PATTERNS.length)];

        if (rand.nextDouble() < mutationRate)

            bottomStyle = getBottomStylesForGender(this.gender)[rand

                    .nextInt(getBottomStylesForGender(this.gender).length)];

        if (rand.nextDouble() < mutationRate)

            bottomMaterial = BOTTOM_MATERIALS[rand.nextInt(BOTTOM_MATERIALS.length)];

        if (rand.nextDouble() < mutationRate)

            shoeStyle = getShoeStylesForGender(this.gender)[rand.nextInt(getShoeStylesForGender(this.gender).length)];

        if (rand.nextDouble() < mutationRate)

            shoeMaterial = SHOE_MATERIALS[rand.nextInt(SHOE_MATERIALS.length)];

    }

    private static String[] getTopStylesForGender(String gender) {

        return "womens".equals(gender) ? TOP_STYLES_FEMALE : TOP_STYLES_MALE;

    }

    private static String[] getBottomStylesForGender(String gender) {

        return "womens".equals(gender) ? BOTTOM_STYLES_FEMALE : BOTTOM_STYLES_MALE;

    }

    private static String[] getShoeStylesForGender(String gender) {

        return "womens".equals(gender) ? SHOE_STYLES_FEMALE : SHOE_STYLES_MALE;

    }

    /**
     * 
     * 日本語の身長を英語に変換する
     * 
     */

    private String convertHeightToEnglish(String japaneseHeight) {

        switch (japaneseHeight) {

            case "低め":

                return "short";

            case "平均的":

                return "average height";

            case "高め":

                return "tall";

            default:

                return japaneseHeight;

        }

    }

    /**
     * 
     * 日本語の体格を英語に変換する
     * 
     */

    private String convertBuildToEnglish(String japaneseBuild) {

        switch (japaneseBuild) {

            case "細身":

                return "slim";

            case "標準":

                return "normal build";

            case "がっしり":

                return "muscular";

            default:

                return japaneseBuild;

        }

    }

    public String toPrompt() {

        String englishHeight = convertHeightToEnglish(this.height);

        String englishBuild = convertBuildToEnglish(this.build);

        return String.format(

                "A %s fashion model, %s, %s, full body shot, wearing a %s %s %s and %s %s %s with %s %s. Outfit color: %s. Studio photo, plain white background.",

                gender.toLowerCase(),

                englishHeight,

                englishBuild,

                topMaterial, topPattern, topStyle,

                bottomMaterial, bottomPattern, bottomStyle,

                shoeMaterial, shoeStyle,

                baseColorName);

    }

    // ゲッター・セッター

    public void setBaseColorName(String colorName) {

        this.baseColorName = colorName;

    }

    public String getBaseColorName() {

        return baseColorName;

    }

    public void setImageFilename(String filename) {

        this.imageFilename = filename;

    }

    public String getImageFilename() {

        return imageFilename;

    }

    public void setScore(int score) {

        this.score = score;

    }

    public int getScore() {

        return score;

    }

    public void setGender(String gender) {

        this.gender = gender;

    }

    public String getGender() {

        return gender;

    }

    public String getTopPattern() {

        return topPattern;

    }

    public void setTopPattern(String topPattern) {

        this.topPattern = topPattern;

    }

    public String getTopStyle() {

        return topStyle;

    }

    public void setTopStyle(String topStyle) {

        this.topStyle = topStyle;

    }

    public String getTopMaterial() {

        return topMaterial;

    }

    public void setTopMaterial(String topMaterial) {

        this.topMaterial = topMaterial;

    }

    public String getBottomPattern() {

        return bottomPattern;

    }

    public void setBottomPattern(String bottomPattern) {

        this.bottomPattern = bottomPattern;

    }

    public String getBottomStyle() {

        return bottomStyle;

    }

    public void setBottomStyle(String bottomStyle) {

        this.bottomStyle = bottomStyle;

    }

    public String getBottomMaterial() {

        return bottomMaterial;

    }

    public void setBottomMaterial(String bottomMaterial) {

        this.bottomMaterial = bottomMaterial;

    }

    public String getShoeStyle() {

        return shoeStyle;

    }

    public void setShoeStyle(String shoeStyle) {

        this.shoeStyle = shoeStyle;

    }

    public String getShoeMaterial() {

        return shoeMaterial;

    }

    public void setShoeMaterial(String shoeMaterial) {

        this.shoeMaterial = shoeMaterial;

    }

    public String getHeight() {

        return height;

    }

    public void setHeight(String height) {

        this.height = height;

    }

    public String getBuild() {

        return build;

    }

    public void setBuild(String build) {

        this.build = build;

    }

}