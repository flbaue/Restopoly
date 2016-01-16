package io.github.flbaue.restopoly.directoryservice;


import com.google.gson.Gson;
import io.github.flbaue.restopoly.directoryservice.model.Service;
import spark.Request;
import spark.Response;

import java.util.Set;

import static io.github.flbaue.restopoly.directoryservice.Constants.*;
import static spark.Spark.*;

/**
 * Created by florian on 14.01.16.
 */
public class DirectoryService {

    private ServiceRepository controller = new ServiceRepository();
    private Gson gson = new Gson();

    public DirectoryService(String[] args) {

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


        after((request, response) -> {
            response.header("Content-Encoding", "gzip");
        });
    }

    private Object getService(Request request, Response response) {
        int id;
        try {
            id = Integer.parseInt(request.params(":id"));
        } catch (NumberFormatException e) {
            response.status(400);
            return "";
        }

        Service service = controller.getService(id);

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
            service = controller.addService(service);
            String path = SERVICE_PATH.replace(":id", String.valueOf(service.id));
            response.status(200);
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
            serviceSet = controller.getServicesByName(serviceName);
        } else if (serviceType != null && !serviceType.isEmpty()) {
            serviceSet = controller.getServicesByType(serviceType);
        } else if (serviceAuthor != null && !serviceAuthor.isEmpty()) {
            serviceSet = controller.getServicesByAuthor(serviceAuthor);
        } else {
            serviceSet = controller.getAllServices();
        }

        response.status(200);
        response.type(APPLICATION_JSON);

        return serviceSet;
    }

    private Object root(Request request, Response response) {
        return "Hello World";
    }

}
