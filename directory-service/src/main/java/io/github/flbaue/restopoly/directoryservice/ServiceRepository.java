package io.github.flbaue.restopoly.directoryservice;

import io.github.flbaue.restopoly.directoryservice.model.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by florian on 14.01.16.
 */
public class ServiceRepository {

    private Set<Service> serviceList = new HashSet<>();
    private AtomicInteger counter = new AtomicInteger(0);


    public Set<Service> getAllServices() {
        return Collections.unmodifiableSet(serviceList);
    }

    public Service addService(Service service) {
        Service newService = new Service(counter.getAndIncrement(), service);
        serviceList.add(newService);
        return newService;
    }

    public Service getService(int id) {
        return serviceList.stream()
                .filter(s -> s.id == id)
                .findAny()
                .orElse(null);
    }

    public Set<Service> getServicesByName(String name) {
        return serviceList.stream()
                .filter(s -> s.name.equals(name))
                .collect(Collectors.toSet());
    }

    public Set<Service> getServicesByType(String type) {
        return serviceList.stream()
                .filter(s -> s.type.equals(type))
                .collect(Collectors.toSet());
    }

    public Set<Service> getServicesByAuthor(String author) {
        return serviceList.stream()
                .filter(s -> s.author.equals(author))
                .collect(Collectors.toSet());
    }
}
