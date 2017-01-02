package com.networknt.oauth.client.handler;

import com.networknt.config.Config;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Oauth2ClientGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ClientGetHandler.class);
    static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static final String sql = "SELECT * FROM clients";
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> client = new HashMap<>();
                    client.put("clientId", rs.getString("client_id"));
                    client.put("clientType", rs.getString("client_type"));
                    client.put("clientName", rs.getString("client_name"));
                    client.put("clientDesc", rs.getString("client_desc"));
                    client.put("scope", rs.getString("scope"));
                    client.put("redirectUrl", rs.getString("redirect_url"));
                    client.put("authenticateClass", rs.getString("authenticate_class"));
                    client.put("ownerId", rs.getString("owner_id"));
                    client.put("createDt", rs.getDate("create_dt"));
                    client.put("updateDt", rs.getDate("update_dt"));
                    result.add(client);
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw e;
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}