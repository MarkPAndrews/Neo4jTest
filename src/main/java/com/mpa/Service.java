package com.mpa;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import spark.Filter;
import spark.Spark;

@Slf4j
public class Service {
    //private final static String neo_url = "bolt://34.228.233.64:7687";
    private final static String neo_url = "bolt://localhost:7687";
    private final static String neo_user = "neo4j";
    private final static String neo_pwd = "i-061fd58857cc3ead4";

    private  final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final HashMap<String, String> corsHeaders = new HashMap<>();

    static {
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
    }

    private static void apply() {
        Spark.staticFileLocation("/public");

        Filter filter = (request, response) -> corsHeaders.forEach(response::header);
        Spark.after(filter);
    }

    public static void main( String[] args) {

        Service.apply();

        get("/employee/list", (Request request, Response response) -> {
            response.status(200);

            response.type("application/json");

            List<Employee> empList;

            try ( NeoCommunicator communicator = new NeoCommunicator( neo_url, neo_user, neo_pwd ) )
            {
                empList = communicator.getEmployees();
            }

            return gson.toJson(empList);
        });

        post("/employee",(Request request, Response response) -> {
            response.status(200);

            response.type("application/json");
            log.info("Body:" + request.body());
            log.info("Parms:" + request.params());
            Employee emp = gson.fromJson(request.body(),Employee.class);

            boolean result;
            try ( NeoCommunicator communicator = new NeoCommunicator( neo_url, neo_user, neo_pwd ) )
            {
                result = communicator.createEmployee(emp.getId(),emp.getEmployee_name());
            }

            return gson.toJson(result);
        });
    }

    }
