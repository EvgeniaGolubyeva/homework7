package auction.rest;

import auction.database.FakeDatabase;
import auction.entity.Bid;
import auction.entity.Product;
import auction.service.BidService;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Evgenia
 */

@Path("/product")
@Produces("application/json")
public class ProductService {
    private FakeDatabase database;

    //since bid can be placed both through restful service and through websockets
    //placeBid is extracted to a separate class
    private BidService bidService;

    @GET
    @Path("/featured")
    //since I never used "heading" in client code, it's better for me to return products without additional wrapping
    public List<Product> getFeaturedProducts() {
        return database.getFeaturedProducts();
    }

    @GET
    @Path("/search")
    //since I never used "heading" in client code, it's better for me to return products without additional wrapping
    public List<Product> getSearchProducts() {
        return database.getSearchProducts();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") int id) {
        return Response.ok(database.getProductById(id)).build();
    }

    @GET
    @Path("/{productId}/bid")
    public List<Bid> getProductBids(@PathParam("productId") int productId) {
        return database.getProductBids(database.getProductById(productId));
    }

    @POST
    @Consumes("application/json")
    @Path("/{id}/bid")
    public Response placeBid(@PathParam("id") int productId, Bid bid) {
        return Response.ok(bidService.placeBid(productId, bid)).build();
    }

    @Inject
    public void setDatabase(FakeDatabase database) {
        this.database = database;
    }

    @Inject
    public void setBidService(BidService bidService) {
        this.bidService = bidService;
    }
}
