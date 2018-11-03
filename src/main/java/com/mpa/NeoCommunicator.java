package com.mpa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.v1.*;


import java.util.*;

import static org.neo4j.driver.v1.Values.parameters;

@Slf4j
public class NeoCommunicator implements AutoCloseable{
    private final Driver driver;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    NeoCommunicator(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close()  {
        driver.close();
    }


    List<Employee> getEmployees() {

        List<Employee> response;

        final String query = "MATCH (a:Employee)" +
                "RETURN a";

        try (Session session = driver.session()) {
            response = session.readTransaction(tx -> {
                StatementResult result = tx.run(query,
                        parameters());

                return buildEmployeeList(result);
            });
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            response = Collections.emptyList();
        }
        return response;
    }

    boolean createEmployee(int id, String name) {

        boolean response = false;

        final String query = "Create (a:Employee {empId:$id, name:$name})";

        try (Session session = driver.session()) {
            response = session.readTransaction(tx -> {
                StatementResult result = tx.run(query,
                        parameters("id",id,"name",name));
                return true;

            });
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return response;
    }

     private List<Employee> buildEmployeeList(StatementResult result) {

        List<Employee> accountList = new ArrayList<>();
        while (result.hasNext()) {
            Record row = result.next();
            for (String key : result.keys()) {
                accountList.add(
                        new Employee(
                                Integer.parseInt(row.get(key).asNode().get("empId").toString().replace("\"", "")),
                                row.get(key).asNode().get("name").toString().replace("\"", "")
                        ));
            }
        }
        return accountList;
    }

}
