package com.netsim.domain;

/**
 * A single network interface on a {@link Router}, identified by the id
 * of the link it terminates.
 */
public record Interface(String linkId, String name) {
}
