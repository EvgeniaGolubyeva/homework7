package auction.database;

import auction.entity.Bid;
import auction.entity.Product;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Evgenia
 */


@Singleton
public class FakeDatabase {
    private List<Product> featuredProducts = new ArrayList<>();
    private List<Product> searchProducts = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();

    private List<Bid> bids = new ArrayList<>();

    @PostConstruct void init() {
        createFeaturedProducts();
        createSearchProducts();

        allProducts.addAll(featuredProducts);
        allProducts.addAll(searchProducts);
    }

    public List<Bid> getBids() {
        return bids;
    }

    public List<Product> getFeaturedProducts() {
        return featuredProducts;
    }

    public List<Product> getSearchProducts() {
        return searchProducts;
    }

    public Product getProductById(int id) {
        List<Product> products = allProducts.stream().filter(p -> p.getId() == id)
                                                     .collect(Collectors.toList());

        if (products.size() != 1)
            throw new IllegalStateException("There is none or more then one product with the same id");

        return products.get(0);
    }

    public List<Bid> getProductBids(Product product) {
        return bids.stream().filter(b -> b.getProduct().equals(product))
                            .sorted(Comparator.comparing(Bid::getAmount).reversed())
                            .collect(Collectors.toList());
    }

    private void createFeaturedProducts() {
        for (int i = 1; i < 7; i++) {
            featuredProducts.add(createProduct(i));
        }
    }

    private void createSearchProducts() {
        for (int i = 7; i < 13; i++) {
            searchProducts.add(createProduct(i));
        }
    }

    private Product createProduct(int i) {
        Product res = new Product();

        res.setId(i);
        res.setTitle("Item " + i);
        res.setThumb("images/" + (i < 10 ? "0" : "") + i + ".jpg");
        res.setDescription("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor " +
                           "incididunt ut labore et dolore adipiscing elit. Ut enim.");
        res.setAuctionEndTime(LocalDateTime.of(2015, 6, 23, 0, 0, 0));
        res.setWatchers(5);
        res.setMinimalPrice(new BigDecimal(50));
        res.setReservedPrice(new BigDecimal(120));

        return res;
    }
}
