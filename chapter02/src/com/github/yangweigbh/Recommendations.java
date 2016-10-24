package com.github.yangweigbh;

import com.github.yangweigbh.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yangwei on 2016/10/23.
 */
public class Recommendations {
    static Map<String, Map<String, Float>> sPrefs = new HashMap<>();
    static {
        generateUserPrefs(sPrefs, "Lisa Rose", "Lady in the Water", 2.5f);
        generateUserPrefs(sPrefs, "Lisa Rose", "Snakes on a Plane", 3.5f);
        generateUserPrefs(sPrefs, "Lisa Rose", "Just My Luck", 3.0f);
        generateUserPrefs(sPrefs, "Lisa Rose", "Superman Returns", 3.5f);
        generateUserPrefs(sPrefs, "Lisa Rose", "You, Me and Dupree", 2.5f);
        generateUserPrefs(sPrefs, "Lisa Rose", "The Night Listener", 3.0f);

        generateUserPrefs(sPrefs, "Gene Seymour", "Lady in the Water", 3.0f);
        generateUserPrefs(sPrefs, "Gene Seymour", "Snakes on a Plane", 3.5f);
        generateUserPrefs(sPrefs, "Gene Seymour", "Just My Luck", 1.5f);
        generateUserPrefs(sPrefs, "Gene Seymour", "Superman Returns", 5.0f);
        generateUserPrefs(sPrefs, "Gene Seymour", "You, Me and Dupree", 3.5f);
        generateUserPrefs(sPrefs, "Gene Seymour", "The Night Listener", 3.0f);

        generateUserPrefs(sPrefs, "Michael Phillips", "Lady in the Water", 2.5f);
        generateUserPrefs(sPrefs, "Michael Phillips", "Snakes on a Plane", 3.0f);
        generateUserPrefs(sPrefs, "Michael Phillips", "Superman Returns", 3.5f);
        generateUserPrefs(sPrefs, "Michael Phillips", "The Night Listener", 4.0f);

        generateUserPrefs(sPrefs, "Claudia Puig", "Snakes on a Plane", 3.5f);
        generateUserPrefs(sPrefs, "Claudia Puig", "Just My Luck", 3.0f);
        generateUserPrefs(sPrefs, "Claudia Puig", "The Night Listener", 4.5f);
        generateUserPrefs(sPrefs, "Claudia Puig", "Superman Returns", 4.0f);
        generateUserPrefs(sPrefs, "Claudia Puig", "You, Me and Dupree", 2.5f);

        generateUserPrefs(sPrefs, "Mick LaSalle", "Lady in the Water", 3.0f);
        generateUserPrefs(sPrefs, "Mick LaSalle", "Snakes on a Plane", 4.0f);
        generateUserPrefs(sPrefs, "Mick LaSalle", "Just My Luck", 2.0f);
        generateUserPrefs(sPrefs, "Mick LaSalle", "Superman Returns", 3.0f);
        generateUserPrefs(sPrefs, "Mick LaSalle", "The Night Listener", 3.0f);
        generateUserPrefs(sPrefs, "Mick LaSalle", "You, Me and Dupree", 2.0f);

        generateUserPrefs(sPrefs, "Jack Matthews", "Lady in the Water", 3.0f);
        generateUserPrefs(sPrefs, "Jack Matthews", "Snakes on a Plane", 4.0f);
        generateUserPrefs(sPrefs, "Jack Matthews", "Superman Returns", 5.0f);
        generateUserPrefs(sPrefs, "Jack Matthews", "The Night Listener", 3.0f);
        generateUserPrefs(sPrefs, "Jack Matthews", "You, Me and Dupree", 3.5f);

        generateUserPrefs(sPrefs, "Toby", "Snakes on a Plane", 4.5f);
        generateUserPrefs(sPrefs, "Toby", "You, Me and Dupree", 1.0f);
        generateUserPrefs(sPrefs, "Toby", "Superman Returns", 4.0f);
    }

    private static void generateUserPrefs(Map<String, Map<String, Float>> prefs, String user, String movie, float score) {
        if (prefs.get(user) == null) {
            prefs.put(user, new HashMap<>());
        }
        prefs.get(user).put(movie, score);
    }

