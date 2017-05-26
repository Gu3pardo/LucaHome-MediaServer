package guepardoapps.games.common;

public class Coordinates {

    @SuppressWarnings("unused")
    private static final String TAG = Coordinates.class.getName();

    public int X;
    public int Y;

    public Coordinates(int x, int y) {
        X = x;
        Y = y;
    }

    @Override
    public String toString() {
        return "(" + X + "," + Y + ")";
    }
}
