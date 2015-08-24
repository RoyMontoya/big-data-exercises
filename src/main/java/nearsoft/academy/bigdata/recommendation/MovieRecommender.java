package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.HashBiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class MovieRecommender {

    private int totalReviews;
    private int totalProducts;
    private int totalUsers;
    private File outFile = new File("/home/roy/Documents/output.txt");
    private File filteredOutput = new File("/home/roy/Documents/filteredOutput.csv");

    private HashBiMap<String, Integer> productsNumberID= HashBiMap.create();
    private HashBiMap<String, Integer> userNumberID =HashBiMap.create();
    private UserBasedRecommender recommender;
    private List<RecommendedItem> recommendationItems;

    public MovieRecommender(String textPath) throws IOException, TasteException {
        //TODO finish constructor
        File dataFile = new File(textPath);
        filterFile(dataFile);
        createHashMaps();
        DataModel model = new FileDataModel(new File(filteredOutput.getPath()));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

    }


    public int getTotalReviews() throws IOException {
        if (totalReviews != 0) {
            return totalReviews;
        } else {
            totalReviews = readFileForReviews();
            return totalReviews;
        }
    }

    public void setTotaReviews(int totaReviews) {
        this.totalReviews = totaReviews;
    }

    public int getTotalProducts() throws IOException {
        if (totalProducts == 0) {

            totalProducts = productsNumberID.size();
        }
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalUsers() {
        if (totalUsers == 0) {
            totalUsers = userNumberID.size();
        }
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int readFileForReviews() throws IOException {

        LineIterator it = FileUtils.lineIterator(outFile, "UTF-8");
        int counter = 0;
        while (it.hasNext()) {
            counter++;
            it.nextLine();
        }
        LineIterator.closeQuietly(it);
        return counter;

    }

    public int readFilteredFileForReviews() throws IOException {

        LineIterator it = FileUtils.lineIterator(filteredOutput, "UTF-8");
        int counter = 0;
        while (it.hasNext()) {
            counter++;
            it.nextLine();
        }
        LineIterator.closeQuietly(it);
        return counter;
    }

    public void createHashMaps() throws IOException {

        LineIterator it = FileUtils.lineIterator(outFile, "UTF-8");
        int product = 0;
        int user = 0;
        while (it.hasNext()) {
            String line = it.nextLine();
            String productId = line.substring(0, 10);
            int secondComa = line.indexOf(',', 12);
            String userId = line.substring(12, secondComa);
            String score = line.substring(secondComa + 2);


            if ((productsNumberID.get(productId)==null) ){
                productsNumberID.put(productId, product);
                product++;
            }
            if (userNumberID.get(userId) == null) {
                userNumberID.put(userId, user);
                user++;
            }

            fileStringsToInt(productId, userId, score);

        }
        LineIterator.closeQuietly(it);
    }


    public void fileStringsToInt(String productId, String userId, String score) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(userNumberID.get(userId))
                .append(",")
                .append(productsNumberID.get(productId))
                .append(",")
                .append(score)
                .append('\n');
        FileUtils.writeStringToFile(filteredOutput, sb.toString(), true);
    }


    public void filterFile(File dataFile) throws IOException {
        LineIterator it = FileUtils.lineIterator(dataFile, "UTF-8");
        try {
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("product/productId")) {
                    String productId = line.substring(19) + ", ";
                    FileUtils.writeStringToFile(outFile, productId, true);
                }
                if (line.startsWith("review/userId")) {
                    String userId = line.substring(15) + ", ";
                    FileUtils.writeStringToFile(outFile, userId, true);
                }
                if (line.startsWith("review/score")) {
                    String score = line.substring(14) + "\n";
                    FileUtils.writeStringToFile(outFile, score, true);
                }


            }
        } finally {
            LineIterator.closeQuietly(it);

        }
    }


    public List<String> getRecommendationsForUser(String user) throws TasteException {

        recommendationItems = recommender.recommend(userNumberID.get(user), 60);
       ArrayList<String> recommendations= new ArrayList<>();
        for (RecommendedItem recommendedItem :recommendationItems){
            recommendations.add(productsNumberID.inverse().get((int)recommendedItem.getItemID()));
        }

        return recommendations;

    }

    public int getProductNumberId(String productId) {
        return productsNumberID.get(productId);
    }
}
