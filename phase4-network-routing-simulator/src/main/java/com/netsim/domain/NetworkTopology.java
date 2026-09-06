package com.netsim.domain;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Aggregate root owning the set of Routers and Links for a named,
 * persistable network (FR-3). Exposes graph-query operations and
 * mediates all reads/writes to nodes and edges. Mutations are
 * synchronized so fault injection (FR-6) is safe under concurrent
 * simulation runs (see design doc 10.3).
 */
public class NetworkTopology {

    private final String id;
    private String name;
    private final Map<String, Router> routers = new ConcurrentHashMap<>();
    private final Map<String, Link> links = new ConcurrentHashMap<>();

    public NetworkTopology(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public synchronized Router addRouter(Router router) {
        routers.put(router.getId(), router);
        return router;
    }

    public synchronized void removeRouter(String routerId) {
        routers.remove(routerId);
        links.values().removeIf(l -> l.getSourceRouterId().equals(routerId) || l.getTargetRouterId().equals(routerId));
    }

    public synchronized Link addLink(Link link) {
        links.put(link.getId(), link);
        Router source = routers.get(link.getSourceRouterId());
        if (source != null) {
            source.addInterface(new Interface(link.getId(), link.getTargetRouterId()));
        }
        if (link.isBidirectional()) {
            Router target = routers.get(link.getTargetRouterId());
            if (target != null) {
                target.addInterface(new Interface(link.getId(), link.getSourceRouterId()));
            }
        }
        return link;
    }

    public synchronized void removeLink(String linkId) {
        Link link = links.remove(linkId);
        if (link == null) return;
        Optional.ofNullable(routers.get(link.getSourceRouterId())).ifPresent(r -> r.removeInterface(linkId));
        Optional.ofNullable(routers.get(link.getTargetRouterId())).ifPresent(r -> r.removeInterface(linkId));
    }

    public synchronized void setRouterStatus(String routerId, RouterStatus status) {
        Router router = requireRouter(routerId);
        router.setStatus(status);
    }

    public synchronized void setLinkStatus(String linkId, LinkStatus status) {
        Link link = requireLink(linkId);
        link.setStatus(status);
    }

    /** All links (in either direction) that are UP and touch {@code routerId}. */
    public List<Link> activeLinksFor(String routerId) {
        return links.values().stream()
                .filter(Link::isUp)
                .filter(l -> l.connects(routerId))
                .collect(Collectors.toList());
    }

    public List<String> neighborsOf(String routerId) {
        return activeLinksFor(routerId).stream()
                .map(l -> l.otherEnd(routerId))
                .filter(id -> {
                    Router r = routers.get(id);
                    return r != null && r.isReachable();
                })
                .toList();
    }

    public Optional<Link> linkBetween(String routerAId, String routerBId) {
        return links.values().stream()
                .filter(Link::isUp)
                .filter(l -> (l.getSourceRouterId().equals(routerAId) && l.getTargetRouterId().equals(routerBId))
                        || (l.isBidirectional() && l.getSourceRouterId().equals(routerBId) && l.getTargetRouterId().equals(routerAId)))
                .findFirst();
    }

    public Router requireRouter(String routerId) {
        Router r = routers.get(routerId);
        if (r == null) throw new NoSuchElementException("Unknown router: " + routerId);
        return r;
    }

    public Link requireLink(String linkId) {
        Link l = links.get(linkId);
        if (l == null) throw new NoSuchElementException("Unknown link: " + linkId);
        return l;
    }

    public Collection<Router> getRouters() {
        return routers.values();
    }

    public Collection<Link> getLinks() {
        return links.values();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
