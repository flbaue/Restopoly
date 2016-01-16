package io.github.flbaue.restopoly.directoryservice;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.github.flbaue.restopoly.directoryservice.model.Service;
import spark.Request;
import spark.Response;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.flbaue.restopoly.directoryservice.Constants.*;
import static spark.Spark.*;

/**
 * Created by florian on 14.01.16.
 */
public class DirectoryService {

    private static final Logger log = Logger.getLogger(DirectoryService.class.getName());

    private ServiceRepository repository = new ServiceRepository();
    private Gson gson = new Gson();
    private Timer cleanupThread;

    public DirectoryService(String[] args) {
        for (String arg : args) {
            if (arg.equals("--pretty")) {
                gson = new GsonBuilder().setPrettyPrinting().create();
            }
        }
    }

    public static void main(String[] args) {
        new DirectoryService(args).run();
    }

    private void run() {

        get(ROOT_PATH, this::root);
        get(ROOT_PATH_SLASH, this::root);

        post(SERVICES_PATH, APPLICATION_JSON, this::addService);
        post(SERVICES_PATH_SLASH, APPLICATION_JSON, this::addService);

        get(SERVICES_PATH, this::getServices, gson::toJson);
        get(SERVICES_PATH_SLASH, this::getServices, gson::toJson);

        get(SERVICE_PATH, this::getService, gson::toJson);
        get(SERVICE_PATH_SLASH, this::getService, gson::toJson);

        delete(SERVICE_PATH, this::removeService);
        delete(SERVICE_PATH_SLASH, this::removeService);

        get(PING_PATH, this::ping);

        after((request, response) -> {
            response.header("Content-Encoding", "gzip");
        });

        cleanupThread = new Timer("Service cleanup thread", true);
        cleanupThread.schedule(new TimerTask() {
            @Override
            public void run() {
                removeDeadServices();
            }
        }, 1000L * 60 * 5, 1000L * 60 * 5);
    }

    private Object removeService(Request request, Response response) {

        int id;
        try {
            id = Integer.parseInt(request.params(":id"));
        } catch (NumberFormatException e) {
            id = -1;
            log.log(Level.INFO, "Service ID '" + request.params(":id") + "'is not valid", e);
        }

        if (repository.removeService(id)) {
            response.status(200);
        } else {
            response.status(404);
        }
        return "";
    }

    private Object getService(Request request, Response response) {
        int id;
        try {
            id = Integer.parseInt(request.params(":id"));
        } catch (NumberFormatException e) {
            id = -1;
            log.log(Level.INFO, "Service ID '" + request.params(":id") + "'is not valid", e);
        }

        Service service = repository.getService(id);

        if (service != null) {
            response.status(200);
            response.type(APPLICATION_JSON);
            return service;
        } else {
            response.status(404);
            return "";
        }
    }

    private Object addService(Request request, Response response) {
        Service service = gson.fromJson(request.body(), Service.class);

        if (service != null) {
            service = repository.addService(service);
            String path = SERVICE_PATH.replace(":id", String.valueOf(service.id));
            response.status(201);
            response.header("Location", path);
        } else {
            response.status(400);
        }

        return "";
    }

    private Object getServices(Request request, Response response) {
        String serviceName = request.queryParams("name");
        String serviceType = request.queryParams("type");
        String serviceAuthor = request.queryParams("author");

        Set<Service> serviceSet;

        if (serviceName != null && !serviceName.isEmpty()) {
            serviceSet = repository.getServicesByName(serviceName);
        } else if (serviceType != null && !serviceType.isEmpty()) {
            serviceSet = repository.getServicesByType(serviceType);
        } else if (serviceAuthor != null && !serviceAuthor.isEmpty()) {
            serviceSet = repository.getServicesByAuthor(serviceAuthor);
        } else {
            serviceSet = repository.getAllServices();
        }

        response.status(200);
        response.type(APPLICATION_JSON);

        return serviceSet;
    }

    private Object ping(Request request, Response response) {
        response.status(200);
        return "pong";
    }

    private Object root(Request request, Response response) {
        return "Directory Service";
    }

    private void removeDeadServices() {
        Set<Service> services = repository.getAllServices();
        for (Service service : services) {
            String uri = service.baseUri + PING_PATH;
            HttpResponse<String> response = null;
            try {
                response = Unirest.get(uri).asString();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot ping service", e);
            }
            if (response == null || response.getStatus() != 200 || !response.getBody().equalsIgnoreCase("pong")) {
                repository.removeService(service.id);
            }
        }
    }

}
