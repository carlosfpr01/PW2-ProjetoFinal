package br.com.gateway;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class HealthResource {

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"API Gateway\"}";
    }
}
