package entities;

import java.util.Map;
import java.util.Set;

public class ServerManager {


    /**
     * This server name.
     */
    private String server_name;
    /**
     * Saves All cities in the system.
     */
    private Set<City> cities;
    /**
     * Maps shard name to its server leader name
     */
    private Map<String, String> shard_leaders;
    /**
     * Represetns the name of the shards that this server is responsible for.
     */
    private Set<String> shards;

    private static ServerManager instance = null;

    private ServerManager() {
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }
}
