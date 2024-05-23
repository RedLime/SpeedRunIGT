package com.redlimerl.speedrunigt;

import org.apache.commons.lang3.Validate;

/**
 * The namespace and path must contain only lowercase letters ([a-z]), digits ([0-9]), or the characters '_', '.', and '-'. The path can also contain the standard path separator '/'.
 */
public class Identifier {
    private final String namespace;
    private final String path;

    public Identifier(String string, String string2) {
        Validate.notNull(string2);
        if (string != null && !string.isEmpty()) {
            this.namespace = string;
        } else {
            this.namespace = "minecraft";
        }

        this.path = string2;
    }

    public Identifier(String string) {
        String var2 = "minecraft";
        String var3 = string;
        int var4 = string.indexOf(58);
        if (var4 >= 0) {
            var3 = string.substring(var4 + 1);
            if (var4 > 1) {
                var2 = string.substring(0, var4);
            }
        }

        this.namespace = var2.toLowerCase();
        this.path = var3;
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Identifier)) {
            return false;
        } else {
            Identifier var2 = (Identifier)object;
            return this.namespace.equals(var2.namespace) && this.path.equals(var2.path);
        }
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }
}
