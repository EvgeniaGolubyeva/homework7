package auction.websockets;

import auction.entity.Bid;
import auction.entity.User;
import auction.service.BidService;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgenia
 */

@ServerEndpoint(
        value = "/server/product/{id}/bidws",
        decoders = { BidDecoder.class })
public class PlaceBidEndpoint {
    //since bid can be placed both through restful service and through websockets
    //placeBid is extracted to a separate class
    private BidService bidService;

    @OnMessage
    public void placeBid(@PathParam("id") int productId, Bid bid, Session mySession) {
        //place bid
        JsonObject result = bidService.placeBid(productId, bid);

        //if bid was added than notify all clients,
        //if amount < min or amount > reserved then notify one client (that placed request) only.
        if (BidService.RESULT_STATUS_ADDED.equals(result.getString("status"))) {
            mySession.getOpenSessions().forEach(session -> sendText(session, result.toString()));
        } else {
            sendText(mySession, result.toString());
        }
    }

    private void sendText(Session session, String text) {
        try {
            session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Inject
    public void setBidService(BidService bidService) {
        this.bidService = bidService;
    }
}