    public static float similarityByEuclideanDistance(Map<String, Map<String, Float>> prefs, String user1, String user2) {
        float sumOfSquares = 0;
        Map<String, Float> prefOfUser1 = prefs.get(user1);
        Map<String, Float> prefOfUser2 = prefs.get(user2);
        Set<String> itemOfUser2 = prefOfUser2.keySet();
        boolean intersect = false;
        for(Map.Entry<String, Float> pref: prefOfUser1.entrySet()) {
            if (itemOfUser2.contains(pref.getKey())) {
                intersect = true;
                sumOfSquares += Math.pow(pref.getValue() - prefOfUser2.get(pref.getKey()), 2);
            }
        }
        if (!intersect) return 0;
        return (float) (1 / (1 + Math.sqrt(sumOfSquares)));
    }

    public static float similarityByPearsonCorrelation(Map<String, Map<String, Float>> prefs, String user1, String user2) {
        Map<String, Float> prefOfUser1 = prefs.get(user1);
        Map<String, Float> prefOfUser2 = prefs.get(user2);

        Set<String> itemsOfUser1 = prefOfUser1.keySet();
        Set<String> itemsOfUser2 = prefOfUser2.keySet();
        Set<String> intersection = new HashSet<>(itemsOfUser1);
        intersection.retainAll(itemsOfUser2);

        int n = intersection.size();
        if (n == 0) return 0;

        float sum1 = 0;
        float sum2 = 0;
        float sum1Pow2 = 0;
        float sum2Pow2 = 0;
        float sumOfProduct = 0;

        sum1 = (float) intersection.stream().mapToDouble(item -> prefOfUser1.get(item)).sum();
        sum2 = (float) intersection.stream().mapToDouble(item -> prefOfUser2.get(item)).sum();

        sum1Pow2 = (float) intersection.stream().mapToDouble(item -> Math.pow(prefOfUser1.get(item), 2)).sum();
        sum2Pow2 = (float) intersection.stream().mapToDouble(item -> Math.pow(prefOfUser2.get(item), 2)).sum();

        sumOfProduct = (float) intersection.stream().mapToDouble(item -> prefOfUser1.get(item)*prefOfUser2.get(item)).sum();

        float num = sumOfProduct - (sum1*sum2/n);
        float den = (float) Math.sqrt((sum1Pow2-Math.pow(sum1, 2)/n)*(sum2Pow2-Math.pow(sum2, 2)/n));


        if (den == 0) return 0;

        return num/den;
    }

    public static List<Pair<Float, String>> topMatch(Map<String, Map<String, Float>> prefs, String user, Strategy strategy, int n) {
        if (strategy == null) {
            strategy = new PearsonStrategy();
        }

        final Strategy finalStrategy = strategy;
        List<Pair<Float, String>> result = prefs.keySet().stream()
                                                        .filter(item -> !user.equals(item))
                                                        .map(item-> finalStrategy.apply(prefs, user, item))
                                                        .collect(Collectors.toList());

        Collections.sort(result, (t1, t2) -> -Float.compare(t1.first, t2.first));

        if (n > result.size()) return result;
        return result.subList(0, n);
    }

    public static List<Pair<Float, String>> getRecommendations(Map<String, Map<String, Float>> prefs, String user, Strategy strategy) {
        if (strategy == null) {
            strategy = new PearsonStrategy();
        }

        Map<String, Float> totals = new HashMap<>();
        Map<String, Float> simSums = new HashMap<>();

        for (String otherUser: prefs.keySet()) {
            if (!user.equals(otherUser)) {
                float similarity = strategy.apply(prefs, user, otherUser).first;
                if (similarity < 0) continue;
                for(Map.Entry<String, Float> entry: prefs.get(otherUser).entrySet()) {
                    String item = entry.getKey();
                    if (!prefs.get(user).keySet().contains(item) || prefs.get(user).get(item) == 0) {
                        totals.put(item, totals.getOrDefault(item, 0f) + prefs.get(otherUser).get(item)*similarity);

                        simSums.put(item, simSums.getOrDefault(item, 0f) + similarity);
                    }
                }
            }
        }

        List<Pair<Float, String>> result = new ArrayList<>();
        for (Map.Entry<String, Float> entry: totals.entrySet()) {
            result.add(Pair.create(entry.getValue()/simSums.get(entry.getKey()), entry.getKey()));
        }

        Collections.sort(result, (t1, t2) -> -Float.compare(t1.first, t2.first));
        return result;
    }

