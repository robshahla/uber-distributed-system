package entities;

public class City {

    private final String name;
    private final double x, y;
    private final String shard;

    public City(String name, double x, double y, String shard) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.shard = shard;
    }

    public String getShard() {
        return shard;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }


}
