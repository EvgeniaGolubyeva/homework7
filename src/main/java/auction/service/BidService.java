package auction.service;

import auction.database.FakeDatabase;
import auction.entity.Bid;
import auction.entity.Product;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Evgenia
 */

@Singleton
public class BidService {
    public static final String RESULT_STATUS_REJECTED = "rejected";
    public static final String RESULT_STATUS_WIN = "win";
    public static final String RESULT_STATUS_ADDED = "added";

    private INotificationService notificationService;
    private FakeDatabase database;

    public JsonObject placeBid(int productId, Bid bid) {
        //TODO set bid id
        bid.setProduct(database.getProductById(productId));
        bid.setBidTime(LocalDateTime.now());

        BigDecimal min = bid.getProduct().getMinimalPrice();
        BigDecimal res = bid.getProduct().getReservedPrice();
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        if (bid.getAmount().compareTo(min) < 0) {
            notificationService.sendSorryNotification(bid);
            return jsonBuilder.add("status", RESULT_STATUS_REJECTED)
                    .add("reason", "Sorry! Bid was rejected. Amount you placed is less then product minPrice [" + min + "]")
                    .build();
        }

        if (bid.getAmount().compareTo(res) >= 0) {
            notificationService.sendWinNotification(bid);
            return jsonBuilder.add("status", RESULT_STATUS_WIN)
                    .add("reason", "Congratulations! You win! Amount you placed is more then product resPrice [" + res + "]")
                    .build();
        }

        database.getBids().add(bid);

        List<Bid> productBids = getBidsByProduct(bid.getProduct());
        notificationService.sendBidWasPlacedNotification(bid, productBids);
        notifyOverbidUsers(bid, productBids);

        Bid topBid = productBids.stream().max(Comparator.comparing(Bid::getAmount)).get();
        return jsonBuilder.add("status", RESULT_STATUS_ADDED)
                .add("topBidPrice", topBid.getAmount())
                .add("topBidUser", topBid.getUser().getId())
                .add("totalCountOfBids", productBids.size())
                .build();
    }

    @Inject
    public void setDatabase(FakeDatabase database) {
        this.database = database;
    }

    @Inject
    public void setNotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private void notifyOverbidUsers(Bid bid, List<Bid> productBids) {
        productBids.stream()
                .filter(b -> bid.getAmount().compareTo(b.getAmount()) > 0)
                .filter(b -> b.getUser().isGetOverbidNotifications())
                .forEach(b -> notificationService.sendOverbidNotification(b, bid));
    }

    private List<Bid> getBidsByProduct(Product product) {
        return database.getBids().stream()
                .filter(b -> product.equals(b.getProduct()))
                .collect(Collectors.toList());
    }
}
