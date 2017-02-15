package com.test.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/users")
    public class HelloWorld {

            @GET
            @Path("{year}/{month}/{day}")
            public Response getUserHistory(
                            @PathParam("year") int year,
                            @PathParam("month") int month,
                            @PathParam("day") int day) {

               String date = year + "/" + month + "/" + day;
               System.out.println ("date ---"+date);

               return Response.status(200)
                    .entity("getUserHistory is called, year/month/day : " + date)
                    .build();

            }

    }

