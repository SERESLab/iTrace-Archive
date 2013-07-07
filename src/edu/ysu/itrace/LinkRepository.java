package edu.ysu.itrace;

import java.util.ArrayList;

public class LinkRepository {
    private static ArrayList<Link> links = new ArrayList<Link>();

    public static void add(Link link) {
        links.add(link);
    }

    public static void remove(Link link) {
        links.remove(link);
    }

    public static int size() {
        // TODO Auto-generated method stub
        return links.size();
    }
}
