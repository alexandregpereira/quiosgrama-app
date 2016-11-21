package io.oxigen.quiosgrama;

/**
 * Created by Alexandre on 02/08/2016.
 *
 */
public class Destination {

    public final int id;
    public final String name;
    public final String iconName;
    public final String printerIp;

    public Destination(ProductType type) {
        id = type.destination;
        name = type.destinationName;
        iconName = type.destinationIcon;
        printerIp = type.printerIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Destination that = (Destination) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
