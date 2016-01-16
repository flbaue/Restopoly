package io.github.flbaue.restopoly.directoryservice.model;

/**
 * Created by florian on 14.01.16.
 */
public class Service {
    public final int id;
    public String name;
    public String type;
    public String baseUri;
    public String author;
    public String comment;

    public Service(int id, Service service) {
        this.id = id;
        this.name = service.name;
        this.type = service.type;
        this.baseUri = service.baseUri;
        this.author = service.author;
        this.comment = service.comment;
    }
}