    public static Map<String, Map<String, Float>> transformPrefs(Map<String, Map<String, Float>> prefs)  {
        Map<String, Map<String, Float>> result = new HashMap<>();
        for (String person: prefs.keySet()) {
            for (Map.Entry<String, Float> itemEntry: prefs.get(person).entrySet()) {
                Map<String, Float> temp = result.getOrDefault(itemEntry.getKey(), new HashMap<>());
                temp.put(person, itemEntry.getValue());
                result.put(itemEntry.getKey(), temp);
            }
        }
        return result;
    }

    public static Map<String, List<Pair<Float, String>>> calculateSimilarItems(Map<String, Map<String, Float>> prefs, int n) {
        Map<String, List<Pair<Float, String>>> result = new HashMap<>();
        Map<String, Map<String, Float>> itemPrefs = transformPrefs(prefs);

        int count = 0;
        for(Map.Entry<String, Map<String, Float>> entry: itemPrefs.entrySet()) {
            count++;
            if (count % 100 == 0) System.out.println(String.format(Locale.US, "%d / %d", count, itemPrefs.size()));
            result.put(entry.getKey(), topMatch(itemPrefs, entry.getKey(), new EuclideanStrategy(), n));
        }
        return result;
    }

    public static List<Pair<Float, String>> getRecommendedItems(Map<String, Map<String, Float>> prefs, Map<String, List<Pair<Float, String>>> itemMatchs, String user) {
        Map<String, Float> userRatings = prefs.get(user);

        Map<String, Float> scores = new HashMap<>();
        Map<String, Float> simSums = new HashMap<>();

        for (Map.Entry<String, Float> itemEntry: userRatings.entrySet()) {
            for (Pair<Float, String> otherItemEntry: itemMatchs.get(itemEntry.getKey())) {
                if (userRatings.keySet().contains(otherItemEntry.second)) {
                    continue;
                }

                scores.put(otherItemEntry.second, scores.getOrDefault(otherItemEntry.second, 0f) + userRatings.get(itemEntry.getKey())*otherItemEntry.first);

                simSums.put(otherItemEntry.second, simSums.getOrDefault(otherItemEntry.second, 0f) + otherItemEntry.first);
            }
        }

        List<Pair<Float, String>> result = new ArrayList<>();
        for (Map.Entry<String, Float> entry: scores.entrySet()) {
            result.add(Pair.create(entry.getValue()/simSums.get(entry.getKey()), entry.getKey()));
        }

        Collections.sort(result, (t1, t2) -> -Float.compare(t1.first, t2.first));
        return result;
    }

    public static void main(String[] args) {
        System.out.println("eulidean " + similarityByEuclideanDistance(sPrefs, "Lisa Rose", "Gene Seymour"));

        System.out.println("pearson " + similarityByPearsonCorrelation(sPrefs, "Lisa Rose", "Gene Seymour"));

        System.out.println("topmatch " + Arrays.toString(topMatch(sPrefs, "Toby", null, 3).toArray()));

        System.out.println("recommendations " + Arrays.toString(getRecommendations(sPrefs, "Toby", null).toArray()));

        System.out.println("product topmatch " + Arrays.toString(topMatch(transformPrefs(sPrefs), "Superman Returns", null, 3).toArray()));

        System.out.println("product recommendations " + Arrays.toString(getRecommendations(transformPrefs(sPrefs), "Just My Luck", null).toArray()));

        System.out.println("similar products dataset======");

        Map<String, List<Pair<Float, String>>> productSimilarDataSet = calculateSimilarItems(sPrefs, 10);
        for (Map.Entry<String, List<Pair<Float, String>>> item: productSimilarDataSet.entrySet()) {
            System.out.println(String.format(Locale.US, "key: %s value: %s", item.getKey(), Arrays.toString(item.getValue().toArray())));
        }

        System.out.println("product recommendations " + Arrays.toString(getRecommendedItems(sPrefs, productSimilarDataSet, "Toby").toArray()));
    }
}
