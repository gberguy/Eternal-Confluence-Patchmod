package net.minecraft.util;
public class ResourceLocation {
    private final String value;
    public ResourceLocation(String value) { this.value = value; }
    public String getNamespace() { return value.split(":")[0]; }
}
