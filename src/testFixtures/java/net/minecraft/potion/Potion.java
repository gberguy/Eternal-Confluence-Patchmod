package net.minecraft.potion;
public class Potion {
    public static Potion registered;
    public static Potion getPotionFromResourceLocation(String name) {
        return "tombstone:ghostly_shape".equals(name) ? registered : null;
    }
}
