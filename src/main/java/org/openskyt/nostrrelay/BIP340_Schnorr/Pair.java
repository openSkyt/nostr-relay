package org.openskyt.nostrrelay.BIP340_Schnorr;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Pair<K, V> {

    private K elementLeft;
    private V elementRight;

    public static <K, V> Pair<K, V> of(K elementLeft, V elementRight) {
        return new Pair<>(elementLeft, elementRight);
    }

    public Pair(K elementLeft, V elementRight) {
        this.elementLeft = elementLeft;
        this.elementRight = elementRight;
    }

    public K getLeft() {
        return elementLeft;
    }

    public V getRight() {
        return elementRight;
    }

    public boolean equals(Pair<K, V> p) {
        return (this.elementLeft.equals(p.getLeft())) && (this.elementRight.equals(p.getRight()));
    }

}
