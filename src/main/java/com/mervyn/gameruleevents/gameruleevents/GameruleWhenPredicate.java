package com.mervyn.gameruleevents.gameruleevents;

@FunctionalInterface
public interface GameruleWhenPredicate {
    GameruleWhenPredicate ANY = context -> true;

    boolean matches(GameruleMatchContext context);
}
